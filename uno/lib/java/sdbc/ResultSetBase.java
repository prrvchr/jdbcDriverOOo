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

import java.io.InputStream;
import java.io.Reader;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.container.XNameAccess;
import com.sun.star.io.XInputStream;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.logging.LogLevel;
import com.sun.star.lib.uno.adapter.XInputStreamToInputStreamAdapter;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XArray;
import com.sun.star.sdbc.XBlob;
import com.sun.star.sdbc.XClob;
import com.sun.star.sdbc.XCloseable;
import com.sun.star.sdbc.XColumnLocate;
import com.sun.star.sdbc.XRef;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbc.XResultSetMetaData;
import com.sun.star.sdbc.XResultSetMetaDataSupplier;
import com.sun.star.sdbc.XResultSetUpdate;
import com.sun.star.sdbc.XRow;
import com.sun.star.sdbc.XRowUpdate;
import com.sun.star.sdbc.XWarningsSupplier;
import com.sun.star.uno.Any;
import com.sun.star.uno.Type;
import com.sun.star.util.Date;
import com.sun.star.util.DateTime;
import com.sun.star.util.Time;

import io.github.prrvchr.uno.beans.PropertySet;
import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertySetter;
import io.github.prrvchr.uno.helper.DataBaseTools;
import io.github.prrvchr.uno.helper.PropertyIds;
import io.github.prrvchr.uno.helper.Resources;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.lang.ServiceInfo;
import io.github.prrvchr.uno.sdbc.ConnectionLog.ObjectType;

public abstract class ResultSetBase
    extends PropertySet
    implements XServiceInfo,
               XCloseable,
               XColumnLocate,
               XResultSet,
               XResultSetMetaDataSupplier,
               XRow,
               XWarningsSupplier,
               XResultSetUpdate,
               XRowUpdate
{

    private final String m_service;
    private final String[] m_services;
    protected ConnectionBase m_Connection;
    protected final ConnectionLog m_logger;
    private StatementMain m_Statement;
    protected java.sql.ResultSet m_ResultSet;
    

    // The constructor method:
    public ResultSetBase(String service,
                         String[] services,
                         ConnectionBase connection,
                         java.sql.ResultSet resultset,
                         StatementMain statement)
        throws SQLException
    {
        m_service = service;
        m_services = services;
        m_Connection = connection;
        m_logger = new ConnectionLog(connection.getLogger(), ObjectType.RESULT);
        m_Statement = statement;
        m_ResultSet = resultset;
        registerProperties();
    }


    private void registerProperties() {
        short readonly = PropertyAttribute.READONLY;
        registerProperty(PropertyIds.CURSORNAME.name, PropertyIds.CURSORNAME.id, Type.STRING, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return _getCursorName();
                }
            }, null);
        registerProperty(PropertyIds.RESULTSETCONCURRENCY.name, PropertyIds.RESULTSETCONCURRENCY.id, Type.LONG, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return _getResultSetConcurrency();
                }
            }, null);
        registerProperty(PropertyIds.RESULTSETTYPE.name, PropertyIds.RESULTSETTYPE.id, Type.LONG, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return _getResultSetType();
                }
            }, null);
        registerProperty(PropertyIds.FETCHDIRECTION.name, PropertyIds.FETCHDIRECTION.id, Type.LONG,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return _getFetchDirection();
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    _setFetchDirection((int) value);
                }
            });
        registerProperty(PropertyIds.FETCHSIZE.name, PropertyIds.FETCHSIZE.id, Type.LONG,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return _getFetchSize();
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    _setFetchSize((int) value);
                }
            });
    }

    private String _getCursorName()
        throws WrappedTargetException
    {
        try {
            String cursor = m_ResultSet.getCursorName();
            return cursor != null ? cursor : "";
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getWrappedException(UnoHelper.getSQLException(e, this));
        }
    }
    private int _getFetchDirection()
        throws WrappedTargetException
    {
        try {
            return m_ResultSet.getFetchDirection();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getWrappedException(UnoHelper.getSQLException(e, this));
        }
        
    }
    private void _setFetchDirection(int direction)
        throws WrappedTargetException
    {
        try {
            m_ResultSet.setFetchDirection(direction);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getWrappedException(UnoHelper.getSQLException(e, this));
        }
        
    }
    private int _getFetchSize()
        throws WrappedTargetException
    {
        try {
            return m_ResultSet.getFetchSize();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getWrappedException(UnoHelper.getSQLException(e, this));
        }
        
    }
    private void _setFetchSize(int size)
        throws WrappedTargetException
    {
        try {
            m_ResultSet.setFetchSize(size);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getWrappedException(UnoHelper.getSQLException(e, this));
        }
        
    }
    private int _getResultSetConcurrency()
        throws WrappedTargetException
    {
        try {
            return m_ResultSet.getConcurrency();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getWrappedException(UnoHelper.getSQLException(e, this));
        }
        
    }
    private int _getResultSetType()
        throws WrappedTargetException
    {
        try {
            return m_ResultSet.getType();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getWrappedException(UnoHelper.getSQLException(e, this));
        }
        
    }

    // com.sun.star.lang.XComponent
    @Override
    public synchronized void postDisposing() {
        super.postDisposing();
        if (m_ResultSet != null) {
            try {
                m_ResultSet.close();
            }
            catch (java.sql.SQLException e) {
                m_logger.log(LogLevel.WARNING, e);
            }
            m_ResultSet = null;
        }
    }


    // com.sun.star.sdbc.XCloseable
    @Override
    public void close() throws SQLException
    {
        dispose();
    }


    // com.sun.star.sdbc.XColumnLocate:
    @Override
    public int findColumn(String name) throws SQLException
    {
        try {
            return m_ResultSet.findColumn(name);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


    // com.sun.star.sdbc.XResultSet:
    @Override
    public boolean absolute(int row) throws SQLException
    {
        try {
            return m_ResultSet.absolute(row);
        }
        catch (java.sql.SQLException e) {
            System.out.println("ResultSetBase.absolute() ERROR:\n" + UnoHelper.getStackTrace(e));
            throw UnoHelper.getSQLException(e, this);
        }
        catch (java.lang.Exception e) {
            System.out.println("ResultSetBase.absolute() ERROR:\n" + UnoHelper.getStackTrace(e));
            throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), this);
        }
    }

    @Override
    public void afterLast() throws SQLException
    {
        try {
            m_ResultSet.afterLast();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void beforeFirst() throws SQLException
    {
        try {
            m_ResultSet.beforeFirst();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }

    }

    @Override
    public boolean first() throws SQLException
    {
        try {
            return m_ResultSet.first();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getRow() throws SQLException
    {
        try {
            return m_ResultSet.getRow();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public com.sun.star.uno.XInterface getStatement() throws SQLException
    {
        return m_Statement;
    }

    @Override
    public boolean isAfterLast() throws SQLException
    {
        try {
            return m_ResultSet.isAfterLast();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isBeforeFirst() throws SQLException
    {
        try {
            return m_ResultSet.isBeforeFirst();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isFirst() throws SQLException
    {
        try {
            return m_ResultSet.isFirst();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isLast() throws SQLException
    {
        try {
            return m_ResultSet.isLast();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean last() throws SQLException
    {
        try {
            return m_ResultSet.last();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean next() throws SQLException
    {
        try {
            return m_ResultSet.next();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean previous() throws SQLException
    {
        try {
            return m_ResultSet.previous();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void refreshRow() throws SQLException
    {
        try {
            m_ResultSet.refreshRow();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean relative(int row) throws SQLException
    {
        try {
            return m_ResultSet.relative(row);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean rowDeleted() throws SQLException
    {
        try {
            return m_ResultSet.rowDeleted();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean rowInserted() throws SQLException
    {
        try {
            return m_ResultSet.rowInserted();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean rowUpdated() throws SQLException
    {
        try {
            return m_ResultSet.rowUpdated();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


    // com.sun.star.lang.XServiceInfo:
    @Override
    public String getImplementationName()
    {
        return ServiceInfo.getImplementationName(m_service);
    }

    @Override
    public String[] getSupportedServiceNames()
    {
        return ServiceInfo.getSupportedServiceNames(m_services);
    }

    @Override
    public boolean supportsService(String service)
    {
        return ServiceInfo.supportsService(m_services, service);
    }


    // com.sun.star.sdbc.XWarningsSupplier:
    @Override
    public void clearWarnings() throws SQLException
    {
        if (m_Connection.getProvider().supportWarningsSupplier())
            WarningsSupplier.clearWarnings(m_ResultSet, this);
    }


    @Override
    public Object getWarnings() throws SQLException
    {
        if (m_Connection.getProvider().supportWarningsSupplier())
            return WarningsSupplier.getWarnings(m_ResultSet, this);
         return Any.VOID;
    }


    // com.sun.star.sdbc.XResultSetUpdate:
    @Override
    public void insertRow() throws SQLException
    {
        try {
            m_ResultSet.insertRow();
            m_ResultSet.moveToCurrentRow();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateRow() throws SQLException
    {
        try {
            m_ResultSet.updateRow();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void deleteRow() throws SQLException
    {
        try {
            m_ResultSet.deleteRow();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void cancelRowUpdates() throws SQLException
    {
        try {
            m_ResultSet.moveToCurrentRow();
            m_ResultSet.cancelRowUpdates();
        }
        catch (java.sql.SQLException e) {
            System.out.println("ResultSetBase.cancelRowUpdates() ERROR\n" + UnoHelper.getStackTrace(e));
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void moveToInsertRow() throws SQLException
    {
        try {
            m_ResultSet.moveToInsertRow();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void moveToCurrentRow() throws SQLException
    {
        try {
            m_ResultSet.moveToCurrentRow();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }


    // com.sun.star.sdbc.XRow:
    @Override
    public XArray getArray(int index) throws SQLException
    {
        try {
            java.sql.Array array = m_ResultSet.getArray(index);
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
        return blob != null ? blob.getBinaryStream() : null;
    }

    @Override
    public XBlob getBlob(int index) throws SQLException
    {
        try {
            java.sql.Blob blob = m_ResultSet.getBlob(index);
            return (blob != null) ? new Blob(m_Connection, blob) : null;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean getBoolean(int index) throws SQLException
    {
        try {
            return m_ResultSet.getBoolean(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public byte getByte(int index) throws SQLException
    {
        try {
            return m_ResultSet.getByte(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public byte[] getBytes(int index) throws SQLException
    {
        try {
            return m_ResultSet.getBytes(index);
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
        try {
            java.sql.Clob clob = m_ResultSet.getClob(index);
            return clob != null ? new Clob(m_Connection, clob) : null;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public Date getDate(int index) throws SQLException
    {
        try {
            java.sql.Date value = m_ResultSet.getDate(index);
            return value != null ? UnoHelper.getUnoDate(value) : new Date();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public double getDouble(int index) throws SQLException
    {
        try {
            return m_ResultSet.getDouble(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public float getFloat(int index) throws SQLException
    {
        try {
            return m_ResultSet.getFloat(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getInt(int index) throws SQLException
    {
        try {
            return m_ResultSet.getInt(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public long getLong(int index) throws SQLException
    {
        try {
            return m_ResultSet.getLong(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public Object getObject(int index, XNameAccess map) throws SQLException
    {
        try {
            Object value = Any.VOID;
            Object object = m_ResultSet.getObject(index);
            if (object instanceof String) {
                value = (String) object;
            }
            else if (object instanceof Boolean) {
                value = (Boolean) object;
            }
            else if (object instanceof java.sql.Date) {
                value = UnoHelper.getUnoDate((java.sql.Date) object);
            }
            else if (object instanceof java.sql.Time) {
                value = UnoHelper.getUnoTime((java.sql.Time) object);
            }
            else if (object instanceof java.sql.Timestamp) {
                value = UnoHelper.getUnoDateTime((java.sql.Timestamp) object);
            }
            return value;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getLoggedSQLException(this, m_logger, e);
        }
    }

    @Override
    public XRef getRef(int index) throws SQLException
    {
        try {
            java.sql.Ref ref = m_ResultSet.getRef(index);
            return ref != null ? new Ref(ref) : null;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public short getShort(int index) throws SQLException
    {
        try {
            return m_ResultSet.getShort(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getString(int index) throws SQLException
    {
        try {
            String value = m_ResultSet.getString(index);
            return value != null ? value : "";
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public Time getTime(int index) throws SQLException
    {
        try {
            java.sql.Time value = m_ResultSet.getTime(index);
            return value != null ? UnoHelper.getUnoTime(value) : new Time();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public DateTime getTimestamp(int index) throws SQLException
    {
        try {
            java.sql.Timestamp value = m_ResultSet.getTimestamp(index);
            return value != null ? UnoHelper.getUnoDateTime(value) : new DateTime();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean wasNull() throws SQLException
    {
        try {
            return m_ResultSet.wasNull();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


    // com.sun.star.sdbc.XRowUpdate:
    @Override
    public void updateNull(int index) throws SQLException
    {
        try {
            m_ResultSet.updateNull(index);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateBoolean(int index, boolean value)
    throws SQLException
    {
        try {
            m_ResultSet.updateBoolean(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateByte(int index, byte value) throws SQLException
    {
        try {
            m_ResultSet.updateByte(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateShort(int index, short value) throws SQLException
    {
        try {
            m_ResultSet.updateShort(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateInt(int index, int value) throws SQLException
    {
        try {
            m_ResultSet.updateInt(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateLong(int index, long value) throws SQLException
    {
        try {
            m_ResultSet.updateLong(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateFloat(int index, float value) throws SQLException
    {
        try {
            m_ResultSet.updateFloat(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateDouble(int index, double value) throws SQLException
    {
        try {
            m_ResultSet.updateDouble(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateString(int index, String value) throws SQLException
    {
        try {
            m_ResultSet.updateString(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateBytes(int index, byte[] value) throws SQLException
    {
        try {
            m_ResultSet.updateBytes(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateDate(int index, Date value) throws SQLException
    {
        try {
            m_ResultSet.updateDate(index, UnoHelper.getJavaDate(value));
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateTime(int index, Time value) throws SQLException
    {
        try {
            m_ResultSet.updateTime(index, UnoHelper.getJavaTime(value));
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateTimestamp(int index, DateTime value) throws SQLException
    {
        try {
            m_ResultSet.updateTimestamp(index, UnoHelper.getJavaDateTime(value));
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateBinaryStream(int index, XInputStream value, int lenght) throws SQLException
    {
        try {
            InputStream input = new XInputStreamToInputStreamAdapter(value);
            m_ResultSet.updateBinaryStream(index, input, lenght);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateCharacterStream(int index, XInputStream value, int lenght) throws SQLException
    {
        try {
            InputStream input = new XInputStreamToInputStreamAdapter(value);
            Reader reader = new java.io.InputStreamReader(input);
            m_ResultSet.updateCharacterStream(index, reader, lenght);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateObject(int index, Object value) throws SQLException
    {
        try {
            if (!DataBaseTools.updateObject(this, index, value)) {
                String error = SharedResources.getInstance().getResourceWithSubstitution(Resources.STR_UNKNOWN_COLUMN_TYPE,
                                                                                         this.getClass().getName(),
                                                                                         "updateObject()",
                                                                                         Integer.toString(index));
                throw new SQLException(error, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
            }

            m_ResultSet.updateObject(index, value);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }

    @Override
    public void updateNumericObject(int index, Object value, int scale) throws SQLException
    {
        try {
            m_ResultSet.updateObject(index, value, scale);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        } 
    }


    // com.sun.star.sdbc.XResultSetMetaDataSupplier:
    @Override
    public XResultSetMetaData getMetaData() throws SQLException
    {
        try {
            java.sql.ResultSetMetaData metadata = m_ResultSet.getMetaData();
            return (metadata != null) ? new ResultSetMetaData(m_Connection, metadata) : null;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


}
