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

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import com.sun.star.beans.XPropertySet;
import com.sun.star.sdbcx.KeyType;

import io.github.prrvchr.uno.driver.helper.StandardSQLState;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedComponent;

public final class KeyColumns
    extends ContainerBase<KeyColumn> {

    private static final String SERVICE = KeyColumns.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.KeyColumns",
                                              "com.sun.star.sdbcx.Container"};

    protected final Key mKey;

    // The constructor method:
    public KeyColumns(Key key,
                      String[] names,
                      boolean sensitive) {
        super(SERVICE, SERVICES, key, sensitive, names);
        mKey = key;
        System.out.println("KeyColumns() 1");
    }

    public ConnectionSuper getConnection() {
        return mKey.getTable().getConnection();
    }

    @Override
    protected void refreshInternal() {
        System.out.println("sdbcx.KeyContainer.refreshInternal() *********************************");
        mKey.refreshColumns();
    }

    @Override
    public void dispose() {
        //getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_KEYCOLUMNS_DISPOSING);
        super.dispose();
    }

    protected KeyColumn createElement(String name)
        throws SQLException {
        System.out.println("KeyColumns.createElement() 1");
        KeyColumn key = null;
        String refColumnName = "";
        final int PKCOLUMN_NAME = 4;
        final int FKCOLUMN_NAME = 8;
        final int FK_NAME = 12;
        try (java.sql.ResultSet result = getImportedKeyResultSet()) {
            while (result.next()) {
                if (name.equals(result.getString(FKCOLUMN_NAME)) &&
                    mKey.getName().equals(result.getString(FK_NAME))) {
                    refColumnName = result.getString(PKCOLUMN_NAME);
                    System.out.println("KeyColumns.createElement() FKColumnName: " + name +
                                       " - Key: " + mKey.getName() + " - refColumnName: " + refColumnName);
                    break;
                }
            }
            ColumnMain column, refColumn = null;
            column = (ColumnMain) mKey.getTable().getColumnsInternal().getElementByName(name);
            if (mKey.getTypeInternal() == KeyType.FOREIGN) {
                refColumn = mKey.getRefTableInternal().getColumnsInternal().getElementByName(refColumnName);
            }
            System.out.println("KeyColumns.createElement() 1 columnName: " + column.getName() +
                               " - refColumnName: '" + refColumnName + "'");
            key = new KeyColumn(column, refColumn);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException(e.getMessage(), StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
        return key;
    }

    @Override
    protected XPropertySet createDescriptor() {
        return new KeyColumnDescriptor(isCaseSensitive());
    }

    @Override
    protected KeyColumn appendElement(XPropertySet descriptor)
        throws SQLException {
        System.out.println("sdbcx.KeyColumns.appendElement() ******************************************");
        throw new SQLException("Cannot change a key's columns, please delete and re-create the key instead");
    }

    @Override
    protected void removeDataBaseElement(int index, String name) throws SQLException {
        System.out.println("sdbcx.KeyColumns.removeDataBaseElement() ***************");
        throw new SQLException("Cannot change a key's columns, please delete and re-create the key instead");
    }

    private java.sql.ResultSet getImportedKeyResultSet()
        throws SQLException {
        NamedComponent component = mKey.getTable().getNamedComponents();
        DatabaseMetaData metadata = getConnection().getProvider().getConnection().getMetaData();
        return metadata.getImportedKeys(component.getCatalog(), component.getSchema(), component.getTable());
    }

}
