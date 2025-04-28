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
import com.sun.star.uno.Any;
import com.sun.star.uno.Type;
import com.sun.star.util.XCancellable;

import io.github.prrvchr.driver.helper.GeneratedKeys;
import io.github.prrvchr.driver.helper.SqlCommand;
import io.github.prrvchr.driver.provider.ConnectionLog;
import io.github.prrvchr.driver.provider.DriverProvider;
import io.github.prrvchr.driver.provider.LoggerObjectType;
import io.github.prrvchr.driver.provider.PropertyIds;
import io.github.prrvchr.driver.provider.Resources;
import io.github.prrvchr.driver.rowset.RowCatalog;
import io.github.prrvchr.uno.helper.PropertySet;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.ServiceInfo;
import io.github.prrvchr.uno.helper.UnoHelper;


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
    protected RowCatalog mCatalog = null;
    protected boolean mParsed = false;
    protected SqlCommand mSql = null;
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
    protected void registerProperties(Map<String, PropertyWrapper> properties) {

        properties.put(PropertyIds.CURSORNAME.getName(),
            new PropertyWrapper(Type.STRING,
                () -> {
                    return _getCursorName();
                },
                value -> {
                    _setCursorName((String) value);
                }));

        properties.put(PropertyIds.FETCHDIRECTION.getName(),
            new PropertyWrapper(Type.LONG,
                () -> {
                    return _getFetchDirection();
                },
                value -> {
                    _setFetchDirection((int) value);
                }));

        properties.put(PropertyIds.FETCHSIZE.getName(),
            new PropertyWrapper(Type.LONG,
                () -> {
                    return _getFetchSize();
                },
                value -> {
                    _setFetchSize((int) value);
                }));

        properties.put(PropertyIds.MAXFIELDSIZE.getName(),
            new PropertyWrapper(Type.LONG,
                () -> {
                    return _getMaxFieldSize();
                },
                value -> {
                    _setMaxFieldSize((int) value);
                }));

        properties.put(PropertyIds.MAXROWS.getName(),
            new PropertyWrapper(Type.LONG,
                () -> {
                    return _getMaxRows();
                },
                value -> {
                    _setMaxRows((int) value);
                }));

        properties.put(PropertyIds.QUERYTIMEOUT.getName(),
            new PropertyWrapper(Type.LONG,
                () -> {
                    return _getQueryTimeout();
                },
                value -> {
                    _setQueryTimeout((int) value);
                }));

        properties.put(PropertyIds.RESULTSETCONCURRENCY.getName(),
            new PropertyWrapper(Type.LONG,
                () -> {
                    return _getResultSetConcurrency();
                },
                value -> {
                    _setResultSetConcurrency((int) value);
                }));

        properties.put(PropertyIds.RESULTSETTYPE.getName(),
            new PropertyWrapper(Type.LONG,
                () -> {
                    return _getResultSetType();
                },
                value -> {
                    _setResultSetType((int) value);
                }));

        super.registerProperties(properties);
    }

    protected abstract java.sql.Statement getJdbcStatement() throws java.sql.SQLException;
    protected abstract java.sql.ResultSet getJdbcResultSet() throws java.sql.SQLException;

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

    private synchronized void _setCursorName(String cursor)
        throws WrappedTargetException {
        System.out.println("StatementMain._setCursorName() Value: " + cursor);
        mCursorName = cursor;
        if (mStatement != null) {
            try {
                mStatement.setCursorName(cursor);
            } catch (java.sql.SQLException e) {
                throw new WrappedTargetException("SQL error", this, UnoHelper.getSQLException(e, this));
            }
        }
    }

    private String _getCursorName() {
        return mCursorName;
    }

    private synchronized void _setFetchDirection(int value)
        throws WrappedTargetException {
        mFetchDirection = value;
        if (mStatement != null) {
            try {
                mStatement.setFetchDirection(value);
            } catch (java.sql.SQLException e) {
                throw new WrappedTargetException("SQL error", this, UnoHelper.getSQLException(e, this));
            }
        }
    }
    private int _getFetchDirection() {
        return mFetchDirection;
    }

    private synchronized void _setFetchSize(int value)
        throws WrappedTargetException {
        System.out.println("StatementMain._setFetchSize() FetchSize: " + value);
        mFetchSize = value;
        if (mStatement != null) {
            try {
                mStatement.setFetchSize(value);
            } catch (java.sql.SQLException e) {
                throw new WrappedTargetException("SQL error", this, UnoHelper.getSQLException(e, this));
            }
        }
    }
    private int _getFetchSize() {
        System.out.println("StatementMain._getFetchSize() FetchSize: " + mFetchSize);
        return mFetchSize;
    }

    private synchronized void _setMaxFieldSize(int value)
        throws WrappedTargetException {
        mMaxFieldSize = value;
        if (mStatement != null) {
            try {
                mStatement.setMaxFieldSize(value);
            } catch (java.sql.SQLException e) {
                throw new WrappedTargetException("SQL error", this, UnoHelper.getSQLException(e, this));
            }
        }
    }
    private int _getMaxFieldSize() {
        return mMaxFieldSize;
    }

    private synchronized void _setMaxRows(int value)
        throws WrappedTargetException {
        System.out.println("StatementMain._setMaxRows() Value: " + value);
        mMaxRows = value;
        if (mStatement != null) {
            try {
                mStatement.setMaxRows(value);
            } catch (java.sql.SQLException e) {
                throw new WrappedTargetException("SQL error", this, UnoHelper.getSQLException(e, this));
            }
        }
    }
    private int _getMaxRows() {
        return mMaxRows;
    }

    private synchronized void _setQueryTimeout(int value)
        throws WrappedTargetException {
        mQueryTimeout = value;
        if (mStatement != null) {
            try {
                mStatement.setQueryTimeout(value);
            } catch (java.sql.SQLException e) {
                throw new WrappedTargetException("SQL error", this, UnoHelper.getSQLException(e, this));
            }
        }
    }
    private int _getQueryTimeout() {
        return mQueryTimeout;
    }

    private synchronized void _setResultSetConcurrency(int value) {
        // FIXME: We are doing lazy loading on Statement because we need this property to create one!!!
        mResultSetConcurrency = value;
        if (mStatement != null) {
            mLogger.logprb(LogLevel.SEVERE, Resources.STR_LOG_STATEMENT_SET_RESULTSET_CONCURRENCY_ERROR, value);
        } else {
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_SET_RESULTSET_CONCURRENCY, value);
        }
    }
    private int _getResultSetConcurrency() {
        int value = mResultSetConcurrency;
        if (mStatement != null) {
            try {
                value = mStatement.getResultSetConcurrency();
            } catch (java.sql.SQLException e) {
                UnoHelper.getSQLException(e, this);
            }
        }
        mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_RESULTSET_CONCURRENCY, value);
        return value;
    }

    private synchronized void _setResultSetType(int value) {
        // FIXME: We are doing lazy loading on Statement because we need this property to create one!!!
        mResultSetType = value;
        if (mStatement != null) {
            mLogger.logprb(LogLevel.SEVERE, Resources.STR_LOG_STATEMENT_SET_RESULTSET_TYPE_ERROR, value);
        } else {
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_SET_RESULTSET_TYPE, value);
        }
    }
    private int _getResultSetType() {
        int value = mResultSetType;
        if (mStatement != null) {
            try {
                value = mStatement.getResultSetType();
            } catch (java.sql.SQLException e) {
                UnoHelper.getSQLException(e, this);
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
        if (mConnection.getProvider().supportWarningsSupplier()) {
            WarningsSupplier.clearWarnings(mStatement, this);
        }
    }

    @Override
    public synchronized Object getWarnings() throws SQLException {
        // XXX: getWarnings() should not prevent the Statement from lazy loading
        Object warning = Any.VOID;
        if (mConnection.getProvider().supportWarningsSupplier()) {
            warning =  WarningsSupplier.getWarnings(mStatement, this);
        }
        return warning;
    }


    // com.sun.star.lang.XComponent
    @Override
    protected synchronized void postDisposing() {
        if (mStatement != null) {
            mLogger.logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_CLOSING);
            super.postDisposing();
            try {
                mStatement.close();
            } catch (java.sql.SQLException e) {
                mLogger.logp(LogLevel.WARNING, e);
            }
            mStatement = null;
        }
    }


    // com.sun.star.sdbc.XCloseable
    @Override
    public synchronized void close() throws SQLException {
        checkDisposed();
        dispose();
    }


    // com.sun.star.util.XCancellable:
    @Override
    public void cancel() {
        if (mSql != null) {
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
        checkDisposed();
        checkSqlCommand();
        ResultSet resultset = null;
        try {
            int resource;
            java.sql.ResultSet result = null;
            DriverProvider provider = mConnection.getProvider();
            String command = provider.getSQLQuery().getAutoRetrievingStatement();
            if (provider.getSQLQuery().isAutoRetrievingEnabled() && command != null) {
                if (command.isBlank()) {
                    result = getJdbcStatement().getGeneratedKeys();
                } else {
                    RowCatalog catalog = getStatementCatalog();
                    if (catalog != null) {
                        resource = Resources.STR_LOG_STATEMENT_GENERATED_VALUES_TABLE;
                        mLogger.logprb(LogLevel.FINE, resource, catalog.getMainTable().getName(), mSql);
                        result = GeneratedKeys.getGeneratedResult(provider, getJdbcStatement(), catalog, command);
                    }
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
            throw UnoHelper.getSQLException(e, this);
        }
        return resultset;
    }

    private RowCatalog getStatementCatalog()
        throws java.sql.SQLException {
        if (mCatalog == null) {
            if (mSql.hasTable() && mSql.isInsertCommand()) {
                mCatalog = new RowCatalog(mConnection.getProvider(), mSql.getTable());
            }
        }
        return mCatalog;
    }

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
            throw UnoHelper.getSQLException(e, this);
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
            throw UnoHelper.getSQLException(e, this);
        }
    }

    public void checkSqlCommand() throws SQLException {
        if (mSql == null) {
            throw UnoHelper.getUnoSQLException("ERROR: checkSqlCommand not set");
        }
    }

}
