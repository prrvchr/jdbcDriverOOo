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
/**************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 *************************************************************/
package io.github.prrvchr.uno.driver.provider;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSetMetaData;
import java.sql.RowIdLifetime;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.sun.star.beans.Property;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.XIndexAccess;
import com.sun.star.io.XInputStream;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.adapter.XInputStreamToInputStreamAdapter;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.DataType;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XAppend;
import com.sun.star.sdbcx.XColumnsSupplier;
import com.sun.star.uno.Any;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Exception;
import com.sun.star.uno.Type;
import com.sun.star.uno.TypeClass;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XInterface;
import com.sun.star.util.Date;
import com.sun.star.util.DateTime;
import com.sun.star.util.DateTimeWithTimezone;
import com.sun.star.util.DateWithTimezone;
import com.sun.star.util.Time;
import com.sun.star.util.TimeWithTimezone;

import io.github.prrvchr.uno.driver.config.ConfigDDL;
import io.github.prrvchr.uno.driver.config.ParameterDDL;
import io.github.prrvchr.uno.driver.helper.ComponentHelper;
import io.github.prrvchr.uno.driver.helper.StandardSQLState;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedComponent;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedSupport;
import io.github.prrvchr.uno.driver.property.PropertyID;
import io.github.prrvchr.uno.helper.ResourceBasedEventLogger;
import io.github.prrvchr.uno.helper.UnoHelper;


public class DBTools {

    public static final Comparator<String> getComparator(boolean sensitive) {
        return new Comparator<String>() {
            @Override
            public int compare(String x, String y) {
                int comp;
                if (sensitive) {
                    comp = x.compareTo(y);
                } else {
                    comp = x.compareToIgnoreCase(y);
                }
                return comp;
            }
        };
    }

    /** creates a SQL CREATE VIEW statement.
     *
     * @param config
     *    The DDL configuration.
     * @param support
     *    The named component support.
     * @param descriptor
     *    The descriptor of the new view.
     * @param sensitive
     *    Is the name case sensitive.
     *
     * @return
     *   The CREATE VIEW statement.
     */
    public static String getCreateViewQuery(ConfigDDL config,
                                            NamedSupport support,
                                            XPropertySet descriptor,
                                            boolean sensitive)
        throws java.sql.SQLException {
        String view = ComponentHelper.composeTableName(support, descriptor, sensitive);
        String command = getDescriptorStringValue(descriptor, PropertyID.COMMAND);
        String query = config.getCreateViewCommand(ParameterDDL.getCreateView(view, command));
        System.out.println("DBTools.getCreateViewQuery() Query: " + query);
        return query;
    }

    /** creates a SQL CREATE VIEW statement.
     *
     * @param config
     *    The DDL configuration.
     * @param support
     *    The named component support.
     * @param component
     *    The component name.
     * @param command
     *    The SQL command to create view.
     * @param sensitive
     *    Is the name case sensitive.
     * @return The CREATE VIEW statement.
     */
    public static String getCreateViewQuery(ConfigDDL config,
                                            NamedSupport support,
                                            NamedComponent component,
                                            String command,
                                            boolean sensitive)
        throws java.sql.SQLException {
        String view = ComponentHelper.composeTableName(support, component, sensitive);
        String query = config.getCreateViewCommand(ParameterDDL.getCreateView(view, command));
        System.out.println("DBTools.getViewCommand() Query: " + query);
        return query;
    }

    public static void cloneDescriptorColumns(XPropertySet source,
                                              XPropertySet destination)
        throws java.sql.SQLException {
        XColumnsSupplier sourceColumnsSupplier = UnoRuntime.queryInterface(XColumnsSupplier.class, source);
        XIndexAccess sourceColumns = UnoRuntime.queryInterface(XIndexAccess.class, sourceColumnsSupplier.getColumns());
        
        XColumnsSupplier destinationColumnsSupplier = UnoRuntime.queryInterface(XColumnsSupplier.class, destination);
        XAppend destinationAppend = UnoRuntime.queryInterface(XAppend.class, destinationColumnsSupplier.getColumns());
        
        int count = sourceColumns.getCount();
        for (int i = 0; i < count; i++) {
            try {
                XPropertySet columnProperties = UnoRuntime.queryInterface(XPropertySet.class,
                                                                          sourceColumns.getByIndex(i));
                destinationAppend.appendByDescriptor(columnProperties);
            } catch (WrappedTargetException | IndexOutOfBoundsException |
                     IllegalArgumentException | ElementExistException | SQLException exception) {
                throw new java.sql.SQLException("Error", StandardSQLState.SQL_GENERAL_ERROR.text(), 0, exception);
            }
        }
    }

    public static boolean updateObject(java.sql.ResultSet resultset,
                                       int index,
                                       Object any)
        throws SQLException {
        try {
            boolean success = true;
            Type type = AnyConverter.getType(any);
            switch (type.getTypeClass().getValue()) {
                case TypeClass.VOID_value:
                    resultset.updateNull(index);
                    break;
                case TypeClass.STRING_value:
                    resultset.updateString(index, AnyConverter.toString(any));
                    break;
                case TypeClass.BOOLEAN_value:
                    resultset.updateBoolean(index, AnyConverter.toBoolean(any));
                    break;
                case TypeClass.BYTE_value:
                    resultset.updateByte(index, AnyConverter.toByte(any));
                    break;
                case TypeClass.UNSIGNED_SHORT_value:
                    resultset.updateShort(index, AnyConverter.toUnsignedShort(any));
                    break;
                case TypeClass.SHORT_value:
                    resultset.updateShort(index, AnyConverter.toShort(any));
                    break;
                case TypeClass.CHAR_value:
                    resultset.updateString(index, Character.toString(AnyConverter.toChar(any)));
                    break;
                case TypeClass.UNSIGNED_LONG_value:
                    resultset.updateInt(index, AnyConverter.toUnsignedInt(any));
                    break;
                case TypeClass.LONG_value:
                    resultset.updateInt(index, AnyConverter.toInt(any));
                    break;
                case TypeClass.UNSIGNED_HYPER_value:
                    resultset.updateLong(index, AnyConverter.toUnsignedLong(any));
                    break;
                case TypeClass.HYPER_value:
                    resultset.updateLong(index, AnyConverter.toLong(any));
                    break;
                case TypeClass.FLOAT_value:
                    resultset.updateFloat(index, AnyConverter.toFloat(any));
                    break;
                case TypeClass.DOUBLE_value:
                    resultset.updateDouble(index, AnyConverter.toDouble(any));
                    break;
                case TypeClass.SEQUENCE_value:
                    success = updateSequence(resultset, index, any);
                    break;
                case TypeClass.STRUCT_value:
                    success = updateStruct(resultset, index, any);
                    break;
                case TypeClass.INTERFACE_value:
                    success = updateInterface(resultset, index, any);
                    break;
                default:
                    success = false;
            }
            return success;
        } catch (IllegalArgumentException | java.sql.SQLException | java.io.IOException e) {
            throw new SQLException("Error", Any.VOID, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    private static boolean updateSequence(java.sql.ResultSet resultset,
                                   int index,
                                   Object any)
        throws java.sql.SQLException {
        boolean success = true;
        if (AnyConverter.isArray(any)) {
            Object array = AnyConverter.toArray(any);
            if (array instanceof byte[]) {
                resultset.updateBytes(index, (byte[]) array);
            } else {
                success = false;
            }
        } else {
            success = false;
        }
        return success;
    }

    private static boolean updateStruct(java.sql.ResultSet resultset,
                                        int index,
                                        Object any)
        throws java.sql.SQLException {
        boolean success = true;
        Object object = AnyConverter.toObject(Object.class, any);
        if (object instanceof Date) {
            resultset.updateObject(index, UnoHelper.getJavaLocalDate((Date) object));
        } else if (object instanceof Time) {
            resultset.updateObject(index, UnoHelper.getJavaLocalTime((Time) object));
        } else if (object instanceof DateTime) {
            resultset.updateObject(index, UnoHelper.getJavaLocalDateTime((DateTime) object));
        } else {
            success = false;
        }
        return success;
    }

    private static boolean updateInterface(java.sql.ResultSet resultset,
                                           int index,
                                           Object any)
        throws java.sql.SQLException, IOException {
        boolean success = true;
        XInputStream stream = UnoRuntime.queryInterface(XInputStream.class,
                                                        AnyConverter.toObject(Object.class, any));
        if (stream != null) {
            InputStream input = new XInputStreamToInputStreamAdapter(stream);
            resultset.updateBinaryStream(index, input, input.available());
        } else {
            success = false;
        }
        return success;
    }

    public static boolean setObject(java.sql.PreparedStatement statement,
                                    int index,
                                    Object any)
        throws SQLException {
        Type type = AnyConverter.getType(any);
        try {
            boolean success = true;
            switch (type.getTypeClass().getValue()) {
                case TypeClass.HYPER_value:
                    statement.setLong(index, AnyConverter.toLong(any));
                    break;
                case TypeClass.UNSIGNED_HYPER_value:
                    statement.setLong(index, AnyConverter.toUnsignedLong(any));
                    break;
                case TypeClass.VOID_value:
                    statement.setNull(index, DataType.VARCHAR);
                    break;
                case TypeClass.STRING_value:
                    statement.setString(index, AnyConverter.toString(any));
                    break;
                case TypeClass.BOOLEAN_value:
                    statement.setBoolean(index, AnyConverter.toBoolean(any));
                    break;
                case TypeClass.BYTE_value:
                    statement.setByte(index, AnyConverter.toByte(any));
                    break;
                case TypeClass.SHORT_value:
                    statement.setShort(index, AnyConverter.toShort(any));
                    break;
                case TypeClass.UNSIGNED_SHORT_value:
                    statement.setShort(index, AnyConverter.toUnsignedShort(any));
                    break;
                case TypeClass.CHAR_value:
                    statement.setString(index, Character.toString(AnyConverter.toChar(any)));
                    break;
                case TypeClass.LONG_value:
                    statement.setInt(index, AnyConverter.toInt(any));
                    break;
                case TypeClass.UNSIGNED_LONG_value:
                    statement.setInt(index, AnyConverter.toUnsignedInt(any));
                    break;
                case TypeClass.FLOAT_value:
                    statement.setFloat(index, AnyConverter.toFloat(any));
                    break;
                case TypeClass.DOUBLE_value:
                    statement.setDouble(index, AnyConverter.toDouble(any));
                    break;
                case TypeClass.SEQUENCE_value:
                    success = setSequence(statement, index, any);
                    break;
                case TypeClass.STRUCT_value:
                    success = setStruct(statement, index, any);
                    break;
                case TypeClass.INTERFACE_value:
                    success = setInterface(statement, index, any);
                    break;
                default:
                    success = false;
            }
            return success;
        } catch (java.sql.SQLException | IllegalArgumentException | java.io.IOException e) {
            throw new SQLException("Error", Any.VOID, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
    }

    private static boolean setSequence(java.sql.PreparedStatement statement,
                                       int index,
                                       Object any)
        throws java.sql.SQLException {
        boolean success = true;
        if (AnyConverter.isArray(any)) {
            Object array = AnyConverter.toArray(any);
            if (array instanceof byte[]) {
                statement.setBytes(index, (byte[])array);
            } else {
                success = false;
            }
        } else {
            success = false;
        }
        return success;
    }

    private static boolean setStruct(java.sql.PreparedStatement statement,
                                     int index,
                                     Object any)
        throws java.sql.SQLException {
        boolean success = true;
        if (any instanceof Date) {
            statement.setObject(index, UnoHelper.getJavaLocalDate((Date) any));
        } else if (any instanceof Time) {
            statement.setObject(index, UnoHelper.getJavaLocalTime((Time) any));
        } else if (any instanceof DateTime) {
            statement.setObject(index, UnoHelper.getJavaLocalDateTime((DateTime) any));
        } else if (any instanceof DateWithTimezone) {
            DateWithTimezone date = (DateWithTimezone) any;
            statement.setObject(index, UnoHelper.getJavaLocalDate(date.DateInTZ));
        } else if (any instanceof TimeWithTimezone) {
            TimeWithTimezone time = (TimeWithTimezone) any;
            statement.setObject(index, UnoHelper.getJavaOffsetTime(time));
        } else if (any instanceof DateTimeWithTimezone) {
            DateTimeWithTimezone datetime = (DateTimeWithTimezone) any;
            statement.setObject(index, UnoHelper.getJavaOffsetDateTime(datetime));
        } else {
            success = false;
        }
        return success;
    }

    private static boolean setInterface(java.sql.PreparedStatement statement,
                                        int index,
                                        Object any)
        throws java.sql.SQLException, IOException {
        boolean success = true;
        XInputStream stream = UnoRuntime.queryInterface(XInputStream.class,
                                                        AnyConverter.toObject(Object.class, any));
        if (stream != null) {
            InputStream input = new XInputStreamToInputStreamAdapter(stream);
            statement.setBinaryStream(index, input, input.available());
        } else {
            success = false;
        }
        return success;
    }

    public static Object getObject(Object object) {
        Object value = Any.VOID;
        if (object != null) {
            if (object instanceof String) {
                value = (String) object;
            } else if (object instanceof Boolean) {
                value = (Boolean) object;
            } else if (object instanceof Integer) {
                value = (Integer) object;
            } else {
                value = getTimedObject(object);
            }
        }
        return value;
    }

    private static Object getTimedObject(Object object) {
        Object value = Any.VOID;
        if (object instanceof java.time.OffsetTime) {
            value  = UnoHelper.getTimeWithTimezone((java.time.OffsetTime) object);
        } else if (object instanceof java.time.OffsetDateTime) {
            value = UnoHelper.getDateTimeWithTimezone((java.time.OffsetDateTime) object);
        } else if (object instanceof java.time.LocalDate) {
            value = UnoHelper.getUnoDate((java.time.LocalDate) object);
        } else if (object instanceof java.time.LocalTime) {
            value = UnoHelper.getUnoTime((java.time.LocalTime) object);
        } else if (object instanceof java.time.LocalDateTime) {
            value = UnoHelper.getDateTime((java.time.LocalDateTime) object);
        } else if (object instanceof java.sql.Date) {
            value = getDate(object);
        } else if (object instanceof java.sql.Time) {
            value = getTime(object);
        } else if (object instanceof java.sql.Timestamp) {
            value = getTimestamp(object);
        }
        return value;
    }

    private static Date getDate(Object object) {
        java.sql.Date date = (java.sql.Date) object;
        return UnoHelper.getUnoDate(date.toLocalDate());
    }

    private static Time getTime(Object object) {
        java.sql.Time time = (java.sql.Time) object;
        return UnoHelper.getUnoTime(time.toLocalTime());
    }

    private static DateTime getTimestamp(Object object) {
        java.sql.Timestamp timestamp = (java.sql.Timestamp) object;
        return UnoHelper.getUnoDateTime(timestamp.toLocalDateTime());
    }

    public static boolean useBookmarks(Provider provider) {
        RowIdLifetime lifetime = RowIdLifetime.ROWID_UNSUPPORTED;
        try {
            lifetime = provider.getConnection().getMetaData().getRowIdLifetime();
        } catch (java.sql.SQLException e) {
            // XXX Auto-generated catch block
            e.printStackTrace();
        }
        return lifetime != RowIdLifetime.ROWID_UNSUPPORTED;
    }

    public static boolean supportsService(XPropertySet descriptor,
                                          String service) {
        XServiceInfo info = UnoRuntime.queryInterface(XServiceInfo.class, descriptor);
        return info.supportsService(service);
    }

    public static boolean hasDescriptorProperty(XPropertySet descriptor,
                                                PropertyID pid) {
        return descriptor.getPropertySetInfo().hasPropertyByName(pid.getName());
    }

    public static String getDescriptorStringValue(XPropertySet descriptor,
                                                  PropertyID pid) {
        String value;
        try {
            value = getDescriptorStrValue(descriptor, pid);
        } catch (java.sql.SQLException e) {
            value = "";
        }
        return value;
    }

    public static String getDescriptorStrValue(XPropertySet descriptor,
                                               PropertyID pid)
        throws java.sql.SQLException {
        try {
            return AnyConverter.toString(descriptor.getPropertyValue(pid.getName()));
        } catch (WrappedTargetException | UnknownPropertyException | IllegalArgumentException e) {
            throw new java.sql.SQLException(e.getMessage(), e);
        }
    }

    public static boolean getDescriptorBooleanValue(XPropertySet descriptor,
                                                    PropertyID pid) {
        boolean value;
        try {
            value = getDescriptorBoolValue(descriptor, pid);
        } catch (java.sql.SQLException e) {
            value = false;
        }
        return value;
    }

    public static boolean getDescriptorBoolValue(XPropertySet descriptor,
                                                 PropertyID pid)
        throws java.sql.SQLException {
        try {
            return AnyConverter.toBoolean(descriptor.getPropertyValue(pid.getName()));
        } catch (WrappedTargetException | UnknownPropertyException | IllegalArgumentException e) {
            throw new java.sql.SQLException(e.getMessage(), e);
        }
    }

    public static int getDescriptorIntegerValue(XPropertySet descriptor,
                                                PropertyID pid) {
        int value;
        try {
            value = getDescriptorIntValue(descriptor, pid);
        } catch (java.sql.SQLException e) {
            value = 0;
        }
        return value;
    }

    public static int getDescriptorIntValue(XPropertySet descriptor,
                                            PropertyID pid)
        throws java.sql.SQLException {
        try {
            return AnyConverter.toInt(descriptor.getPropertyValue(pid.getName()));
        } catch (WrappedTargetException | UnknownPropertyException | IllegalArgumentException e) {
            throw new java.sql.SQLException(e.getMessage(), e);
        }
    }

    public static boolean executeSQLQuery(Provider provider,
                                          String query)
        throws java.sql.SQLException {
        Object[] parameters =  new Object[]{};
        Integer[] positions = new Integer[]{};
        return executeSQLQuery(provider, query, parameters, positions);
    }

    public static boolean executeSQLQuery(Provider provider,
                                          String query,
                                          Object[] parameters,
                                          Integer[] positions)
        throws java.sql.SQLException {
        boolean result = false;
        if (!query.isBlank()) {
            boolean auto = false;
            boolean support = provider.supportsTransactions();
    
            java.sql.Connection connection = provider.getConnection();
            try {
                if (support) {
                    auto = connection.getAutoCommit();
                    connection.setAutoCommit(false);
                }
                provider.getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_CONNECTION_EXECUTE_QUERY, query);
                try (java.sql.PreparedStatement statement = connection.prepareStatement(query)) {
                    executeSQL(statement, parameters, positions);
                }
                if (support) {
                    connection.commit();
                    connection.setAutoCommit(auto);
                }
            } catch (java.sql.SQLException e) {
                if (support) {
                    try {
                        connection.rollback();
                        connection.setAutoCommit(auto);
                    } catch (java.sql.SQLException ex) {
                        e.setNextException(ex);
                    }
                }
                throw e;
            }
            result = true;
        }
        return result;

    }

    public static boolean executeSQLQueries(Provider provider,
                                            List<String> queries)
        throws java.sql.SQLException {
        Object[] parameters =  new Object[]{};
        List<Integer[]> positions = new ArrayList<Integer[]>();
        return executeSQLQueries(provider, queries, parameters, positions);
    }

    public static boolean executeSQLQueries(Provider provider,
                                            List<String> queries,
                                            Object[] parameters,
                                            List<Integer[]> positions)
        throws java.sql.SQLException {
        int count = 0;
        int index = 0;
        boolean auto = false;
        boolean support = provider.supportsTransactions();
        java.sql.Connection connection = provider.getConnection();
        try {
            if (support) {
                auto = connection.getAutoCommit();
                connection.setAutoCommit(false);
            }
            for (String query : queries) {
                if (query.isBlank()) {
                    index ++;
                    continue;
                }
                provider.getLogger().logprb(LogLevel.INFO, Resources.STR_LOG_CONNECTION_EXECUTE_QUERY, query);
                try (java.sql.PreparedStatement statement = connection.prepareStatement(query)) {
                    Integer[] position;
                    if (positions.size() > index) {
                        position = positions.get(index);
                    } else {
                        position = new Integer[]{};
                    }
                    executeSQL(statement, parameters, position);
                    index ++;
                    count ++;
                }
            }
            if (support) {
                connection.commit();
                connection.setAutoCommit(auto);
            }
        } catch (java.sql.SQLException e) {
            if (support) {
                try {
                    connection.rollback();
                    connection.setAutoCommit(auto);
                } catch (java.sql.SQLException ex) {
                    e.setNextException(ex);
                }
            }
            throw e;
        }
        return count > 0;
    }

    private static void executeSQL(java.sql.PreparedStatement statement,
                                   Object[] parameters,
                                   Integer[] positions)
        throws java.sql.SQLException {
        setStatementParameters(statement, parameters, positions);
        statement.executeUpdate();
    }

    public static void setStatementParameters(java.sql.PreparedStatement statement,
                                              Object[] parameters,
                                              Integer[] positions)
        throws java.sql.SQLException {
        int i = 1;
        for (int position : positions) {
            statement.setString(i++, (String) parameters[position]);
        }
    }

    public static void printDescriptor(XPropertySet descriptor) {
        for (Property property: descriptor.getPropertySetInfo().getProperties()) {
            String name = property.Name;
            try {
                Object value = descriptor.getPropertyValue(name);
                System.out.println("Name: " + name + " - Value: '" + value.toString() + "'");
            } catch (UnknownPropertyException | WrappedTargetException e) {
                e.printStackTrace();
            }
        }
        XColumnsSupplier supplier = UnoRuntime.queryInterface(XColumnsSupplier.class, descriptor);
        if (supplier != null) {
            XIndexAccess indexes = UnoRuntime.queryInterface(XIndexAccess.class, supplier.getColumns());
            for (int i = 0; i < indexes.getCount(); i++) {
                try {
                    XPropertySet property = UnoRuntime.queryInterface(XPropertySet.class, indexes.getByIndex(i));
                    if (property != null) {
                        printDescriptor(property);
                    }
                } catch (IndexOutOfBoundsException | WrappedTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void printResultSet(java.sql.ResultSet result) throws java.sql.SQLException {
        printResultSet(result, null);
    }

    public static void printResultSet(java.sql.ResultSet result, String[] colNames)
        throws java.sql.SQLException {
        int row = 0;
        ResultSetMetaData metadata = result.getMetaData();
        while (result.next()) {
            row ++;
            System.out.println("Row: " + row + "\t*********************");
            for (int i = 1; i <= metadata.getColumnCount(); i++) {
                if (colNames == null || Arrays.asList(colNames).contains( metadata.getColumnName(i))) {
                    System.out.println("Column: " + metadata.getColumnName(i) +
                                       " - Value: '" + result.getString(i) + "'");
                }
            }
        }
    }

    // XXX: MessageFormat don't like simple quote!!!
    public static String formatSQLQuery(String query,
                                        Object... arguments) {
        // XXX: If we have a simple quote then we have to double simple quote!!!
        if (query.contains("'")) {
            query = query.replace("'", "''");
        }
        return MessageFormat.format(query, arguments);
    }

    public static int getEvenLength(final int length) {
        int len = length;
        if ((length & 1) != 0) {
            len = length - 1;
        }
        return len;
    }

    public static WrappedTargetException getWrappedException(java.lang.Exception e) {
        WrappedTargetException exception = null;
        if (e != null) {
            Exception ex = new Exception(e.getMessage());
            exception = getWrappedException(ex);
        }
        return exception;
    }

    public static WrappedTargetException getWrappedException(Exception e) {
        WrappedTargetException exception = null;
        if (e != null) {
            exception = new WrappedTargetException(e.getMessage());
            exception.Context = e.Context;
            exception.TargetException = e;
        }
        return exception;
    }

    public static SQLException getSQLException(Throwable e) {
        return getSQLException(e, null);
    }

    public static SQLException getSQLException(Throwable e,
                                               XInterface context) {
        SQLException ex = getUnoSQLException(e, context);
        if (e instanceof java.sql.SQLException) {
            SQLException prev = ex;
            java.sql.SQLException e1 = (java.sql.SQLException) e;
            Iterator<Throwable> it = e1.iterator();
            while (it.hasNext()) {
                prev = getUnoSQLException(prev, it.next(), context);
            }
        }
        return ex;
    }

    public static SQLException getSQLException(String msg) {
        SQLException e;
        if (msg != null) {
            e = new SQLException(msg);
        } else {
            e = new SQLException();
        }
        return e;
    }

    public static SQLException getLoggedSQLException(Throwable e,
                                                     XInterface component,
                                                     ResourceBasedEventLogger logger) {
        
        SQLException ex = getSQLException(e, component);
        logger.log(LogLevel.SEVERE, e);
        return ex;
    }

    private static SQLException getUnoSQLException(Throwable e,
                                                   XInterface context) {
        SQLException ex;
        String msg = e.getLocalizedMessage();
        if (msg != null) {
            ex = new SQLException(msg);
        } else {
            ex = new SQLException();
        }
        if (context != null) {
            ex.Context = context;
        }
        if (e instanceof java.sql.SQLException) {
            java.sql.SQLException e1 = (java.sql.SQLException) e;
            ex.ErrorCode = e1.getErrorCode();
            String state = e1.getSQLState();
            if (state != null) {
                ex.SQLState = e1.getSQLState();
            }
        }
        return ex;
    }

    private static SQLException getUnoSQLException(SQLException ex,
                                                   Throwable e,
                                                   XInterface context) {
        SQLException exception = getUnoSQLException(e, context);
        ex.NextException = exception;
        return exception;
    }

}
