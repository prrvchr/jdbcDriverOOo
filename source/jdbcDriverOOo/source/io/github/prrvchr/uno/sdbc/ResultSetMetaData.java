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
package io.github.prrvchr.uno.sdbc;

import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XResultSetMetaData;

import io.github.prrvchr.uno.helper.UnoHelper;


public final class ResultSetMetaData
    extends WeakBase
    implements XResultSetMetaData {

    private final java.sql.ResultSetMetaData mMetadata;
    private final ConnectionBase mConnection;
    private int mCount;

    // The constructor method:
    public ResultSetMetaData(ConnectionBase connection,
                             java.sql.ResultSetMetaData metadata) {
        mConnection = connection;
        mMetadata = metadata;
        mCount = -1;
    }


    // com.sun.star.sdbc.XResultSetMetaData:
    @Override
    public int getColumnCount()
        throws SQLException {
        try {
            if (mCount == -1) {
                mCount = mMetadata.getColumnCount();
            }
            return mCount;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getCatalogName(int index)
        throws SQLException {
        try {
            String value = mMetadata.getCatalogName(index);
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getColumnDisplaySize(int index)
        throws SQLException {
        try {
            return mMetadata.getColumnDisplaySize(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getColumnLabel(int index)
        throws SQLException {
        try {
            String value = mMetadata.getColumnLabel(index);
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getColumnName(int index)
        throws SQLException {
        try {
            String value = mMetadata.getColumnName(index);
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getColumnServiceName(int index)
        throws SQLException {
        try {
            String value = mMetadata.getColumnClassName(index);
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getColumnType(int index)
        throws SQLException {
        try {
            return mConnection.getProvider().getDataType(mMetadata.getColumnType(index));
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getColumnTypeName(int index)
        throws SQLException {
        try {
            String value = mMetadata.getColumnTypeName(index);
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getSchemaName(int index)
        throws SQLException {
        try {
            String value = mMetadata.getSchemaName(index);
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getTableName(int index)
        throws SQLException {
        try {
            String value = mMetadata.getTableName(index);
            if (value == null) {
                value = "";
            }
            return value;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isCaseSensitive(int index)
        throws SQLException {
        try {
            return mMetadata.isCaseSensitive(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isCurrency(int index)
        throws SQLException {
        try {
            boolean currency = false;
            if (!mConnection.getProvider().isIgnoreCurrencyEnabled()) {
                currency = mMetadata.isCurrency(index);
            }
            return currency;
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isAutoIncrement(int index)
        throws SQLException {
        try {
            return mMetadata.isAutoIncrement(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isSigned(int index)
        throws SQLException {
        try {
            return mMetadata.isSigned(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getPrecision(int index)
        throws SQLException {
        try {
            return mMetadata.getPrecision(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getScale(int index)
        throws SQLException {
        try {
            return mMetadata.getScale(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int isNullable(int index)
        throws SQLException {
        try {
            return mMetadata.isNullable(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isSearchable(int index)
        throws SQLException {
        try {
            return mMetadata.isSearchable(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isReadOnly(int index)
        throws SQLException {
        try {
            return mMetadata.isReadOnly(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isDefinitelyWritable(int index)
        throws SQLException {
        try {
            return  mMetadata.isDefinitelyWritable(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isWritable(int index)
        throws SQLException {
        try {
            return mMetadata.isWritable(index);
        } catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }


}