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
package io.github.prrvchr.uno.sdbc;

import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XResultSetMetaData;

import io.github.prrvchr.uno.helper.UnoHelper;


public final class ResultSetMetaData
extends WeakBase
implements XResultSetMetaData
{
    private final java.sql.ResultSetMetaData m_Metadata;
    
    // The constructor method:
    public ResultSetMetaData(java.sql.ResultSetMetaData metadata)
    {
        m_Metadata = metadata;
    }


    // com.sun.star.sdbc.XResultSetMetaData:
    @Override
    public String getCatalogName(int index) throws SQLException
    {
        try
        {
            String value = m_Metadata.getCatalogName(index);
            System.out.println("sdbc.ResultSetMetaData.getCatalogName() 1 :" + value);
            return value != null ? value : "";
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getColumnCount() throws SQLException
    {
        try
        {
            int value = m_Metadata.getColumnCount();
            System.out.println("sdbc.ResultSetMetaData.getColumnCount() 1 :" + value);
            return value;
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getColumnDisplaySize(int index) throws SQLException
    {
        try
        {
            int value = m_Metadata.getColumnDisplaySize(index);
            System.out.println("sdbc.ResultSetMetaData.getColumnDisplaySize() 1 :" + value);
            return value;
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getColumnLabel(int index) throws SQLException
    {
        try
        {
            String value = m_Metadata.getColumnLabel(index);
            System.out.println("sdbc.ResultSetMetaData.getColumnLabel() 1 :" + value);
            return value != null ? value : "";
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getColumnName(int index) throws SQLException
    {
        try
        {
            System.out.println("sdbc.ResultSetMetaData.getColumnName() 1");
            String value = m_Metadata.getColumnName(index);
            return value != null ? value : "";
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getColumnServiceName(int index) throws SQLException
    {
        try
        {
            String value = m_Metadata.getColumnClassName(index);
            return value != null ? value : "";
    } catch (java.sql.SQLException e)
    {
        throw UnoHelper.getSQLException(e, this);
    }
    }

    @Override
    public int getColumnType(int index) throws SQLException
    {
        try
        {
            System.out.println("sdbc.ResultSetMetaData.getColumnType() 1 :" + index);
            int value = UnoHelper.mapSQLDataType(m_Metadata.getColumnType(index));
            System.out.println("sdbc.ResultSetMetaData.getColumnType() 2 :" + value);
            return value;
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getColumnTypeName(int index) throws SQLException
    {
        try
        {
            System.out.println("sdbc.ResultSetMetaData.getColumnTypeName() 1 " + index);
            String value = m_Metadata.getColumnTypeName(index);
            return value != null ? UnoHelper.mapSQLDataTypeName(value, getColumnType(index)) : "";
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
        }

    @Override
    public int getPrecision(int index) throws SQLException
    {
        try
        {
            System.out.println("sdbc.ResultSetMetaData.getPrecision() 1");
            return m_Metadata.getPrecision(index);
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int getScale(int index) throws SQLException
    {
        try
        {
            System.out.println("sdbc.ResultSetMetaData.getScale() 1");
            return m_Metadata.getScale(index);
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getSchemaName(int index) throws SQLException
    {
        try
        {
            String value = m_Metadata.getSchemaName(index);
            System.out.println("sdbc.ResultSetMetaData.getSchemaName() 1 : " + value);
            return value != null ? value : "";
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public String getTableName(int index) throws SQLException
    {
        try
        {
            System.out.println("sdbc.ResultSetMetaData.getTableName() 1");
            String value = m_Metadata.getTableName(index);
            return value != null ? value : "";
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isAutoIncrement(int index) throws SQLException
    {
        try
        {
            System.out.println("sdbc.ResultSetMetaData.isAutoIncrement() 1");
            return m_Metadata.isAutoIncrement(index);
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isCaseSensitive(int index) throws SQLException
    {
        try
        {
            System.out.println("sdbc.ResultSetMetaData.isCaseSensitive() 1");
            return m_Metadata.isCaseSensitive(index);
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isCurrency(int index) throws SQLException
    {
        try
        {
            System.out.println("sdbc.ResultSetMetaData.isCurrency() 1");
            return m_Metadata.isCurrency(index);
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isDefinitelyWritable(int index) throws SQLException
    {
        try
        {
            System.out.println("sdbc.ResultSetMetaData.isDefinitelyWritable() 1");
            return m_Metadata.isDefinitelyWritable(index);
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public int isNullable(int index) throws SQLException
    {
        try
        {
            System.out.println("sdbc.ResultSetMetaData.isNullable() 1");
            return m_Metadata.isNullable(index);
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isReadOnly(int index) throws SQLException
    {
        try
        {
            System.out.println("sdbc.ResultSetMetaData.isReadOnly() 1");
            return m_Metadata.isReadOnly(index);
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isSearchable(int index) throws SQLException
    {
        try
        {
            System.out.println("sdbc.ResultSetMetaData.isSearchable() 1");
            return m_Metadata.isSearchable(index);
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isSigned(int index) throws SQLException
    {
        try
        {
            System.out.println("sdbc.ResultSetMetaData.isSigned() 1");
            return m_Metadata.isSigned(index);
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    public boolean isWritable(int index) throws SQLException
    {
        try
        {
            System.out.println("sdbc.ResultSetMetaData.isWritable() 1");
            return m_Metadata.isWritable(index);
        } catch (java.sql.SQLException e)
        {
            throw UnoHelper.getSQLException(e, this);
        }
    }


}