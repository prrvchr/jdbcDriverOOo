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
package io.github.prrvchr.uno.sdbc;

import java.io.InputStream;

import com.sun.star.io.XInputStream;
import com.sun.star.lib.uno.adapter.XInputStreamToInputStreamAdapter;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XArray;
import com.sun.star.sdbc.XBlob;
import com.sun.star.sdbc.XClob;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbc.XParameters;
import com.sun.star.sdbc.XPreparedBatchExecution;
import com.sun.star.sdbc.XPreparedStatement;
import com.sun.star.sdbc.XRef;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbc.XResultSetMetaData;
import com.sun.star.sdbc.XResultSetMetaDataSupplier;
import com.sun.star.uno.Any;
import com.sun.star.util.Date;
import com.sun.star.util.DateTime;
import com.sun.star.util.Time;

import io.github.prrvchr.jdbcdriver.helper.DBTools;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class PreparedStatementMain<C extends ConnectionBase, S extends java.sql.PreparedStatement>
    extends StatementMain<C, S>
    implements XParameters,
               XPreparedBatchExecution,
               XPreparedStatement,
               XResultSetMetaDataSupplier
{

    // The constructor method:
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.CallableStatement()
    // XXX: - io.github.prrvchr.uno.sdbc.PreparedStatementMain()
    // XXX: - io.github.prrvchr.uno.sdbc.PreparedStatementBase()
    // XXX: - io.github.prrvchr.uno.sdbc.CallableStatementBase()
    public PreparedStatementMain(String service,
                                 String[] services,
                                 C connection)
    {
        super(service, services, connection);
    }

    @Override
    protected java.sql.ResultSet getJdbcResultSet()
        throws java.sql.SQLException
    {
        return getJdbcStatement().executeQuery();
    }


    // com.sun.star.sdbc.XParameters:
    @Override
    public void clearParameters() throws SQLException
    {
        try {
            getJdbcStatement().clearParameters();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setArray(int index, XArray value) throws SQLException
    {
        try {
            java.sql.Array array = getJdbcStatement().getConnection().createArrayOf(value.getBaseTypeName(), value.getArray(null));
            getJdbcStatement().setArray(index, array);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setBinaryStream(int index, XInputStream value, int lenght) throws SQLException
    {
        try {
            InputStream input = new XInputStreamToInputStreamAdapter(value);
            getJdbcStatement().setBinaryStream(index, input, lenght);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setBlob(int index, XBlob value) throws SQLException
    {
        try {
            java.sql.Blob blob = getJdbcStatement().getConnection().createBlob();
            blob.setBytes(1, value.getBytes(1, (int) value.length()));
            getJdbcStatement().setBlob(index, blob);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setBoolean(int index, boolean value) throws SQLException
    {
        try {
            getJdbcStatement().setBoolean(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setByte(int index, byte value) throws SQLException
    {
        try {
            getJdbcStatement().setByte(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setBytes(int index, byte[] value) throws SQLException
    {
        try {
            getJdbcStatement().setBytes(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setCharacterStream(int index, XInputStream value, int lenght) throws SQLException
    {
        try {
            getJdbcStatement().setCharacterStream(index, new java.io.InputStreamReader(new XInputStreamToInputStreamAdapter(value)), lenght);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setClob(int index, XClob value) throws SQLException
    {
        try {
            java.sql.Clob clob = getJdbcStatement().getConnection().createClob();
            clob.setString(1, value.toString());
            getJdbcStatement().setClob(index, clob);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setDate(int index, Date value) throws SQLException
    {
        try {
            java.sql.Date date = java.sql.Date.valueOf(UnoHelper.getJavaLocalDate(value));
            getJdbcStatement().setDate(index, date);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setDouble(int index, double value) throws SQLException
    {
        try {
            getJdbcStatement().setDouble(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setFloat(int index, float value) throws SQLException
    {
        try {
            getJdbcStatement().setFloat(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setInt(int index, int value) throws SQLException
    {
        try {
            getJdbcStatement().setInt(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setLong(int index, long value) throws SQLException
    {
        try {
            getJdbcStatement().setLong(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setNull(int index, int type) throws SQLException
    {
        try {
            getJdbcStatement().setNull(index, type);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setObject(int index, Object value)
        throws SQLException
    {
        try {
            if (!DBTools.setObject(getJdbcStatement(), index, value)) {
                String error = SharedResources.getInstance().getResourceWithSubstitution(Resources.STR_UNKNOWN_PARA_TYPE,
                                                                                         "$position$",
                                                                                         Integer.toString(index));
                throw new SQLException(error, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
            }
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }

    }

    @Override
    public void setObjectNull(int index, int type, String name) throws SQLException
    {
        try {
            getJdbcStatement().setObject(index, null);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setObjectWithInfo(int index, Object value, int type, int scale) throws SQLException
    {
        try {
            getJdbcStatement().setObject(index, value, type, scale);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setRef(int index, XRef value) throws SQLException
    {
        // TODO: Implement me!!!
    }

    @Override
    public void setShort(int index, short value) throws SQLException
    {
        try {
            getJdbcStatement().setShort(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setString(int index, String value) throws SQLException
    {
        try {
            getJdbcStatement().setString(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setTime(int index, Time value) throws SQLException
    {
        try {
            java.sql.Time time = java.sql.Time.valueOf(UnoHelper.getJavaLocalTime(value));
            getJdbcStatement().setTime(index, time);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setTimestamp(int index, DateTime value) throws SQLException
    {
        try {
            java.sql.Timestamp timestamp = java.sql.Timestamp.valueOf(UnoHelper.getJavaLocalDateTime(value));
            getJdbcStatement().setTimestamp(index, timestamp);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


    // com.sun.star.sdbc.XPreparedBatchExecution:
    @Override
    public void addBatch() throws SQLException
    {
        try {
            getJdbcStatement().addBatch();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void clearBatch() throws SQLException
    {
        try {
            getJdbcStatement().clearBatch();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int[] executeBatch() throws SQLException
    {
        try {
            return getJdbcStatement().executeBatch();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


    // com.sun.star.sdbc.XPreparedStatement:
    @Override
    public boolean execute() throws SQLException
    {
        try {
            return getJdbcStatement().execute();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet executeQuery() throws SQLException
    {
        return getResultSet();
    }

    @Override
    public int executeUpdate() throws SQLException
    {
        try {
            return getJdbcStatement().executeUpdate();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XConnection getConnection() throws SQLException
    {
        return m_Connection;
    }


    // com.sun.star.sdbc.XResultSetMetaDataSupplier:
    @Override
    public XResultSetMetaData getMetaData() throws SQLException
    {
        try {
            java.sql.ResultSetMetaData metadata = getJdbcStatement().getMetaData();
            return metadata != null ? new ResultSetMetaData(m_Connection, metadata) : null;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


}
