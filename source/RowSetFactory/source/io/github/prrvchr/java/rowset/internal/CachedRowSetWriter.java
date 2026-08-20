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
/*
 * Copyright (c) 2003, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package io.github.prrvchr.java.rowset.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.sql.RowSetInternal;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.spi.SyncProviderException;
import javax.sql.rowset.spi.SyncResolver;
import javax.sql.rowset.spi.TransactionalWriter;

import io.github.prrvchr.java.rowset.CachedRowSetImpl;
import io.github.prrvchr.java.rowset.JdbcRowSetResourceBundle;
import io.github.prrvchr.java.rowset.internal.TableWriter.Table;


/**
 * The facility called on internally by the {@code RIOptimisticProvider} implementation to
 * propagate changes back to the data source from which the rowset got its data.
 * <P>
 * A {@code CachedRowSetWriter} object, called a writer, has the public
 * method {@code writeData} for writing modified data to the underlying data source.
 * This method is invoked by the rowset internally and is never invoked directly by an application.
 * A writer also has public methods for setting and getting
 * the {@code CachedRowSetReader} object, called a reader, that is associated
 * with the writer. The remainder of the methods in this class are private and
 * are invoked internally, either directly or indirectly, by the method
 * {@code writeData}.
 * <P>
 * Typically the {@code SyncFactory} manages the {@code RowSetReader} and
 * the {@code RowSetWriter} implementations using {@code SyncProvider} objects.
 * Standard JDBC RowSet implementations provide an object instance of this
 * writer by invoking the {@code SyncProvider.getRowSetWriter()} method.
 *
 * @version 0.2
 * @author Jonathan Bruce
 * @see javax.sql.rowset.spi.SyncProvider
 * @see javax.sql.rowset.spi.SyncFactory
 * @see javax.sql.rowset.spi.SyncFactoryException
 */
public class CachedRowSetWriter implements TransactionalWriter, Serializable {

    private static final long serialVersionUID = 1751318974164335483L;

    /**
     * The {@code Connection} object that this writer will use to make a
     * connection to the data source to which it will write data.
     *
     */
    private transient Connection connection;

    /**
     * The {@code Logger} object that this writer will use.
     *
     */
    private transient Logger logger;

    /**
     * The Tables holding SQL command relative to the columns composing
     * this rowset. The method {@code initSQLStatements} builds the list of tables.
     *
     * @serial
     */
    private TableWriter[] tables;

    private int rsType = ResultSet.TYPE_SCROLL_SENSITIVE;

    private int rsConcurrency = ResultSet.CONCUR_READ_ONLY;

    /**
     * The {@code CachedRowSetReader} object that has been
     * set as the reader for the {@code CachedRowSet} object
     * for which this {@code CachedRowSetWriter} object is the writer.
     *
     * @serial
     */
    private CachedRowSetReader reader;

    /**
     * The {@code ResultSetMetaData} object that contains information
     * about the columns in the {@code CachedRowSet} object
     * for which this {@code CachedRowSetWriter} object is the writer.
     *
     * @serial
     */
    private ResultSetMetaData metadata;

    /**
     * The number of columns in the {@code CachedRowSet} object
     * for which this {@code CachedRowSetWriter} object is the writer.
     *
     * @serial
     */
    private int columnCount;

    private JdbcRowSetResourceBundle resBundle;

    public CachedRowSetWriter() {
        try {
            resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
            logger = System.getLogger(CachedRowSetWriter.class.getName());
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Propagates changes in the given {@code RowSet} object
     * back to its underlying data source and returns {@code true}
     * if successful. The writer will check to see if
     * the data in the pre-modified rowset (the original values) differ
     * from the data in the underlying data source.  If data in the data
     * source has been modified by someone else, there is a conflict,
     * and in that case, the writer will not write to the data source.
     * In other words, the writer uses an optimistic concurrency algorithm:
     * It checks for conflicts before making changes rather than restricting
     * access for concurrent users.
     * <P>
     * This method is called by the rowset internally when
     * the application invokes the method {@code acceptChanges}.
     * The {@code writeData} method in turn calls private methods that
     * it defines internally.
     * The following is a general summary of what the method
     * {@code writeData} does, much of which is accomplished
     * through calls to its own internal methods.
     * <OL>
     * <LI>Creates a {@code CachedRowSet} object from the given
     *     {@code RowSet} object
     * <LI>Makes a connection with the data source
     *   <UL>
     *      <LI>Disables autocommit mode if it is not already disabled
     *      <LI>Sets the transaction isolation level to that of the rowset
     *   </UL>
     * <LI>Checks to see if the reader has read new data since the writer
     *     was last called and, if so, calls the method
     *    {@code initSQLStatements} to initialize new SQL statements
     *   <UL>
     *       <LI>Builds new {@code SELECT}, {@code UPDATE},
     *           {@code INSERT}, and {@code DELETE} statements
     *       <LI>Uses the {@code CachedRowSet} object's metadata to
     *           determine the table name, column names, and the columns
     *           that make up the primary key
     *   </UL>
     * <LI>When there is no conflict, propagates changes made to the
     *     {@code CachedRowSet} object back to its underlying data source
     *   <UL>
     *      <LI>Iterates through each row of the {@code CachedRowSet} object
     *          to determine whether it has been updated, inserted, or deleted
     *      <LI>If the corresponding row in the data source has not been changed
     *          since the rowset last read its
     *          values, the writer will use the appropriate command to update,
     *          insert, or delete the row
     *      <LI>If any data in the data source does not match the original values
     *          for the {@code CachedRowSet} object, the writer will roll
     *          back any changes it has made to the row in the data source.
     *   </UL>
     * </OL>
     *
     * @param caller a <code>RowSet</code> object that has implemented
     *               the <code>RowSetInternal</code> interface and had
     *               this <code>CachedRowSetReader</code> object set as
     *               its reader
     * @return {@code true} if changes to the rowset were successfully
     *         written to the rowset's underlying data source;
     *         {@code false} otherwise
     */
    public boolean writeData(RowSetInternal caller) throws SQLException {
        boolean success = false;

        // The reader is registered with the writer at design time.
        // This is not required, in general.  The reader has logic
        // to get a JDBC connection, so call it.

        connection = reader.connect(caller);

        if (connection == null) {
            throw new SQLException(resBundle.handleGetObject("crswriter.connect").toString());
        }

        /*
         // Fix 6200646.
         // Don't change the connection or transaction properties. This will fail in a
         // J2EE container.
        if (con.getAutoCommit() == true)  {
            con.setAutoCommit(false);
        }

        con.setTransactionIsolation(crs.getTransactionIsolation());
        */

        // We assume caller is a CachedRowSet
        CachedRowSetImpl crs = (CachedRowSetImpl) caller;

        if (tables == null) {
            initSQLStatements(crs);
        }
        if (tables.length > 0) {
            success = writeData(crs);
        } else {
            crs.setReadOnly(true);
            success =  false;
        }
        return success;
    }

    private boolean writeData(CachedRowSetImpl crs) throws SQLException {

        boolean success = false;

        if (columnCount < 1) {
            // No data, so return success.
            if (reader.getCloseConnection()) {
                connection.close();
            }
            success = true;
        } else {

            // Create the {@code CachedRowSet} will hold the conflicting values.
            CachedRowSetImpl crsRes = new CachedRowSetImpl();
            setCachedRowSetResolverMetaData(crsRes);

            List<Integer> status = new ArrayList<>(crs.size() + 1);
            // XXX: The first entry in the status will take the value of the first conflict that occurs
            status.add(0, SyncResolver.NO_ROW_CONFLICT);

            // We need to save the cursor position before processing.
            crs.saveCursor();
            // We need to see rows marked for deletion.
            boolean showDeleted = crs.getShowDeleted();
            if (!showDeleted) {
                crs.setShowDeleted(true);
            }

            // Look at all the rows.
            crs.beforeFirst();

            // Process all the rows in the CachedRowSet.
            List<SQLException> conflicts = writeData(crs, crsRes, status);

            // reset
            if (!showDeleted) {
                crs.setShowDeleted(false);
            }
            // We need to restore the cursor position after processing.
            crs.restoreCursor();

            crsRes.beforeFirst();

            if (conflicts.isEmpty()) {
                success = true;

            } else {
                SyncResolverImpl syncRes = new SyncResolverImpl();
                syncRes.setCachedRowSet(crs);
                syncRes.setCachedRowSetResolver(crsRes);
                syncRes.setStatus((ArrayList<?>) status);
                syncRes.setCachedRowSetWriter(this);

                SQLException e = conflicts.remove(0);
                SyncProviderException spe = new SyncProviderException(e.getMessage());
                spe.setNextException(e);
                for (SQLException ex : conflicts) {
                    e = setNextSQLException(e, ex);
                }
                spe.setSyncResolver(syncRes);
                throw spe;
            }

        }
        return success;
    }

    private void setCachedRowSetResolverMetaData(CachedRowSetImpl crsRes)
        throws SQLException {
        RowSetMetaDataImpl md = new RowSetMetaDataImpl();
        md.setColumnCount(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            md.setColumnType(i, metadata.getColumnType(i));
            md.setColumnName(i, metadata.getColumnName(i));
            md.setNullable(i, ResultSetMetaData.columnNullableUnknown);
        }
        crsRes.setMetaData(md);
    }

    private SQLException setNextSQLException(SQLException e, SQLException ex) {
        e.setNextException(ex);
        return ex;
    }

    private List<SQLException> writeData(CachedRowSetImpl crs, CachedRowSetImpl crsRes,
                                         List<Integer> status)
        throws SQLException {
        List<SQLException> conflicts = new ArrayList<>();
        int row = 1;

        while (crs.next()) {

            try {
                // XXX: If a conflict occurs, an exception will be thrown

                if (crs.rowRemoved()) {
                    // The row has been removed and will be deleted.
                    deleteCurrentRow(crs, crsRes, status, row);

                } else if (crs.rowCreated()) {
                    // The row has been created and will be inserted.
                    insertCurrentRow(crs, crsRes, status, row);

                } else  if (crs.rowUpdated()) {
                    // The row has been updated.
                    updateCurrentRow(crs, crsRes, status, row);
                }

                setResolverNoConflict(crsRes);
                status.add(row, SyncResolver.NO_ROW_CONFLICT);

            } catch (SQLException e) {
                conflicts.add(e);
            }
            row++;
        }
        return conflicts;
    }

    /**
     * Updates the given {@code CachedRowSet} object's underlying data
     * source so that updates to the rowset are reflected in the original
     * data source, and returns {@code true} if the update was successful.
     * A return value of {@code false} indicates that there is a conflict,
     * meaning that a value updated in the rowset has already been changed by
     * someone else in the underlying data source.  A conflict can also exist
     * if, for example, more than one row in the data source would be affected
     * by the update or if no rows would be affected.  In any case, if there is
     * a conflict, this method does not update the underlying data source.
     * <P>
     * This method is called internally by the method {@code writeData}
     * if a row in the {@code CachedRowSet} object for which this
     * {@code CachedRowSetWriter} object is the writer has been updated.
     *
     * @param crs the {@code CachedRowSet} object to be updated
     * @param crsRes the {@code CachedRowSet} will hold the conflicting values
     * retrieved from the db and hold it.
     * @param status the {@code List<Integer>} object to update if
     * a conflict occur
     * @param row the {@code int} row number to be updated
     *
     * @throws SQLException if a database access error occurs
     */
    private void updateCurrentRow(CachedRowSet crs, CachedRowSetImpl crsRes,
                                  List<Integer> status, int row)
        throws SQLException {

        // Select the row from the database.
        ResultSet origVals = crs.getOriginalRow();
        origVals.next();

        Map<TableWriter, String> queries = new HashMap<>();
        try {
            Map<String, List<Entry<Object, Integer>>> parameters = new HashMap<>();
            // FIXME: We need to check all table before updating anything.
            for (TableWriter table : tables) {
                if (table.isRowUpdated(crs)) {
                    List<Entry<Object, Integer>> params = new ArrayList<>();
                    queries.put(table, table.getUpdateQuery(connection, metadata, origVals, crs,
                                                            row, params, rsType, rsConcurrency));
                    parameters.put(table.getName(), params);
                }
            }
            for (Entry<TableWriter, String> entry : queries.entrySet()) {
                TableWriter table = entry.getKey();
                table.executeUpdateStatement(connection, entry.getValue(), row, parameters.get(table.getName()));
            }
        } catch (SQLException e) {
            setResolverConflict(queries.keySet().toArray(new TableWriter[0]), crsRes,
                                origVals, status, row, SyncResolver.UPDATE_ROW_CONFLICT);
            throw e;
        }
    }

    /**
     * Inserts a row that has been inserted into the given
     * {@code CachedRowSet} object into the data source from which
     * the rowset is derived, returning {@code true} if the insertion
     * was successful.
     *
     * @param crs the {@code CachedRowSet} object that has had a row inserted
     *            and to whose underlying data source the row will be inserted
     * @param crsRes the {@code CachedRowSet} will hold the conflicting values
     * @param status the {@code List<Integer>} object to update if
     * a conflict occur
     * @param row the {@code int} row number to be updated
     * retrieved from the db and hold it.
     * @throws SQLException if a database access error occurs
     */
    private void insertCurrentRow(CachedRowSet crs, CachedRowSetImpl crsRes,
                                  List<Integer> status, int row) throws SQLException {

        try {
            for (TableWriter table: tables) {
                table.insertCurrentRow(connection, metadata, crs, row);
            }
        } catch (SQLException e) {
            /*
             * Cursor will come here if executeUpdate() fails.
             * There can be many reasons why the insertion failed,
             * one can be violation of primary key.
             * Hence we cannot exactly identify why the insertion failed,
             * present the current row as a null row to the caller.
             */
            setResolverConflict(tables, crsRes, crs, status, row, SyncResolver.INSERT_ROW_CONFLICT);
            throw e;
        }
    }

    /**
     * Deletes the row in the underlying data source that corresponds to
     * a row that has been deleted in the given {@code  CachedRowSet} object
     * and returns {@code true} if the deletion was successful.
     * <P>
     * This method is called internally by this writer's {@code writeData}
     * method when a row in the rowset has been deleted. The values in the
     * deleted row are the same as those that are stored in the original row
     * of the given {@code CachedRowSet} object.  If the values in the
     * original row differ from the row in the underlying data source, the row
     * in the data source is not deleted, and {@code deleteOriginalRow}
     * returns {@code false} to indicate that there was a conflict.
     *
     * @param crs the {@code CachedRowSet} object for which this
     *     {@code CachedRowSetWriter} object is the writer
     * @param crsRes the {@code CachedRowSet} will hold the conflicting values
     * retrieved from the db and hold it.
     * @param status the {@code List<Integer>} object to update if
     * a conflict occur
     * @param row the {@code int} row number to be updated
     *
     * @throws SQLException if there was a conflict or database access error
     */
    private void deleteCurrentRow(CachedRowSet crs, CachedRowSetImpl crsRes,
                                  List<Integer> status, int row) throws SQLException {
        // Select the row from the database.
        ResultSet origVals = crs.getOriginalRow();
        origVals.next();

        Map<TableWriter, String> queries = new HashMap<>();
        try {
            Map<String, List<Entry<Object, Integer>>> parameters = new HashMap<>();
            // FIXME: We need to check all table before deleting anything.
            for (TableWriter table : tables) {
                List<Entry<Object, Integer>> params = new ArrayList<>();
                queries.put(table, table.getDeleteQuery(connection, metadata, origVals, crs,
                                                        row, params, rsType, rsConcurrency));
                parameters.put(table.getName(), params);
            }
            for (Entry<TableWriter, String> entry : queries.entrySet()) {
                TableWriter table = entry.getKey();
                table.executeDeleteStatement(connection, entry.getValue(), row, parameters.get(table.getName()));
            }
        } catch (SQLException e) {
            status.add(row, SyncResolver.DELETE_ROW_CONFLICT);
            setResolverConflict(queries.keySet().toArray(new TableWriter[0]), crsRes,
                                origVals, status, row, SyncResolver.DELETE_ROW_CONFLICT);
            throw e;
        }
    }

    private void setResolverNoConflict(CachedRowSetImpl crsRes) throws SQLException {
        crsRes.moveToInsertRow();
        for (int cols = 1; cols <= columnCount; cols++) {
            crsRes.updateNull(cols);
        }
        crsRes.insertRow();
        crsRes.moveToCurrentRow();
    }

    private void setResolverConflict(TableWriter[] tableWriters, CachedRowSetImpl crsRes,  ResultSet origVals,
                                     List<Integer> status, int row, int conflict)
        throws SQLException {
        status.add(row, conflict);
        // XXX: The first stat entry corresponds to the first conflict
        if (status.get(0) == SyncResolver.NO_ROW_CONFLICT) {
            status.set(0, conflict);
        }

        crsRes.moveToInsertRow();
        if (tableWriters.length < 1) {
            tableWriters = tables;
        }
        List<Integer> indexes = Arrays.stream(tableWriters).flatMap(table -> table.getTableColumns().stream()).toList();
        for (int index : indexes) {
            Object value = origVals.getObject(index);
            crsRes.updateObject(index, value);
        }
        crsRes.insertRow();
        crsRes.moveToCurrentRow();
    }

    /**
     * Sets the reader for this writer to the given reader.
     *
     * @param crsr the {@code CachedRowSetReader} object to be set
     * @throws SQLException if a database access error occurs
     */
    public void setReader(CachedRowSetReader crsr) throws SQLException {
        reader = crsr;
    }

    /**
     * Gets the reader for this writer.
     *
     * @return the {@code CachedRowSetReader} object from this writer
     * @throws SQLException if a database access error occurs
     */
    public CachedRowSetReader getReader() throws SQLException {
        return reader;
    }

    /**
     * Composes a {@code SELECT}, {@code UPDATE}, {@code INSERT},
     * and {@code DELETE} statement that can be used by this writer to
     * write data to the data source backing the given {@code CachedRowSet}
     * object.
     *
     * @param caller a {@code CachedRowSet} object for which this
     *        {@code CachedRowSetWriter} object is the writer
     * @throws SQLException if a database access error occurs
     */
    private void initSQLStatements(CachedRowSet caller)
        throws SQLException {

        metadata = caller.getMetaData();
        columnCount = metadata.getColumnCount();
        DatabaseMetaData dbmd = connection.getMetaData();
        if (!dbmd.supportsResultSetConcurrency(rsType, rsConcurrency)) {
            rsType = ResultSet.TYPE_FORWARD_ONLY;
        }
        /*
         * set the key descriptors that will be
         * needed to construct where clauses.
         */
        tables = getTableWriters(dbmd, caller.getKeyColumns());

        if (tables == null) {
            throw new SQLException(resBundle.handleGetObject("crswriter.twriter.ko").toString());
        }
        if (tables.length > 0) {
            String description = Arrays.stream(tables)
                    .map(TableWriter::getDescription).collect(Collectors.joining(", "));
            log(Level.INFO, "crswriter.twriter.ok", description);
        } else {
            log(Level.ERROR, "crswriter.twriter.ko");
        }
    }

    /**
     * Assigns to the given {@code CachedRowSet} object's
     * {@code params}
     * field an array whose length equals the number of columns needed
     * to uniquely identify a row in the rowset. The array is given
     * values by the method {@code buildWhereClause}.
     * <P>
     * If the {@code CachedRowSet} object's {@code keyCols}
     * field has length {@code 0} or is {@code null}, the array
     * is set with the primary key with the column number of every column in the rowset.
     * Otherwise, the array in the field {@code keyCols} is set with only
     * the column numbers of the columns that are required to form a unique
     * identifier for a row.
     *
     * @param dbmd a {@code DatabaseMetaData} object
     * @param keys a {@code Int[]} object coming from the caller's
     *
     * @return the {@code TableWriter} object from this writer
     * @throws SQLException if a database access error occurs
     */

    private TableWriter[] getTableWriters(DatabaseMetaData dbmd, int[] keys)
        throws SQLException {

        if (columnCount > 0 && keys != null && keys.length > 0) {
            Map<Table, TableWriter> tablesBuilder = new HashMap<>();
            List<Integer> primaryKeys = new ArrayList<>();
            List<Integer> indexes = getKeyIndexes(keys, primaryKeys);
            for (int index : indexes) {
                Table table = new Table(metadata, index);
                if (!tablesBuilder.containsKey(table)) {
                    tablesBuilder.put(table, new TableWriter(metadata, dbmd, table, indexes,
                                                             primaryKeys, resBundle, logger));
                }
            }
            return tablesBuilder.values().toArray(new TableWriter[0]);
        }
        return new TableWriter[0];
    }

    private List<Integer> getKeyIndexes(int[] keys, List<Integer> primaryKeys) {
        List<Integer> indexes = new ArrayList<>();
        for (int key : keys) {
            int index;
            if (key < 0) {
                index = Math.abs(key);
                primaryKeys.add(index);
            } else {
                index = key;
            }
            indexes.add(index);
        }
        return indexes;
    }

    void updateResolvedConflictToDB(CachedRowSet crs, Connection con)
        throws SQLException {
        for (TableWriter table : tables) {
            table.updateResolvedConflictToDB(crs, con);
        }
    }

    public void commit() throws SQLException {
        connection.commit();
        if (reader.getCloseConnection()) {
            connection.close();
        }
    }

    public void commit(CachedRowSetImpl crs, boolean updateRowset) throws SQLException {
        connection.commit();
        if (updateRowset) {
            if (crs.getCommand() != null) {
                crs.execute(connection);
            }
        }

        if (reader.getCloseConnection()) {
            connection.close();
        }
    }

    public void rollback() throws SQLException {
        connection.rollback();
        if (reader.getCloseConnection()) {
            connection.close();
        }
    }

    public void rollback(Savepoint s) throws SQLException {
        connection.rollback(s);
        if (reader.getCloseConnection()) {
            connection.close();
        }
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // Default state initialization happens here
        ois.defaultReadObject();
        // Initialization of  Res Bundle happens here .
        try {
            resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

    }

    private void log(Level level, String resource, Object... args) {
        if (logger.isLoggable(level)) {
            String msg = resBundle.handleGetObject(resource).toString();
            logger.log(level, MessageFormat.format(msg, args));
        }
    }

}
