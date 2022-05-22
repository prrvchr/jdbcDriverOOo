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
package io.github.prrvchr.uno.sdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.SQLWarning;
import com.sun.star.uno.Any;
import com.sun.star.uno.XInterface;

import io.github.prrvchr.uno.helper.UnoHelper;


final class WarningsSupplier
{

    static void clearWarnings(final java.sql.Wrapper wrapper,
                              final XInterface component)
        throws SQLException
    {
        // FIXME: Statement performs lazy loading and the wrapper can be null!!!
        if (wrapper != null) {
            try {
                if (wrapper.isWrapperFor(ResultSet.class)) {
                    wrapper.unwrap(ResultSet.class).clearWarnings();
                }
                else if (wrapper.isWrapperFor(Statement.class)) {
                    wrapper.unwrap(Statement.class).clearWarnings();
                }
                else if (wrapper.isWrapperFor(Connection.class)) {
                    wrapper.unwrap(Connection.class).clearWarnings();
                }
            }
            catch (java.sql.SQLException e) {
                throw UnoHelper.getSQLException(e, component);
            }
        }
    }


    static Object getWarnings(final java.sql.Wrapper wrapper,
                              final XInterface component)
        throws SQLException
    {
        java.sql.SQLWarning warning = null;
        // FIXME: Statement performs lazy loading and the wrapper can be null!!!
        if (wrapper != null) {
            try {
                if (wrapper.isWrapperFor(ResultSet.class)) {
                    warning = wrapper.unwrap(ResultSet.class).getWarnings();
                }
                else if (wrapper.isWrapperFor(Statement.class)) {
                    warning = wrapper.unwrap(Statement.class).getWarnings();
                }
                else if (wrapper.isWrapperFor(Connection.class)) {
                    warning = wrapper.unwrap(Connection.class).getWarnings();
                }
            }
            catch (java.sql.SQLException e) {
                throw UnoHelper.getSQLException(e, component);
            }
        }
        return _getWarnings(warning, component);
    }


    private static Object _getWarnings(java.sql.SQLWarning warning, XInterface component)
    {
        if (warning != null)
            return _getWarning(warning, component);
        return Any.VOID;
    }

    private static SQLWarning _getWarning(java.sql.SQLWarning w, XInterface component)
    {
        SQLWarning warning = new SQLWarning(w.getMessage());
        warning.Context = component;
        warning.SQLState = w.getSQLState();
        warning.ErrorCode = w.getErrorCode();
        warning.NextException = _getWarnings(w.getNextWarning(), component);
        return warning;
    }


}
