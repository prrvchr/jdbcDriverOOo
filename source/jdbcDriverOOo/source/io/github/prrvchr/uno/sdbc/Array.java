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
package io.github.prrvchr.uno.sdbc;

import com.sun.star.container.XNameAccess;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XArray;
import com.sun.star.sdbc.XResultSet;

import io.github.prrvchr.uno.helper.UnoHelper;

public final class Array
    extends ComponentBase
    implements XArray {

    private final ConnectionBase mConnection;
    private final java.sql.Array mArray;

    // The constructor method:
    public Array(ConnectionBase connection,
                 java.sql.Array array) {
        mConnection = connection;
        mArray = array;
    }

    // com.sun.star.lang.XComponent
    @Override
    protected void postDisposing() {
        try {
            mArray.free();
        } catch (java.sql.SQLException e) {
            mConnection.getLogger().log(LogLevel.WARNING, e);
        }
    }
    

    // com.sun.star.sdbc.XArray
    @Override
    public Object[] getArray(XNameAccess map)
        throws SQLException {
        try {
            return (Object[]) mArray.getArray();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public Object[] getArrayAtIndex(int index, int count, XNameAccess map)
        throws SQLException {
        try {
            return (Object[]) mArray.getArray(index, count);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getBaseType()
        throws SQLException {
        try {
            return mConnection.getProvider().getDataType(mArray.getBaseType());
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getBaseTypeName()
        throws SQLException {
        try {
            return mArray.getBaseTypeName();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getResultSet(XNameAccess map)
        throws SQLException {
        try {
            XResultSet resultset = null;
            java.sql.ResultSet result = mArray.getResultSet();
            if (result != null) {
                resultset = new ResultSet(mConnection, result);
            }
            return resultset;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getResultSetAtIndex(int index, int count, XNameAccess map)
        throws SQLException {
        try {
            XResultSet resultset = null;
            java.sql.ResultSet result = mArray.getResultSet(index, count);
            if (result != null) {
                resultset = new ResultSet(mConnection, result);
            }
            return resultset;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


}
