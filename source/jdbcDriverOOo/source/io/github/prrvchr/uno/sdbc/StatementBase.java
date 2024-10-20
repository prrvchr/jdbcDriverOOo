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

import java.util.Map;

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XBatchExecution;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbc.XStatement;
import com.sun.star.uno.Any;
import com.sun.star.uno.Type;

import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.helper.SqlCommand;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertySetter;


public abstract class StatementBase
    extends StatementMain
    implements XBatchExecution,
               XStatement
{

    private boolean m_EscapeProcessing = true;

    // The constructor method:
   public StatementBase(String service,
                        String[] services,
                        ConnectionBase connection)
    {
        super(service, services, connection);
    }

   @Override
   protected java.sql.ResultSet getJdbcResultSet()
       throws java.sql.SQLException
   {
        m_parsed = false;
        return getJdbcStatement().executeQuery(m_Sql.getCommand());
   }

   @Override
   protected void registerProperties(Map<String, PropertyWrapper> properties) {

       properties.put(PropertyIds.ESCAPEPROCESSING.getName(),
                      new PropertyWrapper(Type.BOOLEAN,
                                          new PropertyGetter() {
                                              @Override
                                              public Object getValue() throws WrappedTargetException
                                              {
                                                  return _getEscapeProcessing();
                                              }
                                          },
                                          new PropertySetter() {
                                              @Override
                                              public void setValue(Object value) throws PropertyVetoException,
                                                                                        IllegalArgumentException,
                                                                                        WrappedTargetException
                                              {
                                                  _setEscapeProcessing((boolean) value);
                                              }
                                          }));

       super.registerProperties(properties);
   }

    @Override
    protected java.sql.Statement setStatement(java.sql.Statement statement)
        throws java.sql.SQLException
    {
        statement.setEscapeProcessing(m_EscapeProcessing);
        return super.setStatement(statement);
    }

    protected synchronized void _setEscapeProcessing(boolean value)
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
    protected java.sql.Statement getJdbcStatement()
        throws java.sql.SQLException
    {
        checkDisposed();
        if (m_Statement == null) {
            java.sql.Statement statement;
            if (m_ResultSetType != java.sql.ResultSet.TYPE_FORWARD_ONLY || m_ResultSetConcurrency != java.sql.ResultSet.CONCUR_READ_ONLY) {
                statement = m_Connection.getProvider().getConnection().createStatement(m_ResultSetType, m_ResultSetConcurrency);
            } 
            else {
                statement = m_Connection.getProvider().getConnection().createStatement();
            }
            m_Statement = setStatement(statement);
        }
        return m_Statement;
    }

    // com.sun.star.sdbc.XBatchExecution
    @Override
    public void addBatch(String sql)
        throws SQLException
    {
        try {
            m_Sql = new SqlCommand(sql);
            getJdbcStatement().addBatch(sql);
        }
        catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

    @Override
    public void clearBatch()
        throws SQLException
    {
        if (m_Sql != null) {
            try {
                getJdbcStatement().clearBatch();
            }
            catch (java.sql.SQLException e) {
                throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
            }
        }
    }

    @Override
    public int[] executeBatch()
        throws SQLException
    {
        if (m_Sql !=null) {
            try {
                return getJdbcStatement().executeBatch();
            }
            catch (java.sql.SQLException e) {
                throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
            }
        }
        return new int[0];
    }


    // com.sun.star.sdbc.XStatement:
    @Override
    public boolean execute(String sql)
        throws SQLException
    {
        try {
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_EXECUTE, sql);
            m_Sql = new SqlCommand(sql);
            return getJdbcStatement().execute(sql);
        }
        catch (java.sql.SQLException e) {
            e.printStackTrace();
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

    @Override
    public XResultSet executeQuery(String sql)
        throws SQLException
    {
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_EXECUTE_QUERY, sql);
        m_Sql = new SqlCommand(sql);
        return getResultSet();
    }

    @Override
    public int executeUpdate(String sql)
        throws SQLException
    {
        try {
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_EXECUTE_UPDATE, sql);
            m_Sql = new SqlCommand(sql);
            return getJdbcStatement().executeUpdate(sql);
        }
        catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

    @Override
    public XConnection getConnection()
        throws SQLException
    {
        return m_Connection;
    }


}
