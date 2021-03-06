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
require('script!./third-party/jquery-ui.js');
require('script!./third-party/select2.js');
require('script!./third-party/keymaster.js');
require('script!./third-party/bootstrap/tooltip.js');
require('script!./third-party/bootstrap/dropdown.js');
require('script!./select2-jquery-ui-fix.js');
require('script!./inputs.js');
require('script!./jquery-isolated-scroll.js');
require('script!./application.js');
var request = require('../helpers/request');

window.$j = jQuery.noConflict();

jQuery(function () {
  jQuery('.open-modal').modal();
});

jQuery.ajaxSetup({
  beforeSend: function (jqXHR) {
    jqXHR.setRequestHeader(request.getCSRFTokenName(), request.getCSRFTokenValue());
  },
  statusCode: {
    401: function () {
      window.location = window.baseUrl + '/sessions/new?return_to=' +
          encodeURIComponent(window.location.pathname + window.location.search + window.location.hash);
    }
  }
});

window.sonarqube = {};
window.sonarqube.el = '#content';
