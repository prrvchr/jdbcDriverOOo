package io.github.prrvchr.jdbcdriver.resultset;

import java.sql.SQLException;


public class TablePrivilegesResultSet
    extends TablePrivilegesResultSetBase
{

    //private String[] m_privileges = {"SELECT", "INSERT", "UPDATE", "DELETE",
    //                                 "READ", "CREATE", "ALTER", "REFERENCE", "DROP"};
    private final String[] m_privileges;
    private final TablePrivilegesRows m_rows;
    private int m_index = -1;

    public TablePrivilegesResultSet(java.sql.ResultSet result,
                                    String [] privileges,
                                    String username)
        throws SQLException
    {
        super(result, result.getMetaData());
        m_privileges = privileges;
        m_rows = new TablePrivilegesRows(username);
    }

    @Override
    public boolean next()
        throws SQLException
    {
        m_index ++;
        boolean next = true;
        int index = m_index % m_privileges.length;
        if (index == 0) {
            next = m_result.next();
        }
        if (next) {
            m_rows.setPrivilege(m_privileges[index]);
        }
        return next;
    }

    @Override
    public int getRow()
        throws SQLException
    {
        if (m_index >= 0) {
            return m_index + 1;
        }
        return 0;
    }

    @Override
    public boolean wasNull()
        throws SQLException
    {
        return m_rows.wasNull(m_result.wasNull());
    }

    @Override
    public String getString(int index)
        throws SQLException
    {
        return m_rows.getValue(index, m_result.getString(index));
    }

    @Override
    public String getString(String label)
        throws SQLException
    {
        return getString(findColumn(label));
    }

    @Override
    public boolean getBoolean(int index)
        throws SQLException
    {
        return m_rows.getValue(index, m_result.getBoolean(index));
    }

    @Override
    public boolean getBoolean(String label)
        throws SQLException
    {
        return getBoolean(findColumn(label));
    }

    @Override
    public short getShort(int index)
        throws SQLException
    {
        return m_rows.getValue(index, m_result.getShort(index));
    }

    @Override
    public short getShort(String label)
        throws SQLException
    {
        return getShort(findColumn(label));
    }

    @Override
    public int getInt(int index)
        throws SQLException
    {
        return m_rows.getValue(index, m_result.getInt(index));
    }

    @Override
    public int getInt(String label)
        throws SQLException
    {
        return getInt(findColumn(label));
    }

    @Override
    public long getLong(int index)
        throws SQLException
    {
        return m_rows.getValue(index, m_result.getLong(index));
    }

    @Override
    public long getLong(String label)
        throws SQLException
    {
        return getLong(findColumn(label));
    }

}
