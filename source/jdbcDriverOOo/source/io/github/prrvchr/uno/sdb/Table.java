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
package io.github.prrvchr.uno.sdb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.star.awt.FontDescriptor;
import com.sun.star.beans.PropertyAttribute;
import com.sun.star.container.ElementExistException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.driver.config.ConfigDCL;
import io.github.prrvchr.uno.driver.helper.PrivilegesHelper;
import io.github.prrvchr.uno.driver.helper.ColumnHelper.ColumnDescription;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedComponent;
import io.github.prrvchr.uno.driver.provider.ConnectionLog;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.driver.provider.PropertyIds;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbcx.TableSuper;


public final class Table
    extends TableSuper {

    private static final String SERVICE = Table.class.getName();
    private static final String[] SERVICES = {"com.sun.star.sdb.Table",
                                              "com.sun.star.sdb.DataSettings",
                                              "com.sun.star.sdbcx.Table"};
    private static final int ROW_HEIGHT = 15;

    protected boolean mApplyFilter = false;
    protected String mFilter = "";
    protected FontDescriptor mFontDescriptor = null;
    protected String mGroupBy = "";
    protected String mHavingClause = "";
    protected String mOrder = "";
    protected int mRowHeight = ROW_HEIGHT;
    protected int mTextColor = 0;

    // The constructor method:
    public Table(Connection connection,
                 boolean sensitive,
                 String catalog,
                 String schema,
                 String name,
                 String type,
                 String remarks) {
        super(SERVICE, SERVICES, connection, sensitive, catalog, schema, name, type, remarks);
        registerProperties(new HashMap<String, PropertyWrapper>());
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

    private void registerProperties(HashMap<String, PropertyWrapper> properties) {
        short bound = PropertyAttribute.BOUND;

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

        registerProperties(properties, bound);
    }

    private void registerProperties(Map<String, PropertyWrapper> properties, short bound) {

        properties.put(PropertyIds.ORDER.getName(),
            new PropertyWrapper(Type.STRING, bound,
                () -> {
                    return mOrder;
                },
                value -> {
                    mOrder = (String) value;
                }));

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
    protected NamedComponent getNamedComponents() {
        return super.getNamedComponents();
    }

    @Override
    public String getCatalogName() {
        return super.getCatalogName();
    }

    @Override
    public String getSchemaName() {
        return super.getSchemaName();
    }

    @Override
    public String getName() {
        return super.getName();
    }

    protected int getPrivileges()
        throws WrappedTargetException {
        try {
            System.out.println("scb.Table.getPrivileges() 1");
            if (mPrivileges == 0) {
                java.sql.Connection connection = getConnection().getProvider().getConnection();
                ConfigDCL config = getConnection().getProvider().getConfigDCL();
                Provider provider = getConnection().getProvider();
                int privileges = PrivilegesHelper.getTablePrivileges(connection, config, getNamedComponents());
                if (privileges == 0) {
                    privileges = provider.getConfigDCL().getMockPrivileges();
                }
                mPrivileges = privileges;
            }
            System.out.println("scb.Table.getPrivileges() 2: " + mPrivileges);
            return mPrivileges;
        } catch (java.sql.SQLException e) {
            System.out.println("scb.Table.getPrivileges() 2 ERROR ******************");
            throw UnoHelper.getWrappedException(e);
        }
    }

}
