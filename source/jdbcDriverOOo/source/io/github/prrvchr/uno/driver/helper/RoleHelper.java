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

import java.util.Map;

import com.sun.star.beans.XPropertySet;

import io.github.prrvchr.uno.driver.config.ConfigDCL;
import io.github.prrvchr.uno.driver.config.ParameterDCL;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedSupport;
import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.driver.provider.DBTools;


public class RoleHelper {

    /** creates a SQL CREATE USER statement.
     *
     * @param config
     *    The DCL configuration.
     * @param support
     *    The named component support.
     * @param descriptor
     *    The descriptor of the new user.
     * @param user
     *    The name of the new user.
     * @param sensitive
     *    Is the name case sensitive.
     *   
     * @return
     *   The CREATE USER statement.
     * @throws java.sql.SQLException 
     */
    public static String getCreateUserCommand(ConfigDCL config,
                                              NamedSupport support,
                                              XPropertySet descriptor,
                                              String user,
                                              boolean sensitive)
        throws java.sql.SQLException {
        String password = DBTools.getDescriptorStringValue(descriptor, PropertyID.PASSWORD);
        Map<String, Object> arguments = ParameterDCL.getUserArguments(support, user, password, sensitive);
        return config.getCreateUserCommand(arguments);
    }

    /** creates a SQL DROP USER statement.
     *
     * @param config
     *    The DCL configuration.
     * @param support
     *    The named component support.
     * @param name
     *    The name of the user.
     * @param sensitive
     *    Is the name case sensitive.
     *   
     * @return
     *   The DROP USER statement.
     * @throws java.sql.SQLException 
     */
    public static String getDropUserCommand(ConfigDCL config,
                                            NamedSupport support,
                                            String name,
                                            boolean sensitive)
        throws java.sql.SQLException {
        name = support.enquoteIdentifier(name, sensitive);
        return config.getDropUserCommand(name);
    }

    /** creates a SQL ALTER USER SET PASSWORD statement.
     *
     * @param config
     *    The DCL configuration.
     * @param support
     *    The named component support.
     * @param user
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
    public static String getChangeUserPasswordCommand(ConfigDCL config,
                                                      NamedSupport support,
                                                      String user,
                                                      String password,
                                                      boolean sensitive)
        throws java.sql.SQLException {
        Map<String, Object> arguments = ParameterDCL.getUserArguments(support, user, password, sensitive);
        return config.getAlterUserCommand(arguments);
    }

    /** creates a SQL CREATE ROLE statement.
     *
     * @param config
     *    The DCL configuration.
     * @param support
     *    The named component support.
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
    public static String getCreateGroupCommand(ConfigDCL config,
                                               NamedSupport support,
                                               XPropertySet descriptor,
                                               String name,
                                               boolean sensitive)
        throws java.sql.SQLException {
        Map<String, Object> arguments = ParameterDCL.getGroupArguments(support, name, sensitive);
        return config.getCreateRoleCommand(arguments);
    }

    /** creates a SQL DROP ROLE statement.
     *
     * @param config
     *    The DCL configuration.
     * @param support
     *    The named component support.
     * @param name
     *    The name of the role.
     * @param sensitive
     *    Is the name case sensitive.
     *   
     * @return
     *   The DROP ROLE statement.
     * @throws java.sql.SQLException 
     */
    public static String getDropGroupCommand(ConfigDCL config,
                                             NamedSupport support,
                                             String name,
                                             boolean sensitive)
        throws java.sql.SQLException {
        Map<String, Object> arguments = ParameterDCL.getGroupArguments(support, name, sensitive);
        return config.getDropRoleCommand(arguments);
    }

    /** creates a SQL GRANT ROLE statement.
     *
     * @param config
     *    The DCL configuration.
     * @param support
     *    The named component support.
     * @param grantor
     *    The granted role.
     * @param role
     *    The grantee type (ROLE or USER).
     * @param grantee
     *    The role member.
     * @param sensitive
     *    Is the role and user case sensitive.
     *   
     * @return
     *   The GRANT ROLE statement.
     * @throws java.sql.SQLException 
     */
    public static String getGrantRoleCommand(ConfigDCL config,
                                             NamedSupport support,
                                             String grantor,
                                             String role,
                                             String grantee,
                                             boolean sensitive)
        throws java.sql.SQLException {
        grantor = support.enquoteIdentifier(grantor, sensitive);
        grantee = support.enquoteIdentifier(grantee, sensitive);
        return config.getGrantRoleCommand(grantor, role, grantee);
    }

    /** creates a SQL REVOKE ROLE statement.
     *
     * @param config
     *    The DCL configuration.
     * @param support
     *    The named component support.
     * @param grantor
     *    The granted role.
     * @param role
     *    The grantee type (ROLE or USER).
     * @param grantee
     *    The role member.
     * @param sensitive
     *    Is the role and user case sensitive.
     *   
     * @return
     *   The REVOKE ROLE statement.
     * @throws java.sql.SQLException 
     */
    public static String getRevokeRoleCommand(ConfigDCL config,
                                              NamedSupport support,
                                              String grantor,
                                              String role,
                                              String grantee,
                                              boolean sensitive)
        throws java.sql.SQLException {
        grantor = support.enquoteIdentifier(grantor, sensitive);
        grantee = support.enquoteIdentifier(grantee, sensitive);
        return config.getRevokeRoleCommand(grantor, role, grantee);
    }

}
