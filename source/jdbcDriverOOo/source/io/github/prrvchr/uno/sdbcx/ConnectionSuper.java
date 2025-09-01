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
package io.github.prrvchr.uno.sdbcx;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.sql.ResultSet;

import com.sun.star.container.ElementExistException;
import com.sun.star.container.XNameAccess;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XTablesSupplier;
import com.sun.star.sdbcx.XViewsSupplier;
import com.sun.star.uno.Any;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.uno.driver.helper.ComponentHelper;
import io.github.prrvchr.uno.driver.helper.ComposeRule;
import io.github.prrvchr.uno.driver.helper.StandardSQLState;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedSupport;
import io.github.prrvchr.uno.driver.logger.ConnectionLog;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.sdbc.ConnectionBase;


public abstract class ConnectionSuper
    extends ConnectionBase
    implements XTablesSupplier,
               XViewsSupplier {

    protected TableContainerSuper<?> mTables = null;
    private ViewContainer mViews = null;
    private TableListener mListener = null;

    // The constructor method:
    protected ConnectionSuper(XComponentContext ctx,
                              String service,
                              String[] services,
                              Provider provider,
                              String url,
                              Set<String> properties) {
        super(ctx, service, services, provider, url, properties);
    }

    protected Provider getProvider() {
        return super.getProvider();
    }

    protected ConnectionLog getLogger() {
        return super.getLogger();
    }

    // com.sun.star.lang.XComponent
    @Override
    public synchronized void dispose() {
        if (mTables != null) {
            if (mListener != null) {
                mTables.removeContainerListener(mListener);
            }
            mTables.dispose();
            mTables = null;
        }
        if (mViews != null) {
            mViews.dispose();
            mViews = null;
        }
        super.dispose();
    }

    // com.sun.star.sdbcx.XTablesSupplier:
    @Override
    public synchronized XNameAccess getTables() {
        return getTablesInternal();
    }

    // com.sun.star.sdbcx.XViewsSupplier:
    @Override
    public synchronized XNameAccess getViews() {
        return getViewsInternal();
    }

    // Protected methods
    protected synchronized TableContainerSuper<?> getTablesInternal() {
        checkDisposed();
        if (mTables == null) {
            refreshTables();
        }
        return mTables;
    }

    protected synchronized ViewContainer getViewsInternal() {
        checkDisposed();
        if (mViews == null) {
            refreshViews();
        }
        return mViews;
    }

    protected synchronized void refresh() {
        checkDisposed();
        refreshTables();
        refreshViews();
    }

    protected abstract TableContainerSuper<?> getTableContainer(String[] names) throws ElementExistException;
    protected abstract ViewContainer getViewContainer(String[] names) throws ElementExistException;

    private void refreshTables() {
        try {
            String[] names = getTableNames();
            if (mTables == null) {
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_TABLES);
                mTables = getTableContainer(names);
                mListener = new TableListener();
                mTables.addContainerListener(mListener);
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_TABLES_ID,
                                   mTables.getLogger().getObjectId());
            } else {
                mTables.refill(names);
            }
        } catch (ElementExistException | java.sql.SQLException | SQLException e) {
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
    }

    private void refreshViews() {
        try {
            String[] names = getViewNames();
            if (mViews == null) {
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_VIEWS);
                mViews = getViewContainer(names);
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_VIEWS_ID, mViews.getLogger().getObjectId());
            } else {
                mViews.refill(names);
            }
        } catch (ElementExistException | SQLException | java.sql.SQLException e) {
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
    }

    private String[] getTableNames() throws java.sql.SQLException, SQLException {
        // FIXME: It is preferable to display all the entities of the underlying database.
        // FIXME: Filtering tables in Base or creating users with the appropriate rights seems more sensible.
        List<String> names = new ArrayList<>();
        String[] types = getProvider().getConfigSQL().getTableTypes();
        java.sql.ResultSet result = getProvider().getConnection().getMetaData().getTables(null, null, "%", types);
        ComposeRule rule = ComposeRule.InDataManipulation;
        NamedSupport support = getProvider().getNamedSupport(rule);
        try (ResultSet rs = getProvider().getConfigSQL().getResultSetTable(result)) {
            while (rs.next()) {
                String name = buildName(support, rs);
                names.add(name);
            }
        }
        return names.toArray(new String[0]);
    }

    private String[] getViewNames() throws java.sql.SQLException, SQLException {
        List<String> names = new ArrayList<>();
        String[] types = getProvider().getConfigSQL().getViewTypes();
        java.sql.ResultSet rs = getProvider().getConnection().getMetaData().getTables(null, null, "%", types);
        ComposeRule rule = ComposeRule.InDataManipulation;
        NamedSupport support = getProvider().getNamedSupport(rule);
        try (java.sql.ResultSet result = getProvider().getConfigSQL().getResultSetView(rs)) {
            while (result.next()) {
                String name = buildName(support, result);
                names.add(name);
            }
        }
        return names.toArray(new String[0]);
    }

    private String buildName(NamedSupport support, java.sql.ResultSet result)
        throws SQLException {
        try {
            return ComponentHelper.buildName(support, result);
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

}