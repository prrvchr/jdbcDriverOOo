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
package io.github.prrvchr.driver.query;

import java.util.HashMap;
import java.util.Map;

import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.driver.helper.DBTools;
import io.github.prrvchr.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.driver.provider.ComposeRule;
import io.github.prrvchr.driver.provider.DriverProvider;


public class DCLParameter extends ParameterBase {

    public static Map<String, Object> getPrivilegesArguments(DriverProvider provider,
                                                             String grantee,
                                                             NamedComponents table,
                                                             ComposeRule rule)
        throws java.sql.SQLException, SQLException {
        Map<String, Object> arguments = new HashMap<>();
        // XXX: ${Grantee} quoted / unquoted grantee name
        arguments.put("Grantee", grantee);
        // XXX: ${Catalog} quoted / unquoted catalog name
        arguments.put("Catalog", table.getCatalogName());
        // XXX: ${Schema} quoted / unquoted schema name
        arguments.put("Schema", table.getSchemaName());
        // XXX: ${Table} quoted / unquoted table name
        arguments.put("Table",  table.getTableName());
        return arguments;
    }

    public static Map<String, Object> getAlterPrivilegesArguments(DriverProvider provider,
                                                                  NamedComponents table,
                                                                  String privileges,
                                                                  boolean isrole,
                                                                  String grantee,
                                                                  ComposeRule rule,
                                                                  boolean sensitive)
        throws java.sql.SQLException, SQLException {
        Map<String, Object> arguments = new HashMap<>();
        // XXX: ${Privileges} the list of privileges to revoke
        arguments.put("Privileges", privileges);
        // XXX: ${TableName} quoted / unquoted full qualified table name
        arguments.put("TableName", DBTools.buildName(provider, table, rule, sensitive));
        // XXX: ${RoleType} literal (USER or ROLE)
        arguments.put("RoleType", getRole(isrole));
        // XXX: ${Grantee} quoted / unquoted grantee name
        arguments.put("Grantee", provider.enquoteIdentifier(grantee, sensitive));
        return arguments;
    }

    public static Map<String, Object> getAlterRoleArguments(DriverProvider provider,
                                                            String role1,
                                                            String role2,
                                                            boolean isrole,
                                                            String role,
                                                            boolean sensitive)
        throws java.sql.SQLException, SQLException {
        Map<String, Object> arguments = new HashMap<>();
        // XXX: ${Grantor} quoted / unquoted role name
        arguments.put("Grantor", provider.enquoteIdentifier(role1, sensitive));
        // XXX: ${RoleType} unquoted literal
        if (role != null) {
            arguments.put("RoleType", role);
        } else {
            arguments.put("RoleType", getRole(isrole));
        }
        // XXX: ${Grantee} quoted / unquoted role name
        arguments.put("Grantee", provider.enquoteIdentifier(role2, sensitive));
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
