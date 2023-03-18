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
package io.github.prrvchr.uno.sdbc;

import java.io.Reader;

import com.sun.star.io.XInputStream;
import com.sun.star.lib.uno.adapter.InputStreamToXInputStreamAdapter;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XClob;

//import org.apache.commons.io.input.ReaderInputStream;


public class Clob
    extends ComponentBase
    implements XClob
{

    private final ConnectionBase m_Connection;
    private java.sql.Clob m_Clob;

    // The constructor method:
    public Clob(ConnectionBase connection,
                java.sql.Clob clob)
    {
        m_Connection = connection;
        m_Clob = clob;
    }

    // com.sun.star.lang.XComponent
    @Override
    protected void postDisposing() {
        try {
            m_Clob.free();
        }
        catch (java.sql.SQLException e) {
            m_Connection.getLogger().log(LogLevel.WARNING, e);
        }
    }

    // com.sun.star.sdbc.XClob:
    @Override
    public XInputStream getCharacterStream()
        throws SQLException
    {
        try {
            Reader reader = m_Clob.getCharacterStream();
            return reader != null ? new InputStreamToXInputStreamAdapter(new ReaderInputStream(reader)) : null;
        }
        catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    @Override
    public String getSubString(long position, int lenght)
        throws SQLException
    {
        try {
            return m_Clob.getSubString(position, lenght);
        }
        catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    @Override
    public long length()
        throws SQLException
    {
        try {
            return m_Clob.length();
        }
        catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    @Override
    public long position(String str, int start)
        throws SQLException
    {
        try {
            return m_Clob.position(str, start);
        }
        catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    @Override
    public long positionOfClob(XClob clob, long start)
        throws SQLException
    {
        long position = 0;
        int lenght = (int) clob.length();
        return position(getSubString(position, lenght), (int) start);
    }


}
