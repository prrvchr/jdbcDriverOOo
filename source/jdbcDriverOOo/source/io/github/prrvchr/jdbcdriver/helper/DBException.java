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
package io.github.prrvchr.jdbcdriver.helper;

import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.XInterface;

import io.github.prrvchr.jdbcdriver.StandardSQLState;


public class DBException
{

    public static SQLException getSQLException(XInterface context,
                                               java.sql.SQLException e)
    {
        String msg = e.getLocalizedMessage();
        SQLException ex = msg != null ? new SQLException(msg) : new SQLException();
        ex.Context = context;
        ex.ErrorCode = e.getErrorCode();
        String state = e.getSQLState();
        if (state != null) {
            ex.SQLState = e.getSQLState();
        }
        java.sql.SQLException next = e.getNextException();
        if (next != null) {
            ex.NextException = getSQLException(context, next);
        }
        return ex;
    }

    public static SQLException getSQLException(String msg,
                                               StandardSQLState state,
                                               SQLException e)
    {
        return getSQLException(msg, state, e, e.ErrorCode);
    }

    public static SQLException getSQLException(String msg,
                                               StandardSQLState state,
                                               SQLException e,
                                               int code)
    {
        return new SQLException(msg, e.Context, state.text(), code, e);
    }

    public static SQLException getSQLException(String msg,
                                               XInterface context,
                                               StandardSQLState state)
    {
        return getSQLException(msg, context, state, 0);
    }

    public static SQLException getSQLException(String msg,
                                               XInterface context,
                                               StandardSQLState state,
                                               int code)
    {
        return getSQLException(msg, context, state, null, code);
    }

    public static SQLException getSQLException(String msg,
                                               XInterface context,
                                               StandardSQLState state,
                                               Throwable e)
    {
        return getSQLException(msg, context, state, e, 0);
    }

    public static SQLException getSQLException(String msg,
                                               XInterface context,
                                               StandardSQLState state,
                                               Throwable e,
                                               int code)
    {
        SQLException ex = null;
        if (e != null) {
            ex = getUnoSQLException(e.getLocalizedMessage(), context, e.getClass().getName());
        }
        return getUnoSQLException(msg, context, state.text(), ex, code);
    }

    private static SQLException getUnoSQLException(String msg,
                                                   XInterface context,
                                                   String state)
    {
        return getUnoSQLException(msg, context, state, null, 0);
    }

    private static SQLException getUnoSQLException(String msg,
                                                   XInterface context,
                                                   String state,
                                                   SQLException next,
                                                   int code)
    {
        SQLException e = msg != null ? new SQLException(msg) : new SQLException();
        if (context != null) {
            e.Context = context;
        }
        e.ErrorCode = code;
        if (state != null) {
            e.SQLState = state;
        }
        if (next != null) {
            e.NextException = next;
        }
        return e;
    }

}
