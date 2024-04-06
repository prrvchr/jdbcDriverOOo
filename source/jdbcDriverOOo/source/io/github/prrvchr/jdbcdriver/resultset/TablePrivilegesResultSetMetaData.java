package io.github.prrvchr.jdbcdriver.resultset;

import java.sql.SQLException;
import java.util.List;


public class TablePrivilegesResultSetMetaData extends ResultSetMetaData
{

    private java.sql.ResultSet m_result;
    private List<String> m_columns;

    public TablePrivilegesResultSetMetaData(java.sql.ResultSetMetaData metadata,
                                            List<String> columns)
    {
        this(metadata, null, columns);
    }

    public TablePrivilegesResultSetMetaData(java.sql.ResultSetMetaData metadata,
                                            java.sql.ResultSet result,
                                            List<String> columns)
    {
        super(metadata);
        m_result = result;
        m_columns = columns;
    }

    @Override
    public int getColumnCount()
        throws SQLException
    {
        return m_columns.size();
    }

    @Override
    public String getColumnLabel(int index)
        throws SQLException
    {
        return m_columns.get(index - 1);
    }

    @Override
    public String getColumnName(int index)
        throws SQLException 
    {
        return getColumnLabel(index);
    }

    @Override
    int getIndex(int index)
        throws SQLException
    {
        // XXX: The index needs to be rewritten if we have a ResultSet
        if (m_result != null) {
            index = m_result.findColumn(getColumnLabel(index));
        }
        return index;
    }

}
