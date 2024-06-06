/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-24 https://prrvchr.github.io                                  ║
║                                                                                    ║
║   Permission is hereby granted, free of charge, to any person obtaining            ║
║   a copy of this software and associated documentation files (the "Software"),     ║
║   to deal in the Software without restriction, including without limitation        ║
║   the rights to use, copy, modify, merge, publish, distribute, sublicense,         ║
║   and/or sell copies of the Software, and to permit persons to whom the Software   ║
║   is furnished to do so, subject to the following conditions:                      ║
║                                                                                    ║
║   The above copyright notice and this permission notice shall be included in       ║
║   all copies or substantial portions of the Software.                              ║
║                                                                                    ║
║   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,                  ║
║   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES                  ║
║   OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.        ║
║   IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY             ║
║   CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,             ║
║   TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE       ║
║   OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                                    ║
║                                                                                    ║
╚════════════════════════════════════════════════════════════════════════════════════╝
*/
/**************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 *************************************************************/
package io.github.prrvchr.jdbcdriver.helper;

import java.util.ArrayList;
import java.util.List;

import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.helper.DBTools.NamedComponents;
import io.github.prrvchr.jdbcdriver.metadata.TablePrivilegesResultSet;
import io.github.prrvchr.jdbcdriver.metadata.TablePrivilegesResultSetBase;


public class DBPrivilegesHelper
{

    public static String getGrantPrivilegesQuery(DriverProvider provider,
                                                 NamedComponents table,
                                                 String privileges,
                                                 boolean isrole,
                                                 String grantee,
                                                 ComposeRule rule,
                                                 boolean sensitive)
        throws java.sql.SQLException, SQLException
    {
        Object[] arguments = DBParameterHelper.getAlterPrivilegesArguments(provider, table, privileges, isrole, grantee, rule, sensitive);
        String query = provider.getGrantPrivilegesQuery(arguments);
        System.out.println("DBPrivilegesHelper.getGrantPrivilegesQuery() SQL: " + query);
        return query;
    }

    public static String getRevokePrivilegesQuery(DriverProvider provider,
                                                  NamedComponents table,
                                                  String privileges,
                                                  boolean isrole,
                                                  String grantee,
                                                  ComposeRule rule,
                                                  boolean sensitive)
        throws java.sql.SQLException, SQLException
    {
        Object[] arguments = DBParameterHelper.getAlterPrivilegesArguments(provider, table, privileges, isrole, grantee, rule, sensitive);
        String query = provider.getRevokePrivilegesQuery(arguments);
        System.out.println("DBPrivilegesHelper.getRevokePrivilegesQuery() SQL: " + query);
        return query;
    }


    public static java.sql.ResultSet getTablePrivilegesResultSet(DriverProvider provider,
                                                                 java.sql.DatabaseMetaData metadata,
                                                                 String catalog,
                                                                 String schema,
                                                                 String table)
        throws java.sql.SQLException
    {
        java.sql.ResultSet result = null;
        try {
            if (provider.ignoreDriverPrivileges()) {
                result = metadata.getTables(catalog, schema, table, null);
                result = new TablePrivilegesResultSet(result, provider.getPrivileges(), metadata.getUserName());
            }
            else {
                result = metadata.getTablePrivileges(catalog, schema, table);
                // XXX: We have to check the result columns for the tables privileges #106324#
                if (result != null && result.getMetaData().getColumnCount() != 7) {
                    // XXX: Here we know that the count of column doesn't match
                    result = new TablePrivilegesResultSetBase(result);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /** get Privileges on a Table for the current connection
    *
    * @param provider
    *    The driver provider.
    * @param table
    *    The named components of the table.
    *   
    * @return
    *   The Privileges.
    * @throws java.sql.SQLException 
    */
    public static int getTablePrivileges(DriverProvider provider,
                                         NamedComponents table)
    throws java.sql.SQLException
    {
        java.sql.DatabaseMetaData metadata = provider.getConnection().getMetaData();
        return getTablePrivileges(provider, metadata, table);
    }

    /** get Privileges on a Table for the current connection
    *
    * @param provider
    *    The driver provider.
    * @param metadata
    *    The database metadata.
    * @param table
    *    The named components of the table.
    *   
    * @return
    *   The Privileges.
    * @throws java.sql.SQLException 
    */
    public static int getTablePrivileges(DriverProvider provider,
                                         java.sql.DatabaseMetaData metadata,
                                         NamedComponents table)
    throws java.sql.SQLException
    {
        int privileges = 0;
        try (java.sql.ResultSet result = getTablePrivilegesResultSet(provider, metadata, table.getCatalog(), table.getSchema(), table.getTable()))
        {
            while (result.next()) {
                String privilege = result.getString(6);
                if (!result.wasNull()) {
                    privilege = privilege.toUpperCase().strip();
                    if (provider.hasPrivilege(privilege)) {
                        privileges |= provider.getPrivilege(privilege);
                    }
                }
            }
        }
        return privileges;
    }

    /** get Privileges on a Table for a grantee
    *
    * @param provider
    *    The driver provider.
    * @param grantee
    *    The grantee.
    * @param table
    *    The named components of the table.
    * @param rule
    *    The composition rule applying to the naming of the table.
    *   
    * @return
    *   The Privileges.
    * @throws SQLException
    * @throws java.sql.SQLException 
    */
    public static int getTablePrivileges(DriverProvider provider,
                                         String grantee,
                                         NamedComponents table,
                                         ComposeRule rule)
    throws java.sql.SQLException, SQLException
    {
        List<Integer[]> positions = new ArrayList<>();
        String query = provider.getTablePrivilegesQuery(positions);
        if (query == null) {
            return provider.getMockPrivileges();
        }
        return getPrivileges(provider, grantee, positions.get(0), query, table, rule);
    }


    /** get Grantable Privileges on a Table for a grantee
    *
    * @param provider
    *    The driver provider.
    * @param grantee
    *    The grantee.
    * @param table
    *    The named components of the table.
    * @param rule
    *    The composition rule applying to the naming of the table.
    *   
    * @return
    *   The Privileges.
    * @throws SQLException
    * @throws java.sql.SQLException 
    */
    public static int getGrantablePrivileges(DriverProvider provider,
                                             String grantee,
                                             NamedComponents table,
                                             ComposeRule rule)
    throws java.sql.SQLException, SQLException
    {
        List<Integer[]> positions = new ArrayList<>();
        String query = provider.getGrantablePrivilegesQuery(positions);
        if (query == null) {
            return provider.getMockPrivileges();
        }
        return getPrivileges(provider, grantee, positions.get(0), query, table, rule);
    }

    private static int getPrivileges(DriverProvider provider,
                                     String grantee,
                                     Integer[] positions,
                                     String query,
                                     NamedComponents table,
                                     ComposeRule rule)
    throws java.sql.SQLException, SQLException
    {
        String[] arguments = DBParameterHelper.getPrivilegesArguments(provider, grantee, table, rule);
        return getPrivileges(provider, arguments, positions, query);
    }


    private static int getPrivileges(DriverProvider provider,
                                     String[] arguments,
                                     Integer[] positions,
                                     String query)
        throws java.sql.SQLException, SQLException
    {
        int privileges = 0;
        try (java.sql.PreparedStatement statement = provider.getConnection().prepareStatement(query))
        {
            DBTools.setStatementParameters(statement, arguments, positions);
            try (java.sql.ResultSet result = statement.executeQuery())
            {
                java.sql.ResultSetMetaData metadata = result.getMetaData();
                int count = metadata.getColumnCount();
                while (result.next()) {
                    for (int i = 1; i <= count; i++) {
                        switch (metadata.getColumnType(i)) {
                        case java.sql.Types.INTEGER:
                            int value = result.getInt(i);
                            if (!result.wasNull() && value != 0) {
                                privileges |= value;
                            }
                            break;
                        default:
                            String privilege = result.getString(i);
                            if (!result.wasNull()) {
                                privilege = privilege.toUpperCase().strip();
                                if (provider.hasPrivilege(privilege)) {
                                    privileges |= provider.getPrivilege(privilege);
                                }
                            }
                        }
                    }
                }
            }
        }
        return privileges;
    }

}
