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
import { connect } from 'react-redux';
import LoginForm from './LoginForm';
import { doLogin } from '../../../app/store/rootActions';

class LoginFormContainer extends React.Component {
  handleSubmit = (login: string, password: string) => {
    this.props.doLogin(login, password).then(() => {
      window.location = this.props.location.query['return_to'] || (window.baseUrl + '/');
    });
  };

  render () {
    // FIXME allowUsersToSignUp
    return (
        <LoginForm allowUsersToSignUp={false} onSubmit={this.handleSubmit}/>
    );
  }
}

const mapStateToProps = () => ({});

const mapDispatchToProps = { doLogin };

export default connect(mapStateToProps, mapDispatchToProps)(LoginFormContainer);
