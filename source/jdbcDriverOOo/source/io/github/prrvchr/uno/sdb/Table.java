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

import com.sun.star.awt.FontDescriptor;
import com.sun.star.beans.PropertyAttribute;
//import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbcx.Privilege;
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
//import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertySetter;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.PropertyIds;
import io.github.prrvchr.uno.sdbcx.TableBase;


public final class Table
    extends TableBase<Column>
{

    private static final String m_name = Table.class.getName();
    private static final String[] m_services = {"com.sun.star.sdb.Table",
                                                "com.sun.star.sdbcx.Table"};
    private int m_Privileges = Privilege.SELECT | Privilege.INSERT | Privilege.UPDATE | Privilege.DELETE | Privilege.READ | Privilege.CREATE | Privilege.ALTER | Privilege.REFERENCE | Privilege.DROP;
    protected String m_Filter = "";
    protected boolean m_ApplyFilter = false;
    protected String m_Order = "";
    protected FontDescriptor m_FontDescriptor = null;
    protected int m_RowHeight = 15;
    protected int m_TextColor = 0;
    protected String m_HavingClause = "";
    protected String m_GroupBy = "";

    // The constructor method:
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdb.Table.createDataDescriptor()
    // XXX: - io.github.prrvchr.uno.sdbcx.TableContainer.createDataDescriptor()
    public Table(Connection connection,
                 XPropertySet descriptor,
                 String name)
        throws java.sql.SQLException
    {
        super(m_name, m_services, connection, Column.class, descriptor, name);
        //m_Privileges = _getTablePrivileges();
        registerProperties();
        System.out.println("sdb.Table.Table()");
    }
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbcx.TableContainer()
    public Table(Connection connection,
                 String catalog,
                 String schema,
                 String name,
                 String type,
                 String description)
        throws java.sql.SQLException
    {
        super(m_name, m_services, connection, Column.class, catalog, schema, name, type, description);
        //m_Privileges = _getTablePrivileges();
        registerProperties();
        System.out.println("sdb.Table.Table() *********************");
    }
    public Table(Connection connection,
                 schemacrawler.schema.Table table)
        throws java.sql.SQLException
    {
        super(m_name, m_services, connection, Column.class, table);
        //m_Privileges = _getTablePrivileges(table);
        registerProperties();
        System.out.println("sdb.Table.Table() : 1" );
    }
    protected int _getPrivileges()
    {
        System.out.println("sdb.Table._getPrivileges() : 1 ************************************: " + m_Privileges);
        return m_Privileges;
    }
    protected String _getFilter()
    {
        System.out.println("sdb.Table._getFilter() : 1 ************************************: " + m_Filter);
        return m_Filter;
    }
    protected void _setFilter(String filter)
    {
        System.out.println("sdb.Table._setFilter() : 1 ************************************: " + filter);
        m_Filter = filter;
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
        registerProperty(PropertyIds.FONTDESCRIPTOR.name, PropertyIds.FONTDESCRIPTOR.id, Type.ANY,
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


    @SuppressWarnings("unused")
    private int _getTablePrivileges()
        throws java.sql.SQLException
    {
        int value = 0;
        System.out.println("sdb.Table._getTablePrivileges() : 1 Catalog: " + m_CatalogName + " - Schema: " + m_SchemaName + " - Table: " + m_Name);
        java.sql.ResultSet result = m_Connection.getWrapper().getMetaData().getTablePrivileges(m_CatalogName, m_SchemaName, m_Name);
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


    // com.sun.star.sdbcx.XDataDescriptorFactory
    @Override
    public XPropertySet createDataDescriptor()
    {
        System.out.println("sdb.Table.createDataDescriptor() ***************************************************");
        TableDescriptor descriptor = new TableDescriptor(m_Connection, Column.class, this);
        return descriptor;
    }


}
