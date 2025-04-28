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
package io.github.prrvchr.driver.logger;

import java.io.Writer;

import com.sun.star.logging.LogLevel;
import com.sun.star.logging.XLogger;
import com.sun.star.uno.Exception;

import io.github.prrvchr.uno.helper.UnoLoggerPool;

public final class DerbyLoggerBridge {

    private static XLogger sLogger = null;

    private DerbyLoggerBridge()
        throws Exception {
        sLogger = UnoLoggerPool.getNamedLogger("derby");
    }

    public static final class LoggingWriter
        extends Writer {
        @Override
        public void write(final char[] cbuf, final int off, final int len) {
            if (len > 1) {
                sLogger.log(LogLevel.INFO, new String(cbuf, off, len));
            }
        }

        @Override
        public void flush() {
            // noop.
        }

        @Override
        public void close() {
            // noop.
        }
    }

    public static Writer bridge() {
        return new LoggingWriter();
    }


}
