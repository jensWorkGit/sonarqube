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
// @flow
import React from 'react';
import GlobalFooter from './GlobalFooter';

export default class SimpleContainer extends React.Component {
  static propTypes = {
    children: React.PropTypes.element.isRequired
  };

  componentDidMount () {
    document.querySelector('html').classList.add('dashboard-page');
  }

  componentWillUnmount () {
    document.querySelector('html').classList.remove('dashboard-page');
  }

  render () {
    return (
        <div className="global-container">
          <div className="page-wrapper page-wrapper-global" id="container">
            <nav className="navbar navbar-global page-container" id="global-navigation">
              <div className="navbar-header"></div>
            </nav>

            <div id="bd" className="page-wrapper-simple">
              <div id="nonav" className="page-simple">
                {this.props.children}
              </div>
            </div>
          </div>
          <GlobalFooter/>
        </div>
    );
  }
}
