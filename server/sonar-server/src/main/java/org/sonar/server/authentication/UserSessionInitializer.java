/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.server.authentication;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.sonar.api.config.Settings;
import org.sonar.api.server.ServerSide;
import org.sonar.db.DbClient;
import org.sonar.db.user.UserDto;
import org.sonar.server.authentication.event.AuthenticationEvent;
import org.sonar.server.authentication.event.AuthenticationException;
import org.sonar.server.user.ServerUserSession;
import org.sonar.server.user.ThreadLocalUserSession;

import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.sonar.api.CoreProperties.CORE_FORCE_AUTHENTICATION_PROPERTY;
import static org.sonar.api.web.ServletFilter.UrlPattern;
import static org.sonar.api.web.ServletFilter.UrlPattern.Builder.staticResourcePatterns;
import static org.sonar.server.authentication.AuthenticationError.handleAuthenticationError;
import static org.sonar.server.authentication.event.AuthenticationEvent.Method;
import static org.sonar.server.authentication.event.AuthenticationEvent.Source;
import static org.sonar.server.authentication.ws.LoginAction.AUTH_LOGIN_URL;
import static org.sonar.server.authentication.ws.ValidateAction.AUTH_VALIDATE_URL;
import static org.sonar.server.user.ServerUserSession.createForAnonymous;
import static org.sonar.server.user.ServerUserSession.createForUser;

@ServerSide
public class UserSessionInitializer {

  /**
   * Key of attribute to be used for displaying user login
   * in logs/access.log. The pattern to be configured
   * in property sonar.web.accessLogs.pattern is "%reqAttribute{LOGIN}"
   */
  public static final String ACCESS_LOG_LOGIN = "LOGIN";

  // SONAR-6546 these urls should be get from WebService
  private static final Set<String> SKIPPED_URLS = ImmutableSet.of(
    "/batch/index", "/batch/file",
    "/maintenance/*",
    "/setup/*",
    "/sessions/*",
    "/api/system/db_migration_status", "/api/system/status", "/api/system/migrate_db",
    "/api/server/index", "/api/server/setup", "/api/server/version",
    AUTH_LOGIN_URL, AUTH_VALIDATE_URL);

  private static final UrlPattern URL_PATTERN = UrlPattern.builder()
    .includes("/*")
    .excludes(staticResourcePatterns())
    .excludes(SKIPPED_URLS)
    .build();

  private final DbClient dbClient;
  private final Settings settings;
  private final JwtHttpHandler jwtHttpHandler;
  private final BasicAuthenticator basicAuthenticator;
  private final SsoAuthenticator ssoAuthenticator;
  private final ThreadLocalUserSession threadLocalSession;
  private final AuthenticationEvent authenticationEvent;

  public UserSessionInitializer(DbClient dbClient, Settings settings, JwtHttpHandler jwtHttpHandler, BasicAuthenticator basicAuthenticator,
    SsoAuthenticator ssoAuthenticator, ThreadLocalUserSession threadLocalSession, AuthenticationEvent authenticationEvent) {
    this.dbClient = dbClient;
    this.settings = settings;
    this.jwtHttpHandler = jwtHttpHandler;
    this.basicAuthenticator = basicAuthenticator;
    this.ssoAuthenticator = ssoAuthenticator;
    this.threadLocalSession = threadLocalSession;
    this.authenticationEvent = authenticationEvent;
  }

  public boolean initUserSession(HttpServletRequest request, HttpServletResponse response) {
    String path = request.getRequestURI().replaceFirst(request.getContextPath(), "");
    try {
      // Do not set user session when url is excluded
      if (!URL_PATTERN.matches(path)) {
        return true;
      }
      setUserSession(request, response);
      return true;
    } catch (AuthenticationException e) {
      authenticationEvent.failure(request, e);
      if (isWsUrl(path)) {
        response.setStatus(HTTP_UNAUTHORIZED);
        return false;
      }
      if (isNotLocalOrJwt(e.getSource())) {
        // redirect to Unauthorized error page
        handleAuthenticationError(e, response);
        return false;
      }
      // Rails will redirect to login page
      return true;
    }
  }

  private static boolean isNotLocalOrJwt(Source source) {
    AuthenticationEvent.Provider provider = source.getProvider();
    return provider != AuthenticationEvent.Provider.LOCAL && provider != AuthenticationEvent.Provider.JWT;
  }

  private void setUserSession(HttpServletRequest request, HttpServletResponse response) {
    Optional<UserDto> user = authenticate(request, response);
    if (user.isPresent()) {
      ServerUserSession session = createForUser(dbClient, user.get());
      threadLocalSession.set(session);
      request.setAttribute(ACCESS_LOG_LOGIN, session.getLogin());
    } else {
      if (settings.getBoolean(CORE_FORCE_AUTHENTICATION_PROPERTY)) {
        throw AuthenticationException.newBuilder()
          .setSource(Source.local(Method.BASIC))
          .setMessage("User must be authenticated")
          .build();
      }
      threadLocalSession.set(createForAnonymous(dbClient));
      request.setAttribute(ACCESS_LOG_LOGIN, "-");
    }
  }

  public void removeUserSession() {
    threadLocalSession.unload();
  }

  // Try first to authenticate from SSO, then JWT token, then try from basic http header
  private Optional<UserDto> authenticate(HttpServletRequest request, HttpServletResponse response) {
    // SSO authentication should come first in order to update JWT if user from header is not the same is user from JWT
    Optional<UserDto> user = ssoAuthenticator.authenticate(request, response);
    if (user.isPresent()) {
      return user;
    }
    user = jwtHttpHandler.validateToken(request, response);
    if (user.isPresent()) {
      return user;
    }
    return basicAuthenticator.authenticate(request);
  }

  private static boolean isWsUrl(String path) {
    return path.startsWith("/batch/") || path.startsWith("/api/");
  }

}
