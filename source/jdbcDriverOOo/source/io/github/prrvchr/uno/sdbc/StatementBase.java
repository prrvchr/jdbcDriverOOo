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

import java.util.Map;

import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XBatchExecution;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbc.XStatement;
import com.sun.star.uno.Any;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.driver.helper.DBTools;
import io.github.prrvchr.uno.driver.helper.QueryHelper;
import io.github.prrvchr.uno.driver.provider.PropertyIds;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class StatementBase
    extends StatementMain
    implements XBatchExecution,
               XStatement {

    private boolean mEscapeProcessing = true;

    // The constructor method:
    public StatementBase(String service,
                        String[] services,
                        ConnectionBase connection) {
        super(service, services, connection);
    }

    @Override
    protected java.sql.ResultSet getJdbcResultSet()
        throws SQLException {
        try {
            mParsed = false;
            return getJdbcStatement().executeQuery(mQuery.getQuery());
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    protected void registerProperties(Map<String, PropertyWrapper> properties) {

        properties.put(PropertyIds.ESCAPEPROCESSING.getName(),
            new PropertyWrapper(Type.BOOLEAN,
                () -> {
                    return _getEscapeProcessing();
                },
                value -> {
                    _setEscapeProcessing((boolean) value);
                }));

        super.registerProperties(properties);
    }

    @Override
    protected java.sql.Statement setStatement(java.sql.Statement statement)
        throws java.sql.SQLException {
        statement.setEscapeProcessing(mEscapeProcessing);
        return super.setStatement(statement);
    }

    protected synchronized void _setEscapeProcessing(boolean value) {
        mEscapeProcessing = value;
        if (mStatement != null) {
            try {
                mStatement.setEscapeProcessing(value);
            } catch (java.sql.SQLException e) {
                // XXX Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    protected boolean _getEscapeProcessing() {
        return mEscapeProcessing;
    }

    @Override
    protected java.sql.Statement getJdbcStatement()
        throws java.sql.SQLException {
        checkDisposed();
        if (mStatement == null) {
            java.sql.Statement statement;
            if (mResultSetType != java.sql.ResultSet.TYPE_FORWARD_ONLY ||
                mResultSetConcurrency != java.sql.ResultSet.CONCUR_READ_ONLY) {
                statement = mConnection.getProvider().getConnection().createStatement(mResultSetType,
                                                                                      mResultSetConcurrency);
            } else {
                statement = mConnection.getProvider().getConnection().createStatement();
            }
            mStatement = setStatement(statement);
        }
        return mStatement;
    }

    // com.sun.star.sdbc.XBatchExecution
    @Override
    public void addBatch(String sql)
        throws SQLException {
        try {
            mQuery = new QueryHelper(mConnection.getProvider(), sql);
            getJdbcStatement().addBatch(sql);
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

    @Override
    public void clearBatch()
        throws SQLException {
        if (mQuery != null) {
            try {
                getJdbcStatement().clearBatch();
            } catch (java.sql.SQLException e) {
                throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
            }
        }
    }

    @Override
    public int[] executeBatch()
        throws SQLException {
        int[] batch = new int[0];
        if (mQuery != null) {
            try {
                batch = getJdbcStatement().executeBatch();
            } catch (java.sql.SQLException e) {
                throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
            }
        }
        return batch;
    }


    // com.sun.star.sdbc.XStatement:
    @Override
    public boolean execute(String sql)
        throws SQLException {
        try {
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_EXECUTE, sql);
            mQuery = new QueryHelper(mConnection.getProvider(), sql);
            return getJdbcStatement().execute(sql);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

    @Override
    public XResultSet executeQuery(String sql)
        throws SQLException {
        try {
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_EXECUTE_QUERY, sql);
            mQuery = new QueryHelper(mConnection.getProvider(), sql);
            return getResultSet();
        } catch (java.sql.SQLException e) {
            throw DBTools.getSQLException(e, this);
        }
    }

    @Override
    public int executeUpdate(String sql)
        throws SQLException {
        try {
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_EXECUTE_UPDATE, sql);
            mQuery = new QueryHelper(mConnection.getProvider(), sql);
            return getJdbcStatement().executeUpdate(sql);
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

    @Override
    public XConnection getConnection()
        throws SQLException {
        return mConnection;
    }


}
