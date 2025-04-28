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

import java.util.HashMap;
import java.util.Map;

import com.sun.star.uno.XComponentContext;

public class DriverLoggerFinder extends System.LoggerFinder {

    private static Map<String, DriverLogger> sLOGGERS = new HashMap<String, DriverLogger>();
    private XComponentContext mContext = null;

    public DriverLoggerFinder(XComponentContext context) {
        mContext = context;
    }

    @Override
    public System.Logger getLogger(String name, Module module) {
        System.out.println("DriverLoggerFinder() 1 Name: " + name);
        DriverLogger logger = null;
        if (!sLOGGERS.containsKey(name)) {
            try {
                System.out.println("DriverLoggerFinder() 2");
                logger = new DriverLogger(mContext, name, module);
                System.out.println("DriverLoggerFinder() 3 Name: " + name);
                sLOGGERS.put(name, logger);
                System.out.println("DriverLoggerFinder() 4 Name: " + name);
            } catch (Throwable e) {
                e.printStackTrace();
                throw new SecurityException(e);
            }
        } else {
            logger = sLOGGERS.get(name);
        }
        if (logger == null) {
            throw new SecurityException();
        }
        return logger;
    }

}