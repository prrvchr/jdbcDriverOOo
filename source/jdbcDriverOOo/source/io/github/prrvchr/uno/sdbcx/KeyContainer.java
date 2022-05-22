/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020 https://prrvchr.github.io                                     ║
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.sun.star.container.XIndexAccess;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.uno.Type;
import com.sun.star.util.XRefreshListener;
import com.sun.star.util.XRefreshable;

import io.github.prrvchr.uno.sdbc.ConnectionBase;


public class KeyContainer<T extends Key>
    extends WeakBase
    implements XIndexAccess,
               XRefreshable
{

    protected final ConnectionBase m_Connection;
    private final List<Key> m_Elements = new ArrayList<Key>();
    protected final String m_CatalogName;
    protected final String m_SchemaName;
    protected final String m_TableName;
    private final Type m_type;


    // The constructor method:
    public KeyContainer(ConnectionBase connection,
                        String catalog,
                        String schema,
                        String table)
    {
        this(connection, catalog, schema, table, "com.sun.star.beans.XPropertySet");
    }
    public KeyContainer(ConnectionBase connection,
                        String catalog,
                        String schema,
                        String table,
                        String type)
    {
        super();
        m_Connection = connection;
        m_CatalogName = catalog;
        m_SchemaName = schema;
        m_TableName = table;
        m_type = new Type(type);
        refresh();
        System.out.println("sdbcx.KeyContainer() ************************************ : " + getCount());
    }

    // com.sun.star.container.XElementAccess:
    @Override
    public Type getElementType()
    {
        return m_type;
    }

    @Override
    public boolean hasElements()
    {
        return !m_Elements.isEmpty();
    }


    // com.sun.star.container.XIndexAccess:
    @Override
    public Object getByIndex(int index)
        throws IndexOutOfBoundsException, WrappedTargetException
    {
        if (index < getCount()) {
            return m_Elements.get(index);
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int getCount()
    {
        return m_Elements.size();
    }


    // com.sun.star.util.XRefreshable
    @Override
    public void refresh() {
        try {
            m_Elements.clear();
            java.sql.ResultSet result = m_Connection.getWrapper().getMetaData().getPrimaryKeys(null, m_SchemaName, m_TableName);
            while (result.next()) {
                String column = result.getString(4);
                String name = result.getString(6);
                Key key = new Key(m_Connection, m_CatalogName, m_SchemaName, m_TableName, column, name, 1, "", 0, 0);
                m_Elements.add(key);
            }
            result.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Override
    public void addRefreshListener(XRefreshListener listener)
    {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.KeyContainer.addRefreshListener() ****************************************");
    }
    @Override
    public void removeRefreshListener(XRefreshListener listener)
    {
        // TODO Auto-generated method stub
        System.out.println("sdbcx.KeyContainer.removeRefreshListener() ****************************************");
    }


}
