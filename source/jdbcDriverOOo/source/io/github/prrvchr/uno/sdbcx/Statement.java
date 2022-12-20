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

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertySetter;
import io.github.prrvchr.uno.helper.PropertyIds;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.StatementBase;


public final class Statement
    extends StatementBase
{
    
    private static final String m_service = Statement.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbc.Statement",
                                                "com.sun.star.sdbcx.Statement"};
    public boolean m_UseBookmarks = false;

    // The constructor method:
    public Statement(ConnectionBase connection)
    {
        super(m_service, m_services, connection);
        registerProperties();
        System.out.println("sdbcx.Statement() 1");
    }

    private void registerProperties() {
        registerProperty(PropertyIds.USEBOOKMARKS.name, PropertyIds.USEBOOKMARKS.id, Type.BOOLEAN,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    System.out.println("sdbcx.Statement._getUseBookmarks():" + m_UseBookmarks);
                    return m_UseBookmarks;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    System.out.println("sdbcx.Statement._setUseBookmarks():" + (boolean) value);
                    m_UseBookmarks = (boolean) value;
                }
            });
    }


    protected XResultSet _getResultSet(java.sql.ResultSet result)
    throws SQLException
    {
        XResultSet resultset = null;
        if (result != null) {
            resultset =  m_Connection.getProvider().getResultSet(m_Connection, result, this, m_UseBookmarks);
        }
        return resultset;
    }


}
