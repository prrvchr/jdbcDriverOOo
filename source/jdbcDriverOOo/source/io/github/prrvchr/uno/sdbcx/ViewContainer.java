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

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.CheckOption;
import com.sun.star.uno.Any;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.DBTools.NameComponents;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.uno.helper.UnoHelper;


public final class ViewContainer
    extends TableContainerMain<View>
{
    private static final String m_service = ViewContainer.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Views",
                                                "com.sun.star.sdbcx.Container"};

    @Override
    protected Connection getConnection() {
        return (Connection) m_Connection;
    }

    // The constructor method:
    public ViewContainer(ConnectionSuper connection,
                         boolean sensitive,
                         List<String> names)
        throws ElementExistException
    {
        super(m_service, m_services, connection, sensitive, names, LoggerObjectType.VIEWCONTAINER);
    }

    public void dispose()
    {
        m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_VIEWS_DISPOSING);
        super.dispose();
    }

    @Override
    protected boolean createDataBaseElement(XPropertySet descriptor, String name)
        throws SQLException
    {
        try {
            String query = DBTools.getCreateViewQuery(m_Connection.getProvider(), descriptor, isCaseSensitive());
            System.out.println("sdbcx.ViewContainer._createDataBaseElement() 2 SQL: '" + query + "'");
            if (DBTools.executeDDLQuery(m_Connection.getProvider(), query, m_logger, this.getClass().getName(),
                                        "_createView", Resources.STR_LOG_VIEWS_CREATE_VIEW_QUERY, name)) {
                m_Connection.getTablesInternal().insertElement(name, null);
                return true;
            }
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        return false;
    }

    @Override
    protected View createElement(String name)
        throws SQLException
    {
        String command = "";
        int option = CheckOption.NONE;
        ComposeRule rule = ComposeRule.InDataManipulation;
        try {
            NameComponents cpt = DBTools.qualifiedNameComponents(m_Connection.getProvider(), name, rule);
            if (m_Connection.getProvider().supportViewDefinition()) {
                List<Integer[]> positions = new ArrayList<Integer[]>();
                Object[] parameters = DBTools.getViewDefinitionArguments(m_Connection.getProvider(), cpt, name, rule, isCaseSensitive(), true);
                List<String> queries = m_Connection.getProvider().getViewDefinitionQuery(positions, parameters);
                if (!queries.isEmpty() && !positions.isEmpty()) {
                    parameters = DBTools.getViewDefinitionArguments(m_Connection.getProvider(), cpt, name, rule, isCaseSensitive(), false);
                    try (java.sql.PreparedStatement smt = m_Connection.getProvider().getConnection().prepareStatement(queries.get(0)))
                    {
                        int i = 1;
                        for (int position : positions.get(0)) {
                            smt.setString(i++, (String) parameters[position]);
                        }
                        String value = "NONE";
                        try (java.sql.ResultSet result = smt.executeQuery())
                        {
                            // FIXME: The query used comes from the Drivers.xcu file ViewDefinitionCommands property,
                            // FIXME: it must return at least one column for the view's SQL command.
                            // FIXME: If only one column is provided then the Check_Option value defaults to None.
                            if (result.next()) {
                                command = result.getString(1);
                                if (result.getMetaData().getColumnCount() > 1) {
                                    value = result.getString(2);
                                }
                            }
                        }
                        if ("NONE".startsWith(value)) {
                            option = CheckOption.NONE;
                        }
                        else if ("LOCAL".startsWith(value)) {
                            option = CheckOption.LOCAL;
                        }
                        else if ("CASCADED".startsWith(value)) {
                            option = CheckOption.CASCADE;
                        }
                    }
                }
            }
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_VIEW);
            View view = new View(m_Connection, isCaseSensitive(), cpt.getCatalog(),
                                 cpt.getSchema(), cpt.getTable(), command, option);
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_VIEW_ID, view.getLogger().getObjectId());
            return view;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    protected void removeElement(int index,
                                 String name)
        throws SQLException
    {
        View view = getElement(index);
        if (view == null) {
            throw new SQLException("Error", this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
        removeElement(view);
    }

    // Can be called from TableSuper.rename(String name)
    protected void removeElement(View view)
        throws SQLException
    {
        try {
            System.out.println("ViewContainer.removeElement() 1 Name: " + view.getName());
            String table = DBTools.buildName(m_Connection.getProvider(), view, ComposeRule.InTableDefinitions, isCaseSensitive());
            System.out.println("ViewContainer.removeElement() 2 Name: " + table);
            String query = DBTools.getDropViewQuery(table);
            DBTools.executeDDLQuery(m_Connection.getProvider(), query, m_logger, this.getClass().getName(),
                                    "removeElement", Resources.STR_LOG_VIEWS_REMOVE_VIEW_QUERY, view.getName());
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    protected XPropertySet createDescriptor() {
        return new ViewDescriptor(isCaseSensitive());
    }


}
