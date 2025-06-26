/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-25 https://prrvchr.github.io                                  ║
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
package io.github.prrvchr.uno.sdbc;

import java.sql.PreparedStatement;
import java.util.Map;

import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.driver.helper.QueryHelper;
import io.github.prrvchr.uno.driver.provider.PropertyIds;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.helper.PropertyWrapper;

public abstract class PreparedStatementBase
    extends PreparedStatementMain {

    protected boolean mUseBookmarks = false;

    // The constructor method:
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbc.PreparedStatement()
    // XXX: - io.github.prrvchr.uno.sdbc.PreparedStatementSuper()
    public PreparedStatementBase(String service,
                                 String[] services,
                                 ConnectionBase connection,
                                 String sql)
        throws SQLException {
        super(service, services, connection);
        mQuery = new QueryHelper(connection.getProvider(), sql);
    }

    @Override
    protected void registerProperties(Map<String, PropertyWrapper> properties) {

        properties.put(PropertyIds.USEBOOKMARKS.getName(),
            new PropertyWrapper(Type.BOOLEAN,
                () -> {
                    getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_USEBOOKMARKS,
                                       Boolean.toString(mUseBookmarks));
                    System.out.println("PreparedStatementBase.getUseBookmark(): " + mUseBookmarks);
                    return mUseBookmarks;
                },
                value -> {
                    getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_SET_USEBOOKMARKS,
                                       value.toString());
                    System.out.println("PreparedStatementBase.setUseBookmark(): " + value);
                    mUseBookmarks = (boolean) value;
                }));

        super.registerProperties(properties);
    }

    @Override
    protected PreparedStatement getJdbcStatement()
        throws java.sql.SQLException {
        checkDisposed();
        if (mStatement == null) {
            PreparedStatement statement;
            String sql = mQuery.getQuery();
            if (mQuery.isInsertCommand()) {
                int option = mConnection.getProvider().getGeneratedKeysOption();
                statement = mConnection.getProvider().getConnection().prepareStatement(sql, option);
            } else if (mResultSetType != java.sql.ResultSet.TYPE_FORWARD_ONLY ||
                       mResultSetConcurrency != java.sql.ResultSet.CONCUR_READ_ONLY) {
                int holdability = java.sql.ResultSet.HOLD_CURSORS_OVER_COMMIT;
                //int holdability = java.sql.ResultSet.CLOSE_CURSORS_AT_COMMIT;
                statement = mConnection.getProvider().getConnection().prepareStatement(sql,
                                                                                        mResultSetType,
                                                                                        mResultSetConcurrency,
                                                                                        holdability);
            } else {
                statement = mConnection.getProvider().getConnection().prepareStatement(sql);
            }
            mStatement = setStatement(statement);
        }
        return (PreparedStatement) mStatement;
    }

}
