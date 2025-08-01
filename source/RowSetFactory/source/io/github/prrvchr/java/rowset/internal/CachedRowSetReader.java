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
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.sql.RowSet;
import javax.sql.RowSetInternal;
import javax.sql.RowSetReader;
import javax.sql.rowset.CachedRowSet;

import io.github.prrvchr.java.rowset.CachedRowSetImpl;
import io.github.prrvchr.java.rowset.JdbcRowSetResourceBundle;


/**
 * The facility called by the <code>RIOptimisticProvider</code> object
 * internally to read data into it.  The calling <code>RowSet</code> object
 * must have implemented the <code>RowSetInternal</code> interface
 * and have the standard <code>CachedRowSetReader</code> object set as its
 * reader.
 * <P>
 * This implementation always reads all rows of the data source,
 * and it assumes that the <code>command</code> property for the caller
 * is set with a query that is appropriate for execution by a
 * <code>PreparedStatement</code> object.
 * <P>
 * Typically the <code>SyncFactory</code> manages the <code>RowSetReader</code> and
 * the <code>RowSetWriter</code> implementations using <code>SyncProvider</code> objects.
 * Standard JDBC RowSet implementations provide an object instance of this
 * reader by invoking the <code>SyncProvider.getRowSetReader()</code> method.
 *
 * @author Jonathan Bruce
 * @see javax.sql.rowset.spi.SyncProvider
 * @see javax.sql.rowset.spi.SyncFactory
 * @see javax.sql.rowset.spi.SyncFactoryException
 */
public class CachedRowSetReader implements RowSetReader, Serializable {

    static final long serialVersionUID = 5049738185801363801L;

    /**
     * The field that keeps track of whether the writer associated with
     * this <code>CachedRowSetReader</code> object's rowset has been called since
     * the rowset was populated.
     * <P>
     * When this <code>CachedRowSetReader</code> object reads data into
     * its rowset, it sets the field <code>writerCalls</code> to 0.
     * When the writer associated with the rowset is called to write
     * data back to the underlying data source, its <code>writeData</code>
     * method calls the method <code>CachedRowSetReader.reset</code>,
     * which increments <code>writerCalls</code> and returns <code>true</code>
     * if <code>writerCalls</code> is 1. Thus, <code>writerCalls</code> equals
     * 1 after the first call to <code>writeData</code> that occurs
     * after the rowset has had data read into it.
     *
     * @serial
     */
    private int writerCalls = 0;

    private boolean userCon = false;

    private int startPosition;

    private JdbcRowSetResourceBundle resBundle;

    public CachedRowSetReader() {
        try {
            resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }


    /**
     * Reads data from a data source and populates the given
     * <code>RowSet</code> object with that data.
     * This method is called by the rowset internally when
     * the application invokes the method <code>execute</code>
     * to read a new set of rows.
     * <P>
     * After clearing the rowset of its contents, if any, and setting
     * the number of writer calls to <code>0</code>, this reader calls
     * its <code>connect</code> method to make
     * a connection to the rowset's data source. Depending on which
     * of the rowset's properties have been set, the <code>connect</code>
     * method will use a <code>DataSource</code> object or the
     * <code>DriverManager</code> facility to make a connection to the
     * data source.
     * <P>
     * Once the connection to the data source is made, this reader
     * executes the query in the calling <code>CachedRowSet</code> object's
     * <code>command</code> property. Then it calls the rowset's
     * <code>populate</code> method, which reads data from the
     * <code>ResultSet</code> object produced by executing the rowset's
     * command. The rowset is then populated with this data.
     * <P>
     * This method's final act is to close the connection it made, thus
     * leaving the rowset disconnected from its data source.
     *
     * @param caller a <code>RowSet</code> object that has implemented
     *               the <code>RowSetInternal</code> interface and had
     *               this <code>CachedRowSetReader</code> object set as
     *               its reader
     * @throws SQLException if there is a database access error, there is a
     *         problem making the connection, or the command property has not
     *         been set
     */
    public void readData(RowSetInternal caller) throws SQLException {
        Connection con = null;
        try {
            CachedRowSet crs = (CachedRowSet)caller;

            // Get rid of the current contents of the rowset.

            /**
             * Checking added to verify whether page size has been set or not.
             * If set then do not close the object as certain parameters need
             * to be maintained.
             */

            if (crs.getPageSize() == 0 && crs.size() > 0) {
                // When page size is not set,
                // crs.size() will show the total no of rows.
                crs.close();
            }

            writerCalls = 0;

            // Get a connection.  This reader assumes that the necessary
            // properties have been set on the caller to let it supply a
            // connection.
            userCon = false;

            con = connect(caller);

            // Check our assumptions.
            if (con == null || crs.getCommand() == null) {
                throw new SQLException(resBundle.handleGetObject("crsreader.connecterr").toString());
            }

            try {
                con.setTransactionIsolation(crs.getTransactionIsolation());
            } catch (Exception ex) { }

            loadData(caller, crs, con);

            try {
                con.commit();
            } catch (SQLException ex) { }
            // only close connections we created...
            if (getCloseConnection()) {
                con.close();
            }
        } catch (SQLException ex) {
            // Throw an exception if reading fails for any reason.
            throw ex;
        } finally {
            closeConnection(con);
        }
    }

    private void loadData(RowSetInternal caller, CachedRowSet crs, Connection con)
        throws SQLException {
        // Use JDBC to read the data.
        PreparedStatement pstmt = con.prepareStatement(crs.getCommand());
        // Pass any input parameters to JDBC.

        decodeParams(caller.getParams(), pstmt);
        try {
            pstmt.setMaxRows(crs.getMaxRows());
            pstmt.setMaxFieldSize(crs.getMaxFieldSize());
            pstmt.setEscapeProcessing(crs.getEscapeProcessing());
            pstmt.setQueryTimeout(crs.getQueryTimeout());
        } catch (Exception ex) {
            /*
             * drivers may not support the above - esp. older
             * drivers being used by the bridge..
             */
            throw new SQLException(ex.getMessage());
        }

        if (crs.getCommand().toLowerCase().indexOf("select") != -1) {
            // can be (crs.getCommand()).indexOf("select")) == 0
            // because we will be getting resultset when
            // it may be the case that some false select query with
            // select coming in between instead of first.

            // if ((crs.getCommand()).indexOf("?")) does not return -1
            // implies a Prepared Statement like query exists.

            ResultSet rs = pstmt.executeQuery();
            if (crs.getPageSize() == 0) {
                crs.populate(rs);
            } else {
                /**
                 * If page size has been set then create a ResultSet object that is scrollable using a
                 * PreparedStatement handle. Also call the populate(ResultSet,int) function to populate
                 * a page of data as specified by the page size.
                 */
                pstmt = con.prepareStatement(crs.getCommand(), ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                               ResultSet.CONCUR_UPDATABLE);
                decodeParams(caller.getParams(), pstmt);
                try {
                    pstmt.setMaxRows(crs.getMaxRows());
                    pstmt.setMaxFieldSize(crs.getMaxFieldSize());
                    pstmt.setEscapeProcessing(crs.getEscapeProcessing());
                    pstmt.setQueryTimeout(crs.getQueryTimeout());
                } catch (Exception ex) {
                    /*
                     * drivers may not support the above - esp. older
                     * drivers being used by the bridge..
                     */
                    throw new SQLException(ex.getMessage());
                }
                rs = pstmt.executeQuery();
                crs.populate(rs,startPosition);
            }
            rs.close();
        } else  {
            pstmt.executeUpdate();
        }
        // Get the data.
        pstmt.close();
    }

    private void closeConnection(Connection con) {
        try {
            // only close connections we created...
            if (con != null && getCloseConnection()) {
                try {
                    if (!con.getAutoCommit()) {
                        con.rollback();
                    }
                } catch (Exception dummy) {
                    /*
                     * not an error condition, we're closing anyway, but
                     * we'd like to clean up any locks if we can since
                     * it is not clear the connection pool will clean
                     * these connections in a timely manner
                     */
                }
                con.close();
                con = null;
            }
        } catch (SQLException e) {
            // will get exception if something already went wrong, but don't
            // override that exception with this one
        }
    }

    /**
     * Checks to see if the writer associated with this reader needs
     * to reset its state.  The writer will need to initialize its state
     * if new contents have been read since the writer was last called.
     * This method is called by the writer that was registered with
     * this reader when components were being wired together.
     *
     * @return <code>true</code> if writer associated with this reader needs
     *         to reset the values of its fields; <code>false</code> otherwise
     * @throws SQLException if an access error occurs
     */
    public boolean reset() throws SQLException {
        writerCalls++;
        return writerCalls == 1;
    }

    /**
     * Establishes a connection with the data source for the given
     * <code>RowSet</code> object.  If the rowset's <code>dataSourceName</code>
     * property has been set, this method uses the JNDI API to retrieve the
     * <code>DataSource</code> object that it can use to make the connection.
     * If the url, username, and password properties have been set, this
     * method uses the <code>DriverManager.getConnection</code> method to
     * make the connection.
     * <P>
     * This method is used internally by the reader and writer associated with
     * the calling <code>RowSet</code> object; an application never calls it
     * directly.
     *
     * @param caller a <code>RowSet</code> object that has implemented
     *               the <code>RowSetInternal</code> interface and had
     *               this <code>CachedRowSetReader</code> object set as
     *               its reader
     * @return a <code>Connection</code> object that represents a connection
     *         to the caller's data source
     * @throws SQLException if an access error occurs
     */
    public Connection connect(RowSetInternal caller) throws SQLException {

        Connection connection = null;
        // Get a JDBC connection.
        if (caller.getConnection() != null) {
            // A connection was passed to execute(), so use it.
            // As we are using a connection the user gave us we
            // won't close it.
            userCon = true;
            connection = caller.getConnection();
        } else if (((RowSet) caller).getDataSourceName() != null) {
            // Connect using JNDI.
            try {
                Context ctx = new InitialContext();
                DataSource ds = (DataSource)ctx.lookup(((RowSet) caller).getDataSourceName());

                // Check for username, password,
                // if it exists try getting a Connection handle through them
                // else try without these
                // else throw SQLException

                if (((RowSet) caller).getUsername() != null) {
                    connection = ds.getConnection(((RowSet)caller).getUsername(), ((RowSet)caller).getPassword());
                } else {
                    connection = ds.getConnection();
                }
            } catch (javax.naming.NamingException ex) {
                SQLException sqlEx = new SQLException(resBundle.handleGetObject("crsreader.connect").toString());
                sqlEx.initCause(ex);
                throw sqlEx;
            }
        } else if (((RowSet)caller).getUrl() != null) {
            // Connect using the driver manager.
            connection = DriverManager.getConnection(((RowSet)caller).getUrl(),
                                                     ((RowSet)caller).getUsername(),
                                                     ((RowSet)caller).getPassword());
        }
        return connection;
    }

    /**
     * Sets the parameter placeholders
     * in the rowset's command (the given <code>PreparedStatement</code>
     * object) with the parameters in the given array.
     * This method, called internally by the method
     * <code>CachedRowSetReader.readData</code>, reads each parameter, and
     * based on its type, determines the correct
     * <code>PreparedStatement.setXXX</code> method to use for setting
     * that parameter.
     *
     * @param params an array of parameters to be used with the given
     *               <code>PreparedStatement</code> object
     * @param pstmt  the <code>PreparedStatement</code> object that is the
     *               command for the calling rowset and into which
     *               the given parameters are to be set
     * @throws SQLException if an access error occurs
     */
    private void decodeParams(Object[] params, PreparedStatement pstmt) throws SQLException {
        // There is a corresponding decodeParams in JdbcRowSetImpl
        // which does the same as this method. This is a design flaw.
        // Update the JdbcRowSetImpl.decodeParams when you update
        // this method.
    
        // Adding the same comments to JdbcRowSetImpl.decodeParams.

        Object[] param = null;
        final int twoParam = 2;
        final int treeParam = 3;

        for (int i = 0; i < params.length; i++) {
            if (params[i] instanceof Object[]) {
                param = (Object[]) params[i];
                switch (param.length) {
                    case twoParam:
                        setTwoParam(pstmt, param, i);
                        break;
                    case treeParam:
                        setTreeParam(pstmt, param, i);
                        break;
                    default:
                        // common case - this catches all SQL92 types
                        pstmt.setObject(i + 1, param);
                }
            }  else {
                // Try to get all the params to be set here
                pstmt.setObject(i + 1, params[i]);
            }
        }
    }

    private void setTwoParam(PreparedStatement pstmt, Object[] param, int i) throws SQLException {
        if (param[0] == null) {
            pstmt.setNull(i + 1, ((Integer) param[1]).intValue());
        } else if (param[0] instanceof java.sql.Date ||
                   param[0] instanceof java.sql.Time ||
                   param[0] instanceof java.sql.Timestamp) {
            System.err.println(resBundle.handleGetObject("crsreader.datedetected").toString());
            if (param[1] instanceof java.util.Calendar) {
                System.err.println(resBundle.handleGetObject("crsreader.caldetected").toString());
                pstmt.setDate(i + 1, (java.sql.Date) param[0], (java.util.Calendar) param[1]);
            } else {
                throw new SQLException(resBundle.handleGetObject("crsreader.paramtype").toString());
            }
        } else if (param[0] instanceof Reader) {
            pstmt.setCharacterStream(i + 1, (Reader) param[0], ((Integer) param[1]).intValue());
        } else if (param[1] instanceof Integer) {
            /*
             * What's left should be setObject(int, Object, scale)
             */
            pstmt.setObject(i + 1, param[0], ((Integer) param[1]).intValue());
        }
    }

    @SuppressWarnings("deprecation")
    private void setTreeParam(PreparedStatement pstmt, Object[] param, int i) throws SQLException {
        if (param[0] == null) {
            pstmt.setNull(i + 1, ((Integer) param[1]).intValue(), (String) param[2]);
        } else if (param[0] instanceof InputStream) {
            switch (((Integer)param[2]).intValue()) {
                case CachedRowSetImpl.UNICODE_STREAM_PARAM:
                    pstmt.setUnicodeStream(i + 1, (InputStream) param[0], ((Integer) param[1]).intValue());
                    break;
                case CachedRowSetImpl.BINARY_STREAM_PARAM:
                    pstmt.setBinaryStream(i + 1, (InputStream) param[0], ((Integer) param[1]).intValue());
                    break;
                case CachedRowSetImpl.ASCII_STREAM_PARAM:
                    pstmt.setAsciiStream(i + 1, (InputStream) param[0], ((Integer) param[1]).intValue());
                    break;
                default:
                    throw new SQLException(resBundle.handleGetObject("crsreader.paramtype").toString());
            }
        } else if (param[1] instanceof Integer && param[2] instanceof Integer) {
            /*
             * no point at looking at the first element now;
             * what's left must be the setObject() cases.
             */
            pstmt.setObject(i + 1, param[0], ((Integer) param[1]).intValue(),
                                             ((Integer) param[2]).intValue());
        } else {
            throw new SQLException(resBundle.handleGetObject("crsreader.paramtype").toString());
        }
    }

    /**
     * Assists in determining whether the current connection was created by this
     * CachedRowSet to ensure incorrect connections are not prematurely terminated.
     *
     * @return a boolean giving the status of whether the connection has been closed.
     */
    protected boolean getCloseConnection() {
        return !userCon;
    }

    /**
     *  This sets the start position in the ResultSet from where to begin. This is
     * called by the Reader in the CachedRowSetImpl to set the position on the page
     * to begin populating from.
     * @param pos integer indicating the position in the <code>ResultSet</code> to begin
     *        populating from.
     */
    public void setStartPosition(int pos) {
        startPosition = pos;
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

}
