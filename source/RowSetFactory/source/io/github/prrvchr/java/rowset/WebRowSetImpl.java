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
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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

package io.github.prrvchr.java.rowset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import javax.sql.rowset.WebRowSet;
import javax.sql.rowset.spi.SyncFactory;
import javax.sql.rowset.spi.SyncProvider;

import io.github.prrvchr.java.rowset.internal.WebRowSetXmlReader;
import io.github.prrvchr.java.rowset.internal.WebRowSetXmlWriter;


/**
 * The standard implementation of the <code>WebRowSet</code> interface. See the interface
 * definition for full behavior and implementation requirements.
 *
 * @author Jonathan Bruce, Amit Handa
 */
public class WebRowSetImpl extends CachedRowSetImpl implements WebRowSet {

    static final long serialVersionUID = -8771775154092422943L;

    /**
     * The <code>WebRowSetXmlReader</code> object that this
     * <code>WebRowSet</code> object will call when the method
     * <code>WebRowSet.readXml</code> is invoked.
     */
    private WebRowSetXmlReader xmlReader;

    /**
     * The <code>WebRowSetXmlWriter</code> object that this
     * <code>WebRowSet</code> object will call when the method
     * <code>WebRowSet.writeXml</code> is invoked.
     */
    private WebRowSetXmlWriter xmlWriter;

    /* This stores the cursor position prior to calling the writeXML.
     * This variable is used after the write to restore the position
     * to the point where the writeXml was called.
     */
    private int curPosBfrWrite;

    @SuppressWarnings("unused")
    private SyncProvider provider;

    /**
     * Constructs a new <code>WebRowSet</code> object initialized with the
     * default values for a <code>CachedRowSet</code> object instance. This
     * provides the <code>RIOptimistic</code> provider to deliver
     * synchronization capabilities to relational datastores and a default
     * <code>WebRowSetXmlReader</code> object and a default
     * <code>WebRowSetXmlWriter</code> object to enable XML output
     * capabilities.
     *
     * @throws SQLException if an error occurs in configuring the default
     * synchronization providers for relational and XML providers.
     */
    public WebRowSetImpl() throws SQLException {
        super();

        // %%%
        // Needs to use to SPI  XmlReader,XmlWriters
        //
        xmlReader = new WebRowSetXmlReader();
        xmlWriter = new WebRowSetXmlWriter();
    }

    /**
     * Constructs a new <code>WebRowSet</code> object initialized with the
     * synchronization SPI provider properties as specified in the <code>Hashtable</code>. If
     * this hashtable is empty or is <code>null</code> the default constructor is invoked.
     *
     * @param env the <code>Hashtable<?, ?></code> object
     *
     * @throws SQLException if an error occurs in configuring the specified
     * synchronization providers for the relational and XML providers; or
     * if the Hashtanle is null
     */
    public WebRowSetImpl(Hashtable<?, ?> env) throws SQLException {

        try {
            resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        if ( env == null) {
            throw new SQLException(resBundle.handleGetObject("webrowsetimpl.nullhash").toString());
        }

        String providerName =
            (String)env.get(javax.sql.rowset.spi.SyncFactory.ROWSET_SYNC_PROVIDER);

        // set the Reader, this maybe overridden latter
        provider = SyncFactory.getInstance(providerName);

        // xmlReader = provider.getRowSetReader();
        // xmlWriter = provider.getRowSetWriter();
    }

    /**
     * Populates this <code>WebRowSet</code> object with the
     * data in the given <code>ResultSet</code> object and writes itself
     * to the given <code>java.io.Writer</code> object in XML format.
     * This includes the rowset's data,  properties, and metadata.
     *
     * @param rs the <code>java.sql.ResultSet</code> object
     * @param writer the <code>java.io.Writer</code> object
     *
     * @throws SQLException if an error occurs writing out the rowset
     *          contents to XML
     */
    public void writeXml(ResultSet rs, java.io.Writer writer)
        throws SQLException {
        // WebRowSetImpl wrs = new WebRowSetImpl();
        this.populate(rs);

        // Store the cursor position before writing
        curPosBfrWrite = this.getRow();

        this.writeXml(writer);
    }

    /**
     * Writes this <code>WebRowSet</code> object to the given
     * <code>java.io.Writer</code> object in XML format. This
     * includes the rowset's data,  properties, and metadata.
     *
     * @param writer the <code>java.io.Writer</code> object
     *
     * @throws SQLException if an error occurs writing out the rowset
     *          contents to XML
     */
    public void writeXml(java.io.Writer writer) throws SQLException {
        // %%%
        // This will change to a XmlReader, which over-rides the default
        // Xml that is used when a WRS is instantiated.
        // WebRowSetXmlWriter xmlWriter = getXmlWriter();
        if (xmlWriter != null) {

            // Store the cursor position before writing
            curPosBfrWrite = this.getRow();

            xmlWriter.writeXML(this, writer);
        } else {
            throw new SQLException(resBundle.handleGetObject("webrowsetimpl.invalidwr").toString());
        }
    }

    /**
     * Reads this <code>WebRowSet</code> object in its XML format.
     *
     * @param reader the <code>java.io.Reader</code> object
     *
     * @throws SQLException if a database access error occurs
     */
    public void readXml(java.io.Reader reader) throws SQLException {
        // %%%
        // This will change to a XmlReader, which over-rides the default
        // Xml that is used when a WRS is instantiated.
        //WebRowSetXmlReader xmlReader = getXmlReader();
        try {
            if (reader != null) {
                xmlReader.readXML(this, reader);

                // Position is before the first row
                // The cursor position is to be stored while serializng
                // and deserializing the WebRowSet Object.
                if (curPosBfrWrite == 0) {
                    beforeFirst();
                } else {
                    // Return the position back to place prior to callin writeXml
                    absolute(curPosBfrWrite);
                }

            } else {
                throw new SQLException(resBundle.handleGetObject("webrowsetimpl.invalidrd").toString());
            }
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
    }

    // Stream based methods
    /**
     * Reads a stream based XML input to populate this <code>WebRowSet</code>
     * object.
     *
     * @param iStream the <code>java.io.InputStream</code> object
     *
     * @throws SQLException if a data source access error occurs
     * @throws IOException if a IO exception occurs
     */
    public void readXml(java.io.InputStream iStream) throws SQLException, IOException {
        if (iStream != null) {
            xmlReader.readXML(this, iStream);

            // Position is before the first row
            // The cursor position is to be stored while serializng
            // and deserializing the WebRowSet Object.
            if (curPosBfrWrite == 0) {
                this.beforeFirst();
            } else {
                // Return the position back to place prior to callin writeXml
                this.absolute(curPosBfrWrite);
            }

        } else {
            throw new SQLException(resBundle.handleGetObject("webrowsetimpl.invalidrd").toString());
        }
    }

    /**
     * Writes this <code>WebRowSet</code> object to the given <code> OutputStream</code>
     * object in XML format.
     * Creates an output stream of the internal state and contents of a
     * <code>WebRowSet</code> for XML proceessing
     *
     * @param oStream the <code>java.io.OutputStream</code> object
     *
     * @throws SQLException if a datasource access error occurs
     * @throws IOException if an IO exception occurs
     */
    public void writeXml(java.io.OutputStream oStream) throws SQLException, IOException {
        if (xmlWriter != null) {

            // Store the cursor position before writing
            curPosBfrWrite = this.getRow();

            xmlWriter.writeXML(this, oStream);
        } else {
            throw new SQLException(resBundle.handleGetObject("webrowsetimpl.invalidwr").toString());
        }

    }

    /**
     * Populates this <code>WebRowSet</code> object with the
     * data in the given <code>ResultSet</code> object and writes itself
     * to the given <code>java.io.OutputStream</code> object in XML format.
     * This includes the rowset's data,  properties, and metadata.
     *
     * @param rs the <code>java.sql.ResultSet</code> object
     * @param oStream the <code>java.io.OutputStream</code> object
     *
     * @throws SQLException if a datasource access error occurs
     * @throws IOException if an IO exception occurs
     */
    public void writeXml(ResultSet rs, java.io.OutputStream oStream) throws SQLException, IOException {
        populate(rs);

        // Store the cursor position before writing
        curPosBfrWrite = this.getRow();

        this.writeXml(oStream);
    }

    /**
     * This method re populates the resBundle
     * during the deserialization process.
     *
     * @param ois the <code>ObjectInputStream</code> object
     *
     */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // Default state initialization happens here
        ois.defaultReadObject();
        // Initialization of transient Res Bundle happens here .
        try {
            resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

    }

}
