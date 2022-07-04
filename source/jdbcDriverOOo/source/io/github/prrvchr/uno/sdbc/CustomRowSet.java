/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020 https://prrvchr.github.io                                     ║
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
package io.github.prrvchr.uno.sdbc;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import com.sun.star.io.IOException;
import com.sun.star.io.XInputStream;
import com.sun.star.sdbc.DataType;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XBlob;
import com.sun.star.sdbc.XClob;
import com.sun.star.uno.Any;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Type;
import com.sun.star.uno.TypeClass;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.Date;
import com.sun.star.util.DateTime;
import com.sun.star.util.Time;


public class CustomRowSet{

    private Object m_value;
    private int m_type;
    private int m_flags;
    private static final int FLAG_NULL = 0b1000;
    private static final int FLAG_BOUND = 0b0100;
    private static final int FLAG_MODIFIED = 0b0010;
    private static final int FLAG_SIGNED = 0b0001;

    // The constructor method:
    public CustomRowSet()
    {
        m_flags = FLAG_NULL | FLAG_BOUND | FLAG_SIGNED;
        m_type = DataType.VARCHAR;
    }
    public CustomRowSet(boolean value) {
        this();
        setBoolean(value);
    }
    public CustomRowSet(Date value) {
        this();
        setDate(value);
    }
    public CustomRowSet(DateTime value) {
        this();
        setDateTime(value);
    }
    public CustomRowSet(double value) {
        this();
        setDouble(value);
    }
    public CustomRowSet(float value) {
        this();
        setFloat(value);
    }
    public CustomRowSet(byte value) {
        this();
        setInt8(value);
    }
    public CustomRowSet(short value) {
        this();
        setInt16(value);
    }
    public CustomRowSet(int value) {
        this();
        setInt32(value);
    }
    public CustomRowSet(long value) {
        this();
        setLong(value);
    }
    public CustomRowSet(byte[] value) {
        this();
        setSequence(value);
    }
    public CustomRowSet(String value,
                        boolean isnull)
    {
        this();
        if (isnull) {
            setNull();
        }
        else {
            setString(value);
        }
    }

    public CustomRowSet(Time value) {
        this();
        setTime(value);
    }

    public boolean isNull() {
        return (m_flags & FLAG_NULL) != 0;
    }
  
    public void setNull() {
        free();
        m_flags |= FLAG_NULL;
    }
  
    public boolean isBound() {
        return (m_flags & FLAG_BOUND) != 0;
    }
  
    public void setBound(boolean isBound) {
        if (isBound) {
            m_flags |= FLAG_BOUND;
        } else {
            m_flags &= ~FLAG_BOUND;
        }
    }
  
    public boolean isModified() {
        return (m_flags & FLAG_MODIFIED) != 0;
    }
  
    public void setModified(boolean isModified) {
        m_flags |= FLAG_MODIFIED;
    }
  
    public boolean isSigned() {
        return (m_flags & FLAG_SIGNED) != 0;
    }

    public void setSigned() throws IOException, SQLException {
        setSigned(true);
    }

    public void setSigned(boolean isSigned) {
        if (isSigned) {
            m_flags |= FLAG_SIGNED;
        } else {
            m_flags &= ~FLAG_SIGNED;
        }
    }

    private boolean isStorageCompatible(int _eType1, int _eType2) {
        boolean bIsCompatible = true;

        if (_eType1 != _eType2) {
            switch (_eType1) {
            case DataType.CHAR:
            case DataType.VARCHAR:
            case DataType.DECIMAL:
            case DataType.NUMERIC:
            case DataType.LONGVARCHAR:
                bIsCompatible = (DataType.CHAR         == _eType2)
                            ||  (DataType.VARCHAR      == _eType2)
                            ||  (DataType.DECIMAL      == _eType2)
                            ||  (DataType.NUMERIC      == _eType2)
                            ||  (DataType.LONGVARCHAR  == _eType2);
                break;

            case DataType.DOUBLE:
            case DataType.REAL:
                bIsCompatible = (DataType.DOUBLE   == _eType2)
                            ||  (DataType.REAL     == _eType2);
                break;

            case DataType.BINARY:
            case DataType.VARBINARY:
            case DataType.LONGVARBINARY:
                bIsCompatible = (DataType.BINARY           == _eType2)
                            ||  (DataType.VARBINARY        == _eType2)
                            ||  (DataType.LONGVARBINARY    == _eType2);
                break;

            case DataType.INTEGER:
                bIsCompatible = (DataType.SMALLINT == _eType2)
                            ||  (DataType.TINYINT  == _eType2)
                            ||  (DataType.BIT      == _eType2)
                            ||  (DataType.BOOLEAN  == _eType2);
                break;
            case DataType.SMALLINT:
                bIsCompatible = (DataType.TINYINT  == _eType2)
                            ||  (DataType.BIT      == _eType2)
                            ||  (DataType.BOOLEAN  == _eType2);
                break;
            case DataType.TINYINT:
                bIsCompatible = (DataType.BIT      == _eType2)
                            ||  (DataType.BOOLEAN  == _eType2);
                break;

            case DataType.BLOB:
            case DataType.CLOB:
            case DataType.OBJECT:
                bIsCompatible = (DataType.BLOB     == _eType2)
                            ||  (DataType.CLOB     == _eType2)
                            ||  (DataType.OBJECT   == _eType2);
                break;

            default:
                bIsCompatible = false;
            }
        }
        return bIsCompatible;
    }
  
    public int getTypeKind() {
        return m_type;
    }
  
    public void setTypeKind(int type) throws SQLException {
        if (!isNull() && !isStorageCompatible(type, m_type)) {
            switch (type) {
            case DataType.VARCHAR:
            case DataType.CHAR:
            case DataType.DECIMAL:
            case DataType.NUMERIC:
            case DataType.LONGVARCHAR:
                setString(getString());
                break;
            case DataType.BIGINT:
                setLong(getLong());
                break;

            case DataType.FLOAT:
                setFloat(getFloat());
                break;
            case DataType.DOUBLE:
            case DataType.REAL:
                setDouble(getDouble());
                break;
            case DataType.TINYINT:
                setInt8(getInt8());
                break;
            case DataType.SMALLINT:
                setInt16(getInt16());
                break;
            case DataType.INTEGER:
                setInt32(getInt32());
                break;
            case DataType.BIT:
            case DataType.BOOLEAN:
                setBoolean(getBoolean());
                break;
            case DataType.DATE:
                setDate(getDate());
                break;
            case DataType.TIME:
                setTime(getTime());
                break;
            case DataType.TIMESTAMP:
                setDateTime(getDateTime());
                break;
            case DataType.BINARY:
            case DataType.VARBINARY:
            case DataType.LONGVARBINARY:
                setSequence(getSequence());
                break;
            case DataType.BLOB:
            case DataType.CLOB:
            case DataType.OBJECT:
            case DataType.OTHER:
                setAny(getAny());
                break;
            default:
                setAny(getAny());
                //OSL_ENSURE(0,"RowSetValue:operator==(): UNSPUPPORTED TYPE!");
            }
        }
        m_type = type;
    }

    private void free() {
        if (!isNull()) {
            m_value = null;
            m_flags |= FLAG_NULL;
        }
    }

    public void fill(Object any) {
        final Type type = AnyConverter.getType(any);
        
        switch (type.getTypeClass().getValue()) {
        case TypeClass.VOID_value:
            setNull();
            break;
        case TypeClass.BOOLEAN_value: {
            boolean value = false;
            try {
                value = AnyConverter.toBoolean(any);
            } catch (IllegalArgumentException illegalArgumentException) {
            }
            setBoolean(value);
            break;
        }
        case TypeClass.CHAR_value: {
            char value = 0;
            try {
                value = AnyConverter.toChar(any);
            } catch (IllegalArgumentException illegalArgumentException) {
            }
            setString(Character.toString(value));
            break;
        }
        case TypeClass.STRING_value: {
            String value = "";
            try {
                value = AnyConverter.toString(any);
            } catch (IllegalArgumentException illegalArgumentException) {
            }
            setString(value);
            break;
        }
        case TypeClass.FLOAT_value: {
            float value = 0.0f;
            try {
                value = AnyConverter.toFloat(any);
            } catch (IllegalArgumentException illegalArgumentException) {
            }
            setFloat(value);
            break;
        }
        case TypeClass.DOUBLE_value: {
            double value = 0.0;
            try {
                value = AnyConverter.toDouble(any);
            } catch (IllegalArgumentException illegalArgumentException) {
            }
            setDouble(value);
            break;
        }
        case TypeClass.BYTE_value: {
            byte value = 0;
            try {
                value = AnyConverter.toByte(any);
            } catch (IllegalArgumentException illegalArgumentException) {
            }
            setInt8(value);
            break;
        }
        case TypeClass.SHORT_value: {
            short value = 0;
            try {
                AnyConverter.toShort(any);
            } catch (IllegalArgumentException illegalArgumentException) {
            }
            setInt16(value);
            break;
        }
        case TypeClass.UNSIGNED_SHORT_value: {
            short value = 0;
            try {
                AnyConverter.toUnsignedShort(any);
            } catch (IllegalArgumentException illegalArgumentException) {
            }
            setInt16(value);
            setSigned(false);
            break;
        }
        case TypeClass.LONG_value: {
            int value = 0;
            try {
                value = AnyConverter.toInt(any);
            } catch (IllegalArgumentException illegalArgumentException) {
            }
            setInt32(value);
            break;
        }
        case TypeClass.UNSIGNED_LONG_value: {
            int value = 0;
            try {
                value = AnyConverter.toUnsignedInt(any);
            } catch (IllegalArgumentException illegalArgumentException) {
            }
            setInt32(value);
            setSigned(false);
            break;
        }
        case TypeClass.HYPER_value: {
            long value = 0;
            try {
                value = AnyConverter.toLong(any);
            } catch (IllegalArgumentException illegalArgumentException) {
            }
            setLong(value);
            break;
        }
        case TypeClass.UNSIGNED_HYPER_value: {
            long value = 0;
            try {
                value = AnyConverter.toUnsignedLong(any);
            } catch (IllegalArgumentException illegalArgumentException) {
            }
            setLong(value);
            setSigned(false);
            break;
        }
        case TypeClass.ENUM_value: {
            // FIXME: is this how an enum is unboxed from Any?
            int value = 0;
            try {
                Object object = AnyConverter.toObject(type, any);
                if (object instanceof com.sun.star.uno.Enum) {
                    value = ((com.sun.star.uno.Enum)object).getValue();
                }
            } catch (IllegalArgumentException illegalArgumentException) {
            }
            setInt32(value);
            break;
        }
        case TypeClass.SEQUENCE_value: {
            byte[] value = new byte[0];
            try {
                Object array = AnyConverter.toArray(value);
                if (array instanceof byte[]) {
                    value = (byte[]) array;
                }
            } catch (IllegalArgumentException illegalArgumentException) {
            }
            setSequence(value);
            break;
        }
        case TypeClass.STRUCT_value:
        case TypeClass.INTERFACE_value: {
            try {
                Object object = AnyConverter.toObject(Object.class, any);
                if (object instanceof Date) {
                    setDate((Date)object);
                } else if (object instanceof Time) {
                    setTime((Time)object);
                } else if (object instanceof DateTime) {
                    setDateTime((DateTime)object);
                } else {
                    XClob clob = UnoRuntime.queryInterface(XClob.class, object);
                    if (clob != null) {
                        setAny(clob);
                    } else {
                        XBlob blob = UnoRuntime.queryInterface(XBlob.class, object);
                        if (blob != null) {
                            setAny(blob);
                        }
                    }
                }
            } catch (IllegalArgumentException illegalArgumentException) {
            }
            break;
        }
        default:
            // unknown type
        }
    }

    public Object getAny() {
        return m_value;
    }

    public boolean getBoolean() {
        boolean bRet = false;
        if (!isNull()) {
            switch (getTypeKind()) {
            case DataType.CHAR:
            case DataType.VARCHAR:
            case DataType.LONGVARCHAR:
                if (((String)m_value).equals("true")) {
                    bRet = true;
                } else if (((String)m_value).equals("false")) {
                    bRet = false;
                }
                // fall through
            case DataType.DECIMAL:
            case DataType.NUMERIC:
                bRet = CustomRowTypeConversion.safeParseInt((String)m_value) != 0;
                break;
            case DataType.BIGINT:
                bRet = (long)m_value != 0;
                break;
            case DataType.FLOAT:
                bRet = (float)m_value != 0.0;
                break;
            case DataType.DOUBLE:
            case DataType.REAL:
                bRet = (double)m_value != 0.0;
                break;
            case DataType.DATE:
            case DataType.TIME:
            case DataType.TIMESTAMP:
            case DataType.BINARY:
            case DataType.VARBINARY:
            case DataType.LONGVARBINARY:
                break;
            case DataType.BIT:
            case DataType.BOOLEAN:
                bRet = (boolean)m_value;
                break;
            case DataType.TINYINT:
                bRet = (byte)m_value != 0;
                break;
            case DataType.SMALLINT:
                bRet = (short)m_value != 0;
                break;
            case DataType.INTEGER:
                bRet = (int)m_value != 0;
                break;
            default:
                try {
                    bRet = AnyConverter.toBoolean(m_value);
                } catch (com.sun.star.lang.IllegalArgumentException e) {
                }
                break;
            }
        }
        return bRet;
    }

    public Date getDate() throws SQLException {
        Date aValue = new Date();
        if (!isNull()) {
            switch (getTypeKind()) {
            case DataType.CHAR:
            case DataType.VARCHAR:
            case DataType.LONGVARCHAR:
                aValue = CustomRowTypeConversion.toDate(getString());
                break;
            case DataType.DECIMAL:
            case DataType.NUMERIC:
            case DataType.FLOAT:
            case DataType.DOUBLE:
            case DataType.REAL:
                aValue = CustomRowTypeConversion.toDate(getDouble());
                break;
            case DataType.DATE:
                Date date    = (Date)m_value;
                aValue.Day   = date.Day;
                aValue.Month = date.Month;
                aValue.Year  = date.Year;
                break;
            case DataType.TIMESTAMP:
                DateTime dateTime = (DateTime)m_value;
                aValue.Day        = dateTime.Day;
                aValue.Month      = dateTime.Month;
                aValue.Year       = dateTime.Year;
                break;
            case DataType.BIT:
            case DataType.BOOLEAN:
            case DataType.TINYINT:
            case DataType.SMALLINT:
            case DataType.INTEGER:
            case DataType.BIGINT:
                aValue = CustomRowTypeConversion.toDate((double)getLong());
                break;

            case DataType.BLOB:
            case DataType.CLOB:
            case DataType.OBJECT:
            default:
                //OSL_ENSURE( false, "RowSetValue::getDate: cannot retrieve the data!" );
                // NO break!

            case DataType.BINARY:
            case DataType.VARBINARY:
            case DataType.LONGVARBINARY:
            case DataType.TIME:
                aValue = CustomRowTypeConversion.toDate(0.0);
                break;
            }
        }
        return aValue;

    }

    public DateTime getDateTime() throws SQLException {
        DateTime aValue = new DateTime();
        if (!isNull()) {
            switch (getTypeKind()) {
            case DataType.CHAR:
            case DataType.VARCHAR:
            case DataType.LONGVARCHAR:
                aValue = CustomRowTypeConversion.toDateTime(getString());
                break;
            case DataType.DECIMAL:
            case DataType.NUMERIC:
            case DataType.FLOAT:
            case DataType.DOUBLE:
            case DataType.REAL:
                aValue = CustomRowTypeConversion.toDateTime(getDouble());
                break;
            case DataType.DATE:
                Date date       = (Date)m_value;
                aValue.Day      = date.Day;
                aValue.Month    = date.Month;
                aValue.Year     = date.Year;
                break;
            case DataType.TIME:
                Time time               = (Time)m_value;
                aValue.NanoSeconds      = time.NanoSeconds;
                aValue.Seconds          = time.Seconds;
                aValue.Minutes          = time.Minutes;
                aValue.Hours            = time.Hours;
                break;
            case DataType.TIMESTAMP:
                DateTime dateTime       = (DateTime)m_value;
                aValue.Year             = dateTime.Year;
                aValue.Month            = dateTime.Month;
                aValue.Day              = dateTime.Day;
                aValue.Hours            = dateTime.Hours;
                aValue.Minutes          = dateTime.Minutes;
                aValue.Seconds          = dateTime.Seconds;
                aValue.NanoSeconds      = dateTime.NanoSeconds;
                break;
            default:
                try {
                    DateTime any            = (DateTime) AnyConverter.toObject(DateTime.class, m_value);
                    aValue.Year             = any.Year;
                    aValue.Month            = any.Month;
                    aValue.Day              = any.Day;
                    aValue.Hours            = any.Hours;
                    aValue.Minutes          = any.Minutes;
                    aValue.Seconds          = any.Seconds;
                    aValue.NanoSeconds      = any.NanoSeconds;
                } catch (com.sun.star.lang.IllegalArgumentException e) {
                } catch (ClassCastException classCastException) {
                }
                break;
            }
        }
        return aValue;
    }

    public double getDouble() {
        double nRet = 0.0;
        if (!isNull()) {
            switch (getTypeKind()) {
            case DataType.CHAR:
            case DataType.VARCHAR:
            case DataType.DECIMAL:
            case DataType.NUMERIC:
            case DataType.LONGVARCHAR:
                nRet = CustomRowTypeConversion.safeParseDouble((String)m_value);
                break;
            case DataType.BIGINT:
                nRet = isSigned() ? (long)m_value : CustomRowTypeConversion.unsignedLongToDouble((long)m_value);
                break;
            case DataType.FLOAT:
                nRet = (float)m_value;
                break;
            case DataType.DOUBLE:
            case DataType.REAL:
                nRet = (double)m_value;
                break;
            case DataType.DATE:
                nRet = CustomRowTypeConversion.toDouble((Date)m_value);
                break;
            case DataType.TIME:
                nRet = CustomRowTypeConversion.toDouble((Time)m_value);
                break;
            case DataType.TIMESTAMP:
                nRet = CustomRowTypeConversion.toDouble((DateTime)m_value);
                break;
            case DataType.BINARY:
            case DataType.VARBINARY:
            case DataType.LONGVARBINARY:
            case DataType.BLOB:
            case DataType.CLOB:
                //OSL_ASSERT(!"getDouble() for this type is not allowed!");
                break;
            case DataType.BIT:
            case DataType.BOOLEAN:
                nRet = (boolean)m_value ? 1 : 0;
                break;
            case DataType.TINYINT:
                nRet = isSigned() ? (byte)m_value : 0xff & (byte)m_value;
                break;
            case DataType.SMALLINT:
                nRet = isSigned() ? (short)m_value : 0xffff & (short)m_value;
                break;
            case DataType.INTEGER:
                nRet = isSigned() ? (int)m_value : 0xffffFFFFL & (int)m_value;
                break;
            default:
                try {
                    nRet = AnyConverter.toDouble(m_value);
                } catch (com.sun.star.lang.IllegalArgumentException e) {
                }
                break;
            }
        }
        return nRet;
    }

    public float getFloat() {
        float nRet = 0.0f;
        if (!isNull()) {
            switch (getTypeKind()) {
            case DataType.CHAR:
            case DataType.VARCHAR:
            case DataType.DECIMAL:
            case DataType.NUMERIC:
            case DataType.LONGVARCHAR:
                nRet = CustomRowTypeConversion.safeParseFloat((String)m_value);
                break;
            case DataType.BIGINT:
                nRet = isSigned() ? (long)m_value : CustomRowTypeConversion.unsignedLongToFloat((long)m_value);
                break;
            case DataType.FLOAT:
                nRet = (float)m_value;
                break;
            case DataType.DOUBLE:
            case DataType.REAL:
                nRet = (float)(double)m_value;
                break;
            case DataType.DATE:
                nRet = (float)CustomRowTypeConversion.toDouble((Date)m_value);
                break;
            case DataType.TIME:
                nRet = (float)CustomRowTypeConversion.toDouble((Time)m_value);
                break;
            case DataType.TIMESTAMP:
                nRet = (float)CustomRowTypeConversion.toDouble((DateTime)m_value);
                break;
            case DataType.BINARY:
            case DataType.VARBINARY:
            case DataType.LONGVARBINARY:
            case DataType.BLOB:
            case DataType.CLOB:
                //OSL_ASSERT(!"getDouble() for this type is not allowed!");
                break;
            case DataType.BIT:
            case DataType.BOOLEAN:
                nRet = (boolean)m_value ? 1 : 0;
                break;
            case DataType.TINYINT:
                nRet = isSigned() ? (byte)m_value : 0xff & (byte)m_value;
                break;
            case DataType.SMALLINT:
                nRet = isSigned() ? (short)m_value : 0xffff & (short)m_value;
                break;
            case DataType.INTEGER:
                nRet = isSigned() ? (int)m_value : 0xffffFFFFL & (int)m_value;
                break;
            default:
                try {
                    nRet = AnyConverter.toFloat(m_value);
                } catch (com.sun.star.lang.IllegalArgumentException e) {
                }
                break;
            }
        }
        return nRet;
    }

    public byte getInt8() {
        byte nRet = 0;
        if (!isNull()) {
            switch (getTypeKind()) {
            case DataType.CHAR:
            case DataType.VARCHAR:
            case DataType.DECIMAL:
            case DataType.NUMERIC:
            case DataType.LONGVARCHAR:
                nRet = (byte)CustomRowTypeConversion.safeParseInt((String)m_value);
                break;
            case DataType.BIGINT:
                nRet = (byte)(long)m_value;
                break;
            case DataType.FLOAT:
                nRet = (byte)(float)m_value;
                break;
            case DataType.DOUBLE:
            case DataType.REAL:
                nRet = (byte)(double)m_value;
                break;
            case DataType.DATE:
            case DataType.TIME:
            case DataType.TIMESTAMP:
            case DataType.BINARY:
            case DataType.VARBINARY:
            case DataType.LONGVARBINARY:
            case DataType.BLOB:
            case DataType.CLOB:
                break;
            case DataType.BIT:
            case DataType.BOOLEAN:
                nRet = (byte)((boolean)m_value ? 1 : 0);
                break;
            case DataType.TINYINT:
                nRet = (byte)m_value;
                break;
            case DataType.SMALLINT:
                nRet = (byte)(short)m_value;
                break;
            case DataType.INTEGER:
                nRet = (byte)(int)m_value;
                break;
            default:
                try {
                    nRet = AnyConverter.toByte(m_value);
                } catch (com.sun.star.lang.IllegalArgumentException e) {
                }
                break;
            }
        }
        return nRet;
    }

    public short getInt16() {
        short nRet = 0;
        if (!isNull()) {
            switch (getTypeKind()) {
            case DataType.CHAR:
            case DataType.VARCHAR:
            case DataType.DECIMAL:
            case DataType.NUMERIC:
            case DataType.LONGVARCHAR:
                nRet = (short)CustomRowTypeConversion.safeParseInt((String)m_value);
                break;
            case DataType.BIGINT:
                nRet = (short)(long)m_value;
                break;
            case DataType.FLOAT:
                nRet = (short)(float)m_value;
                break;
            case DataType.DOUBLE:
            case DataType.REAL:
                nRet = (short)(double)m_value;
                break;
            case DataType.DATE:
            case DataType.TIME:
            case DataType.TIMESTAMP:
            case DataType.BINARY:
            case DataType.VARBINARY:
            case DataType.LONGVARBINARY:
            case DataType.BLOB:
            case DataType.CLOB:
                break;
            case DataType.BIT:
            case DataType.BOOLEAN:
                nRet = (short)((boolean)m_value ? 1 : 0);
                break;
            case DataType.TINYINT:
                nRet = (short)(isSigned() ? (byte)m_value : 0xff & (byte)m_value);
                break;
            case DataType.SMALLINT:
                nRet = (short)m_value;
                break;
            case DataType.INTEGER:
                nRet = (short)(int)m_value;
                break;
            default:
                try {
                    nRet = AnyConverter.toShort(m_value);
                } catch (com.sun.star.lang.IllegalArgumentException e) {
                }
                break;
            }
        }
        return nRet;
    }

    public int getInt32() {
        int nRet = 0;
        if (!isNull()) {
            switch (getTypeKind()) {
            case DataType.CHAR:
            case DataType.VARCHAR:
            case DataType.DECIMAL:
            case DataType.NUMERIC:
            case DataType.LONGVARCHAR:
                nRet = CustomRowTypeConversion.safeParseInt((String)m_value);
                break;
            case DataType.BIGINT:
                nRet = (int)(long)m_value;
                break;
            case DataType.FLOAT:
                nRet = (int)(float)m_value;
                break;
            case DataType.DOUBLE:
            case DataType.REAL:
                nRet = (int)(double)m_value;
                break;
            case DataType.DATE:
                nRet = CustomRowTypeConversion.toDays((Date)m_value);
                break;
            case DataType.TIME:
            case DataType.TIMESTAMP:
            case DataType.BINARY:
            case DataType.VARBINARY:
            case DataType.LONGVARBINARY:
            case DataType.BLOB:
            case DataType.CLOB:
                break;
            case DataType.BIT:
            case DataType.BOOLEAN:
                nRet = (boolean)m_value ? 1 : 0;
                break;
            case DataType.TINYINT:
                nRet = isSigned() ? (byte)m_value : 0xff & (byte)m_value;
                break;
            case DataType.SMALLINT:
                nRet = isSigned() ? (short)m_value : 0xffff & (short)m_value;
                break;
            case DataType.INTEGER:
                nRet = (int)m_value;
                break;
            default:
                try {
                    nRet = AnyConverter.toInt(m_value);
                } catch (com.sun.star.lang.IllegalArgumentException e) {
                }
                break;
            }
        }
        return nRet;
    }

    public long getLong() {
        long nRet = 0;
        if (!isNull()) {
            switch (getTypeKind()) {
            case DataType.CHAR:
            case DataType.VARCHAR:
            case DataType.DECIMAL:
            case DataType.NUMERIC:
            case DataType.LONGVARCHAR:
                nRet = CustomRowTypeConversion.safeParseLong((String)m_value);
                break;
            case DataType.BIGINT:
                nRet = (long)m_value;
                break;
            case DataType.FLOAT:
                nRet = (long)(float)m_value;
                break;
            case DataType.DOUBLE:
            case DataType.REAL:
                nRet = (long)(double)m_value;
                break;
            case DataType.DATE:
                nRet = CustomRowTypeConversion.toDays((Date)m_value);
                break;
            case DataType.TIME:
            case DataType.TIMESTAMP:
            case DataType.BINARY:
            case DataType.VARBINARY:
            case DataType.LONGVARBINARY:
            case DataType.BLOB:
            case DataType.CLOB:
                break;
            case DataType.BIT:
            case DataType.BOOLEAN:
                nRet = (boolean)m_value ? 1 : 0;
                break;
            case DataType.TINYINT:
                nRet = isSigned() ? (byte)m_value : 0xff & (byte)m_value;
                break;
            case DataType.SMALLINT:
                nRet = isSigned() ? (short)m_value : 0xffff & (short)m_value;
                break;
            case DataType.INTEGER:
                nRet = isSigned() ? (int)m_value : 0xffffFFFFL & (int)m_value;
                break;
            default:
                try {
                    nRet = AnyConverter.toInt(m_value);
                } catch (com.sun.star.lang.IllegalArgumentException e) {
                }
                break;
            }
        }
        return nRet;
    }

    public byte[] getSequence() throws SQLException {
        byte[] aSeq = new byte[0];
        if (!isNull()) {
            switch (getTypeKind()) {
            case DataType.OBJECT:
            case DataType.CLOB:
            case DataType.BLOB:
                XInputStream xStream = null;
                if (m_value != null) {
                    XBlob blob = UnoRuntime.queryInterface(XBlob.class, m_value);
                    if (blob != null) {
                        xStream = blob.getBinaryStream();
                    } else {
                        XClob clob = UnoRuntime.queryInterface(XClob.class, m_value);
                        if (clob != null) {
                            xStream = clob.getCharacterStream();
                        }
                    }
                    if (xStream != null) {
                        try {
                            try {
                                final int bytesToRead = 65535;
                                byte[][] aReadSeq = new byte[1][];
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                int read;
                                do {
                                    read = xStream.readBytes(aReadSeq, bytesToRead);
                                    baos.write(aReadSeq[0], 0, read);
                                } while (read == bytesToRead);
                                aSeq = baos.toByteArray();
                            } finally {
                                xStream.closeInput();
                            }
                        } catch (IOException ioException) {
                            throw new SQLException(ioException.getMessage());
                        }
                    }
                }
                break;
            case DataType.VARCHAR:
            case DataType.LONGVARCHAR:
                try {
                    aSeq = ((String)m_value).getBytes("UTF-16");
                } catch (UnsupportedEncodingException unsupportedEncodingException) {
                }
                break;
            case DataType.BINARY:
            case DataType.VARBINARY:
            case DataType.LONGVARBINARY:
                aSeq = ((byte[])m_value).clone();
                break;
            default:
                try {
                    aSeq = ((byte[])AnyConverter.toArray(m_value)).clone();
                } catch (com.sun.star.lang.IllegalArgumentException e) {
                } catch (ClassCastException classCastException) {
                }
                break;
            }
        }
        return aSeq;
    }
   
    public String getString() throws SQLException {
        String aRet = "";
        if (!isNull()) {
            switch (getTypeKind()) {
            case DataType.CHAR:
            case DataType.VARCHAR:
            case DataType.DECIMAL:
            case DataType.NUMERIC:
            case DataType.LONGVARCHAR:
                aRet = (String)m_value;
                break;
            case DataType.BIGINT:
                aRet = isSigned() ? Long.toString((long)m_value) : CustomRowTypeConversion.toUnsignedString((long)m_value);
                break;
            case DataType.FLOAT:
                aRet = ((Float)m_value).toString();
                break;
            case DataType.DOUBLE:
            case DataType.REAL:
                aRet = ((Double)m_value).toString();
                break;
            case DataType.DATE:
                aRet = CustomRowTypeConversion.toDateString((Date)m_value);
                break;
            case DataType.TIME:
                aRet = CustomRowTypeConversion.toTimeString((Time)m_value);
                break;
            case DataType.TIMESTAMP:
                aRet = CustomRowTypeConversion.toDateTimeString((DateTime)m_value);
                break;
            case DataType.BINARY:
            case DataType.VARBINARY:
            case DataType.LONGVARBINARY:
                {
                    StringBuilder sVal = new StringBuilder("0x");
                    byte[] sSeq = getSequence();
                    for (byte b : sSeq) {
                        sVal.append(String.format("%02x", CustomRowTypeConversion.toUnsignedInt(b))); 
                    }
                    aRet = sVal.toString();
                }
                break;
            case DataType.BIT:
            case DataType.BOOLEAN:
                aRet = ((Boolean)m_value).toString();
                break;
            case DataType.TINYINT:
                aRet = isSigned() ? Integer.toString((byte)m_value) : CustomRowTypeConversion.toUnsignedString(0xff & (byte)m_value);
                break;
            case DataType.SMALLINT:
                aRet = isSigned() ? Integer.toString((short)m_value) : CustomRowTypeConversion.toUnsignedString(0xffff & (short)m_value); 
                break;
            case DataType.INTEGER:
                aRet = isSigned() ? Integer.toString((int)m_value) : CustomRowTypeConversion.toUnsignedString((int)m_value);
                break;
            case DataType.CLOB:
                if (AnyConverter.isObject(m_value)) {
                    try {
                        XClob clob = (XClob) AnyConverter.toObject(XClob.class, m_value);
                        if (clob != null) {
                            aRet = clob.getSubString(1, (int)clob.length());
                        }
                    } catch (ClassCastException classCastException) {
                    } catch (com.sun.star.lang.IllegalArgumentException e) {
                    }
                }
                break;
            default:
                try {
                    aRet = AnyConverter.toString(m_value);
                } catch (com.sun.star.lang.IllegalArgumentException e) {
                }
                break;
            }
        }
        return aRet;
    }

    public Time getTime() throws SQLException {
        Time aValue = new Time();
        if (!isNull()) {
            switch (getTypeKind()) {
                case DataType.CHAR:
                case DataType.VARCHAR:
                case DataType.LONGVARCHAR:
                    aValue = CustomRowTypeConversion.toTime(getString());
                    break;
                case DataType.DECIMAL:
                case DataType.NUMERIC:
                    aValue = CustomRowTypeConversion.toTime(getDouble());
                    break;
                case DataType.FLOAT:
                case DataType.DOUBLE:
                case DataType.REAL:
                    aValue = CustomRowTypeConversion.toTime(getDouble());
                    break;
                case DataType.TIMESTAMP:
                    DateTime pDateTime      = (DateTime)m_value;
                    aValue.NanoSeconds      = pDateTime.NanoSeconds;
                    aValue.Seconds          = pDateTime.Seconds;
                    aValue.Minutes          = pDateTime.Minutes;
                    aValue.Hours            = pDateTime.Hours;
                    break;
                case DataType.TIME:
                    Time time               = (Time)m_value;
                    aValue.Hours            = time.Hours;
                    aValue.Minutes          = time.Minutes;
                    aValue.Seconds          = time.Seconds;
                    aValue.NanoSeconds      = time.NanoSeconds;
                    break;
                default:
                    try {
                        aValue = (Time) AnyConverter.toObject(Time.class, m_value);
                    } catch (com.sun.star.lang.IllegalArgumentException e) {
                    } catch (ClassCastException classCastException) {
                    }
                    break;
            }
        }
        return aValue;
    }

    public void setAny(Object value) {
        m_flags &= ~FLAG_NULL;
        this.m_value = value;
        m_type = DataType.OBJECT;
    }

    public void setBoolean(boolean value) {
        m_flags &= ~FLAG_NULL;
        this.m_value = value;
        m_type = DataType.BIT;
    }

    public void setDate(Date date) {
        m_flags &= ~FLAG_NULL;
        this.m_value = new Date(date.Day, date.Month, date.Year);
        m_type = DataType.DATE;
    }

    public void setDateTime(DateTime value) {
        m_flags &= ~FLAG_NULL;
        this.m_value = new DateTime(value.NanoSeconds, value.Seconds, value.Minutes, value.Hours,
                                  value.Day, value.Month, value.Year, false);
        m_type = DataType.TIMESTAMP;
    }

    public void setDouble(double value) {
        m_flags &= ~FLAG_NULL;
        this.m_value = value;
        m_type = DataType.DOUBLE;
    }

    public void setFloat(float value) {
        m_flags &= ~FLAG_NULL;
        this.m_value = value;
        m_type = DataType.FLOAT;
    }
  
    public void setInt8(byte value) {
        m_flags &= ~FLAG_NULL;
        this.m_value = value;
        m_type = DataType.TINYINT;
    }
  
    public void setInt16(short value) {
        m_flags &= ~FLAG_NULL;
        this.m_value = value;
        m_type = DataType.SMALLINT;
    }
  
    public void setInt32(int value) {
        m_flags &= ~FLAG_NULL;
        this.m_value = value;
        m_type = DataType.INTEGER;
    }

    public void setLong(long value) {
        m_flags &= ~FLAG_NULL;
        this.m_value = value;
        m_type = DataType.BIGINT;
    }

    public void setSequence(byte[] value) {
        m_flags &= ~FLAG_NULL;
        this.m_value = value.clone();
        m_type = DataType.LONGVARBINARY;
    }

    public void setString(String value) {
        m_flags &= ~FLAG_NULL;
        this.m_value = value;
        m_type = DataType.VARCHAR;
    }

    public void setTime(Time value) {
        m_flags &= ~FLAG_NULL;
        this.m_value = new Time(value.NanoSeconds, value.Seconds, value.Minutes, value.Hours, false);
        m_type = DataType.TIME;
    }

    public Object makeAny() {
        Object rValue = Any.VOID;
        if(isBound() && !isNull()) {
            switch (getTypeKind()) {
            case DataType.CHAR:
            case DataType.VARCHAR:
            case DataType.DECIMAL:
            case DataType.NUMERIC:
            case DataType.LONGVARCHAR:
                rValue = m_value;
                break;
            case DataType.BIGINT:
                rValue = m_value;
                break;
            case DataType.FLOAT:
                rValue = m_value;
                break;
            case DataType.DOUBLE:
            case DataType.REAL:
                rValue = m_value;
                break;
            case DataType.DATE:
                Date date = (Date)m_value;
                Date dateOut = new Date();
                dateOut.Day = date.Day;
                dateOut.Month = date.Month;
                dateOut.Year = date.Year;
                rValue = dateOut;
                break;
            case DataType.TIME:
                Time time = (Time)m_value;
                Time timeOut = new Time();
                timeOut.Hours = time.Hours;
                timeOut.Minutes = time.Minutes;
                timeOut.Seconds = time.Seconds;
                timeOut.NanoSeconds = time.NanoSeconds;
                rValue = timeOut;
                break;
            case DataType.TIMESTAMP:
                DateTime dateTime = (DateTime)m_value;
                DateTime dateTimeOut = new DateTime(dateTime.NanoSeconds, dateTime.Seconds, dateTime.Minutes, dateTime.Hours,
                                                    dateTime.Day, dateTime.Month, dateTime.Year, false);
                rValue = dateTimeOut;
                break;
            case DataType.BINARY:
            case DataType.VARBINARY:
            case DataType.LONGVARBINARY:
                rValue = ((byte[])m_value).clone();
                break;
            case DataType.BLOB:
            case DataType.CLOB:
            case DataType.OBJECT:
            case DataType.OTHER:
                rValue = getAny();
                break;
            case DataType.BIT:
            case DataType.BOOLEAN:
                rValue = (boolean)m_value;
                break;
            case DataType.TINYINT:
                rValue = (byte)m_value;
                break;
            case DataType.SMALLINT:
                rValue = (short)m_value;
                break;
            case DataType.INTEGER:
                rValue = (int)m_value;
                break;
            default:
                rValue = getAny();
                break;
            }
        }
        return rValue;
    }


}