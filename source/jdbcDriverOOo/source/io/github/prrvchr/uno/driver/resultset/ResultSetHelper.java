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
package io.github.prrvchr.uno.driver.resultset;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;

import javax.sql.RowSetMetaData;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import javax.sql.rowset.spi.SyncFactory;
import javax.sql.rowset.spi.SyncProvider;

import io.github.prrvchr.uno.driver.metadata.RowSetData;
import io.github.prrvchr.uno.helper.UnoHelper;


public class ResultSetHelper {

    static final String ROWSET_FACTORY = "io.github.prrvchr.java.rowset.RowSetFactoryImpl";
    static final String ROWSET_PROVIDER = "io.github.prrvchr.java.rowset.providers.OptimisticProvider";

    private static RowSetFactory sFactory = null;

    public static CachedRowSet getCachedRowSet() throws java.sql.SQLException {
        try {
            CachedRowSet rowset = getRowSetFactory().createCachedRowSet();
            rowset.setSyncProvider(ROWSET_PROVIDER);
            return rowset;
        } catch (Throwable e) {
            System.out.println("ResultSetHelper.getCachedRowSet() ERROR: " + UnoHelper.getStackTrace(e));
            throw e;
        }
    }

    public static void setDefaultColumnValues(ResultSet result, BitSet column)
        throws java.sql.SQLException {
        // XXX: On insert if we want to succeed, we need to set to NULL all
        // XXX: auto-increment and nullable columns that have not been updated
        ResultSetMetaData metadata = result.getMetaData();
        int count = metadata.getColumnCount();
        for (int index = 1; index <= count; index ++) {
            if (!column.get(index - 1)) {
                System.out.println("ResultSetHelper.setDefaultColumnValues() 1 Index: " + index);
                //boolean nullable = metadata.isNullable(index) == ResultSetMetaData.columnNullable;
                //if (nullable) {
                //    result.updateNull(index);
                //}
            }
        }
    }

    public static java.sql.ResultSet getCustomResultSet(java.sql.ResultSet result,
                                                        RowSetData... dataset)
        throws java.sql.SQLException {
        return getCustomResultSet(result, false,dataset);
    }

    public static java.sql.ResultSet getCustomResultSet(java.sql.ResultSet result,
                                                        boolean ignoreCurrency,
                                                        RowSetData... dataset)
        throws java.sql.SQLException {
        if (dataset.length > 0) {
            CachedRowSet rowset = null;
            int count = 0;
            for (RowSetData data : dataset) {
                if (data == null) {
                    continue;
                }
                if (rowset == null) {
                    rowset = getCachedRowSet();
                    rowset.populate(result);
                    result.close();
                    RowSetMetaData metadata = (RowSetMetaData) rowset.getMetaData();
                    count = metadata.getColumnCount();
                    if (ignoreCurrency) {
                        for (int i = 1; i <= count; i++) {
                            metadata.setCurrency(i, false);
                        }
                    }
                }
                if (rowset.size() > 0) {
                    setCustomRowSet(rowset, data, count);
                }
            }
            if (rowset != null && rowset.size() > 0) {
                rowset.setReadOnly(true);
                result = rowset;
            }
        }
        return result;
    }

    private static void setCustomRowSet(CachedRowSet rowset, RowSetData data, int count)
        throws java.sql.SQLException {
        List<String> usedKeys = new ArrayList<>();
        while (rowset.next()) {
            boolean updated = false;
            for (int index = 1; index <= count; index++) {
                String key = rowset.getString(index);
                if (rowset.wasNull() || !data.hasEntry(index, key)) {
                    continue;
                }
                key = RowSetData.getDataKey(index, key);
                System.out.println("ResultSetHelper.setCustomRowSet() 1 key: " + key);
                usedKeys.add(key);
                if (data.isDeletableRow(key)) {
                    System.out.println("ResultSetHelper.setCustomRowSet() 2");
                    rowset.deleteRow();
                    break;
                }
                updateRowSetData(rowset, data, key);
                updated = true;
            }
            if (updated) {
                rowset.setOriginalRow();
            }
        }
        //insertRowSetData(rowset, data, usedKeys);
        rowset.beforeFirst();
        System.out.println("ResultSetHelper.setCustomRowSet() 3");
    }

    @SuppressWarnings("unused")
    private static void insertRowSetData(CachedRowSet rowset,
                                         RowSetData data,
                                         List<String> usedKeys)
        throws java.sql.SQLException {
        System.out.println("ResultSetHelper.insertRowSetData() 1");
        for (Entry<Integer, List<String>> entry : data.getKeys().entrySet()) {
            Integer index = entry.getKey();
            for (String value : entry.getValue()) {
                String key = RowSetData.getDataKey(index, value);
                if (!usedKeys.contains(key)) {
                    System.out.println("ResultSetHelper.insertRowSetData() 2");
                    rowset.moveToInsertRow();
                    updateRowSetData(rowset, data, key);
                    rowset.insertRow();
                    rowset.moveToCurrentRow();
                    rowset.setOriginalRow();
                }
            }
        }
    }

    private static void updateRowSetData(CachedRowSet rowset,
                                         RowSetData data,
                                         String key)
        throws java.sql.SQLException {
        try {
            for (SimpleImmutableEntry<Integer, String> entry : data.getValue(key)) {
                int index = entry.getKey();
                String value = entry.getValue();
                if (value == null) {
                    rowset.updateNull(index);
                } else {
                    switch (rowset.getMetaData().getColumnType(index)) {
                        case Types.BIGINT:
                            rowset.updateLong(index, Long.valueOf(value));
                            break;
                        case Types.INTEGER:
                        case Types.SMALLINT:
                            rowset.updateInt(index, Integer.valueOf(value));
                            break;
                        case Types.TINYINT:
                            rowset.updateShort(index, Short.valueOf(value));
                            break;
                        case Types.BIT:
                        case Types.BOOLEAN:
                            rowset.updateBoolean(index, Boolean.valueOf(value));
                            break;
                        case Types.LONGVARCHAR:
                        case Types.VARCHAR:
                        case Types.CHAR:
                            rowset.updateString(index, value);
                            break;
                        case Types.DOUBLE:
                            rowset.updateDouble(index, Double.valueOf(value));
                            break;
                        case Types.FLOAT:
                            rowset.updateFloat(index, Float.valueOf(value));
                            break;
                        default:
                            rowset.updateObject(index, value);
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("ResultSetHelper.updateRowSetData() ERROR **********************");
            throw new java.sql.SQLException(e);
        }
    }

    private static RowSetFactory getRowSetFactory()
        throws java.sql.SQLException {
        if (sFactory == null) {
            SyncFactory.registerProvider(ROWSET_PROVIDER);
            sFactory = RowSetProvider.newFactory(ROWSET_FACTORY, null);
        }
        return sFactory;
    }

    @SuppressWarnings("unused")
    private static void initializeRowSetProvider() throws java.sql.SQLException {
        try {
            SyncProvider p = new io.github.prrvchr.java.rowset.providers.OptimisticProvider();
            // XXX: If we want to be able to access the RIOptimisticProvider implementation
            // XXX: then it is necessary to unregister RIOptimisticProvider
            //System.setProperty(SyncFactory.ROWSET_SYNC_PROVIDER, ROWSET_PROVIDER);
            List<String> providers = new ArrayList<>();
            Enumeration<SyncProvider> it = SyncFactory.getRegisteredProviders();
            while (it.hasMoreElements()) {
                providers.add(it.nextElement().getProviderID());
            }
            for (String provider : providers) {
                System.out.println("ResultSetHelper.initializeRowSetProvider() 1 Provider: " + provider);
                //SyncFactory.unregisterProvider(provider);
            }
            System.out.println("ResultSetHelper.initializeRowSetProvider() 2");
            /*Hashtable<String, String> env = new Hashtable<String, String>();
            System.out.println("ResultSetHelper.initializeRowSetProvider() 3");
            env.put(Context.INITIAL_CONTEXT_FACTORY, "CosNaming");
            env.put(SyncFactory.ROWSET_SYNC_PROVIDER, ROWSET_PROVIDER);
            InitialContext context = new InitialContext(env);
            context.bind ("io/github/prrvchr/java/rowset/providers/OptimisticProvider", p);
            SyncFactory.setJNDIContext(context);*/
            System.out.println("ResultSetHelper.initializeRowSetProvider() 4");
            SyncFactory.registerProvider(ROWSET_PROVIDER);
            System.out.println("ResultSetHelper.initializeRowSetProvider() 5");
        } catch (Throwable e) {
            System.out.println("ResultSetHelper.getRowSetFactory() ERROR: " + UnoHelper.getStackTrace(e));
        }
    }

}
