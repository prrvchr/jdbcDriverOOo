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
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.Vector;

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
     * The SQL {@code WHERE} clause the writer will use for update
     * statements in the {@code PreparedStatement} object
     * it sends to the underlying data source.
     *
     * @serial
     */
    private String updateWhere;

    /**
     * The SQL {@code DELETE} command that this writer will call
     * internally to delete a row in the rowset's underlying data source.
     *
     * @serial
     */
    private String deleteCmd;

    /**
     * The SQL {@code WHERE} clause the writer will use for delete
     * statements in the {@code PreparedStatement} object
     * it sends to the underlying data source.
     *
     * @serial
     */
    private String deleteWhere;

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
     * An array containing the column numbers of the columns that are
     * needed to uniquely identify a row in the {@code CachedRowSet} object
     * for which this {@code CachedRowSetWriter} object is the writer.
     *
     * @serial
     */
    private int[] keyCols;

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

    /**
     * This {@code CachedRowSet} will hold the conflicting values
     * retrieved from the db and hold it.
     */
    private CachedRowSetImpl crsResolve;

    /**
     * This {@code ArrayList} will hold the values of SyncResolver.
     */
    private ArrayList<Integer> status;

    /**
     * This will check whether the same field value has changed both
     * in database and CachedRowSet.
     */
    private int iChangedValsInDbAndCRS;

    /**
     * This will hold the number of cols for which the values have
     * changed only in database.
     */
    private int iChangedValsinDbOnly ;

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
        boolean conflict = false;
        long conflicts = 0;
        boolean showDel = false;
        iChangedValsInDbAndCRS = 0;
        iChangedValsinDbOnly = 0;

        // We assume caller is a CachedRowSet
        CachedRowSetImpl crs = (CachedRowSetImpl)caller;
        crsResolve = new CachedRowSetImpl();

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

        initSQLStatements(con.getMetaData(), crs);
        int iColCount;

        RowSetMetaDataImpl rsmdWrite = (RowSetMetaDataImpl) crs.getMetaData();
        RowSetMetaDataImpl rsmdResolv = new RowSetMetaDataImpl();

        iColCount = rsmdWrite.getColumnCount();
        int sz = crs.size() + 1;
        status = new ArrayList<>(sz);

        status.add(0,null);
        rsmdResolv.setColumnCount(iColCount);

        for (int i = 1; i <= iColCount; i++) {
            rsmdResolv.setColumnType(i, rsmdWrite.getColumnType(i));
            rsmdResolv.setColumnName(i, rsmdWrite.getColumnName(i));
            rsmdResolv.setNullable(i, ResultSetMetaData.columnNullableUnknown);
        }
        crsResolve.setMetaData(rsmdResolv);

        if (callerColumnCount < 1) {
            // No data, so return success.
            if (reader.getCloseConnection()) {
                con.close();
            }
            conflict = true;
        } else {
            // We need to see rows marked for deletion.
            showDel = crs.getShowDeleted();
            crs.setShowDeleted(true);

            // Look at all the rows.
            crs.beforeFirst();

            int opt;
            if (con.getMetaData().supportsGetGeneratedKeys()) {
                opt = Statement.RETURN_GENERATED_KEYS;
            } else {
                opt = Statement.NO_GENERATED_KEYS;
            }
            System.out.println("CachedRowSetWriter.writeData() 1 opt: " + opt);
            int rows = 1;
            while (crs.next()) {
                if (crs.rowDeleted()) {
                    // The row has been deleted.
                    if (deleteOriginalRow(crs, crsResolve)) {
                        status.add(rows, SyncResolver.DELETE_ROW_CONFLICT);
                        conflicts++;
                    } else {
                        // delete happened without any occurrence of conflicts
                        // so update status accordingly
                        status.add(rows, SyncResolver.NO_ROW_CONFLICT);
                    }

                } else if (crs.rowInserted()) {
                    // The row has been inserted.

                    if (insertNewRow(crs, opt)) {
                        System.out.println("CachedRowSetWriter.writeData() 2 insertNewRow as conflict");
                        status.add(rows, SyncResolver.INSERT_ROW_CONFLICT);
                        conflicts++;
                    } else {
                        // insert happened without any occurrence of conflicts
                        // so update status accordingly
                        System.out.println("CachedRowSetWriter.writeData() 3 insertNewRow as no conflict");
                        status.add(rows, SyncResolver.NO_ROW_CONFLICT);
                    }
                } else  if (crs.rowUpdated()) {
                    // The row has been updated.
                    if (updateOriginalRow(crs)) {
                        status.add(rows, SyncResolver.UPDATE_ROW_CONFLICT);
                        conflicts++;
                    } else {
                        // update happened without any occurrence of conflicts
                        // so update status accordingly
                        status.add(rows, SyncResolver.NO_ROW_CONFLICT);
                    }

                } else {
                    /** The row is neither of inserted, updated or deleted.
                     *  So set nulls in the this.crsResolve for this row,
                     *  as nothing is to be done for such rows.
                     *  Also note that if such a row has been changed in database
                     *  and we have not changed(inserted, updated or deleted)
                     *  that is fine.
                     **/
                    status.add(rows, SyncResolver.NO_ROW_CONFLICT);

                    crsResolve.moveToInsertRow();
                    for (int cols = 0; cols < iColCount; cols++) {
                        crsResolve.updateNull(cols + 1);
                    } //end for

                    crsResolve.insertRow();
                    crsResolve.moveToCurrentRow();

                }
                rows++;
            }

            // reset
            crs.setShowDeleted(showDel);

            crs.beforeFirst();
            crsResolve.beforeFirst();

            if (conflicts != 0) {
                SyncProviderException spe = new SyncProviderException(conflicts + " " +
                    resBundle.handleGetObject("crswriter.conflictsno").toString());
                //SyncResolver syncRes = spe.getSyncResolver();

                SyncResolverImpl syncResImpl = (SyncResolverImpl) spe.getSyncResolver();

                syncResImpl.setCachedRowSet(crs);
                syncResImpl.setCachedRowSetResolver(crsResolve);

                syncResImpl.setStatus(status);
                syncResImpl.setCachedRowSetWriter(this);

                throw spe;
            } else {
                conflict = true;
            }
        }
        return conflict;
    }

    /**
     * Updates the given {@code CachedRowSet} object's underlying data
     * source so that updates to the rowset are reflected in the original
     * data source, and returns {@code false} if the update was successful.
     * A return value of {@code true} indicates that there is a conflict,
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
     * @return {@code false} if the update to the underlying data source is
     *         successful; {@code true} otherwise
     * @throws SQLException if a database access error occurs
     */
    private boolean updateOriginalRow(CachedRowSet crs)
        throws SQLException {
        boolean failed = false;
        PreparedStatement pstmt;
        int i = 0;
        int idx = 0;

        // Select the row from the database.
        ResultSet origVals = crs.getOriginalRow();
        origVals.next();

        try {
            updateWhere = buildWhereClause(updateWhere, origVals);

            /**
             *  The following block of code is for checking a particular type of
             *  query where in there is a where clause. Without this block, if a
             *  SQL statement is built the "where" clause will appear twice hence
             *  the DB errors out and a SQLException is thrown. This code also
             *  considers that the where clause is in the right place as the
             *  CachedRowSet object would already have been populated with this
             *  query before coming to this point.
             **/

            String tempselectCmd = selectCmd.toLowerCase();

            int idxWhere = tempselectCmd.indexOf("where");

            if (idxWhere != -1) {
                String tempSelect = selectCmd.substring(0,idxWhere);
                selectCmd = tempSelect;
            }

            pstmt = con.prepareStatement(selectCmd + updateWhere,
                        ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            for (i = 0; i < keyCols.length; i++) {
                if (params[i] == null) {
                    continue;
                }
                pstmt.setObject(++idx, params[i]);
            }

            try {
                pstmt.setMaxRows(crs.getMaxRows());
                pstmt.setMaxFieldSize(crs.getMaxFieldSize());
                pstmt.setEscapeProcessing(crs.getEscapeProcessing());
                pstmt.setQueryTimeout(crs.getQueryTimeout());
            } catch (Exception ex) {
                // Older driver don't support these operations.
            }

            ResultSet rs = null;
            rs = pstmt.executeQuery();

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
                    failed = true;
                }

                if (!failed) {
                    rs.first();
                    failed = updateOriginalRow(crs, pstmt, rs, origVals);
                } else {
                    rs.close();
                    pstmt.close();
                }

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
                failed = true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            // if executeUpdate fails it will come here,
            // update crsResolve with null rows
            crsResolve.moveToInsertRow();

            for (i = 1; i <= callerColumnCount; i++) {
                crsResolve.updateNull(i);
            }

            crsResolve.insertRow();
            crsResolve.moveToCurrentRow();

            failed = true;
        }
        return failed;
    }

    private boolean updateOriginalRow(CachedRowSet crs, PreparedStatement pstmt, ResultSet rs, ResultSet origVals)
        throws SQLException {
        boolean failed = false;

        // how many fields need to be updated
        int colsNotChanged = 0;
        Vector<Integer> cols = new Vector<>();
        String updateExec = updateCmd;
        Object orig;
        Object curr;
        Object rsval;
        boolean boolNull = true;
        Object objVal = null;

        // There's only one row and the cursor
        // needs to be on that row.

        boolean first = true;
        boolean flag = true;

        crsResolve.moveToInsertRow();

        for (int i = 1; i <= callerColumnCount; i++) {
            orig = origVals.getObject(i);
            curr = crs.getObject(i);
            rsval = rs.getObject(i);
            /*
             * the following block creates equivalent objects
             * that would have been created if this rs is populated
             * into a CachedRowSet so that comparison of the column values
             * from the ResultSet and CachedRowSet are possible
             */
            Map<String, Class<?>> map;
            if (crs.getTypeMap() == null) {
                map = con.getTypeMap();
            } else {
                map = crs.getTypeMap();
            }
            if (rsval instanceof Struct) {

                Struct s = (Struct)rsval;

                // look up the class in the map
                Class<?> c = null;
                c = map.get(s.getSQLTypeName());
                if (c != null) {
                    // create new instance of the class
                    SQLData obj = null;
                    try {
                        Object tmp = c.getDeclaredConstructor().newInstance();
                        obj = (SQLData)tmp;
                    } catch (Exception ex) {
                        throw new SQLException("Unable to Instantiate: ", ex);
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
                rsval = new SerialStruct((SQLData)rsval, map);
            } else if (rsval instanceof Blob) {
                rsval = new SerialBlob((Blob)rsval);
            } else if (rsval instanceof Clob) {
                rsval = new SerialClob((Clob)rsval);
            } else if (rsval instanceof java.sql.Array) {
                rsval = new SerialArray((java.sql.Array)rsval, map);
            }

            // reset boolNull if it had been set
            boolNull = true;

            /** This additional checking has been added when the current value
             *  in the DB is null, but the DB had a different value when the
             *  data was actually fetched into the CachedRowSet.
             **/

            if (rsval == null && orig != null) {
                // value in db has changed
                // don't proceed with synchronization
                // get the value in db and pass it to the resolver.

                iChangedValsinDbOnly++;
                // Set the boolNull to false,
                // in order to set the actual value;
                boolNull = false;
                objVal = rsval;
            } else if (rsval != null && !rsval.equals(orig)) {
                /** Adding the checking for rsval to be "not" null or else
                 *  it would through a NullPointerException when the values
                 *  are compared.
                 **/
                // value in db has changed
                // don't proceed with synchronization
                // get the value in db and pass it to the resolver.

                iChangedValsinDbOnly++;
                // Set the boolNull to false,
                // in order to set the actual value;
                boolNull = false;
                objVal = rsval;
            } else if (orig == null || curr == null) {

                /** Adding the additional condition of checking for "flag"
                 *  boolean variable, which would otherwise result in
                 *  building a invalid query, as the comma would not be
                 *  added to the query string.
                 **/

                if (!first || !flag) {
                    updateExec += ", ";
                }
                updateExec += getQuotedIdentifier(crs.getMetaData().getColumnName(i));
                cols.add(i);
                updateExec += " = ? ";
                first = false;

            /** Adding the extra condition for orig to be "not" null as the
             *  condition for orig to be null is take prior to this, if this
             *  is not added it will result in a NullPointerException when
             *  the values are compared.
             **/

            } else if (orig.equals(curr)) {
                colsNotChanged++;
                //nothing to update in this case since values are equal

            /** Adding the extra condition for orig to be "not" null as the
             *  condition for orig to be null is take prior to this, if this
             *  is not added it will result in a NullPointerException when
             *  the values are compared.
             **/

            } else if (!orig.equals(curr)) {
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

                if (crs.columnUpdated(i)) {
                    if (rsval.equals(orig)) {
                        // At this point we are sure that
                        // the value updated in crs was from
                        // what is in db now and has not changed
                        if (!flag || !first) {
                            updateExec += ", ";
                        }
                        updateExec += getQuotedIdentifier(crs.getMetaData().getColumnName(i));
                        cols.add(i);
                        updateExec += " = ? ";
                        flag = false;
                    } else {
                        // Here the value has changed in the db after
                        // data was fetched
                        // Plus store this row from CachedRowSet and keep it
                        // in a new CachedRowSet
                        boolNull = false;
                        objVal = rsval;
                        iChangedValsInDbAndCRS++;
                    }
                }
            }

            if (!boolNull) {
                crsResolve.updateObject(i,objVal);
            } else {
                crsResolve.updateNull(i);
            }
        }

        rs.close();
        pstmt.close();

        crsResolve.insertRow();
        crsResolve.moveToCurrentRow();

        /**
         * if nothing has changed return now - this can happen
         * if column is updated to the same value.
         * if colsNotChanged == callerColumnCount implies we are updating
         * the database with ALL COLUMNS HAVING SAME VALUES,
         * so skip going to database, else do as usual.
         **/
        if (!first && cols.size() == 0 || colsNotChanged == callerColumnCount ) {
            failed = false;
        } else if (iChangedValsInDbAndCRS != 0 || iChangedValsinDbOnly != 0) {
            failed = true;
        } else {
            failed = executeUpdate(crs, cols, updateExec) != 1;
        }
        return failed;
    }

    private int executeUpdate(CachedRowSet crs, Vector<Integer> cols, String updateExec)
        throws SQLException {
        int state = 0;
        updateExec += updateWhere;

        try (PreparedStatement pstmt = con.prepareStatement(updateExec)) {

            // Comments needed here
            int i;
            List<String> values = new ArrayList<>();
            for (i = 0; i < cols.size(); i++) {
                Object obj = crs.getObject(cols.get(i));
                if (obj != null) {
                    pstmt.setObject(i + 1, obj);
                    values.add(obj.toString());
                } else {
                    pstmt.setNull(i + 1, crs.getMetaData().getColumnType(i + 1));
                    values.add("null");
                }
            }
            int index = i;

            // Comments needed here
            for (i = 0; i < keyCols.length; i++) {
                Object obj = params[i];
                if (obj != null) {
                    pstmt.setObject(++index, obj);
                    values.add(obj.toString());
                }
            }

            String msg = resBundle.handleGetObject("crswriter.executeStatement").toString();
            logger.log(Logger.Level.INFO, MessageFormat.format(msg, updateExec, String.join(", ", values)));

            /**
             * i should be equal to 1(row count), because we update
             * one row(returned as row count) at a time, if all goes well.
             * if 1 != 1, this implies we have not been able to
             * do updations properly i.e there is a conflict in database
             * versus what is in CachedRowSet for this particular row.
             **/
            state = pstmt.executeUpdate();
        }
        return state;
    }

    /**
    * Inserts a row that has been inserted into the given
    * {@code CachedRowSet} object into the data source from which
    * the rowset is derived, returning {@code false} if the insertion
    * was successful.
    *
    * @param crs the {@code CachedRowSet} object that has had a row inserted
    *            and to whose underlying data source the row will be inserted
    * @param opt the {@code PreparedStatement} object that will be used
    * @return {@code false} to indicate that the insertion was successful;
    *         {@code true} otherwise
    * @throws SQLException if a database access error occurs
    */
    private boolean insertNewRow(CachedRowSet crs, int opt) throws SQLException {

        boolean hasConfict = false;
        System.out.println("CachedRowSetWriter.insertNewRow() 1 insertCmd: " + insertCmd);
        System.out.println("CachedRowSetWriter.insertNewRow() 2 selectCmd: " + selectCmd);
        try (PreparedStatement insertStmt = con.prepareStatement(insertCmd, opt);
             PreparedStatement selectStmt = con.prepareStatement(selectCmd,
                       ResultSet.TYPE_SCROLL_SENSITIVE,
                       ResultSet.CONCUR_READ_ONLY);
             ResultSet rs = selectStmt.executeQuery();
             ResultSet rs2 = con.getMetaData().getPrimaryKeys(null, null,
                       crs.getTableName())
        ) {

            ResultSetMetaData rsmd = crs.getMetaData();
            int icolCount = rsmd.getColumnCount();
            String[] primaryKeys = new String[icolCount];
            int k = 0;
            while (rs2.next()) {
                primaryKeys[k] = rs2.getString("COLUMN_NAME");
                k++;
            }

            if (rs.next()) {
                for (String pkName : primaryKeys) {
                    if (!isPKNameValid(pkName, rsmd)) {

                        /* We came here as one of the primary keys
                         * of the table is not present in the cached
                         * rowset object, it should be an autoincrement column
                         * and not included while creating CachedRowSet
                         * Object, proceed to check for other primary keys
                         */
                        System.out.println("CachedRowSetWriter.insertNewRow() 3 continue");
                        continue;
                    }

                    Object crsPK = crs.getObject(pkName);
                    if (crsPK == null) {
                        /*
                         * It is possible that the PK is null on some databases
                         * and will be filled in at insert time (MySQL for example)
                         */
                        System.out.println("CachedRowSetWriter.insertNewRow() 4 break");
                        break;
                    }

                    String rsPK = rs.getObject(pkName).toString();
                    if (crsPK.toString().equals(rsPK)) {
                        hasConfict = true;
                        crsResolve.moveToInsertRow();
                        System.out.println("CachedRowSetWriter.insertNewRow() 5 hasConfict");
                        for (int i = 1; i <= icolCount; i++) {
                            String colname = (rs.getMetaData()).getColumnName(i);
                            if (colname.equals(pkName)) {
                                crsResolve.updateObject(i,rsPK);
                            } else {
                                crsResolve.updateNull(i);
                            }
                        }
                        crsResolve.insertRow();
                        crsResolve.moveToCurrentRow();
                    }
                }
            }

            if (!hasConfict) {
                System.out.println("CachedRowSetWriter.insertNewRow() 6");
                hasConfict = executeInsertStatement(crs, rsmd, insertStmt, rs, icolCount, opt);
            }
        }
        return hasConfict;
    }

    private boolean executeInsertStatement(CachedRowSet crs, ResultSetMetaData rsmd,
                                           PreparedStatement pstmt, ResultSet rs,
                                           int icolCount, int opt)
        throws SQLException {
        boolean conflict = false;
        try {
            // XXX: Auto-increment columns are ignored during inserts (needed by PostGreSQL)
            int index = 1;
            List<String> values = new ArrayList<>();
            for (int i = 1; i <= icolCount; i++) {
                if (!rsmd.isAutoIncrement(i)) {
                    Object obj = crs.getObject(i);
                    if (obj != null) {
                        pstmt.setObject(index, obj);
                    } else {
                        pstmt.setNull(index, rsmd.getColumnType(i));
                    }
                    values.add(obj.toString());
                    index++;
                }
            }
            String msg = resBundle.handleGetObject("crswriter.executeStatement").toString();
            logger.log(Logger.Level.INFO, MessageFormat.format(msg, insertCmd, String.join(", ", values)));
            conflict = pstmt.executeUpdate() != 1;
            if (!conflict && opt == Statement.RETURN_GENERATED_KEYS) {
                updateGeneratedKeys(crs, rsmd, pstmt, rs.getMetaData().getColumnCount());
            }

        } catch (SQLException ex) {
            /*
             * Cursor will come here if executeUpdate fails.
             * There can be many reasons why the insertion failed,
             * one can be violation of primary key.
             * Hence we cannot exactly identify why the insertion failed,
             * present the current row as a null row to the caller.
             */
            crsResolve.moveToInsertRow();

            for (int i = 1; i <= icolCount; i++) {
                crsResolve.updateNull(i);
            }

            crsResolve.insertRow();
            crsResolve.moveToCurrentRow();

            conflict = true;
        }
        return conflict;
    }
    /**
     * Update the generated keys obtained from the prepared statement (if it supports it) that performed
     * the insert into the data source from which the given {@code CachedRowSet} object is derived.
     *
     * @param crs the {@code CachedRowSet} object that has had a row inserted
     *        and to whose underlying data source the row will be inserted
     * @param rsmd the {@code ResultSetMetaData} of the {@code CachedRowSet} object that will be used
     *        to execute the insertion
     * @param pstmt the {@code PreparedStatement} object that will be used
     *        to execute the insertion
     * @param colCount the table {@code CachedRowSet} columns count
     * @throws SQLException if a database access error occurs
     */
    private void updateGeneratedKeys(CachedRowSet crs, ResultSetMetaData rsmd,
                                     PreparedStatement pstmt, int colCount)
        throws SQLException {
        String select = null;
        Object[] param = null;
        try (ResultSet rsKey = pstmt.getGeneratedKeys()) {
            System.out.println("CachedRowSetWriter.updateGeneratedKeys() 1");
            ResultSetMetaData mdKey = rsKey.getMetaData();
            int keyCount = mdKey.getColumnCount();
            System.out.println("CachedRowSetWriter.updateGeneratedKeys() 2 keyCount: " + keyCount +
                               " - colCount: " + colCount);
            if (keyCount < colCount) {
                System.out.println("CachedRowSetWriter.updateGeneratedKeys() 3");
                param = new Object[keyCols.length];
                select = buildSelectNewInsertedRow(rsKey, mdKey , param);
            } else {
                System.out.println("CachedRowSetWriter.updateGeneratedKeys() 4");
                updateCachedRowSet(crs, rsKey, rsmd);
            }
        }
        if (select != null) {
            executeSelectNewInsertedRow(crs, rsmd, param, select);
        }
    }

    private void executeSelectNewInsertedRow(CachedRowSet crs, ResultSetMetaData rsmd,
                                             Object[] param, String select)
        throws SQLException {
        try (PreparedStatement pstmt = con.prepareStatement(select)) {
            executeNewInsertedRow(crs, rsmd, param, pstmt);
        }
    }

    private void executeNewInsertedRow(CachedRowSet crs, ResultSetMetaData rsmd,
                                       Object[] param, PreparedStatement pstmt)
        throws SQLException {
        for (int i = 0; i < param.length; i++) {
            pstmt.setObject(i + 1, param[i]);
        }
        try (ResultSet rs = pstmt.executeQuery()) {
            System.out.println("CachedRowSetWriter.executeNewInsertedRow() 1 *************************************");
            updateCachedRowSet(crs, rs, rsmd);
        }
    }

    private String buildSelectNewInsertedRow(ResultSet rs, ResultSetMetaData rsmd, Object[] param)
        throws SQLException {
        String select = null;
        if (rs.next()) {
            int count = rsmd.getColumnCount();
            String predicates = "";
            for (int i = 0; i < keyCols.length; i++) {
                if (i > 0) {
                    predicates += " AND ";
                }
                String predicate = null;
                String colName = callerMd.getColumnName(keyCols[i]);
                for (int j = 1; j <= count; j++) {
                    System.out.println("CachedRowSetWriter.buildSelectNewInsertedRow() 1 ColumnName: "
                            + rsmd.getColumnName(j));
                    if (colName.equals(rsmd.getColumnName(j))) {
                        predicate = getWherePredicate(rs, param, colName, j, i);
                    }
                }
                // With MariaDB Column name can't be retrieved we follow column order...
                if (predicate == null) {
                    predicate = getWherePredicate(rs, param, colName, i + 1, i);
                }
                predicates += predicate;
            }
            if (!predicates.isEmpty()) {
                select = selectCmd + " WHERE " + predicates;
            }
        }
        System.out.println("CachedRowSetWriter.buildSelectNewInsertedRow() 1 select: " + select);
        return select;
    }

    private String getWherePredicate(ResultSet rs, Object[] param, String colName, int colIndex, int index)
        throws SQLException {
        String predicate = getQuotedIdentifier(colName);
        param[index] = rs.getObject(colIndex);
        if (rs.wasNull()) {
            predicate += " IS NULL";
        } else {
            predicate += " = ?";
        }
        return predicate;
    }
    private void updateCachedRowSet(CachedRowSet crs, ResultSet rsKey, ResultSetMetaData rsmd)
        throws SQLException {
        if (rsKey.next()) {
            ResultSetMetaData keymd = rsKey.getMetaData();
            for (int i = 1; i <= keymd.getColumnCount(); i++) {
                System.out.println("CachedRowSetWriter.updateCachedRowSet() 1 Column: " + keymd.getColumnLabel(i));
                int j = 0;
                try {
                    j = crs.findColumn(keymd.getColumnLabel(i));
                } catch (SQLException e) { }
                if (j > 0 && rsmd.getColumnType(j) == keymd.getColumnType(i)) {
                    Object keyval = rsKey.getObject(i);
                    System.out.println("CachedRowSetWriter.updateCachedRowSet() 2 value: " + keyval);
                    if (keyval != null) {
                        crs.updateObject(j, keyval);
                    } else {
                        crs.updateNull(j);
                    }
                    crs.setOriginalRow();
                    System.out.println("CachedRowSetWriter.updateCachedRowSet() 3");
                }
            }
        }
    }

    /**
     * Deletes the row in the underlying data source that corresponds to
     * a row that has been deleted in the given {@code  CachedRowSet} object
     * and returns {@code false} if the deletion was successful.
     * <P>
     * This method is called internally by this writer's {@code writeData}
     * method when a row in the rowset has been deleted. The values in the
     * deleted row are the same as those that are stored in the original row
     * of the given {@code CachedRowSet} object.  If the values in the
     * original row differ from the row in the underlying data source, the row
     * in the data source is not deleted, and {@code deleteOriginalRow}
     * returns {@code true} to indicate that there was a conflict.
     *
     * @param crs the {@code CachedRowSet} object for which this
     *     {@code CachedRowSetWriter} object is the writer
     * @param crsRes the {@code CachedRowSetImpl} object who as called writeData()
     *
     * @return {@code false} if the deletion was successful, which means that
     *         there was no conflict; {@code true} otherwise
     * @throws SQLException if there was a database access error
     */
    private boolean deleteOriginalRow(CachedRowSet crs, CachedRowSetImpl crsRes) throws SQLException {
        boolean conflict = false;
        int i;
        int index = 0;
        // Select the row from the database.
        ResultSet origVals = crs.getOriginalRow();
        origVals.next();

        deleteWhere = buildWhereClause(deleteWhere, origVals);
        try (PreparedStatement pstmt = con.prepareStatement(selectCmd + deleteWhere,
                                                            ResultSet.TYPE_SCROLL_SENSITIVE,
                                                            ResultSet.CONCUR_READ_ONLY)) {

            for (i = 0; i < keyCols.length; i++) {
                Object obj = params[i];
                if (obj != null) {
                    pstmt.setObject(++index, obj);
                }
            }

            try {
                pstmt.setMaxRows(crs.getMaxRows());
                pstmt.setMaxFieldSize(crs.getMaxFieldSize());
                pstmt.setEscapeProcessing(crs.getEscapeProcessing());
                pstmt.setQueryTimeout(crs.getQueryTimeout());
            } catch (Exception ex) {
                /*
                 * Older driver don't support these operations...
                 */
            }

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    if (rs.next()) {
                        // more than one row
                        conflict = true;
                    } else {
                        rs.first();

                        // Now check all the values in rs to be same in
                        // db also before actually going ahead with deleting
                        crsRes.moveToInsertRow();
                        boolean changed = isCachedRowSetModified(crs, crsRes, rs, origVals);
                        crsRes.insertRow();
                        crsRes.moveToCurrentRow();

                        if (changed) {
                            // do not delete as values in db have changed
                            // deletion will not happen for this row from db
                            // exit now returning true. i.e. conflict
                            conflict = true;
                        } else {
                            conflict = executeDeleteStatement();
                        }
                    }
                } else {
                    // didn't find the row
                    conflict = true;
                }
            }
        }
        return conflict;
    }

    private boolean isCachedRowSetModified(CachedRowSet crs, CachedRowSetImpl crsRes,
                                           ResultSet rs, ResultSet origVals)
        throws SQLException {
        boolean modified = false;

        for (int i = 1; i <= crs.getMetaData().getColumnCount(); i++) {

            Object original = origVals.getObject(i);
            Object changed = rs.getObject(i);

            if (original != null && changed != null ) {
                if (!(original.toString()).equals(changed.toString()) ) {
                    modified = true;
                    crsRes.updateObject(i,origVals.getObject(i));
                }
            } else {
                crsRes.updateNull(i);
            }
        }

        return modified;
    }

    private boolean executeDeleteStatement() throws SQLException {
        boolean conflict = true;
        try (PreparedStatement pstmt = con.prepareStatement(deleteCmd + deleteWhere)) {
            int index = 0;
            List<String> values = new ArrayList<>();
            for (int i = 0; i < keyCols.length; i++) {
                Object obj = params[i];
                if (obj != null) {
                    pstmt.setObject(++index, obj);
                    values.add(obj.toString());
                }
            }

            String msg = resBundle.handleGetObject("crswriter.executeStatement").toString();
            logger.log(Logger.Level.INFO, MessageFormat.format(msg, deleteCmd + deleteWhere,
                                                               String.join(", ", values)));

            conflict = pstmt.executeUpdate() != 1;
        }
        return conflict;
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
     * @param dbmd a {@code DatabaseMetaData} object
     * @param caller a {@code CachedRowSet} object for which this
     *        {@code CachedRowSetWriter} object is the writer
     * @throws SQLException if a database access error occurs
     */
    private void initSQLStatements(DatabaseMetaData dbmd, CachedRowSet caller)
        throws SQLException {

        callerMd = caller.getMetaData();
        callerColumnCount = callerMd.getColumnCount();
        identifierQuote = dbmd.getIdentifierQuoteString();
        if (callerColumnCount < 1) {
            // No data, so return.
            return;
        }

        /*
         * If the RowSet has a Table name we should use it.
         * This is really a hack to get round the fact that
         * a lot of the jdbc drivers can't provide the tab.
         */
        String table = caller.getTableName();
        if (table == null) {
            /*
             * attempt to build a table name using the info
             * that the driver gave us for the first column
             * in the source result set.
             */
            table = callerMd.getTableName(1);
            if (table == null || table.length() == 0) {
                throw new SQLException(resBundle.handleGetObject("crswriter.tname").toString());
            }
        }
        String catalog = callerMd.getCatalogName(1);
        String schema = callerMd.getSchemaName(1);

        /*
         * Compose a SELECT statement.
         */
        selectCmd = composeSelectStatement(dbmd, catalog, schema, table);

        /*
         * Compose an UPDATE statement.
         */
        updateCmd = "UPDATE " + buildTableName(dbmd, catalog, schema, table) + " SET ";

        /*
         * Compose an INSERT statement.
         */
        insertCmd = composeInsertStatement(dbmd, catalog, schema, table);

        /*
         * Compose a DELETE statement.
         */
        deleteCmd = "DELETE FROM " + buildTableName(dbmd, catalog, schema, table);

        /*
         * set the key descriptors that will be
         * needed to construct where clauses.
         */
        buildKeyDesc(caller);
    }

    private String composeSelectStatement(DatabaseMetaData dbmd, String catalog, String schema, String table)
        throws SQLException {
        /*
         * Compose a SELECT statement.  There are three parts.
         */
        String select = "SELECT %s FROM ";
        select += buildTableName(dbmd, catalog, schema, table);

        // Column List
        StringJoiner names = new StringJoiner(", ") ;
        for (int i = 1; i <= callerColumnCount; i++) {
            names.add(getQuotedIdentifier(callerMd.getColumnName(i)));
        }

        return String.format(select, names.toString());
    }

    private String composeInsertStatement(DatabaseMetaData dbmd, String catalog, String schema, String table)
        throws SQLException {
        /*
         * Compose an INSERT statement.
         */
        String insert = "INSERT INTO ";
        insert += buildTableName(dbmd, catalog, schema, table);
        insert += " %s VALUES %s";

        // Column list
        int count = 0;
        StringJoiner names = new StringJoiner(", ", "(", ")") ;
        // XXX: Auto-increment columns are ignored during inserts (needed by PostGreSQL)
        for (int i = 1; i <= callerColumnCount; i++) {
            if (!callerMd.isAutoIncrement(i)) {
                names.add(getQuotedIdentifier(callerMd.getColumnName(i)));
                count++;
            }
        }
        StringJoiner values = new StringJoiner(", ", "(", ")");
        for (int i = 0; i < count; i++) {
            values.add("?");
        }

        return String.format(insert, names.toString(), values.toString());
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
                tableName.append(".");
            }
            tableName.append(getQuotedIdentifier(table));
        } else {
            if (schema != null && !schema.isBlank()) {
                tableName.append(getQuotedIdentifier(schema));
                tableName.append(".");
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
     * is set with the column number of every column in the rowset.
     * Otherwise, the array in the field {@code keyCols} is set with only
     * the column numbers of the columns that are required to form a unique
     * identifier for a row.
     *
     * @param crs the {@code CachedRowSet} object for which this
     *     {@code CachedRowSetWriter} object is the writer
     *
     * @throws SQLException if a database access error occurs
     */
    private void buildKeyDesc(CachedRowSet crs) throws SQLException {

        keyCols = crs.getKeyColumns();
        ResultSetMetaData rsmd = crs.getMetaData();
        if (keyCols == null || keyCols.length == 0) {
            List<Integer> keys = new ArrayList<>();

            for (int i = 0; i < callerColumnCount; i++ ) {
                switch (rsmd.getColumnType(i + 1)) {
                    case java.sql.Types.CLOB:
                    case java.sql.Types.STRUCT:
                    case java.sql.Types.SQLXML:
                    case java.sql.Types.BLOB:
                    case java.sql.Types.ARRAY:
                    case java.sql.Types.OTHER:
                        break;
                    default:
                        keys.add(i + 1);
                }
            }
            keyCols = new int[keys.size()];
            for (int i = 0; i < keys.size(); i++ ) {
                keyCols[i] = keys.get(i);
            }
        }
        params = new Object[keyCols.length];
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
     * @param whereClause a {@code String} object that is an empty
     *                    string ("")
     * @param rs a {@code ResultSet} object that can be used
     *           to access the {@code CachedRowSet} object's data
     * @return a {@code WHERE} clause of the form "{@code WHERE}
     *         columnName = ? AND columnName = ? AND columnName = ? ..."
     * @throws SQLException if a database access error occurs
     */
    private String buildWhereClause(String whereClause, ResultSet rs)
        throws SQLException {
        for (int i = 0; i < keyCols.length; i++) {
            if (i > 0) {
                whereClause += " AND ";
            }
            whereClause += getWherePredicate(rs, params, callerMd.getColumnName(keyCols[i]), keyCols[i], i);
            //whereClause += getQuotedIdentifier(callerMd.getColumnName(keyCols[i]));
            //params[i] = rs.getObject(keyCols[i]);
            //if (rs.wasNull()) {
            //    whereClause += " IS NULL ";
            //} else {
            //    whereClause += " = ? ";
            //}
        }
        return whereClause;
    }

    void updateResolvedConflictToDB(CachedRowSet crs, Connection connection) throws SQLException {
        //String updateExe = ;
        PreparedStatement pStmt  ;
        String strWhere = "WHERE " ;
        @SuppressWarnings("unused")
        String strExec = " ";
        String strUpdate = "UPDATE ";
        int icolCount = crs.getMetaData().getColumnCount();
        int keyColumns[] = crs.getKeyColumns();
        Object param[];
        String strSet = "";

        strWhere = buildWhereClause(strWhere, crs);

        if (keyColumns == null || keyColumns.length == 0) {
            keyColumns = new int[icolCount];
            for (int i = 0; i < keyColumns.length; ) {
                keyColumns[i] = ++i;
            }
        }
        param = new Object[keyColumns.length];

        strUpdate = "UPDATE " + buildTableName(connection.getMetaData(),
                                               crs.getMetaData().getCatalogName(1),
                                               crs.getMetaData().getSchemaName(1),
                                               crs.getTableName());

        // changed or updated values will become part of
        // set clause here
        strUpdate += " SET ";

        boolean first = true;

        for (int i = 1; i <= icolCount;i++) {
            if (crs.columnUpdated(i)) {
                if (!first) {
                    strSet += ", ";
                }
                strSet += getQuotedIdentifier(crs.getMetaData().getColumnName(i));
                strSet += " = ? ";
                first = false;
            }
        }

        // keycols will become part of where clause
        strUpdate += strSet;
        strWhere = "WHERE ";

        for (int i = 0; i < keyColumns.length; i++) {
            if (i > 0) {
                strWhere += " AND ";
            }
            strWhere += getWherePredicate(crs, param, crs.getMetaData().getColumnName(keyColumns[i]), keyColumns[i], i);
        }
        strUpdate += strWhere;

        pStmt = connection.prepareStatement(strUpdate);

        int idx = 0;
        for (int i = 0; i < icolCount; i++) {
            if (crs.columnUpdated(i + 1)) {
                Object obj = crs.getObject(i + 1);
                if (obj != null) {
                    pStmt.setObject(++idx, obj);
                } else {
                    pStmt.setNull(i + 1,crs.getMetaData().getColumnType(i + 1));
                }
            }
        }

        // Set the key cols for after WHERE =? clause
        for (int i = 0; i < keyColumns.length; i++) {
            if (param[i] != null) {
                pStmt.setObject(++idx, param[i]);
            }
        }

        @SuppressWarnings("unused")
        int id = pStmt.executeUpdate();
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

    /**
     * Validate whether the Primary Key is known to the CachedRowSet.  If it is
     * not, it is an auto-generated key
     * @param pk - Primary Key to validate
     * @param rsmd - ResultSetMetadata for the RowSet
     * @return true if found, false otherwise (auto generated key)
     */
    private boolean isPKNameValid(String pk, ResultSetMetaData rsmd) throws SQLException {
        boolean isValid = false;
        int cols = rsmd.getColumnCount();
        for (int i = 1; i <= cols; i++) {
            String colName = rsmd.getColumnName(i);
            System.out.println("CachedRowSetWriter.isPKNameValid() 1 colName: " + colName + " - pkName: " + pk);
            if (colName.equalsIgnoreCase(pk)) {
                isValid = true;
                break;
            }
        }

        return isValid;
    }

}
