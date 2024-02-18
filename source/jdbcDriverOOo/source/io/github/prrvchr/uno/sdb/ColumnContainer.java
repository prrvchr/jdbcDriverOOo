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
package io.github.prrvchr.uno.sdb;

import java.util.List;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;


import io.github.prrvchr.jdbcdriver.DataBaseTableHelper.ColumnDescription;
import io.github.prrvchr.uno.sdbcx.ColumnContainerBase;
import io.github.prrvchr.uno.sdbcx.ColumnDescriptor;
import io.github.prrvchr.uno.sdbcx.TableBase;


public final class ColumnContainer
    extends ColumnContainerBase
{

    // The constructor method:
    public ColumnContainer(TableBase table,
                           boolean sensitive,
                           List<ColumnDescription> descriptions)
        throws ElementExistException
    {
        super(table, sensitive, descriptions);
    }

    @Override
    protected XPropertySet _createDescriptor() {
        return new ColumnDescriptor(m_table.getCatalogName(), m_table.getSchemaName(), m_table.getName(), isCaseSensitive());
    }

    protected Column _getColumn(String name,
                                String typename,
                                String defaultvalue,
                                String description,
                                int nullable,
                                int precision,
                                int scale,
                                int type,
                                boolean autoincrement,
                                boolean rowversion,
                                boolean currency)
    {
        return new Column(m_table, isCaseSensitive(), name, typename, defaultvalue, description, nullable, precision, scale, type, autoincrement, rowversion, currency);
    }

}
