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
package io.github.prrvchr.uno.sdbc;

import com.sun.star.io.XInputStream;
import com.sun.star.lib.uno.adapter.InputStreamToXInputStreamAdapter;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XBlob;

import io.github.prrvchr.uno.helper.UnoHelper;


public final class Blob
    extends ComponentBase
    implements XBlob {

    private final ConnectionBase mConnection;
    private java.sql.Blob mBlob;

    // The constructor method:
    public Blob(ConnectionBase connection,
                java.sql.Blob blob) {
        mConnection = connection;
        mBlob = blob;
    }

    // com.sun.star.lang.XComponent
    @Override
    protected void postDisposing() {
        try {
            mBlob.free();
        } catch (java.sql.SQLException e) {
            mConnection.getLogger().log(LogLevel.WARNING, e);
        }
    }

    // com.sun.star.sdbc.XBlob:
    @Override
    public XInputStream getBinaryStream()
        throws SQLException {
        try {
            return new InputStreamToXInputStreamAdapter(mBlob.getBinaryStream());
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public byte[] getBytes(long position, int length)
        throws SQLException {
        try {
            return mBlob.getBytes(position, length);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public long length()
        throws SQLException {
        try {
            return mBlob.length();
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public long position(byte[] pattern, long start)
        throws SQLException {
        try {
            return mBlob.position(pattern, start);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public long positionOfBlob(XBlob blob, long start)
        throws SQLException {
        long position = 0;
        int lenght = (int) blob.length();
        return position(blob.getBytes(position, lenght), start);
    }


}
