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

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.CheckOption;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.DBTools.NameComponents;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionSuper;


public class ViewContainer
    extends Container
{

    private final ConnectionSuper m_Connection;
    private final ConnectionLog m_logger; 

    // The constructor method:
    public ViewContainer(ConnectionSuper connection,
                         boolean sensitive,
                         List<String> names)
        throws ElementExistException
    {
        super(connection, sensitive, names);
        m_Connection = connection;
        m_logger = new ConnectionLog(connection.getLogger(), LoggerObjectType.VIEWCONTAINER);

    }

    public ConnectionLog getLogger()
    {
        return m_logger;
    }

    @Override
    public String _getElementName(List<String> names,
                                  XPropertySet descriptor)
        throws SQLException, ElementExistException
    {
        String name = DBTools.composeTableName(m_Connection, descriptor, ComposeRule.InTableDefinitions, false);
        if (names.contains(name)) {
            throw new ElementExistException();
        }
        return name;
    }

    @Override
    protected XPropertySet _appendElement(XPropertySet descriptor,
                                          String name)
        throws SQLException
    {
        XPropertySet view = null;
        System.out.println("sdbcx.ViewContainer._appendElement() 1");
        if (_createView(descriptor, name)) {
            // Append it to the tables container too:
            m_Connection.getTablesInternal().insertElement(name, null);
           System.out.println("sdbcx.ViewContainer._appendElement() 3");
           view = _createElement(name);
        }
        return view;
   }

    private boolean _createView(XPropertySet descriptor,
                                String name)
        throws SQLException
    {
        String query = DBTools.getCreateViewQuery(m_Connection, descriptor, isCaseSensitive());
        System.out.println("sdbcx.ViewContainer._appendElement() 2 SQL: '" + query + "'");
        return DBTools.executeDDLQuery(m_Connection, query, m_logger, this.getClass().getName(),
                                       "_createView", Resources.STR_LOG_VIEWS_CREATE_VIEW_QUERY, name);
    }

    @Override
    protected XPropertySet _createElement(String name)
        throws SQLException
    {
        View view = null;
        NameComponents component = DBTools.qualifiedNameComponents(m_Connection, name, ComposeRule.InDataManipulation);
        final String sql = m_Connection.getProvider().getViewQuery(component);
        final String command;
        final String option;
        try (java.sql.PreparedStatement statement = m_Connection.getProvider().getConnection().prepareStatement(sql);
             java.sql.ResultSet result = _getCreateElementResultSet(component, statement))
        {
            if (result.next()) {
                String cmd = result.getString(1);
                command = m_Connection.getProvider().getViewCommand(cmd);
                option = result.getString(2);
            }
            else {
                throw new SQLException("View not found", this, StandardSQLState.SQL_TABLE_OR_VIEW_NOT_FOUND.text(), 0, null);
            }
            final int value;
            if (option.equals("NONE")) {
                value = CheckOption.NONE;
            }
            else if (option.equals("LOCAL")) {
                value = CheckOption.LOCAL;
            }
            else if (option.equals("CASCADED")) {
                value = CheckOption.CASCADE;
            }
            else {
                throw new SQLException("Unsupported check option '" + option + "'", this,
                        StandardSQLState.SQL_FEATURE_NOT_IMPLEMENTED.text(), 0, null);
            }
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_VIEW);
            view = new View(m_Connection, isCaseSensitive(), component.getCatalog(),
                            component.getSchema(), component.getTable(), command, value);
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_VIEW_ID, view.getLogger().getObjectId());
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        return view;
    }

    private java.sql.ResultSet _getCreateElementResultSet(NameComponents component,
                                                          java.sql.PreparedStatement statement)
        throws java.sql.SQLException
    {
        int index = 1;
        if (!component.getCatalog().isEmpty()) {
            statement.setString(index++, component.getCatalog());
        }
        if (!component.getSchema().isEmpty()) {
            statement.setString(index++, component.getSchema());
        }
        statement.setString(index, component.getTable());
        return statement.executeQuery();
    }

    @Override
    protected void _removeElement(int index,
                                  String name)
        throws SQLException
    {
        try {
            XPropertySet descriptor = UnoRuntime.queryInterface(XPropertySet.class, _getElement(index));
            UnoHelper.ensure(descriptor != null, "Object returned from view collection isn't an XPropertySet", m_logger);
            String view = DBTools.composeTableName(m_Connection, descriptor, ComposeRule.InTableDefinitions, isCaseSensitive());
            String query = m_Connection.getProvider().getDropViewQuery(view);
            DBTools.executeDDLQuery(m_Connection, query, m_logger, this.getClass().getName(),
                                     "_createView", Resources.STR_LOG_VIEWS_REMOVE_VIEW_QUERY, name);
        }
        catch (WrappedTargetException exception) {
            throw new SQLException("Error", this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, exception);
        }
    }

    @Override
    protected void _refresh() {
        m_Connection._refresh();
    }

    @Override
    protected XPropertySet _createDescriptor() {
        return new ViewDescriptor(isCaseSensitive());
    }


}
