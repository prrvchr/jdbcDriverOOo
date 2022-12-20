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

import java.util.ArrayList;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.container.XNameAccess;
import com.sun.star.io.XInputStream;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.FetchDirection;
import com.sun.star.sdbc.ResultSetConcurrency;
import com.sun.star.sdbc.ResultSetType;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XArray;
import com.sun.star.sdbc.XBlob;
import com.sun.star.sdbc.XClob;
import com.sun.star.sdbc.XCloseable;
import com.sun.star.sdbc.XColumnLocate;
import com.sun.star.sdbc.XRef;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbc.XResultSetMetaData;
import com.sun.star.sdbc.XResultSetMetaDataSupplier;
import com.sun.star.sdbc.XRow;
import com.sun.star.sdbcx.CompareBookmark;
import com.sun.star.sdbcx.XRowLocate;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Type;
import com.sun.star.util.Date;
import com.sun.star.util.DateTime;
import com.sun.star.util.Time;

import io.github.prrvchr.uno.beans.PropertySet;
import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertySetter;
import io.github.prrvchr.uno.helper.PropertyIds;


public class CustomResultSet
    extends PropertySet
    implements XCloseable,
               XColumnLocate,
               XResultSet,
               XResultSetMetaDataSupplier,
               XRow,
               XRowLocate
{

    private XResultSetMetaData m_xMetaData;
    private ArrayList<CustomRowSet[]> m_rows;
    private final boolean m_IsBookmarkable;
    /// 0-based:
    private int m_row = -1;
    /// 1-based:
    private int m_column;


    // The constructor method:
    public CustomResultSet(XResultSetMetaData metadata,
                           ArrayList<CustomRowSet[]> rows)
    {
        m_xMetaData = metadata;
        m_rows = rows;
        m_IsBookmarkable = true;
        registerProperties();
    }

    private void registerProperties() {
        short readonly = PropertyAttribute.READONLY;
        registerProperty(PropertyIds.CURSORNAME.name, PropertyIds.CURSORNAME.id, Type.STRING, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return _getCursorName();
                }
            }, null);
        registerProperty(PropertyIds.RESULTSETCONCURRENCY.name, PropertyIds.RESULTSETCONCURRENCY.id, Type.LONG, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return _getResultSetConcurrency();
                }
            }, null);
        registerProperty(PropertyIds.RESULTSETTYPE.name, PropertyIds.RESULTSETTYPE.id, Type.LONG, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return _getResultSetType();
                }
            }, null);
        registerProperty(PropertyIds.FETCHDIRECTION.name, PropertyIds.FETCHDIRECTION.id, Type.LONG,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return _getFetchDirection();
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    _setFetchDirection((int)value);
                }
            });
        registerProperty(PropertyIds.FETCHSIZE.name, PropertyIds.FETCHSIZE.id, Type.LONG,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return _getFetchSize();
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    _setFetchSize((int)value);
                }
            });
        registerProperty(PropertyIds.ISBOOKMARKABLE.name, PropertyIds.ISBOOKMARKABLE.id, Type.BOOLEAN, readonly,
                new PropertyGetter() {
                    @Override
                    public Object getValue() throws WrappedTargetException {
                        return _getIsBookmarkable();
                    }
                }, null);
    }

    protected String _getCursorName()
    {
        String cursor = "";
        System.out.println("CustomResultSet._getCursorName() Value: " + cursor);
        return cursor;
    }

    protected int _getFetchDirection()
    {
        return FetchDirection.FORWARD;
    }
    protected void _setFetchDirection(int direction)
    {
        // noop
    }

    protected int _getFetchSize()
    {
        return 0;
    }
    protected void _setFetchSize(int size)
    {
        // noop
    }

    protected int _getResultSetConcurrency()
    {
         System.out.println("CustomResultSet.getResultSetConcurrency() Value: " + ResultSetConcurrency.READ_ONLY);
         return ResultSetConcurrency.READ_ONLY;
    }

    protected int _getResultSetType()
    {
        System.out.println("CustomResultSet.getResultSetType() Value: " + ResultSetType.FORWARD_ONLY);
        return ResultSetType.SCROLL_INSENSITIVE;
    }


    protected boolean _getIsBookmarkable()
    {
        System.out.println("sdbc.ResultSetSuper._getIsBookmarkable(): " + m_IsBookmarkable);
        return m_IsBookmarkable;
    }


    // com.sun.star.lang.XComponent
    @Override
    public void postDisposing() { 
        System.out.println("CustomResultSet.postDisposing() **************************************");
    }


    // com.sun.star.sdbc.XCloseable
    @Override
    public void close() throws SQLException
    {
        System.out.println("CustomResultSet.close() **************************************");
    }


    // com.sun.star.sdbc.XResultSet:
    private CustomRowSet getField(int columnIndex)
        throws SQLException
    {
        if (isBeforeFirst() || isAfterLast()) {
            throw new SQLException("Row out of range");
        }
        CustomRowSet[] fields = m_rows.get(m_row);
        if (columnIndex < 1 || fields.length < columnIndex) {
            throw new SQLException("Column out of range");
        }
        m_column = columnIndex;
        return fields[columnIndex - 1];
    }

    public synchronized boolean absolute(int position)
        throws SQLException
    {
        System.out.println("CustomResultSet.absolute()");
        checkDisposed();
        if (position >= 0) {
            m_row = position;
        } else {
            m_row = m_rows.size() + position;
        }
        if (m_row <= -1) {
            m_row = -1;
            return false;
        }
        if (m_row >= m_rows.size()) {
            m_row = m_rows.size();
            return false;
        }
        return true;
    }

    public synchronized void afterLast()
        throws SQLException
    {
        //System.out.println("CustomResultSet.afterLast()");
        checkDisposed();
        m_row = m_rows.size();
    }

    public synchronized void beforeFirst() throws SQLException {
        //System.out.println("CustomResultSet.beforeFirst()");
        checkDisposed();
        m_row = -1;
    }

    public synchronized boolean first() throws SQLException {
        //System.out.println("CustomResultSet.first()");
        checkDisposed();
        m_row = 0;
        return true;
    }

    public synchronized int getRow() throws SQLException {
        System.out.println("CustomResultSet.getRow()");
        checkDisposed();
        return m_row + 1;
    }

    public synchronized Object getStatement() throws SQLException {
        System.out.println("CustomResultSet.getStatement()");
        checkDisposed();
        return null;
    }

    public synchronized boolean isAfterLast() throws SQLException {
        //System.out.println("CustomResultSet.isAfterLast()");
        checkDisposed();
        return m_row == m_rows.size();
    }

    public synchronized boolean isBeforeFirst() throws SQLException {
        //System.out.println("CustomResultSet.isBeforeFirst()");
        checkDisposed();
        return m_row == -1;
    }

    public synchronized boolean isFirst() throws SQLException {
        //System.out.println("CustomResultSet.isFirst()");
        checkDisposed();
        return m_row == 0;
    }

    public synchronized boolean isLast() throws SQLException {
        //System.out.println("CustomResultSet.isLast()");
        checkDisposed();
        return m_row == (m_rows.size() - 1);
    }

    public synchronized boolean last() throws SQLException {
        //System.out.println("CustomResultSet.last()");
        checkDisposed();
        m_row = m_rows.size() - 1;
        return true;
    }

    public synchronized boolean next() throws SQLException {
        //System.out.println("CustomResultSet.next()");
        checkDisposed();
        if (m_row < m_rows.size()) {
            ++m_row;
        }
        return m_row < m_rows.size();
    }

    public synchronized boolean previous() throws SQLException {
        //System.out.println("CustomResultSet.previous()");
        checkDisposed();
        if (m_row > -1) {
            --m_row;
        }
        return m_row > -1;
    }

    public synchronized void refreshRow() throws SQLException {
        //System.out.println("CustomResultSet.refreshRow()");
        checkDisposed();
    }

    public synchronized boolean relative(int offset) throws SQLException {
        //System.out.println("CustomResultSet.relative()");
        checkDisposed();
        m_row += offset;
        if (m_row <= -1) {
            m_row = -1;
            return false;
        }
        if (m_row >= m_rows.size()) {
            m_row = m_rows.size();
            return false;
        }
        return true;
    }

    public synchronized boolean rowDeleted() throws SQLException {
        System.out.println("CustomResultSet.rowDeleted()");
        checkDisposed();
        return false;
    }

    public synchronized boolean rowInserted() throws SQLException {
        System.out.println("CustomResultSet.rowInserted()");
        checkDisposed();
        return false;
    }

    public synchronized boolean rowUpdated() throws SQLException {
        System.out.println("CustomResultSet.rowUpdated()");
        checkDisposed();
        return false;
    }

    // com.sun.star.sdbc.XResultSetMetaDataSupplier:
    public synchronized XResultSetMetaData getMetaData() throws SQLException {
        checkDisposed();
        return m_xMetaData;
    }

    // com.sun.star.sdbc.XRow:
    public synchronized XArray getArray(int columnIndex) throws SQLException {
        checkDisposed();
        return null;
    }

    public synchronized XInputStream getBinaryStream(int columnIndex) throws SQLException {
        checkDisposed();
        return null;
    }

    public synchronized XBlob getBlob(int columnIndex) throws SQLException {
        checkDisposed();
        return null;
    }

    public synchronized boolean getBoolean(int columnIndex) throws SQLException {
        checkDisposed();
        CustomRowSet field = getField(columnIndex);
        return field.getBoolean();
    }

    public synchronized byte getByte(int columnIndex) throws SQLException {
        checkDisposed();
        CustomRowSet field = getField(columnIndex);
        return field.getInt8();
    }

    public synchronized byte[] getBytes(int columnIndex) throws SQLException {
        checkDisposed();
        CustomRowSet field = getField(columnIndex);
        return field.getSequence();
    }

    public synchronized XInputStream getCharacterStream(int columnIndex) throws SQLException {
        checkDisposed();
        return null;
    }

    public synchronized XClob getClob(int columnIndex) throws SQLException {
        checkDisposed();
        return null;
    }

    public synchronized Date getDate(int columnIndex) throws SQLException {
        checkDisposed();
        CustomRowSet field = getField(columnIndex);
        return field.getDate();
    }

    public synchronized double getDouble(int columnIndex) throws SQLException {
        checkDisposed();
        CustomRowSet field = getField(columnIndex);
        return field.getDouble();
    }

    public synchronized float getFloat(int columnIndex) throws SQLException {
        checkDisposed();
        CustomRowSet field = getField(columnIndex);
        return field.getFloat();
    }

    public synchronized int getInt(int columnIndex) throws SQLException {
        checkDisposed();
        CustomRowSet field = getField(columnIndex);
        return field.getInt32();
    }

    public synchronized long getLong(int columnIndex) throws SQLException {
        checkDisposed();
        CustomRowSet field = getField(columnIndex);
        return field.getLong();
    }

    public synchronized Object getObject(int columnIndex, XNameAccess arg1) throws SQLException {
        checkDisposed();
        CustomRowSet field = getField(columnIndex);
        return field.makeAny();
    }

    public synchronized XRef getRef(int columnIndex) throws SQLException {
        checkDisposed();
        return null;
    }

    public synchronized short getShort(int columnIndex) throws SQLException {
        checkDisposed();
        CustomRowSet field = getField(columnIndex);
        return field.getInt16();
    }

    public synchronized String getString(int columnIndex) throws SQLException {
        checkDisposed();
        CustomRowSet field = getField(columnIndex);
        return field.getString();
    }

    public synchronized Time getTime(int columnIndex) throws SQLException {
        checkDisposed();
        CustomRowSet field = getField(columnIndex);
        return field.getTime();
    }

    public synchronized DateTime getTimestamp(int columnIndex) throws SQLException {
        checkDisposed();
        CustomRowSet field = getField(columnIndex);
        return field.getDateTime();
    }

    public synchronized boolean wasNull() throws SQLException {
        checkDisposed();
        CustomRowSet field = getField(m_column);
        return field.isNull();
    }

    // com.sun.star.sdbc.XColumnLocate:
    public synchronized int findColumn(String name) throws SQLException {
        checkDisposed();
        for (int i = 1; i <= m_xMetaData.getColumnCount(); i++) {
            boolean isCaseSensitive = m_xMetaData.isCaseSensitive(i);
            String columnName = m_xMetaData.getColumnName(i);
            boolean matched;
            if (isCaseSensitive) {
                matched = columnName.equals(name);
            }
            else {
                matched = columnName.equalsIgnoreCase(name);
            }
            if (matched) {
                return i;
            }
        }
        // TODO: String error = SharedResources.getInstance().getResourceStringWithSubstitution(
        // TODO:     Resources.STR_UNKNOWN_COLUMN_NAME, "$columnname$", name);
        String message = "Error column not found";
        throw new SQLException(message, this, StandardSQLState.SQL_COLUMN_NOT_FOUND.text(), 0, null);
    }

    // com.sun.star.sdbcx.XRowLocate:
    public synchronized int compareBookmarks(Object arg0, Object arg1) throws SQLException {
        System.out.println("CustomResultSet.compareBookmarks()");
        checkDisposed();
        int bookmark1, bookmark2;
        try {
            bookmark1 = AnyConverter.toInt(arg0);
            bookmark2 = AnyConverter.toInt(arg1);
        } catch (IllegalArgumentException illegalArgumentException) {
            return CompareBookmark.NOT_COMPARABLE;
        }
        if (bookmark1 < bookmark2) {
            return CompareBookmark.LESS;
        } else if (bookmark1 > bookmark2) {
            return CompareBookmark.GREATER;
        } else {
            return CompareBookmark.EQUAL;
        }
    }

    public synchronized Object getBookmark() throws SQLException {
        System.out.println("CustomResultSet.getBookmark()");
        checkDisposed();
        return m_row;
    }

    public synchronized boolean hasOrderedBookmarks() throws SQLException {
        System.out.println("CustomResultSet.hasOrderedBookmarks()");
        checkDisposed();
        return true;
    }

    public synchronized int hashBookmark(Object arg0) throws SQLException {
        System.out.println("CustomResultSet.hashBookmark()");
        checkDisposed();
        int bookmark;
        try {
            bookmark = AnyConverter.toInt(arg0);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new SQLException("Bad bookmark", this, StandardSQLState.SQL_INVALID_BOOKMARK_VALUE.text(), 0, null);
        }
        return bookmark;
    }

    public synchronized boolean moveRelativeToBookmark(Object arg0, int arg1) throws SQLException {
        System.out.println("CustomResultSet.moveRelativeToBookmark()");
        checkDisposed();
        int bookmark;
        boolean moved = false;
        try {
            bookmark = AnyConverter.toInt(arg0);
            moved = absolute(bookmark);
            if (moved) {
                moved = relative(arg1);
            }
        } catch (IllegalArgumentException illegalArgumentException) {
        }
        if (!moved) {
            afterLast();
        }
        return moved;
    }

    public synchronized boolean moveToBookmark(Object arg0) throws SQLException {
        System.out.println("CustomResultSet.moveToBookmark()");
        checkDisposed();
        int bookmark;
        boolean moved = false;
        try {
            bookmark = AnyConverter.toInt(arg0);
            moved = absolute(bookmark);
        }catch (IllegalArgumentException illegalArgumentException) { }
        if (!moved) {
            afterLast();
        }
            return moved;
    }


}

