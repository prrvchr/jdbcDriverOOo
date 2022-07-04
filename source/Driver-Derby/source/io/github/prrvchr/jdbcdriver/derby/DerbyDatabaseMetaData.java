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
package io.github.prrvchr.jdbcdriver.derby;

import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;


public final class DerbyDatabaseMetaData
    extends DatabaseMetaDataBase
{

    // The constructor method:
    public DerbyDatabaseMetaData(final ConnectionBase connection)
        throws java.sql.SQLException
    {
        super(connection);
        System.out.println("derby.DerbyDatabaseMetaData() 1");
    }

    @Override
    public String getCatalogSeparator() throws SQLException
    {
        try {
            System.out.println("derby.DerbyDatabaseMetaData.getCatalogSeparator() 1 ");
            String value = m_Metadata.getCatalogSeparator();
            System.out.println("derby.DerbyDatabaseMetaData.getCatalogSeparator() 2: '" + value + "'");
            return ".";
        }
        catch (java.sql.SQLException e) {
            System.out.println("derby.DerbyDatabaseMetaData.getCatalogSeparator() ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
    }

    //@Override
    public boolean supportsCatalogsInDataManipulation1() throws SQLException
    {
        System.out.println("derby.DatabaseMetaData.supportsCatalogsInDataManipulation() 1");
        boolean value = false;
        try {
            if (m_Connection.isEnhanced()) {
                value = m_Metadata.supportsSchemasInDataManipulation();
            }
        }
        catch (java.sql.SQLException e)
        {
            System.out.println("derby.DatabaseMetaData.supportsCatalogsInDataManipulation() ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        System.out.println("derby.DatabaseMetaData.supportsCatalogsInDataManipulation() 2: " + value);
        return value;
    }

    //@Override
    public boolean supportsSchemasInDataManipulation1() throws SQLException
    {
        System.out.println("derby.DatabaseMetaData.supportsSchemasInDataManipulation() 1 : " + m_Connection.isEnhanced());
        boolean value = false;
        try {
            if (m_Connection.isEnhanced()) {
                value = m_Metadata.supportsSchemasInDataManipulation();
            }
        }
        catch (java.sql.SQLException e)
        {
            System.out.println("derby.DatabaseMetaData.supportsSchemasInDataManipulation() ********************************* ERROR: " + e);
            throw UnoHelper.getSQLException(e, this);
        }
        System.out.println("derby.DatabaseMetaData.supportsSchemasInDataManipulation() 2: " + value);
        return value;
    }

    @Override
    protected String _mapDatabaseTableTypes(String type)
    {
        return type;
    }

    @Override
    protected String _mapDatabaseTableType(String schema,
                                           String type)
    {
        return type;
    }

    @Override
    protected int _getDataType(int type)
    {
        return type;
    }


}