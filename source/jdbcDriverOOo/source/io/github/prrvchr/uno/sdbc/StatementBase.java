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

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XBatchExecution;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbc.XStatement;
import com.sun.star.uno.Type;

import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertySetter;


public abstract class StatementBase<C extends ConnectionBase>
    extends StatementMain<java.sql.Statement, C>
    implements XBatchExecution,
               XStatement
{

    private boolean m_EscapeProcessing = true;

    // The constructor method:
   public StatementBase(String service,
                        String[] services,
                        C connection)
    {
        super(service, services, connection);
        registerProperties();
    }

    private void registerProperties() {
        registerProperty(PropertyIds.ESCAPEPROCESSING.name, PropertyIds.ESCAPEPROCESSING.id, Type.BOOLEAN,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    System.out.println("sdbc.StatementBase._getEscapeProcessing(): " + m_EscapeProcessing);
                    return _getEscapeProcessing();
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    System.out.println("sdbc.StatementBase._setEscapeProcessing(): " + (boolean) value);
                    _setEscapeProcessing((boolean) value);
                }
            });
    }

    protected java.sql.Statement setStatement(java.sql.Statement statement)
        throws java.sql.SQLException
    {
        statement.setEscapeProcessing(m_EscapeProcessing);
        return super.setStatement(statement);
    }

    protected void _setEscapeProcessing(boolean value)
    {
        m_EscapeProcessing = value;
        if (m_Statement != null) {
            try {
                m_Statement.setEscapeProcessing(value);
            }
            catch (java.sql.SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    protected boolean _getEscapeProcessing()
    {
        return m_EscapeProcessing;
    }

    @Override
    protected java.sql.Statement getStatement()
        throws SQLException
    {
        checkDisposed();
        if (m_Statement == null) {
            try {
                java.sql.Statement statement;
                if (m_Connection.getProvider().isResultSetUpdatable() &&
                   (m_ResultSetType != java.sql.ResultSet.TYPE_FORWARD_ONLY || m_ResultSetConcurrency != java.sql.ResultSet.CONCUR_READ_ONLY)) {
                    statement = m_Connection.getProvider().getConnection().createStatement(m_ResultSetType, m_ResultSetConcurrency);
                } 
                else {
                    statement = m_Connection.getProvider().getConnection().createStatement();
                }
                m_Statement = setStatement(statement);
            } 
            catch (java.sql.SQLException e) {
                System.out.println("sdbc.StatementBase.getStatement() ERROR: " + m_ResultSetType + " - " + m_ResultSetConcurrency + " - SQL: '" + m_Sql + "'");
                throw UnoHelper.getSQLException(e, this);
            }
        }
        return m_Statement;
    }

    // com.sun.star.sdbc.XBatchExecution
    @Override
    public void addBatch(String sql)
        throws SQLException
    {
        try {
            getStatement().addBatch(sql);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void clearBatch()
        throws SQLException
    {
        try {
            getStatement().clearBatch();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int[] executeBatch()
        throws SQLException {
        try {
            return getStatement().executeBatch();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


    // com.sun.star.sdbc.XStatement:
    @Override
    public boolean execute(String sql)
        throws SQLException
    {
        try {
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_EXECUTE, sql);
            m_Sql = sql;
            return getStatement().execute(sql);
        }
        catch (java.sql.SQLException e) {
            e.printStackTrace();
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet executeQuery(String sql)
        throws SQLException
    {
        try {
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_EXECUTE_QUERY, sql);
            m_Sql = sql;
            java.sql.ResultSet resultset = getStatement().executeQuery(sql);
            return _getResultSet(resultset);
        }
        catch (java.sql.SQLException e) {
            e.printStackTrace();
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int executeUpdate(String sql)
        throws SQLException
    {
        try {
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_EXECUTE_UPDATE, sql);
            m_Sql = sql;
            return getStatement().executeUpdate(sql);
        }
        catch (java.sql.SQLException e) {
            e.printStackTrace();
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XConnection getConnection()
        throws SQLException
    {
        return m_Connection;
    }


}
