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

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.uno.AnyConverter;

import net.sf.jsqlparser.schema.Database;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import io.github.prrvchr.uno.helper.PropertyIds;
import io.github.prrvchr.uno.sdb.Connection;


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
    public static String getCreateTableQuery(Connection connection,
                                             XPropertySet descriptor)
        throws SQLException
    {
        CreateTable query = new CreateTable();
        query.setTable(getable(connection, descriptor));
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
   public static Table getable(Connection connection,
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

}

