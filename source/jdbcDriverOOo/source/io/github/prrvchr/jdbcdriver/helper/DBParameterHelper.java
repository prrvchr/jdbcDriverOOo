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
package io.github.prrvchr.jdbcdriver.helper;

import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.helper.DBTools.NamedComponents;


public class DBParameterHelper
{

    public static String[] getPrivilegesArguments(DriverProvider provider,
                                                  String grantee,
                                                  NamedComponents table,
                                                  ComposeRule rule)
        throws java.sql.SQLException, SQLException
    {
        String[] arguments = new String[4];
        // XXX: {0} unquoted grantee name
        arguments[0] = grantee;
        // XXX: {1} unquoted catalog name
        arguments[1] = table.getCatalogName();
        // XXX: {2} unquoted schema name
        arguments[2] = table.getSchemaName();
        // XXX: {3} unquoted table name
        arguments[3] = table.getTableName();
        return arguments;
    }


    public static Object[] getAlterPrivilegesArguments(DriverProvider provider,
                                                       NamedComponents component,
                                                       String privileges,
                                                       boolean isrole,
                                                       String grantee,
                                                       ComposeRule rule,
                                                       boolean sensitive)
        throws java.sql.SQLException, SQLException
    {
        String[] arguments = new String[4];
        // XXX: {0} the list of privileges to revoke
        arguments[0] = privileges;
        // XXX: {1} quoted / unquoted full qualified table name
        arguments[1] = DBTools.buildName(provider, component, rule, sensitive);
        // XXX: {2} literal (USER or ROLE)
        arguments[2] = getRole(isrole);
        // XXX: {3} quoted / unquoted grantee name
        arguments[3] = DBTools.enquoteIdentifier(provider, grantee, sensitive);
        return arguments;
    }


    public static Object[] getAlterRoleArguments(DriverProvider provider,
                                                 String role1,
                                                 String role2,
                                                 boolean isrole,
                                                 String role,
                                                 boolean sensitive)
        throws java.sql.SQLException, SQLException
    {
        String[] arguments = new String[3];
        // XXX: {0} quoted / unquoted role name
        arguments[0] = DBTools.enquoteIdentifier(provider, role1, sensitive);
        // XXX: {1} unquoted literal
        arguments[1] = role != null ? role : getRole(isrole);
        // XXX: {2} quoted / unquoted role name
        arguments[2] = DBTools.enquoteIdentifier(provider, role2, sensitive);
        return arguments;
    }


    public static Object[] getRenameTableArguments(DriverProvider provider,
                                                   NamedComponents newtable,
                                                   NamedComponents oldtable,
                                                   String fullname,
                                                   boolean reversed,
                                                   ComposeRule rule,
                                                   boolean sensitive)
        throws java.sql.SQLException, SQLException
    {
        String[] arguments = new String[8];
        // XXX: {0} quoted / unquoted full old table name
        arguments[0] = DBTools.quoteTableName(provider, fullname, rule, sensitive);
        // XXX: {1} quoted / unquoted new schema name
        arguments[1] = DBTools.enquoteIdentifier(provider, newtable.getSchemaName(), sensitive);
        // XXX: {2} quoted / unquoted full old table name overwritten with the new schema name
        arguments[2] = DBTools.buildName(provider, oldtable.getCatalogName(), newtable.getSchemaName(), oldtable.getTableName(), rule, sensitive);
        // XXX: {3} quoted / unquoted new table name
        arguments[3] = DBTools.enquoteIdentifier(provider, newtable.getTableName(), sensitive);
        // XXX: {4} quoted / unquoted full old table name overwritten with the new table name
        arguments[4] = DBTools.buildName(provider, oldtable.getCatalogName(), oldtable.getSchemaName(), newtable.getTableName(), rule, sensitive);
        // XXX: {5} quoted / unquoted new catalog name
        arguments[5] = DBTools.enquoteIdentifier(provider, newtable.getCatalogName(), sensitive);
        // XXX: {6} quoted / unquoted full old table name overwritten with the new catalog name
        arguments[6] = DBTools.buildName(provider, newtable.getCatalogName(), oldtable.getSchemaName(), oldtable.getTableName(), rule, sensitive);
        // XXX: {7} quoted / unquoted full new table name
        arguments[7] = DBTools.buildName(provider, newtable.getCatalogName(), newtable.getSchemaName(), newtable.getTableName(), rule, sensitive);
        if (reversed) {
            String argument = arguments[0];
            arguments[0] = arguments[4];
            arguments[4] = arguments[2];
            arguments[2] = argument;
        }
        return arguments;
    }


    public static Object[] getAlterViewArguments(DriverProvider provider,
                                                 NamedComponents component,
                                                 String fullname,
                                                 String command,
                                                 ComposeRule rule,
                                                 boolean sensitive)
        throws java.sql.SQLException, SQLException
    {
        String[] arguments = new String[5];
        // XXX: {0} quoted / unquoted full view name
        arguments[0] = DBTools.quoteTableName(provider, fullname, rule, sensitive);
        // XXX: {1} quoted / unquoted catalog view name
        arguments[1] = DBTools.enquoteIdentifier(provider, component.getCatalogName(), sensitive);
        // XXX: {2} quoted / unquoted schema view name
        arguments[2] = DBTools.enquoteIdentifier(provider, component.getSchemaName(), sensitive);
        // XXX: {3} quoted / unquoted view name
        arguments[3] = DBTools.enquoteIdentifier(provider, component.getTableName(), sensitive);
        // XXX: {4} raw view command
        arguments[4] = command;
        return arguments;
    }


    public static Object[] getViewDefinitionArguments(DriverProvider provider,
                                                      NamedComponents component,
                                                      String fullname,
                                                      ComposeRule rule,
                                                      boolean sensitive)
        throws java.sql.SQLException, SQLException
    {
        String[] arguments = new String[4];
        // XXX: {0} quoted / unquoted  full view name
        arguments[0] = DBTools.quoteTableName(provider, fullname, rule, sensitive);
        // XXX: {1} quoted / unquoted  catalog view name
        arguments[1] = DBTools.enquoteIdentifier(provider, component.getCatalogName(), sensitive);
        // XXX: {2} quoted / unquoted  schema view name
        arguments[2] = DBTools.enquoteIdentifier(provider, component.getSchemaName(), sensitive);
        // XXX: {3} quoted / unquoted  view name
        arguments[3] = DBTools.enquoteIdentifier(provider, component.getTableName(), sensitive);
        return arguments;
    }

    private static String getRole(boolean isrole)
    {
        return isrole ? "ROLE" : "USER";
    }

}
