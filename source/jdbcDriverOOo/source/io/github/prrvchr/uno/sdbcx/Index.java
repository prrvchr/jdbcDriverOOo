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
import java.util.HashMap;
import java.util.Map;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameAccess;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.sdbcx.XDataDescriptorFactory;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedComponent;
import io.github.prrvchr.uno.driver.helper.DBTools;
import io.github.prrvchr.uno.driver.helper.IndexHelper;
import io.github.prrvchr.uno.driver.helper.IndexHelper.IndexProperties;
import io.github.prrvchr.uno.driver.provider.PropertyIds;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.UnoHelper;


public final class Index
    extends Descriptor
    implements XColumnsSupplier,
               XDataDescriptorFactory {

    private static final String SERVICE = Index.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.Index"};

    protected IndexColumns mColumns = null;
    protected final TableSuper mTable;
    protected String mCatalog;
    protected boolean mIsUnique;
    protected boolean mIsPrimaryKeyIndex;
    protected boolean mIsClustered;

    //private ColumnListener<IndexColumn> mListener;
    private final String[] mNames;

    // The constructor method:
    public Index(TableSuper table,
                 boolean sensitive,
                 String name,
                 String catalog,
                 boolean unique,
                 boolean primarykey,
                 boolean clustered,
                 String[] columns) {
        super(SERVICE, SERVICES, sensitive, name);
        System.out.println("sdbcx.Index() 1");
        mTable = table;
        mCatalog = catalog;
        mIsUnique = unique;
        mIsPrimaryKeyIndex = primarykey;
        mIsClustered = clustered;
        mNames = columns;
        //mColumns = new IndexColumns(this, columns, sensitive);
        registerProperties();
    }

    private void registerProperties() {
        Map<String, PropertyWrapper> properties = new HashMap<String, PropertyWrapper>();
        short readonly = PropertyAttribute.READONLY;

        properties.put(PropertyIds.CATALOG.getName(),
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return mCatalog;
                },
                null));

        properties.put(PropertyIds.ISCLUSTERED.getName(),
            new PropertyWrapper(Type.BOOLEAN, readonly,
                () -> {
                    return mIsClustered;
                },
                null));

        properties.put(PropertyIds.ISPRIMARYKEYINDEX.getName(),
            new PropertyWrapper(Type.BOOLEAN, readonly,
                () -> {
                    return mIsPrimaryKeyIndex;
                },
                null));

        properties.put(PropertyIds.ISUNIQUE.getName(),
            new PropertyWrapper(Type.BOOLEAN, readonly,
                () -> {
                    return mIsUnique;
                },
                null));

        super.registerProperties(properties);
    }

    protected TableSuper getTable() {
        return mTable;
    }

    // com.sun.star.lang.XComponent
    @Override
    public void dispose() {
        if (mColumns != null) {
            //if (mListener != null) {
            //    mTable.getColumnsInternal().removeContainerListener(mListener);
            //}
            mColumns.dispose();
        }
        super.dispose();
    }

    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor() {
        System.out.println("sdbcx.Index.createDataDescriptor() ***************************************************");
        IndexDescriptor descriptor = new IndexDescriptor(isCaseSensitive());
        UnoHelper.copyProperties(this, descriptor);
        try {
            DBTools.cloneDescriptorColumns(this, descriptor);
        } catch (java.sql.SQLException e) {
        }
        return descriptor;
    }

    // com.sun.star.sdbcx.XColumnsSupplier
    @Override
    public XNameAccess getColumns() {
        checkDisposed();
        return getColumnsInternal();
    }

    // Protected methods
    protected boolean isColumnsLoaded() {
        return mColumns != null;
    }

    protected IndexColumns getColumnsInternal() {
        if (mColumns == null) {
            refreshColumns();
        }
        return mColumns;
    }

    protected synchronized IndexColumns refreshColumns() {
        if (mColumns == null) {
            //getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATE_TABLES);
            mColumns = new IndexColumns(this, mNames, isCaseSensitive());
            //getLogger().logprb(LogLevel.FINE, Resources.STR_LOG_CREATED_TABLES_ID,
            //                   mTables.getLogger().getObjectId());
        } else {
            mColumns.refill(getIndexColumns());
        }
        return mColumns;
    }

    private String[] getIndexColumns() {
        String[] columns = null;
        try {
            Provider provider = mTable.getConnection().getProvider();
            NamedComponent component = mTable.getNamedComponents();
            DatabaseMetaData metadata;
            metadata = provider.getConnection().getMetaData();
            IndexProperties properties = IndexHelper.getIndexProperties(provider.getConfigSQL(),
                                                                        metadata, component, mCatalog, getName());
            columns = properties.getColumns();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return columns;
    }

}
