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

import java.util.HashMap;
import java.util.Map;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameAccess;
import com.sun.star.sdbcx.KeyType;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.sdbcx.XDataDescriptorFactory;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedComponent;
import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.driver.property.PropertyWrapper;
import io.github.prrvchr.uno.driver.helper.KeyHelper;
import io.github.prrvchr.uno.driver.provider.Provider;


public final class Key
    extends Descriptor
    implements XColumnsSupplier,
               XDataDescriptorFactory {

    private static final String SERVICE = Key.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.Key"};

    private KeyColumns mColumns = null;
    private final TableSuper mTable;
    private int mType;
    private int mUpdateRule;
    private int mDeleteRule;
    private String mReferencedTable;
    private TableSuper mRefTable;

    //private ColumnListener<KeyColumn> mListener;
    private final String[] mNames;

    // The constructor method:
    public Key(TableSuper table,
               TableSuper refTable,
               boolean sensitive,
               String name,
               int type,
               int update,
               int delete,
               String[] columns) {
        super(SERVICE, SERVICES, sensitive, name);
        mTable = table;
        mRefTable = refTable;
        mType = type;
        mUpdateRule = update;
        mDeleteRule = delete;
        mNames = columns;
        registerProperties();
    }

    private void registerProperties() {
        Map<PropertyID, PropertyWrapper> properties = new HashMap<PropertyID, PropertyWrapper>();
        short readonly = PropertyAttribute.READONLY;

        properties.put(PropertyID.DELETERULE,
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return mDeleteRule;
                },
                null));

        properties.put(PropertyID.REFERENCEDTABLE,
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return getReferencedTable();
                },
                null));

        properties.put(PropertyID.TYPE,
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return mType;
                },
                null));

        properties.put(PropertyID.UPDATERULE,
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return mUpdateRule;
                },
                null));

        super.registerProperties(properties);
    }

    // com.sun.star.lang.XComponent
    @Override
    public void dispose() {
        if (mColumns != null) {
            mColumns.dispose();
        }
        super.dispose();
    }

    // com.sun.star.sdbcx.XColumnsSupplier
    @Override
    public XNameAccess getColumns() {
        checkDisposed();
        return getColumnsInternal();
    }

    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor() {
        return new KeyDescriptor(isCaseSensitive());
    }

    protected TableSuper getTable() {
        return mTable;
    }

    @Override
    public String toString() {
        return String.format("%s: Name: %s, Type: %s, ReferencedTable: %s, UpdateRule: %s, DeleteRule: %s ",
                             this.getClass().getName(), getName(), mType, mReferencedTable, mUpdateRule, mDeleteRule);
    }

    // Protected methods
    protected boolean isColumnsLoaded() {
        return mColumns != null;
    }

    protected KeyColumns getColumnsInternal() {
        if (mColumns == null) {
            refreshColumns();
        }
        return mColumns;
    }

    protected KeyColumns refreshColumns() {
        if (mColumns == null) {
            //getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_TABLES);
            System.out.println("sdbcx.Key.getColumnsInternal() Columns: " + String.join(", ", mNames));
            mColumns = new KeyColumns(this, mNames, isCaseSensitive());
            //getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_TABLES_ID,
            //                   mTables.getLogger().getObjectId());
        } else {
            mColumns.refill(getKeyColumns());
        }
        return mColumns;
    }

    protected int getTypeInternal() {
        return mType;
    }

    protected String getReferencedTableInternal() {
        return getReferencedTable();
    }

    protected TableSuper getRefTableInternal() {
        return mRefTable;
    }

    private String getReferencedTable() {
        String referencedTable = "";
        if (mRefTable != null) {
            referencedTable = mRefTable.composeTableName();
        }
        return referencedTable;
    }

    private String[] getKeyColumns() {
        String[] columns;
        Provider provider = mTable.getConnection().getProvider();
        NamedComponent component = mTable.getNamedComponents();
        if (getTypeInternal() == KeyType.PRIMARY) {
            columns = KeyHelper.getPrimaryKeyColumns(provider, component, getName());
        } else {
            columns = KeyHelper.getForeignKeyColumns(provider, component, getName());
        }
        return columns;
    }

}
