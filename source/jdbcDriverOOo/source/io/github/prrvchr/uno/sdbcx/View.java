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
package io.github.prrvchr.uno.sdbcx;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.container.ElementExistException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XAlterView;
import com.sun.star.sdbcx.XRename;
import com.sun.star.uno.Type;

import io.github.prrvchr.jdbcdriver.DataBaseTools;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdb.Connection;


public class View
    extends Descriptor
    implements XAlterView,
               XRename
{
    private static final String m_service = View.class.getName();
    private static final String[] m_services = {"com.sun.star.sdbcx.View"};

    private Connection m_connection;

    protected String m_CatalogName = "";
    protected String m_SchemaName = "";
    private String m_Command = "";
    private int m_CheckOption;

    // The constructor method:
    public View(Connection connection,
                boolean sensitive,
                String catalog,
                String schema,
                String name,
                String command,
                int option)
    {
        super(m_service, m_services, sensitive, name);
        m_connection = connection;
        m_CatalogName = catalog;
        m_SchemaName = schema;
        m_Command = command;
        m_CheckOption = option;
        registerProperties();
    }

    private void registerProperties() {
        short readonly = PropertyAttribute.READONLY;
        registerProperty(PropertyIds.CATALOGNAME.name, PropertyIds.CATALOGNAME.id, Type.STRING, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_CatalogName;
                }
            }, null);
        registerProperty(PropertyIds.SCHEMANAME.name, PropertyIds.SCHEMANAME.id, Type.STRING, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_SchemaName;
                }
            }, null);
        registerProperty(PropertyIds.COMMAND.name, PropertyIds.COMMAND.id, Type.STRING, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_Command;
                }
            }, null);
        registerProperty(PropertyIds.CHECKOPTION.name, PropertyIds.CHECKOPTION.id, Type.LONG, readonly,
            new PropertyGetter() {
                @Override
                public Object getValue() throws WrappedTargetException {
                    return m_CheckOption;
                }
            }, null);
    }

    // com.sun.star.sdbcx.XAlterView
    @Override
    public void alterCommand(String command)
        throws SQLException
    {
        try (java.sql.Statement statement = m_connection.getProvider().getConnection().createStatement()){
            System.out.println("sdbcx.View.alterCommand() 1 : " + command);
            String sql = DataBaseTools.getAlterViewQuery(m_connection, this, command);
            statement.execute(sql);
            m_Command = command;
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, m_connection);
        }
    }

    @Override
    public void rename(String name)
        throws SQLException,
        ElementExistException
    {
        System.out.println("sdbcx.View.rename() 1 : " + name);
    }


}
