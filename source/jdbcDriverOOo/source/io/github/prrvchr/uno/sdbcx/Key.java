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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.XNameAccess;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.sdbcx.XDataDescriptorFactory;
import com.sun.star.uno.Type;

import io.github.prrvchr.driver.provider.PropertyIds;
import io.github.prrvchr.uno.helper.PropertyWrapper;


public final class Key
    extends Descriptor
    implements XColumnsSupplier,
               XDataDescriptorFactory {

    private static final String SERVICE = Key.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdbcx.Key"};
    public int mType;
    protected int mDeleteRule;
    protected String mReferencedTable;
    protected KeyColumnContainer mColumns = null;
    protected int mUpdateRule;
    private final TableSuper mTable;

    // The constructor method:
    public Key(TableSuper table,
               boolean sensitive,
               String name,
               String reference,
               int type,
               int update,
               int delete,
               List<String> columns)
        throws ElementExistException {
        super(SERVICE, SERVICES, sensitive, name);
        System.out.println("sdbcx.Key() 1");
        mTable = table;
        mType = type;
        mReferencedTable = reference;
        mUpdateRule = update;
        mDeleteRule = delete;
        mColumns = new KeyColumnContainer(this, columns);
        registerProperties();
    }

    private void registerProperties() {
        Map<String, PropertyWrapper> properties = new HashMap<String, PropertyWrapper>();
        short readonly = PropertyAttribute.READONLY;

        properties.put(PropertyIds.DELETERULE.getName(),
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return mDeleteRule;
                },
                null));

        properties.put(PropertyIds.REFERENCEDTABLE.getName(),
            new PropertyWrapper(Type.STRING, readonly,
                () -> {
                    return mReferencedTable;
                },
                null));

        properties.put(PropertyIds.TYPE.getName(),
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return mType;
                },
                null));

        properties.put(PropertyIds.UPDATERULE.getName(),
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    return mUpdateRule;
                },
                null));

        super.registerProperties(properties);
    }

    protected KeyColumnContainer getColumnsInternal() {
        return mColumns;
    }


    // com.sun.star.sdbcx.XColumnsSupplier
    @Override
    public XNameAccess getColumns() {
        return getColumnsInternal();
    }


/*    protected void _addColumn(KeyColumn column) {
        int index = 0;
        for (KeyColumn element : m_columns.m_Elements) {
            if (column.m_position < element.m_position) {
                m_columns.m_Elements.add(index, column);
                m_columns.m_Names.add(index, column.m_Name);
                break;
            }
            if (column.m_position > element.m_position) {
                m_columns.m_Elements.add(index + 1, column);
                m_columns.m_Names.add(index + 1, column.m_Name);
                break;
            }
            index++;
        }
    }*/


    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor() {
        return new KeyDescriptor(isCaseSensitive());
    }

    public TableSuper getTable() {
        return mTable;
    }

    @Override
    public String toString() {
        return String.format("%s: Name: %s, Type: %s, ReferencedTable: %s, UpdateRule: %s, DeleteRule: %s ",
                             this.getClass().getName(), getName(), mType, mReferencedTable, mUpdateRule, mDeleteRule);
    }
}
