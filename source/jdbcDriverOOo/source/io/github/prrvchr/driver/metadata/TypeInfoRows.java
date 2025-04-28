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
package io.github.prrvchr.driver.metadata;

import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

import io.github.prrvchr.driver.helper.DBTools;


public class TypeInfoRows {

    private Map<String, Map<Integer, String>> mRows;
    private final String mPattern = "%s:%s";
    private String mKey = null;
    private boolean mReplaced = false;

    public TypeInfoRows(Object[] typeinfos)
        throws SQLException {
        mRows = getTypeInfoRows(typeinfos);
    }

    public boolean next(final boolean next) {
        mKey = null;
        return next;
    }

    public boolean wasNull(final boolean wasnull) {
        boolean isnull = wasnull;
        if (mReplaced) {
            isnull = false;
        }
        return isnull;
    }

    public String getValue(final int index,
                           final String value) {
        String newvalue = value;
        if (setValue(index, value)) {
            newvalue = getValue(index);
        }
        return newvalue;
    }

    public boolean getValue(final int index,
                            final boolean value) {
        boolean newvalue = value;
        if (setValue(index, value)) {
            newvalue = Boolean.parseBoolean(getValue(index));
        }
        return newvalue;
    }

    public short getNewValue(final int index,
                             final short value) {
        short newvalue = value;
        if (setValue(index, value)) {
            newvalue = Short.parseShort(getValue(index));
        }
        return newvalue;
    }

    public int getValue(final int index,
                        final int value) {
        int newvalue = value;
        if (setValue(index, value)) {
            newvalue = Integer.parseInt(getValue(index));
        }
        return newvalue;
    }

    public long getValue(final int index,
                         final long value) {
        long newvalue = value;
        if (setValue(index, value)) {
            newvalue = Long.parseLong(getValue(index));
        }
        return newvalue;
    }

    private String getValue(final int index) {
        return mRows.get(mKey).get(index);
    }

    private boolean setValue(int index,
                             Object value) {
        mReplaced = false;
        if (mKey == null) {
            String key = getKey(index, value);
            if (key != null && mRows.containsKey(key)) {
                mKey = key;
            }
        }
        if (mKey != null && mRows.containsKey(mKey) && mRows.get(mKey).containsKey(index)) {
            mReplaced = true;
        }
        return mReplaced;
    }

    private Map<String, Map<Integer, String>> getTypeInfoRows(Object[] typeinfos) {
        Map<String, Map<Integer, String>> rows = new TreeMap<>();
        int size = DBTools.getEvenLength(typeinfos.length);
        for (int i = 0; i < size; i += 2) {
            String key = getKey(typeinfos[i].toString());
            if (key != null && !rows.containsKey(key)) {
                Map<Integer, String> columns = new TreeMap<>();
                rows.put(key, columns);
            }
            if (rows.containsKey(key)) {
                String value = typeinfos[i + 1].toString();
                int index = getIndex(value);
                String newvalue = getValue(value);
                if (index != -1 && newvalue != null) {
                    rows.get(key).put(index, newvalue);
                }
            }
        }
        return rows;
    }

    private String getKey(String value) {
        String key = null;
        int index = getIndex(value);
        if (index != -1) {
            key = getKey(index, getValue(value));
        }
        return key;
    }

    private String getKey(int index,
                          Object value) {
        String key = null;
        if (value != null) {
            key = String.format(mPattern, index, value);
        }
        return key;
    }

    private int getIndex(final String value) {
        int index = -1;
        int start = value.indexOf("(");
        int end = value.indexOf(")");
        if (start != -1 && end != -1 && start < end) {
            index = Integer.parseInt(value.substring(start + 1, end).strip());
        }
        return index;
    }

    private String getValue(final String value) {
        String newvalue = null;
        int index = value.indexOf("=");
        if (index != -1) {
            newvalue = value.substring(index + 1).strip();
        }
        return newvalue;
    }

}
