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
/**************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 *************************************************************/
package io.github.prrvchr.uno.driver.provider;

import io.github.prrvchr.uno.helper.ResourceBasedEventLogger;

import com.sun.star.beans.NamedValue;
import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.uno.XInterface;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.XCloseable;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.Any;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;


public class Tools {
    public static SQLException toUnoException(Object source, Throwable throwable) {
        // FIXME: use SQLException.getNextException() instead of getCause()?
        // There are up to 3 dimensions of exception chaining of warnings in Java,
        // getCause(), getNextException(), and getNextWarning().
        // The C++ implementation used getNextException() only,
        // but I am using the widely used and more helpful getCause().
        SQLException ex;
        Throwable cause = throwable.getCause();
        Object unoCause = Any.VOID;
        if (cause != null) {
            unoCause = toUnoException(source, throwable);
        }
        if (throwable instanceof SQLException) {
            ex = (SQLException) throwable;
        } else if (throwable instanceof java.sql.SQLException) {
            java.sql.SQLException sqlException = (java.sql.SQLException) throwable;
            ex = new SQLException(sqlException.getMessage(), source,
                    sqlException.getSQLState(), sqlException.getErrorCode(), unoCause);
        } else if (throwable instanceof com.sun.star.uno.Exception) {
            // General UNO exception. Wrap in an SQLException and rethrow:
            com.sun.star.uno.Exception exception = (com.sun.star.uno.Exception) throwable;
            ex = new SQLException(exception.getMessage(), source,
                                  StandardSQLState.SQL_GENERAL_ERROR.text(), 0, exception);
        } else {
            // General Java exception. We can't pass this to UNO, so convert it to an UNO SQLException:
            String message = throwable.getMessage();
            if (message.isEmpty()) {
                message = throwable.getLocalizedMessage();
            }
            if (message.isEmpty()) {
                message = throwable.toString();
            }
            ex = new SQLException(message, source, "", -1, unoCause);
        }
        return ex;
    }
    
    public static SQLException toUnoExceptionLogged(Object source,
                                                    ResourceBasedEventLogger logger,
                                                    Throwable throwable) {
        
        SQLException exception = toUnoException(source, throwable);
        logger.log(LogLevel.SEVERE, exception);
        return exception;
    }

    public static String getOrDefault(PropertyValue[] properties,
                                      String name,
                                      String defaultValue)
        throws IllegalArgumentException {
        String ret = defaultValue;
        for (PropertyValue property : properties) {
            if (property.Name.equals(name)) {
                ret = AnyConverter.toString(property.Value);
                break;
            }
        }
        return ret;
    }

    public static boolean getOrDefault(PropertyValue[] properties,
                                       String name,
                                       boolean defaultValue)
        throws IllegalArgumentException {
        boolean ret = defaultValue;
        for (PropertyValue property : properties) {
            if (property.Name.equals(name)) {
                ret = AnyConverter.toBoolean(property.Value);
                break;
            }
        }
        return ret;
    }

    public static Object getOrDefault(PropertyValue[] properties,
                                      String name,
                                      Object defaultValue)
        throws IllegalArgumentException {
        Object ret = defaultValue;
        for (PropertyValue property : properties) {
            if (property.Name.equals(name)) {
                ret = property.Value;
                break;
            }
        }
        return ret;
    }

    public static NamedValue[] getOrDefault(PropertyValue[] properties,
                                            String name,
                                            NamedValue[] defaultValue)
        throws IllegalArgumentException {
        NamedValue[] ret = defaultValue;
        for (PropertyValue property : properties) {
            if (property.Name.equals(name)) {
                Object[] array = (Object[]) AnyConverter.toArray(property.Value);
                ret = new NamedValue[array.length];
                for (int i = 0; i < array.length; i++) {
                    ret[i] = (NamedValue) array[i];
                }
                break;
            }
        }
        return ret;
    }

    public static void close(XInterface object)
        throws SQLException {
        XCloseable closeable = UnoRuntime.queryInterface(XCloseable.class, object);
        if (closeable != null) {
            closeable.close();
        }
    }

}
