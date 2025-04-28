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
package io.github.prrvchr.driver.resultset;

import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.driver.helper.SqlCommand;
import io.github.prrvchr.driver.provider.ConnectionLog;
import io.github.prrvchr.driver.provider.DriverProvider;
import io.github.prrvchr.driver.rowset.RowCatalog;


public class ResultSetHelper {

    public static boolean isUpdatable(DriverProvider provider,
                                      java.sql.ResultSet result,
                                      RowCatalog catalog,
                                      SqlCommand sql)
        throws SQLException {
        try {
            boolean updatable = provider.isResultSetUpdatable(result);
            if (!updatable && sql.hasTable() && sql.isSelectCommand()) {
                catalog = new RowCatalog(provider, result, sql.getTable());
                updatable = catalog.hasRowIdentifier();
            }
            return updatable;
        } catch (java.sql.SQLException e) {
            throw new SQLException();
        }
    }

    // XXX: getResultSet() will be called only if isUpdatable() return true.
    public static CachedResultSet getResultSet(DriverProvider provider,
                                               java.sql.ResultSet result,
                                               RowCatalog catalog,
                                               String table,
                                               ConnectionLog logger)
        throws SQLException {
        try {
            CachedResultSet resultset;
            int rstype = result.getType();
            boolean updatable = provider.isResultSetUpdatable(result);
            boolean forwardonly = rstype == java.sql.ResultSet.TYPE_FORWARD_ONLY;
            boolean sensitive = rstype == java.sql.ResultSet.TYPE_SCROLL_SENSITIVE;
            int fetchsize = result.getFetchSize();
            System.out.println("ResultSetHelper.getCachedResultSet() Updatable: " + updatable + " - IsForwardOnly: " +
                               forwardonly + " - IsSensitive: " + sensitive + " - FetchSize: " + fetchsize);
            if (rstype == java.sql.ResultSet.TYPE_FORWARD_ONLY) {
                resultset = new ScrollableResultSet(provider, result, catalog, table, logger);
                System.out.println("ResultSetHelper.getCachedResultSet() ResultSet: ScrollableResultSet");
            } else {
                resultset = new SensitiveResultSet(provider, result, catalog, table, logger);
                System.out.println("ResultSetHelper.getCachedResultSet() ResultSet: SensitiveResultSet");
            }
            return resultset;
        } catch (java.sql.SQLException e) {
            throw new SQLException();
        }
    }

}
