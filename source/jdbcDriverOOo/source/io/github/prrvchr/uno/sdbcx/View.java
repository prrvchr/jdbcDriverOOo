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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.container.ElementExistException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XAlterView;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.driver.config.ConfigDDL;
import io.github.prrvchr.uno.driver.config.ParameterDDL;
import io.github.prrvchr.uno.driver.helper.ComponentHelper;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedComponent;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedSupport;
import io.github.prrvchr.uno.driver.helper.DBTools;
import io.github.prrvchr.uno.driver.provider.ComposeRule;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.LoggerObjectType;
import io.github.prrvchr.uno.driver.provider.PropertyIds;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.SharedResources;


public final class View
    extends TableBase
    implements XAlterView {
    private static final String SERVICE = View.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.View"};

    protected String mCommand = "";
    private int mCheckOption;

    // The constructor method:
    public View(ConnectionSuper connection,
                boolean sensitive,
                String catalog,
                String schema,
                String name,
                String command,
                int option) {
        super(SERVICE, SERVICES, connection, catalog, schema, sensitive, name, LoggerObjectType.VIEW);
        mCommand = command;
        mCheckOption = option;
        registerProperties();
    }

    private void registerProperties() {
        Map<String, PropertyWrapper> properties = new HashMap<String, PropertyWrapper>();
        short readonly = PropertyAttribute.READONLY;

        properties.put(PropertyIds.CHECKOPTION.getName(),
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return mCheckOption;
                },
                null));

        properties.put(PropertyIds.COMMAND.getName(),
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return mCommand;
                },
                null));

        super.registerProperties(properties);
    }


    // com.sun.star.sdbcx.XAlterView
    @Override
    public void alterCommand(String command)
        throws SQLException {
        if (!mCommand.equals(command)) {
            String name = null;
            List<String> queries = new ArrayList<>();
            try {
                NamedComponent component = getNamedComponents();
                ComposeRule rule = ComposeRule.InViewDefinitions;
                NamedSupport support = mConnection.getProvider().getNamedSupport(rule);
                Provider provider = getConnection().getProvider();
                name = ComponentHelper.buildName(support, component);
                Map<String, Object> arguments = ParameterDDL.getAlterView(support, component,
                                                                          command, isCaseSensitive());
                ConfigDDL config = mConnection.getProvider().getConfigDDL();
                queries =  config.getAlterViewCommands(arguments);
                if (!queries.isEmpty()) {
                    String query = String.join("> <", queries);
                    name = ComponentHelper.buildName(support, component, false);
                    getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_VIEW_ALTER_QUERY, name, query);
                    DBTools.executeSQLQueries(provider, queries);
                }
            } catch (java.sql.SQLException e) {
                int resource = Resources.STR_LOG_VIEW_ALTER_QUERY_ERROR;
                String query = String.join("> <", queries);
                String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, query);
                throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
            }
            mCommand = command;
        }
    }


    // com.sun.star.sdbcx.XRename
    @Override
    public void rename(String newname)
        throws SQLException,
               ElementExistException {
        String oldname = null;
        try {
            ComposeRule rule = ComposeRule.InDataManipulation;
            NamedSupport support = mConnection.getProvider().getNamedSupport(rule);
            oldname = ComponentHelper.buildName(support, getNamedComponents());
            NamedComponent table = ComponentHelper.qualifiedNameComponents(support, newname);
            if (rename(table, oldname, newname, true, rule)) {
                mCatalogName = table.getCatalogName();
                mSchemaName = table.getSchemaName();
                setName(table.getTableName());
                getConnection().getViewsInternal().rename(oldname, newname);
            }
        } catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_VIEW_RENAME_UNSPECIFIED_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, oldname);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

}
