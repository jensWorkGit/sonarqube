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
import Header from './Header';
import LoginFormContainer from './LoginFormContainer';

export default class App extends React.Component {
  componentDidMount () {
    document.querySelector('html').classList.add('dashboard-page');
  }

  componentWillUnmount () {
    document.querySelector('html').classList.remove('dashboard-page');
  }

  render () {
    return (
        <div>
          <Header/>

          <div id="bd" className="page-wrapper page-wrapper-simple">
            <div id="nonav" className="page-simple">
              <h1 className="maintenance-title text-center">Log In to SonarQube</h1>
              <LoginFormContainer location={this.props.location}/>
            </div>
          </div>
        </div>
    );
  }
}
