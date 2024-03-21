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


import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.uno.Any;

import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.DBQueryParser;
import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.DriverProvider;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;


public final class GeneratedResultSet
{
    public final static XResultSet getGeneratedValues(ConnectionSuper connection,
                                                      java.sql.Statement statement,
                                                      ConnectionLog logger)
        throws SQLException
    {
        //statement.checkDisposed();
        ResultSetSuper<?> resultset = null;
        java.sql.ResultSet result;
        try {
            if (connection.supportsService("com.sun.star.sdbcx.Connection")) {
                System.out.println("StatementMain.getGeneratedKeys() 1");
                result = statement.getGeneratedKeys();
            }
            else {
                String query = "SELECT 1 WHERE 0 = 1";
                result = statement.executeQuery(query);
            }
            logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_RESULTSET);
            resultset = new ResultSet(connection, result);
            logger.logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_RESULTSET_ID, resultset.getLogger().getObjectId());
            int count = result.getMetaData().getColumnCount();
            logger.logprb(LogLevel.FINE, Resources.STR_LOG_STATEMENT_GENERATED_VALUES_RESULT, count, DBTools.getGeneratedColumnNames(result, count));
            System.out.println("StatementMain.getGeneratedKeys() 2");
        }
        catch (java.sql.SQLException e) {
            logger.logprb(LogLevel.SEVERE, Resources.STR_LOG_STATEMENT_GENERATED_VALUES_ERROR, e.getMessage());
            throw new SQLException(e.getMessage(), connection, StandardSQLState.SQL_OPERATION_CANCELED.text(), 0, Any.VOID);
        }
        return resultset;
    }

    public static String[] getGeneratedColumns(ConnectionSuper connection,
                                               DriverProvider provider,
                                               String sql)
        throws SQLException
    {
        String[] columns = null;
        if (connection.supportsService("com.sun.star.sdbcx.Connection") &&
            connection.getProvider().isAutoRetrievingEnabled()) {
            DBQueryParser parser = new DBQueryParser(sql);
            if (parser.isExecuteUpdateStatement() && parser.hasTable()) {
                String name = DBTools.unQuoteTableName(connection.getProvider(), parser.getTable());
                TableSuper<?> table = connection.getTablesInternal().getElement(name);
                if (table != null) {
                    columns = table.getColumnsInternal().getElementNames();
                }
            }
        }
        return columns;
    }

}
