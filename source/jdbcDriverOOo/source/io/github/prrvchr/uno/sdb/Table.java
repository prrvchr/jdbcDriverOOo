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
package io.github.prrvchr.uno.sdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.Privilege;
import com.sun.star.sdbcx.XGroupsSupplier;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.helper.DataBaseTools;
import io.github.prrvchr.uno.helper.PropertyIds;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionSuper;
import io.github.prrvchr.uno.sdbcx.TableBase;


public final class Table
    extends TableBase
{

    private static final String m_service = Table.class.getName();
    private static final String[] m_services = {"com.sun.star.sdb.Table",
                                                "com.sun.star.sdbcx.Table"};
    protected final ConnectionSuper m_connection;

    //private int m_Privileges = 0;
    private int m_Privileges = Privilege.SELECT | Privilege.INSERT | Privilege.UPDATE | Privilege.DELETE | Privilege.READ | Privilege.CREATE | Privilege.ALTER | Privilege.REFERENCE | Privilege.DROP;
    /*protected String m_Filter = "";
    protected boolean m_ApplyFilter = false;
    protected String m_Order = "";
    protected int m_RowHeight = 15;
    protected int m_TextColor = 0;
    protected String m_HavingClause = "";
    protected FontDescriptor m_FontDescriptor = null;
    protected String m_GroupBy = "";*/

    // The constructor method:
    public Table(ConnectionSuper connection,
                 boolean sensitive,
                 String catalog,
                 String schema,
                 String name,
                 String type,
                 String remarks)
    {
        super(m_service, m_services, sensitive, name);
        m_connection = connection;
        System.out.println("sdb.Table() 1");
        super.m_CatalogName = catalog;
        super.m_SchemaName= schema;
        super.m_Type = type;
        super.m_Description = remarks;
        registerProperties();
        System.out.println("sdb.Table() 2");
    }

    private void registerProperties() {
        short readonly = PropertyAttribute.READONLY;
        registerProperty(PropertyIds.PRIVILEGES.name, PropertyIds.PRIVILEGES.id, Type.LONG, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    //return _getPrivileges();
                    return m_Privileges;
                }
            }, null);
        /*registerProperty(PropertyIds.FILTER.name, PropertyIds.FILTER.id, Type.STRING,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Filter;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_Filter = (String) value;
                }
            });
        registerProperty(PropertyIds.APPLYFILTER.name, PropertyIds.APPLYFILTER.id, Type.BOOLEAN,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_ApplyFilter;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_ApplyFilter = (boolean) value;
                }
            });
        registerProperty(PropertyIds.ORDER.name, PropertyIds.ORDER.id, Type.STRING,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Order;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_Order = (String) value;
                }
            });
        registerProperty(PropertyIds.ROWHEIGHT.name, PropertyIds.ROWHEIGHT.id, Type.LONG,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_RowHeight;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_RowHeight = (int) value;
                }
            });
        registerProperty(PropertyIds.TEXTCOLOR.name, PropertyIds.TEXTCOLOR.id, Type.LONG,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_TextColor;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_TextColor = (int) value;
                }
            });
        registerProperty(PropertyIds.HAVINGCLAUSE.name, PropertyIds.HAVINGCLAUSE.id, Type.STRING,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_HavingClause;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_HavingClause = (String) value;
                }
            });
        registerProperty(PropertyIds.FONTDESCRIPTOR.name, PropertyIds.FONTDESCRIPTOR.id, new Type(FontDescriptor.class),
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_FontDescriptor;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_FontDescriptor = (FontDescriptor) value;
                }
            });
        registerProperty(PropertyIds.GROUPBY.name, PropertyIds.GROUPBY.id, Type.STRING,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_GroupBy;
                }
            },
            new PropertySetter() {
                @Override
                public void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
                    m_GroupBy = (String) value;
                }
            });*/
    }

    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor()
    {
        System.out.println("sdb.Table.createDataDescriptor() ***************************************************");
        TableDescriptor descriptor = new TableDescriptor(isCaseSensitive());
        synchronized (this) {
            UnoHelper.copyProperties(this, descriptor);
        }
        return descriptor;
    }

    @SuppressWarnings("unused")
    private int _getPrivileges()
        throws WrappedTargetException
    {
        int privileges = 0;
        try {
            String name = getConnection().getMetaData().getUserName();
            XGroupsSupplier groups = (XGroupsSupplier) AnyConverter.toObject(XGroupsSupplier.class, getConnection().getUsers().getByName(name));
            List<String> grantees = new ArrayList<>(List.of(name));
            grantees.addAll(Arrays.asList(groups.getGroups().getElementNames()));
            privileges = DataBaseTools.getTableOrViewPrivileges(getConnection(), grantees, m_CatalogName, m_SchemaName, getName());
        }
        catch (NoSuchElementException | SQLException e) {
            throw UnoHelper.getWrappedException(e);
        }
        System.out.println("sdb.Table._getTablePrivileges() : 4 " + privileges);
        return privileges;
    }


    @SuppressWarnings("unused")
    private static int _getTablePrivileges(schemacrawler.schema.Table table)
        throws java.sql.SQLException
    {
        int value = 265;
        System.out.println("sdb.Table._getTablePrivileges() : 1");
        Collection<schemacrawler.schema.Privilege<schemacrawler.schema.Table>> privileges = table.getPrivileges();
        System.out.println("sdb.Table._getTablePrivileges() : 2 : " + privileges.size());
        for (schemacrawler.schema.Privilege<schemacrawler.schema.Table> privilege : privileges)
        {
            System.out.println("sdb.Table._getTablePrivileges() : 3 " + privilege.getName());
            value = UnoHelper.getConstantValue(Privilege.class, privilege.getName());
            System.out.println("sdb.Table._getTablePrivileges() : 4 " + value);
            break;
        }
        return value;
    }


    public Connection getConnection()
    {
        return (Connection) m_connection;
    }


}
