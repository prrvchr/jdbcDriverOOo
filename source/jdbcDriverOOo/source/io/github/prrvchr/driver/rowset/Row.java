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
package io.github.prrvchr.driver.rowset;

import java.util.BitSet;
import java.util.List;


public class Row
    extends BaseRow {

    protected Object[] mNewValues;
    protected BitSet mUpdated;
    protected boolean mUpdatable = true;


    public Row(int count) {
        super(count);
        mNewValues = new Object[count];
        mUpdated = new BitSet(count);
    }

    public Row(BaseRow row) {
        super(row);
        mNewValues = new Object[mCount];
        mUpdated = new BitSet(mCount);
    }

    public void clearUpdated(List<RowColumn> columns, int status) {
        for (RowColumn column : columns) {
            int i = column.getIndex() - 1;
            if (status != 0) {
                mOldValues[i] = mNewValues[i];
            }
            mNewValues[i] = null;
            mUpdated.clear(i);
        }
    }

    public Object getOldColumnObject(int index) {
        return mOldValues[index - 1];
    }

    public boolean isUpdated() {
        // XXX: In order to be able to throw an exception again if necessary
        // XXX: we need to reset the m_Updatable flag if it's set...
        boolean updated = false;
        if (!mUpdatable) {
            mUpdatable = true;
        }
        for (int i = 1; i <= mCount; i++) {
            updated = isColumnSet(i);
        }
        return updated;
    }

    public boolean isUpdatable() {
        return mUpdatable;
    }

    public void clearUpdatable() {
        mUpdatable = false;
    }

    @Override
    public boolean isColumnNull(int index) {
        boolean isnull = true;
        if (isColumnSet(index)) {
            isnull = mNewValues[index - 1] == null;
        } else {
            isnull = mOldValues[index - 1] == null;
        }
        return isnull;
    }

    @Override
    public Object getColumnObject(int index) {
        Object value = null;
        if (isColumnSet(index)) {
            value = mNewValues[index - 1];
        } else {
            value = mOldValues[index - 1];
        }
        return value;
    }

    @Override
    public void setColumnObject(int index, Object value) {
        if (mUpdatable) {
            int i = index - 1;
            mNewValues[i] = value;
            mUpdated.set(i);
        }
    }

    @Override
    public void setColumnDouble(int index, Double value, int type) {
        if (mUpdatable) {
            int i = index - 1;
            mNewValues[i] = RowHelper.getDoubleValue(value, type);
            mUpdated.set(i);
        }
    }

    @Override
    public boolean isColumnSet(int index) {
        return mUpdated.get(index - 1);
    }


}
