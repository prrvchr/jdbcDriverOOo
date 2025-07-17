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
/*
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package io.github.prrvchr.java.rowset.internal;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.BitSet;


/**
 * A class that keeps track of a row's values. A <code>Row</code> object
 * maintains an array of current column values and an array of original
 * column values, and it provides methods for getting and setting the
 * value of a column.  It also keeps track of which columns have
 * changed and whether the change was a delete, insert, or update.
 * <P>
 * Note that column numbers for rowsets start at <code>1</code>,
 * whereas the first element of an array or bitset is <code>0</code>.
 * The argument for the method <code>getColumnUpdated</code> refers to
 * the column number in the rowset (the first column is <code>1</code>);
 * the argument for <code>setColumnUpdated</code> refers to the index
 * into the rowset's internal bitset (the first bit is <code>0</code>).
 */
public class Row extends BaseRow implements Serializable, Cloneable {

    static final long serialVersionUID = 5047859032611314762L;

    /**
     * An array containing the current column values for this <code>Row</code>
     * object.
     * @serial
     */
    private Object[] currentVals;

    /**
     * A <code>BitSet</code> object containing a flag for each column in
     * this <code>Row</code> object, with each flag indicating whether or
     * not the value in the column has been changed.
     * @serial
     */
    private BitSet colsChanged;

    /**
     * A <code>boolean</code> indicating whether or not this <code>Row</code>
     * object has been removed. <code>true</code> indicates that it has
     * been removed; <code>false</code> indicates that it has not.
     * @serial
     */
    private boolean removed;

    /**
     * A <code>boolean</code> indicating whether or not this <code>Row</code>
     * object has been removed and executed the SQL deleted. <code>true</code> indicates that it has
     * been executed; <code>false</code> indicates that it has not.
     * @serial
     */
    private boolean deleted;

    /**
     * A <code>boolean</code> indicating whether or not this <code>Row</code>
     * object has been updated.  <code>true</code> indicates that it has
     * been updated; <code>false</code> indicates that it has not.
     * @serial
     */
    private boolean updated;

    /**
     * A <code>boolean</code> indicating whether or not this <code>Row</code>
     * object has been created.  <code>true</code> indicates that it has
     * been created; <code>false</code> indicates that it has not.
     * @serial
     */
    private boolean created;

    /**
     * A <code>boolean</code> indicating whether or not this <code>Row</code>
     * object has been inserted.  <code>true</code> indicates that it has
     * been inserted; <code>false</code> indicates that it has not.
     * @serial
     */
    private boolean inserted;

    /**
     * The number of columns in this <code>Row</code> object.
     * @serial
     */
    private int numCols;

    /**
     * Creates a new <code>Row</code> object with the given number of columns.
     * The newly-created row includes an array of original values,
     * an array for storing its current values, and a <code>BitSet</code>
     * object for keeping track of which column values have been changed.
     *
     * @param idx the number of the column in this <code>Row</code> object
     *            that is to be set; the index of the first column is
     *            <code>1</code>
     */
    public Row(int idx) {
        origVals = new Object[idx];
        currentVals = new Object[idx];
        colsChanged = new BitSet(idx);
        numCols = idx;
    }

    /**
     * Creates a new <code>Row</code> object with the given number of columns
     * and with its array of original values initialized to the given array.
     * The new <code>Row</code> object also has an array for storing its
     * current values and a <code>BitSet</code> object for keeping track
     * of which column values have been changed.
     *
     * @param idx the number of the column in this <code>Row</code> object
     *            that is to be set; the index of the first column is
     *            <code>1</code>
     * @param vals the new values to be set
     */
    public Row(int idx, Object[] vals) {
        origVals = new Object[idx];
        System.arraycopy(vals, 0, origVals, 0, idx);
        currentVals = new Object[idx];
        colsChanged = new BitSet(idx);
        numCols = idx;
    }

    /**
     *
     * This method is called internally by the <code>CachedRowSet.populate</code>
     * methods.
     *
     * @param idx the number of the column in this <code>Row</code> object
     *            that is to be set; the index of the first column is
     *            <code>1</code>
     * @param val the new value to be set
     */
    public void initColumnObject(int idx, Object val) {
        origVals[idx - 1] = val;
    }


    /**
     *
     * This method is called internally by the <code>CachedRowSet.updateXXX</code>
     * methods.
     *
     * @param idx the number of the column in this <code>Row</code> object
     *            that is to be set; the index of the first column is
     *            <code>1</code>
     * @param val the new value to be set
     */
    public void setColumnObject(int idx, Object val) {
        currentVals[idx - 1] = val;
        setColUpdated(idx - 1);
    }

    /**
     * Retrieves the column value stored in the designated column of this
     * <code>Row</code> object.
     *
     * @param columnIndex the index of the column value to be retrieved;
     *                    the index of the first column is <code>1</code>
     * @return an <code>Object</code> in the Java programming language that
     *         represents the value stored in the designated column
     * @throws SQLException if there is a database access error
     */
    public Object getColumnObject(int columnIndex) throws SQLException {
        Object value;
        if (getColUpdated(columnIndex - 1)) {
            value = currentVals[columnIndex - 1];
        } else {
            value = origVals[columnIndex - 1];
        }
        return value;
    }

    /**
     * Indicates whether the designated column of this <code>Row</code> object
     * has been changed.
     * @param idx the index into the <code>BitSet</code> object maintained by
     *            this <code>Row</code> object to keep track of which column
     *            values have been modified; the index of the first bit is
     *            <code>0</code>
     * @return <code>true</code> if the designated column value has been changed;
     *         <code>false</code> otherwise
     *
     */
    public boolean getColUpdated(int idx) {
        return colsChanged.get(idx);
    }

    /**
     * Sets this <code>Row</code> object's <code>deleted</code> field
     * to <code>true</code>.
     *
     * @see #getDeleted
     */
    public void setDeleted() {
        deleted = true;
    }

    /**
     * Sets this <code>Row</code> object's <code>removed</code> field
     * to <code>true</code>.
     *
     * @see #getRemoved
     */
    public void setRemoved() {
        removed = true;
    }

    /**
     * Retrieves the value of this <code>Row</code> object's <code>deleted</code> field,
     * which will be <code>true</code> if one or more of its columns has been
     * deleted.
     * @return <code>true</code> if a column value has been deleted; <code>false</code>
     *         otherwise
     *
     * @see #setDeleted
     */
    public boolean getDeleted() {
        return deleted;
    }

    /**
     * Retrieves the value of this <code>Row</code> object's <code>removed</code> field,
     * which will be <code>true</code> if the SQL delete command has already been executed.
     *
     * @return <code>true</code> if the SQL delete command has already been executed;
     *         <code>false</code> otherwise
     *
     * @see #setRemoved
     */
    public boolean getRemoved() {
        return removed;
    }

    /**
     * Sets the <code>deleted</code> field for this <code>Row</code> object to
     * <code>false</code>.
     */
    public void clearDeleted() {
        deleted = false;
    }

    /**
     * Sets the <code>removed</code> field for this <code>Row</code> object to
     * <code>false</code>.
     */
    public void clearRemoved() {
        removed = false;
    }

    /**
     * Sets the value of this <code>Row</code> object's <code>created</code> field
     * to <code>true</code>.
     *
     * @see #getCreated
     */
    public void setCreated() {
        created = true;
    }

    /**
     * Sets the value of this <code>Row</code> object's <code>inserted</code> field
     * to <code>true</code>.
     *
     * @see #getInserted
     */
    public void setInserted() {
        inserted = true;
    }

    /**
     * Retrieves the value of this <code>Row</code> object's <code>created</code> field,
     * which will be <code>true</code> if this row has been created.
     * @return <code>true</code> if this row has been created; <code>false</code>
     *         otherwise
     *
     * @see #setCreated
     */
    public boolean getCreated() {
        return created;
    }

    /**
     * Retrieves the value of this <code>Row</code> object's <code>inserted</code> field,
     * which will be <code>true</code> if this row has been inserted.
     * @return <code>true</code> if this row has been inserted; <code>false</code>
     *         otherwise
     *
     * @see #setInserted
     */
    public boolean getInserted() {
        return inserted;
    }

    /**
     * Sets the <code>created</code> field for this <code>Row</code> object to
     * <code>false</code>.
     */
    public void clearCreated() {
        created = false;
    }

    /**
     * Sets the <code>inserted</code> field for this <code>Row</code> object to
     * <code>false</code>.
     */
    public void clearInserted() {
        inserted = false;
    }

    /**
     * Retrieves the value of this <code>Row</code> object's
     * <code>updated</code> field.
     * @return <code>true</code> if this <code>Row</code> object has been
     *         updated; <code>false</code> if it has not
     *
     * @see #setUpdated
     */
    public boolean getUpdated() {
        return updated;
    }

    /**
     * Sets the <code>updated</code> field for this <code>Row</code> object to
     * <code>true</code> if one or more of its column values has been changed.
     *
     * @see #getUpdated
     */
    public void setUpdated() {
        boolean update = false;
        // only mark something as updated if one or
        // more of the columns has been changed.
        for (int i = 0; i < numCols; i++) {
            if (getColUpdated(i)) {
                update = true;
                break;
            }
        }
        updated = update;
    }

    /**
     * Sets the bit at the given index into this <code>Row</code> object's internal
     * <code>BitSet</code> object, indicating that the corresponding column value
     * (column <code>idx</code> + 1) has been changed.
     *
     * @param idx the index into the <code>BitSet</code> object maintained by
     *            this <code>Row</code> object; the first bit is at index
     *            <code>0</code>
     *
     */
    private void setColUpdated(int idx) {
        colsChanged.set(idx);
    }

    /**
     * Sets the <code>updated</code> field for this <code>Row</code> object to
     * <code>false</code>, sets all the column values in this <code>Row</code>
     * object's internal array of current values to <code>null</code>, and clears
     * all of the bits in the <code>BitSet</code> object maintained by this
     * <code>Row</code> object.
     */
    public void clearUpdated() {
        clearChanged(true);
        updated = false;
    }

    /**
     * Sets the <code>updated</code> field for this <code>Row</code> object to
     * <code>false</code>, sets all the column values in this <code>Row</code> object's internal
     * array of original values with the values in its internal array of
     * current values.
     */
    public void clearChanged() {
        clearChanged(false);
        updated = false;
    }

    private void clearChanged(boolean reset) {
        for (int i = 0; i < numCols; i++) {
            if (reset) {
                currentVals[i] = null;
            } else if (currentVals[i] != null) {
                origVals[i] = currentVals[i];
            }
            colsChanged.clear(i);
        }
    }

    /**
     * Sets the column values in this <code>Row</code> object's internal
     * array of original values with the values in its internal array of
     * current values, sets all the values in this <code>Row</code>
     * object's internal array of current values to <code>null</code>,
     * clears all the bits in this <code>Row</code> object's internal bitset,
     * and sets its <code>updated</code> field to <code>false</code>.
     * <P>
     * This method is called internally by the <code>CachedRowSet</code>
     * method <code>makeRowOriginal</code>.
     */
    public void moveCurrentToOrig() {
        for (int i = 0; i < numCols; i++) {
            if (getColUpdated(i)) {
                if (currentVals[i] != null) {
                    origVals[i] = currentVals[i];
                    currentVals[i] = null;
                }
                colsChanged.clear(i);
            }
        }
        updated = false;
    }

    /**
     * Returns the row on which the cursor is positioned.
     *
     * @return the <code>Row</code> object on which the <code>CachedRowSet</code>
     *           implementation objects's cursor is positioned
     */
    public BaseRow getCurrentRow() {
        return null;
    }
}
