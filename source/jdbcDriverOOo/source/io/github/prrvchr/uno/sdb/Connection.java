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
package io.github.prrvchr.uno.sdb;

import java.util.ArrayList;
import java.util.List;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.XChild;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.NoSupportException;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.sdb.XCommandPreparation;
import com.sun.star.sdb.XQueriesSupplier;
import com.sun.star.sdb.XSQLQueryComposer;
import com.sun.star.sdb.XSQLQueryComposerFactory;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XPreparedStatement;
import com.sun.star.sdbc.XStatement;
import com.sun.star.sdbcx.XGroupsSupplier;
import com.sun.star.sdbcx.XTablesSupplier;
import com.sun.star.sdbcx.XUsersSupplier;
import com.sun.star.sdbcx.XViewsSupplier;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DataBaseTools;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbcx.GroupContainer;
import io.github.prrvchr.uno.sdbcx.Statement;
//import io.github.prrvchr.uno.sdbcx.Column;
//import io.github.prrvchr.uno.sdbcx.Table;
//import io.github.prrvchr.uno.sdbcx.TableDescriptor;
import io.github.prrvchr.uno.sdbcx.TableContainer;
import io.github.prrvchr.uno.sdbcx.UserContainer;
import io.github.prrvchr.uno.sdbcx.ViewContainer;


public final class Connection
    extends ConnectionBase
    implements XChild,
               XCommandPreparation,
               XQueriesSupplier,
               XSQLQueryComposerFactory,
               XMultiServiceFactory,
               XTablesSupplier,
               XUsersSupplier,
               XViewsSupplier,
               XGroupsSupplier
{

    private static final String m_service = Connection.class.getName();
    private static final String[] m_services = {"com.sun.star.sdb.Connection",
                                                "com.sun.star.sdbc.Connection",
                                                "com.sun.star.sdbcx.DatabaseDefinition"};
    private TableContainer m_Tables = null;
    private ViewContainer m_Views = null;
    private UserContainer m_Users = null;
    private GroupContainer m_Groups = null;
    private static final boolean m_crawler = false;

    // The constructor method:
    public Connection(XComponentContext ctx,
                      DriverProvider provider,
                      String url,
                      PropertyValue[] info,
                      boolean enhanced)
    {
        super(ctx, m_service, m_services, provider, url, info, enhanced, m_crawler);
        System.out.println("sdb.Connection() 1");
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
        if (m_Users != null) {
            m_Users.dispose();
        }
        if (m_Groups != null) {
            m_Groups.dispose();
        }
        super.postDisposing();
    }
 
    // com.sun.star.container.XChild:
    @Override
    public Object getParent()
    {
        System.out.println("sdb.Connection.getParent() *************************");
        return null;
    }


    @Override
    public void setParent(Object arg0) throws NoSupportException
    {
        System.out.println("sdb.Connection.setParent() *************************");
    }


    // com.sun.star.sdb.XCommandPreparation:
    @Override
    public XPreparedStatement prepareCommand(String command, int type)
    throws SQLException
    {
        System.out.println("sdb.Connection.prepareCommand() *************************");
        return null;
    }


    // import com.sun.star.lang.XMultiServiceFactory:
    @Override
    public Object createInstance(String service)
    throws Exception
    {
        System.out.println("sdb.Connection.createInstance() *************************");
        return null;
    }


    @Override
    public Object createInstanceWithArguments(String service, Object[] arguments) throws Exception {
        System.out.println("sdb.Connection.createInstanceWithArguments() *************************");
        return null;
    }


    @Override
    public String[] getAvailableServiceNames()
    {
        System.out.println("sdb.Connection.getAvailableServiceNames() *************************");
        String[] services = {"com.sun.star.sdb.SQLQueryComposer"};
        return services;
    }


    // com.sun.star.sdb.XQueriesSupplier:
    @Override
    public XNameAccess getQueries()
    {
        System.out.println("sdb.Connection.getQueries() *************************");
        return null;
    }


    // com.sun.star.sdb.XSQLQueryComposerFactory:
    @Override
    public XSQLQueryComposer createQueryComposer()
    {
        System.out.println("sdb.Connection.createQueryComposer() *************************");
        return null;
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

    // com.sun.star.sdbcx.XUsersSupplier:
    @Override
    public synchronized XNameAccess getUsers()
    {
        System.out.println("sdb.Connection.getUsers() 1");
        checkDisposed();
        if (m_Users == null) {
            _refreshUsers();
        }
        System.out.println("sdb.Connection.getUsers() 2");
        return m_Users;
    }

    // com.sun.star.sdbcx.XViewsSupplier:
    @Override
    public synchronized XNameAccess getViews()
    {
        System.out.println("sdb.Connection.getViews() 1");
        checkDisposed();
        if (m_Views == null) {
            _refreshViews();
        }
        System.out.println("sdb.Connection.getViews() 2");
        return m_Views;
    }

    // com.sun.star.sdbcx.XGroupsSupplier:
    @Override
    public XNameAccess getGroups()
    {
        System.out.println("sdb.Connection.getGroups() *********************************************");
        checkDisposed();
        if (m_Groups == null) {
            _refreshGroups();
        }
        System.out.println("sdb.Connection.getViews() 2");
        return m_Groups;
    }


    public synchronized TableContainer getTablesInternal()
    {
        return m_Tables;
    }

    public synchronized UserContainer getUsersInternal()
    {
        return m_Users;
    }


    protected XStatement _getStatement()
    {
        System.out.println("sdb.Connection._getStatement() *****************************");
        Statement statement = new Statement(this);
        //m_statements.put(statement, statement);
        return statement;
    }

    protected XPreparedStatement _getPreparedStatement(String sql)
    {
        System.out.println("sdb.Connection._getPreparedStatement() *****************************: '" + sql + "'");
        PreparedStatement statement = new PreparedStatement(this, sql);
        //m_statements.put(statement, statement);
        return statement;
    }

    protected XPreparedStatement _getCallableStatement(String sql)
    {
        System.out.println("sdb.Connection._getCallableStatement() *****************************: '" + sql + "'");
        CallableStatement statement = new CallableStatement(this, sql);
        //m_statements.put(statement, statement);
        return statement;
    }

    public synchronized void _refresh()
    {
        checkDisposed();
        _refreshTables();
        _refreshViews();
        _refreshUsers();
        _refreshGroups();
    }

    private void _refreshTables()
    {
        try {
            // FIXME: It is preferable to display all the entities of the underlying database.
            // FIXME: Filtering tables in Base or creating users with the appropriate rights seems more sensible.
            //String[] types = getProvider().getTableTypes();
            String[] types = null;
            java.sql.DatabaseMetaData metadata = getProvider().getConnection().getMetaData();
            java.sql.ResultSet result = metadata.getTables(null, null, "%", types);
            List<String> names = new ArrayList<>();
            while (result.next()) {
                String name = _buildName(result);
                names.add(name);
            }
            result.close();
            if (m_Tables == null) {
                m_Tables = new TableContainer(this, getProvider().isCaseSensitive(), names);
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
            System.out.println("sdb.Connection._refreshViews() 1");
            java.sql.DatabaseMetaData metadata = getProvider().getConnection().getMetaData();
            java.sql.ResultSet result = metadata.getTables(null, null, "%", new String[] { "VIEW" });
            List<String> names = new ArrayList<>();
            while (result.next()) {
                String name = _buildName(result);
                System.out.println("sdb.Connection._refreshViews() 2 View Name: " + name);
                names.add(name);
            }
            result.close();
            if (m_Views == null) {
                m_Views = new ViewContainer(this, getProvider().isCaseSensitive(), names);
            }
            else {
                m_Views.refill(names);
            }
        }
        catch (ElementExistException | SQLException | java.sql.SQLException e) {
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
        System.out.println("sdb.Connection._refreshViews() 3");
    }

    public void _refreshGroups()
    {
        System.out.println("sdb.Connection._refreshGroups() 1");
        try (java.sql.Statement statement = getProvider().getConnection().createStatement()) {
            java.sql.ResultSet result = statement.executeQuery(getProvider().getGroupQuery());
            List<String> names = new ArrayList<>();
            while (result.next()) {
                String name = result.getString(1);
                System.out.println("sdb.Connection._refreshGroups() 2 Group Name: " + name);
                names.add(name);
            }
            result.close();
            if (m_Groups == null) {
                m_Groups = new GroupContainer(this, getProvider().isCaseSensitive(), names);
            }
            else {
                m_Groups.refill(names);
            }
        }
        catch (ElementExistException | java.sql.SQLException e) {
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
        System.out.println("sdb.Connection._refreshGroups() 3");
    }

    public void _refreshUsers()
    {
        System.out.println("sdb.Connection._refreshUsers() 1");
        try (java.sql.Statement statement = getProvider().getConnection().createStatement()) {
            java.sql.ResultSet result = statement.executeQuery(getProvider().getUserQuery());
            List<String> names = new ArrayList<>();
            while (result.next()) {
                String name = result.getString(1);
                System.out.println("sdb.Connection._refreshUsers() 2 User Name: " + name);
                names.add(name);
            }
            result.close();
            if (m_Users == null) {
                m_Users = new UserContainer(this, getProvider().isCaseSensitive(), names);
            }
            else {
                m_Users.refill(names);
            }
        }
        catch (ElementExistException | java.sql.SQLException e) {
            throw new com.sun.star.uno.RuntimeException("Error", e);
        }
        System.out.println("sdb.Connection._refreshUsers() 3");
    }

    protected String _buildName(java.sql.ResultSet result)
        throws SQLException
    {
        return DataBaseTools.buildName(this, result, ComposeRule.InDataManipulation);
    }


    /*    public synchronized XNameAccess getTables1()
    {
        m_catalog = SchemaCrawler.getCatalog(provider.getConnection());
        for (final schemacrawler.schema.Table t : m_catalog.getTables()) {
            System.out.println("Connection.Connection() 2 Table Type: " + t.getTableType().getTableType());
        }
        System.out.println("sdb.Connection.getTables() 1");
        checkDisposed();
        if (m_Tables == null) {
            try {
                System.out.println("sdb.Connection.getTables() 2");
                m_Tables = m_crawler ? SchemaCrawler.getTables(m_provider, this) :
                                       new TableContainer(this);
                System.out.println("sdb.Connection.getTables() 3");
            }
            catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
        System.out.println("sdb.Connection.getTables() 4");
        return m_Tables;
    }*/


}