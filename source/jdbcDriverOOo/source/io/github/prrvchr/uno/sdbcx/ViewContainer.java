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

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.sdbc.ConnectionBase;


public class ViewContainer
    extends ContainerSuper<View>
{

    // The constructor method:
    public ViewContainer(ConnectionBase connection)
    {
        super(connection);
        refresh();
    }


    // com.sun.star.sdbcx.XDrop method of Container:
    protected String _getDropQuery(View view)
    {
        return m_Connection.getProvider().getDropViewQuery(m_Connection, view.m_CatalogName, view.m_SchemaName, view.m_Name);
    }



    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor() {
        System.out.println("sdbcx.ViewContainer.createDataDescriptor() ***************************");
        return null;
    }


    // com.sun.star.util.XRefreshable
    @Override
    public void refresh()
    {
        m_Names.clear();
        m_Elements.clear();
        try {
            java.sql.DatabaseMetaData metadata = m_Connection.getWrapper().getMetaData();
            String[] types = {"VIEW"};
            java.sql.ResultSet result = metadata.getTables(null, null, "%", types);
            String query = m_Connection.getProvider().getViewQuery();
            while (result.next())
            {
                String catalog = result.getString(1);
                String schema = result.getString(2);
                String name = result.getString(3);
                View view = new View(m_Connection, query, catalog, schema, name);
                m_Elements.add(view);
                m_Names.add(name);
            }
            result.close();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }


    // com.sun.star.sdbcx.XAppend
    @Override
    public void appendByDescriptor(XPropertySet descriptor)
        throws SQLException,
               ElementExistException
    {
        System.out.println("sdbcx.ViewContainer.appendByDescriptor() ****************************");
    }


}
