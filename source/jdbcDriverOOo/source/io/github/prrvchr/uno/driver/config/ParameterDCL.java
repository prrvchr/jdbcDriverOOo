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
package io.github.prrvchr.uno.driver.config;

import java.util.HashMap;
import java.util.Map;

import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.driver.helper.ComponentHelper;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedComponent;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedSupport;


public class ParameterDCL extends ParameterBase {

    public static Map<String, Object> getUserArguments(NamedSupport support,
                                                       String user,
                                                       String password,
                                                       boolean sensitive)
        throws java.sql.SQLException {
        Map<String, Object> arguments = new HashMap<>();
        // XXX: ${User} quoted / unquoted user name
        arguments.put("User", support.enquoteIdentifier(user, sensitive));
        arguments.put("RawUser", user);
        // XXX: ${Password} quoted password
        arguments.put("Password", support.enquoteLiteral(password));
        // XXX: ${RawPwd} unquoted password
        arguments.put("RawPwd", password);
        return arguments;
    }

    public static Map<String, Object> getGroupArguments(NamedSupport support,
                                                        String group,
                                                        boolean sensitive)
        throws java.sql.SQLException {
        Map<String, Object> arguments = new HashMap<>();
        // XXX: ${Role} quoted / unquoted user name
        arguments.put("Role", support.enquoteIdentifier(group, sensitive));
        arguments.put("RawRole", group);
        return arguments;
    }

    public static Map<String, Object> getPrivilegesArguments(NamedSupport support,
                                                             NamedComponent table,
                                                             String grantee)
        throws java.sql.SQLException {
        // These parameters do not need to be quoted, it will be used with a PreparedStatement
        Map<String, Object> arguments = new HashMap<>();
        // XXX: ${Grantee} unquoted grantee name
        arguments.put("Grantee", grantee);
        // XXX: ${TableName} unquoted full table name
        arguments.put("TableName", ComponentHelper.buildName(support, table));
        // XXX: ${Catalog} unquoted catalog name
        arguments.put("Catalog", table.getCatalogName());
        // XXX: ${Schema} unquoted schema name
        arguments.put("Schema", table.getSchemaName());
        // XXX: ${Table} unquoted table name
        arguments.put("Table",  table.getTableName());
        return arguments;
    }

    public static Map<String, Object> getAlterPrivilegesArguments(NamedSupport support,
                                                                  NamedComponent table,
                                                                  String privileges,
                                                                  boolean isrole,
                                                                  String grantee,
                                                                  boolean sensitive)
        throws java.sql.SQLException, SQLException {
        Map<String, Object> arguments = new HashMap<>();
        // XXX: ${Privileges} the list of privileges to revoke
        arguments.put("Privileges", privileges);
        // XXX: ${TableName} quoted / unquoted full qualified table name
        arguments.put("TableName", ComponentHelper.buildName(support, table, sensitive));
        // XXX: ${RoleType} literal (USER or ROLE)
        arguments.put("RoleType", getRole(isrole));
        // XXX: ${Grantee} quoted / unquoted grantee name
        arguments.put("Grantee", support.enquoteIdentifier(grantee, sensitive));
        return arguments;
    }

    public static Map<String, Object> getAlterRoleArguments(NamedSupport support,
                                                            String role1,
                                                            String role2,
                                                            boolean isrole,
                                                            String role,
                                                            boolean sensitive) {
        Map<String, Object> arguments = new HashMap<>();
        // XXX: ${Grantor} quoted / unquoted role name
        arguments.put("Grantor", support.enquoteIdentifier(role1, sensitive));
        // XXX: ${RoleType} unquoted literal
        if (role != null) {
            arguments.put("RoleType", role);
        } else {
            arguments.put("RoleType", getRole(isrole));
        }
        // XXX: ${Grantee} quoted / unquoted role name
        arguments.put("Grantee", support.enquoteIdentifier(role2, sensitive));
        return arguments;
    }

    private static String getRole(boolean isrole) {
        String role;
        if (isrole) {
            role = "ROLE";
        } else {
            role = "USER";
        }
        return role;
    }

}
