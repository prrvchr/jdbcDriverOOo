/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-24 https://prrvchr.github.io                                  ║
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
package io.github.prrvchr.uno.sdb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.star.awt.FontDescriptor;
import com.sun.star.beans.PropertyAttribute;
import com.sun.star.container.ElementExistException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.Type;

import io.github.prrvchr.driver.helper.PrivilegesHelper;
import io.github.prrvchr.driver.helper.ColumnHelper.ColumnDescription;
import io.github.prrvchr.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.driver.provider.ConnectionLog;
import io.github.prrvchr.driver.provider.DriverProvider;
import io.github.prrvchr.driver.provider.PropertyIds;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbcx.TableSuper;


public final class Table
    extends TableSuper {

    private static final String SERVICE = Table.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdb.Table",
                                              "com.sun.star.sdb.DataSettings",
                                              "com.sun.star.sdbcx.Table"};

    protected boolean mApplyFilter = false;
    protected String mFilter = "";
    protected FontDescriptor mFontDescriptor = null;
    protected String mGroupBy = "";
    protected String mHavingClause = "";
    protected String mOrder = "";
    // CHECKSTYLE:OFF: MagicNumber - Specific for database
    protected int mRowHeight = 15;
    // CHECKSTYLE:ON: MagicNumber - Specific for database
    protected int mTextColor = 0;
    private int mPrivileges = 0;

    // The constructor method:
    public Table(Connection connection,
                 boolean sensitive,
                 String catalog,
                 String schema,
                 String name,
                 String type,
                 String remarks) {
        super(SERVICE, SERVICES, connection, sensitive, catalog, schema, name, type, remarks);
        registerProperties();
    }

    // XXX: To keep access to logger in protected mode we need this access
    @Override
    protected ConnectionLog getLogger() {
        return super.getLogger();
    }

    protected Connection getConnection() {
        return (Connection) mConnection;
    }

    @Override
    protected ColumnContainer getColumnContainer(List<ColumnDescription> descriptions)
            throws ElementExistException {
        return new ColumnContainer(this, isCaseSensitive(), descriptions);
    }

    private void registerProperties() {
        Map<String, PropertyWrapper> properties = new HashMap<String, PropertyWrapper>();
        short bound = PropertyAttribute.BOUND;
        short readonly = PropertyAttribute.READONLY;

        properties.put(PropertyIds.APPLYFILTER.getName(),
            new PropertyWrapper(Type.BOOLEAN,
                () -> {
                    return mApplyFilter;
                },
                value -> {
                    mApplyFilter = (boolean) value;
                }));

        properties.put(PropertyIds.FILTER.getName(),
            new PropertyWrapper(Type.STRING,
                () -> {
                    return mFilter;
                },
                value -> {
                    mFilter = (String) value;
                }));

        properties.put(PropertyIds.FONTDESCRIPTOR.getName(),
            new PropertyWrapper(new Type(FontDescriptor.class),
                () -> {
                    return mFontDescriptor;
                },
                value -> {
                    mFontDescriptor = (FontDescriptor) value;
                }));

        properties.put(PropertyIds.GROUPBY.getName(),
            new PropertyWrapper(Type.STRING,
                () -> {
                    return mGroupBy;
                },
                value -> {
                    mGroupBy = (String) value;
                }));

        properties.put(PropertyIds.HAVINGCLAUSE.getName(),
            new PropertyWrapper(Type.STRING,
                () -> {
                    return mHavingClause;
                },
                value -> {
                    mHavingClause = (String) value;
                }));

        registerProperties(properties, bound, readonly);
    }

    private void registerProperties(Map<String, PropertyWrapper> properties, short bound, short readonly) {

        properties.put(PropertyIds.ORDER.getName(),
            new PropertyWrapper(Type.STRING, bound,
                () -> {
                    return mOrder;
                },
                value -> {
                    mOrder = (String) value;
                }));

        properties.put(PropertyIds.PRIVILEGES.getName(),
            new PropertyWrapper(Type.LONG, readonly,
                () -> {
                    System.out.println("sdb.Table.getPrivileges() 1");
                    int privileges = getPrivileges();
                    System.out.println("sdb.Table.getPrivileges() 2 Privileges: " + privileges);
                    return privileges;
                },
                null));

        properties.put(PropertyIds.ROWHEIGHT.getName(),
            new PropertyWrapper(Type.LONG,
                () -> {
                    return mRowHeight;
                },
                value -> {
                    mRowHeight = (int) value;
                }));

        properties.put(PropertyIds.TEXTCOLOR.getName(),
            new PropertyWrapper(Type.LONG,
                () -> {
                    return mTextColor;
                },
                value -> {
                    mTextColor = (int) value;
                }));

        super.registerProperties(properties);
    }

    @Override
    protected NamedComponents getNamedComponents() {
        return super.getNamedComponents();
    }

    @Override
    protected String getCatalogName() {
        return super.getCatalogName();
    }

    @Override
    protected String getSchemaName() {
        return super.getSchemaName();
    }

    @Override
    protected String getName() {
        return super.getName();
    }

    private int getPrivileges()
        throws WrappedTargetException {
        try {
            System.out.println("sdb.Table.getPrivileges() 1");
            if (mPrivileges == 0) {
                DriverProvider provider = getConnection().getProvider();
                int privileges = PrivilegesHelper.getTablePrivileges(provider, getNamedComponents());
                if (privileges == 0) {
                    privileges = provider.getMockPrivileges();
                }
                mPrivileges = privileges;
            }
            System.out.println("sdb.Table.getPrivileges() 2: " + mPrivileges);
            return mPrivileges;
        } catch (java.sql.SQLException e) {
            System.out.println("sdb.Table.getPrivileges() 2 ERROR ******************");
            throw UnoHelper.getWrappedException(e);
        }
    }

}
