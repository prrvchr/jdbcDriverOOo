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

import java.util.ArrayList;
import java.util.List;

import com.sun.star.container.ElementExistException;
import com.sun.star.container.XNameAccess;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XTablesSupplier;
import com.sun.star.sdbcx.XViewsSupplier;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DataBaseTools;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.uno.helper.ResourceBasedEventLogger;
import io.github.prrvchr.uno.sdbcx.TableContainerBase;
import io.github.prrvchr.uno.sdbcx.ViewContainer;


public abstract class ConnectionSuper
    extends ConnectionBase
    implements XTablesSupplier,
               XViewsSupplier

{

    private TableContainerBase m_Tables = null;
    private ViewContainer m_Views = null;

    // The constructor method:
    public ConnectionSuper(XComponentContext ctx,
                           String service,
                           String[] services,
                           DriverProvider provider,
                           ResourceBasedEventLogger logger,
                           boolean enhanced,
                           boolean showsystem,
                           boolean usebookmark)
    {
        super(ctx, service, services, provider, logger, enhanced, showsystem, usebookmark);
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
        checkDisposed();
        if (m_Tables == null) {
            _refreshTables();
        }
        return m_Tables;
    }

    // com.sun.star.sdbcx.XViewsSupplier:
    @Override
    public synchronized XNameAccess getViews()
    {
        checkDisposed();
        if (m_Views == null) {
            _refreshViews();
        }
        return m_Views;
    }


    public synchronized TableContainerBase getTablesInternal()
    {
        return m_Tables;
    }

    public synchronized void _refresh()
    {
        checkDisposed();
        _refreshTables();
        _refreshViews();
    }

    private void _refreshTables()
    {
        try {
            // FIXME: It is preferable to display all the entities of the underlying database.
            // FIXME: Filtering tables in Base or creating users with the appropriate rights seems more sensible.
            String[] types = (m_showsystem) ? null : m_provider.getTableTypes();
            System.out.println("sdbc.ConnectionSuper._refreshTables() Show Sytem Table: " + m_showsystem);
            java.sql.DatabaseMetaData metadata = getProvider().getConnection().getMetaData();
            java.sql.ResultSet result = metadata.getTables(null, null, "%", types);
            List<String> names = new ArrayList<>();
            while (result.next()) {
                String name = _buildName(result);
                names.add(name);
            }
            result.close();
            if (m_Tables == null) {
                m_Tables = _getTableContainer(names);
            }
            else {
                m_Tables.refill(names);
            }
        }
        catch (ElementExistException | java.sql.SQLException | SQLException e) {
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
    }

    public void _refreshViews() {
        try {
            java.sql.DatabaseMetaData metadata = getProvider().getConnection().getMetaData();
            java.sql.ResultSet result = metadata.getTables(null, null, "%", m_provider.getViewTypes(m_showsystem));
            List<String> names = new ArrayList<>();
            while (result.next()) {
                String name = _buildName(result);
                System.out.println("sdb.Connection._refreshViews() View Name: " + name);
                names.add(name);
            }
            result.close();
            if (m_Views == null) {
                m_Views = new ViewContainer(this, getProvider().isCaseSensitive(null), names);
            }
            else {
                m_Views.refill(names);
            }
        }
        catch (ElementExistException | SQLException | java.sql.SQLException e) {
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
    }

    protected String _buildName(java.sql.ResultSet result)
        throws SQLException
    {
        return DataBaseTools.buildName(this, result, ComposeRule.InDataManipulation);
    }

    protected abstract TableContainerBase _getTableContainer(List<String> names) throws ElementExistException;


}