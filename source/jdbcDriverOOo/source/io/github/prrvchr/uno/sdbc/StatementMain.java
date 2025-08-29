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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XCloseable;
import com.sun.star.sdbc.XGeneratedResultSet;
import com.sun.star.sdbc.XMultipleResults;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbc.XWarningsSupplier;
import com.sun.star.uno.Type;
import com.sun.star.util.XCancellable;

import io.github.prrvchr.uno.driver.config.ConfigSQL;
import io.github.prrvchr.uno.driver.helper.QueryHelper;
import io.github.prrvchr.uno.driver.logger.ConnectionLog;
import io.github.prrvchr.uno.driver.logger.LoggerObjectType;
import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.driver.property.PropertySet;
import io.github.prrvchr.uno.driver.property.PropertyWrapper;
import io.github.prrvchr.uno.driver.provider.DBTools;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.helper.ServiceInfo;


public abstract class StatementMain
    extends PropertySet
    implements XServiceInfo,
               XWarningsSupplier,
               XCancellable,
               XCloseable,
               XGeneratedResultSet,
               XMultipleResults {

    protected ConnectionBase mConnection;
    protected java.sql.Statement mStatement = null;
    protected boolean mParsed = false;
    protected QueryHelper mQuery = null;
    // FIXME: We are doing lazy loading on Statement because we need this property to create one!!!
    protected int mResultSetConcurrency = java.sql.ResultSet.CONCUR_READ_ONLY;
    // FIXME: We are doing lazy loading on Statement because we need this property to create one!!!
    protected int mResultSetType = java.sql.ResultSet.TYPE_FORWARD_ONLY;

    private final String mService;
    private final String[] mServices;
    private final ConnectionLog mLogger;

    private String mCursorName = "";
    private int mFetchDirection = java.sql.ResultSet.FETCH_FORWARD;
    private int mFetchSize = 0;
    private int mMaxFieldSize = 0;
    private int mMaxRows = 0;
    private int mQueryTimeout = 0;


    // The constructor method:
    public StatementMain(String service,
                         String[] services,
                         ConnectionBase connection) {
        mService = service;
        mServices = services;
        mConnection = connection;
        mLogger = new ConnectionLog(connection.getProvider().getLogger(), LoggerObjectType.STATEMENT);
    }

    protected abstract ConnectionBase getConnectionInternal();

    protected ConnectionLog getLogger() {
        return mLogger;
    }

    public java.sql.Statement getGeneratedStatement()
        throws java.sql.SQLException {
        return mConnection.getProvider().getStatement();
    }

    @Override
    protected void registerProperties(Map<PropertyID, PropertyWrapper> properties) {

        properties.put(PropertyID.CURSORNAME,
            new PropertyWrapper(Type.STRING,
                () -> {
                    return getCursorName();
                },
                value -> {
                    setCursorName((String) value);
                }));

        properties.put(PropertyID.FETCHDIRECTION,
            new PropertyWrapper(Type.LONG,
                () -> {
                    return getFetchDirection();
                },
                value -> {
                    setFetchDirection((int) value);
                }));

        properties.put(PropertyID.FETCHSIZE,
            new PropertyWrapper(Type.LONG,
                () -> {
                    return getFetchSize();
                },
                value -> {
                    setFetchSize((int) value);
                }));

        properties.put(PropertyID.MAXFIELDSIZE,
            new PropertyWrapper(Type.LONG,
                () -> {
                    return getMaxFieldSize();
                },
                value -> {
                    setMaxFieldSize((int) value);
                }));

        properties.put(PropertyID.MAXROWS,
            new PropertyWrapper(Type.LONG,
                () -> {
                    return getMaxRows();
                },
                value -> {
                    setMaxRows((int) value);
                }));

        properties.put(PropertyID.QUERYTIMEOUT,
            new PropertyWrapper(Type.LONG,
                () -> {
                    return getQueryTimeout();
                },
                value -> {
                    setQueryTimeout((int) value);
                }));

        properties.put(PropertyID.RESULTSETCONCURRENCY,
            new PropertyWrapper(Type.LONG,
                () -> {
                    return getResultSetConcurrency();
                },
                value -> {
                    setResultSetConcurrency((int) value);
                }));

        properties.put(PropertyID.RESULTSETTYPE,
            new PropertyWrapper(Type.LONG,
                () -> {
                    return getResultSetType();
                },
                value -> {
                    setResultSetType((int) value);
                }));

        super.registerProperties(properties);
    }

    protected abstract java.sql.Statement getJdbcStatement() throws java.sql.SQLException;
    protected abstract java.sql.ResultSet getJdbcResultSet() throws SQLException;

    protected java.sql.Statement setStatement(java.sql.Statement statement)
        throws java.sql.SQLException {
        if (!mCursorName.isBlank()) {
            statement.setCursorName(mCursorName);
        }
        statement.setFetchDirection(mFetchDirection);
        statement.setFetchSize(mFetchSize);
        statement.setMaxFieldSize(mMaxFieldSize);
        statement.setMaxRows(mMaxRows);
        statement.setQueryTimeout(mQueryTimeout);
        return statement;
    }

    private synchronized void setCursorName(String cursor)
        throws WrappedTargetException {
        System.out.println("StatementMain._setCursorName() Value: " + cursor);
        mCursorName = cursor;
        if (mStatement != null) {
            try {
                mStatement.setCursorName(cursor);
            } catch (java.sql.SQLException e) {
                throw new WrappedTargetException("SQL error", this, DBTools.getSQLException(e, this));
            }
        }
    }

    private String getCursorName() {
        return mCursorName;
    }

    private synchronized void setFetchDirection(int value)
        throws WrappedTargetException {
        mFetchDirection = value;
        if (mStatement != null) {
            try {
                mStatement.setFetchDirection(value);
            } catch (java.sql.SQLException e) {
                throw new WrappedTargetException("SQL error", this, DBTools.getSQLException(e, this));
            }
        }
    }
    private int getFetchDirection() {
        return mFetchDirection;
    }

    private synchronized void setFetchSize(int value)
        throws WrappedTargetException {
        System.out.println("StatementMain._setFetchSize() FetchSize: " + value);
        mFetchSize = value;
        if (mStatement != null) {
            try {
                mStatement.setFetchSize(value);
            } catch (java.sql.SQLException e) {
                throw new WrappedTargetException("SQL error", this, DBTools.getSQLException(e, this));
            }
        }
    }
    private int getFetchSize() {
        System.out.println("StatementMain._getFetchSize() FetchSize: " + mFetchSize);
        return mFetchSize;
    }

    private synchronized void setMaxFieldSize(int value)
        throws WrappedTargetException {
        mMaxFieldSize = value;
        if (mStatement != null) {
            try {
                mStatement.setMaxFieldSize(value);
            } catch (java.sql.SQLException e) {
                throw new WrappedTargetException("SQL error", this, DBTools.getSQLException(e, this));
            }
        }
    }
    private int getMaxFieldSize() {
        return mMaxFieldSize;
    }

    private synchronized void setMaxRows(int value)
        throws WrappedTargetException {
        System.out.println("StatementMain._setMaxRows() Value: " + value);
        mMaxRows = value;
        if (mStatement != null) {
            try {
                mStatement.setMaxRows(value);
            } catch (java.sql.SQLException e) {
                throw new WrappedTargetException("SQL error", this, DBTools.getSQLException(e, this));
            }
        }
    }
    private int getMaxRows() {
        return mMaxRows;
    }

    private synchronized void setQueryTimeout(int value)
        throws WrappedTargetException {
        mQueryTimeout = value;
        if (mStatement != null) {
            try {
                mStatement.setQueryTimeout(value);
            } catch (java.sql.SQLException e) {
                throw new WrappedTargetException("SQL error", this, DBTools.getSQLException(e, this));
            }
        }
    }
    private int getQueryTimeout() {
        return mQueryTimeout;
    }

    private synchronized void setResultSetConcurrency(int value) {
        // FIXME: We are doing lazy loading on Statement because we need this property to create one!!!
        mResultSetConcurrency = value;
        if (mStatement != null) {
            mLogger.logprb(LogLevel.SEVERE, Resources.STR_LOG_STATEMENT_SET_RESULTSET_CONCURRENCY_ERROR, value);
        } else {
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_SET_RESULTSET_CONCURRENCY, value);
        }
    }
    private int getResultSetConcurrency() {
        int value = mResultSetConcurrency;
        if (mStatement != null) {
            try {
                value = mStatement.getResultSetConcurrency();
            } catch (java.sql.SQLException e) {
                DBTools.getSQLException(e, this);
            }
        }
        mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_RESULTSET_CONCURRENCY, value);
        return value;
    }

    private synchronized void setResultSetType(int value) {
        // FIXME: We are doing lazy loading on Statement because we need this property to create one!!!
        mResultSetType = value;
        if (mStatement != null) {
            mLogger.logprb(LogLevel.SEVERE, Resources.STR_LOG_STATEMENT_SET_RESULTSET_TYPE_ERROR, value);
        } else {
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_SET_RESULTSET_TYPE, value);
        }
    }
    private int getResultSetType() {
        int value = mResultSetType;
        if (mStatement != null) {
            try {
                value = mStatement.getResultSetType();
            } catch (java.sql.SQLException e) {
                DBTools.getSQLException(e, this);
            }
        }
        mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_RESULTSET_TYPE, value);
        return value;
    }


    // com.sun.star.lang.XServiceInfo:
    @Override
    public String getImplementationName() {
        return ServiceInfo.getImplementationName(mService);
    }

    @Override
    public String[] getSupportedServiceNames() {
        return ServiceInfo.getSupportedServiceNames(mServices);
    }

    @Override
    public boolean supportsService(String service) {
        return ServiceInfo.supportsService(mServices, service);
    }


    // com.sun.star.sdbc.XWarningsSupplier:
    @Override
    public synchronized void clearWarnings() throws SQLException {
        // XXX: clearWargnings() should not prevent the Statement from lazy loading
        WarningsSupplier.clearWarnings(mStatement, this);
    }

    @Override
    public synchronized Object getWarnings() throws SQLException {
        // XXX: getWarnings() should not prevent the Statement from lazy loading
        return WarningsSupplier.getWarnings(mStatement, this);
    }


    // com.sun.star.lang.XComponent
    @Override
    public synchronized void dispose() {
        if (mStatement != null) {
            try {
                mStatement.close();
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
            mStatement = null;
            super.dispose();
        }
    }


    // com.sun.star.sdbc.XCloseable
    @Override
    public synchronized void close() throws SQLException {
        checkDisposed();
        mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_CLOSING);
        dispose();
    }


    // com.sun.star.util.XCancellable:
    @Override
    public void cancel() {
        if (mQuery != null) {
            try {
                getJdbcStatement().cancel();
            } catch (java.sql.SQLException e) {
                System.out.println("StatementMain.cancel() ERROR");
            }
        }
    }


    // com.sun.star.sdbc.XGeneratedResultSet:
    @Override
    public synchronized XResultSet getGeneratedValues() throws SQLException {
        try {
            checkDisposed();
            checkSqlCommand();
            java.sql.ResultSet result = getGeneratedValues(mConnection.getProvider().getConfigSQL(), mStatement);
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_RESULTSET);
            ResultSet resultset = new ResultSet(getConnectionInternal(), result);
            String services = String.join(", ", resultset.getSupportedServiceNames());
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_RESULTSET_ID, services,
                           resultset.getLogger().getObjectId());
            int count = result.getMetaData().getColumnCount();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_GENERATED_VALUES_RESULT,
                           count, getColumnNames(result, count));
            return resultset;
        } catch (java.sql.SQLException e) {
            throw DBTools.getSQLException(e, this);
        }
    }
    
    /*try {
            int resource;
            java.sql.ResultSet result = null;
            DriverProvider provider = mConnection.getProvider();
            String command = provider.getSQLQuery().getAutoRetrievingStatement();
            if (provider.getSQLQuery().isAutoRetrievingEnabled() && command != null) {
                if (command.isBlank()) {
                    result = getJdbcStatement().getGeneratedKeys();
                } else if (mSql.hasTable()) {
                    String query = String.format(command, mSql.getTable());
                    resource = Resources.STR_LOG_STATEMENT_GENERATED_VALUES_TABLE;
                    mLogger.logprb(LogLevel.FINE, resource, query);
                    result = GeneratedKeys.getGeneratedResult(provider, getJdbcStatement(), query);
                }
            }
            if (result == null) {
                resource = Resources.STR_LOG_STATEMENT_GENERATED_VALUES_QUERY;
                String query = provider.getSQLQuery().getEmptyResultSetQuery();
                mLogger.logprb(LogLevel.FINE, resource, query);
                result = provider.getStatement().executeQuery(query);
            }
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_RESULTSET);
            resultset = new ResultSet(getConnectionInternal(), result);
            String services = String.join(", ", resultset.getSupportedServiceNames());
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_RESULTSET_ID, services,
                           resultset.getLogger().getObjectId());
            int count = result.getMetaData().getColumnCount();
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_GENERATED_VALUES_RESULT,
                           count, getColumnNames(result, count));
        } catch (java.sql.SQLException e) {
            mLogger.logprb(LogLevel.SEVERE, Resources.STR_LOG_STATEMENT_GENERATED_VALUES_ERROR, e.getMessage());
            throw DBTools.getSQLException(e, this);
        }
        return resultset;
    } */

    protected abstract java.sql.ResultSet getGeneratedValues(ConfigSQL config, java.sql.Statement statement)
        throws SQLException;

    private String getColumnNames(java.sql.ResultSet result,
                                   int count)
        throws java.sql.SQLException  {
        List<String> names = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            names.add(result.getMetaData().getColumnName(i));
        }
        return String.join(", ", names);
    }


    // com.sun.star.sdbc.XMultipleResults:
    @Override
    public boolean getMoreResults() throws SQLException {
        checkSqlCommand();
        try {
            return getJdbcStatement().getMoreResults();
        } catch (java.sql.SQLException e) {
            throw DBTools.getSQLException(e, this);
        }
    }

    @Override
    public abstract XResultSet getResultSet() throws SQLException;

    @Override
    public int getUpdateCount() throws SQLException {
        checkSqlCommand();
        try {
            return getJdbcStatement().getUpdateCount();
        } catch (java.sql.SQLException e) {
            throw DBTools.getSQLException(e, this);
        }
    }

    public void checkSqlCommand() throws SQLException {
        if (mQuery == null) {
            throw DBTools.getSQLException("ERROR: checkSqlCommand not set");
        }
    }

}
