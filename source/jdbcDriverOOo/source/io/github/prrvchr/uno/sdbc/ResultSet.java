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

import com.sun.star.beans.PropertyValue;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;

import io.github.prrvchr.jdbcdriver.DriverProvider;


public class ResultSet
extends ResultSetBase
{
    private static final String m_name = ResultSet.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbc.ResultSet"};


    // The constructor method:
    public ResultSet(XComponentContext ctx,
                     DriverProvider provider,
                     java.sql.ResultSet resultset,
                     PropertyValue[] info)
    {
        super(ctx, m_name, m_services, provider, resultset, info);
        System.out.println("sdbc.ResultSet() 1");
    }
    public ResultSet(XComponentContext ctx,
                     DriverProvider provider,
                     XInterface statement,
                     java.sql.ResultSet resultset,
                     PropertyValue[] info)
    {
        super(ctx, m_name, m_services, provider, statement, resultset, info);
        System.out.println("sdbc.ResultSet() 1");
    }


}
