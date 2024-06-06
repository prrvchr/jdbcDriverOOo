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

    public static int setColumnValues(ResultSet result,
                                      BaseRow row,
                                      int count,
                                      int insert)
        throws SQLException
    {
        return setColumnValues(result, row, count) ? insert : 0;
    }

    public static boolean setColumnValues(ResultSet result,
                                          BaseRow row,
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

    public static Object getResultSetValue(ResultSet result,
                                           int index)
        throws SQLException
    {
        Object value = null;
        int type = result.getMetaData().getColumnType(index);
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
            case Types.BOOLEAN:
                value = result.getObject(index, Boolean.class);
                break;
            case Types.TINYINT:
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

    public static boolean updateColumnValue(ResultSet result,
                                            int index,
                                            Object value)
        throws SQLException
    {
        if (value != null) {
            int type = result.getMetaData().getColumnType(index);
            result.updateObject(index, value, JDBCType.valueOf(type));
            /*switch (type) {
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                    result.updateString(index, (String) value);
                    break;
                case Types.NCHAR:
                case Types.NVARCHAR:
                case Types.LONGNVARCHAR:
                    result.updateNString(index, (String) value);
                    break;
                case Types.BOOLEAN:
                    result.updateBoolean(index, (Boolean) value);
                    break;
                case Types.TINYINT:
                case Types.SMALLINT:
                case Types.INTEGER:
                    result.updateInt(index, (Integer) value);
                    break;
                case Types.BIGINT:
                    result.updateLong(index, (Long) value);
                    break;
                case Types.FLOAT:
                    result.updateDouble(index, (Double) value);
                    break;
                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                    result.updateBytes(index, (byte[]) value);
                    break;
                case Types.CLOB:
                    result.updateClob(index, (Clob) value);
                    break;
                case Types.NCLOB:
                    result.updateNClob(index, (NClob) value);
                    break;
                case Types.BLOB:
                    result.updateBlob(index, (Blob) value);
                    break;
                case Types.ARRAY:
                    result.updateArray(index, (Array) value);
                    break;
                case Types.DATE:
                    result.updateDate(index, (Date) value);
                    break;
                case Types.TIME:
                    result.updateTime(index, (Time) value);
                    break;
                case Types.TIMESTAMP:
                    result.updateTimestamp(index, (Timestamp) value);
                    break;
                case Types.TIME_WITH_TIMEZONE:
                    result.updateObject(index, (OffsetTime) value);
                    break;
                case Types.TIMESTAMP_WITH_TIMEZONE:
                    result.updateObject(index, (OffsetDateTime) value);
                    break;
                default:
                    result.updateObject(index, value);
            }*/
        }
        else {
            result.updateNull(index);
        }
        return true;
    }


    public static void setRowValue(PreparedStatement statement,
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
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.FLOAT:
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
