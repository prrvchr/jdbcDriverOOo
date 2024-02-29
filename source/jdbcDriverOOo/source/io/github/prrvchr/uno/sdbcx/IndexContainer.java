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

import java.util.ArrayList;
import java.util.List;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.XIndexAccess;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.IndexType;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.uno.helper.UnoHelper;


public final class IndexContainer
    extends Container<Index>
{
    private static final String m_service = IndexContainer.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.Indexes",
                                                "com.sun.star.sdbcx.Container"};

    private final TableSuper m_Table;

    // The constructor method:
    public IndexContainer(TableSuper table,
                          boolean sensitive,
                          List<String> indexes)
    throws ElementExistException
    {
        super(m_service, m_services, table, sensitive, indexes);
        m_Table = table;
    }


    @Override
    protected Index createElement(String name)
        throws SQLException
    {
        Index index = null;
        try {
            System.out.println("sdbcx.IndexContainer._createElement() 1: " + name);
            ConnectionSuper connection = m_Table.getConnection();
            if (connection == null) {
                return null;
            }
            System.out.println("sdbcx.IndexContainer._createElement() 2");
            String qualifier = "";
            String subname;
            int len = name.indexOf('.');
            if (len >= 0) {
                qualifier = name.substring(0, len);
                subname = name.substring(len + 1);
            }
            else {
                subname = name;
            }
            System.out.println("sdbcx.IndexContainer._createElement() 3: Name: " + name + " - Qualifier: " + qualifier + " - Subname: " + subname);
            java.sql.DatabaseMetaData metadata = connection.getProvider().getConnection().getMetaData();
            java.sql.ResultSet result = metadata.getIndexInfo(m_Table.getCatalog(), m_Table.getSchema(), m_Table.getName(), false, false);
            boolean found = false;
            boolean unique = false;
            int type = -1;
            boolean primary = false;
            List<String> columns = new ArrayList<>();
            while (result.next()) {
                unique  = !result.getBoolean(4);
                System.out.println("sdbcx.IndexContainer._createElement() 4 SubName: " + result.getString(6));
                if ((qualifier.isEmpty() || qualifier.equals(result.getString(5))) && subname.equals(result.getString(6))) {
                    System.out.println("sdbcx.IndexContainer._createElement() 5");
                    found = true;
                    type = result.getShort(7);
                    primary = isPrimaryKeyIndex(metadata, m_Table, subname);
                    String columnName = result.getString(9);
                    if (!result.wasNull()) {
                        System.out.println("sdbcx.IndexContainer._createElement() 6 ColumnName: " + columnName);
                        columns.add(columnName);
                    }
                }
            }
            if (found) {
                boolean clustered = type == IndexType.CLUSTERED;
                index = new Index(m_Table, isCaseSensitive(), subname, qualifier, unique, primary, clustered, columns);
                System.out.println("sdbcx.IndexContainer._createElement() 7");
            }
        }
        catch (ElementExistException e) {
            throw UnoHelper.getSQLException(e, m_Table);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, m_Table);
        }
        System.out.println("sdbcx.IndexContainer._createElement() 8");
        return index;
    }
    
    private static boolean isPrimaryKeyIndex(java.sql.DatabaseMetaData metadata,
                                             TableSuper table,
                                             String name)
        throws java.sql.SQLException
    {
        boolean primary = false;
        java.sql.ResultSet result = metadata.getPrimaryKeys(table.getCatalog(), table.getSchema(), table.getName());
        if (result.next()) { // there can be only one primary key
            primary = name.equals(result.getString(6));
        }
        return primary;
    }

    @Override
    public String getElementName(List<String> names,
                                  XPropertySet descriptor)
        throws SQLException, ElementExistException
    {
        String name = DBTools.getDescriptorStringValue(descriptor, PropertyIds.NAME);
        if (names.contains(name)) {
            throw new ElementExistException();
        }
        return name;
    }

    @Override
    protected Index appendElement(XPropertySet descriptor,
                                          String name)
        throws SQLException
    {
        Index index = null;
        if (createIndex(descriptor, name)) {
            index = createElement(name);
        }
        return index;
        
    }

    protected boolean createIndex(XPropertySet descriptor,
                                  String name)
        throws SQLException
    {
        try {
            if (_getConnection() == null) {
                return false;
            }
            String separator = ", ";
            boolean unique = DBTools.getDescriptorBooleanValue(descriptor, PropertyIds.ISUNIQUE);
            String table = DBTools.composeTableName(_getConnection().getProvider(), m_Table, ComposeRule.InIndexDefinitions, false, false, isCaseSensitive());
            XColumnsSupplier supplier = UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
            XIndexAccess columns = UnoRuntime.queryInterface(XIndexAccess.class, supplier.getColumns());
            List<String> indexes = new ArrayList<String>();
            for (int i = 0; i < columns.getCount(); i++) {
                XPropertySet column = (XPropertySet) AnyConverter.toObject(XPropertySet.class, columns.getByIndex(i));
                String index = DBTools.enquoteIdentifier(_getConnection().getProvider(), DBTools.getDescriptorStringValue(column, PropertyIds.NAME), isCaseSensitive());
                // FIXME: ::dbtools::getBooleanDataSourceSetting( m_pTable->getConnection(), "AddIndexAppendix" );
                String ascending = DBTools.getDescriptorBooleanValue(column, PropertyIds.ISASCENDING) ? " ASC" : " DESC";
                indexes.add(index + ascending);
            }
            if (!indexes.isEmpty()) {
                StringBuilder buffer = new StringBuilder("CREATE ");
                if (unique) buffer.append("UNIQUE ");
                buffer.append("INDEX ");
                if (!name.isEmpty()) {
                    buffer.append(DBTools.enquoteIdentifier(_getConnection().getProvider(), name, isCaseSensitive()));
                    buffer.append(" ON ");
                }
                else {
                    buffer.append("ON ");
                }
                buffer.append(table);
                buffer.append(" (");
                buffer.append(String.join(separator, indexes));
                buffer.append(")");
                return DBTools.executeDDLQuery(_getConnection().getProvider(), buffer.toString(), m_Table.getLogger(),
                                               "IndexContainer", "_appendElement", Resources.STR_LOG_INDEX_CREATE_QUERY);
            }
            return false;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        catch (WrappedTargetException | IndexOutOfBoundsException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    @Override
    protected void removeDataBaseElement(int index,
                                         String elementName)
        throws SQLException
    {
        if (_getConnection() == null) {
            return;
        }
        String name;
        @SuppressWarnings("unused")
        String schema = "";
        int len = elementName.indexOf('.');
        if (len >= 0) {
            schema = elementName.substring(0, len);
        }
        name = elementName.substring(len + 1);
        try {
            StringBuilder buffer = new StringBuilder("ALTER TABLE ");
            buffer.append(DBTools.composeTableName(_getConnection().getProvider(), m_Table, ComposeRule.InTableDefinitions, false, false, isCaseSensitive()));
            buffer.append(" DROP CONSTRAINT ");
            buffer.append(name);
            DBTools.executeDDLQuery(_getConnection().getProvider(), buffer.toString(), m_Table.getLogger(),
                                     "IndexContainer", "removeDataBaseElement", Resources.STR_LOG_INDEX_REMOVE_QUERY);
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
    }

    public ConnectionSuper _getConnection()
    {
        return m_Table.getConnection();
    }

    @Override
    protected void _refresh() {
        System.out.println("sdbcx.IndexContainer._refresh() *********************************");
        // FIXME
    }


    @Override
    protected XPropertySet createDescriptor() {
        return new IndexDescriptor(isCaseSensitive());
    }


/*    public IndexContainer(Connection connection,
                          TableBase table)
        throws SQLException
    {
        super(m_name, m_services, connection);
        m_Table = table;
        try {
            java.sql.ResultSet result = m_Connection.getWrapper().getMetaData().getIndexInfo(null, table.m_SchemaName, table.m_Name, false, true);
            while (result.next()) {
                boolean unique = !result.getBoolean(4);
                String catalog = result.getString(5);
                String name = result.getString(6);
                boolean primary = table.m_keys.hasNamedElement(name);
                int cluster = result.getShort(7);
                boolean clustered = result.wasNull() ? false : cluster == java.sql.DatabaseMetaData.tableIndexClustered;
                int position = result.getShort(8);
                String column = result.getString(9);
                String ascending = result.getString(10);
                boolean isascending = result.wasNull() ? false : ascending == "A";
                if (m_Names.contains(name)) {
                    m_Elements.get(m_Names.indexOf(name))._addColumn(new IndexColumn(m_Connection, table, isascending, column, position));
                }
                else {
                    Index index = new Index(m_Connection, table, name, catalog, unique, primary, clustered, isascending, column, position);
                    m_Elements.add(index);
                    m_Names.add(name);
                }
                System.out.println("sdbcx.IndexContainer.refresh() Column: " + column + " - Name: " + name);
            }
            result.close();
        }
        catch (UnknownPropertyException | WrappedTargetException | NoSuchElementException e) {
            System.out.println("sdbcx.IndexContainer.refresh() ERROR:\n" + UnoHelper.getStackTrace(e));
        }
        catch (java.sql.SQLException e) {
            System.out.println("sdbcx.IndexContainer.refresh() ERROR:\n" + UnoHelper.getStackTrace(e));
        }
        System.out.println("sdbcx.IndexContainer(): " + getCount());
    }
    public IndexContainer(Connection connection,
                          XNameAccess indexes)
        throws SQLException
    {
        super(m_name, m_services, connection);
        XEnumeration iter = ((XEnumerationAccess) UnoRuntime.queryInterface(XEnumerationAccess.class, indexes)).createEnumeration();
        System.out.println("sdbcx.ColumnContainer() 1");
        try {
            while (iter.hasMoreElements()) {
                XPropertySet descriptor = UnoRuntime.queryInterface(XPropertySet.class, iter.nextElement());
                System.out.println("sdbcx.ColumnContainer() 2"); 
                String name = (String) descriptor.getPropertyValue("Name");
                Index index = new Index(m_Connection, descriptor, name);
                m_Elements.add(index);
                m_Names.add(name);
            }
        }
        catch (NoSuchElementException | WrappedTargetException | UnknownPropertyException e) {
            UnoHelper.getSQLException(e, this);
        }
    }*/


}
