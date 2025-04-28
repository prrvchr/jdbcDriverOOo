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

abstract class ResultSetMetaData implements java.sql.ResultSetMetaData {

    private final java.sql.ResultSetMetaData mMetadata;

    protected ResultSetMetaData(java.sql.ResultSetMetaData metadata) {
        mMetadata = metadata;
    }

    @Override
    public <T> T unwrap(Class<T> iface)
        throws SQLException {
        return mMetadata.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface)
        throws SQLException {
        return mMetadata.isWrapperFor(iface);
    }

    @Override
    public int getColumnCount()
        throws SQLException {
        return mMetadata.getColumnCount();
    }

    @Override
    public boolean isAutoIncrement(int index)
        throws SQLException {
        return mMetadata.isAutoIncrement(getIndex(index));
    }

    @Override
    public boolean isCaseSensitive(int index)
        throws SQLException {
        return mMetadata.isCaseSensitive(getIndex(index));
    }

    @Override
    public boolean isSearchable(int index)
        throws SQLException {
        return mMetadata.isSearchable(getIndex(index));
    }

    @Override
    public boolean isCurrency(int index)
        throws SQLException {
        return mMetadata.isCurrency(getIndex(index));
    }

    @Override
    public int isNullable(int index)
        throws SQLException {
        return mMetadata.isNullable(getIndex(index));
    }

    @Override
    public boolean isSigned(int index)
        throws SQLException {
        return mMetadata.isSigned(getIndex(index));
    }

    @Override
    public int getColumnDisplaySize(int index)
        throws SQLException {
        return mMetadata.getColumnDisplaySize(getIndex(index));
    }

    @Override
    public String getColumnLabel(int index)
        throws SQLException {
        return mMetadata.getColumnLabel(getIndex(index));
    }

    @Override
    public String getColumnName(int index)
        throws SQLException {
        return mMetadata.getColumnName(getIndex(index));
    }

    @Override
    public String getSchemaName(int index)
        throws SQLException {
        return mMetadata.getSchemaName(getIndex(index));
    }

    @Override
    public int getPrecision(int index)
        throws SQLException {
        return mMetadata.getPrecision(getIndex(index));
    }

    @Override
    public int getScale(int index)
        throws SQLException {
        return mMetadata.getScale(getIndex(index));
    }

    @Override
    public String getTableName(int index)
        throws SQLException {
        return mMetadata.getTableName(getIndex(index));
    }

    @Override
    public String getCatalogName(int index)
        throws SQLException {
        return mMetadata.getCatalogName(getIndex(index));
    }

    @Override
    public int getColumnType(int index)
        throws SQLException {
        return mMetadata.getColumnType(getIndex(index));
    }

    @Override
    public String getColumnTypeName(int index)
        throws SQLException {
        return mMetadata.getColumnTypeName(getIndex(index));
    }

    @Override
    public boolean isReadOnly(int index)
        throws SQLException {
        return mMetadata.isReadOnly(getIndex(index));
    }

    @Override
    public boolean isWritable(int index)
        throws SQLException {
        return mMetadata.isWritable(getIndex(index));
    }

    @Override
    public boolean isDefinitelyWritable(int index)
        throws SQLException {
        return mMetadata.isDefinitelyWritable(getIndex(index));
    }

    @Override
    public String getColumnClassName(int index)
        throws SQLException {
        return mMetadata.getColumnClassName(getIndex(index));
    }

    abstract int getIndex(int index) throws SQLException;
}
