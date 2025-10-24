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

import com.sun.star.beans.XPropertySet;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.driver.config.ConfigSQL;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedComponent;

public final class IndexColumns
    extends ContainerBase<IndexColumn> {

    private static final String SERVICE = IndexColumns.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.IndexColumns",
                                              "com.sun.star.sdbcx.Container"};

    protected final Index mIndex;

    // The constructor method:
    public IndexColumns(Index index,
                        String[] names,
                        boolean sensitive) {
        super(SERVICE, SERVICES, index, sensitive, names);
        mIndex = index;
        System.out.println("IndexColumns() 1");
    }

    public ConnectionSuper getConnection() {
        return mIndex.getTable().getConnection();
    }

    @Override
    protected void refreshInternal() {
        System.out.println("sdbcx.IndexColumns.refreshInternal() *********************************");
        mIndex.refreshColumns();
    }

    @Override
    public void dispose() {
        //getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_INDEXCOLUMNS_DISPOSING);
        super.dispose();
    }

    @Override
    protected XPropertySet createDescriptor() {
        System.out.println("sdbcx.IndexColumnContainer.createDescriptor() *********************************");
        return new IndexColumnDescriptor(isCaseSensitive());
    }

    protected IndexColumn createElement(String name)
        throws java.sql.SQLException {
        IndexColumn index = null;
        ContainerBase<?> columns = mIndex.getTable().getColumnsInternal();
        if (columns.hasByName(name)) {
            boolean isascending = true;
            final int COLUMN_NAME = 9;
            final int ASC_OR_DESC = 10;
            NamedComponent component = mIndex.getTable().getNamedComponents();
            ConfigSQL config = getConnection().getProvider().getConfigSQL();
            String catalog = config.getMetaDataIdentifier(component.getCatalog());
            String schema = config.getMetaDataIdentifier(component.getSchema());
            String table = config.getMetaDataIdentifier(component.getTable());
            java.sql.DatabaseMetaData metadata = getConnection().getProvider().getConnection().getMetaData();
            try (java.sql.ResultSet result = metadata.getIndexInfo(catalog, schema, table, false, false)) {
                while (result.next()) {
                    if (name.equals(result.getString(COLUMN_NAME))) {
                        isascending = !"D".equals(result.getString(ASC_OR_DESC));
                    }
                }
            }
            ColumnMain column = (ColumnMain) columns.getElementByName(name);
            index = new IndexColumn(column, isascending);
        }
        return index;
    }

    @SuppressWarnings("unused")
    private IndexColumn createIndex(java.sql.DatabaseMetaData metadata,
                                    NamedComponent table,
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
                    index = new IndexColumn(isCaseSensitive(), name, typename, defaultvalue, "",
                                            nullable, precision, scale, datatype,
                                            false, false, false, isascending);
                    break;
                }
            }
        }
        return index;
    }

    public void renameIndexColumn(String oldname, String newname) throws SQLException {
        if (hasByName(oldname)) {
            replaceElement(oldname, newname, false);
        }
    }

    @Override
    protected IndexColumn appendElement(XPropertySet descriptor) throws java.sql.SQLException {
        System.out.println("sdbcx.IndexColumnContainer.appendElement() *********************************");
        throw new java.sql.SQLException("Unsupported");
    }

    @Override
    protected void removeDataBaseElement(int index, String name) throws java.sql.SQLException {
        System.out.println("sdbcx.IndexColumnContainer.removeDataBaseElement() *********************************");
        throw new java.sql.SQLException("Unsupported");
    }

}
