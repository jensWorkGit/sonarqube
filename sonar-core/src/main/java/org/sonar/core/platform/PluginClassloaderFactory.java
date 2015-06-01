/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.core.platform;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.server.ServerSide;
import org.sonar.api.utils.TempFolder;
import org.sonar.classloader.ClassloaderBuilder;
import org.sonar.classloader.Mask;

import static org.sonar.classloader.ClassloaderBuilder.LoadingOrder.PARENT_FIRST;
import static org.sonar.classloader.ClassloaderBuilder.LoadingOrder.SELF_FIRST;

/**
 * Builds the graph of classloaders to be used to instantiate plugins. It deals with:
 * <ul>
 *   <li>isolation of plugins against core classes (except api)</li>
 *   <li>backward-compatibility with plugins built for versions of SQ lower than 5.2. At that time
 *   API declared transitive dependencies that were automatically available to plugins</li>
 *   <li>sharing of some packages between plugins</li>
 *   <li>loading of the libraries embedded in plugin JAR files (directory META-INF/libs)</li>
 * </ul>
 */
@BatchSide
@ServerSide
public class PluginClassloaderFactory {

  // underscores are used to not conflict with plugin keys (if someday a plugin key is "api")
  private static final String API_CLASSLOADER_KEY = "_api_";

  private final TempFolder temp;
  private URL compatibilityModeJar;

  public PluginClassloaderFactory(TempFolder temp) {
    this.temp = temp;
  }

  /**
   * Creates as many classloaders as requested by the input parameter.
   */
  public Map<PluginClassloaderDef, ClassLoader> create(Collection<PluginClassloaderDef> defs) {
    ClassloaderBuilder builder = new ClassloaderBuilder();
    builder.newClassloader(API_CLASSLOADER_KEY, baseClassloader());
    builder.setMask(API_CLASSLOADER_KEY, apiMask());

    for (PluginClassloaderDef def : defs) {
      builder.newClassloader(def.getBasePluginKey());
      builder.setParent(def.getBasePluginKey(), API_CLASSLOADER_KEY, new Mask());
      builder.setLoadingOrder(def.getBasePluginKey(), def.isSelfFirstStrategy() ? SELF_FIRST : PARENT_FIRST);
      for (File jar : def.getFiles()) {
        builder.addURL(def.getBasePluginKey(), fileToUrl(jar));
      }
      if (def.isCompatibilityMode()) {
        builder.addURL(def.getBasePluginKey(), extractCompatibilityModeJar());
      }
      exportResources(def, builder, defs);
    }

    return build(defs, builder);
  }

  /**
   * A plugin can export some resources to other plugins
   */
  private void exportResources(PluginClassloaderDef def, ClassloaderBuilder builder, Collection<PluginClassloaderDef> allPlugins) {
    // export the resources to all other plugins
    builder.setExportMask(def.getBasePluginKey(), def.getExportMask());
    for (PluginClassloaderDef other : allPlugins) {
      if (!other.getBasePluginKey().equals(def.getBasePluginKey())) {
        builder.addSibling(def.getBasePluginKey(), other.getBasePluginKey(), new Mask());
      }
    }
  }

  /**
   * Builds classloaders and verifies that all of them are correctly defined
   */
  private Map<PluginClassloaderDef, ClassLoader> build(Collection<PluginClassloaderDef> defs, ClassloaderBuilder builder) {
    Map<PluginClassloaderDef, ClassLoader> result = new HashMap<>();
    Map<String, ClassLoader> classloadersByBasePluginKey = builder.build();
    for (PluginClassloaderDef def : defs) {
      ClassLoader classloader = classloadersByBasePluginKey.get(def.getBasePluginKey());
      if (classloader == null) {
        throw new IllegalStateException(String.format("Fail to create classloader for plugin [%s]", def.getBasePluginKey()));
      }
      result.put(def, classloader);
    }
    return result;
  }

  ClassLoader baseClassloader() {
    return getClass().getClassLoader();
  }

  private URL extractCompatibilityModeJar() {
    if (compatibilityModeJar == null) {
      File jar = temp.newFile("sonar-plugin-api-deps", "jar");
      try {
        FileUtils.copyURLToFile(getClass().getResource("/sonar-plugin-api-deps.jar"), jar);
        compatibilityModeJar = jar.toURI().toURL();
      } catch (Exception e) {
        throw new IllegalStateException("Can not extract sonar-plugin-api-deps.jar to " + jar.getAbsolutePath(), e);
      }
    }
    return compatibilityModeJar;
  }

  private static URL fileToUrl(File file) {
    try {
      return file.toURI().toURL();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * The resources (packages) that API exposes to plugins. Other core classes (SonarQube, MyBatis, ...)
   * can't be accessed.
   * <p>To sum-up, these are the classes packaged in sonar-plugin-api.jar or available as
   * a transitive dependency of sonar-plugin-api</p>
   */
  private static Mask apiMask() {
    return new Mask()
      // inclusions
      .addInclusion("org/sonar/api/")
      .addInclusion("org/sonar/channel/")
      .addInclusion("org/sonar/check/")
      .addInclusion("org/sonar/colorizer/")
      .addInclusion("org/sonar/duplications/")
      .addInclusion("org/sonar/graph/")
      .addInclusion("org/sonar/plugins/emailnotifications/api/")
      .addInclusion("net/sourceforge/pmd/")
      .addInclusion("org/apache/maven/")
      .addInclusion("org/slf4j/")

      // exclusions
      .addExclusion("org/sonar/api/internal/");
  }
}