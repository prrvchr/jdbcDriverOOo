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
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.Any;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.UnoHelper;


abstract class TableContainerMain<T extends TableMain>
    extends Container<T>
{
    protected final ConnectionSuper m_Connection;
    protected final ConnectionLog m_logger;

    // The constructor method:
    public TableContainerMain(String service,
                              String[] services,
                              ConnectionSuper connection,
                              boolean sensitive,
                              List<String> names,
                              LoggerObjectType logtype)
        throws ElementExistException
    {
        super(service, services, connection, sensitive, names);
        m_Connection = connection;
        m_logger = new ConnectionLog(connection.getProvider().getLogger(), logtype);
    }

    protected ConnectionLog getLogger()
    {
        return m_logger;
    }

    protected abstract ConnectionSuper getConnection();

    // FIXME: This is the Java implementation of com.sun.star.sdbcx.XContainer interface for the
    // FIXME: com.sun.star.sdbcx.XRename interface available for the com.sun.star.sdbcx.XTable and XView
    protected void rename(String oldname, String newname, int offset)
        throws SQLException
    {
        synchronized (m_Connection) {
            if (!m_Elements.containsKey(oldname) || !m_Names.contains(oldname)) {
                int resource = Resources.STR_LOG_TABLE_RENAME_TABLE_NOT_FOUND_ERROR + offset;
                String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, oldname);
                throw new SQLException(msg, this, StandardSQLState.SQL_TABLE_OR_VIEW_NOT_FOUND.text(), 0, Any.VOID);
            }
            replaceElement(oldname, newname);
        }
    }

    @Override
    public String getElementName(List<String> names, XPropertySet descriptor)
        throws SQLException, ElementExistException
    {
        String name;
        try {
            name = DBTools.composeTableName(m_Connection.getProvider(), descriptor, ComposeRule.InTableDefinitions, false);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        if (names.contains(name)) {
            throw new ElementExistException();
        }
        return name;
    }

    @Override
    protected T appendElement(XPropertySet descriptor,
                              String name)
        throws SQLException
    {
        T element = null;
        if (createDataBaseElement(descriptor, name)) {
           element = createElement(name);
        }
        return element;
    }

    @Override
    protected void _refresh() {
        m_Connection.refresh();
    }

    abstract boolean createDataBaseElement(XPropertySet descriptor, String name) throws SQLException;

}
