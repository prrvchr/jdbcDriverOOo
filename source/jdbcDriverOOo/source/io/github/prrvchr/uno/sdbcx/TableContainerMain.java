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

import java.util.Iterator;
import java.util.List;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ContainerEvent;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.XContainerListener;
import com.sun.star.lang.EventObject;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.Any;
import com.sun.star.util.XRefreshListener;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.uno.helper.SharedResources;


abstract class TableContainerMain<T>
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
        m_logger = new ConnectionLog(connection.getLogger(), logtype);
    }

    protected ConnectionLog getLogger()
    {
        return m_logger;
    }

    // FIXME: This is the Java implementation of com.sun.star.sdbcx.XContainer interface for the
    // FIXME: com.sun.star.sdbcx.XRename interface available for the com.sun.star.sdbcx.XTable and XView
    protected void rename(String oldname, String newname, int resource)
        throws SQLException
    {
        synchronized (m_Connection) {
            if (!m_Elements.containsKey(oldname) || !m_Names.contains(oldname)) {
                String msg = SharedResources.getInstance().getResourceWithSubstitution(resource + 3, oldname);
                throw new SQLException(msg, this, StandardSQLState.SQL_TABLE_OR_VIEW_NOT_FOUND.text(), 0, Any.VOID);
            }
            T element = m_Elements.remove(oldname);
            m_Elements.put(newname, element);
            m_Names.set(m_Names.indexOf(oldname), newname);
            ContainerEvent event = new ContainerEvent(this, newname, element, oldname);
            for (Iterator<?> iterator = m_container.iterator(); iterator.hasNext();) {
                XContainerListener listener = (XContainerListener) iterator.next();
                listener.elementReplaced(event);
            }
            EventObject event2 = new EventObject(this);
            for (Iterator<?> iterator2 = m_refresh.iterator(); iterator2.hasNext();) {
                XRefreshListener listener = (XRefreshListener) iterator2.next();
                listener.refreshed(event2);
            }
        }
    }

    @Override
    public String _getElementName(List<String> names, XPropertySet descriptor)
        throws SQLException, ElementExistException
    {
        String name = DBTools.composeTableName(m_Connection, descriptor, ComposeRule.InTableDefinitions, false);
        if (names.contains(name)) {
            throw new ElementExistException();
        }
        return name;
    }

    @Override
    protected T _appendElement(XPropertySet descriptor,
                               String name)
        throws SQLException
    {
        T element = null;
        if (_createDataBaseElement(descriptor, name)) {
           element = _createElement(name);
        }
        return element;
    }

    @Override
    protected void _refresh() {
        m_Connection._refresh();
    }

    abstract boolean _createDataBaseElement(XPropertySet descriptor, String name) throws SQLException;

}
