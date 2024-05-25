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
package io.github.prrvchr.rowset.factory;

import java.sql.SQLException;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.FilteredRowSet;
import javax.sql.rowset.JdbcRowSet;
import javax.sql.rowset.JoinRowSet;
import javax.sql.rowset.RowSetProvider;
import javax.sql.rowset.WebRowSet;

import javax.sql.rowset.spi.SyncFactory;


public class RowSetFactory 
    implements javax.sql.rowset.RowSetFactory
{

    final static String CUSTOM_ROWSET_FACTORY = "io.github.prrvchr.rowset.RowSetFactoryImpl";
    final static String CUSTOM_SYNC_PROVIDER  = "io.github.prrvchr.rowset.providers.RIOptimisticProvider";
    final static String DEFAULT_SYNC_PROVIDER = "com.sun.rowset.providers.RIOptimisticProvider";

    @Override
    public CachedRowSet createCachedRowSet()
        throws SQLException
    {
        System.out.println("RowSetFactory.createCachedRowSet()");
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        CachedRowSet rowset = getRowSetProvider().createCachedRowSet();
        Thread.currentThread().setContextClassLoader(context);
        return rowset;
    }

    @Override
    public FilteredRowSet createFilteredRowSet()
        throws SQLException
    {
        return getRowSetProvider().createFilteredRowSet();
    }

    @Override
    public JdbcRowSet createJdbcRowSet()
        throws SQLException
    {
        return getRowSetProvider().createJdbcRowSet();
    }

    @Override
    public JoinRowSet createJoinRowSet()
        throws SQLException
    {
        return getRowSetProvider().createJoinRowSet();
    }

    @Override
    public WebRowSet createWebRowSet()
        throws SQLException
    {
        return getRowSetProvider().createWebRowSet();
    }

    private javax.sql.rowset.RowSetFactory getRowSetProvider()
        throws SQLException
    {
        // XXX: As the jdbcDriverOOo extension is loaded using an URL ClassLoader by LibreOffice
        // XXX: it is necessary to modify the current Thread classLoader accordingly...
        ClassLoader loader = RowSetFactory.class.getClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        // XXX: If we want the RIOptimisticProvider to be loaded with the correct
        // XXX: classloader we must first unregister the RIOptimisticProvider.
        SyncFactory.unregisterProvider(DEFAULT_SYNC_PROVIDER);
        SyncFactory.registerProvider(CUSTOM_SYNC_PROVIDER);
        return RowSetProvider.newFactory(CUSTOM_ROWSET_FACTORY, loader);
    }

}
