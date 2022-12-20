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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.XNameAccess;
import com.sun.star.sdbc.ColumnValue;
import com.sun.star.sdbc.DataType;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.UnoRuntime;

import io.github.prrvchr.uno.helper.ComposeRule;
import io.github.prrvchr.uno.helper.DataBaseTableHelper;
import io.github.prrvchr.uno.helper.DataBaseTools;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.helper.DataBaseTableHelper.ColumnDescription;
import io.github.prrvchr.uno.sdbc.ConnectionSuper;


public class ColumnContainer
    extends Container
{

    private Map<String, ColumnDescription> m_descriptions = new HashMap<>();
    private Map<String, ExtraColumnInfo> m_extrainfos = new HashMap<>();
    private final TableBase m_table;


    // The constructor method:
    public ColumnContainer(TableBase table,
                           Object lock,
                           boolean sensitive,
                           List<ColumnDescription> descriptions)
        throws ElementExistException
    {
        super(lock, sensitive, toColumnNames(descriptions));
        m_table = table;
        for (ColumnDescription description : descriptions) {
            m_descriptions.put(description.columnName, description);
        }

        System.out.println("sdbcx.ColumnContainer(): Column.getCount(): " + getCount());
    }

    private static List<String> toColumnNames(List<ColumnDescription> columns)
    {
        List<String> columnNames = new ArrayList<>(columns.size());
        for (ColumnDescription columnDescription : columns) {
            columnNames.add(columnDescription.columnName);
        }
        return columnNames;
    }


    @Override
    protected XPropertySet _appendElement(XPropertySet descriptor,
                                          String name)
        throws SQLException
    {
        if (m_table == null) {
            return _cloneDescriptor(descriptor);
        }
        try {
            String sql = String.format("ALTER TABLE %s ADD %s",
                                       DataBaseTools.composeTableName(_getConnection(), m_table, ComposeRule.InTableDefinitions, false, false, true),
                                       DataBaseTools.getStandardColumnPartQuery(_getConnection(), descriptor, null, m_table.getTypeCreatePattern()));
            java.sql.Statement statement = _getConnection().getProvider().getConnection().createStatement();
            statement.execute(sql);
            statement.close();
            return _createElement(name);
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, this);
        }
        return null;
    }


    @Override
    protected Column _createElement(String name)
        throws SQLException
    {
        Column column = null;
        try {
            @SuppressWarnings("unused")
            boolean queryInfo = true;
            boolean isAutoIncrement = false;
            boolean isCurrency = false;
            @SuppressWarnings("unused")
            int dataType = DataType.OTHER;
            
            ColumnDescription columnDescription = m_descriptions.get(name);
            if (columnDescription == null) {
                // could be a recently added column. Refresh:
                List<ColumnDescription> newColumns = DataBaseTableHelper.readColumns(_getConnection(), m_table);
                for (ColumnDescription newColumnDescription : newColumns) {
                    if (newColumnDescription.columnName.equals(name)) {
                        m_descriptions.put(name, newColumnDescription);
                        break;
                    }
                }
                columnDescription = m_descriptions.get(name);
            }
            if (columnDescription == null) {
                throw new SQLException("No column " + name + " found");
            }
            
            ExtraColumnInfo columnInfo = m_extrainfos.get(name);
            if (columnInfo == null) {
                String composedName = DataBaseTools.composeTableNameForSelect(_getConnection(), m_table);
                m_extrainfos = DataBaseTools.collectColumnInformation(_getConnection(), composedName, "*");
                columnInfo = m_extrainfos.get(name);
            }
            if (columnInfo != null) {
                queryInfo = false;
                isAutoIncrement = columnInfo.isAutoIncrement;
                isCurrency = columnInfo.isCurrency;
                dataType = columnInfo.dataType;
            }
            
            XNameAccess primaryKeyColumns = DataBaseTools.getPrimaryKeyColumns(UnoRuntime.queryInterface(XPropertySet.class, m_table));
            int nullable = columnDescription.nullable;
            if (nullable != ColumnValue.NO_NULLS && primaryKeyColumns != null && primaryKeyColumns.hasByName(name)) {
                nullable = ColumnValue.NO_NULLS;
            }
            column = new Column(m_table.getConnection(), isCaseSensitive(), m_table.m_CatalogName, m_table.m_SchemaName, m_table.getName(), name, columnDescription.typeName, columnDescription.defaultValue, columnDescription.remarks,
                    nullable, columnDescription.columnSize, columnDescription.decimalDigits, columnDescription.type,
                    isAutoIncrement, false, isCurrency);
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, m_table);
        }
        return column;
    }

    @Override
    protected void _removeElement(int index,
                                  String name)
        throws SQLException
    {
        UnoHelper.ensure(m_table, "Table is null!");
        if (m_table == null) {
            return;
        }
        try {
            String quote = _getConnection().getProvider().getConnection().getMetaData().getIdentifierQuoteString();
            String sql = String.format("ALTER TABLE %s DROP %s",
                    DataBaseTools.composeTableName(_getConnection(), m_table, ComposeRule.InTableDefinitions, false, false, true),
                    DataBaseTools.quoteName(quote, name));
            java.sql.Statement statement = _getConnection().getProvider().getConnection().createStatement();
            statement.execute(sql);
            statement.close();
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, m_table);
        }
    }

    /// The XDatabaseMetaData.getColumns() data stored in columnDescriptions doesn't provide everything we need, so this class stores the rest.
    public static class ExtraColumnInfo
    {
        public boolean isAutoIncrement;
        public boolean isCurrency;
        public int dataType;
    }

    
    @Override
    protected void _refresh() {
        m_extrainfos.clear();
        // FIXME: won't help
        m_table._refreshColumns();
    }

    public ConnectionSuper _getConnection()
    {
        return m_table.getConnection();
    }

    @Override
    protected ColumnDescriptor _createDescriptor() {
        return new ColumnDescriptor(isCaseSensitive());
    }


    /*    // The constructor method:
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.TableBase()
    public ColumnContainer(Connection connection,
                           TableBase table)
    {
        super(m_name, m_services, connection);
        _refresh(table);
        System.out.println("sdbcx.ColumnContainer(): " + getCount());
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.TableBase()
    public ColumnContainer(Connection connection,
                           XNameAccess columns)
    {
        super(m_name, m_services, connection);
        XEnumeration iter = ((XEnumerationAccess) UnoRuntime.queryInterface(XEnumerationAccess.class, columns)).createEnumeration();
        System.out.println("sdbcx.ColumnContainer() 1");
        int position = 1;
        try {
            while (iter.hasMoreElements()) {
                XPropertySet descriptor = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, iter.nextElement());
                System.out.println("sdbcx.ColumnContainer() 2"); 
                String name = (String) descriptor.getPropertyValue("Name");
                m_Elements.add(new Column(m_Connection, descriptor, name, position++));
                m_Names.add(name);
            }
        }
        catch (NoSuchElementException | WrappedTargetException | UnknownPropertyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (java.sql.SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("sdbcx.ColumnContainer(): " + getCount());
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.TableBase()
    public ColumnContainer(Connection connection,
                           schemacrawler.schema.Table table)
        throws java.sql.SQLException
    {
        super(m_name, m_services, connection);
        try {
            for (schemacrawler.schema.Column c : table.getColumns()) {
                String name = c.getName();
                m_Elements.add(new Column(connection, c, name));
                m_Names.add(name);
            }
        }
        catch (IllegalArgumentException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.TableDescriptorBase()
    public ColumnContainer(Connection connection)
    {
        super(m_name, m_services, connection);
        System.out.println("sdbcx.ColumnContainer()");
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdb.CallableStatement.getColumns()
    // XXX: - io.github.prrvchr.uno.sdb.PreparedStatement.getColumns()
    public ColumnContainer(Connection connection,
                           java.sql.ResultSetMetaData metadata)
        throws java.sql.SQLException
    {
        super(m_name, m_services, connection);
        _refresh(metadata);
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdb.ResultSet.getColumns()
    public ColumnContainer(Connection connection,
                           java.sql.ResultSet result)
        throws java.sql.SQLException
    {
        super(m_name, m_services, connection);
        if (connection.useSchemaCrawler()) {
            try {
                ResultsCrawler crawler = new ResultsCrawler(result);
                for (ResultsColumn c : crawler.crawl())
                {
                    String name = c.getName();
                    m_Names.add(name);
                    m_Elements.add(new Column(connection, c, name));
                }
            }
            catch (IllegalArgumentException | SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else {
            _refresh(result.getMetaData());
        }
    }

   public void _refresh(TableBase table)
    {
        m_Names.clear();
        m_Elements.clear();
        try {
            _refresh(table.m_CatalogName, table.m_SchemaName, table.m_Name);
        }
        catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    private void _refresh(String catalog,
                          String schema,
                          String table)
        throws java.sql.SQLException
    {
        try {
            java.sql.ResultSet result = m_Connection.getWrapper().getMetaData().getColumns(catalog, schema, table, "%");
            while (result.next()) {
                String name = result.getString(4);
                m_Elements.add(new Column(m_Connection, result, name));
                m_Names.add(name);
            }
            result.close();
        } 
        catch (IllegalArgumentException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void _refresh(java.sql.ResultSetMetaData metadata)
        throws java.sql.SQLException
    {
        try {
            for (int position = 1; position <= metadata.getColumnCount(); position++) {
                String name = metadata.getColumnName(position);
                m_Elements.add(new Column(m_Connection, metadata, name, position));
                m_Names.add(name);
            }
        }
        catch (IllegalArgumentException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // com.sun.star.sdbcx.XAppend
    @Override
    public void appendByDescriptor(XPropertySet descriptor)
        throws SQLException,
               ElementExistException
    {
        System.out.println("sdbcx.ColumnContainer.appendByDescriptor() 1");
        try {
            int position = m_Elements.size() + 1;
            String name = (String) descriptor.getPropertyValue("Name");
            if (m_Names.contains(name)) {
                throw new ElementExistException();
            }
            Column column = new Column(m_Connection, descriptor, name, position);
            m_Elements.add(column);
            m_Names.add(name);
            _insertElement(column, name);
            System.out.println("sdbcx.ColumnContainer.appendByDescriptor() 2 : " + name);
        } 
        catch (IllegalArgumentException | SecurityException e) {
            UnoHelper.getSQLException(UnoHelper.getSQLException(e), this);
        }
        catch (java.sql.SQLException e) {
            UnoHelper.getSQLException(e, this);
        }
        catch (UnknownPropertyException | WrappedTargetException e) {
            UnoHelper.getSQLException(e, this);
        }
    }*/



}
