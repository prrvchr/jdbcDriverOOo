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
package io.github.prrvchr.uno.helper;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.sun.star.logging.LogLevel;
import com.sun.star.logging.XLogHandler;
import com.sun.star.logging.XLogger;
import com.sun.star.logging.XLoggerPool;
import com.sun.star.uno.DeploymentException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;


public class EventLogger {
    private static XLoggerPool sPOOL;
    private static String sSERVICE = "io.github.prrvchr.jdbcDriverOOo.LoggerPool";
    protected XComponentContext mContext;
    private String mName;
    private XLogger mLogger;
    
    // The constructor method:
    public EventLogger(XComponentContext context)
        throws com.sun.star.uno.Exception {
        this(context, "");
    }
    /**
     * Creates an <code>EventLogger</code> instance working with a css.logging.XLogger
     * instance given by name.
     *
     * @param context
     *    the component context to create services.
     * @param name
     *    the name of the logger to create. If empty, the office-wide default logger will be used.
     * @throws com.sun.star.uno.Exception 
     * @throws com.sun.star.uno.Exception 
     */
    public EventLogger(XComponentContext context,
                       String name) {
        mContext = context;
        mName = name;
        if (sPOOL == null) {
            sPOOL = getLoggerPool();
        }
        if (!name.isEmpty()) {
            mLogger = sPOOL.getNamedLogger(name);
        } else {
            mLogger = sPOOL.getDefaultLogger();
        }
    }

    /**
     * Returns the name of the logger.
     * @return the name of the logger
     */
    public String getName() {
        return mName;
    }

    /// Returns the current log level threshold of the logger.
    public int getLogLevel() {
        int level = LogLevel.OFF;
        try {
            if (mLogger != null) {
                level = mLogger.getLevel();
            }
        } catch (Throwable e) { }
        return level;
    }

    /// Sets a new log level threshold of the logger.
    void setLogLevel(int level) {
        try {
            if (mLogger != null) {
                mLogger.setLevel(level);
            }
        } catch (Throwable e) { }
    }

    /// Determines whether an event with the given level would be logged.
    public boolean isLoggable(int level) {
        boolean loggable = false;
        if (mLogger != null) {
            try {
                loggable = mLogger.isLoggable(level);
            } catch (Throwable e) { }
        }
        return loggable;
    }

    /**
     * Adds the given log handler to the logger's set of handlers.
     *
     * Note that normally, you would not use this method: The logger implementations
     * initialize themselves from the configuration, where usually, a default log handler
     * is specified. In this case, the logger will create and use this handler.
     *
     * @param logHandler the handler to add
     *
     * @return
     *   true if and only if the addition was successful (as far as this can be detected
     *   from outside the <code>XLogger</code>'s implementation.
     */
    public boolean addLogHandler(XLogHandler logHandler) {
        boolean added = false;
        if (mLogger != null) {
            try {
                mLogger.addLogHandler(logHandler);
                added = true;
            } catch (Throwable e) { }
        }
        return added;
    }

    /** removes the given log handler from the logger's set of handlers.
     *
     * @param logHandler the handler to remove
     *
     * @return
     *   true if and only if the addition was successful (as far as this can be detected
     *   from outside the <code>XLogger</code>'s implementation.
     */
    public boolean removeLogHandler(XLogHandler logHandler) {
        boolean removed = false;
        if (mLogger != null) {
            try {
                mLogger.removeLogHandler(logHandler);
                removed = true;
            } catch (Throwable e) { }
        }
        return removed;
    }

    /**
     * Logs a given message with its arguments, without the caller's class and method.
     * @param level the log level
     * @param message the message to log
     * @param arguments the arguments to log, which are converted to strings
     *        and replace $1$, $2$, up to $n$ in the message
     * @return whether logging succeeded
     */
    public boolean log(int level, String message, Object... arguments) {
        boolean logged = false;
        if (isLoggable(level)) {
            logged = log(level, null, null, message, arguments);
        }
        return logged;
    }

    /**
     * Logs the given exception.
     * @param level the log level
     * @param exception the exception
     * @return whether logging succeeded
     */
    public boolean log(int level, Throwable exception) {
        return log(level, "", exception);
    }

    /**
     * Logs the given message and exception.
     * @param level the log level
     * @param message the message
     * @param exception the exception
     * @return whether logging succeeded
     */
    public boolean log(int level, String message, Throwable exception) {
        boolean logged = false;
        if (isLoggable(level)) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printerWriter = new PrintWriter(stringWriter);
            exception.printStackTrace(printerWriter);
            message += "\n" + stringWriter.getBuffer().toString();
            logged = log(level, null, null, message);
        }
        return logged;
    }

    /**
     * Logs a given message with its arguments, with the caller's class and method
     * taken from a (relatively costly!) stack trace.
     * @param level the log level
     * @param message the message to log
     * @param arguments the arguments to log, which are converted to strings
     *        and replace $1$, $2$, up to $n$ in the message
     * @return whether logging succeeded
     */
    public boolean logp(int level, String message, Object...arguments) {
        boolean logged = false;
        if (isLoggable(level)) {
            StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
            logged = log(level, caller.getClassName(), caller.getMethodName(), message, arguments);
        }
        return logged;
    }

    /**
     * Logs the given exception, with the caller's class and method.
     * @param level the log level
     * @param exception the exception
     * @return whether logging succeeded
     */
    public boolean logp(int level, Throwable exception) {
        return logp(level, "", exception);
    }

    /**
     * Logs the given message and exception, with the caller's class and method.
     * @param level the log level
     * @param message the message
     * @param exception the exception
     * @return whether logging succeeded
     */
    public boolean logp(int level, String message, Throwable exception) {
        boolean logged = false;
        if (isLoggable(level)) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printerWriter = new PrintWriter(stringWriter);
            exception.printStackTrace(printerWriter);
            message += "\n" + stringWriter.getBuffer().toString();
            StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
            logged = log(level, caller.getClassName(), caller.getMethodName(), message);
        }
        return logged;
    }

    protected boolean log(int level, String clazz, String method, String message, Object... arguments) {
        boolean logged = false;
        if (mLogger != null) {
            if (arguments.length > 0) {
                try {
                    message = String.format(message, arguments);
                } catch (java.lang.Exception e) {
                    System.out.println("EventLogger._log ERROR with message: " + message);
                    // pass
                }
            }
            if (clazz != null && method != null) {
                mLogger.logp(level, clazz, method, message);
            } else {
                mLogger.log(level, message);
            }
            logged = true;
        }
        return logged;
    }

    private XLoggerPool getLoggerPool() {
        XLoggerPool pool = null;
        try {
            Object object = mContext.getServiceManager().createInstanceWithContext(sSERVICE, mContext);
            pool = UnoRuntime.queryInterface(XLoggerPool.class, object);
        } catch (Exception e) { }
        if (pool == null) {
            String msg = "Error: Component context fails to supply singleton css.logging.LoggerPool";
            throw new DeploymentException(msg, mContext);
        }
        return pool;
    }

}
