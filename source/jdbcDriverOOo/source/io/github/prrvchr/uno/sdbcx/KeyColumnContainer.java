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

import io.github.prrvchr.uno.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.StandardSQLState;


public final class KeyColumnContainer
    extends ContainerSuper<KeyColumn> {
    private static final String SERVICE = IndexContainer.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.KeyColumns",
                                              "com.sun.star.sdbcx.Container"};

    protected final Key mKey;

    // The constructor method:
    public KeyColumnContainer(Key key,
                              List<String> columns)
        throws ElementExistException {
        super(SERVICE, SERVICES, key.getTable(), true, columns);
        mKey = key;
    }

    @Override
    protected KeyColumn createElement(String name)
        throws SQLException {
        KeyColumn column = null;
        final int PKCOLUMN_NAME = 4;
        final int FKCOLUMN_NAME = 8;
        final int FK_NAME = 12;
        try {
            Provider provider = getConnection().getProvider();
            NamedComponents table = mKey.getTable().getNamedComponents();
            String refColumnName = "";
            try (java.sql.ResultSet result = provider.getConnection().getMetaData().getImportedKeys(table.getCatalog(),
                                                                                                    table.getSchema(),
                                                                                                    table.getTable())) {
                while (result.next()) {
                    if (name.equals(result.getString(FKCOLUMN_NAME)) &&
                        mKey.getName().equals(result.getString(FK_NAME))) {
                        refColumnName = result.getString(PKCOLUMN_NAME);
                        break;
                    }
                }
            }
            column = createKey(provider, table, name, refColumnName);
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage(), this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, Any.VOID);
        }
        return column;
    }

    private KeyColumn createKey(Provider provider,
                                NamedComponents table,
                                String name,
                                String refColumnName) throws java.sql.SQLException {
        KeyColumn column = null;
        // now describe the column name and set its related column
        try (java.sql.ResultSet result = provider.getConnection().getMetaData().getColumns(table.getCatalog(),
                                                                                           table.getSchema(),
                                                                                           table.getTable(),
                                                                                           name)) {
            if (result.next()) {
                final int COLUMN_NAME = 4;
                final int DATA_TYPE = 5;
                final int TYPE_NAME = 6;
                final int COLUMN_SIZE = 7;
                final int DECIMAL_DIGITS = 9;
                final int NULLABLE = 11;
                final int COLUMN_DEF = 13;
                if (result.getString(COLUMN_NAME).equals(name)) {
                    int dataType = provider.getDataType(result.getInt(DATA_TYPE));
                    String typeName = result.getString(TYPE_NAME);
                    int size = result.getInt(COLUMN_SIZE);
                    int dec = result.getInt(DECIMAL_DIGITS);
                    int nul = result.getInt(NULLABLE);
                    String columnDef = "";
                    try {
                        columnDef = result.getString(COLUMN_DEF);
                    } catch (java.sql.SQLException e) {
                        // sometimes we get an error when asking for this param
                    }
                    column = new KeyColumn(table.getCatalog(), table.getSchema(), table.getTable(),
                                           isCaseSensitive(), name, typeName, "", columnDef,
                                           nul, size, dec, dataType, false, false, false, refColumnName);
                }
            }
        }
        return column;
    }

    @Override
    protected KeyColumn appendElement(XPropertySet descriptor)
        throws SQLException {
        System.out.println("sdbcx.KeyColumnContainer.appendElement() ******************************************");
        throw new SQLException("Cannot change a key's columns, please delete and re-create the key instead");
    }

    @Override
    protected void removeDataBaseElement(int index,
                                         String name)
        throws SQLException {
        System.out.println("sdbcx.KeyColumnContainer.removeDataBaseElement() ***************");
        throw new SQLException("Cannot change a key's columns, please delete and re-create the key instead");
    }

    
    @Override
    protected XPropertySet createDescriptor() {
        return new KeyColumnDescriptor(isCaseSensitive());
    }

    @Override
    protected void refreshInternal() {
    }

    protected void renameKeyColumn(String oldname, String newname)
        throws SQLException {
        System.out.println("KeyColumnContainer.renameKeyColumn() 1");
        if (hasByName(oldname)) {
            System.out.println("KeyColumnContainer.renameKeyColumn() 2");
            replaceElement(oldname, newname);
            System.out.println("KeyColumnContainer.renameKeyColumn() 3");
        }
        System.out.println("KeyColumnContainer.renameKeyColumn() 4");
    }

    public ConnectionSuper getConnection() {
        return mKey.getTable().getConnection();
    }

}
