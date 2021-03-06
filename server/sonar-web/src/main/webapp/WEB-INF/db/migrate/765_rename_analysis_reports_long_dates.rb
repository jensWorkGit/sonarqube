#
# SonarQube, open source software quality management tool.
# Copyright (C) 2008-2016 SonarSource
# mailto:contact AT sonarsource DOT com
#
# SonarQube is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 3 of the License, or (at your option) any later version.
#
# SonarQube is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with this program; if not, write to the Free Software Foundation,
# Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
#

#
# SonarQube 5.1
#
class RenameAnalysisReportsLongDates < ActiveRecord::Migration

  def self.up
    remove_column 'analysis_reports', 'created_at'
    remove_column 'analysis_reports', 'updated_at'
    remove_column 'analysis_reports', 'started_at'
    remove_column 'analysis_reports', 'finished_at'
    rename_column 'analysis_reports', 'created_at_ms', 'created_at'
    rename_column 'analysis_reports', 'updated_at_ms', 'updated_at'
    rename_column 'analysis_reports', 'started_at_ms', 'started_at'
    rename_column 'analysis_reports', 'finished_at_ms', 'finished_at'
  end
end

