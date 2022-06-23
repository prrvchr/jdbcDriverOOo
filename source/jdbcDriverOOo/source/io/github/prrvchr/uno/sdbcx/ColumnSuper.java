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
package io.github.prrvchr.uno.sdbcx;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.WrappedTargetException;

import io.github.prrvchr.uno.sdb.Connection;


public class ColumnSuper
    extends ColumnBase
{

    // The constructor method:
    public ColumnSuper(String service,
                       String[] services,
                       Connection connection,
                       XPropertySet descriptor,
                       String name,
                       int position)
        throws java.sql.SQLException, UnknownPropertyException, WrappedTargetException
    {
        super(service, services, connection, descriptor, name, position);
    }
    public ColumnSuper(String service,
                       String[] services,
                       Connection connection,
                       ResultSetMetaData metadata,
                       String name,
                       int index)
        throws SQLException
    {
        super(service, services, connection, metadata, name, index);
    }
    public ColumnSuper(String service,
                       String[] services,
                       Connection connection,
                       java.sql.ResultSet result,
                       String name)
        throws java.sql.SQLException
    {
        super(service, services, connection, result, name);
    }
    public ColumnSuper(String service,
                       String[] services,
                       Connection connection,
                       java.sql.ResultSet result,
                       String name,
                       int position)
        throws java.sql.SQLException
    {
        super(service, services, connection, result, name, position);
    }
    public ColumnSuper(String service,
                       String[] services,
                       Connection connection,
                       schemacrawler.schema.Column column,
                       String name)
    {
        super(service, services, connection, column, name);
    }
    public ColumnSuper(String service,
                       String[] services,
                       Connection connection,
                       schemacrawler.schema.ResultsColumn column,
                       String name)
    {
        super(service, services, connection, column, name);
    }


}
