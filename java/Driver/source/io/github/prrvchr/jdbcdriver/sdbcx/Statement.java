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
package io.github.prrvchr.jdbcdriver.sdbcx;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.StatementBase;


public final class Statement
    extends StatementBase
{
    
    private static String m_name = Statement.class.getName();
    private static String[] m_services = {"com.sun.star.sdbc.Statement",
                                          "com.sun.star.sdbcx.Statement"};
    public boolean m_UseBookmarks = false;
    private static Map<String, Property> _getPropertySet()
    {
        Map<String, Property> map = new LinkedHashMap<String, Property>();
        map.put("m_UseBookmarks", UnoHelper.getProperty("UseBookmarks", "boolean"));
        return map;
    }

    // The constructor method:
    public Statement(XComponentContext context,
                     DriverProvider provider,
                     ConnectionBase xConnection,
                     java.sql.Connection connection)
    throws SQLException
    {
        super(context, m_name, m_services, provider, xConnection, connection, _getPropertySet());
        System.out.println("sdbcx.Statement() 1");
    }

    protected XResultSet _getResultSet(XComponentContext ctx,
                                       java.sql.ResultSet resultset)
    throws java.sql.SQLException
    {
        return new ResultSet(ctx, m_provider, this, resultset);
    }


}
