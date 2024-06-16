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
package io.github.prrvchr.jdbcdriver.rowset;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.BitSet;


public class RowHelper
{

    public static void setDefaultColumnValues(ResultSet result,
                                              BitSet column)
        throws SQLException
    {
        // XXX: On insert if we want to succeed, we need to set to NULL all
        // XXX: auto-increment and nullable columns that have not been updated
        System.out.println("DBTools.setDefaultColumnValues() 1");
        ResultSetMetaData metadata = result.getMetaData();
        int count = metadata.getColumnCount();
        for (int index = 1; index <= count; index ++) {
            if (!column.get(index - 1)) {
                boolean nullable = metadata.isNullable(index) != ResultSetMetaData.columnNoNulls;
                if (nullable) {
                    System.out.println("DBTools.setDefaultColumnValues() 2 updateNull Index: " + index);
                    result.updateNull(index);
                }
                System.out.println("DBTools.setDefaultColumnValues() 3");
            }
        }
    }

    public static boolean setCurrentRow(ResultSet result,
                                        Row row,
                                        int count)
        throws SQLException
    {
        System.out.println("DBTools.setColumnValues() 1");
        boolean updated = true;
        boolean hasupdate = false;
        for (int i = 1; i <= count; i++) {
            if (row.isColumnUpdated(i)) {
                hasupdate = true;
                Object value = row.getColumnObject(i);
                if (value != null) {
                    System.out.println("DBTools.setColumnValues() 2 Value: " + value.toString());
                    updated |= updateColumnValue(result, i, value);
                }
                else {
                    System.out.println("DBTools.setColumnValues() 3");
                    result.updateNull(i);
                }
            }
        }
        System.out.println("DBTools.setColumnValues() 4");
        return hasupdate && updated;
    }

    public static boolean updateColumnValue(ResultSet result,
                                            int index,
                                            Object value)
        throws SQLException
    {
        if (value != null) {
            int type = result.getMetaData().getColumnType(index);
            result.updateObject(index, value, JDBCType.valueOf(type));
        }
        else {
            result.updateNull(index);
        }
        return true;
    }

    public static void setStatementValue(PreparedStatement statement,
                                         int type,
                                         int index,
                                         Object value)
        throws SQLException
    {
        if (value != null) {
            statement.setObject(index, value, type);
        }
        else {
            statement.setNull(index, type);
        }
    }

    public static void setWhereParameter(PreparedStatement statement,
                                         RowCatalog catalog,
                                         RowTable table,
                                         Row row)
        throws SQLException
    {
        setWhereParameter(statement, catalog, table, row, 1);
    }

    public static void setWhereParameter(PreparedStatement statement,
                                         RowCatalog catalog,
                                         RowTable table,
                                         Row row,
                                         int offset)
        throws SQLException
    {
        int i = offset;
        for (int index : table.getKeyIndex()) {
            int type = catalog.getColumns()[index - 1].getType();
            Object value = row.getOldColumnObject(index);
            System.out.println("RowHelper.setWhereParameter() Value: " + value);
            RowHelper.setStatementValue(statement, type, i, value);
            i ++;
        }
    }

    public static Object getResultSetValue(ResultSet result,
                                           int index)
        throws SQLException
    {
        int type = result.getMetaData().getColumnType(index);
        return getResultSetValue(result, index, type);
    }

    public static Object getResultSetValue(ResultSet result,
                                           int index,
                                           int type)
        throws SQLException
    {
        Object value = null;
        switch (type) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                value = result.getString(index);
                break;
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                value = result.getNString(index);
                break;
            case Types.BIT:
            case Types.BOOLEAN:
                value = result.getObject(index, Boolean.class);
                break;
            case Types.TINYINT:
                System.out.println("RowHelper.getResultSetValue() 1");
                value = result.getObject(index, Byte.class);
                break;
            case Types.SMALLINT:
            case Types.INTEGER:
                value = result.getObject(index, Integer.class);
                break;
            case Types.BIGINT:
                value = result.getObject(index, Long.class);
                break;
            case Types.FLOAT:
                value = result.getObject(index, Double.class);
                break;
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                value = result.getBytes(index);
                break;
            case Types.CLOB:
                value = result.getObject(index, Clob.class);
                break;
            case Types.NCLOB:
                value = result.getObject(index, NClob.class);
                break;
            case Types.BLOB:
                value = result.getObject(index, Blob.class);
                break;
            case Types.ARRAY:
                value = result.getObject(index, Array.class);
                break;
            case Types.DATE:
                value = result.getObject(index, Date.class);
                break;
            case Types.TIME:
                value = result.getObject(index, Time.class);
                break;
            case Types.TIMESTAMP:
                value = result.getObject(index, Timestamp.class);
                break;
            case Types.TIME_WITH_TIMEZONE:
                value = result.getObject(index, OffsetTime.class);
                break;
            case Types.TIMESTAMP_WITH_TIMEZONE:
                value = result.getObject(index, OffsetDateTime.class);
                break;
            default:
                value = result.getObject(index);
        }
        return result.wasNull() ? null : value;
    }

    public static Object getDoubleValue(Double value,
                                        int type)
        throws SQLException
    {
        Object object;
        switch (type) {
            case Types.TINYINT:
                System.out.println("RowHelper.getDoubleValue() 1");
                object = value.byteValue();
                break;
            case Types.SMALLINT:
                object = value.shortValue();
                break;
            case Types.INTEGER:
                object = value.intValue();
                break;
            case Types.BIGINT:
                object = value.longValue();
                break;
            case Types.FLOAT:
                object = value.floatValue();
                break;
            default:
                object = value;
        }
        return object;
    }

    public static boolean isValidKeyType(int type)
    {
        boolean valid = false;
        switch (type) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.BOOLEAN:
            case Types.TINYINT:
            case Types.DOUBLE:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.FLOAT:
            case Types.REAL:
            case Types.DECIMAL:
            case Types.NUMERIC:
            case Types.ROWID:
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.TIME_WITH_TIMEZONE:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                valid = true;
                break;
            default:
                valid = false;
        }
        return valid;
    }

}
