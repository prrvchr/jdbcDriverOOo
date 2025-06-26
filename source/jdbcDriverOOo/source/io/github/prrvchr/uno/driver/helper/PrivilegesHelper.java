/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-25 https://prrvchr.github.io                                  ║
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
package io.github.prrvchr.uno.driver.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.driver.config.ParameterDCL;
import io.github.prrvchr.uno.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.uno.driver.provider.ComposeRule;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.resultset.ResultSetHelper;
import io.github.prrvchr.uno.driver.resultset.RowSetData;
import io.github.prrvchr.uno.helper.UnoHelper;


public class PrivilegesHelper {

    public static String getGrantPrivilegesCommand(Provider provider,
                                                   NamedComponents table,
                                                   String privileges,
                                                   boolean isrole,
                                                   String grantee,
                                                   ComposeRule rule,
                                                   boolean sensitive)
        throws java.sql.SQLException, SQLException {
        Map<String, Object> arguments = ParameterDCL.getAlterPrivilegesArguments(provider, table, privileges,
                                                                                    isrole, grantee, rule, sensitive);
        String query = provider.getConfigDCL().getGrantPrivilegesCommand(arguments);
        System.out.println("DBPrivilegesHelper.getGrantPrivilegesCommand() SQL: " + query);
        return query;
    }

    public static String getRevokePrivilegesCommand(Provider provider,
                                                    NamedComponents table,
                                                    String privileges,
                                                    boolean isrole,
                                                    String grantee,
                                                    ComposeRule rule,
                                                    boolean sensitive)
        throws java.sql.SQLException, SQLException {
        Map<String, Object> arguments = ParameterDCL.getAlterPrivilegesArguments(provider, table, privileges,
                                                                                    isrole, grantee, rule, sensitive);
        String query = provider.getConfigDCL().getRevokePrivilegesCommand(arguments);
        System.out.println("DBPrivilegesHelper.getRevokePrivilegesCommand() SQL: " + query);
        return query;
    }

    public static java.sql.ResultSet getTablePrivilegesResultSet(Provider provider,
                                                                 java.sql.DatabaseMetaData md,
                                                                 String catalog,
                                                                 String schema,
                                                                 String table)
        throws java.sql.SQLException {
        java.sql.ResultSet result = null;
        if (provider.getConfigSQL().ignoreDriverPrivileges()) {
            String user = md.getUserName();
            System.out.println("PrivilegesHelper.getTablePrivilegesResultSet() 1 username: " + user);
            result = ResultSetHelper.getDefaultTablePrivilegesResultset(provider, catalog, schema, table, user);
        } else {
            System.out.println("PrivilegesHelper.getTablePrivilegesResultSet() 2");
            String[] columns = provider.getConfigSQL().getTablePrivilegesColumns();
            RowSetData data = provider.getConfigSQL().getTablePrivilegeData();
            result = ResultSetHelper.getTablePrivilegesResultset(md.getTablePrivileges(catalog, schema, table),
                                                                 columns, data);
        }
        return result;
    }

    /** get Privileges on a Table for the current connection.
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
    public static int getTablePrivileges(Provider provider,
                                         NamedComponents table)
        throws java.sql.SQLException {
        java.sql.DatabaseMetaData metadata = provider.getConnection().getMetaData();
        return getTablePrivileges(provider, metadata, table);
    }

    /** get Privileges on a Table for the current connection.
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
    public static int getTablePrivileges(Provider provider,
                                         java.sql.DatabaseMetaData metadata,
                                         NamedComponents table)
        throws java.sql.SQLException {
        int privileges = 0;
        final int PRIVILEGE = 6;
        try (java.sql.ResultSet result = getTablePrivilegesResultSet(provider, metadata, table.getCatalog(),
                                                                     table.getSchema(), table.getTable())) {
            while (result.next()) {
                String privilege = result.getString(PRIVILEGE);
                if (!result.wasNull()) {
                    privilege = privilege.toUpperCase().strip();
                    if (provider.getConfigDCL().hasPrivilege(privilege)) {
                        privileges |= provider.getConfigDCL().getPrivilege(privilege);
                    }
                }
            }
        }
        return privileges;
    }

    /** get Privileges on a Table for a grantee.
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
    public static int getTablePrivileges(Provider provider,
                                         String grantee,
                                         NamedComponents table,
                                         ComposeRule rule)
        throws java.sql.SQLException, SQLException {
        int privileges = 0;
        try {
            System.out.println("PrivilegesHelper.getTablePrivileges() 1");
            if (provider.getConfigDCL().supportsTablePrivileges()) {
                Map<String, Object> arguments = ParameterDCL.getPrivilegesArguments(grantee, table);
                List<Object> values = new ArrayList<>();
                String query = provider.getConfigDCL().getTablePrivilegesQuery(arguments, values);
                privileges = getPrivileges(provider, query, values);
                System.out.println("PrivilegesHelper.getTablePrivileges() 2 privileges: " + privileges);
            } else {
                privileges = provider.getConfigDCL().getMockPrivileges();
                System.out.println("PrivilegesHelper.getTablePrivileges() 3 privileges: " + privileges);
            }
        } catch (Exception e) {
            System.out.println("PrivilegesHelper.getPrivileges() ERROR: " + UnoHelper.getStackTrace(e));
        }
        return privileges;
    }

    /** get Grantable Privileges on a Table for a grantee.
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
    public static int getGrantablePrivileges(Provider provider,
                                             String grantee,
                                             NamedComponents table,
                                             ComposeRule rule)
        throws java.sql.SQLException, SQLException {
        int privileges;
        if (provider.getConfigDCL().supportsGrantablePrivileges()) {
            Map<String, Object> arguments = ParameterDCL.getPrivilegesArguments(grantee, table);
            List<Object> values = new ArrayList<>();
            String query = provider.getConfigDCL().getGrantablePrivilegesQuery(arguments, values);
            privileges = getPrivileges(provider, query, values);
        } else {
            privileges = provider.getConfigDCL().getMockPrivileges();
        }
        return privileges;
    }

    private static int getPrivileges(Provider provider,
                                     String query,
                                     List<Object> values)
        throws java.sql.SQLException, SQLException {
        int privileges = 0;
        System.out.println("PrivilegesHelper.getPrivileges() 1 query: " + query);
        try (java.sql.PreparedStatement statement = provider.getConnection().prepareStatement(query)) {
            setPreparedStatementParameter(statement, values);
            try (java.sql.ResultSet result = statement.executeQuery()) {
                java.sql.ResultSetMetaData metadata = result.getMetaData();
                int count = metadata.getColumnCount();
                while (result.next()) {
                    for (int i = 1; i <= count; i++) {
                        switch (metadata.getColumnType(i)) {
                            case java.sql.Types.INTEGER:
                            case java.sql.Types.BIGINT:
                                int value = result.getInt(i);
                                if (!result.wasNull() && value != 0) {
                                    privileges |= value;
                                }
                                break;
                            default:
                                String privilege = result.getString(i);
                                if (!result.wasNull()) {
                                    privilege = privilege.toUpperCase().strip();
                                    if (provider.getConfigDCL().hasPrivilege(privilege)) {
                                        privileges |= provider.getConfigDCL().getPrivilege(privilege);
                                    }
                                }
                        }
                    }
                }
            }
        }
        return privileges;
    }

    private static void setPreparedStatementParameter(java.sql.PreparedStatement statement, List<Object> values)
        throws java.sql.SQLException {
        int i = 1;
        for (Object value : values) {
            statement.setObject(i, value);
            i++;
        }
    }
}
