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

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XNameAccess;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbc.XDatabaseMetaData;
import com.sun.star.sdbc.XPreparedStatement;
import com.sun.star.sdbc.XStatement;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.SchemaCrawler;
import io.github.prrvchr.jdbcdriver.helper.UnoHelper;
import schemacrawler.schema.Catalog;


public abstract class ConnectionBase
    extends WarningsSupplierComponent<java.sql.Connection>
    implements XConnection
{

    private final XComponentContext m_xContext;
    protected final java.sql.Connection m_Connection;
    protected final PropertyValue[] m_info;
    private final String m_url;
    protected final DriverProvider m_provider;
    private Catalog m_catalog = null;

    // The constructor method:
    public ConnectionBase(XComponentContext ctx,
                          String name,
                          String[] services,
                          DriverProvider provider,
                          java.sql.Connection connection,
                          PropertyValue[] info,
                          String url)
        throws java.sql.SQLException
    {
        this(ctx, name, services, provider, connection, info, url, false);
    }
    public ConnectionBase(XComponentContext ctx,
                          String name,
                          String[] services,
                          DriverProvider provider,
                          java.sql.Connection connection,
                          PropertyValue[] info,
                          String url,
                          boolean crawler)
        throws java.sql.SQLException
    {
        super(name , services, provider.supportWarningsSupplier());
        System.out.println("Connection.Connection() 1");
        m_xContext = ctx;
        m_Connection = connection;
        m_info = info;
        m_url = url;
        m_provider = provider;
        if (crawler)
        {
            m_catalog = SchemaCrawler.getCatalog(connection);
            for (final schemacrawler.schema.Table t : m_catalog.getTables())
            {
                System.out.println("Connection.Connection() 2 Table Type: " + t.getTableType().getTableType());
            }
        }
        System.out.println("Connection.Connection() 3");
    }


    // com.sun.star.sdbc.XConnection:
    @Override
    public XDatabaseMetaData getMetaData() throws SQLException
    {
        try
        {
            XDatabaseMetaData metadata = m_provider.getDatabaseMetaData(m_xContext, this, m_Connection.getMetaData(), m_info, m_url);
            System.out.println("Connection.getMetaData() 1");
            return metadata;
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void close() throws SQLException
    {
        try
        {
            if (! m_Connection.isClosed())
            {
                m_Connection.close();
                dispose();
            }
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
        
    }

    @Override
    public void commit() throws SQLException
    {
        try
        {
            m_Connection.commit();
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean getAutoCommit() throws SQLException
    {
        try
        {
            return m_Connection.getAutoCommit();
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getCatalog() throws SQLException
    {
        try
        {
            String value = m_Connection.getCatalog();
            System.out.println("Connection.getCatalog() 1 Catalog: " + value);
            return value != null ? value : "";
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getTransactionIsolation() throws SQLException
    {
        try
        {
            return m_Connection.getTransactionIsolation();
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isClosed() throws SQLException
    {
        try
        {
            return m_Connection.isClosed();
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isReadOnly() throws SQLException
    {
        try
        {
            boolean readonly = m_Connection.isReadOnly();
            System.out.println("Connection.isReadOnly() 1 readonly: " + readonly);
            return readonly;
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String nativeSQL(String sql) throws SQLException
    {
        try
        {
            String value = m_Connection.nativeSQL(sql);
            return value != null ? value : "";
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void rollback() throws SQLException
    {
        try
        {
            m_Connection.rollback();
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setAutoCommit(boolean commit) throws SQLException
    {
        try
        {
            m_Connection.setAutoCommit(commit);
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setCatalog(String catalog) throws SQLException
    {
        try
        {
            m_Connection.setCatalog(catalog);
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setReadOnly(boolean readonly) throws SQLException
    {
        try
        {
            m_Connection.setReadOnly(readonly);
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setTransactionIsolation(int isolation) throws SQLException
    {
        try
        {
            m_Connection.setTransactionIsolation(isolation);
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XNameAccess getTypeMap() throws SQLException
    {
        System.out.println("Connection.getTypeMap() 1");
        // TODO: Implement me!!!
        return null;
    }

    @Override
    public void setTypeMap(XNameAccess typemap) throws SQLException
    {
        System.out.println("Connection.setTypeMap() 1 : " + typemap);
        // TODO: Implement me!!!
    }

    @Override
    public XStatement createStatement() throws SQLException
    {
        try {
            return _getStatement(m_xContext, m_provider, m_Connection);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XPreparedStatement prepareStatement(String sql) throws SQLException
    {
        try {
            return _getPreparedStatement(m_xContext, m_provider, m_Connection, sql);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XPreparedStatement prepareCall(String sql) throws SQLException
    {
        try {
            return _getCallableStatement(m_xContext, m_provider, m_Connection, sql);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    protected java.sql.Connection _getWrapper()
    {
        return m_Connection;
    }
    
    abstract protected XStatement _getStatement(XComponentContext ctx,
                                                DriverProvider provider,
                                                java.sql.Connection connection)
        throws java.sql.SQLException;

    abstract protected XPreparedStatement _getPreparedStatement(XComponentContext ctx,
                                                                DriverProvider provider,
                                                                java.sql.Connection connection,
                                                                String sql)
        throws java.sql.SQLException;

    abstract protected XPreparedStatement _getCallableStatement(XComponentContext ctx,
                                                                DriverProvider provider,
                                                                java.sql.Connection connection,
                                                                String sql)
        throws java.sql.SQLException;


}