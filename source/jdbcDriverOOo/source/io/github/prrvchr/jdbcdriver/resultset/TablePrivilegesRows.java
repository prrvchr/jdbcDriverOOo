package io.github.prrvchr.jdbcdriver.resultset;

import java.sql.SQLException;
import java.util.Map;


public class TablePrivilegesRows
{

    // XXX: We rewrite column indexes 4,5,6 and 7, index 5 and 6 will be assigned dynamically
    private Map<Integer, Object> m_columns = Map.ofEntries(Map.entry(4, null),
                                                           Map.entry(7, "YES"));
    private boolean m_replaced = false;
    private boolean m_wasnull = false;

    public TablePrivilegesRows(final String username)
        throws SQLException
    {
        m_columns.put(5, username);
    }

    public void setPrivilege(final String privilege)
    {
        m_columns.put(6, privilege);
    }

    public boolean wasNull(final boolean wasnull)
    {
        return m_replaced ? m_wasnull : wasnull;
    }

    public String getValue(final int index,
                           final String value)
    {
        if (setValue(index)) {
            return (String) getValue(index);
        }
        return value;
    }

    public boolean getValue(final int index,
                            final boolean value)
    {
        if (setValue(index)) {
            return (boolean) getValue(index);
        }
        return value;
    }

    public short getValue(final int index,
                          final short value)
    {
        if (setValue(index)) {
            return (short) getValue(index);
        }
        return value;
    }

    public int getValue(final int index,
                        final int value)
    {
        if (setValue(index)) {
            return (int) getValue(index);
        }
        return value;
    }

    public long getValue(final int index,
                         final long value)
    {
        if (setValue(index)) {
            return (long) getValue(index);
        }
        return value;
    }

    private boolean setValue(int index)
    {
        m_replaced = m_columns.containsKey(index);
        return m_replaced;
    }

    private Object getValue(final int index)
    {
        Object value = m_columns.get(index);
        m_wasnull = value == null;
        return value;
    }

}
