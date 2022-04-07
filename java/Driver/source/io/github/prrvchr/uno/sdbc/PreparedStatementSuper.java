/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020 https://prrvchr.github.io                                     ║
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
package io.github.prrvchr.uno.sdbc;

import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class PreparedStatementSuper
extends PreparedStatementBase
{

    protected boolean m_UseBookmarks = false;
    private static Map<String, Property> _getPropertySet()
    {
        Map<String, Property> map = new LinkedHashMap<String, Property>();
        map.put("m_UseBookmarks", UnoHelper.getProperty("UseBookmarks", "boolean"));
        return map;
    }

    // The constructor method:
    public PreparedStatementSuper(XComponentContext context,
                                 String name,
                                 String[] services,
                                 DriverProvider provider,
                                 ConnectionBase xConnection,
                                 java.sql.Connection connection,
                                 String sql)
    {
        super(context, name, services, provider, xConnection, connection, sql, _getPropertySet());
        System.out.println("sdbcx.SuperPreparedStatement() 1: '" + sql + "'");
    }


}
