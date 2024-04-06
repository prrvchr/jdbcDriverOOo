package io.github.prrvchr.jdbcdriver.resultset;

import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

import io.github.prrvchr.jdbcdriver.DBTools;


public class TypeInfoRows
{

    private Map<String, Map<Integer, String>> m_rows;
    private final String m_pattern = "%s:%s";
    private String m_key = null;
    private boolean m_replaced = false;

    public TypeInfoRows(Object[] typeinfos)
        throws SQLException
    {
        m_rows = getTypeInfoRows(typeinfos);
    }

    public boolean next(final boolean next)
    {
        m_key = null;
        return next;
    }

    public boolean wasNull(final boolean wasnull)
    {
        return m_replaced ? false : wasnull;
    }

    public String getValue(final int index,
                           final String value)
    {
        if (setValue(index, value)) {
            return getValue(index);
        }
        return value;
    }

    public boolean getValue(final int index,
                            final boolean value)
    {
        if (setValue(index, value)) {
            return Boolean.parseBoolean(getValue(index));
        }
        return value;
    }

    public short getNewValue(final int index,
                             final short value)
    {
        if (setValue(index, value)) {
            return Short.parseShort(getValue(index));
        }
        return value;
    }

    public int getValue(final int index,
                        final int value)
    {
        if (setValue(index, value)) {
            return Integer.parseInt(getValue(index));
        }
        return value;
    }

    public long getValue(final int index,
                         final long value)
    {
        if (setValue(index, value)) {
            return Long.parseLong(getValue(index));
        }
        return value;
    }

    private String getValue(final int index)
    {
        return m_rows.get(m_key).get(index);
    }

    private boolean setValue(int index,
                             Object value)
    {
        m_replaced = false;
        if (m_key == null) {
            String key = getKey(index, value);
            if (key != null && m_rows.containsKey(key)) {
                m_key = key;
            }
        }
        if (m_key != null && m_rows.containsKey(m_key) && m_rows.get(m_key).containsKey(index)) {
            m_replaced = true;
        }
        return m_replaced;
    }

    private Map<String, Map<Integer, String>> getTypeInfoRows(Object[] typeinfos)
    {
        Map<String, Map<Integer, String>> rows = new TreeMap<>();
        int size = DBTools.getEvenLength(typeinfos.length);
        for (int i = 0; i < size; i += 2) {
            String key = getKey(typeinfos[i].toString());
            if (key != null && !rows.containsKey(key)) {
                Map<Integer, String> columns = new TreeMap<>();
                rows.put(key, columns);
            }
            if (rows.containsKey(key)) {
                String value = typeinfos[i +1].toString();
                int index = getIndex(value);
                String newvalue = getValue(value);
                if (index != -1 && newvalue != null) {
                    rows.get(key).put(index, newvalue);
                }
            }
        }
        return rows;
    }

    private String getKey(String value)
    {
        String key = null;
        int index = getIndex(value);
        if (index != -1) {
            key = getKey(index, getValue(value));
        }
        return key;
    }

    private String getKey(int index,
                          Object value)
    {
        String key = null;
        if (value != null) {
            key = String.format(m_pattern, index, value);
        }
        return key;
    }

    private int getIndex(final String value)
    {
        int index = -1;
        int start = value.indexOf("(");
        int end = value.indexOf(")");
        if (start != -1 && end != -1 && start < end) {
            index = Integer.parseInt(value.substring(start +1, end).strip());
        }
        return index;
    }

    private String getValue(final String value)
    {
        String newvalue = null;
        int index = value.indexOf("=");
        if (index != -1) {
            newvalue = value.substring(index +1).strip();
        }
        return newvalue;
    }

}
