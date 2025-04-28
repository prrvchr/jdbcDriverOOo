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
import java.util.Map;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.CheckOption;
import com.sun.star.uno.Any;

import io.github.prrvchr.driver.helper.DBTools;
import io.github.prrvchr.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.driver.provider.ComposeRule;
import io.github.prrvchr.driver.provider.DriverProvider;
import io.github.prrvchr.driver.provider.LoggerObjectType;
import io.github.prrvchr.driver.provider.Resources;
import io.github.prrvchr.driver.provider.StandardSQLState;
import io.github.prrvchr.driver.query.DDLParameter;
import io.github.prrvchr.uno.helper.SharedResources;


public final class ViewContainer
    extends TableContainerMain<View> {
    private static final String SERVICE = ViewContainer.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.Views",
                                              "com.sun.star.sdbcx.Container"};

    // The constructor method:
    public ViewContainer(ConnectionSuper connection,
                         boolean sensitive,
                         List<String> names)
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
            DriverProvider provider = getConnection().getProvider();
            query = DBTools.getCreateViewCommand(provider, descriptor, isCaseSensitive());
            System.out.println("sdbcx.ViewContainer.createDataBaseElement() SQL: '" + query + "'");
            getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_VIEWS_CREATE_VIEW_QUERY, name, query);
            if (DBTools.executeSQLQuery(provider, query)) {
                TableContainerMain<?> tables = getConnection().getTablesInternal();
                tables.insertElement(name, null);
                created = true;
            }
        } catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_VIEWS_CREATE_VIEW_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
        return created;
    }

    @Override
    protected View createElement(String name)
        throws SQLException {
        try {
            return createView(name, ComposeRule.InDataManipulation);
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
    }

    private View createView(String name, ComposeRule rule) throws java.sql.SQLException {
        int option = CheckOption.NONE;
        String command = "";
        DriverProvider provider = getConnection().getProvider();
        NamedComponents component = DBTools.qualifiedNameComponents(provider, name, rule);
        if (provider.getDDLQuery().supportsViewDefinition()) {
            Map<String, Object> parameters = DDLParameter.getViewDefinition(provider, component,
                                                                                     name, rule, false);
            List<Object> values = new ArrayList<Object>();
            String query = provider.getDDLQuery().getViewDefinitionQuery(parameters, values);
            if (query != null && !query.isBlank() && !values.isEmpty()) {
                try (java.sql.PreparedStatement smt = provider.getConnection().prepareStatement(query)) {
                    for (int i = 0; i < values.size(); i++) {
                        smt.setObject(i + 1, values.get(i));
                    }
                    String value = "NONE";
                    try (java.sql.ResultSet result = smt.executeQuery()) {
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
        }
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_VIEW);
        View view = new View(getConnection(), isCaseSensitive(), component.getCatalogName(),
                             component.getSchemaName(), component.getTableName(), command, option);
        getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_VIEW_ID, view.getLogger().getObjectId());
        return view;
    }

    private String getViewCheckOption(java.sql.ResultSet result,
                                      String value) throws java.sql.SQLException {
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
        View view = (View) getElement(index);
        if (view == null) {
            throw new SQLException("Error", this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
        removeView(view);
    }

    // Can be called from TableSuper.rename(String name)
    protected void removeView(View view)
        throws SQLException {
        String query = null;
        try {
            ComposeRule rule = ComposeRule.InTableDefinitions;
            DriverProvider provider = getConnection().getProvider();
            String table = DBTools.buildName(provider, view.getNamedComponents(), rule, isCaseSensitive());
            query = DBTools.getDropViewCommand(provider, table);
            getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_VIEWS_REMOVE_VIEW_QUERY, view.getName(), query);
            DBTools.executeSQLQuery(provider, query);
        } catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_VIEWS_REMOVE_VIEW_QUERY_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, view.getName(), query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    @Override
    protected XPropertySet createDescriptor() {
        return new ViewDescriptor(isCaseSensitive());
    }


}
