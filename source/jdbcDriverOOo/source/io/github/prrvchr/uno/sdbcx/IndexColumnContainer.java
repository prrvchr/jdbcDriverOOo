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
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.Any;

import io.github.prrvchr.uno.driver.config.ConfigSQL;
import io.github.prrvchr.uno.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.uno.driver.provider.StandardSQLState;


public final class IndexColumnContainer
    extends ContainerSuper<IndexColumn> {
    private static final String SERVICE = IndexColumnContainer.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.IndexColumns",
                                              "com.sun.star.sdbcx.Container"};

    private final Index mIndex;

    // The constructor method:
    // XXX: - io.github.prrvchr.uno.sdbcx.IndexDescriptor()
    public IndexColumnContainer(Index index,
                                List<String> columns)
        throws ElementExistException {
        super(SERVICE, SERVICES, index.getTable(), true, columns);
        mIndex = index;
        System.out.println("sdbcx.IndexColumnContainer() Count: " + getCount());
    }

    @Override
    protected XPropertySet createDescriptor() {
        return new IndexColumnDescriptor(isCaseSensitive());
    }
    
    @Override
    protected IndexColumn createElement(String name)
        throws SQLException {
        IndexColumn index = null;
        boolean isascending = true;
        final int COLUMN_NAME = 9;
        final int ASC_OR_DESC = 10;
        NamedComponents table = mIndex.getTable().getNamedComponents();
        try {
            java.sql.DatabaseMetaData metadata = getConnection().getProvider().getConnection().getMetaData();
            ConfigSQL config = getConnection().getProvider().getConfigSQL();
            try (java.sql.ResultSet result = metadata.getIndexInfo(config.getMetaDataIdentifier(table.getCatalog()),
                                                                   config.getMetaDataIdentifier(table.getSchema()),
                                                                   config.getMetaDataIdentifier(table.getTable()),
                                                                   false, false)) {
                while (result.next()) {
                    if (name.equals(result.getString(COLUMN_NAME))) {
                        isascending = !"D".equals(result.getString(ASC_OR_DESC));
                    }
                }
            }
            index = createIndex(metadata, table, name, isascending);
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
        return index;
    }

    private IndexColumn createIndex(java.sql.DatabaseMetaData metadata,
                                    NamedComponents table,
                                    String name,
                                    boolean isascending) throws java.sql.SQLException {
        IndexColumn index = null;
        final int COLUMN_NAME = 4;
        final int DATA_TYPE = 5;
        final int TYPE_NAME = 6;
        final int COLUMN_SIZE = 7;
        final int DECIMAL_DIGITS = 9;
        final int NULLABLE = 11;
        final int COLUMN_DEF = 13;
        try (java.sql.ResultSet result = metadata.getColumns(table.getCatalog(),
                                                             table.getSchema(),
                                                             table.getTable(),
                                                             name)) {
            while (result.next()) {
                if (name.equals(result.getString(COLUMN_NAME))) {
                    int datatype = getConnection().getProvider().getDataType(result.getInt(DATA_TYPE));
                    String typename = result.getString(TYPE_NAME);
                    int precision = result.getInt(COLUMN_SIZE);
                    int scale = result.getInt(DECIMAL_DIGITS);
                    if (result.wasNull()) {
                        scale = 0;
                    }
                    int nullable = result.getInt(NULLABLE);
                    String defaultvalue = result.getString(COLUMN_DEF);
                    if (result.wasNull()) {
                        defaultvalue = "";
                    }
                    index = new IndexColumn(table.getCatalog(), table.getSchema(), table.getTable(),
                                            isCaseSensitive(), name, typename, defaultvalue, "",
                                            nullable, precision, scale, datatype,
                                            false, false, false, isascending);
                    break;
                }
            }
        }
        return index;
    }

    @Override
    protected void refreshInternal() {
        System.out.println("sdbcx.IndexColumnContainer.refreshInternal() *********************************");
        // FIXME
    }

    @Override
    protected IndexColumn appendElement(XPropertySet descriptor)
        throws SQLException {
        System.out.println("sdbcx.IndexColumnContainer.appendElement() *********************************");
        throw new SQLException("Unsupported");
    }

    @Override
    protected void removeDataBaseElement(int index,
                                         String name)
        throws SQLException {
        System.out.println("sdbcx.IndexColumnContainer.removeDataBaseElement() *********************************");
        throw new SQLException("Unsupported");
    }

    protected ConnectionSuper getConnection() {
        return mIndex.getTable().getConnection();
    }

    protected void renameIndexColumn(String oldname, String newname)
        throws SQLException {
        if (hasByName(oldname)) {
            replaceElement(oldname, newname);
        }
    }

}
