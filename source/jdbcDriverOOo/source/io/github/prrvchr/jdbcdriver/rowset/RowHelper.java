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
/**************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 *************************************************************/
package io.github.prrvchr.jdbcdriver.rowset;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.BitSet;

import com.sun.star.sdbc.SQLException;


public class RowHelper
{

    public static void setDefaultColumnValues(java.sql.ResultSet result,
                                              BitSet column)
        throws java.sql.SQLException
    {
        // XXX: If we want to succeed, we need to set to NULL all auto-increment
        // XXX: and nullable columns that have not been updated
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

    public static int setColumnValues(java.sql.ResultSet result,
                                      BaseRow row,
                                      int count,
                                      int insert)
        throws SQLException, java.sql.SQLException
    {
        return setColumnValues(result, row, count) ? insert : 0;
    }

    public static boolean setColumnValues(java.sql.ResultSet result,
                                          BaseRow row,
                                          int count)
        throws SQLException, java.sql.SQLException
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

    public static void setColumnValue(Object[] values, ResultSet result, int index)
        throws java.sql.SQLException
    {
        Object value = null;
        switch (result.getMetaData().getColumnType(index)) {
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
        default:
            value = result.getObject(index);
        }
        values[index - 1] = result.wasNull() ? null : value;
    }

    public static boolean updateColumnValue(ResultSet result, int index, Object value)
        throws java.sql.SQLException
    {
        switch (result.getMetaData().getColumnType(index)) {
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
        default:
            result.updateObject(index, value);
        }
        return true;
    }


}
