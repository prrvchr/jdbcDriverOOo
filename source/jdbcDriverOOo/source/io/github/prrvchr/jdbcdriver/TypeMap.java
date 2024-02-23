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
package io.github.prrvchr.jdbcdriver;

import java.util.HashMap;
import java.util.Map;


public class TypeMap
{

    public static Map<String, Class<?>> getDefaultTypeMap()
    {
        final Map<String, Class<?>> typemap = new HashMap<>();
        typemap.put("ARRAY", java.sql.Array.class);
        typemap.put("BIGINT", Long.class);
        typemap.put("BINARY", byte[].class);
        typemap.put("BIT", Boolean.class);
        typemap.put("BLOB", java.sql.Blob.class);
        typemap.put("BOOLEAN", Boolean.class);
        typemap.put("CHAR", String.class);
        typemap.put("CLOB", java.sql.Clob.class);
        typemap.put("DATALINK", java.net.URL.class);
        typemap.put("DATE", java.sql.Date.class);
        typemap.put("DECIMAL", java.math.BigDecimal.class);
        typemap.put("DISTINCT", Object.class);
        typemap.put("DOUBLE", Double.class);
        typemap.put("FLOAT", Double.class);
        typemap.put("INTEGER", Integer.class);
        typemap.put("JAVA_OBJECT", Object.class);
        typemap.put("LONGNVARCHAR", String.class);
        typemap.put("LONGVARBINARY", byte[].class);
        typemap.put("LONGVARCHAR", String.class);
        typemap.put("NCHAR", String.class);
        typemap.put("NCLOB", java.sql.NClob.class);
        typemap.put("NULL", Void.class);
        typemap.put("NUMERIC", java.math.BigDecimal.class);
        typemap.put("NVARCHAR", String.class);
        typemap.put("OTHER", Object.class);
        typemap.put("REAL", Float.class);
        typemap.put("REF", java.sql.Ref.class);
        typemap.put("REF_CURSOR", java.lang.Object.class);
        typemap.put("ROWID", java.sql.RowId.class);
        typemap.put("SMALLINT", Integer.class);
        typemap.put("SQLXML", java.sql.SQLXML.class);
        typemap.put("STRUCT", java.sql.Struct.class);
        typemap.put("TIME", java.sql.Time.class);
        typemap.put("TIMESTAMP", java.sql.Timestamp.class);
        typemap.put("TIMESTAMP_WITH_TIMEZONE", java.time.OffsetDateTime.class);
        typemap.put("TIME_WITH_TIMEZONE", java.time.OffsetTime.class);
        typemap.put("TINYINT", Integer.class);
        typemap.put("VARBINARY", byte[].class);
        typemap.put("VARCHAR", String.class);
        return typemap;
        }

}
