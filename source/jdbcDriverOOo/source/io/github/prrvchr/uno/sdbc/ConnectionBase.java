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

//import java.util.Iterator;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbc.XDatabaseMetaData;
import com.sun.star.sdbc.XPreparedStatement;
import com.sun.star.sdbc.XStatement;
import com.sun.star.sdbc.XWarningsSupplier;
import com.sun.star.uno.Any;
import com.sun.star.uno.XComponentContext;
//import com.sun.star.lib.util.WeakMap;

import com.sun.star.lib.uno.helper.ComponentBase;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.SchemaCrawler;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.lang.ServiceInfo;
import schemacrawler.schema.Catalog;


public abstract class ConnectionBase
    extends ComponentBase
    implements XServiceInfo,
               XWarningsSupplier,
               XConnection
{

    protected final XComponentContext m_xContext;
    private final String m_name;
    private final String[] m_services;
    protected final DriverProvider m_provider;
    protected final java.sql.Connection m_Connection;
    protected final PropertyValue[] m_info;
    private final String m_url;
    public final boolean m_enhanced;
    private boolean m_crawler;
    private Catalog m_catalog = null;
    //protected final WeakMap<StatementMain, StatementMain> m_statements = new WeakMap<StatementMain, StatementMain>();

    // The constructor method:
    public ConnectionBase(XComponentContext ctx,
                          String name,
                          String[] services,
                          DriverProvider provider,
                          java.sql.Connection connection,
                          String url,
                          PropertyValue[] info,
                          boolean enhanced)
        throws java.sql.SQLException
    {
        this(ctx, name, services, provider, connection, url, info, enhanced, false);
    }
    public ConnectionBase(XComponentContext ctx,
                          String name,
                          String[] services,
                          DriverProvider provider,
                          java.sql.Connection connection,
                          String url,
                          PropertyValue[] info,
                          boolean enhanced,
                          boolean crawler)
        throws java.sql.SQLException
    {
        super();
        System.out.println("Connection.Connection() 1");
        m_xContext = ctx;
        m_name = name;
        m_services = services;
        m_Connection = connection;
        m_url = url;
        m_info = info;
        m_enhanced = enhanced;
        m_provider = provider;
        m_crawler = crawler;
        if (crawler) {
            m_catalog = SchemaCrawler.getCatalog(connection);
            for (final schemacrawler.schema.Table t : m_catalog.getTables()) {
                System.out.println("Connection.Connection() 2 Table Type: " + t.getTableType().getTableType());
            }
        }
        System.out.println("Connection.Connection() 3");
    }

    // com.sun.star.lang.XComponent
    @Override
    protected synchronized void postDisposing()
    {
        try {
            /*for (Iterator<StatementMain> it = m_statements.keySet().iterator(); it.hasNext();) {
                StatementMain statement = it.next();
                it.remove();
                statement.dispose();
            }*/
            if (m_Connection != null) {
                m_Connection.close();
            }
        }
        catch (java.sql.SQLException e) {
            System.out.println("Connection.postDisposing() ERROR:\n" + UnoHelper.getStackTrace(e));
        }
        catch (java.lang.Exception e) {
            System.out.println("Connection.postDisposing() ERROR:\n" + UnoHelper.getStackTrace(e));
        }
    }

    // com.sun.star.lang.XServiceInfo:
    @Override
    public String getImplementationName()
    {
        return ServiceInfo.getImplementationName(m_name);
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
        if (m_provider.supportWarningsSupplier()) {
            WarningsSupplier.clearWarnings(getWrapper(), this);
        }
    }


    @Override
    public Object getWarnings() throws SQLException
    {
        if (m_provider.supportWarningsSupplier()) {
            return WarningsSupplier.getWarnings(getWrapper(), this);
        }
        return Any.VOID;
    }


    // com.sun.star.sdbc.XConnection:
    @Override
    public XDatabaseMetaData getMetaData() throws SQLException
    {
        XDatabaseMetaData metadata = null;
        try {
            metadata = m_provider.getDatabaseMetaData(this);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        System.out.println("Connection.getMetaData() 1");
        return metadata;
    }

    @Override
    public void close() throws SQLException
    {
        dispose();
    }

    @Override
    public void commit() throws SQLException
    {
        try {
            m_Connection.commit();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean getAutoCommit() throws SQLException
    {
        try {
            return m_Connection.getAutoCommit();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getCatalog() throws SQLException
    {
        try {
            String value = m_Connection.getCatalog();
            System.out.println("Connection.getCatalog() 1 Catalog: " + value);
            return value != null ? value : "";
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getTransactionIsolation() throws SQLException
    {
        try {
            return m_Connection.getTransactionIsolation();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isClosed() throws SQLException
    {
        try {
            return m_Connection.isClosed();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isReadOnly() throws SQLException
    {
        try {
            boolean readonly = m_Connection.isReadOnly();
            System.out.println("Connection.isReadOnly() 1 readonly: " + readonly);
            return readonly;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String nativeSQL(String sql) throws SQLException
    {
        try {
            String value = m_Connection.nativeSQL(sql);
            return value != null ? value : "";
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void rollback() throws SQLException
    {
        try {
            m_Connection.rollback();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setAutoCommit(boolean commit) throws SQLException
    {
        try {
            m_Connection.setAutoCommit(commit);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setCatalog(String catalog) throws SQLException
    {
        try {
            m_Connection.setCatalog(catalog);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setReadOnly(boolean readonly) throws SQLException
    {
        try {
            m_Connection.setReadOnly(readonly);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setTransactionIsolation(int isolation) throws SQLException
    {
        try {
            m_Connection.setTransactionIsolation(isolation);
        }
        catch (java.sql.SQLException e) {
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
         return _getStatement();

    }

    @Override
    public XPreparedStatement prepareStatement(String sql) throws SQLException
    {
        return _getPreparedStatement(sql);

    }

    @Override
    public XPreparedStatement prepareCall(String sql) throws SQLException
    {
        return _getCallableStatement(sql);

    }


    public java.sql.Connection getWrapper()
    {
        return m_Connection;
    }
    public DriverProvider getProvider()
    {
        return m_provider;
    }
    public String getUrl()
    {
        return m_url;
    }
    public PropertyValue[] getInfo()
    {
        return m_info;
    }
    public boolean isEnhanced()
    {
        return m_enhanced;
    }
    public boolean useSchemaCrawler()
    {
        return m_crawler;
    }

    abstract protected XStatement _getStatement();
    abstract protected XPreparedStatement _getPreparedStatement(String sql);
    abstract protected XPreparedStatement _getCallableStatement(String sql);


}