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

import java.util.List;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.container.ElementExistException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XAlterView;
import com.sun.star.uno.Any;
import com.sun.star.uno.Type;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.DBTools.NameComponents;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;


public final class View
    extends TableMain<ConnectionSuper>
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
        short readonly = PropertyAttribute.READONLY;
        registerProperty(PropertyIds.COMMAND.name, PropertyIds.COMMAND.id, Type.STRING, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Command;
                }
            }, null);
        registerProperty(PropertyIds.CHECKOPTION.name, PropertyIds.CHECKOPTION.id, Type.LONG, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_CheckOption;
                }
            }, null);
    }


    // com.sun.star.sdbcx.XAlterView
    @Override
    public void alterCommand(String command)
        throws SQLException
    {
        if (!m_Command.equals(command)) {
            try {
                ComposeRule rule = ComposeRule.InDataManipulation;
                System.out.println("sdbcx.View.alterCommand() 1 Command : " + command);
                String view = DBTools.buildName(getConnection().getProvider(), getCatalogName(), getSchemaName(), m_Name, rule, false);
                //String view = DBTools.composeTableName(getConnection(), this, rule, isCaseSensitive());
                NameComponents component = DBTools.qualifiedNameComponents(getConnection().getProvider(), view, rule);
                System.out.println("sdbcx.View.alterCommand() 2 View name: " + view);
                Object[] arguments = DBTools.getAlterViewArguments(getConnection().getProvider(), component, view, command, rule, isCaseSensitive());
                List<String> queries =  getConnection().getProvider().getAlterViewQueries(arguments);
                if (!queries.isEmpty()) {
                    String name = DBTools.composeTableName(getConnection().getProvider(), this, rule, false);
                DBTools.executeDDLQueries(getConnection().getProvider(), queries, getLogger(), this.getClass().getName(),
                                          "alterCommand", Resources.STR_LOG_VIEW_ALTER_QUERY, name);
                }
            }
            catch (java.sql.SQLException e) {
                throw UnoHelper.getSQLException(e, this);
            }
            m_Command = command;
        }
    }


    // com.sun.star.sdbcx.XRename
    @Override
    public void rename(String name)
        throws SQLException,
               ElementExistException
    {
        try {
            ComposeRule rule = ComposeRule.InDataManipulation;
            String oldname = DBTools.composeTableName(getConnection().getProvider(), this, rule, false);
            if (!getConnection().getProvider().supportRenamingTable()) {
                int resource = Resources.STR_LOG_VIEW_RENAME_UNSUPPORTED_FEATURE_ERROR;
                String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, oldname);
                throw new SQLException(msg, this, StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, Any.VOID);
            }
            int offset = Resources.STR_JDBC_LOG_MESSAGE_TABLE_VIEW_OFFSET;
            NameComponents component = DBTools.qualifiedNameComponents(getConnection().getProvider(), name, rule);
            rename(component, oldname, name, rule, offset);
            m_SchemaName = component.getSchema();
            m_Name = component.getTable();
            getConnection().getViewsInternal().rename(oldname, name, offset);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

}
