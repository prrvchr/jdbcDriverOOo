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
package io.github.prrvchr.uno.helper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.sun.star.beans.Property;
import com.sun.star.beans.NamedValue;
import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XIntrospection;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.i18n.XLocaleData;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.logging.LogLevel;
import com.sun.star.resource.XStringResourceResolver;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XRow;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Exception;
import com.sun.star.uno.RuntimeException;
import com.sun.star.uno.Type;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;
import com.sun.star.util.Date;
import com.sun.star.util.DateTime;
import com.sun.star.util.DateTimeWithTimezone;
import com.sun.star.util.Time;
import com.sun.star.util.TimeWithTimezone;


public class UnoHelper {

    public static void ensure(boolean condition, String message) {
        ensure(condition, message, null);
    }

    public static void ensure(Object reference, String message) {
        ensure(reference, message, null);
    }

    public static void ensure(boolean condition, String message, EventLogger logger) {
        if (!condition) {
            RuntimeException error = new com.sun.star.uno.RuntimeException(message);
            if (logger != null) {
                logger.logp(LogLevel.SEVERE, error);
            }
            throw error;
        }
    }

    public static void ensure(Object reference, String message, EventLogger logger) {
        if (reference == null) {
            RuntimeException error = new com.sun.star.uno.RuntimeException(message);
            if (logger != null) {
                logger.logp(LogLevel.SEVERE, error);
            }
            throw error;
        }
    }

    public static void disposeComponent(final Object object) {
        final XComponent component = UnoRuntime.queryInterface(XComponent.class, object);
        if (component != null) {
            component.dispose();
        }
    }

    public static void copyProperties(final XPropertySet src,
                                      final XPropertySet dst) {
        if (src == null || dst == null) {
            return;
        }
        
        XPropertySetInfo srcPropertySetInfo = src.getPropertySetInfo();
        XPropertySetInfo dstPropertySetInfo = dst.getPropertySetInfo();
        
        for (Property srcProperty : srcPropertySetInfo.getProperties()) {
            if (dstPropertySetInfo.hasPropertyByName(srcProperty.Name)) {
                try {
                    Property dstProperty = dstPropertySetInfo.getPropertyByName(srcProperty.Name);
                    System.out.println("UnoHelper.copyProperties() Property: " + srcProperty.Name);
                    if ((dstProperty.Attributes & PropertyAttribute.READONLY) == 0) {
                        Object value = src.getPropertyValue(srcProperty.Name);
                        if ((dstProperty.Attributes & PropertyAttribute.MAYBEVOID) == 0 || value != null) {
                            dst.setPropertyValue(srcProperty.Name, value);
                        }
                    }
                } catch (Exception e) {
                    String error = "Could not copy property '" + srcProperty.Name + "' to the destination set";
                    XServiceInfo serviceInfo = UnoRuntime.queryInterface(XServiceInfo.class, dst);
                    if (serviceInfo != null) {
                        error += " (a '" + serviceInfo.getImplementationName() + "' implementation)";
                    }
                    System.out.println("UnoHelper.copyProperties() ERROR: " + error);
                }
            }
        }
    }

    public static void disposeComponent(final XComponent component) {
        if (component != null) {
            component.dispose();
        }
    }

    public static Object createService(XComponentContext context,
                                       String name) {
        Object service = null;
        try {
            XMultiComponentFactory manager = context.getServiceManager();
            service = manager.createInstanceWithContext(name, context);
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
        return service;
    }

    public static Object createService(XComponentContext context,
                                       String name,
                                       Object... arguments) {
        Object service = null;
        try {
            XMultiComponentFactory manager = context.getServiceManager();
            service = manager.createInstanceWithArgumentsAndContext(name, arguments, context);
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
        return service;
    }

    public static XMultiServiceFactory getMultiServiceFactory(XComponentContext context,
                                                              String service) {
        return UnoRuntime.queryInterface(XMultiServiceFactory.class, createService(context, service));
    }

    public static XNameAccess getFlatConfig(final XComponentContext context,
                                            final String path)
        throws Exception {
        return UnoRuntime.queryInterface(XNameAccess.class, getConfig(context, path, false, null));
    }

    public static XHierarchicalNameAccess getTreeConfig(final XComponentContext context,
                                                        final String path)
        throws Exception {
        return UnoRuntime.queryInterface(XHierarchicalNameAccess.class, getConfig(context, path, false, null));
    }

    public static XHierarchicalNameAccess getTreeConfig(final XComponentContext context,
                                                        final String path,
                                                        final boolean update)
        throws Exception {
        return UnoRuntime.queryInterface(XHierarchicalNameAccess.class, getConfig(context, path, update, null));
    }

    private static Object getConfig(final XComponentContext context,
                                    final String path,
                                    final boolean update,
                                    final String language)
        throws Exception {
        String service = "com.sun.star.configuration.Configuration";
        final XMultiServiceFactory provider = getMultiServiceFactory(context, service + "Provider");
        ArrayList<NamedValue> arguments = new ArrayList<>(Arrays.asList(new NamedValue("nodepath", path)));
        if (language != null) {
            arguments.add(new NamedValue("Locale", language));
        }
        if (update) {
            service += "UpdateAccess";
        } else {
            service += "Access";
        }
        return provider.createInstanceWithArguments(service, arguments.toArray());
    }

    public static String getPackageLocation(XComponentContext context, String identifier, String path) {
        String location = getPackageLocation(context, identifier);
        return location + "/" + path + "/";
    }

    public static String getPackageLocation(XComponentContext context, String identifier) {
        String location = "";
        XPackageInformationProvider provider = null;
        String service = "/singletons/com.sun.star.deployment.PackageInformationProvider";
        provider = UnoRuntime.queryInterface(XPackageInformationProvider.class, context.getValueByName(service));
        if (provider != null) {
            location = provider.getPackageLocation(identifier);
        }
        return location;
    }

    public static Locale getCurrentLocale(XComponentContext context)
        throws NoSuchElementException,
               Exception {
        String nodepath = "/org.openoffice.Setup/L10N";
        String config = "";
        config = (String) getTreeConfig(context, nodepath).getByHierarchicalName("ooLocale");
        String[] parts = config.split("-");
        Locale locale = new Locale(parts[0], "", "");
        if (parts.length > 1) {
            locale.Country = parts[1];
        } else {
            Object service = createService(context, "com.sun.star.i18n.LocaleData");
            XLocaleData data = UnoRuntime.queryInterface(XLocaleData.class, service);
            locale.Country = data.getLanguageCountryInfo(locale).Country;
        }
        return locale;
    }

    public static XStringResourceResolver getResourceResolver(XComponentContext ctx,
                                                              String identifier,
                                                              String path,
                                                              String filename)
        throws NoSuchElementException,
               Exception {
        Locale locale = getCurrentLocale(ctx);
        return getResourceResolver(ctx, identifier, path, filename, locale);
    }

    public static XStringResourceResolver getResourceResolver(XComponentContext ctx,
                                                              String identifier,
                                                              String path,
                                                              String filename,
                                                              Locale locale) {
        String name = "com.sun.star.resource.StringResourceWithLocation";
        String location = getPackageLocation(ctx, identifier, path);
        Object service = createService(ctx, name, location, true, locale, filename, "", null);
        return UnoRuntime.queryInterface(XStringResourceResolver.class, service);
    }

    public static String getDefaultPropertyValue(PropertyValue[] properties, String name, String value)
        throws IllegalArgumentException {
        for (PropertyValue property : properties) {
            if (property.Name.equals(name)) {
                value = AnyConverter.toString(property.Value);
                break;
            }
        }
        return value;
    }

    public static boolean getDefaultPropertyValue(PropertyValue[] properties, String name, boolean value)
        throws IllegalArgumentException {
        for (PropertyValue property : properties) {
            if (property.Name.equals(name)) {
                value = AnyConverter.toBoolean(property.Value);
                break;
            }
        }
        return value;
    }

    public static Object getDefaultDriverInfo(PropertyValue[] properties, String name, Object value)
        throws IllegalArgumentException {
        for (PropertyValue property : properties) {
            if (property.Name.equals(name)) {
                value = property.Value;
                break;
            }
        }
        return value;
    }

    public static Property getProperty(String name, String type) {
        short attributes = 0;
        return getProperty(name, type, attributes);
    }

    public static Property getProperty(String name, int handle, String type) {
        short attributes = 0;
        return getProperty(name, handle, type, attributes);
    }

    public static Property getProperty(String name, String type, short attributes) {
        int handle = -1;
        return getProperty(name, handle, type, attributes);
    }

    public static Property getProperty(String name, int handle, String type, short attributes) {
        Property property = new Property();
        property.Name = name;
        property.Handle = handle;
        property.Type = new Type(type);
        property.Attributes = attributes;
        return property;
    }

    public static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public static String getObjectString(Object object) {
        String value = null;
        if (AnyConverter.isString(object)) {
            value = AnyConverter.toString(object);
            System.out.println("UnoHelper.getObjectString() 1");
        }
        return value;
    }

    public static Date getDate(java.time.LocalDate date) {
        Date value = new Date();
        if (date != null) {
            value.Year = (short) date.getYear();
            value.Month = (short) date.getMonthValue();
            value.Day = (short) date.getDayOfMonth();
        }
        return value;
    }

    public static Time getTime(java.time.LocalTime time) {
        Time value = new Time();
        if (time != null) {
            value.Hours = (short) time.getHour();
            value.Minutes = (short) time.getMinute();
            value.Seconds = (short) time.getSecond();
            value.NanoSeconds = time.getNano();
        }
        return value;
    }

    public static DateTime getDateTime(java.time.LocalDateTime datetime) {
        DateTime value = new DateTime();
        if (datetime != null) {
            value.Year = (short) datetime.getYear();
            value.Month = (short) datetime.getMonthValue();
            value.Day = (short) datetime.getDayOfMonth();
            value.Hours = (short) datetime.getHour();
            value.Minutes = (short) datetime.getMinute();
            value.Seconds = (short) datetime.getSecond();
            value.NanoSeconds = datetime.getNano();
            value.IsUTC = false;
        }
        return value;
    }

    public static TimeWithTimezone getTimeWithTimezone(java.time.OffsetTime time) {
        TimeWithTimezone value = new TimeWithTimezone();
        if (time != null) {
            value.TimeInTZ = getTime(time.toLocalTime());
            value.Timezone = getTimezone(time.getOffset());
        }
        return value;
    }

    public static DateTimeWithTimezone getDateTimeWithTimezone(OffsetDateTime datetime) {
        DateTimeWithTimezone value = new DateTimeWithTimezone();
        if (datetime != null) {
            value.DateTimeInTZ = getDateTime(datetime.toLocalDateTime());
            value.Timezone = getTimezone(datetime.getOffset());
        }
        return value;
    }

    public static short getTimezone(java.time.ZoneOffset offset) {
        final int SECOND = 60;
        return (short) (offset.getTotalSeconds() / SECOND);
    }

    public static java.time.LocalDate getJavaLocalDate(Date date) {
        return java.time.LocalDate.of(date.Year, date.Month, date.Day);
    }

    public static java.time.LocalTime getJavaLocalTime(Time time) {
        return java.time.LocalTime.of(time.Hours, time.Minutes, time.Seconds, time.NanoSeconds);
    }

    public static java.time.LocalDateTime getJavaLocalDateTime(DateTime timestamp) {
        return java.time.LocalDateTime.of(timestamp.Year, timestamp.Month, timestamp.Day,
                                          timestamp.Hours, timestamp.Minutes, timestamp.Seconds, timestamp.NanoSeconds);
    }

    public static java.time.ZoneOffset getJavaZoneOffset(int offset) {
        final int SECOND = 60;
        return java.time.ZoneOffset.ofTotalSeconds(offset * SECOND);
    }

    public static java.time.OffsetTime getJavaOffsetTime(TimeWithTimezone time) {
        return java.time.OffsetTime.of(getJavaLocalTime(time.TimeInTZ), getJavaZoneOffset(time.Timezone));
    }

    public static OffsetDateTime getJavaOffsetDateTime(DateTimeWithTimezone datetime) {
        return OffsetDateTime.of(getJavaLocalDateTime(datetime.DateTimeInTZ), getJavaZoneOffset(datetime.Timezone));
    }

    public static Object getObjectFromResult(java.sql.ResultSet result, int index) {
        Object value = null;
        try {
            value = result.getObject(index);
        } catch (java.sql.SQLException e) {
            e.getStackTrace();
        }
        return value;
    }

    public static String getResultSetValue(java.sql.ResultSet result, int index) {
        String value = null;
        try {
            value = result.getString(index);
        } catch (java.sql.SQLException e) {
            e.getStackTrace();
        }
        return value;
    }

    public static Object getResultValue(java.sql.ResultSet result, int index) {
        boolean retrieved = true;
        Object value = null;
        try {
            switch (result.getMetaData().getColumnType(index)) {
                case java.sql.Types.CHAR:
                case java.sql.Types.VARCHAR:
                    value = result.getString(index);
                    break;
                case java.sql.Types.BOOLEAN:
                    value = result.getBoolean(index);
                    break;
                case java.sql.Types.TINYINT:
                    value = result.getByte(index);
                    break;
                case java.sql.Types.SMALLINT:
                    value = result.getShort(index);
                    break;
                case java.sql.Types.INTEGER:
                    value = result.getInt(index);
                    break;
                case java.sql.Types.BIGINT:
                    value = result.getLong(index);
                    break;
                case java.sql.Types.FLOAT:
                    value = result.getFloat(index);
                    break;
                case java.sql.Types.DOUBLE:
                    value = result.getDouble(index);
                    break;
                case java.sql.Types.TIMESTAMP:
                    value = result.getTimestamp(index);
                    break;
                case java.sql.Types.TIME:
                    value = result.getTime(index);
                    break;
                case java.sql.Types.DATE:
                    value = result.getDate(index);
                    break;
                case java.sql.Types.BINARY:
                    value = result.getBytes(index);
                    break;
                case java.sql.Types.TIME_WITH_TIMEZONE:
                case java.sql.Types.TIMESTAMP_WITH_TIMEZONE:
                    value = result.getObject(index);
                    break;
                default:
                    retrieved = false;
            }
            if (retrieved && result.wasNull()) {
                value = null;
            }
        } catch (java.sql.SQLException e) {
            e.getStackTrace();
        }
        return value;
    }

    public static Object getRowValue(XRow row, int dbtype, int index)
        throws SQLException {
        return getRowValue(row, dbtype, index, null);
    }

    public static Object getRowValue(XRow row, int dbtype, int index, Object value)
        throws SQLException {
        boolean retrieved = true;
        switch (dbtype) {
            case java.sql.Types.CHAR:
            case java.sql.Types.VARCHAR:
                value = row.getString(index);
                break;
            case java.sql.Types.BOOLEAN:
                value = row.getBoolean(index);
                break;
            case java.sql.Types.TINYINT:
                value = row.getByte(index);
                break;
            case java.sql.Types.SMALLINT:
                value = row.getShort(index);
                break;
            case java.sql.Types.INTEGER:
                value = row.getInt(index);
                break;
            case java.sql.Types.BIGINT:
                value = row.getLong(index);
                break;
            case java.sql.Types.FLOAT:
                value = row.getFloat(index);
                break;
            case java.sql.Types.DOUBLE:
                value = row.getDouble(index);
                break;
            case java.sql.Types.TIMESTAMP:
                value = row.getTimestamp(index);
                break;
            case java.sql.Types.TIME:
                value = row.getTime(index);
                break;
            case java.sql.Types.DATE:
                value = row.getDate(index);
                break;
            case java.sql.Types.BINARY:
                value = row.getBytes(index);
                break;
            case java.sql.Types.ARRAY:
                value = row.getArray(index);
                break;
            case java.sql.Types.TIME_WITH_TIMEZONE:
            case java.sql.Types.TIMESTAMP_WITH_TIMEZONE:
                value = row.getObject(index, null);
                break;
            default:
                retrieved = false;
        }
        if (retrieved && row.wasNull()) {
            value = null;
        }
        return value;
    }

    public static DateTimeWithTimezone currentDateTimeInTZ()  {
        return currentDateTimeInTZ(true);
    }

    public static DateTimeWithTimezone currentDateTimeInTZ(boolean utc)  {
        final int SECOND = 60;
        DateTimeWithTimezone dtz = new DateTimeWithTimezone();
        OffsetDateTime now;
        if (utc) {
            now = OffsetDateTime.now(java.time.ZoneOffset.UTC);
            dtz.Timezone = 0;
        } else {
            now = OffsetDateTime.now();
            dtz.Timezone = (short) (now.getOffset().getTotalSeconds() / SECOND);
        }
        dtz.DateTimeInTZ = currentDateTime(now, utc);
        return dtz;
    }

    public static DateTime currentDateTime() {
        OffsetDateTime now = OffsetDateTime.now(java.time.ZoneOffset.UTC);
        DateTime dt = new DateTime();
        dt.Year = (short) now.getYear();
        dt.Month = (short) now.getMonthValue();
        dt.Day = (short) now.getDayOfMonth();
        dt.Hours = (short) now.getHour();
        dt.Minutes = (short) now.getMinute();
        dt.Seconds = (short) now.getSecond();
        return dt;
    }

    public static DateTime currentDateTime(boolean utc) {
        OffsetDateTime now;
        if (utc) {
            now = OffsetDateTime.now(java.time.ZoneOffset.UTC);
        } else {
            now = OffsetDateTime.now();
        }
        return currentDateTime(now, utc);
    }

    private static DateTime currentDateTime(OffsetDateTime now,
                                            boolean utc) {
        DateTime dt = new DateTime();
        dt.Year = (short) now.getYear();
        dt.Month = (short) now.getMonthValue();
        dt.Day = (short) now.getDayOfMonth();
        dt.Hours = (short) now.getHour();
        dt.Minutes = (short) now.getMinute();
        dt.Seconds = (short) now.getSecond();
        dt.NanoSeconds = now.getNano();
        dt.IsUTC = utc;
        return dt;
    }

    public static Integer getConstantValue(Class<?> clazz, String name)
        throws java.sql.SQLException {
        int value = 0;
        if (name != null && !name.isBlank()) {
            try {
                value = (int) clazz.getDeclaredField(name).get(null);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                e.printStackTrace(System.out);
            }
        }
        return value;
    }

    public static String getClassPath() {
        StringBuffer buffer = new StringBuffer();
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader)cl).getURLs();
        for (URL url : urls) {
            buffer.append(url.getFile());
            buffer.append(System.getProperty("path.separator"));
        }
        return buffer.toString();
    }

    public static void inspect(XComponentContext context, XInterface descriptor) {
        String service = "mytools.Mri";
        Object object = UnoHelper.createService(context, service);
        XIntrospection mri = UnoRuntime.queryInterface(XIntrospection.class, object);
        mri.inspect(descriptor);
    }

    public static String getConfigurationOption(XHierarchicalNameAccess config,
                                                String property,
                                                String value) {
        String option = value;
        try {
            if (config.hasByHierarchicalName(property)) {
                option = AnyConverter.toString(config.getByHierarchicalName(property));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return option;
    }

    public static boolean getConfigurationOption(XHierarchicalNameAccess config,
                                                 String property,
                                                 boolean value) {
        boolean option = value;
        try {
            if (config.hasByHierarchicalName(property)) {
                option = AnyConverter.toBoolean(config.getByHierarchicalName(property));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return option;
    }

    public static String getCaller() {
        String value;
        StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        StackWalker.StackFrame frame = stackWalker.walk(stream1 -> stream1.skip(2)
                                                                          .findFirst()
                                                                          .orElse(null));
        if (frame == null) {
            value = "caller: null";
        } else {
            value = String.format("caller: %s#%s, %s", frame.getClassName(),
                                  frame.getMethodName(), frame.getLineNumber());
        }
        return value;
    }

    public static void printStackTrace() {
        Thread thread = Thread.currentThread();
        StackTraceElement[] stackTrace = thread.getStackTrace();
        for (int i = 1; i < stackTrace.length; i++) {
            System.out.println(stackTrace[i].getClassName() + " " +
                               stackTrace[i].getMethodName() + " " +
                               stackTrace[i].getLineNumber());
        }
    }

    public static SQLException getSQLException(Throwable e,
                                               XInterface component) {
        SQLException ex = getUnoSQLException(e, component);
        if (e instanceof java.sql.SQLException) {
            SQLException prev = ex;
            java.sql.SQLException e1 = (java.sql.SQLException) e;
            Iterator<Throwable> it = e1.iterator();
            while (it.hasNext()) {
                prev = getUnoSQLException(prev, it.next(), component);
            }
        }
        return ex;
    }

    private static SQLException getUnoSQLException(Throwable e,
                                                   XInterface component) {
        SQLException ex;
        String msg = e.getLocalizedMessage();
        if (msg != null) {
            ex = new SQLException(msg);
        } else {
            ex = new SQLException();
        }
        if (component != null) {
            ex.Context = component;
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
                                                   XInterface component) {
        SQLException exception = getUnoSQLException(e, component);
        ex.NextException = exception;
        return exception;
    }

}
