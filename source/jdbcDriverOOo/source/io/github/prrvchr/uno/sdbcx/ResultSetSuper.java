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


import com.sun.star.beans.PropertyAttribute;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.Type;

import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.sdbc.ResultSetBase;
import io.github.prrvchr.uno.sdbc.StatementMain;


public abstract class ResultSetSuper<C extends ConnectionSuper, S extends StatementMain<?, ?>>
    extends ResultSetBase<C, S>
{
    private boolean m_IsBookmarkable = false;
    private boolean m_CanUpdateInsertedRows = false;
    
    // The constructor method:
    public ResultSetSuper(String service,
                          String[] services,
                          C connection,
                          java.sql.ResultSet resultset,
                          S statement,
                          boolean bookmark,
                          boolean updatable)
    throws SQLException
    {
        super(service, services, connection, resultset, statement);
        m_IsBookmarkable = bookmark;
        m_CanUpdateInsertedRows = updatable;
        registerProperties();
    }

    private void registerProperties() {
        short readonly = PropertyAttribute.READONLY;
        registerProperty(PropertyIds.ISBOOKMARKABLE.name, PropertyIds.ISBOOKMARKABLE.id, Type.BOOLEAN, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    System.out.println("sdbcx.ResultSetSuper.getIsBookmarkable() 1 IsBookmarkable: " + m_IsBookmarkable);
                    getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_ISBOOKMARKABLE, Boolean.toString(m_IsBookmarkable));
                    return m_IsBookmarkable;
                }
            }, null);
        registerProperty(PropertyIds.CANUPDATEINSERTEDROWS.name, PropertyIds.CANUPDATEINSERTEDROWS.id, Type.BOOLEAN, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_RESULTSET_CANUPDATEINSERTEDROWS, Boolean.toString(m_CanUpdateInsertedRows));
                    return m_CanUpdateInsertedRows;
                }
            }, null);
    }

    @Override
    protected java.sql.ResultSet getJdbcResultSet()
        throws SQLException
    {
        return super.getJdbcResultSet();
    }

    @Override
    protected ConnectionLog getLogger()
    {
        return super.getLogger();
    }
}
