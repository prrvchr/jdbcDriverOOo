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
package io.github.prrvchr.rowset;

import java.sql.SQLException;

public class CachedResultSet
    extends CachedRowSetImpl
{

    private static final long serialVersionUID = 1884577171200622430L;

    /**
     * Constructs a new default <code>CachedResultSetImpl</code> object with
     * the capacity to hold 100 rows. This new object has no metadata
     * and has the following default values:
     * <pre>
     *     onInsertRow = false
     *     insertRow = null
     *     cursorPos = 0
     *     numRows = 0
     *     showDeleted = false
     *     queryTimeout = 0
     *     maxRows = 0
     *     maxFieldSize = 0
     *     rowSetType = ResultSet.TYPE_SCROLL_INSENSITIVE
     *     concurrency = ResultSet.CONCUR_UPDATABLE
     *     readOnly = false
     *     isolation = Connection.TRANSACTION_READ_COMMITTED
     *     escapeProcessing = true
     *     onInsertRow = false
     *     insertRow = null
     *     cursorPos = 0
     *     absolutePos = 0
     *     numRows = 0
     * </pre>
     * A <code>CachedResultSetImpl</code> object is configured to use the default
     * <code>RIOptimisticProvider</code> implementation to provide connectivity
     * and synchronization capabilities to the set data source.
     * <P>
     * @throws SQLException if an error occurs
     */
    public CachedResultSet()
        throws SQLException
    {
        super();
        System.out.println("CachedResultSetImpl() 1");
    }

}
