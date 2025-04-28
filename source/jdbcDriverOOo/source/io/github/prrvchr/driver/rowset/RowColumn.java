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
package io.github.prrvchr.driver.rowset;

import java.sql.ResultSet;


public class RowColumn {

    private static String sAUTOINCREMENT = "YES";
    private RowTable mTable;
    private String mName;
    private String mIdentifier;
    private int mType = 0;
    private int mIndex = 0;
    private boolean mAutoIncrement = false;

    // The constructor method:
    public RowColumn(RowTable table,
                     ResultSet result,
                     boolean ordinal)
        throws java.sql.SQLException {
        mTable = table;
        final int COLUMN_NAME = 4;
        final int DATA_TYPE = 5;
        final int ORDINAL_POSITION = 17;
        final int IS_AUTOINCREMENT = 23;
        mName = result.getString(COLUMN_NAME);
        mIdentifier = table.getCatalog().enquoteIdentifier(mName);
        mType = result.getInt(DATA_TYPE);
        if (ordinal) {
            mIndex = result.getInt(ORDINAL_POSITION);
        } else {
            mIndex = 0;
        }
        mAutoIncrement = sAUTOINCREMENT.equalsIgnoreCase(result.getString(IS_AUTOINCREMENT));
    }

    public RowTable getTable() {
        return mTable;
    }

    public String getCatalogName() {
        return mTable.getCatalogName();
    }

    public String getSchemaName() {
        return mTable.getSchemaName();
    }

    public String getTableName() {
        return mTable.getName();
    }

    public String getName() {
        return mName;
    }

    public String getPredicate() {
        return String.format(mTable.getParameter(), mIdentifier);
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int index) {
        mIndex = index;
    }

    public int getType() {
        return mType;
    }

    public boolean isAutoIncrement() {
        return mAutoIncrement;
    }

}
