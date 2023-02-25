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
package io.github.prrvchr.jdbcdriver;

import java.util.ArrayList;
import java.util.List;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexAccess;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;

import net.sf.jsqlparser.schema.Database;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionBase;


public class JSQLParserHelper {


    /** creates a SQL CREATE TABLE statement
     *
     * @param  connection
     *    The connection.
     * @param  descriptor
     *    The descriptor of the new table.
     * @param  helper
     *    Allow to add special SQL constructs.
     * @param  pattern
     *   
     * @return
     *   The CREATE TABLE statement.
     */
    public static String getCreateTableQuery(ConnectionBase connection,
                                             XPropertySet descriptor)
        throws SQLException
    {
        CreateTable query = new CreateTable();
        Table table = getable(connection, descriptor);
        query.setTable(table);
        query.setColumnDefinitions(getColumns(connection, descriptor, table.getName()));
        System.out.println("JSQLParserHelper.createSqlCreateTableStatement() SQL: " + query.toString());
            
        return query.toString();
    }
    
    /** get anJSQLParser Table
    *
    * @param  connection
    *    The connection.
    * @param  descriptor
    *    The descriptor of the new table.
    *   
    * @return
    *   The net.sf.jsqlparser.schema.Table.
    */
   public static Table getable(ConnectionBase connection,
                               XPropertySet descriptor)
       throws SQLException
   {
       Table table = new Table();
       try {
           table.setName(AnyConverter.toString(descriptor.getPropertyValue(PropertyIds.NAME.name)));
           java.sql.DatabaseMetaData metadata = connection.getProvider().getConnection().getMetaData();
           if (metadata.supportsCatalogsInTableDefinitions()) {
               table.setDatabase(new Database(AnyConverter.toString(descriptor.getPropertyValue(PropertyIds.CATALOGNAME.name))));
           }
           if (metadata.supportsSchemasInTableDefinitions()) {
               table.setSchemaName(AnyConverter.toString(descriptor.getPropertyValue(PropertyIds.SCHEMANAME.name)));
           }
       }
       catch (IllegalArgumentException | UnknownPropertyException | WrappedTargetException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
       catch (java.sql.SQLException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
    }
       return table;
   }

   /** creates the columns parts of the SQL CREATE TABLE statement.
    * @param connection
    *    The connection.
    * @param descriptor
    *    The descriptor of the new table.
    * @param helper
    *    Allow to add special SQL constructs.
    * @param pattern
    *   
    * @return
    *   The columns parts.
    * @throws SQLException
    */
   public static List<ColumnDefinition> getColumns(ConnectionBase connection,
                                                   XPropertySet descriptor,
                                                   String table)
       throws SQLException
   {
       List<ColumnDefinition> parts = new ArrayList<ColumnDefinition>();
       try {
           XIndexAccess columns = null;
           XColumnsSupplier supplier = UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
           if (supplier != null) {
               columns = UnoRuntime.queryInterface(XIndexAccess.class, supplier.getColumns());
           }
           if (columns == null || columns.getCount() <= 0) {
               String message = String.format("The '%s' table has no columns, it is not possible to create the table", table);
               throw new SQLException(message);
           }
           final String quote = connection.getMetaData().getIdentifierQuoteString();
           int count = columns.getCount();
           for (int i = 0; i < count; i++) {
               XPropertySet column;
               column = (XPropertySet) AnyConverter.toObject(XPropertySet.class, columns.getByIndex(i));
               if (column != null) {
                   parts.add(getStandardColumnPartQuery(connection, column, quote));
              }
           }
       }
       catch (IllegalArgumentException | WrappedTargetException | IndexOutOfBoundsException e) {
           throw UnoHelper.getSQLException(UnoHelper.getSQLException(e), connection);
       }
       return parts;
   }

   /** creates the standard sql statement for the column part of statement.
    *  @param connection
    *      The connection.
    *  @param columnProperties
    *      The descriptor of the column.
    *  @param helper
    *       Allow to add special SQL constructs.
    *  @param pattern
    *      
    * @throws SQLException
    */
   public static ColumnDefinition getStandardColumnPartQuery(ConnectionBase connection,
                                                   XPropertySet columnProperties,
                                                   String quote)
       throws SQLException
   {
       String name = null;
       String typename = null;
        try {
            //name = DataBaseTools.quoteName(quote, AnyConverter.toString(columnProperties.getPropertyValue(PropertyIds.NAME.name)));
            name = AnyConverter.toString(columnProperties.getPropertyValue(PropertyIds.NAME.name));
            typename = AnyConverter.toString(columnProperties.getPropertyValue(PropertyIds.TYPENAME.name));
        }
        catch (IllegalArgumentException | UnknownPropertyException | WrappedTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ColDataType datatype = new ColDataType(typename);
        ColumnDefinition column = new ColumnDefinition(name, datatype);
        return column;
   }

}

