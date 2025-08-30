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

import java.sql.CallableStatement;

import com.sun.star.container.XNameAccess;
import com.sun.star.io.XInputStream;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XArray;
import com.sun.star.sdbc.XBlob;
import com.sun.star.sdbc.XClob;
import com.sun.star.sdbc.XOutParameters;
import com.sun.star.sdbc.XRef;
import com.sun.star.sdbc.XRow;
import com.sun.star.util.Date;
import com.sun.star.util.DateTime;
import com.sun.star.util.Time;

import io.github.prrvchr.uno.driver.helper.QueryHelper;
import io.github.prrvchr.uno.driver.provider.DBTools;
import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class CallableStatementBase
    extends PreparedStatementMain
    implements XOutParameters,
               XRow {

    // The constructor method:
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbc.CallableStatement()
    // XXX: - io.github.prrvchr.uno.sdbc.CallableStatementSuper()
    public CallableStatementBase(String service,
                                 String[] services,
                                 ConnectionBase connection,
                                 String sql)
        throws java.sql.SQLException {
        super(service, services, connection);
        mQuery = new QueryHelper(connection.getProvider(), sql);
        System.out.println("sdbc.BaseCallableStatement() 1: '" + sql + "'");
    }

    @Override
    protected java.sql.CallableStatement getJdbcStatement()
        throws java.sql.SQLException {
        checkDisposed();
        if (mStatement == null) {
            java.sql.CallableStatement statement;
            if (mResultSetType != java.sql.ResultSet.TYPE_FORWARD_ONLY ||
                mResultSetConcurrency != java.sql.ResultSet.CONCUR_READ_ONLY) {
                statement = mConnection.getProvider().getConnection().prepareCall(mQuery.getQuery(),
                        mResultSetType, mResultSetConcurrency);
            } else {
                statement = mConnection.getProvider().getConnection().prepareCall(mQuery.getQuery());
            }
            mStatement = setStatement(statement);
        }
        return (CallableStatement) mStatement;
    }

    // com.sun.star.sdbc.XOutParameters:
    @Override
    public void registerNumericOutParameter(int index, int type, int scale) throws SQLException {
        try {
            getJdbcStatement().registerOutParameter(index, type, scale);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void registerOutParameter(int index, int type, String name) throws SQLException {
        try {
            getJdbcStatement().registerOutParameter(index, type, name);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


    // com.sun.star.sdbc.XRow:
    @Override
    public XArray getArray(int index) throws SQLException {
        try {
            XArray xArray = null;
            java.sql.Array array = getJdbcStatement().getArray(index);
            if (array != null) {
                xArray = new Array(mConnection, array);
            }
            return xArray;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XInputStream getBinaryStream(int index) throws SQLException {
        XInputStream input = null;
        XBlob blob = getBlob(index);
        if (blob != null) {
            input = blob.getBinaryStream();
        }
        return input;
    }

    @Override
    public XBlob getBlob(int index) throws SQLException {
        try {
            XBlob xBlob = null;
            java.sql.Blob blob = getJdbcStatement().getBlob(index);
            if (blob != null) {
                xBlob = new Blob(mConnection, blob);
            }
            return xBlob;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean getBoolean(int index) throws SQLException {
        try {
            return getJdbcStatement().getBoolean(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public byte getByte(int index) throws SQLException {
        try {
            return getJdbcStatement().getByte(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public byte[] getBytes(int index) throws SQLException {
        try {
            return getJdbcStatement().getBytes(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XInputStream getCharacterStream(int index) throws SQLException {
        XInputStream input = null;
        XClob clob = getClob(index);
        if (clob != null) {
            input = clob.getCharacterStream();
        }
        return input;
    }

    @Override
    public XClob getClob(int index) throws SQLException {
        try {
            XClob xClob = null;
            java.sql.Clob clob = getJdbcStatement().getClob(index);
            if (clob != null) {
                xClob = new Clob(mConnection, clob);
            }
            return xClob;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public Date getDate(int index) throws SQLException {
        try {
            Date date = new Date();
            java.sql.Date value = getJdbcStatement().getDate(index);
            if (value != null) {
                date = UnoHelper.getUnoDate(value.toLocalDate());
            }
            return date;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public double getDouble(int index) throws SQLException {
        try {
            return getJdbcStatement().getDouble(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public float getFloat(int index) throws SQLException {
        try {
            return getJdbcStatement().getFloat(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getInt(int index) throws SQLException {
        try {
            return getJdbcStatement().getInt(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public long getLong(int index) throws SQLException {
        try {
            return getJdbcStatement().getLong(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public Object getObject(int index, XNameAccess map) throws SQLException {
        try {
            return DBTools.getObject(getJdbcStatement().getObject(index));
        } catch (java.sql.SQLException e) {
            throw DBTools.getLoggedSQLException(e, this, getLogger());
        }
    }

    @Override
    public XRef getRef(int index) throws SQLException {
        try {
            XRef xRef = null;
            java.sql.Ref ref = getJdbcStatement().getRef(index);
            if (ref != null) {
                xRef = new Ref(ref);
            }
            return xRef;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public short getShort(int index) throws SQLException {
        try {
            return getJdbcStatement().getShort(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getString(int index) throws SQLException {
        try {
            String value = getJdbcStatement().getString(index);
            if (value != null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public Time getTime(int index) throws SQLException {
        try {
            Time time = new Time();
            java.sql.Time value = getJdbcStatement().getTime(index);
            if (value != null) {
                time = UnoHelper.getUnoTime(value.toLocalTime());
            }
            return time;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public DateTime getTimestamp(int index) throws SQLException {
        try {
            DateTime datetime = new DateTime();
            java.sql.Timestamp value = getJdbcStatement().getTimestamp(index);
            if (value != null) {
                datetime = UnoHelper.getUnoDateTime(value.toLocalDateTime());
            }
            return datetime;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean wasNull() throws SQLException {
        try {
            return getJdbcStatement().wasNull();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

}
