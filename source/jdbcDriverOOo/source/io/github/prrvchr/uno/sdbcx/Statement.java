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

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.uno.Type;

import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertySetter;
import io.github.prrvchr.uno.sdbc.StatementBase;


public final class Statement
    extends StatementBase<ConnectionSuper>
{
    
    private static final String m_service = Statement.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbc.Statement",
                                                "com.sun.star.sdbcx.Statement"};
    protected boolean m_UseBookmarks = false;

    // The constructor method:
    public Statement(ConnectionSuper connection)
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

    protected ConnectionLog getLogger()
    {
        return super.getLogger();
    }

    @Override
    protected XResultSet getResultSet(java.sql.ResultSet result)
    throws SQLException
    {
        ResultSet resultset = null;
        m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_RESULTSET);
        if (result != null) {
            resultset =  new ResultSet(m_Connection, result, this, m_UseBookmarks);
            m_logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_RESULTSET_ID, resultset.getLogger().getObjectId());
        }
        return resultset;
    }

    @Override
    protected java.sql.ResultSet getGeneratedResult(String command)
        throws SQLException, java.sql.SQLException
    {
        // XXX: At this level of API (sdbcx or sdb) normally a ResultSet with all columns is already available... 
        return getStatement().getGeneratedKeys();
    }


}
