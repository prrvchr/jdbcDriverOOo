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
package io.github.prrvchr.uno.driver.resultset;

import java.sql.SQLException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map;


public class RowSetData {

    private Map<Integer, List<String>> mKeys;
    private Map<String, List<SimpleImmutableEntry<Integer, String>>> mValues;

    public RowSetData(Map<Integer, List<String>> keys,
                      Map<String, List<SimpleImmutableEntry<Integer, String>>> values)
        throws SQLException {
        mKeys = keys;
        mValues = values;
    }

    // XXX: A custom key is required to bind mKeys and mValues
    public static String getDataKey(int index, String value) {
        return String.format("%s=%s", index, value);
    }

    public boolean hasEntry(int index, String key) {
        return mKeys.containsKey(index) && mKeys.get(index).contains(key);
    }

    public List<SimpleImmutableEntry<Integer, String>> getValue(String key) {
        return mValues.get(key);
    }

    public boolean isDeletableRow(String key) {
        return !mValues.containsKey(key);
    }

    public Map<Integer, List<String>> getKeys() {
        return mKeys;
    }

    public Map<String, List<SimpleImmutableEntry<Integer, String>>> getValues() {
        return mValues;
    }

}
