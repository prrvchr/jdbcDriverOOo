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

import java.util.Collection;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbcx.Privilege;
import com.sun.star.uno.Type;

import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbcx.TableBase;
import io.github.prrvchr.uno.sdbcx.TableContainer;


public final class Table
    extends TableBase
{

    private static final String m_service = Table.class.getName();
    private static final String[] m_services = {"com.sun.star.sdb.Table",
                                                "com.sun.star.sdbcx.Table"};
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
    public Table(TableContainer container,
                 boolean sensitive,
                 String catalog,
                 String schema,
                 String name,
                 String type,
                 String remarks)
    {
        super(m_service, m_services, container, sensitive, name);
        System.out.println("sdbc.Table() 1");
        super.m_CatalogName = catalog;
        super.m_SchemaName= schema;
        super.m_Type = type;
        super.m_Description = remarks;
        registerProperties();
        System.out.println("sdbc.Table() 2");
    }

    private void registerProperties() {
        short readonly = PropertyAttribute.READONLY;
        registerProperty(PropertyIds.PRIVILEGES.name, PropertyIds.PRIVILEGES.id, Type.LONG, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
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


/*    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdb.Table.createDataDescriptor()
    // XXX: - io.github.prrvchr.uno.sdbcx.TableContainer.createDataDescriptor()
    public Table(Connection connection,
                 XPropertySet descriptor)
        throws SQLException
    {
        super(m_name, m_services, connection, descriptor);
        //m_Privileges = _getTablePrivileges();
        registerProperties();
        System.out.println("sdb.Table.Table()");
    }
    public Table(Connection connection,
                 schemacrawler.schema.Table table)
        throws java.sql.SQLException
    {
        super(m_name, m_services, connection, table);
        //m_Privileges = _getTablePrivileges(table);
        registerProperties();
        System.out.println("sdb.Table.Table() : 1" );
    }*/

    protected int _getPrivileges()
    {
        System.out.println("sdb.Table._getPrivileges() : 1 ************************************: " + m_Privileges);
        return m_Privileges;
    }

    @SuppressWarnings("unused")
    private int _getTablePrivileges()
        throws java.sql.SQLException
    {
        int value = 0;
        System.out.println("sdb.Table._getTablePrivileges() : 1 Catalog: " + m_CatalogName + " - Schema: " + m_SchemaName + " - Table: " + getName());
        java.sql.ResultSet result = m_tables.getConnection().getProvider().getConnection().getMetaData().getTablePrivileges(m_CatalogName, m_SchemaName, getName());
        while (result != null && result.next()) {
            System.out.println("sdb.Table._getTablePrivileges() : 2 " + result.getString(6));
            //value += UnoHelper.getConstantValue(Privilege.class, result.getString(6));
            System.out.println("sdb.Table._getTablePrivileges() : 3 " + value);
        }
        result.close();
        value = 100;
        System.out.println("sdb.Table._getTablePrivileges() : 4 " + value);
        return value;
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


}
