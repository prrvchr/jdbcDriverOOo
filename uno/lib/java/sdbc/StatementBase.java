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

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XBatchExecution;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbc.XStatement;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertySetter;
import io.github.prrvchr.uno.helper.PropertyIds;
import io.github.prrvchr.uno.helper.UnoHelper;


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


    protected void _setEscapeProcessing(boolean value)
    {
        m_EscapeProcessing = value;
        if (m_Statement != null)
        {
            try {
                m_Statement.setEscapeProcessing(value);
            } catch (java.sql.SQLException e) {
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
    protected void _createStatement()
        throws SQLException {
        checkDisposed();
        if (m_Statement == null) {
            try {
                try {
                    m_Statement = m_Connection.getProvider().getConnection().createStatement(m_ResultSetType, m_ResultSetConcurrency);
                    _setStatement();
                } 
                catch (NoSuchMethodError e) {
                    m_Statement = m_Connection.getProvider().getConnection().createStatement();
                    _setStatement();
                }
            } 
            catch (java.sql.SQLException e) {
                throw UnoHelper.getSQLException(e, this);
            }
        }
    }

    protected void _setStatement()
        throws java.sql.SQLException
    {
        super._setStatement();
        m_Statement.setEscapeProcessing(m_EscapeProcessing);
    }


    // com.sun.star.sdbc.XBatchExecution
    @Override
    public void addBatch(String sql) throws SQLException {
        try
        {
            _createStatement();
            m_Statement.addBatch(sql);
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void clearBatch() throws SQLException {
        try
        {
            _createStatement();
            m_Statement.clearBatch();
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int[] executeBatch() throws SQLException {
        try
        {
            _createStatement();
            return m_Statement.executeBatch();
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }


    // com.sun.star.sdbc.XStatement:
    @Override
    public boolean execute(String sql) throws SQLException
    {
        try
        {
            System.out.println("BaseStatement.execute() 1 Query: " + sql);
            _createStatement();
            m_Sql = sql;
            return m_Statement.execute(sql, m_AutoGeneratedKeys);
        } catch (java.sql.SQLException e)
        {
            e.printStackTrace();
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XResultSet executeQuery(String sql) throws SQLException
    {
        try
        {
            System.out.println("BaseStatement.executeQuery() 1 Query: " + sql);
            _createStatement();
            m_Sql = sql;
            java.sql.ResultSet resultset = m_Statement.executeQuery(sql);
            return _getResultSet(resultset);
        } catch (java.sql.SQLException e)
        {
            e.printStackTrace();
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int executeUpdate(String sql) throws SQLException
    {
        try
        {
            System.out.println("BaseStatement.executeUpdate() 1 Query: " + sql);
            _createStatement();
            m_Sql = sql;
            return m_Statement.executeUpdate(sql, m_AutoGeneratedKeys);
        } catch (java.sql.SQLException e)
        {
            e.printStackTrace();
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XConnection getConnection() throws SQLException
    {
        return m_Connection;
    }


}
