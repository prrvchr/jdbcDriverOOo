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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.driver.config.ConfigDCL;
import io.github.prrvchr.uno.driver.config.ParameterDCL;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedComponent;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedSupport;


public class PrivilegesHelper {

    public static String getGrantPrivilegesCommand(ConfigDCL config,
                                                   NamedSupport support,
                                                   NamedComponent table,
                                                   String privileges,
                                                   boolean isrole,
                                                   String grantee,
                                                   boolean sensitive)
        throws java.sql.SQLException, SQLException {
        Map<String, Object> arguments = ParameterDCL.getAlterPrivilegesArguments(support, table, privileges, isrole,
                                                                                 grantee, sensitive);
        return config.getGrantPrivilegesCommand(arguments);
    }

    public static String getRevokePrivilegesCommand(ConfigDCL config,
                                                    NamedSupport support,
                                                    NamedComponent table,
                                                    String privileges,
                                                    boolean isrole,
                                                    String grantee,
                                                    boolean sensitive)
        throws java.sql.SQLException, SQLException {
        Map<String, Object> arguments = ParameterDCL.getAlterPrivilegesArguments(support, table, privileges, isrole,
                                                                                 grantee, sensitive);
        return config.getRevokePrivilegesCommand(arguments);
    }

    /** get Privileges on a Table for a grantee.
    *
    * @param connection
    *    The java.sql.Connection.
    * @param support
    *    The named support of the table.
    * @param config
    *    The DCL configuration.
    * @param table
    *    The named components of the table.
    * @param grantee
    *    The grantee.
    *
    * @return
    *   The Privileges.
    * @throws java.sql.SQLException 
    */
    public static int getTablePrivileges(Connection connection,
                                         NamedSupport support,
                                         ConfigDCL config,
                                         String table,
                                         String grantee)
        throws java.sql.SQLException {
        int privileges = 0;
        if (config.supportsTablePrivileges()) {
            Map<String, Object> arguments = ParameterDCL.getPrivilegesArguments(support, table, grantee);
            List<Object> values = new ArrayList<>();
            String query = config.getTablePrivilegesQuery(arguments, values);
            privileges = getPrivileges(connection, config, query, values);
        } else {
            System.out.println("PrivilegesHelper.getTablePrivileges() MockPrivileges ********************");
            privileges = config.getMockPrivileges();
        }
        return privileges;
    }

    /** get Grantable Privileges on a Table for a grantee.
    *
    * @param connection
    *    The java.sql.Connection.
    * @param support
    *    The named support of the table.
    * @param config
    *    The DCL configuration.
    * @param table
    *    The named components of the table.
    * @param grantee
    *    The grantee.
    *
    * @return
    *   The Privileges.
    * @throws java.sql.SQLException 
    */
    public static int getGrantablePrivileges(Connection connection,
                                             NamedSupport support,
                                             ConfigDCL config,
                                             String table,
                                             String grantee)
        throws java.sql.SQLException {
        int privileges;
        if (config.supportsGrantablePrivileges()) {
            Map<String, Object> arguments = ParameterDCL.getPrivilegesArguments(support, table, grantee);
            List<Object> values = new ArrayList<>();
            String query = config.getGrantablePrivilegesQuery(arguments, values);
            privileges = getPrivileges(connection, config, query, values);
        } else {
            System.out.println("PrivilegesHelper.getGrantablePrivileges() MockPrivileges ********************");
            privileges = config.getMockPrivileges();
        }
        return privileges;
    }

    private static int getPrivileges(Connection connection,
                                     ConfigDCL config,
                                     String query,
                                     Collection<Object> values)
        throws java.sql.SQLException {
        int privileges = 0;
        try (PreparedStatement statement = getPrivilegeStatement(connection, query, values);
             java.sql.ResultSet result = statement.executeQuery()) {
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
                                if (config.hasPrivilege(privilege)) {
                                    privileges |= config.getPrivilege(privilege);
                                }
                            }
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw new java.sql.SQLException(e);
        }
        return privileges;
    }

    private static PreparedStatement getPrivilegeStatement(Connection connection,
                                                           String query,
                                                           Collection<Object> values)
        throws java.sql.SQLException {
        PreparedStatement stmt = connection.prepareStatement(query);
        if (values != null) {
            int index = 1;
            for (Object value : values) {
                stmt.setObject(index++, value);
            }
        }
        return stmt;
    }

}
