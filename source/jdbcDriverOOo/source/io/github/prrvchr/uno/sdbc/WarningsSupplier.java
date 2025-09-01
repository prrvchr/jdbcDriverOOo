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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.Wrapper;

import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.SQLWarning;
import com.sun.star.uno.Any;
import com.sun.star.uno.XInterface;

import io.github.prrvchr.uno.helper.UnoHelper;


public final class WarningsSupplier {

    public static void clearWarnings(final Wrapper wrapper,
                                     final XInterface component)
        throws SQLException {
        // FIXME: Statement performs lazy loading and the wrapper can be null!!!
        if (wrapper != null) {
            try {
                if (wrapper.isWrapperFor(ResultSet.class)) {
                    wrapper.unwrap(ResultSet.class).clearWarnings();
                } else if (wrapper.isWrapperFor(CallableStatement.class)) {
                    wrapper.unwrap(CallableStatement.class).clearWarnings();
                } else if (wrapper.isWrapperFor(PreparedStatement.class)) {
                    wrapper.unwrap(PreparedStatement.class).clearWarnings();
                } else if (wrapper.isWrapperFor(Statement.class)) {
                    wrapper.unwrap(Statement.class).clearWarnings();
                } else if (wrapper.isWrapperFor(Connection.class)) {
                    wrapper.unwrap(Connection.class).clearWarnings();
                }
            } catch (java.sql.SQLException e) {
                throw UnoHelper.getSQLException(e, component);
            }
        }
    }

    public static Object getWarnings(final Wrapper wrapper,
                                     final XInterface component)
        throws SQLException {
        // FIXME: Statement performs lazy loading and the wrapper can be null!!!
        Object warning = Any.VOID;
        if (wrapper != null) {
            java.sql.SQLWarning w = null;
            try {
                if (wrapper.isWrapperFor(ResultSet.class)) {
                    w = wrapper.unwrap(ResultSet.class).getWarnings();
                } else if (wrapper.isWrapperFor(CallableStatement.class)) {
                    w = wrapper.unwrap(CallableStatement.class).getWarnings();
                } else if (wrapper.isWrapperFor(PreparedStatement.class)) {
                    w = wrapper.unwrap(PreparedStatement.class).getWarnings();
                } else if (wrapper.isWrapperFor(Statement.class)) {
                    w = wrapper.unwrap(Statement.class).getWarnings();
                } else if (wrapper.isWrapperFor(Connection.class)) {
                    w = wrapper.unwrap(Connection.class).getWarnings();
                }
            } catch (java.sql.SQLException e) {
                throw UnoHelper.getSQLException(e, component);
            }
            if (w != null) {
                warning = getWarnings(w, component);
            }
        }
        return warning;
    }

    public static SQLWarning getWarnings(java.sql.SQLWarning w, XInterface component) {
        SQLWarning warning;
        String msg = w.getLocalizedMessage();
        if (msg != null) {
            warning = new SQLWarning(msg);
        } else {
            warning = new SQLWarning();
        }
        if (component != null) {
            warning.Context = component;
        }
        String state = w.getSQLState();
        if (state != null) {
            warning.SQLState = w.getSQLState();
        }
        warning.ErrorCode = w.getErrorCode();
        if (w.getNextWarning() != null) {
            warning.NextException = getWarnings(w.getNextWarning(), component);
        }
        return warning;
    }

}
