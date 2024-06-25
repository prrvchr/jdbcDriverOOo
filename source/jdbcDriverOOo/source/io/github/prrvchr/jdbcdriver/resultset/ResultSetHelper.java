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
package io.github.prrvchr.jdbcdriver.resultset;

import java.sql.ResultSet;

import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.rowset.RowCatalog;


public  class ResultSetHelper
{

    public static boolean isResultSetUpdatable(DriverProvider provider,
                                               java.sql.ResultSet result,
                                               RowCatalog catalog,
                                               String query)
        throws SQLException
    {
        try {
            boolean updatable = provider.isResultSetUpdatable(result);
            if (!updatable) {
                catalog = new RowCatalog(provider, result, query);
                updatable = catalog.hasRowIdentifier();
            }
            return updatable;
        }
        catch (java.sql.SQLException e) {
            throw new SQLException();
        }
    }

    public static CachedResultSet getCachedResultSet(DriverProvider provider,
                                                     java.sql.ResultSet result,
                                                     RowCatalog catalog,
                                                     String query)
        throws SQLException
    {
        try {
            CachedResultSet resultset;
            int rstype = result.getType();
            boolean updatable = provider.isResultSetUpdatable(result);
            boolean forwardonly = rstype == ResultSet.TYPE_FORWARD_ONLY;
            boolean sensitive = rstype == ResultSet.TYPE_SCROLL_SENSITIVE;
            int fetchsize = result.getFetchSize();
            System.out.println("ResultSetHelper.getCachedResultSet() Updatable: " + updatable + " - IsForwardOnly: " + forwardonly + " - IsSensitive: " + sensitive + " - FetchSize: " + fetchsize);
            if (rstype == ResultSet.TYPE_FORWARD_ONLY) {
                resultset = new ScrollableResultSet(provider, result, catalog, query);
                System.out.println("ResultSetHelper.getCachedResultSet() ResultSet: ScrollableResultSet");
            }
            else {
                resultset = new SensitiveResultSet(provider, result, catalog, query);
                System.out.println("ResultSetHelper.getCachedResultSet() ResultSet: SensitiveResultSet");
            }
            return resultset;
        }
        catch (java.sql.SQLException e) {
            throw new SQLException();
        }
    }

}
