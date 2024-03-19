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
package io.github.prrvchr.jdbcdriver;

import java.util.ArrayList;
import java.util.List;

import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.jdbcdriver.DBTools.NameComponents;

public class DBParameterHelper
{


    public static Object[] getRenameTableArguments(DriverProvider provider,
                                                   NameComponents newname,
                                                   String catalog,
                                                   String schema,
                                                   String table,
                                                   String fullname,
                                                   boolean reversed,
                                                   ComposeRule rule,
                                                   boolean sensitive)
        throws java.sql.SQLException, SQLException
    {
        List<String> args = new ArrayList<>();
        // TODO: {0} quoted / unquoted full old table name
        args.add(DBTools.quoteTableName(provider, fullname, rule, sensitive));
        // TODO: {1} quoted / unquoted new schema name
        args.add(DBTools.enquoteIdentifier(provider, newname.getSchema(), sensitive));
        // TODO: {2} quoted / unquoted full old table name overwritten with the new schema name
        args.add(DBTools.buildName(provider, catalog, newname.getSchema(), table, rule, sensitive));
        // TODO: {3} quoted / unquoted new table name
        args.add(DBTools.enquoteIdentifier(provider, newname.getTable(), sensitive));
        // TODO: {4} quoted / unquoted full old table name overwritten with the new table name
        args.add(DBTools.buildName(provider, catalog, schema, newname.getTable(), rule, sensitive));
        // TODO: {5} quoted / unquoted new catalog name
        args.add(DBTools.enquoteIdentifier(provider, newname.getCatalog(), sensitive));
        // TODO: {6} quoted / unquoted full old table name overwritten with the new catalog name
        args.add(DBTools.buildName(provider, newname.getCatalog(), schema, table, rule, sensitive));
        // TODO: {7} quoted / unquoted full new table name
        args.add(DBTools.buildName(provider, newname.getCatalog(), newname.getSchema(), newname.getTable(), rule, sensitive));
        if (reversed) {
            String buffers = args.get(0);
            args.set(0, args.get(4));
            args.set(4, args.get(2));
            args.set(2, buffers);
        }
        return args.toArray(new Object[0]);
    }


    public static Object[] getAlterViewArguments(DriverProvider provider,
                                                 NameComponents component,
                                                 String fullname,
                                                 String command,
                                                 ComposeRule rule,
                                                 boolean sensitive)
        throws java.sql.SQLException, SQLException
    {
        List<String> args = new ArrayList<>();
        // TODO: {0} quoted / unquoted full view name
        args.add(DBTools.quoteTableName(provider, fullname, rule, sensitive));
        // TODO: {1} quoted / unquoted catalog view name
        args.add(DBTools.enquoteIdentifier(provider, component.getCatalog(), sensitive));
        // TODO: {2} quoted / unquoted schema view name
        args.add(DBTools.enquoteIdentifier(provider, component.getSchema(), sensitive));
        // TODO: {3} quoted / unquoted view name
        args.add(DBTools.enquoteIdentifier(provider, component.getTable(), sensitive));
        // TODO: {4} raw view command
        args.add(command);
        return args.toArray(new Object[0]);
    }


    public static Object[] getViewDefinitionArguments(DriverProvider provider,
                                                      NameComponents component,
                                                      String fullname,
                                                      ComposeRule rule,
                                                      boolean sensitive)
        throws java.sql.SQLException, SQLException
    {
        List<String> args = new ArrayList<>();
        // TODO: {0} quoted / unquoted  full view name
        args.add(DBTools.quoteTableName(provider, fullname, rule, sensitive));
        // TODO: {1} quoted / unquoted  catalog view name
        args.add(DBTools.enquoteIdentifier(provider, component.getCatalog(), sensitive));
        // TODO: {2} quoted / unquoted  schema view name
        args.add(DBTools.enquoteIdentifier(provider, component.getSchema(), sensitive));
        // TODO: {3} quoted / unquoted  view name
        args.add(DBTools.enquoteIdentifier(provider, component.getTable(), sensitive));
        // TODO: {4} quoted literal 'SELECT '
        args.add("'SELECT '");
        return args.toArray(new Object[0]);
    }

}
