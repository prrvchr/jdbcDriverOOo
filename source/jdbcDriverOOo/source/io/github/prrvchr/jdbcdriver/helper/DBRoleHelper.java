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

import com.sun.star.beans.XPropertySet;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.PropertyIds;


public class DBRoleHelper
{

    /** creates a SQL CREATE USER statement
     *
     * @param connection
     *    The connection.
     * @param descriptor
     *    The descriptor of the new user.
     * @param name
     *    The name of the new user.
     * @param sensitive
     *    Is the name case sensitive.
     *   
     * @return
     *   The CREATE USER statement.
     * @throws java.sql.SQLException 
     */
    public static String getCreateUserQuery(DriverProvider provider,
                                            XPropertySet descriptor,
                                            String name,
                                            boolean sensitive)
        throws java.sql.SQLException
    {
        String password = DBTools.getDescriptorStringValue(descriptor, PropertyIds.PASSWORD);
        name = DBTools.enquoteIdentifier(provider, name, sensitive);
        password = provider.enquoteLiteral(password);
        String command = provider.getCreateUserQuery();
        return DBTools.formatSQLQuery(command, name, password);
    }

    /** creates a SQL DROP USER statement
     *
     * @param connection
     *    The connection.
     * @param name
     *    The name of the user.
     * @param sensitive
     *    Is the name case sensitive.
     *   
     * @return
     *   The DROP USER statement.
     * @throws java.sql.SQLException 
     */
    public static String getDropUserQuery(DriverProvider provider,
                                          String name,
                                          boolean sensitive)
        throws java.sql.SQLException
    {
        String command = provider.getSQLQuery(DBDefaultQuery.STR_QUERY_DROP_USER);
        return DBTools.formatSQLQuery(command, DBTools.enquoteIdentifier(provider, name, sensitive));
    }

    /** creates a SQL ALTER USER SET PASSWORD statement
     *
     * @param connection
     *    The connection.
     * @param name
     *    The name of the user.
     * @param password
     *    The new password of the user.
     * @param sensitive
     *    Is the name of user case sensitive.
     *   
     * @return
     *   The ALTER USER SET PASSWORD statement.
     * @throws java.sql.SQLException 
     */
    public static String getChangeUserPasswordQuery(DriverProvider provider,
                                                    String name,
                                                    String password,
                                                    boolean sensitive)
        throws java.sql.SQLException
    {
        name = DBTools.enquoteIdentifier(provider, name, sensitive);
        password = provider.enquoteLiteral(password);
        String command = provider.getSQLQuery(DBDefaultQuery.STR_QUERY_ALTER_USER);
        return DBTools.formatSQLQuery(command, name, password);
    }

    /** creates a SQL CREATE ROLE statement
     *
     * @param connection
     *    The connection.
     * @param descriptor
     *    The descriptor of the new group.
     * @param name
     *    The name of the new group.
     * @param sensitive
     *    Is the name case sensitive.
     *   
     * @return
     *   The CREATE ROLE statement.
     * @throws java.sql.SQLException 
     */
    public static String getCreateGroupQuery(DriverProvider provider,
                                             XPropertySet descriptor,
                                             String name,
                                             boolean sensitive)
        throws java.sql.SQLException
    {
        String command = provider.getSQLQuery(DBDefaultQuery.STR_QUERY_CREATE_ROLE);
        return DBTools.formatSQLQuery(command, DBTools.enquoteIdentifier(provider, name, sensitive));
    }

    /** creates a SQL DROP ROLE statement
     *
     * @param connection
     *    The connection.
     * @param name
     *    The name of the role.
     * @param sensitive
     *    Is the name case sensitive.
     *   
     * @return
     *   The DROP ROLE statement.
     * @throws java.sql.SQLException 
     */
    public static String getDropGroupQuery(DriverProvider provider,
                                           String name,
                                           boolean sensitive)
        throws java.sql.SQLException
    {
        String command = provider.getSQLQuery(DBDefaultQuery.STR_QUERY_DROP_ROLE);
        return DBTools.formatSQLQuery(command, DBTools.enquoteIdentifier(provider, name, sensitive));
    }

    /** creates a SQL GRANT ROLE statement
     *
     * @param connection
     *    The connection.
     * @param group
     *    The role.
     * @param user
     *    The role member user.
     * @param sensitive
     *    Is the role and user case sensitive.
     *   
     * @return
     *   The GRANT ROLE statement.
     * @throws SQLException
     * @throws java.sql.SQLException 
     */
    public static String getGrantRoleQuery(DriverProvider provider,
                                           String group,
                                           String user,
                                           boolean sensitive)
        throws java.sql.SQLException
    {
        group = DBTools.enquoteIdentifier(provider, group, sensitive);
        user = DBTools.enquoteIdentifier(provider, user, sensitive);
        String command = provider.getSQLQuery(DBDefaultQuery.STR_QUERY_GRANT_ROLE);
        return DBTools.formatSQLQuery(command, group, user);
    }

    /** creates a SQL REVOKE ROLE statement
     *
     * @param connection
     *    The connection.
     * @param group
     *    The role.
     * @param user
     *    The role member user.
     * @param sensitive
     *    Is the role and user case sensitive.
     *   
     * @return
     *   The REVOKE ROLE statement.
     * @throws SQLException
     * @throws java.sql.SQLException 
     */
    public static String getRevokeRoleQuery(DriverProvider provider,
                                            String group,
                                            String user,
                                            boolean sensitive)
        throws java.sql.SQLException
    {
        group = DBTools.enquoteIdentifier(provider, group, sensitive);
        user = DBTools.enquoteIdentifier(provider, user, sensitive);
        return DBTools.formatSQLQuery(provider.getRevokeRoleQuery(), group, user);
    }

}
