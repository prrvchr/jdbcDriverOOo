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
package io.github.prrvchr.uno.sdbcx;

import java.util.List;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.CheckOption;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.uno.helper.ComposeRule;
import io.github.prrvchr.uno.helper.DataBaseTools;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.helper.DataBaseTools.NameComponents;
import io.github.prrvchr.uno.sdbc.ConnectionSuper;
import io.github.prrvchr.uno.sdbc.StandardSQLState;


public class ViewContainer
    extends Container
{

    private ConnectionSuper m_Connection;

    // The constructor method:
    public ViewContainer(ConnectionSuper connection,
                         boolean sensitive,
                         List<String> names)
        throws ElementExistException
    {
        super(connection, sensitive, names);
        m_Connection = connection;
    }


    @Override
    protected XPropertySet _appendElement(XPropertySet descriptor,
                                          String name)
        throws SQLException
    {
        try {
            System.out.println("sdbcx.ViewContainer._appendElement() 1");
            String sql = DataBaseTools.getCreateViewQuery(m_Connection, descriptor);
            System.out.println("sdbcx.ViewContainer._appendElement() 2 SQL: '" + sql + "'");
            java.sql.Statement statement = m_Connection.getProvider().getConnection().createStatement();
            statement.execute(sql);
            statement.close();
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, this);
        }
        // Append it to the tables container too:
        m_Connection.getTablesInternal().insertElement(name, null);
        System.out.println("sdbcx.ViewContainer._appendElement() 3");
        return _createElement(name);
    }

    @Override
    protected XPropertySet _createElement(String name)
        throws SQLException
    {
        System.out.println("sdbcx.ViewContainer._createElement() 1 Name: " + name);
        View view = null;
        NameComponents component = DataBaseTools.qualifiedNameComponents(m_Connection, name, ComposeRule.InDataManipulation);
        final String sql = m_Connection.getProvider().getViewQuery(component);
        final String command;
        final String option;
        try (java.sql.PreparedStatement statement = m_Connection.getProvider().getConnection().prepareStatement(sql)){
            System.out.println("sdbcx.ViewContainer._createElement() 2 Name: " + name);
            int next = 1;
            if (!component.getCatalog().isEmpty()) {
                statement.setString(next++, component.getCatalog());
            }
            if (!component.getSchema().isEmpty()) {
                statement.setString(next++, component.getSchema());
            }
            statement.setString(next, component.getTable());
            java.sql.ResultSet result = statement.executeQuery();
            if (result.next()) {
                command = m_Connection.getProvider().getViewCommand(result.getString(1));
                option = result.getString(2);
            }
            else {
                throw new SQLException("View not found", this, StandardSQLState.SQL_TABLE_OR_VIEW_NOT_FOUND.text(), 0, null);
            }
            result.close();
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
            
            view = new View(m_Connection, isCaseSensitive(), component.getCatalog(),
                            component.getSchema(), component.getTable(), command, value);
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, this);
        }
        System.out.println("sdbcx.ViewContainer._createElement() 2");
        return view;
    }


    @Override
    protected void _removeElement(int index,
                                  String name)
        throws SQLException
    {
        try {
            Object object = _getElement(index);
            XPropertySet propertySet = UnoRuntime.queryInterface(XPropertySet.class, object);
            UnoHelper.ensure(propertySet != null, "Object returned from view collection isn't an XPropertySet");
            String sql = String.format("DROP VIEW %s", DataBaseTools.composeTableName(m_Connection, propertySet, ComposeRule.InTableDefinitions,
                    false, false, true));
            
            java.sql.Statement statement = m_Connection.getProvider().getConnection().createStatement();
            statement.execute(sql);
            statement.close();
        }
        catch (WrappedTargetException exception) {
            throw new SQLException("Error", this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, exception);
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    protected void _refresh() {
        m_Connection._refresh();
    }

    @Override
    protected XPropertySet _createDescriptor() {
        System.out.println("sdbcx.ViewContainer._createDescriptor()");
        return new ViewDescriptor(isCaseSensitive());
    }


}
