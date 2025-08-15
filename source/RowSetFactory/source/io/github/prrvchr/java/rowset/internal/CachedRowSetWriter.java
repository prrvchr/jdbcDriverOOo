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
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import javax.sql.RowSetInternal;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.serial.SQLInputImpl;
import javax.sql.rowset.serial.SerialArray;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;
import javax.sql.rowset.serial.SerialStruct;
import javax.sql.rowset.spi.SyncProviderException;
import javax.sql.rowset.spi.SyncResolver;
import javax.sql.rowset.spi.TransactionalWriter;

import io.github.prrvchr.java.rowset.CachedRowSetImpl;
import io.github.prrvchr.java.rowset.JdbcRowSetResourceBundle;


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

    static final long serialVersionUID = -8506030970299413976L;

    static final String DOT = ".";

    /**
     * The {@code Connection} object that this writer will use to make a
     * connection to the data source to which it will write data.
     *
     */
    private transient Connection con;

    /**
     * The {@code Logger} object that this writer will use.
     *
     */
    private transient Logger logger;

    /**
     * The {@code String} object that this writer will use to quote
     * identifier in SQL queries.
     *
     * @serial
     */
    private String identifierQuote;

    /**
     * The SQL {@code SELECT} command that this writer will call
     * internally. The method {@code initSQLStatements} builds this
     * command by supplying the words "SELECT" and "FROM," and using
     * metadata to get the table name and column names .
     *
     * @serial
     */
    private String selectCmd;

    /**
     * The SQL {@code UPDATE} command that this writer will call
     * internally to write data to the rowset's underlying data source.
     * The method {@code initSQLStatements} builds this {@code String}
     * object.
     *
     * @serial
     */
    private String updateCmd;

    /**
     * The SQL {@code DELETE} command that this writer will call
     * internally to delete a row in the rowset's underlying data source.
     *
     * @serial
     */
    private String deleteCmd;

    /**
     * The SQL {@code INSERT INTO} command that this writer will internally use
     * to insert data into the rowset's underlying data source.  The method
     * {@code initSQLStatements} builds this command with a question
     * mark parameter placeholder for each column in the rowset.
     *
     * @serial
     */
    private String insertCmd;

    /**
     * A <code>boolean</code> indicating whether the {@code CachedRowSet}
     * object has primary keys.
     *
     * @serial
     */
    private boolean hasPrimarykeys;

    private boolean updateOnInsert;

    private boolean supportsGeneratedKeys;

    /**
     * An array containing the column numbers of the columns that are
     * needed to uniquely identify a row in the {@code CachedRowSet} object
     * for which this {@code CachedRowSetWriter} object is the writer.
     *
     * @serial
     */
    private int[] keyCols;

    /**
     * An array containing the column numbers of the columns that coming from
     * the same table as keyCols.
     *
     * @serial
     */
    private int[] tabCols;

    /**
     * An array containing the column name of the columns that coming from
     * the same table as keyCols and using for getGeneratedKeys().
     *
     * @serial
     */
    private String[] autoCols;


    /**
     * An array of the parameters that should be used to set the parameter
     * placeholders in a {@code PreparedStatement} object that this
     * writer will execute.
     *
     * @serial
     */
    private Object[] params;

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
    private ResultSetMetaData callerMd;

    /**
     * The number of columns in the {@code CachedRowSet} object
     * for which this {@code CachedRowSetWriter} object is the writer.
     *
     * @serial
     */
    private int callerColumnCount;

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

        con = reader.connect(caller);

        if (con == null) {
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

        if (keyCols == null) {
            initSQLStatements(crs);
        }
        if (keyCols.length > 0) {
            success = writeData(crs);
        } else {
            crs.setReadOnly(true);
            success =  false;
        }
        return success;
    }

    private boolean writeData(CachedRowSetImpl crs) throws SQLException {

        boolean success = false;

        if (callerColumnCount < 1) {
            // No data, so return success.
            if (reader.getCloseConnection()) {
                con.close();
            }
            success = true;
        } else {

            // Create the {@code CachedRowSet} will hold the conflicting values.
            CachedRowSetImpl crsRes = new CachedRowSetImpl();
            setCachedRowSetResolverMetaData(crsRes);

            List<Integer> status = new ArrayList<>(crs.size() + 1);
            status.add(0, null);

            // We need to save the sursor position before processing.
            crs.saveCursor();

            // We need to see rows marked for deletion.
            boolean showDel = crs.getShowDeleted();
            crs.setShowDeleted(true);

            // Look at all the rows.
            crs.beforeFirst();

            // Process all the rows in the CachedRowSet.
            List<SQLException> conflicts = writeData(crs, crsRes, status);

            // reset
            crs.setShowDeleted(showDel);

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

                String msg = conflicts.remove(0).getMessage();
                SyncProviderException spe = new SyncProviderException(msg);
                setSyncProviderException(spe, conflicts);
                spe.setSyncResolver(syncRes);
                throw spe;
            }

        }
        return success;
    }

    private void setCachedRowSetResolverMetaData(CachedRowSetImpl crsRes)
        throws SQLException {
        RowSetMetaDataImpl md = new RowSetMetaDataImpl();
        md.setColumnCount(callerColumnCount);
        for (int i = 1; i <= callerColumnCount; i++) {
            if (callerMd.isAutoIncrement(i)) {
                updateOnInsert = true;
            }
            md.setColumnType(i, callerMd.getColumnType(i));
            md.setColumnName(i, callerMd.getColumnName(i));
            md.setNullable(i, ResultSetMetaData.columnNullableUnknown);
        }
        crsRes.setMetaData(md);
    }

    private void setSyncProviderException(SQLException ex, List<SQLException> conflicts) {
        for (SQLException e : conflicts) {
            ex.setNextException(e);
            ex = e;
        }
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
                e.printStackTrace();
                conflicts.add(e);
            } catch (Throwable ex) {
                ex.printStackTrace();
                SQLException e = new SQLException(ex.getMessage());
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

        String predicate = buildWhereClause(origVals);

        try (PreparedStatement stmt = con.prepareStatement(selectCmd + predicate,
                                                           ResultSet.TYPE_SCROLL_SENSITIVE,
                                                           ResultSet.CONCUR_READ_ONLY)) {

            setStatementParameters(stmt);
            setStatementProperties(crs, stmt);

            if (hasPrimarykeys) {
                updateCurrentRow(crs, origVals, stmt, predicate, row);
            } else {
                updateCurrentRowWithCheck(crs, origVals, stmt, predicate, row);
            }

        } catch (SQLException e) {
            status.add(row, SyncResolver.UPDATE_ROW_CONFLICT);
            setResolverConflict(crsRes, origVals);
            throw e;
        }
    }

    private void setStatementParameters(PreparedStatement stmt)
        throws SQLException {
        int index = 0;
        for (int i = 0; i < params.length; i++) {
            Object obj = params[i];
            if (obj != null) {
                stmt.setObject(++index, obj);
            }
        }
    }

    private void setStatementProperties(CachedRowSet crs, PreparedStatement stmt) {
        try {
            stmt.setMaxRows(crs.getMaxRows());
            stmt.setMaxFieldSize(crs.getMaxFieldSize());
            stmt.setEscapeProcessing(crs.getEscapeProcessing());
            stmt.setQueryTimeout(crs.getQueryTimeout());
        } catch (Exception e) {
            // Older driver don't support these operations.
        }
    }

    private void updateCurrentRow(CachedRowSet crs, ResultSet origVals,
                                  PreparedStatement stmt, String predicate, int row)
        throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                updateCurrentRow(crs, origVals, rs, predicate, row);
            } else {
                /**
                 * Cursor will be here, if the ResultSet may not return even a single row
                 * i.e. we can't find the row where to update because it has been deleted
                 * etc. from the db.
                 * Present the whole row as null to user, to force null to be sync'ed
                 * and hence nothing to be synced.
                 *
                 * NOTE:
                 * ------
                 * In the database if a column that is mapped to java.sql.Types.REAL stores
                 * a Double value and is compared with value got from ResultSet.getFloat()
                 * no row is retrieved and will throw a SyncProviderException. For details
                 * see bug Id 5053830
                 **/
                String msg = resBundle.handleGetObject("crswriter.update.norow.error").toString();
                throw new SQLException(MessageFormat.format(msg, row));
            }
        }
    }

    private void updateCurrentRowWithCheck(CachedRowSet crs, ResultSet origVals,
                                           PreparedStatement stmt, String predicate, int row)
        throws SQLException {
        boolean updated = false;
        try (ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                if (rs.next()) {
                    /** More than one row conflict.
                     *  If rs has only one row we are able to
                     *  uniquely identify the row where update
                     *  have to happen else if more than one
                     *  row implies we cannot uniquely identify the row
                     *  where we have to do updates.
                     *  crs.setKeyColumns needs to be set to
                     *  come out of this situation.
                     */
                    int duplicate = 2;
                    while (rs.next()) {
                        duplicate++;
                    }
                    String msg = resBundle.handleGetObject("crswriter.update.duplicate.error").toString();
                    throw new SQLException(MessageFormat.format(msg, row, duplicate));
                }
                if (rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
                    rs.first();
                    updateCurrentRow(crs, origVals, rs, predicate, row);
                    updated = true;
                }
            } else {
                String msg = resBundle.handleGetObject("crswriter.update.norow.error").toString();
                throw new SQLException(MessageFormat.format(msg, row));
            }
        }

        // XXX: We need to close the ResultSet and open an other one for
        // XXX: database like SQLite supporting only ResultSet.TYPE_FORWARD_ONLY
        if (!updated) {
            updateCurrentRow(crs, origVals, stmt, predicate, row);
        }
    }

    private void updateCurrentRow(CachedRowSet crs, ResultSet origVals,
                                  ResultSet rs, String predicate, int row)
        throws SQLException {

        // how many fields need to be updated
        StringJoiner updateSet = new StringJoiner(", ");

        Map<String, Class<?>> map;
        if (crs.getTypeMap() != null) {
            map = crs.getTypeMap();
        } else {
            map = con.getTypeMap();
        }

        List<Integer> cols = new ArrayList<>();

        for (int i = 0; i < tabCols.length; i++) {
            int index = tabCols[i];
            updateCurrentRow(crs, origVals, rs, index, map, cols, updateSet, row);
        }

        if (cols.size() > 0 ) {
            String query = updateCmd + updateSet.toString() + predicate;
            executeUpdate(crs, cols, query, row);
        }
    }

    private void updateCurrentRow(CachedRowSet crs, ResultSet origVals, ResultSet rs,
                                  int index, Map<String, Class<?>> map,
                                  List<Integer> cols, StringJoiner updateSet, int row)
        throws SQLException {
        Object orig = origVals.getObject(index);
        Object curr = crs.getObject(index);
        Object rsval = rs.getObject(index);

        /**
         * the following block creates equivalent objects
         * that would have been created if this rs is populated
         * into a CachedRowSet so that comparison of the column values
         * from the ResultSet and CachedRowSet are possible
         */
        rsval = getResultSetValue(map, rsval, row, index);

        /** This additional checking has been added when the current value
         *  in the DB is null, but the DB had a different value when the
         *  data was actually fetched into the CachedRowSet.
         **/

        if (rsval == null && orig != null || rsval != null && !rsval.equals(orig)) {
            // value in db has changed
            // don't proceed with synchronization
            // get the value in db and pass it to the resolver.
            String msg = resBundle.handleGetObject("crswriter.update.conflict.error").toString();
            throw new SQLException(MessageFormat.format(msg, row));
        }
        if (orig == null && curr != null || curr != null && !curr.equals(orig)) {
            // When values from db and values in CachedRowSet are not equal,
            // if db value is same as before updation for each col in
            // the row before fetching into CachedRowSet,
            // only then we go ahead with updation, else we
            // throw SyncProviderException.

            // if value has changed in db after fetching from db
            // for some cols of the row and at the same time, some other cols
            // have changed in CachedRowSet, no synchronization happens

            // Synchronization happens only when data when fetching is
            // same or at most has changed in cachedrowset

            // check orig value with what is there in crs for a column
            // before updation in crs.

            if (crs.columnUpdated(index)) {
                if (!rsval.equals(orig)) {
                    String msg = resBundle.handleGetObject("crswriter.update.conflict.error").toString();
                    throw new SQLException(MessageFormat.format(msg, row));
                }
                // At this point we are sure that
                // the value updated in crs was from
                // what is in db now and has not changed
                String col = getQuotedIdentifier(callerMd.getColumnName(index));
                updateSet.add(col + " = ?");
                cols.add(index);
            } else {
                // XXX: Normally this should never happen
                String msg = resBundle.handleGetObject("crswriter.update.error").toString();
                throw new SQLException(MessageFormat.format(msg, row));
            }
        }
    }

    private Object getResultSetValue(Map<String, Class<?>> map, Object rsval, int row, int index)
        throws SQLException {
        if (rsval instanceof Struct) {
            Struct s = (Struct) rsval;
            // look up the class in the map
            Class<?> c = null;
            String type = s.getSQLTypeName();
            c = map.get(type);
            if (c != null) {
                // create new instance of the class
                SQLData obj = null;
                try {
                    Object tmp = c.getDeclaredConstructor().newInstance();
                    obj = (SQLData) tmp;
                } catch (Exception ex) {
                    String column = callerMd.getColumnName(index);
                    String msg = resBundle.handleGetObject("crswriter.update.struct.error").toString();
                    throw new SQLException(MessageFormat.format(msg, row, type, column), ex);
                }
                // get the attributes from the struct
                Object attribs[] = s.getAttributes(map);
                // create the SQLInput "stream"
                SQLInputImpl sqlInput = new SQLInputImpl(attribs, map);
                // read the values...
                obj.readSQL(sqlInput, s.getSQLTypeName());
                rsval = obj;
            }
        } else if (rsval instanceof SQLData) {
            rsval = new SerialStruct((SQLData) rsval, map);
        } else if (rsval instanceof Blob) {
            rsval = new SerialBlob((Blob) rsval);
        } else if (rsval instanceof Clob) {
            rsval = new SerialClob((Clob) rsval);
        } else if (rsval instanceof java.sql.Array) {
            rsval = new SerialArray((java.sql.Array) rsval, map);
        }
        return rsval;
    }

    private void executeUpdate(CachedRowSet crs, List<Integer> cols, String query, int row)
        throws SQLException {

        try (PreparedStatement stmt = con.prepareStatement(query)) {

            // Comments needed here
            int i;
            StringJoiner values = new StringJoiner(", ");
            for (i = 0; i < cols.size(); i++) {
                int index = cols.get(i);
                Object value = crs.getObject(index);
                if (crs.wasNull()) {
                    stmt.setNull(i + 1, callerMd.getColumnType(index));
                    values.add("null");
                } else {
                    stmt.setObject(i + 1, value);
                    values.add(value.toString());
                }
            }
            int index = i;

            // Comments needed here
            for (i = 0; i < keyCols.length; i++) {
                Object obj = params[i];
                if (obj != null) {
                    stmt.setObject(++index, obj);
                    values.add(obj.toString());
                }
            }

            /**
             * i should be equal to 1(row count), because we update
             * one row(returned as row count) at a time, if all goes well.
             * if 1 != 1, this implies we have not been able to
             * do updations properly i.e there is a conflict in database
             * versus what is in CachedRowSet for this particular row.
             **/
            int count = stmt.executeUpdate();
            if (count != 1) {
                String msg = resBundle.handleGetObject("crswriter.update.cmd.error").toString();
                throw new SQLException(MessageFormat.format(msg, row, query, count));
            }
            log(Level.INFO, "crswriter.update.cmd", row, query, values.toString());
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


        // We update on insert only if we have auto-increment columns in the CachedRowSet
        boolean generatedKey = updateOnInsert && supportsGeneratedKeys;

        try (PreparedStatement stmt = getInsertPreparedStatement(generatedKey)) {
            // XXX: Auto-increment columns are ignored during inserts (needed by PostGreSQL)
            StringJoiner values = new StringJoiner(", ");
            int index = 1;
            for (int i = 0; i < tabCols.length; i++) {
                int j = tabCols[i];
                if (isInsertableColumns(j)) {
                    Object value = crs.getObject(j);
                    if (value != null) {
                        stmt.setObject(index, value);
                        values.add(value.toString());
                    } else {
                        stmt.setNull(index, callerMd.getColumnType(j));
                        values.add("null");
                    }
                    
                    index++;
                }
            }

            int count = stmt.executeUpdate();
            if (count != 1) {
                String msg = resBundle.handleGetObject("crswriter.insert.cmd.error").toString();
                throw new SQLException(MessageFormat.format(msg, row, insertCmd, count));
            }

            if (generatedKey) {
                updateGeneratedKeys(crs, stmt);
            }
            log(Level.INFO, "crswriter.insert.cmd", row, insertCmd, values.toString());

        } catch (SQLException e) {
            /*
             * Cursor will come here if executeUpdate() fails.
             * There can be many reasons why the insertion failed,
             * one can be violation of primary key.
             * Hence we cannot exactly identify why the insertion failed,
             * present the current row as a null row to the caller.
             */
            status.add(row, SyncResolver.INSERT_ROW_CONFLICT);
            setResolverConflict(crsRes, crs);
            throw e;
        }
    }

    private PreparedStatement getInsertPreparedStatement(boolean generatedKey) throws SQLException {
        PreparedStatement stmt;
        if (generatedKey) {
            if (autoCols == null) {
                autoCols = getGeneratedColumns();
            }
            stmt = con.prepareStatement(insertCmd, autoCols);
        } else {
            stmt = con.prepareStatement(insertCmd, Statement.NO_GENERATED_KEYS);
        }
        return stmt;
    }

    private String[] getGeneratedColumns() throws SQLException {
        int index;
        List<String> columns = new ArrayList<>();
        for (int i = 0; i < tabCols.length; i++) {
            index = tabCols[i];
            if (callerMd.isAutoIncrement(index)) {
                columns.add(callerMd.getColumnName(index));
            }
        }
        return columns.toArray(new String[0]);
    }

    /**
     * Update the generated keys obtained from the prepared statement (if it supports it) that performed
     * the insert into the data source from which the given {@code CachedRowSet} object is derived.
     *
     * @param crs the {@code CachedRowSet} object that has had a row inserted
     *        and to whose underlying data source the row will be inserted
     *        to execute the insertion
     * @param stmt the {@code PreparedStatement} object that will be used
     *        to execute the insertion
     * @throws SQLException if a database access error occurs
     */
    private void updateGeneratedKeys(CachedRowSet crs, PreparedStatement stmt)
        throws SQLException {
        String select = null;
        Map<Object, Integer> param = new HashMap<>();

        try (ResultSet rsKey = stmt.getGeneratedKeys()) {
            ResultSetMetaData mdKey = rsKey.getMetaData();
            int keyCount = mdKey.getColumnCount();
            if (keyCount < tabCols.length) {
                select = buildSelectNewInsertedRow(rsKey, mdKey , param);
            } else {
                updateCachedRowSet(crs, rsKey);
            }
        }
        if (select != null) {
            executeSelectNewInsertedRow(crs, param, select);
        }
    }

    private void executeSelectNewInsertedRow(CachedRowSet crs, Map<Object, Integer> param,
                                             String select)
        throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(select)) {
            int i = 1;
            for (Entry<Object, Integer> entry : param.entrySet()) {
                stmt.setObject(i, entry.getKey(), entry.getValue());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                updateCachedRowSet(crs, rs);
            }
        }
    }

    private String buildSelectNewInsertedRow(ResultSet rsKey, ResultSetMetaData mdKey,
                                             Map<Object, Integer> param)
        throws SQLException {
        String select = null;

        if (rsKey.next()) {
            int count = mdKey.getColumnCount();
            StringJoiner predicates = new StringJoiner(" AND ");
            for (int i = 0; i < keyCols.length; i++) {
                int index = keyCols[i];
                setWherePredicate(rsKey, mdKey, param, predicates, count, index);
            }

            // With MariaDB Column name can't be retrieved we need to retrieve the table's primary key
            if (predicates.length() == 0 && hasPrimarykeys) {
                for (int i = 0; i < keyCols.length; i++) {
                    int index = keyCols[i];
                    setWherePredicatePk(rsKey, mdKey, param, predicates, count, index);
                }
            }
            if (predicates.length() > 0) {
                select = selectCmd + " WHERE " + predicates.toString();
            }
        }
        return select;
    }

    private void setWherePredicate(ResultSet rsKey, ResultSetMetaData mdKey,
                                   Map<Object, Integer> param, StringJoiner predicates,
                                   int count, int index)
        throws SQLException {
        String predicate = null;
        String column = callerMd.getColumnName(index);
        int type = callerMd.getColumnType(index);
        for (int j = 1; j <= count; j++) {
            if (column.equals(mdKey.getColumnName(j))) {
                predicate = getWherePredicate(rsKey, param, column, type, j);
                break;
            }
        }
        if (predicate != null) {
            predicates.add(predicate);
        }
    }

    private void setWherePredicatePk(ResultSet rsKey, ResultSetMetaData mdKey,
                                     Map<Object, Integer> param, StringJoiner predicates,
                                     int count, int index)
        throws SQLException {
        String predicate = null;
        String column = callerMd.getColumnName(index);
        int type = callerMd.getColumnType(index);
        for (int j = 1; j <= count; j++) {
            if (RowSetHelper.isSimilarType(mdKey.getColumnType(j), type)) {
                predicate = getWherePredicate(rsKey, param, column, type, j);
                break;
            }
        }
        if (predicate != null) {
            predicates.add(predicate);
        }
    }

    private String getWherePredicate(ResultSet rs, Map<Object, Integer> param, String column,
                                     int type, int colIndex)
        throws SQLException {
        Object value = rs.getObject(colIndex);
        Boolean wasnull = rs.wasNull();
        if (!wasnull) {
            param.put(value, type);
        }
        return getWherePredicate(wasnull, column);
    }

    private String getWherePredicate(ResultSet rs, Object[] param, String column,
                                     int colIndex, int index)
        throws SQLException {
        Object value = rs.getObject(colIndex);
        Boolean wasnull = rs.wasNull();
        if (!wasnull) {
            param[index] = value;
        }
        return getWherePredicate(wasnull, column);
    }

    private String getWherePredicate(boolean wasnull, String column)
        throws SQLException {
        String predicate = getQuotedIdentifier(column);
        if (wasnull) {
            predicate += " IS NULL";
        } else {
            predicate += " = ?";
        }
        return predicate;
    }

    private void updateCachedRowSet(CachedRowSet crs, ResultSet rsKey)
        throws SQLException {
        ResultSetMetaData mdKey = rsKey.getMetaData();
        if (rsKey.next()) {
            for (int i = 1; i <= mdKey.getColumnCount(); i++) {
                int j = 0;
                try {
                    j = crs.findColumn(mdKey.getColumnLabel(i));
                } catch (SQLException e) { }
                if (j > 0 && callerMd.getColumnType(j) == mdKey.getColumnType(i)) {
                    Object keyval = rsKey.getObject(i);
                    if (keyval != null) {
                        crs.updateObject(j, keyval);
                    } else {
                        crs.updateNull(j);
                    }
                }
            }
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

        String predicate = buildWhereClause(origVals);

        try (PreparedStatement stmt = con.prepareStatement(selectCmd + predicate,
                                                           ResultSet.TYPE_SCROLL_SENSITIVE,
                                                           ResultSet.CONCUR_READ_ONLY)) {

            setStatementParameters(stmt);
            setStatementProperties(crs, stmt);

            if (hasPrimarykeys) {
                deleteCurrentRow(origVals, stmt, predicate, row);
            } else {
                deleteCurrentRowWithCheck(origVals, stmt, predicate, row);
            }
        } catch (SQLException e) {
            status.add(row, SyncResolver.DELETE_ROW_CONFLICT);
            setResolverConflict(crsRes, origVals);
            throw e;
        }
    }

    private void deleteCurrentRow(ResultSet origVals, PreparedStatement stmt,
                                  String predicate, int row)
        throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                deleteCurrentRow(origVals, rs, predicate, row);
            } else {
                // didn't find the row
                String msg = resBundle.handleGetObject("crswriter.delete.norow.error").toString();
                throw new SQLException(MessageFormat.format(msg, row));
            }
        }
    }

    private void deleteCurrentRowWithCheck(ResultSet origVals, PreparedStatement stmt,
                                           String predicate, int row)
        throws SQLException {
        boolean deleted = false;
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                if (rs.next()) {
                    // more than one row
                    int duplicate = 2;
                    while (rs.next()) {
                        duplicate++;
                    }
                    String msg = resBundle.handleGetObject("crswriter.delete.duplicate.error").toString();
                    throw new SQLException(MessageFormat.format(msg, row, duplicate));
                }
                if (rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
                    rs.first();
                    deleteCurrentRow(origVals, rs, predicate, row);
                    deleted = true;
                }
            } else {
                String msg = resBundle.handleGetObject("crswriter.delete.norow.error").toString();
                throw new SQLException(MessageFormat.format(msg, row));
            }
        }

        // XXX: We need to close the ResultSet and open an other one for
        // XXX: database like SQLite supporting only ResultSet.TYPE_FORWARD_ONLY
        if (!deleted) {
            deleteCurrentRow(origVals, stmt, predicate, row);
        }
    }

    private void deleteCurrentRow(ResultSet origVals, ResultSet rs,
                                  String predicate, int row)
        throws SQLException {
        // Now check all the values in rs to be same in
        // db also before actually going ahead with deleting

        if (isCachedRowSetModified(origVals, rs)) {
            String msg = resBundle.handleGetObject("crswriter.delete.conflict.error").toString();
            throw new SQLException(MessageFormat.format(msg, row));
        }
        executeDeleteStatement(deleteCmd + predicate, row);
    }

    private boolean isCachedRowSetModified(ResultSet origVals, ResultSet rs)
        throws SQLException {
        boolean modified = false;

        for (int i = 0; i < tabCols.length; i++) {
            int index = tabCols[i];
            Object original = origVals.getObject(index);
            Object changed = rs.getObject(index);

            if (original != null && changed != null ) {
                if (!(original.toString()).equals(changed.toString())) {
                    modified = true;
                    break;
                }
            }
        }
        return modified;
    }

    private void executeDeleteStatement(String query, int row) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            int index = 0;
            StringJoiner values = new StringJoiner(", ");
            for (int i = 0; i < keyCols.length; i++) {
                Object obj = params[i];
                if (obj != null) {
                    stmt.setObject(++index, obj);
                    values.add(obj.toString());
                }
            }

            int count = stmt.executeUpdate();
            if (count != 1) {
                String msg = resBundle.handleGetObject("crswriter.delete.cmd.error").toString();
                throw new SQLException(MessageFormat.format(msg, row, query, count));
            }
            log(Level.INFO, "crswriter.delete.cmd", row, query, values.toString());
        }
    }

    private void setResolverNoConflict(CachedRowSetImpl crsRes) throws SQLException {
        crsRes.moveToInsertRow();
        for (int cols = 1; cols <= callerColumnCount; cols++) {
            crsRes.updateNull(cols);
        }
        crsRes.insertRow();
        crsRes.moveToCurrentRow();
    }

    private void setResolverConflict(CachedRowSetImpl crsRes, ResultSet origVals)
        throws SQLException {
        crsRes.moveToInsertRow();
        for (int i = 0; i < tabCols.length; i++) {
            int index = tabCols[i];
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

        callerMd = caller.getMetaData();
        callerColumnCount = callerMd.getColumnCount();
        if (callerColumnCount > 0) {
            DatabaseMetaData dbmd = con.getMetaData();
            identifierQuote = dbmd.getIdentifierQuoteString();
            supportsGeneratedKeys = dbmd.supportsGetGeneratedKeys();
            /*
             * set the key descriptors that will be
             * needed to construct where clauses.
             */
            buildKeyDesc(dbmd, caller);

            if (keyCols != null && keyCols.length > 0) {
                setSQLStatements(dbmd);
            }
        }
    }

    private void setSQLStatements(DatabaseMetaData dbmd)
        throws SQLException {
        /*
         * If the RowSet has a Table name we should use it.
         * This is really a hack to get round the fact that
         * a lot of the jdbc drivers can't provide the tab.
         */
        int index = keyCols[0];
        String table = callerMd.getTableName(index);
        if (table == null || table.isBlank()) {
            throw new SQLException(resBundle.handleGetObject("crswriter.tname").toString());
        }
        String catalog = callerMd.getCatalogName(index);
        String schema = callerMd.getSchemaName(index);

        String tableName = buildTableName(dbmd, catalog, schema, table);

        /*
         * Compose a SELECT statement.
         */
        selectCmd = "SELECT " + getSelectColumn() + " FROM " + tableName;

        /*
         * Compose an UPDATE statement.
         */
        updateCmd = "UPDATE " + tableName + " SET ";

        /*
         * Compose an INSERT statement.
         */
        insertCmd = "INSERT INTO " + tableName + composeInsertStatement();

        /*
         * Compose a DELETE statement.
         */
        deleteCmd = "DELETE FROM " + tableName;

    }

    private String getSelectColumn()
        throws SQLException {
        StringJoiner names = new StringJoiner(", ") ;

        for (int i = 0; i < tabCols.length; i++) {
            String name = callerMd.getColumnName(tabCols[i]);
            names.add(getQuotedIdentifier(name));
        }

        return names.toString();
    }

    private String composeInsertStatement()
        throws SQLException {

        int count = 0;
        StringJoiner names = new StringJoiner(", ", "(", ")");
        for (int i = 0; i < tabCols.length; i++) {
            int index = tabCols[i];
            if (isInsertableColumns(index)) {
                // XXX: Auto-increment columns are ignored during inserts (needed by PostGreSQL)
                names.add(getQuotedIdentifier(callerMd.getColumnName(index)));
                count++;
            }
        }

        StringJoiner values = new StringJoiner(", ", "(", ")");
        for (int i = 0; i < count; i++) {
            values.add("?");
        }

        return String.format(" %s VALUES %s", names.toString(), values.toString());
    }

    private boolean isInsertableColumns(int i)
        throws SQLException {
        return !callerMd.isAutoIncrement(i) && !callerMd.isReadOnly(i) && callerMd.isWritable(i);
    }

    /**
     * Returns a fully qualified table name built from the given catalog and
     * table names. The given metadata object is used to get the proper order
     * and separator.
     *
     * @param dbmd a {@code DatabaseMetaData} object that contains metadata
     *          about this writer's {@code CachedRowSet} object
     * @param catalog a {@code String} object with the rowset's catalog
     *          name
     * @param schema a {@code String} object with the rowset's schema
     *          name
     * @param table a {@code String} object with the name of the table from
     *          which this writer's rowset was derived
     * @return a {@code String} object with the fully qualified name of the
     *          table from which this writer's rowset was derived
     * @throws SQLException if a database access error occurs
     */
    private String buildTableName(DatabaseMetaData dbmd, String catalog, String schema, String table)
        throws SQLException {

        StringBuilder tableName = new StringBuilder();

        if (dbmd.isCatalogAtStart()) {
            if (catalog != null && !catalog.isBlank()) {
                tableName.append(getQuotedIdentifier(catalog));
                tableName.append(dbmd.getCatalogSeparator());
            }
            if (schema != null && !schema.isBlank()) {
                tableName.append(getQuotedIdentifier(schema));
                tableName.append(DOT);
            }
            tableName.append(getQuotedIdentifier(table));
        } else {
            if (schema != null && !schema.isBlank()) {
                tableName.append(getQuotedIdentifier(schema));
                tableName.append(DOT);
            }
            tableName.append(getQuotedIdentifier(table));
            if (catalog != null && !catalog.isBlank()) {
                tableName.append(dbmd.getCatalogSeparator());
                tableName.append(getQuotedIdentifier(catalog));
            }
        }
        return tableName.toString();
    }

    private String getQuotedIdentifier(String identifier) {
        return identifierQuote + identifier.trim() + identifierQuote;
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
     * @param caller a {@code CachedRowSet} object for which this
     *        {@code CachedRowSetWriter} object is the writer
     *
     * @throws SQLException if a database access error occurs
     */

    private void buildKeyDesc(DatabaseMetaData dbmd, CachedRowSet caller) throws SQLException {

        int[] keys = caller.getKeyColumns();
        if (keys == null || keys.length == 0) {
            throw new SQLException(resBundle.handleGetObject("crswriter.coldesc").toString());
        }
        keyCols = keys;
        params = new Object[keys.length];
        int index = keys[0];
        String catalog = callerMd.getCatalogName(index);
        String schema = callerMd.getSchemaName(index);
        String table = callerMd.getTableName(index);
        tabCols = getTableColumns(catalog, schema, table);
        hasPrimarykeys = RowSetHelper.hasPrimaryKeys(dbmd, catalog, schema, table);
    }

    private int[] getTableColumns(String catalog, String schema, String table)
        throws SQLException {
        List<Integer> colIndexes = new ArrayList<>();
        for (int i = 1; i <= callerColumnCount; i++) {
            if (isSameTableColumn(catalog, schema, table, i)) {
                colIndexes.add(i);
            }
        }
        return colIndexes.stream().mapToInt(Integer::intValue).toArray();
    }

    private boolean isSameTableColumn(String catalog, String schema, String table, int i)
        throws SQLException {
        return compare(callerMd.getCatalogName(i), catalog) &&
               compare(callerMd.getSchemaName(i), schema) &&
               compare(callerMd.getTableName(i), table);
    }

    private static boolean compare(String str1, String str2) {
        return str1 == null && str2 == null || str1.equals(str2);
    }

    /**
     * Constructs an SQL {@code WHERE} clause using the given
     * string as a starting point. The resulting clause will contain
     * a column name and " = ?" for each key column, that is, each column
     * that is needed to form a unique identifier for a row in the rowset.
     * This {@code WHERE} clause can be added to
     * a {@code PreparedStatement} object that updates, inserts, or
     * deletes a row.
     * <P>
     * This method uses the given result set to access values in the
     * {@code CachedRowSet} object that called this writer.  These
     * values are used to build the array of parameters that will serve as
     * replacements for the "?" parameter placeholders in the
     * {@code PreparedStatement} object that is sent to the
     * {@code CachedRowSet} object's underlying data source.
     *
     * @param rs a {@code ResultSet} object that can be used
     *           to access the {@code CachedRowSet} object's data
     * @return a {@code WHERE} clause of the form "{@code WHERE}
     *         columnName = ? AND columnName = ? AND columnName = ? ..."
     * @throws SQLException if a database access error occurs
     */
    private String buildWhereClause(ResultSet rs)
        throws SQLException {
        StringJoiner predicates = new StringJoiner(" AND ");
        for (int i = 0; i < keyCols.length; i++) {
            int index = keyCols[i];
            predicates.add(getWherePredicate(rs, params, callerMd.getColumnName(index), index, i));
        }
        return " WHERE " + predicates.toString();
    }

    void updateResolvedConflictToDB(CachedRowSet crs, Connection connection) throws SQLException {
        PreparedStatement stmt;
        int colCount = crs.getMetaData().getColumnCount();
        int keyColumns[] = crs.getKeyColumns();
        Object param[];

        if (keyColumns == null || keyColumns.length == 0) {
            keyColumns = new int[colCount];
            for (int i = 0; i < keyColumns.length; ) {
                keyColumns[i] = ++i;
            }
        }
        param = new Object[keyColumns.length];

        String strUpdate = "UPDATE " + buildTableName(connection.getMetaData(),
                                                      crs.getMetaData().getCatalogName(1),
                                                      crs.getMetaData().getSchemaName(1),
                                                      crs.getTableName());

        // keycols will become part of where clause
        strUpdate += getUpdateSetCmd(crs, colCount);

        StringJoiner predicates = new StringJoiner(" AND ");
        for (int i = 0; i < keyColumns.length; i++) {
            String col = crs.getMetaData().getColumnName(keyColumns[i]);
            predicates.add(getWherePredicate(crs, param, col, keyColumns[i], i));
        }
        strUpdate += " WHERE " + predicates.toString();

        stmt = connection.prepareStatement(strUpdate);

        int idx = 0;
        for (int i = 0; i < colCount; i++) {
            if (crs.columnUpdated(i + 1)) {
                Object obj = crs.getObject(i + 1);
                if (obj != null) {
                    stmt.setObject(++idx, obj);
                } else {
                    stmt.setNull(i + 1,crs.getMetaData().getColumnType(i + 1));
                }
            }
        }

        // Set the key cols for after WHERE = ? clause
        for (int i = 0; i < keyColumns.length; i++) {
            if (param[i] != null) {
                stmt.setObject(++idx, param[i]);
            }
        }

        stmt.executeUpdate();
    }

    private String getUpdateSetCmd(CachedRowSet crs, int colCount) throws SQLException {
        // changed or updated values will become part of
        // set clause here
        String setCmd = " SET ";
        StringJoiner updateSet = new StringJoiner(", ");
        for (int i = 1; i <= colCount;i++) {
            if (crs.columnUpdated(i)) {
                updateSet.add(getQuotedIdentifier(crs.getMetaData().getColumnName(i)) + " = ?");
            }
        }
        return setCmd + updateSet.toString();
    }

    /**
     *
     */
    public void commit() throws SQLException {
        con.commit();
        if (reader.getCloseConnection()) {
            con.close();
        }
    }

    public void commit(CachedRowSetImpl crs, boolean updateRowset) throws SQLException {
        con.commit();
        if (updateRowset) {
            if (crs.getCommand() != null) {
                crs.execute(con);
            }
        }

        if (reader.getCloseConnection()) {
            con.close();
        }
    }

    /**
     *
     */
    public void rollback() throws SQLException {
        con.rollback();
        if (reader.getCloseConnection()) {
            con.close();
        }
    }

    public void rollback(Savepoint s) throws SQLException {
        con.rollback(s);
        if (reader.getCloseConnection()) {
            con.close();
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
