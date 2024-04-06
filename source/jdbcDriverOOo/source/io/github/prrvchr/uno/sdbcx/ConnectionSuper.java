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
package io.github.prrvchr.uno.sdbcx;

import java.util.ArrayList;
import java.util.List;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.XNameAccess;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XStatement;
import com.sun.star.sdbcx.XTablesSupplier;
import com.sun.star.sdbcx.XViewsSupplier;
import com.sun.star.uno.Any;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.helper.DBTools;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.uno.sdbc.ConnectionBase;


public abstract class ConnectionSuper
    extends ConnectionBase
    implements XTablesSupplier,
               XViewsSupplier
{

    private TableContainerSuper<?,?> m_Tables = null;
    private ViewContainer m_Views = null;

    protected DriverProvider getProvider()
    {
        return super.getProvider();
    }
    protected ConnectionLog getLogger()
    {
        return super.getLogger();
    }

    // The constructor method:
    public ConnectionSuper(XComponentContext ctx,
                           String service,
                           String[] services,
                           DriverProvider provider,
                           String url,
                           PropertyValue[] info)
    {
        super(ctx, service, services, provider, url, info);
    }

    // com.sun.star.lang.XComponent
    @Override
    protected synchronized void postDisposing() {
        if (m_Tables != null) {
            m_Tables.dispose();
        }
        if (m_Views != null) {
            m_Views.dispose();
        }
        super.postDisposing();
    }

    // com.sun.star.sdbcx.XTablesSupplier:
    @Override
    public synchronized XNameAccess getTables()
    {
        return getTablesInternal();
    }

    // com.sun.star.sdbcx.XViewsSupplier:
    @Override
    public synchronized XNameAccess getViews()
    {
        return getViewsInternal();
    }

    // Protected methods
    protected synchronized TableContainerSuper<?,?> getTablesInternal()
    {
        checkDisposed();
        if (m_Tables == null) {
            refreshTables();
        }
        return m_Tables;
    }

    protected synchronized ViewContainer getViewsInternal()
    {
        checkDisposed();
        if (m_Views == null) {
            refreshViews();
        }
        return m_Views;
    }

    protected synchronized void refresh()
    {
        checkDisposed();
        refreshTables();
        refreshViews();
    }

    @Override
    protected XStatement _getStatement()
    {
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_STATEMENT);
        Statement statement = new Statement(this);
        getStatements().put(statement, statement);
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_STATEMENT_ID, statement.getLogger().getObjectId());
        return statement;
    }

    protected abstract TableContainerSuper<?,?> getTableContainer(List<String> names) throws ElementExistException;
    protected abstract ViewContainer getViewContainer(List<String> names) throws ElementExistException;

    private void refreshTables()
    {
        try {
            // FIXME: It is preferable to display all the entities of the underlying database.
            // FIXME: Filtering tables in Base or creating users with the appropriate rights seems more sensible.
            List<String> names = new ArrayList<>();
            java.sql.DatabaseMetaData metadata = getProvider().getConnection().getMetaData();
            try (java.sql.ResultSet result = metadata.getTables(null, null, "%", getProvider().getTableTypes()))
            {
                while (result.next()) {
                    String name = buildName(result);
                    names.add(name);
                }
            }
            if (m_Tables == null) {
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_TABLES);
                m_Tables = getTableContainer(names);
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_TABLES_ID, m_Tables.getLogger().getObjectId());
            }
            else {
                m_Tables.refill(names);
            }
        }
        catch (ElementExistException | java.sql.SQLException | SQLException e) {
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
    }

    private void refreshViews() {
        try {
            List<String> names = new ArrayList<>();
            java.sql.DatabaseMetaData metadata = getProvider().getConnection().getMetaData();
            try (java.sql.ResultSet result = metadata.getTables(null, null, "%", getProvider().getViewTypes()))
            {
                while (result.next()) {
                    String name = buildName(result);
                    names.add(name);
                }
            }
            if (m_Views == null) {
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_VIEWS);
                m_Views = getViewContainer(names);
                getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_VIEWS_ID, m_Views.getLogger().getObjectId());
            }
            else {
                m_Views.refill(names);
            }
        }
        catch (ElementExistException | SQLException | java.sql.SQLException e) {
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
    }

    private String buildName(java.sql.ResultSet result)
        throws SQLException
    {
        try {
            return DBTools.buildName(getProvider(), result, ComposeRule.InDataManipulation);
        }
        catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

}