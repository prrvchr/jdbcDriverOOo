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
package io.github.prrvchr.java.rowset.internal;

import java.math.BigDecimal;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import io.github.prrvchr.java.rowset.JdbcRowSetResourceBundle;

public class RowSetHelper {

    private static final String YES = "YES";

    /**
     * Indicates whether the given SQL data type is a numeric type.
     *
     * @param type one of the constants from <code>java.sql.Types</code>
     * @return <code>true</code> if the given type is <code>NUMERIC</code>,'
     *         <code>DECIMAL</code>, <code>BIT</code>, <code>TINYINT</code>,
     *         <code>SMALLINT</code>, <code>INTEGER</code>, <code>BIGINT</code>,
     *         <code>REAL</code>, <code>DOUBLE</code>, or <code>FLOAT</code>;
     *         <code>false</code> otherwise
     */
    public static boolean isNumeric(int type) {
        boolean is;
        switch (type) {
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.BIT:
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.REAL:
            case Types.DOUBLE:
            case Types.FLOAT:
                is = true;
                break;
            default:
                is = false;
        }
        return is;
    }

    /**
     * Indicates whether the given SQL data type is a string type.
     *
     * @param type one of the constants from <code>java.sql.Types</code>
     * @return <code>true</code> if the given type is <code>CHAR</code>,'
     *         <code>VARCHAR</code>, or <code>LONGVARCHAR</code>;
     *         <code>false</code> otherwise
     */
    public static boolean isString(int type) {
        boolean is = false;
        switch (type) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                is = true;
                break;
            default:
                is = false;
        }
        return is;
    }

    /**
     * Indicates whether the given SQL data type is a binary type.
     *
     * @param type one of the constants from <code>java.sql.Types</code>
     * @return <code>true</code> if the given type is <code>BINARY</code>,'
     *         <code>VARBINARY</code>, or <code>LONGVARBINARY</code>;
     *         <code>false</code> otherwise
     */
    public static boolean isBinary(int type) {
        boolean is;
        switch (type) {
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                is = true;
                break;
            default:
                is = false;
        }
        return is;
    }

    /**
     * Indicates whether the given SQL data type is a temporal type.
     * This method is called internally by the conversion methods
     * <code>convertNumeric</code> and <code>convertTemporal</code>.
     *
     * @param type one of the constants from <code>java.sql.Types</code>
     * @return <code>true</code> if the given type is <code>DATE</code>,
     *         <code>TIME</code>, or <code>TIMESTAMP</code>;
     *         <code>false</code> otherwise
     */
    public static boolean isTemporal(int type) {
        boolean is;
        switch (type) {
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                is = true;
                break;
            default:
                is = false;
        }
        return is;
    }

    /**
     * Indicates whether the given SQL data type is a boolean type.
     * This method is called internally by the conversion methods
     * <code>convertNumeric</code> and <code>convertBoolean</code>.
     *
     * @param type one of the constants from <code>java.sql.Types</code>
     * @return <code>true</code> if the given type is <code>BIT</code>,
     *         , or <code>BOOLEAN</code>;
     *         <code>false</code> otherwise
     */
    public static boolean isBoolean(int type) {
        boolean is;
        switch (type) {
            case Types.BIT:
            case Types.BOOLEAN:
                is = true;
                break;
            default:
                is = false;
        }
        return is;
    }

    /**
     * Converts the given <code>Object</code> in the Java programming language
     * to the standard mapping for the specified SQL target data type.
     * The conversion must be to a string or numeric type, but there are no
     * restrictions on the type to be converted.  If the source type and target
     * type are the same, the given object is simply returned.
     *
     * @param resBundle the <code>JdbcRowSetResourceBundle</code> object
     *
     * @param srcObj the <code>Object</code> in the Java programming language
     *               that is to be converted to the target type
     * @param srcType the data type that is the standard mapping in SQL of the
     *                object to be converted; must be one of the constants in
     *                <code>java.sql.Types</code>
     * @param trgType the SQL data type to which to convert the given object;
     *                must be one of the following constants in
     *                <code>java.sql.Types</code>: <code>NUMERIC</code>,
     *         <code>DECIMAL</code>, <code>BIT</code>, <code>TINYINT</code>,
     *         <code>SMALLINT</code>, <code>INTEGER</code>, <code>BIGINT</code>,
     *         <code>REAL</code>, <code>DOUBLE</code>, <code>FLOAT</code>,
     *         <code>VARCHAR</code>, <code>LONGVARCHAR</code>, or <code>CHAR</code>
     * @return an <code>Object</code> value.that is
     *         the standard object mapping for the target SQL type
     * @throws SQLException if the given target type is not one of the string or
     *         numeric types in <code>java.sql.Types</code>
     */
    public static Object convertNumeric(JdbcRowSetResourceBundle resBundle,
                                        Object srcObj, int srcType, int trgType)
        throws SQLException {
        Object value;
        if (srcType == trgType) {
            value = srcObj;
        } else {

            if (!isNumeric(trgType) && !isString(trgType)) {
                String msg = resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString();
                throw new SQLException(msg + trgType);
            }
            try {
                switch (trgType) {
                    case Types.BIT:
                        Integer i;
                        if (srcType == Types.DOUBLE) {
                            i = ((Double) srcObj).intValue();
                        } else if (srcType == Types.FLOAT ||
                                   srcType == Types.REAL) {
                            i = ((Float) srcObj).intValue();
                        } else {
                            i = Integer.valueOf(srcObj.toString().trim());
                        }
                        if (i.equals(0)) {
                            value = Boolean.valueOf(false);
                        } else {
                            value = Boolean.valueOf(true);
                        }
                        break;
                    case Types.TINYINT:
                        if (srcType == Types.DOUBLE) {
                            value = ((Double) srcObj).byteValue();
                        } else if (srcType == Types.FLOAT ||
                                   srcType == Types.REAL) {
                            value = ((Float) srcObj).byteValue();
                        } else {
                            value = Byte.valueOf(srcObj.toString().trim());
                        }
                        break;
                    case Types.SMALLINT:
                        if (srcType == Types.DOUBLE) {
                            value = ((Double) srcObj).shortValue();
                        } else if (srcType == Types.FLOAT ||
                                   srcType == Types.REAL) {
                            value = ((Float) srcObj).shortValue();
                        } else {
                            value = Short.valueOf(srcObj.toString().trim());
                        }
                        break;
                    case Types.INTEGER:
                        if (srcType == Types.DOUBLE) {
                            value = ((Double) srcObj).intValue();
                        } else if (srcType == Types.FLOAT ||
                                   srcType == Types.REAL) {
                            value = ((Float) srcObj).intValue();
                        } else {
                            value = Integer.valueOf(srcObj.toString().trim());
                        }
                        break;
                    case Types.BIGINT:
                        if (srcType == Types.DOUBLE) {
                            value = ((Double) srcObj).longValue();
                        } else if (srcType == Types.FLOAT ||
                                   srcType == Types.REAL) {
                            value = ((Float) srcObj).longValue();
                        } else {
                            value = Long.valueOf(srcObj.toString().trim());
                        }
                        break;
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        value = new BigDecimal(srcObj.toString().trim());
                        break;
                    case Types.REAL:
                    case Types.FLOAT:
                        value = Float.valueOf(srcObj.toString().trim());
                        break;
                    case Types.DOUBLE:
                        value = Double.valueOf(srcObj.toString().trim());
                        break;
                    case Types.CHAR:
                    case Types.VARCHAR:
                    case Types.LONGVARCHAR:
                        value = srcObj.toString();
                        break;
                    default:
                        String msg = resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString();
                        throw new SQLException(msg + trgType);
                }
            } catch (NumberFormatException ex) {
                String msg = resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString();
                throw new SQLException(msg + trgType);
            }
        }
        return value;
    }

    /**
     * Converts the given <code>Object</code> in the Java programming language
     * to the standard object mapping for the specified SQL target data type.
     * The conversion must be to a string or temporal type, and there are also
     * restrictions on the type to be converted.
     * <P>
     * <TABLE ALIGN="CENTER" BORDER CELLPADDING=10 BORDERCOLOR="#0000FF">
     * <CAPTION ALIGN="CENTER"><B>Parameters and Return Values</B></CAPTION>
     * <TR>
     *   <TD><B>Source SQL Type</B>
     *   <TD><B>Target SQL Type</B>
     *   <TD><B>Object Returned</B>
     * </TR>
     * <TR>
     *   <TD><code>TIMESTAMP</code>
     *   <TD><code>DATE</code>
     *   <TD><code>java.sql.Date</code>
     * </TR>
     * <TR>
     *   <TD><code>TIMESTAMP</code>
     *   <TD><code>TIME</code>
     *   <TD><code>java.sql.Time</code>
     * </TR>
     * <TR>
     *   <TD><code>TIME</code>
     *   <TD><code>TIMESTAMP</code>
     *   <TD><code>java.sql.Timestamp</code>
     * </TR>
     * <TR>
     *   <TD><code>DATE</code>, <code>TIME</code>, or <code>TIMESTAMP</code>
     *   <TD><code>CHAR</code>, <code>VARCHAR</code>, or <code>LONGVARCHAR</code>
     *   <TD><code>java.lang.String</code>
     * </TR>
     * </TABLE>
     * <P>
     * If the source type and target type are the same,
     * the given object is simply returned.
     *
     * @param resBundle the <code>JdbcRowSetResourceBundle</code> object
     *
     * @param srcObj the <code>Object</code> in the Java programming language
     *               that is to be converted to the target type
     * @param srcType the data type that is the standard mapping in SQL of the
     *                object to be converted; must be one of the constants in
     *                <code>java.sql.Types</code>
     * @param trgType the SQL data type to which to convert the given object;
     *                must be one of the following constants in
     *                <code>java.sql.Types</code>: <code>DATE</code>,
     *         <code>TIME</code>, <code>TIMESTAMP</code>, <code>CHAR</code>,
     *         <code>VARCHAR</code>, or <code>LONGVARCHAR</code>
     * @return an <code>Object</code> value.that is
     *         the standard object mapping for the target SQL type
     * @throws SQLException if the given target type is not one of the string or
     *         temporal types in <code>java.sql.Types</code>
     */
    public static Object convertTemporal(JdbcRowSetResourceBundle resBundle,
                                         Object srcObj, int srcType, int trgType)
        throws SQLException {
        Object value;
        if (srcType == trgType) {
            value = srcObj;
        } else {

            if (isNumeric(trgType) || !isString(trgType) && !isTemporal(trgType)) {
                String msg = resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString();
                throw new SQLException(msg);
            }

            try {
                switch (trgType) {
                    case Types.DATE:
                        if (srcType == Types.TIMESTAMP) {
                            value = new java.sql.Date(((java.sql.Timestamp) srcObj).getTime());
                        } else {
                            String msg = resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString();
                            throw new SQLException(msg);
                        }
                        break;
                    case Types.TIMESTAMP:
                        if (srcType == Types.TIME) {
                            value = new Timestamp(((java.sql.Time) srcObj).getTime());
                        } else {
                            value = new Timestamp(((java.sql.Date) srcObj).getTime());
                        }
                        break;
                    case Types.TIME:
                        if (srcType == Types.TIMESTAMP) {
                            value = new Time(((java.sql.Timestamp)srcObj).getTime());
                        } else {
                            String msg = resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString();
                            throw new SQLException(msg);
                        }
                        break;
                    case Types.CHAR:
                    case Types.VARCHAR:
                    case Types.LONGVARCHAR:
                        value = srcObj.toString();
                        break;
                    default:
                        String msg = resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString();
                        throw new SQLException(msg);
                }
            } catch (NumberFormatException ex) {
                String msg = resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString();
                throw new SQLException(msg);
            }
        }
        return value;
    }

    /**
     * Converts the given <code>Object</code> in the Java programming language
     * to the standard mapping for the specified SQL target data type.
     * The conversion must be to a string or numeric type, but there are no
     * restrictions on the type to be converted.  If the source type and target
     * type are the same, the given object is simply returned.
     *
     * @param resBundle the <code>JdbcRowSetResourceBundle</code> object
     *
     * @param srcObj the <code>Object</code> in the Java programming language
     *               that is to be converted to the target type
     * @param srcType the data type that is the standard mapping in SQL of the
     *                object to be converted; must be one of the constants in
     *                <code>java.sql.Types</code>
     * @param trgType the SQL data type to which to convert the given object;
     *                must be one of the following constants in
     *                <code>java.sql.Types</code>: <code>BIT</code>,
     *         or <code>BOOLEAN</code>
     * @return an <code>Object</code> value.that is
     *         the standard object mapping for the target SQL type
     * @throws SQLException if the given target type is not one of the Boolean
     *         types in <code>java.sql.Types</code>
     */
    public static Object convertBoolean(JdbcRowSetResourceBundle resBundle,
                                        Object srcObj, int srcType, int trgType)
        throws SQLException {

        Object value;
        if (srcType == trgType) {
            value = srcObj;
        } else {

            if (isNumeric(trgType) || !isString(trgType) && !isBoolean(trgType)) {
                throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString());
            }

            try {
                switch (trgType) {
                    case Types.BIT:
                        Integer i = Integer.valueOf(srcObj.toString().trim());
                        if (i.equals(0)) {
                            value = Boolean.valueOf(false);
                        } else {
                            value = Boolean.valueOf(true);
                        }
                        break;
                    case Types.BOOLEAN:
                        value = Boolean.valueOf(srcObj.toString().trim());
                        break;
                    default:
                        String msg = resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString();
                        throw new SQLException(msg + trgType);
                }
            } catch (NumberFormatException ex) {
                String msg = resBundle.handleGetObject("cachedrowsetimpl.dtypemismt").toString();
                throw new SQLException(msg + trgType);
            }
        }
        return value;
    }

    public static boolean isSimilarType(int type1, int type2) {
        return isNumericType(type1, type2) || isStringType(type1, type2) || isBooleanType(type1, type2);
    }

    public static final boolean hasPrimaryKeys(DatabaseMetaData dbmd, String catalog, String schema, String table)
        throws SQLException {
        boolean has = false;
        try (ResultSet rs = dbmd.getPrimaryKeys(catalog, schema, table)) {
            if (rs.next()) {
                has = true;
            }
        }
        return has;
    }

    public static final List<String> getAutoIncrementColumns(DatabaseMetaData dbmd, NamedComponent table)
        throws SQLException {
        List<String> autos = new ArrayList<>();
        String column, value;
        final int COLUMN_NAME = 4;
        final int IS_AUTOINCREMENT = 23;
        try (ResultSet rs = dbmd.getColumns(table.getCatalog(), table.getSchema(), table.getName(), "%")) {
            while (rs.next()) {
                column = rs.getString(COLUMN_NAME);
                value = rs.getString(IS_AUTOINCREMENT);
                if (!rs.wasNull() && YES.equals(value)) {
                    autos.add(column);
                }
            }
        }
        return autos;
    }

    private static boolean isNumericType(int type1, int type2) {
        return isNumeric(type1) && isNumeric(type2);
    }

    private static boolean isStringType(int type1, int type2) {
        return isString(type1) && isString(type2);
    }

    private static boolean isBooleanType(int type1, int type2) {
        return isBoolean(type1) && isBoolean(type2);
    }

}
