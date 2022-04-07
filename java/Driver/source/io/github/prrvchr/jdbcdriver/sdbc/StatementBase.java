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
package io.github.prrvchr.jdbcdriver.sdbc;

import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.star.beans.Property;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XBatchExecution;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbc.XStatement;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.helper.UnoHelper;


public abstract class StatementBase
    extends StatementMain
    implements XBatchExecution,
               XStatement
{

    private XConnection m_xConnection;
    private java.sql.Connection m_Connection;
    private java.sql.Statement m_Statement = null;
    private boolean m_EscapeProcessing = true;
    private static Map<String, Property> _getPropertySet()
    {
        Map<String, Property> map = new LinkedHashMap<String, Property>();
        map.put("EscapeProcessing", UnoHelper.getProperty("EscapeProcessing", "boolean"));
        return map;
    }
    private static Map<String, Property> _getPropertySet(Map<String, Property> properties)
    {
        Map<String, Property> map = _getPropertySet();
        map.putAll(properties);
        return map;
    }

    // The constructor method:
    public StatementBase(XComponentContext context,
                         String name,
                         String[] services,
                         DriverProvider provider,
                         ConnectionBase xConnection,
                         java.sql.Connection connection)
    {
        super(context, name, services, provider, _getPropertySet());
        m_xConnection = xConnection;
        m_Connection = connection;
        
    }
    public StatementBase(XComponentContext context,
                         String name,
                         String[] services,
                         DriverProvider provider,
                         ConnectionBase xConnection,
                         java.sql.Connection connection,
                         Map<String, Property> properties)
    {
        super(context, name, services, provider, _getPropertySet(properties));
        m_xConnection = xConnection;
        m_Connection = connection;
    }


    protected void _setEscapeProcessing(boolean value)
    {
        m_EscapeProcessing = value;
        if (_getWrapper() != null)
        {
            try {
                _getWrapper().setEscapeProcessing(value);
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


    protected java.sql.Statement _getStatement()
        throws java.sql.SQLException
    {
        if (m_Statement == null)
        {
            m_Statement = m_Connection.createStatement(m_ResultSetType, m_ResultSetConcurrency);
            _setStatement(m_Statement);
        }
        return m_Statement;
    }

    protected java.sql.Statement _getWrapper()
    {
        return m_Statement;
    }

    
    // com.sun.star.sdbc.XBatchExecution
    @Override
    public void addBatch(String sql) throws SQLException {
        try
        {
            _getStatement().addBatch(sql);
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void clearBatch() throws SQLException {
        try
        {
            _getStatement().clearBatch();
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int[] executeBatch() throws SQLException {
        try
        {
            return _getStatement().executeBatch();
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
            return _getStatement().execute(sql, m_AutoGeneratedKeys);
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
            java.sql.ResultSet resultset = _getStatement().executeQuery(sql);
            return _getResultSet(m_xContext, resultset);
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
            return _getStatement().executeUpdate(sql, m_AutoGeneratedKeys);
        } catch (java.sql.SQLException e)
        {
            e.printStackTrace();
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XConnection getConnection() throws SQLException
    {
        return m_xConnection;
    }


    protected void _setStatement(java.sql.Statement statement)
    throws java.sql.SQLException
    {
        super._setStatement(statement);
        statement.setEscapeProcessing(m_EscapeProcessing);
    }


}
