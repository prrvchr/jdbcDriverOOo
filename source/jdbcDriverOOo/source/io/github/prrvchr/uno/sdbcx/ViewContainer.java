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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbcx.CheckOption;

import io.github.prrvchr.uno.driver.config.ParameterDDL;
import io.github.prrvchr.uno.driver.helper.ComponentHelper;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedComponent;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedSupport;
import io.github.prrvchr.uno.driver.helper.DBTools;
import io.github.prrvchr.uno.driver.provider.ComposeRule;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.LoggerObjectType;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.helper.SharedResources;


public final class ViewContainer
    extends TableContainerMain<View> {
    private static final String SERVICE = ViewContainer.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.Views",
                                              "com.sun.star.sdbcx.Container"};

    // The constructor method:
    public ViewContainer(ConnectionSuper connection,
                         String[] names,
                         boolean sensitive)
        throws ElementExistException {
        super(SERVICE, SERVICES, connection, sensitive, names, LoggerObjectType.VIEWCONTAINER);
    }

    public void dispose() {
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_VIEWS_DISPOSING);
        super.dispose();
    }

    @Override
    protected boolean createDataBaseElement(XPropertySet descriptor, String name)
        throws SQLException {
        boolean created = false;
        String query = null;
        try {
            ComposeRule rule = ComposeRule.InViewDefinitions;
            Provider provider = getConnection().getProvider();
            query = DBTools.getCreateViewQuery(provider.getConfigDDL(),
                                               provider.getNamedSupport(rule),
                                               descriptor, isCaseSensitive());
            System.out.println("sdbcx.ViewContainer.createDataBaseElement() SQL: '" + query + "'");
            getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_VIEWS_CREATE_VIEW_QUERY, name, query);
            if (DBTools.executeSQLQuery(provider, query)) {
                TableContainerMain<?> tables = getConnection().getTablesInternal();
                tables.insertElement(name, null);
                created = true;
            }
        } catch (SQLException e) {
            int resource = Resources.STR_LOG_VIEWS_CREATE_VIEW_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, query);
            throw new SQLException(msg, e.getSQLState(), e.getErrorCode(), e);
        }
        return created;
    }

    @Override
    protected View createElement(String name)
        throws SQLException {
        try {
            System.out.println("ViewContainer.createElement() 1 View name: " + name);
            ComposeRule rule = ComposeRule.InTableDefinitions;
            int option = CheckOption.NONE;
            String command = "";
            Provider provider = getConnection().getProvider();
            NamedSupport support = provider.getNamedSupport(rule);
            NamedComponent component = ComponentHelper.qualifiedNameComponents(support, name);
            if (provider.getConfigDDL().supportsViewDefinition()) {
                Map<String, Object> parameters = ParameterDDL.getViewDefinition(support, component, isCaseSensitive());
                List<Object> values = new ArrayList<Object>();
                String query = provider.getConfigDDL().getViewDefinitionQuery(parameters, values);
                String[] options = new String[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    options[i] = values.get(i).toString();
                }
                System.out.println("ViewContainer.createElement() 2 Query: " + query);
                System.out.println("ViewContainer.createElement() 3 Options: " + String.join(", ", options));
                if (query != null && !query.isBlank() && !values.isEmpty()) {
                    String value = "NONE";
                    try (PreparedStatement smt = getCreateViewStatement(provider.getConnection(), values, query);
                         java.sql.ResultSet result = smt.executeQuery()) {
                        // FIXME: The query used comes from the Drivers.xcu file ViewDefinitionQuery property,
                        // FIXME: it must return at least one column for the view's SQL command.
                        // FIXME: If only one column is provided then the Check_Option value defaults to None.
                        if (result.next()) {
                            command = result.getString(1);
                            value = getViewCheckOption(result, value);
                        }
                    }
                    option = getViewCheckOption(value);
                }
            }
            System.out.println("ViewContainer.createElement() 4 Command: " + command);
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_VIEW);
            View view = new View(getConnection(), isCaseSensitive(), component.getCatalogName(),
                                 component.getSchemaName(), component.getTableName(), command, option);
            getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_VIEW_ID, view.getLogger().getObjectId());
            return view;
        } catch (SQLException e) {
            //int resource = Resources.STR_LOG_CREATED_VIEW_ERROR;
            //String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, query);
            throw new SQLException(e.getMessage(), StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    private PreparedStatement getCreateViewStatement(java.sql.Connection connection, List<Object> values, String query)
        throws SQLException {
        PreparedStatement smt = connection.prepareStatement(query);
        for (int i = 0; i < values.size(); i++) {
            smt.setObject(i + 1, values.get(i));
        }
        return smt;
    }

    private String getViewCheckOption(java.sql.ResultSet result,
                                      String value) throws SQLException {
        if (result.getMetaData().getColumnCount() > 1) {
            value = result.getString(2);
        }
        return value;
    }

    private int getViewCheckOption(String value) {
        // FIXME: Some driver can return N for None, L for LOCAL or C for CASCADED...
        int option = CheckOption.NONE;
        if ("LOCAL".startsWith(value)) {
            option = CheckOption.LOCAL;
        } else if ("CASCADED".startsWith(value)) {
            option = CheckOption.CASCADE;
        }
        return option;
    }

    @Override
    protected void removeDataBaseElement(int index,
                                         String name)
        throws SQLException {
        View view = (View) getElementByIndex(index);
        if (view == null) {
            throw new SQLException("Error", StandardSQLState.SQL_GENERAL_ERROR.text());
        }
        removeView(view);
    }

    // Can be called from TableSuper.rename(String name)
    protected void removeView(View view)
        throws SQLException {
        String query = null;
        try {
            ComposeRule rule = ComposeRule.InViewDefinitions;
            Provider provider = getConnection().getProvider();
            NamedSupport support = provider.getNamedSupport(rule);
            String table = ComponentHelper.buildName(support, view.getNamedComponents(), isCaseSensitive());
            query = provider.getConfigDDL().getDropViewCommand(ParameterDDL.getDropView(table));
            System.out.println("ViewContainer.removeView() Query: " + query);
            getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_VIEWS_REMOVE_VIEW_QUERY, view.getName(), query);
            DBTools.executeSQLQuery(provider, query);
        } catch (SQLException e) {
            int resource = Resources.STR_LOG_VIEWS_REMOVE_VIEW_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, view.getName(), query);
            throw new SQLException(msg, e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    protected XPropertySet createDescriptor() {
        return new ViewDescriptor(isCaseSensitive());
    }

}
