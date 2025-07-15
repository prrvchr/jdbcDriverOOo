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

import java.util.Iterator;
import java.util.Set;

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

import io.github.prrvchr.uno.driver.provider.ConnectionLog;
import io.github.prrvchr.uno.driver.provider.PropertiesHelper;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.driver.provider.Tools;
import io.github.prrvchr.uno.helper.ServiceInfo;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.UnoHelper;


public abstract class ConnectionBase
    extends ComponentBase
    implements XServiceInfo,
               XWarningsSupplier,
               XConnection {

    private final XComponentContext mContext;
    private final String mService;
    private final String[] mServices;
    private final Provider mProvider;
    private final WeakMap<StatementMain, StatementMain> mStatements = new WeakMap<StatementMain, StatementMain>();

    // The constructor method:
    protected ConnectionBase(XComponentContext ctx,
                             String service,
                             String[] services,
                             Provider provider,
                             String url,
                             Set<String> properties) {
        mContext = ctx;
        mService = service;
        mServices = services;
        mProvider = provider;
        getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_CONNECTION_ESTABLISHED,
                           PropertiesHelper.getJdbcUrl(url), String.join(", ", properties));
        System.out.println("ConnectionBase() 1");
    }

    protected Provider getProvider() {
        return mProvider;
    }
    protected ConnectionLog getLogger() {
        return mProvider.getLogger();
    }
    protected WeakMap<StatementMain, StatementMain> getStatements() {
        return mStatements;
    }

    // com.sun.star.lang.XComponent
    @Override
    protected synchronized void postDisposing() {
        getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_CONNECTION_SHUTDOWN);
        try {
            for (Iterator<StatementMain> it = mStatements.keySet().iterator(); it.hasNext();) {
                StatementMain statement = it.next();
                it.remove();
                statement.dispose();
            }
            getProvider().closeConnection();
        } catch (java.sql.SQLException e) {
            getLogger().logp(LogLevel.WARNING, e);
            System.out.println("Connection.postDisposing() ERROR:\n" + UnoHelper.getStackTrace(e));
        }
        super.postDisposing();
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
    public void clearWarnings()
        throws SQLException {
        checkDisposed();
        if (getProvider().supportWarningsSupplier()) {
            try {
                
                WarningsSupplier.clearWarnings(getProvider().getConnection(), this);
            } catch (java.sql.SQLException e) {
                throw UnoHelper.getSQLException(e, this);
            }
        }
    }

    @Override
    public Object getWarnings()
        throws SQLException {
        Object warging = Any.VOID;
        checkDisposed();
        if (getProvider().supportWarningsSupplier()) {
            try {
                warging = WarningsSupplier.getWarnings(getProvider().getConnection(), this);
            } catch (java.sql.SQLException e) {
                throw UnoHelper.getSQLException(e, this);
            }
        }
        return warging;
    }


    // com.sun.star.sdbc.XConnection:
    @Override
    public XDatabaseMetaData getMetaData()
        throws SQLException {
        checkDisposed();
        DatabaseMetaData metadata = null;
        try {
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_DATABASE_METADATA);
            metadata = new DatabaseMetaData(this);
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_DATABASE_METADATA_ID,
                               metadata.getLogger().getObjectId());
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        return metadata;
    }

    @Override
    public void close()
        throws SQLException {
        dispose();
    }

    @Override
    public void commit()
        throws SQLException {
        try {
            getProvider().getConnection().commit();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean getAutoCommit()
        throws SQLException {
        try {
            return getProvider().getConnection().getAutoCommit();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getCatalog()
        throws SQLException {
        checkDisposed();
        try {
            System.out.println("Connection.getCatalog() 1");
            String value = getProvider().getConnection().getCatalog();
            System.out.println("Connection.getCatalog() 2 Catalog: " + value);
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getTransactionIsolation()
        throws SQLException {
        checkDisposed();
        try {
            return getProvider().getConnection().getTransactionIsolation();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isClosed()
        throws SQLException {
        try {
            return getProvider().getConnection().isClosed() && bDisposed;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isReadOnly()
        throws SQLException {
        checkDisposed();
        try {
            boolean readonly = getProvider().getConnection().isReadOnly();
            System.out.println("Connection.isReadOnly() 1 readonly: " + readonly);
            return readonly;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String nativeSQL(String sql)
        throws SQLException {
        checkDisposed();
        try {
            String value = getProvider().getConnection().nativeSQL(sql);
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void rollback()
        throws SQLException {
        try {
            getProvider().getConnection().rollback();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setAutoCommit(boolean commit)
        throws SQLException {
        try {
            getProvider().getConnection().setAutoCommit(commit);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setCatalog(String catalog)
        throws SQLException {
        try {
            getProvider().getConnection().setCatalog(catalog);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setReadOnly(boolean readonly)
        throws SQLException {
        try {
            getProvider().getConnection().setReadOnly(readonly);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public void setTransactionIsolation(int isolation)
        throws SQLException {
        checkDisposed();
        try {
            getProvider().getConnection().setTransactionIsolation(isolation);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public XNameAccess getTypeMap()
        throws SQLException {
        checkDisposed();
        // XXX: Implement me!!!
        return null;
    }

    @Override
    public void setTypeMap(XNameAccess typemap)
        throws SQLException {
        checkDisposed();
        // XXX: Implement me!!!
        String msg = SharedResources.getInstance().getResourceWithSubstitution(Resources.STR_UNSUPPORTED_FEATURE,
                                                                               "XConnection::setTypeMap");
        throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
    }

    @Override
    public XStatement createStatement()
        throws SQLException {
        checkDisposed();
        return getStatement();

    }

    @Override
    public XPreparedStatement prepareStatement(String sql)
        throws SQLException {
        checkDisposed();
        return getPreparedStatement(sql);

    }

    @Override
    public XPreparedStatement prepareCall(String sql)
        throws SQLException {
        checkDisposed();
        return getCallableStatement(sql);

    }

    protected XComponentContext getComponentContext() {
        return mContext;
    }

    protected abstract XStatement getStatement();
    protected abstract XPreparedStatement getPreparedStatement(String sql) throws SQLException;
    protected abstract XPreparedStatement getCallableStatement(String sql) throws SQLException;

    @SuppressWarnings("unused")
    private String _substituteVariables(String sql)
        throws SQLException {
        PropertyValue[] properties = new PropertyValue[1];
        properties[0] = new PropertyValue();
        properties[0].Name = "ActiveConnection";
        properties[0].Value = this;
        try {
            String service = "com.sun.star.sdb.ParameterSubstitution";
            Object object = mContext.getServiceManager().createInstanceWithArgumentsAndContext(service,
                    properties, mContext);
            XStringSubstitution substitution = UnoRuntime.queryInterface(XStringSubstitution.class, object);
            return substitution.substituteVariables(sql, true);
        } catch (com.sun.star.uno.Exception e) {
            throw Tools.toUnoExceptionLogged(this, getLogger(), e);
        }
    }

    // XXX: Checks whether this component (which you should have locked, prior to this call,
    // XXX: and until you are done using) is disposed, throwing DisposedException if it is.
    protected final synchronized void checkDisposed() {
        if (bInDispose || bDisposed) {
            String msg = "sdbc.ConnectionBase.checkDisposed() ERROR: **************************";
            System.out.println(msg + this.getClass().getName());
            throw new DisposedException();
        }
    }

}