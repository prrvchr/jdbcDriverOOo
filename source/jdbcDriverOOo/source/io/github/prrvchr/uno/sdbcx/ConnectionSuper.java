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

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.XNameAccess;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XTablesSupplier;
import com.sun.star.sdbcx.XViewsSupplier;
import com.sun.star.uno.Any;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.driver.helper.DBTools;
import io.github.prrvchr.driver.provider.ComposeRule;
import io.github.prrvchr.driver.provider.ConnectionLog;
import io.github.prrvchr.driver.provider.DriverProvider;
import io.github.prrvchr.driver.provider.Resources;
import io.github.prrvchr.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.sdbc.ConnectionBase;


public abstract class ConnectionSuper
    extends ConnectionBase
    implements XTablesSupplier,
               XViewsSupplier {

    protected TableContainerSuper<?> mTables = null;
    private ViewContainer mViews = null;

    // The constructor method:
    protected ConnectionSuper(XComponentContext ctx,
                              String service,
                              String[] services,
                              DriverProvider provider,
                              String url,
                              PropertyValue[] info,
                              Set<String> properties) {
        super(ctx, service, services, provider, url, info, properties);
    }

    protected DriverProvider getProvider() {
        return super.getProvider();
    }
    protected ConnectionLog getLogger() {
        return super.getLogger();
    }

    // com.sun.star.lang.XComponent
    @Override
    protected synchronized void postDisposing() {
        if (mTables != null) {
            mTables.dispose();
        }
        if (mViews != null) {
            mViews.dispose();
        }
        super.postDisposing();
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

    protected abstract TableContainerSuper<?> getTableContainer(List<String> names) throws ElementExistException;
    protected abstract ViewContainer getViewContainer(List<String> names) throws ElementExistException;

    private void refreshTables() {
        try {
            // FIXME: It is preferable to display all the entities of the underlying database.
            // FIXME: Filtering tables in Base or creating users with the appropriate rights seems more sensible.
            List<String> names = new ArrayList<>();
            java.sql.DatabaseMetaData metadata = getProvider().getConnection().getMetaData();
            try (java.sql.ResultSet result = metadata.getTables(null, null, "%", getProvider().getTableTypes())) {
                while (result.next()) {
                    String name = buildName(result);
                    names.add(name);
                }
            }
            if (mTables == null) {
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_TABLES);
                mTables = getTableContainer(names);
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
            List<String> names = new ArrayList<>();
            java.sql.DatabaseMetaData metadata = getProvider().getConnection().getMetaData();
            try (java.sql.ResultSet result = metadata.getTables(null, null, "%", getProvider().getViewTypes())) {
                while (result.next()) {
                    String name = buildName(result);
                    names.add(name);
                }
            }
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

    private String buildName(java.sql.ResultSet result)
        throws SQLException {
        try {
            return DBTools.buildName(getProvider(), result, ComposeRule.InDataManipulation);
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

}