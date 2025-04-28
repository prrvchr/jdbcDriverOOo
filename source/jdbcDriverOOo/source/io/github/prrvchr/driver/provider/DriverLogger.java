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
package io.github.prrvchr.driver.provider;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.StackWalker.StackFrame;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.sun.star.logging.XLogger;
import com.sun.star.logging.XLoggerPool;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;


class DriverLogger implements System.Logger {

    private static final String BASE_NAME = "io.github.prrvchr.jdbcDriverOOo.";
    private static final String SERVICE = BASE_NAME + "LoggerPool";
    private static final String ERROR_MSG = "UNO service <" + SERVICE + "> cant be loaded!!!";
    private static final String LOGGER_CLASS = "java.lang.System$Logger";
    private static final String LOGGER_WRAPPER_CLASS = "jdk.internal.logger.AbstractLoggerWrapper";
    private XLogger mLogger;

    public DriverLogger(XComponentContext context, String name, Module module) {
        System.out.println("DriverLogger() 1 Name: " + name + " - Module: " + module.toString());
        if (name == null || name.isBlank()) {
            name = BASE_NAME + "Driver";
        } else {
            name = BASE_NAME + name;
        }
        mLogger = getUnoLoggerPool(context).getNamedLogger(name);
        mLogger.setLevel(Level.ALL.getSeverity());
    }

    @Override
    public String getName() {
        String name = "";
        if (mLogger != null) {
            name = mLogger.getName();
        }
        return name;
    }

    @Override
    public boolean isLoggable(Level level) {
        boolean loggable = false;
        if (mLogger != null) {
            loggable = level.getSeverity() >= mLogger.getLevel();
        }
        System.out.println("DriverLogger.isLoggable() 1 : " + loggable);
        return loggable;
    }

    @Override
    public void log(Level level, ResourceBundle bundle, String key, Throwable thrown) {
        if (isLoggable(level)) {
            StringWriter sw = new StringWriter();
            thrown.printStackTrace(new PrintWriter(sw));
            logMessage(level, bundle, key, sw.toString());
        }
    }

    @Override
    public void log(Level level, ResourceBundle bundle, String key, Object... parameters) {
        if (isLoggable(level)) {
            logMessage(level, bundle, key, parameters);
        }
    }

    private XLoggerPool getUnoLoggerPool(XComponentContext context) {
        Object service = null;
        try {
            service = context.getServiceManager().createInstanceWithContext(SERVICE, context);
        } catch (Exception e) {
            throw new SecurityException(ERROR_MSG, e);
        }
        if (service != null) {
            return UnoRuntime.queryInterface(XLoggerPool.class, service);
        }
        throw new SecurityException(ERROR_MSG);
    }

    private void logMessage(Level level, ResourceBundle bundle, String key, Object... parameters) {
        String msg = getMessage(bundle, key, parameters);
        StackFrame caller = getCallingClass();
        if (caller != null) {
            mLogger.logp(level.getSeverity(), caller.getClassName(), caller.getMethodName(), msg);
        } else {
            mLogger.log(level.getSeverity(), msg);
        }
    }

    private String getMessage(ResourceBundle bundle, String key, Object... parameters) {
        String template = key;
        if (bundle != null) {
            template = bundle.getString(key);
        }
        String msg = template;
        if (parameters != null) {
            try {
                msg = MessageFormat.format(template, parameters);
            } catch (IllegalArgumentException e) {
                System.out.println("DriverLogger.log() 2 Msg: " + key);
                StringBuffer buffer = new StringBuffer(template);
                for (Object parameter : parameters) {
                    buffer.append(" ");
                    buffer.append(parameter.toString());
                }
                msg = buffer.toString();
            }
        }
        return msg;
    }

    private static StackFrame getCallingClass() {
        StackFrame stack = null;
        boolean collected = false;
        List<StackFrame> stacks = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(s -> s.filter(Objects::nonNull).collect(Collectors.toList()));
        for (StackFrame st : stacks) {
            if (st != null) {
                if (collected && !st.getClassName().startsWith(LOGGER_WRAPPER_CLASS)) {
                    stack = st;
                    break;
                }
                if (st.getClassName().startsWith(LOGGER_CLASS)) {
                    collected = true;
                }
            }
        }
        return stack;
    }

}