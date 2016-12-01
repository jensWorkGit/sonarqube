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
import { translate, translateWithParameters } from '../../../helpers/l10n';

export default class LoginForm extends React.Component {
  static propTypes = {
    allowUsersToSignUp: React.PropTypes.bool.isRequired,
    onSubmit: React.PropTypes.func.isRequired
  };

  state = {
    login: '',
    password: ''
  };

  handleSubmit = (e: any) => {
    e.preventDefault();
    this.props.onSubmit(this.state.login, this.state.password);
  };

  render () {
    return (
        <form id="login_form" onSubmit={this.handleSubmit}>
          <div className="big-spacer-bottom">
            <label htmlFor="login" className="login-label">{translate('login')}</label>
            <input type="text"
                   id="login"
                   name="login"
                   className="login-input"
                   maxLength="255"
                   required={true}
                   placeholder={translate('login')}
                   value={this.state.login}
                   onChange={e => this.setState({ login: e.target.value })}/>

            {this.props.allowUsersToSignUp && (
                <div className="note spacer-top spacer-left spacer-right">
                  {translateWithParameters('sessions.new_account', window.baseUrl + '/users/new')}
                </div>
            )}
          </div>

          <div className="big-spacer-bottom">
            <label htmlFor="password" className="login-label">{translate('password')}</label>
            <input type="password"
                   id="password"
                   name="password"
                   className="login-input"
                   required={true}
                   placeholder={translate('password')}
                   value={this.state.password}
                   onChange={e => this.setState({ password: e.target.value })}/>
          </div>

          <div>
            <div className="text-right overflow-hidden">
              <button name="commit" type="submit">{translate('sessions.log_in')}</button>
              <a className="spacer-left" href={window.baseUrl + '/'}>{translate('cancel')}</a>
            </div>
          </div>
        </form>
    );
  }
}
