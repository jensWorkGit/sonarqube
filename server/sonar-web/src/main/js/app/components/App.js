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
import GlobalLoading from './GlobalLoading';
import { fetchCurrentUser } from '../store/users/actions';
import { fetchLanguages, fetchAppState } from '../store/rootActions';
import { requestMessages } from '../../helpers/l10n';
import { getAppState } from '../store/rootReducer';

class App extends React.Component {
  static propTypes = {
    appState: React.PropTypes.object.isRequired,
    fetchAppState: React.PropTypes.func.isRequired,
    fetchCurrentUser: React.PropTypes.func.isRequired,
    fetchLanguages: React.PropTypes.func.isRequired,
    children: React.PropTypes.element.isRequired
  };

  state = {
    loading: true
  };

  componentDidMount () {
    Promise.all([
      requestMessages(),
      this.props.fetchAppState(),
      this.props.fetchCurrentUser()
    ]).then(() => this.setState({ loading: false }));

    this.props.fetchLanguages();
  }

  render () {
    if (this.state.loading) {
      return <GlobalLoading/>;
    }

    return this.props.children;
  }
}

const mapStateToProps = state => ({
  appState: getAppState(state)
});

export default connect(
    mapStateToProps,
    { fetchAppState, fetchCurrentUser, fetchLanguages }
)(App);
