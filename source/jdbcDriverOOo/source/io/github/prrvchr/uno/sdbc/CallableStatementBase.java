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

import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class CallableStatementBase<C extends ConnectionBase>
    extends PreparedStatementMain<java.sql.CallableStatement, C>
    implements XOutParameters,
               XRow
{

    // The constructor method:
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbc.CallableStatement()
    // XXX: - io.github.prrvchr.uno.sdbc.CallableStatementSuper()
    public CallableStatementBase(String service,
                                 String[] services,
                                 C connection,
                                 String sql)
    {
        super(service, services, connection);
        m_Sql = sql;
        System.out.println("sdbc.BaseCallableStatement() 1: '" + sql + "'");
    }

    @Override
    protected java.sql.CallableStatement getStatement()
        throws SQLException
    {
        checkDisposed();
        if (m_Statement == null) {
            try {
                java.sql.CallableStatement statement;
                if (m_Connection.getProvider().isResultSetUpdatable() &&
                   (m_ResultSetType != java.sql.ResultSet.TYPE_FORWARD_ONLY || m_ResultSetConcurrency != java.sql.ResultSet.CONCUR_READ_ONLY)) {
                    statement = m_Connection.getProvider().getConnection().prepareCall(m_Sql, m_ResultSetType, m_ResultSetConcurrency);
                } 
                else {
                    statement = m_Connection.getProvider().getConnection().prepareCall(m_Sql);
                }
                m_Statement = setStatement(statement);
            } 
            catch (java.sql.SQLException e) {
                System.out.println("sdbc.CallableStatementBase.getStatement() ERROR: " + m_ResultSetType + " - " + m_ResultSetConcurrency + " - SQL: '" + m_Sql + "'");
                throw UnoHelper.getSQLException(e, this);
            }
        }
        return m_Statement;
    }

    // com.sun.star.sdbc.XOutParameters:
    @Override
    public void registerNumericOutParameter(int index, int type, int scale) throws SQLException
    {
        try
        {
            getStatement().registerOutParameter(index, type, scale);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void registerOutParameter(int index, int type, String name) throws SQLException
    {
        try
        {
            getStatement().registerOutParameter(index, type, name);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


    // com.sun.star.sdbc.XRow:
    @Override
    public XArray getArray(int index) throws SQLException
    {
        try
        {
            java.sql.Array array = getStatement().getArray(index);
            return (array != null) ? new Array(m_Connection, array) : null;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XInputStream getBinaryStream(int index) throws SQLException
    {
        XBlob blob = getBlob(index);
        return (blob != null) ? blob.getBinaryStream() : null;
    }

    @Override
    public XBlob getBlob(int index) throws SQLException
    {
        try
        {
            java.sql.Blob blob = getStatement().getBlob(index);
            return (blob != null) ? new Blob(m_Connection, blob) : null;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean getBoolean(int index) throws SQLException
    {
        try
        {
            return getStatement().getBoolean(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public byte getByte(int index) throws SQLException
    {
        try
        {
            return getStatement().getByte(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public byte[] getBytes(int index) throws SQLException
    {
        try
        {
           return getStatement().getBytes(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XInputStream getCharacterStream(int index) throws SQLException
    {
        XClob clob = getClob(index);
        return (clob != null) ? clob.getCharacterStream() : null;
    }

    @Override
    public XClob getClob(int index) throws SQLException
    {
        try
        {
            java.sql.Clob clob = getStatement().getClob(index);
            return (clob != null) ? new Clob(m_Connection, clob) : null;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public Date getDate(int index) throws SQLException
    {
        try
        {
            java.sql.Date value = getStatement().getDate(index);
            return (value != null) ? UnoHelper.getUnoDate(value.toLocalDate()) : new Date();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public double getDouble(int index) throws SQLException
    {
        try
        {
            return getStatement().getDouble(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public float getFloat(int index) throws SQLException
    {
        try
        {
            return getStatement().getFloat(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getInt(int index) throws SQLException
    {
        try
        {
            return getStatement().getInt(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public long getLong(int index) throws SQLException
    {
        try
        {
            return getStatement().getLong(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public Object getObject(int index, XNameAccess map) throws SQLException
    {
        try {
            return DBTools.getObject(getStatement().getObject(index), map);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getLoggedSQLException(this, m_logger, e);
        }
    }

    @Override
    public XRef getRef(int index) throws SQLException
    {
        try
        {
            java.sql.Ref ref = getStatement().getRef(index);
            return ref != null ? new Ref(ref) : null;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public short getShort(int index) throws SQLException
    {
        try
        {
            return getStatement().getShort(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getString(int index) throws SQLException
    {
        try
        {
            String value = getStatement().getString(index);
            return (value != null) ? value : "";
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public Time getTime(int index) throws SQLException
    {
        try
        {
            java.sql.Time value = getStatement().getTime(index);
            return (value != null) ? UnoHelper.getUnoTime(value.toLocalTime()) : new Time();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public DateTime getTimestamp(int index) throws SQLException
    {
        try
        {
            java.sql.Timestamp value = getStatement().getTimestamp(index);
            return (value != null) ? UnoHelper.getUnoDateTime(value.toLocalDateTime()) : new DateTime();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean wasNull() throws SQLException
    {
        try
        {
            return getStatement().wasNull();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


}
