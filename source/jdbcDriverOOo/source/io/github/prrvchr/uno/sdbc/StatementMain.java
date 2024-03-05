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

import java.util.ArrayList;
import java.util.List;

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XCloseable;
import com.sun.star.sdbc.XGeneratedResultSet;
import com.sun.star.sdbc.XMultipleResults;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbc.XWarningsSupplier;
import com.sun.star.uno.Any;
import com.sun.star.uno.Type;
import com.sun.star.util.XCancellable;

import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.uno.helper.PropertySet;
import io.github.prrvchr.uno.helper.ServiceInfo;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertySetter;


public abstract class StatementMain<S extends java.sql.Statement, C extends ConnectionBase>
    extends PropertySet
    implements XServiceInfo,
               XWarningsSupplier,
               XCancellable,
               XCloseable,
               XGeneratedResultSet,
               XMultipleResults
{

    private final String m_service;
    private final String[] m_services;
    protected C m_Connection;
    protected final ConnectionLog m_logger;
    protected S m_Statement = null;
    protected String m_Sql = "";

    private String m_CursorName = "";
    private int m_FetchDirection = java.sql.ResultSet.FETCH_FORWARD;
    private int m_FetchSize = 0;
    private int m_MaxFieldSize = 0;
    private int m_MaxRows = 0;
    private int m_QueryTimeout = 0;
    // FIXME: We are doing lazy loading on Statement because we need this property to create one!!!
    protected int m_ResultSetConcurrency = java.sql.ResultSet.CONCUR_READ_ONLY;
    // FIXME: We are doing lazy loading on Statement because we need this property to create one!!!
    protected int m_ResultSetType = java.sql.ResultSet.TYPE_FORWARD_ONLY;


    // The constructor method:
    public StatementMain(String service,
                         String[] services,
                         C connection)
    {
        m_service = service;
        m_services = services;
        m_Connection = connection;
        m_logger = new ConnectionLog(connection.getProvider().getLogger(), LoggerObjectType.STATEMENT);
        registerProperties();
    }

    protected C getConnectionInternal()
    {
        return m_Connection;
    }

    protected ConnectionLog getLogger()
    {
        return m_logger;
    }

    public java.sql.Statement getGeneratedStatement()
        throws java.sql.SQLException
    {
        return m_Connection.getProvider().getStatement();
    }

    private void registerProperties() {
        registerProperty(PropertyIds.CURSORNAME.name, PropertyIds.CURSORNAME.id, Type.STRING,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return _getCursorName();
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    _setCursorName((String)value);
                }
            });
        registerProperty(PropertyIds.RESULTSETCONCURRENCY.name, PropertyIds.RESULTSETCONCURRENCY.id, Type.LONG,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return _getResultSetConcurrency();
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    _setResultSetConcurrency((int)value);
                }
            });
        registerProperty(PropertyIds.RESULTSETTYPE.name, PropertyIds.RESULTSETTYPE.id, Type.LONG,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return _getResultSetType();
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    _setResultSetType((int)value);
                }
            });
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
                    _setFetchDirection((int)value);
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
                    _setFetchSize((int)value);
                }
            });
        registerProperty(PropertyIds.MAXFIELDSIZE.name, PropertyIds.MAXFIELDSIZE.id, Type.LONG,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return _getMaxFieldSize();
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    _setMaxFieldSize((int)value);
                }
            });
        registerProperty(PropertyIds.MAXROWS.name, PropertyIds.MAXROWS.id, Type.LONG,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return _getMaxRows();
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    _setMaxRows((int)value);
                }
            });
        registerProperty(PropertyIds.QUERYTIMEOUT.name, PropertyIds.QUERYTIMEOUT.id, Type.LONG,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return _getQueryTimeout();
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    _setQueryTimeout((int)value);
                }
            });
    }


    protected abstract S getStatement() throws SQLException;
    protected abstract XResultSet _getResultSet(java.sql.ResultSet resultset) throws SQLException;
    protected abstract java.sql.ResultSet getGeneratedResult(String query) throws java.sql.SQLException, SQLException;

    protected S setStatement(S statement)
        throws java.sql.SQLException
    {
        if (!m_CursorName.isBlank()) {
            statement.setCursorName(m_CursorName);
        }
        statement.setFetchDirection(m_FetchDirection);
        statement.setFetchSize(m_FetchSize);
        statement.setMaxFieldSize(m_MaxFieldSize);
        statement.setMaxRows(m_MaxRows);
        statement.setQueryTimeout(m_QueryTimeout);
        return statement;
    }

    private synchronized void _setCursorName(String cursor)
        throws WrappedTargetException
    {
        System.out.println("StatementMain._setCursorName() Value: " + cursor);
        m_CursorName = cursor;
        if (m_Statement != null) {
            try {
                m_Statement.setCursorName(cursor);
            }
            catch (java.sql.SQLException e) {
                throw new WrappedTargetException("SQL error", this, UnoHelper.getSQLException(e, this));
            }
        }
    }

    private String _getCursorName()
    {
        return m_CursorName;
    }

    private synchronized void _setFetchDirection(int value)
        throws WrappedTargetException
    {
        m_FetchDirection = value;
        if (m_Statement != null) {
            try {
                m_Statement.setFetchDirection(value);
            }
            catch (java.sql.SQLException e) {
                throw new WrappedTargetException("SQL error", this, UnoHelper.getSQLException(e, this));
            }
        }
    }
    private int _getFetchDirection()
    {
        return m_FetchDirection;
    }

    private synchronized void _setFetchSize(int value)
        throws WrappedTargetException
    {
        m_FetchSize = value;
        if (m_Statement != null) {
            try {
                m_Statement.setFetchSize(value);
            }
            catch (java.sql.SQLException e) {
                throw new WrappedTargetException("SQL error", this, UnoHelper.getSQLException(e, this));
            }
        }
    }
    private int _getFetchSize()
    {
        return m_FetchSize;
    }

    private synchronized void _setMaxFieldSize(int value)
        throws WrappedTargetException
    {
        m_MaxFieldSize = value;
        if (m_Statement != null) {
            try {
                m_Statement.setMaxFieldSize(value);
            }
            catch (java.sql.SQLException e) {
                throw new WrappedTargetException("SQL error", this, UnoHelper.getSQLException(e, this));
            }
        }
    }
    private int _getMaxFieldSize()
    {
        return m_MaxFieldSize;
    }

    private synchronized void _setMaxRows(int value)
        throws WrappedTargetException
    {
        System.out.println("StatementMain._setMaxRows() Value: " + value);
        m_MaxRows = value;
        if (m_Statement != null) {
            try {
                m_Statement.setMaxRows(value);
            }
            catch (java.sql.SQLException e) {
                throw new WrappedTargetException("SQL error", this, UnoHelper.getSQLException(e, this));
            }
        }
    }
    private int _getMaxRows()
    {
        return m_MaxRows;
    }

    private synchronized void _setQueryTimeout(int value)
        throws WrappedTargetException
    {
        m_QueryTimeout = value;
        if (m_Statement != null) {
            try {
                m_Statement.setQueryTimeout(value);
            }
            catch (java.sql.SQLException e) {
                throw new WrappedTargetException("SQL error", this, UnoHelper.getSQLException(e, this));
            }
        }
    }
    private int _getQueryTimeout()
    {
        return m_QueryTimeout;
    }

    private synchronized void _setResultSetConcurrency(int value)
    {
        // FIXME: We are doing lazy loading on Statement because we need this property to create one!!!
        m_ResultSetConcurrency = value;
        if (m_Statement != null) {
            m_logger.logprb(LogLevel.SEVERE, Resources.STR_LOG_STATEMENT_SET_RESULTSET_CONCURRENCY_ERROR, value);
        }
        else {
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_SET_RESULTSET_CONCURRENCY, value);
        }
    }
    private int _getResultSetConcurrency()
    {
        int value = m_ResultSetConcurrency;
        if (m_Statement != null) {
            try {
                value = m_Statement.getResultSetConcurrency();
            }
            catch (java.sql.SQLException e) {
                UnoHelper.getSQLException(e, this);
            }
        }
        m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_RESULTSET_CONCURRENCY, value);
        return value;
    }

    private synchronized void _setResultSetType(int value)
    {
        // FIXME: We are doing lazy loading on Statement because we need this property to create one!!!
        m_ResultSetType = value;
        if (m_Statement != null) {
            m_logger.logprb(LogLevel.SEVERE, Resources.STR_LOG_STATEMENT_SET_RESULTSET_TYPE_ERROR, value);
        }
        else {
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_SET_RESULTSET_TYPE, value);
        }
    }
    private int _getResultSetType()
    {
        int value = m_ResultSetType;
        if (m_Statement != null) {
            try {
                value = m_Statement.getResultSetType();
            }
            catch (java.sql.SQLException e) {
                UnoHelper.getSQLException(e, this);
            }
        }
        m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_RESULTSET_TYPE, value);
        return value;
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
        // XXX: clearWargnings() should not prevent the Statement from lazy loading
        if (m_Connection.getProvider().supportWarningsSupplier())
            WarningsSupplier.clearWarnings(m_Statement, this);
    }

    @Override
    public Object getWarnings() throws SQLException
    {
        // XXX: getWarnings() should not prevent the Statement from lazy loading
        if (m_Connection.getProvider().supportWarningsSupplier())
            return WarningsSupplier.getWarnings(m_Statement, this);
         return Any.VOID;
    }


    // com.sun.star.lang.XComponent
    @Override
    protected synchronized void postDisposing()
    {
        m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_CLOSING);
        super.postDisposing();
        try {
            if (m_Statement != null) {
                m_Statement.close();
            }
        }
        catch (java.sql.SQLException e) {
            m_logger.logp(LogLevel.WARNING, e);
        }
        //m_Statement = null;
    }


    // com.sun.star.sdbc.XCloseable
    @Override
    public void close() throws SQLException
    {
        checkDisposed();
        dispose();
    }


    // com.sun.star.util.XCancellable:
    @Override
    public void cancel()
    {
        try {
            getStatement().cancel();
        }
        catch (SQLException | java.sql.SQLException e) {
            System.out.println("StatementMain.cancel() ERROR");
        }
    }


    // com.sun.star.sdbc.XGeneratedResultSet:
    @Override
    public XResultSet getGeneratedValues() throws SQLException
    {
        checkDisposed();
        ResultSet resultset = null;
        try {
            String command = m_Connection.getProvider().getAutoRetrievingStatement();
            java.sql.ResultSet result = getGeneratedResult(command);
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_RESULTSET);
            resultset = new ResultSet(getConnectionInternal(), result);
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_RESULTSET_ID, resultset.getLogger().getObjectId());
            int count = result.getMetaData().getColumnCount();
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_GENERATED_VALUES_RESULT, count, _getColumnNames(result, count));
        }
        catch (java.sql.SQLException e) {
            m_logger.logprb(LogLevel.SEVERE, Resources.STR_LOG_STATEMENT_GENERATED_VALUES_ERROR, e.getMessage());
            throw UnoHelper.getSQLException(e, this);
        }
        return resultset;
    }

    private String _getColumnNames(java.sql.ResultSet result,
                                   int count)
        throws java.sql.SQLException 
    {
        List<String> names = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            names.add(result.getMetaData().getColumnName(i));
        }
        return String.join(", ", names);
    }

    // com.sun.star.sdbc.XMultipleResults:
    @Override
    public boolean getMoreResults() throws SQLException
    {
        try {
            return getStatement().getMoreResults();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet getResultSet() throws SQLException
    {
        try {
            return _getResultSet(getStatement().getResultSet());
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getUpdateCount() throws SQLException
    {
        try {
            return getStatement().getUpdateCount();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

}
