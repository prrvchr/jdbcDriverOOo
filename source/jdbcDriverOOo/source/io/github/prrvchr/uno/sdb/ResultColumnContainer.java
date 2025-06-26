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
package io.github.prrvchr.uno.sdb;


import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.driver.helper.DBTools;
import io.github.prrvchr.uno.sdbcx.ContainerBase;


public final class ResultColumnContainer
    extends ContainerBase<ResultColumn> {
    private static final String SERVICE = ResultColumnContainer.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.Container"};

    private static final int COLUMN_NAME = 2;
    private static final int REMARKS = 12;
    private static final int COLUMN_DEF = 13;

    private DatabaseMetaData mDbMetaData;
    private ResultSetMetaData mRsMetaData;

    // The constructor method:
    protected ResultColumnContainer(Object lock,
                                    DatabaseMetaData dbMetadata,
                                    ResultSetMetaData rsMetadata,
                                    boolean sensitive) {
        super(SERVICE, SERVICES, lock, sensitive, getColumnNames(rsMetadata));
        mDbMetaData = dbMetadata;
        mRsMetaData = rsMetadata;
    }

    private static List<String> getColumnNames(ResultSetMetaData metadata) {
        List<String> names = new ArrayList<>();
        try {
            int count = metadata.getColumnCount();
            for (int i = 1; i <= count; i++) {
                names.add(metadata.getColumnName(i));
            }
        } catch (java.sql.SQLException e) { }
        return names;
    }

    @Override
    protected ResultColumn createElement(int index)
        throws SQLException {
        try {
            int i = index + 1;
            String catalog = mRsMetaData.getCatalogName(i);
            String schema = mRsMetaData.getSchemaName(i);
            String table = mRsMetaData.getTableName(i);
            String name = mRsMetaData.getColumnName(i);
            String typeName = mRsMetaData.getColumnTypeName(i);
            boolean autoincrement = mRsMetaData.isAutoIncrement(i);
            boolean currency = mRsMetaData.isCurrency(i);
            int type = mRsMetaData.getColumnType(i);
            int precision = mRsMetaData.getPrecision(i);
            int scale = mRsMetaData.getScale(i);
            int nullable = mRsMetaData.isNullable(i);
            String description = "";
            String defaultValue = "";
            try (ResultSet rs = mDbMetaData.getColumns(catalog, schema, table, name)) {
                if (rs.next()) {
                    description = rs.getString(REMARKS);
                    if (rs.wasNull()) {
                        description = "";
                    }
                    defaultValue = rs.getString(COLUMN_DEF);
                    if (rs.wasNull()) {
                        defaultValue = "";
                    }
                }
            }
            boolean rowversion = false;
            try (ResultSet rs = mDbMetaData.getVersionColumns(catalog, schema, table)) {
                while (rs.next()) {
                    if (name.equals(rs.getString(COLUMN_NAME))) {
                        rowversion = true;
                        break;
                    }
                }
            }
            return new ResultColumn(mRsMetaData, i, catalog, schema, table,
                                    isCaseSensitive(), name, typeName, defaultValue,
                                    description, nullable, precision, scale, type,
                                    autoincrement, rowversion, currency);
        } catch (java.sql.SQLException e) {
            throw DBTools.getSQLException(e, this);
        }
    }

}
