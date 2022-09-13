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

import com.sun.star.container.XNameAccess;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XArray;
import com.sun.star.sdbc.XResultSet;

import io.github.prrvchr.uno.helper.UnoHelper;

public class Array
    extends ComponentBase
    implements XArray
{

    private final ConnectionBase m_Connection;
    private final java.sql.Array m_Array;

    // The constructor method:
    public Array(ConnectionBase connection,
                 java.sql.Array array)
    {
            m_Connection = connection;
            m_Array = array;
    }

    // com.sun.star.lang.XComponent
    @Override
    protected void postDisposing() {
        try {
            m_Array.free();
        }
        catch (java.sql.SQLException e) {
            m_Connection.getLogger().log(LogLevel.WARNING, e);
        }
    }
    

    // com.sun.star.sdbc.XArray
    @Override
    public Object[] getArray(XNameAccess map)
        throws SQLException
    {
        try {
            return (Object[]) m_Array.getArray();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public Object[] getArrayAtIndex(int index, int count, XNameAccess map)
    throws SQLException
    {
        try {
            return (Object[]) m_Array.getArray(index, count);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getBaseType()
        throws SQLException
    {
        try {
            return m_Connection.getProvider().getDataType(m_Array.getBaseType());
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getBaseTypeName()
    throws SQLException
    {
        try {
            return m_Array.getBaseTypeName();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getResultSet(XNameAccess map)
    throws SQLException
    {
        try {
            java.sql.ResultSet result = m_Array.getResultSet();
            return result != null ? m_Connection.getProvider().getResultSet(m_Connection, result) : null;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getResultSetAtIndex(int index, int count, XNameAccess map)
    throws SQLException
    {
        try {
            java.sql.ResultSet result = m_Array.getResultSet(index, count);
            return result != null ? m_Connection.getProvider().getResultSet(m_Connection, result) : null;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


}
