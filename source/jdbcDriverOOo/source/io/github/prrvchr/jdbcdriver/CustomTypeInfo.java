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
package io.github.prrvchr.jdbcdriver;

import java.util.HashMap;
import java.util.Map;

import com.sun.star.sdbc.SQLException;


public class CustomTypeInfo
{
    private String m_keypattern = "COLUMN(%s) = %s";
    private Map<String, CustomTypeInfoColumns> m_columns = new HashMap<>();

    public CustomTypeInfo(Object[] typeinfos) {
        int size = _getEvenSize(typeinfos.length);
        for (int i = 0; i < size; i+=2) {
            String key = typeinfos[i].toString();
            if (!m_columns.containsKey(key)) {
                m_columns.put(key, new CustomTypeInfoColumns());
            }
            CustomTypeInfoColumns column =  m_columns.get(key);
            String value = typeinfos[i +1].toString();
            column.addEntry(value);
            
        }
    }
    
    public CustomColumn[] getTypeInfoRow(CustomColumn[] columns)
        throws SQLException
    {
        int index = 1;
        CustomTypeInfoColumns values = null;
        for (CustomColumn column: columns) {
            if (!column.isNull()) {
                String key = String.format(m_keypattern, index, column.getString());
                values = m_columns.get(key);
                if (values != null) {
                    break;
                }
            }
            index += 1;
        }
        if (values != null) {
            columns = values.getTypeInfoRow(columns);
        }
        return columns;
    }

    private int _getEvenSize(final int size)
    {
        if ( (size & 1) == 0 ) {
            return size;
        }
        return size -1;
    }


    private class CustomTypeInfoColumns
    {
        private Map<Integer, Object> m_columns = new HashMap<>();

        public CustomTypeInfoColumns() {
        }

        public void addEntry(final String value)
        {
            int index = _getIndex(value);
            Object object = _getObject(value);
            if (index != -1) {
                m_columns.put(index, object);
            }
        }

        public CustomColumn[] getTypeInfoRow(CustomColumn[] columns)
        {
            for (Integer index: m_columns.keySet()) {
                if (0 < index && index <= columns.length) {
                    CustomColumn column = columns[index -1];
                    column.fill(m_columns.get(index));
                }
            }
            return columns;
        }

        private int _getIndex(final String value)
        {
            int index = -1;
            int start = value.indexOf("(");
            int end = value.indexOf(")");
            if (start != -1 && end != -1 && start < end) {
                index = Integer.parseInt(value.substring(start +1, end));
                System.out.println("CustomTypeInfo Column Index: " + index);
            }
            return index;
        }

        private Object _getObject(final String value)
        {
            Object object = null;
            int index = value.indexOf("=");
            if (index != -1) {
                object = value.substring(index +1).strip();
                System.out.println("CustomTypeInfo Column Object: " + object);
            }
            return object;
        }

    }

}