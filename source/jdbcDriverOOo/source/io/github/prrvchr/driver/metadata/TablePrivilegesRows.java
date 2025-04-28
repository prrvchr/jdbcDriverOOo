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
package io.github.prrvchr.driver.metadata;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class TablePrivilegesRows {

    // XXX: We rewrite column indexes 4,5,6 and 7, index 5 and 6 will be assigned dynamically
    private Map<Integer, Object> mColumns = new HashMap<>();
    private boolean mReplaced = false;
    private boolean mWasnull = false;

    public TablePrivilegesRows(final String username)
        throws SQLException {
        final int GRANTOR = 4;
        final int GRANTEE = 5;
        final int IS_GRANTABLE = 7;
        mColumns.put(GRANTOR, null);
        mColumns.put(GRANTEE, username);
        mColumns.put(IS_GRANTABLE, "YES");
    }

    public void setPrivilege(final String privilege) {
        final int PRIVILEGE = 6;
        mColumns.put(PRIVILEGE, privilege);
    }

    public boolean wasNull(final boolean wasnull) {
        boolean isnull = wasnull;
        if (mReplaced) {
            isnull = mWasnull;
        }
        return isnull;
    }

    public String getValue(final int index,
                           final String value) {
        String newvalue = value;
        if (setValue(index)) {
            newvalue = (String) getValue(index);
        }
        return newvalue;
    }

    public boolean getValue(final int index,
                            final boolean value) {
        boolean newvalue = value;
        if (setValue(index)) {
            newvalue = (boolean) getValue(index);
        }
        return newvalue;
    }

    public short getValue(final int index,
                          final short value) {
        short newvalue = value;
        if (setValue(index)) {
            newvalue = (short) getValue(index);
        }
        return newvalue;
    }

    public int getValue(final int index,
                        final int value) {
        int newvalue = value;
        if (setValue(index)) {
            newvalue = (int) getValue(index);
        }
        return newvalue;
    }

    public long getValue(final int index,
                         final long value) {
        long newvalue = value;
        if (setValue(index)) {
            newvalue = (long) getValue(index);
        }
        return newvalue;
    }

    private boolean setValue(int index) {
        mReplaced = mColumns.containsKey(index);
        return mReplaced;
    }

    private Object getValue(final int index) {
        Object value = mColumns.get(index);
        mWasnull = value == null;
        return value;
    }

}
