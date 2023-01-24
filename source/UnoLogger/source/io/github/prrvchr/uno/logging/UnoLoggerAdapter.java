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
package io.github.prrvchr.uno.logging;

import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LocationAwareLogger;

import com.sun.star.logging.LogLevel;
import com.sun.star.logging.XLogger;


@SuppressWarnings("deprecation")
public final class UnoLoggerAdapter
    extends MarkerIgnoringBase
    implements LocationAwareLogger
{

    private static final long serialVersionUID = 1L;
    private final XLogger m_xLogger;

    // The constructor method:
    public UnoLoggerAdapter(String logger)
    {
        System.out.println("logging.UnoLoggerAdapter() 1");
        //m_xLogger = UnoHelper.getLoggerPool().getNamedLogger(logger);
        m_xLogger = UnoLoggerPool.getNamedLogger(logger);

        System.out.println("logging.UnoLoggerAdapter() 2: " + m_xLogger.getName());
    }

    @Override
    public boolean isDebugEnabled() {
        return m_xLogger.isLoggable(LogLevel.FINE);
    }

    @Override
    public boolean isErrorEnabled() {
        return m_xLogger.isLoggable(LogLevel.SEVERE);
    }

    @Override
    public boolean isInfoEnabled() {
        return m_xLogger.isLoggable(LogLevel.INFO);
    }

    @Override
    public boolean isTraceEnabled() {
        return m_xLogger.isLoggable(LogLevel.FINEST);
    }

    @Override
    public boolean isWarnEnabled() {
        return m_xLogger.isLoggable(LogLevel.WARNING);
    }

    @Override
    public void log(Marker marker, String caller, int level, String message, Object[] args, Throwable t) {
        level = toUnoLevel(level);
        if (m_xLogger.isLoggable(level)) {
            StackTraceElement call = Thread.currentThread().getStackTrace()[2];
            m_xLogger.logp(level, call.getClassName(), call.getMethodName(), message);
        }
    }

    private int toUnoLevel(int level) {
        int unolevel;
        switch (level) {
        case LocationAwareLogger.TRACE_INT:
            unolevel = LogLevel.FINEST;
            break;
        case LocationAwareLogger.DEBUG_INT:
            unolevel = LogLevel.FINE;
            break;
        case LocationAwareLogger.INFO_INT:
            unolevel = LogLevel.INFO;
            break;
        case LocationAwareLogger.WARN_INT:
            unolevel = LogLevel.WARNING;
            break;
        case LocationAwareLogger.ERROR_INT:
            unolevel = LogLevel.SEVERE;
            break;
        default:
            throw new IllegalStateException("Level number " + level + " is not recognized.");
        }
        return unolevel;
    }

    @Override
    public void debug(String message) {
        if (m_xLogger.isLoggable(LogLevel.FINE)) {
            m_xLogger.log(LogLevel.FINE, message);
        }
    }

    @Override
    public void debug(String format, Object arg) {
        if (m_xLogger.isLoggable(LogLevel.FINE)) {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            m_xLogger.log(LogLevel.FINE, ft.getMessage());
        }
    }

    @Override
    public void debug(String format, Object... args) {
        if (m_xLogger.isLoggable(LogLevel.FINE)) {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, args);
            m_xLogger.log(LogLevel.FINE, ft.getMessage());
        }
    }

    @Override
    public void debug(String message, Throwable t) {
        debug(message);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (m_xLogger.isLoggable(LogLevel.FINE)) {
            FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            m_xLogger.log(LogLevel.FINE, ft.getMessage());
        }
    }

    @Override
    public void error(String message) {
        if (m_xLogger.isLoggable(LogLevel.SEVERE)) {
            m_xLogger.log(LogLevel.SEVERE, message);
        }
    }

    @Override
    public void error(String format, Object arg) {
        if (m_xLogger.isLoggable(LogLevel.SEVERE)) {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            m_xLogger.log(LogLevel.SEVERE, ft.getMessage());
        }
    }

    @Override
    public void error(String format, Object... args) {
        if (m_xLogger.isLoggable(LogLevel.SEVERE)) {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, args);
            m_xLogger.log(LogLevel.SEVERE, ft.getMessage());
        }
    }

    @Override
    public void error(String message, Throwable t) {
        error(message);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (m_xLogger.isLoggable(LogLevel.SEVERE)) {
            FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            m_xLogger.log(LogLevel.SEVERE, ft.getMessage());
        }
    }

    @Override
    public void info(String message) {
        if (m_xLogger.isLoggable(LogLevel.INFO)) {
            m_xLogger.log(LogLevel.INFO, message);
        }
    }

    @Override
    public void info(String format, Object arg) {
        if (m_xLogger.isLoggable(LogLevel.INFO)) {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            m_xLogger.log(LogLevel.INFO, ft.getMessage());
        }
    }

    @Override
    public void info(String format, Object... args) {
        if (m_xLogger.isLoggable(LogLevel.INFO)) {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, args);
            m_xLogger.log(LogLevel.INFO, ft.getMessage());
        }
    }

    @Override
    public void info(String message, Throwable t) {
        info(message);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (m_xLogger.isLoggable(LogLevel.INFO)) {
            FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            m_xLogger.log(LogLevel.INFO, ft.getMessage());
        }
    }

    @Override
    public void trace(String message) {
        if (m_xLogger.isLoggable(LogLevel.FINEST)) {
            m_xLogger.log(LogLevel.FINEST, message);
        }
    }

    @Override
    public void trace(String format, Object arg) {
        if (m_xLogger.isLoggable(LogLevel.FINEST)) {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            m_xLogger.log(LogLevel.FINEST, ft.getMessage());
        }
    }

    @Override
    public void trace(String format, Object... args) {
        if (m_xLogger.isLoggable(LogLevel.FINEST)) {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, args);
            m_xLogger.log(LogLevel.FINEST, ft.getMessage());
        }
    }

    @Override
    public void trace(String message, Throwable t) {
        trace(message);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (m_xLogger.isLoggable(LogLevel.FINEST)) {
            FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            m_xLogger.log(LogLevel.FINEST, ft.getMessage());
        }
    }

    @Override
    public void warn(String message) {
        if (m_xLogger.isLoggable(LogLevel.WARNING)) {
            m_xLogger.log(LogLevel.WARNING, message);
        }
    }

    @Override
    public void warn(String format, Object arg) {
        if (m_xLogger.isLoggable(LogLevel.WARNING)) {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            m_xLogger.log(LogLevel.WARNING, ft.getMessage());
        }
    }

    @Override
    public void warn(String format, Object... args) {
        if (m_xLogger.isLoggable(LogLevel.WARNING)) {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, args);
            m_xLogger.log(LogLevel.WARNING, ft.getMessage());
        }
    }

    @Override
    public void warn(String message, Throwable t) {
        warn(message);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (m_xLogger.isLoggable(LogLevel.WARNING)) {
            FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            m_xLogger.log(LogLevel.WARNING, ft.getMessage());
        }
    }


}