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
package io.github.prrvchr.uno.helper;

import com.sun.star.logging.XLogger;
import com.sun.star.logging.XLoggerPool;
import com.sun.star.uno.DeploymentException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;


public final class UnoLoggerPool {

    private static XComponentContext m_xContext;
    private static String m_root;
    private static String m_service = "io.github.prrvchr.jdbcDriverOOo.LoggerPool";

    // The constructor method:
    public UnoLoggerPool() {
        System.out.println("logging.UnoLoggerPool()");
    }

    public static void initialize(XComponentContext context,
                                  String root) {
        m_xContext = context;
        m_root = root;
    }

    public static XLogger getNamedLogger(String name) {
        return _getLoggerPool().getNamedLogger(getLoggerName(name));
    }

    public static XLogger getDefaultLogger() {
        return _getLoggerPool().getDefaultLogger();
    }

    private static String getLoggerName(String name) {
        return String.format("%s.%s", m_root, name);
    }

    private static XLoggerPool _getLoggerPool() {
        XLoggerPool pool = null;
        try {
            Object object = m_xContext.getServiceManager().createInstanceWithContext(m_service, m_xContext);
            pool = UnoRuntime.queryInterface(XLoggerPool.class, object);
        }
        catch (Exception e) {}
        if (pool == null) {
            throw new DeploymentException("component context fails to supply singleton com.sun.star.logging.LoggerPool of type com.sun.star.logging.XLoggerPool",
                                          m_xContext);
        }
        return pool;
    }

}