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

import java.util.Map;

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.uno.Type;

import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertySetter;
import io.github.prrvchr.uno.sdbc.PreparedStatementBase;


public abstract class PreparedStatementSuper
    extends PreparedStatementBase
{

    protected boolean m_UseBookmarks = false;


    // The constructor method:
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdb.PreparedStatement()
    // XXX: - io.github.prrvchr.uno.sdbcx.PreparedStatement()
    public PreparedStatementSuper(String service,
                                  String[] services,
                                  ConnectionSuper connection,
                                  String sql)
    {
        super(service, services, connection, sql);
        System.out.println("sdbc.PreparedStatementSuper() 1: '" + sql + "'");
    }

    @Override
    protected void registerProperties(Map<String, PropertyWrapper> properties) {

        properties.put(PropertyIds.USEBOOKMARKS.getName(),
                       new PropertyWrapper(Type.BOOLEAN,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_USEBOOKMARKS, Boolean.toString(m_UseBookmarks));
                                                   System.out.println("sdbc.PreparedStatementSuper.getUseBookmark(): " + m_UseBookmarks);
                                                   return m_UseBookmarks;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_SET_USEBOOKMARKS, value.toString());
                                                   boolean usebookmark = getConnectionInternal().getProvider().useBookmarks((boolean) value);
                                                   System.out.println("sdbc.PreparedStatementSuper.setUseBookmark(): " + usebookmark);
                                                   m_UseBookmarks = usebookmark;
                                               }
                                           }));

        super.registerProperties(properties);
    }

    @Override
    protected java.sql.ResultSet getJdbcResultSet()
        throws java.sql.SQLException
    {
        return super.getJdbcResultSet();
    }

    @Override
    protected ConnectionLog getLogger()
    {
        return super.getLogger();
    }

    @Override
    protected ConnectionSuper getConnectionInternal()
    {
        return (ConnectionSuper) m_Connection;
    }

}
