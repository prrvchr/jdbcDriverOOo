/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-24 https://prrvchr.github.io                                  ║
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
import java.util.List;


public class TablePrivilegesResultSetBase
    extends ResultSet {

    private java.sql.ResultSetMetaData mMetadata;
    private List<String> mColumns = List.of("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME",
                                            "GRANTOR", "GRANTEE", "PRIVILEGE", "IS_GRANTABLE");

    public TablePrivilegesResultSetBase(java.sql.ResultSet result)
        throws SQLException {
        super(result);
        mMetadata = new TablePrivilegesResultSetMetaData(result.getMetaData(), result, mColumns);
    }

    protected TablePrivilegesResultSetBase(java.sql.ResultSet result,
                                           java.sql.ResultSetMetaData metadata)
        throws SQLException {
        super(result);
        mMetadata = new TablePrivilegesResultSetMetaData(metadata, mColumns);
    }


    @Override
    public java.sql.ResultSetMetaData getMetaData()
        throws SQLException {
        return mMetadata;
    }

    @Override
    public int findColumn(String label)
        throws SQLException {
        int index = 0;
        if (mColumns.contains(label)) {
            index = mColumns.indexOf(label) + 1;
        }
        return index;
    }

    @Override
    public String getString(int index)
        throws SQLException {
        if (0 < index && index <= mColumns.size()) {
            return mResult.getString(mColumns.get(index - 1));
        }
        throw new SQLException();
    }

    @Override
    public String getString(String label)
        throws SQLException {
        return mResult.getString(label);
    }

    @Override
    public boolean getBoolean(int index) throws SQLException {
        if (0 < index && index <= mColumns.size()) {
            return mResult.getBoolean(mColumns.get(index - 1));
        }
        throw new SQLException();
    }

    @Override
    public boolean getBoolean(String label)
        throws SQLException {
        return mResult.getBoolean(label);
    }

    @Override
    public short getShort(int index)
        throws SQLException {
        if (0 < index && index <= mColumns.size()) {
            return mResult.getShort(mColumns.get(index - 1));
        }
        throw new SQLException();
    }

    @Override
    public short getShort(String label)
        throws SQLException {
        return mResult.getShort(label);
    }

    @Override
    public int getInt(int index)
        throws SQLException {
        if (0 < index && index <= mColumns.size()) {
            return mResult.getInt(mColumns.get(index - 1));
        }
        throw new SQLException();
    }

    @Override
    public int getInt(String label)
        throws SQLException {
        return mResult.getInt(label);
    }

    @Override
    public long getLong(int index)
        throws SQLException {
        if (0 < index && index <= mColumns.size()) {
            return mResult.getLong(mColumns.get(index - 1));
        }
        throw new SQLException();
    }

    @Override
    public long getLong(String label)
        throws SQLException {
        return mResult.getLong(label);
    }

}
