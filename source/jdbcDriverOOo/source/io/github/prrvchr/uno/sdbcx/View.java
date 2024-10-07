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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.container.ElementExistException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XAlterView;
import com.sun.star.uno.Type;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.helper.ParameterHelper;
import io.github.prrvchr.jdbcdriver.helper.DBTools;
import io.github.prrvchr.jdbcdriver.helper.DBTools.NamedComponents;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;


public final class View
    extends TableMain
    implements XAlterView
{
    private static final String m_service = View.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.View"};

    protected String m_Command = "";
    private int m_CheckOption;

    // The constructor method:
    public View(ConnectionSuper connection,
                boolean sensitive,
                String catalog,
                String schema,
                String name,
                String command,
                int option)
    {
        super(m_service, m_services, connection, catalog, schema, sensitive, name, LoggerObjectType.VIEW);
        m_Command = command;
        m_CheckOption = option;
        registerProperties();
    }

    private void registerProperties() {
        Map<String, PropertyWrapper> properties = new HashMap<String, PropertyWrapper>();
        short readonly = PropertyAttribute.READONLY;

        properties.put(PropertyIds.CHECKOPTION.getName(),
                       new PropertyWrapper(Type.LONG, readonly,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_CheckOption;
                                               }
                                           },
                                           null));

        properties.put(PropertyIds.COMMAND.getName(),
                       new PropertyWrapper(Type.STRING, readonly,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_Command;
                                               }
                                           },
                                           null));

        super.registerProperties(properties);
    }


    // com.sun.star.sdbcx.XAlterView
    @Override
    public void alterCommand(String command)
        throws SQLException
    {
        if (!m_Command.equals(command)) {
            String name = null;
            List<String> queries = new ArrayList<String>();
            try {
                NamedComponents component = getNamedComponents();
                ComposeRule rule = ComposeRule.InDataManipulation;
                DriverProvider provider = getConnection().getProvider();
                name = DBTools.buildName(provider, component, rule);
                Object[] arguments = ParameterHelper.getAlterViewArguments(provider, component, name, command, rule, isCaseSensitive());
                queries =  provider.getAlterViewQueries(arguments);
                if (!queries.isEmpty()) {
                    String query = String.join("> <", queries);
                    getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_VIEW_ALTER_QUERY, name, query);
                    DBTools.executeSQLQueries(provider, queries);
                }
            }
            catch (java.sql.SQLException e) {
                int resource = Resources.STR_LOG_VIEW_ALTER_QUERY_ERROR;
                String query = String.join("> <", queries);
                String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, name, query);
                throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
            }
            m_Command = command;
        }
    }


    // com.sun.star.sdbcx.XRename
    @Override
    public void rename(String newname)
        throws SQLException,
               ElementExistException
    {
        String oldname = null;
        try {
            ComposeRule rule = ComposeRule.InDataManipulation;
            DriverProvider provider = getConnection().getProvider();
            oldname = DBTools.buildName(provider, getNamedComponents(), rule);
            NamedComponents table = DBTools.qualifiedNameComponents(provider, newname, rule);
            if (rename(table, oldname, newname, true, rule)) {
                m_CatalogName = table.getCatalogName();
                m_SchemaName = table.getSchemaName();
                setName(table.getTableName());
                getConnection().getViewsInternal().rename(oldname, newname);
            }
        }
        catch (java.sql.SQLException e) {
            int resource = Resources.STR_LOG_VIEW_RENAME_UNSPECIFIED_ERROR;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, oldname);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

}
