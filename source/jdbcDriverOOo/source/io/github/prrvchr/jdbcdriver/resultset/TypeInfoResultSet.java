package io.github.prrvchr.jdbcdriver.resultset;

import java.sql.SQLException;


public class TypeInfoResultSet
    extends ResultSet
{

    private TypeInfoRows m_rows;

    public TypeInfoResultSet(java.sql.DatabaseMetaData metadata,
                             TypeInfoRows rows)
        throws SQLException
    {
        super(metadata.getTypeInfo());
        m_rows = rows;
    }

    @Override
    public boolean next()
        throws SQLException
    {
        return m_rows.next(super.next());
    }

    @Override
    public boolean wasNull()
        throws SQLException
    {
        return m_rows.wasNull(super.wasNull());
    }

    @Override
    public String getString(int index)
        throws SQLException
    {
        return m_rows.getValue(index, super.getString(index));
    }

    @Override
    public String getString(String label)
        throws SQLException
    {
        return getString(super.findColumn(label));
    }

    @Override
    public boolean getBoolean(int index)
        throws SQLException
    {
        return m_rows.getValue(index, super.getBoolean(index));
    }

    @Override
    public boolean getBoolean(String label)
        throws SQLException
    {
        return getBoolean(super.findColumn(label));
    }

    @Override
    public short getShort(int index)
        throws SQLException
    {
        return m_rows.getNewValue(index, super.getShort(index));
    }

    @Override
    public short getShort(String label)
        throws SQLException
    {
        return getShort(super.findColumn(label));
    }

    @Override
    public int getInt(int index)
        throws SQLException
    {
        return m_rows.getValue(index, super.getInt(index));
    }

    @Override
    public int getInt(String label)
        throws SQLException
    {
        return getInt(super.findColumn(label));
    }

    @Override
    public long getLong(int index)
        throws SQLException
    {
        return m_rows.getValue(index, super.getLong(index));
    }

    @Override
    public long getLong(String label)
        throws SQLException
    {
        return getLong(super.findColumn(label));
    }

}
