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

import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.uno.helper.SharedResources;


public abstract class DescriptorContainer<T>
    extends Container<T>
{
    private static final String m_service = DescriptorContainer.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Descriptors",
                                                "com.sun.star.sdbcx.Container"};

    // The constructor method:
    public DescriptorContainer(Object lock,
                               boolean sensitive)
    {
        super(m_service, m_services, lock, sensitive);
    }

    @Override
    protected T _createElement(String name)
        throws SQLException
    {
        // This should never be called. DescriptorContainer always starts off empty,
        // and only grows as a result of appending.
        String error = SharedResources.getInstance().getResource(Resources.STR_ERRORMSG_SEQUENCE);
        throw new SQLException(error, this, StandardSQLState.SQL_FUNCTION_SEQUENCE_ERROR.text(), 0, null);
    }
    
    @Override
    protected void _removeElement(int index,
                                  String name)
        throws SQLException
    {
    }
    
    @Override
    protected void _refresh()
    {
    }

    @Override
    public String _getElementName(List<String> names,
                                  XPropertySet descriptor)
        throws SQLException, ElementExistException
    {
        String name = DBTools.getDescriptorStringValue(descriptor, PropertyIds.NAME);
        if (names.contains(name)) {
            throw new ElementExistException();
        }
        return name;
    }
    

}
