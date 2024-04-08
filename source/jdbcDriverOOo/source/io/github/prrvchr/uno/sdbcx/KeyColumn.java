/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-24 https://prrvchr.github.io                                  ║
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
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.Type;

import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;


public final class KeyColumn
    extends ColumnBase<TableSuper<?>>
{
    private static final String m_service = KeyColumn.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.KeyColumn"};

    protected String m_RelatedColumn;

    // The constructor method:
    public KeyColumn(TableSuper<?> table,
                     final boolean sensitive,
                     final String name,
                     final String typename,
                     final String defaultvalue,
                     final String description,
                     final int nullable,
                     final int precision,
                     final int scale,
                     final int type,
                     final boolean autoincrement,
                     final boolean rowversion,
                     final boolean currency,
                     final String referenced)
    {
        super(m_service, m_services, table, sensitive, name, typename, defaultvalue, description,
              nullable, precision, scale, type, autoincrement, rowversion, currency);
        m_RelatedColumn = referenced;
        System.out.println("KeyColumn() RelatedColumn: " + referenced);
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


    @Override
    public XPropertySet createDataDescriptor() {
        KeyColumnDescriptor descriptor = new KeyColumnDescriptor(isCaseSensitive());
        synchronized (this) {
            UnoHelper.copyProperties(this, descriptor);
        }
        return descriptor;
    }

    // XXX: Called from KeyColumnContainer.rename(String oldname, String newname)
    protected void setName(String newname)
    {
        System.out.println("sdbcx.KeyColumn.rename() *************************************");
        // We need to rename the RelatedColumn too 
        setName(newname);
        m_RelatedColumn = newname;
    }

    // XXX: Called from KeyContainer.renameForeignKeyColumn(String oldname, String newname)
    protected void setRelatedColumn(String oldcolumn, String newcolumn)
    {
        System.out.println("sdbcx.KeyColumn.setRelatedColumn() *************************************");
        if (m_RelatedColumn.equals(oldcolumn)) {
            m_RelatedColumn = newcolumn;
        }
    }


}
