/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-25 https://prrvchr.github.io                                  ║
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

import java.util.List;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;

import io.github.prrvchr.uno.driver.helper.ColumnHelper.ColumnDescription;


public final class ColumnContainer
    extends ColumnContainerBase<Column> {
    private static final String SERVICE = ColumnContainer.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.Container"};

    // The constructor method:
    protected ColumnContainer(Table table,
                              boolean sensitive,
                              List<ColumnDescription> descriptions)
        throws ElementExistException {
        super(SERVICE, SERVICES, table, sensitive, descriptions);
    }

    @Override
    protected XPropertySet createDescriptor() {
        return new ColumnDescriptor(mTable.mCatalogName, mTable.mSchemaName, mTable.getName(), isCaseSensitive());
    }

    @Override
    protected Column getColumn(String name,
                               String typename,
                               String defaultvalue,
                               String description,
                               int nullable,
                               int precision,
                               int scale,
                               int type,
                               boolean autoincrement,
                               boolean rowversion,
                               boolean currency) {
        return new Column(mTable.getConnection(), mTable.mCatalogName,
                          mTable.mSchemaName, mTable.getName(), isCaseSensitive(),
                          name, typename, defaultvalue, description, nullable, precision,
                          scale, type, autoincrement, rowversion, currency);
    }
}
