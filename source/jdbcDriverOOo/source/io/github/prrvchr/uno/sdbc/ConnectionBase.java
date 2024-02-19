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

import java.util.Iterator;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.DisposedException;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.lib.util.WeakMap;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XConnection;
import com.sun.star.sdbc.XDatabaseMetaData;
import com.sun.star.sdbc.XPreparedStatement;
import com.sun.star.sdbc.XStatement;
import com.sun.star.sdbc.XWarningsSupplier;
import com.sun.star.uno.Any;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XStringSubstitution;

import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.CustomColumn;
import io.github.prrvchr.jdbcdriver.CustomTypeInfo;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.Tools;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.uno.helper.ResourceBasedEventLogger;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.lang.ServiceInfo;


public abstract class ConnectionBase
    extends ComponentBase
    implements XServiceInfo,
               XWarningsSupplier,
               XConnection
{

    protected final XComponentContext m_xContext;
    private final String m_service;
    private final String[] m_services;
    protected final DriverProvider m_provider;
    private String m_url;
    private PropertyValue[] m_info;
    protected final ConnectionLog m_logger; 
    public final boolean m_enhanced;
    public final boolean m_showsystem;
    public final boolean m_usebookmark;
    protected final WeakMap<StatementMain, StatementMain> m_statements = new WeakMap<StatementMain, StatementMain>();
    private CustomTypeInfo m_typeinforows = null;


    // The constructor method:
    public ConnectionBase(XComponentContext ctx,
                          String service,
                          String[] services,
                          DriverProvider provider,
                          String url,
                          PropertyValue[] info,
                          ResourceBasedEventLogger logger,
                          boolean enhanced,
                          boolean showsystem,
                          boolean usebookmark)
    {
        m_xContext = ctx;
        m_service = service;
        m_services = services;
        m_provider = provider;
        m_url = url;
        m_info = info;
        m_enhanced = enhanced;
        m_showsystem = showsystem;
        m_usebookmark = usebookmark;
        m_logger = new ConnectionLog(logger, LoggerObjectType.CONNECTION);
        m_logger.logprb(LogLevel.INFO, Resources.STR_LOG_GOT_JDBC_CONNECTION, getUrl());
    }

    // com.sun.star.lang.XComponent
    @Override
    protected synchronized void postDisposing()
    {
        m_logger.logprb(LogLevel.INFO, Resources.STR_LOG_SHUTDOWN_CONNECTION);
        try {
            for (Iterator<StatementMain> it = m_statements.keySet().iterator(); it.hasNext();) {
                StatementMain statement = it.next();
                it.remove();
                statement.dispose();
            }
            if (getProvider().getConnection() != null) {
                getProvider().getConnection().close();
            }
        }
        catch (java.sql.SQLException e) {
            m_logger.logp(LogLevel.WARNING, e);
            System.out.println("Connection.postDisposing() ERROR:\n" + UnoHelper.getStackTrace(e));
        }
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
    public void clearWarnings()
        throws SQLException
    {
        checkDisposed();
        if (m_provider.supportWarningsSupplier()) {
            WarningsSupplier.clearWarnings(getProvider().getConnection(), this);
        }
    }


    @Override
    public Object getWarnings()
        throws SQLException
    {
        checkDisposed();
        if (m_provider.supportWarningsSupplier()) {
            return WarningsSupplier.getWarnings(getProvider().getConnection(), this);
        }
        return Any.VOID;
    }


    // com.sun.star.sdbc.XConnection:
    @Override
    public XDatabaseMetaData getMetaData()
        throws SQLException
    {
        checkDisposed();
        DatabaseMetaDataBase metadata = null;
        try {
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_DATABASE_METADATA);
            metadata = m_provider.getDatabaseMetaData(this);
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_DATABASE_METADATA_ID, metadata.getLogger().getObjectId());
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        return metadata;
    }

    @Override
    public void close()
        throws SQLException
    {
        dispose();
    }

    @Override
    public void commit()
        throws SQLException
    {
        try {
            getProvider().getConnection().commit();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean getAutoCommit()
        throws SQLException
    {
        try {
            return getProvider().getConnection().getAutoCommit();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getCatalog()
        throws SQLException
    {
        checkDisposed();
        try {
            System.out.println("Connection.getCatalog() 1");
            String value = getProvider().getConnection().getCatalog();
            System.out.println("Connection.getCatalog() 2 Catalog: " + value);
            return value != null ? value : "";
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getTransactionIsolation()
        throws SQLException
    {
        checkDisposed();
        try {
            return getProvider().getConnection().getTransactionIsolation();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isClosed()
        throws SQLException
    {
        try {
            return getProvider().getConnection().isClosed() && bDisposed;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isReadOnly()
        throws SQLException
    {
        checkDisposed();
        try {
            boolean readonly = getProvider().getConnection().isReadOnly();
            System.out.println("Connection.isReadOnly() 1 readonly: " + readonly);
            return readonly;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String nativeSQL(String sql)
        throws SQLException
    {
        checkDisposed();
        try {
            String value = getProvider().getConnection().nativeSQL(sql);
            return value != null ? value : "";
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void rollback()
        throws SQLException
    {
        try {
            getProvider().getConnection().rollback();
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setAutoCommit(boolean commit)
        throws SQLException
    {
        try {
            getProvider().getConnection().setAutoCommit(commit);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setCatalog(String catalog)
        throws SQLException
    {
        try {
            getProvider().getConnection().setCatalog(catalog);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setReadOnly(boolean readonly)
        throws SQLException
    {
        try {
            getProvider().getConnection().setReadOnly(readonly);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setTransactionIsolation(int isolation)
        throws SQLException
    {
        checkDisposed();
        try {
            getProvider().getConnection().setTransactionIsolation(isolation);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XNameAccess getTypeMap()
        throws SQLException
    {
        checkDisposed();
        // TODO: Implement me!!!
        return null;
    }

    @Override
    public void setTypeMap(XNameAccess typemap)
        throws SQLException
    {
        checkDisposed();
        // TODO: Implement me!!!
        String msg = SharedResources.getInstance().getResourceWithSubstitution(Resources.STR_UNSUPPORTED_FEATURE, "XConnection::setTypeMap");
        throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
    }

    @Override
    public XStatement createStatement()
        throws SQLException
    {
         checkDisposed();
         return _getStatement();

    }

    @Override
    public XPreparedStatement prepareStatement(String sql)
        throws SQLException
    {
        checkDisposed();
        return _getPreparedStatement(_substituteVariables(sql));

    }

    @Override
    public XPreparedStatement prepareCall(String sql)
        throws SQLException
    {
        checkDisposed();
        return _getCallableStatement(_substituteVariables(sql));

    }

    public DriverProvider getProvider()
    {
        return m_provider;
    }

    public ConnectionLog getLogger()
    {
        return m_logger;
    }

    public String getUrl()
    {
        return UnoHelper.getDefaultPropertyValue(m_info, "Url", m_url);
    }

    public PropertyValue[] getInfo()
    {
        return m_info;
    }

    public boolean isEnhanced()
    {
        return m_enhanced;
    }

    public XComponentContext getComponentContext()
    {
        return m_xContext;
    }

    abstract protected XStatement _getStatement();
    abstract protected XPreparedStatement _getPreparedStatement(String sql) throws SQLException;
    abstract protected XPreparedStatement _getCallableStatement(String sql) throws SQLException;

    private String _substituteVariables(String sql)
        throws SQLException
    {
        PropertyValue[] properties = new PropertyValue[1];
        properties[0] = new PropertyValue();
        properties[0].Name = "ActiveConnection";
        properties[0].Value = this;
        try {
            String service = "com.sun.star.sdb.ParameterSubstitution";
            Object object = m_xContext.getServiceManager().createInstanceWithArgumentsAndContext(service, properties, m_xContext);
            XStringSubstitution substitution = UnoRuntime.queryInterface(XStringSubstitution.class, object);
            return substitution.substituteVariables(sql, true);
        }
        catch (com.sun.star.uno.Exception e) {
            throw Tools.toUnoExceptionLogged(this, m_logger, e);
        }
    }

    //XXX: Checks whether this component (which you should have locked, prior to this call, and until you are done using) is disposed, throwing DisposedException if it is.
    protected synchronized final void checkDisposed()
    {
        if (bInDispose || bDisposed) {
            System.out.println("sdbc.ConnectionBase.checkDisposed() ERROR: **************************" + this.getClass().getName());
            throw new DisposedException();
        }
    }

    public String getAutoIncrementCreation()
    {
        return UnoHelper.getDefaultPropertyValue(m_info, "AutoIncrementCreation", "");
    }

    public boolean isIgnoreCurrencyEnabled()
    {
        return UnoHelper.getDefaultPropertyValue(m_info, "IgnoreCurrency", false);
    }

    public CustomColumn[] getTypeInfoRow(CustomColumn[] columns)
            throws SQLException
    {
        Object [] typeinfo = m_provider.getTypeInfoSettings();
        if (typeinfo == null) {
            return columns;
        }
        if (m_typeinforows == null) {
            m_typeinforows = new CustomTypeInfo(typeinfo);
        }
        return m_typeinforows.getTypeInfoRow(columns);
    }

}