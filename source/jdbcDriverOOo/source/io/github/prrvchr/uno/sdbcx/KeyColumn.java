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
package io.github.prrvchr.uno.sdbcx;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.sdb.Connection;
import io.github.prrvchr.uno.sdbc.PropertyIds;
import schemacrawler.schema.TableConstraintColumn;


public class KeyColumn
    extends ColumnBase
{
    private static final String m_name = KeyColumn.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.KeyColumn",
                                                "com.sun.star.sdbcx.Column"};
    private final String m_RelatedColumn;

    // The constructor method:
    public KeyColumn(Connection connection,
                     XPropertySet descriptor,
                     String name,
                     int position)
        throws java.sql.SQLException, UnknownPropertyException, WrappedTargetException
    {
        super(m_name, m_services, connection, descriptor, name, position);
        m_RelatedColumn = name;
        registerProperties();
    }


    public KeyColumn(Connection connection,
                     TableBase<?> table,
                     String name,
                     int position)
        throws java.sql.SQLException, UnknownPropertyException, WrappedTargetException, NoSuchElementException
    {
        super(m_name, m_services, connection, table, name, position);
        m_RelatedColumn = name;
        registerProperties();
    }
    public KeyColumn(Connection connection,
                     java.sql.ResultSet result,
                     String name,
                     int position)
        throws java.sql.SQLException
    {
        super(m_name, m_services, connection, result, name, position);
        m_RelatedColumn = name;
        registerProperties();
    }

    public KeyColumn(Connection connection,
                     TableConstraintColumn column)
    {
        super(m_name, m_services, connection, column, column.getName());
        m_RelatedColumn = "";
        registerProperties();
    }

    private void registerProperties() {
        short readonly = PropertyAttribute.READONLY;
        registerProperty(PropertyIds.RELATEDCOLUMN.name, PropertyIds.RELATEDCOLUMN.id, Type.STRING, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_RelatedColumn;
                }
            }, null);
    }


}
