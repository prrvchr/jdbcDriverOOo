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

import com.sun.star.lang.NullPointerException;
import com.sun.star.resource.MissingResourceException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;


public class ResourceBasedEventLogger
    extends EventLogger {
    private String mBasename;
    private String mIdentifier;
    private String mPath;
    private OfficeResourceBundle mBundle;

    // The constructor method:
    public ResourceBasedEventLogger(ResourceBasedEventLogger logger) {
        this(logger.mContext, logger.mIdentifier, logger.mPath, logger.mBasename, logger.getName());
    }
    public ResourceBasedEventLogger(XComponentContext context,
                                    String identifier,
                                    String path,
                                    String basename,
                                    String logger) {
        super(context, logger);
        mIdentifier = identifier;
        mPath = path;
        mBasename = basename;
        try {
            mBundle = new OfficeResourceBundle(context, identifier, path, basename);
        } catch (NullPointerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Logs a given message, without the caller's class and method.
     * @param level the log level
     * @param message the message to log
     * @return whether logging succeeded
     */
    public boolean log(int level, String message) {
        boolean logged = false;
        if (isLoggable(level)) {
            logged = log(level, null, null, message);
        }
        return logged;
    }

    /**
     * Logs a given message with its arguments, with the caller's class and method
     * taken from a (relatively costly!) stack trace.
     * @param level the log level
     * @param cls name who log this message
     * @param method name who log this message
     * @param message the message to log
     * @return whether logging succeeded
     */
    public boolean logp(int level,
                        String cls,
                        String method,
                        String message) {
        boolean logged = false;
        if (isLoggable(level)) {
            logged = log(level, cls, method, message);
        }
        return logged;
    }

    /**
     * Logs a given resource bundle id with its arguments, without the caller's class and method.
     * @param level the log level
     * @param id the resource ID of the message to log
     * @param arguments the arguments to log, which are converted to strings and
     *        replace $1$, $2$, up to $n$ in the message
     * @return whether logging succeeded
     */
    public boolean logrb(int level,
                         int id,
                         Object... arguments) {
        boolean logged = false;
        if (isLoggable(level)) {
            logged = log(level, null, null, loadStringMessage(id), arguments);
        }
        return logged;
    }

    /**
     * Logs a given resource id with its arguments, with the caller's class and method
     * taken from a (relatively costly!) stack trace.
     * @param level the log level
     * @param id the resource ID of the message to log
     * @param arguments the arguments to log, which are converted to strings and
     *        replace $1$, $2$, up to $n$ in the message
     * @return whether logging succeeded
     */
    public boolean logprb(int level,
                        int id,
                        Object... arguments) {
        boolean logged = false;
        if (isLoggable(level)) {
            StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
            logged = log(level, caller.getClassName(), caller.getMethodName(), loadStringMessage(id), arguments);
        }
        return logged;
    }


    /**
     * Logs a given message with its arguments, with the caller's class and method
     * taken from a (relatively costly!) stack trace.
     * @param level the log level
     * @param cls name who log this message
     * @param method name who log this message
     * @param id the resource string id to log
     * @param arguments the arguments to log, which are converted to strings and
     *        replace $1$, $2$, up to $n$ in the message
     * @return whether logging succeeded
     */
    public boolean logprb(int level,
                          String cls,
                          String method,
                          int id,
                          Object...arguments) {
        boolean logged = false;
        if (isLoggable(level)) {
            logged = log(level, cls, method, loadStringMessage(id), arguments);
        }
        return logged;
    }

    protected boolean logprb(int level,
                             StackTraceElement caller,
                             int id,
                             Object... arguments) {
        boolean logged = false;
        if (isLoggable(level)) {
            logged = log(level, caller.getClassName(), caller.getMethodName(), loadStringMessage(id), arguments);
        }
        return logged;
    }

    public String getStringResource(int id, Object... arguments) {
        String message = loadStringMessage(id);
        if (arguments.length > 0) {
            try {
                message = String.format(message, arguments);
            } catch (java.lang.Exception e) {
                // pass
            }
        }
        return message;
    }

    private String loadStringMessage(int id) {
        String message;
        try {
            message = mBundle.loadString(id);
        } catch (MissingResourceException | Exception e) {
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String pattern = "<invalid event resource: '%s:%d'>\n%s";
            message = String.format(pattern, mBasename, id, writer.getBuffer().toString());
        }
        return message;
    }

}
