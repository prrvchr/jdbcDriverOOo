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

import java.util.HashSet;
import java.util.Set;

import com.sun.star.logging.XLogger;
import com.sun.star.logging.XLoggerPool;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;


public final class UnoLoggerPool
{

    private static XComponentContext m_xContext;
    private static String m_root;
    private static String m_service = "/singletons/com.sun.star.logging.LoggerPool";
    private static Set<String> m_loggers = new HashSet<String>();


    // The constructor method:
    public UnoLoggerPool()
    {
        System.out.println("logging.UnoLoggerPool() 1: ");
    }

    public static void initialize(XComponentContext context,
                                  String root)
    {
        System.out.println("logging.UnoLoggerPool.setRoot() 1: ");
        m_xContext = context;
        m_root = root;
    }

    public static String[] getLoggerNames()
    {
        return m_loggers.toArray(new String[m_loggers.size()]);
    }

    public static XLogger getNamedLogger(String name)
    {
        if (!m_loggers.contains(name)) {
            m_loggers.add(name);
        }
        XLogger logger = getLoggerPool().getNamedLogger(getLoggerName(name));
        System.out.println("logging.UnoLoggerPool.getNamedLogger() " + logger.getName());
        return logger;
    }

    public static XLogger getDefaultLogger()
    {
        XLogger logger = getLoggerPool().getDefaultLogger();
        System.out.println("logging.UnoLoggerPool.getDefaultLogger() " + logger.getName());
        return logger;
    }

    private static XLoggerPool getLoggerPool()
    {
        System.out.println("logging.UnoLoggerPool.getLoggerPool()");
        return UnoRuntime.queryInterface(XLoggerPool.class, m_xContext.getValueByName(m_service));
    }

    private static String getLoggerName(String name)
    {
        return String.format("%s.%s", m_root, name);
    }


}