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

import io.github.prrvchr.uno.sdbc.ConnectionBase;

public class Column
    extends ColumnSuper
{
    private static final String m_service = Column.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Column"};

    // The constructor method:
    public Column(final ConnectionBase connection,
                  final boolean sensitive,
                  final String catalog,
                  final String schema,
                  final String table)
    {
        super(m_service, m_services, connection, sensitive, catalog, schema, table);
    }
    public Column(final ConnectionBase connection,
                  final boolean sensitive,
                  final String catalog,
                  final String schema,
                  final String table,
                  final String name,
                  final String typename,
                  final String defaultvalue,
                  final String description,
                  final int nullable,
                  final int precision,
                  final int scale,
                  final int type,
                  final boolean autoincrement,
                  final boolean rowversion,
                  final boolean currency)
    {
        super(m_service, m_services, connection, sensitive, catalog, schema, table, name, typename, defaultvalue, description, nullable, precision, scale, type, autoincrement, rowversion, currency);
    }


/*    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.ColumnContainer()
    // XXX: - io.github.prrvchr.uno.sdbcx.ColumnContainer.appendByDescriptor()
    public Column(Connection connection,
                  XPropertySet descriptor,
                  String name,
                  int position)
        throws java.sql.SQLException, UnknownPropertyException, WrappedTargetException
    {
        super(m_name, m_services, connection, descriptor, name, position);
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.ColumnContainer()
    public Column(Connection connection,
                  ResultSetMetaData metadata,
                  String name,
                  int index)
        throws SQLException
    {
        super(m_name, m_services, connection, metadata, name, index);
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.ColumnContainer()
    public Column(Connection connection,
                  java.sql.ResultSet result,
                  String name)
        throws java.sql.SQLException
    {
        super(m_name, m_services, connection, result, name);
    }
    public Column(Connection connection,
                  java.sql.ResultSet result,
                  String name,
                  int position)
        throws java.sql.SQLException
    {
        super(m_name, m_services, connection, result, name, position);
    }
    public Column(Connection connection,
                  schemacrawler.schema.Column column,
                  String name)
    {
        super(m_name, m_services, connection, column, name);
    }
    public Column(Connection connection,
                  schemacrawler.schema.ResultsColumn column,
                  String name)
    {
        super(m_name, m_services, connection, column, name);
    }*/


}
