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
package io.github.prrvchr.uno.sdbcx;

import java.util.List;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.container.ElementExistException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XRename;
import com.sun.star.uno.Any;
import com.sun.star.uno.Type;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.DBTools;
import io.github.prrvchr.jdbcdriver.DBTools.NameComponents;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;


public abstract class TableMain
    extends Descriptor
    implements XRename
{

    protected final ConnectionSuper m_connection;
    protected final ConnectionLog m_logger; 
    protected String m_CatalogName = "";
    protected String m_SchemaName = "";

    // The constructor method:
    public TableMain(String service,
                     String[] services,
                     ConnectionSuper connection,
                     String catalog,
                     String schema,
                     boolean sensitive,
                     String name,
                     LoggerObjectType logtype)
    {
        super(service, services, sensitive, name);
        m_connection = connection;
        m_logger = new ConnectionLog(connection.getLogger(), logtype);
        m_CatalogName = catalog;
        m_SchemaName = schema;
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
    }

    public ConnectionLog getLogger()
    {
        return m_logger;
    }


    // com.sun.star.sdbcx.XRename
    // TODO: see: https://github.com/LibreOffice/core/blob/6361a9398584defe9ab8db1e3383e02912e3f24c/
    // TODO: connectivity/source/drivers/postgresql/pq_xtable.cxx#L136
    @Override
    public abstract void rename(String name) throws SQLException, ElementExistException;

    // Here we execute the SQL command allowing you to move and/or rename a table or view
    protected void rename(NameComponents component, String oldname, String newname,
                          ComposeRule rule, int resource)
        throws SQLException
    {
        boolean moved = !m_CatalogName.equals(component.getCatalog()) || !m_SchemaName.equals(component.getSchema());
        boolean renamed = !m_Name.equals(component.getTable());
        if (!moved && !renamed) {
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource + 4, oldname);
            throw new SQLException(msg, this, StandardSQLState.SQL_OPERATION_CANCELED.text(), 0, Any.VOID);
        }
        boolean reversed = false;
        boolean duplicate = false;
        boolean fullchange = false;
        boolean multiquery = m_connection.getProvider().hasMultiRenameQueries();
        // FIXME: We have 2 commands for moving and renaming and we need to find the right order to execute queries
        // FIXME: ie: change first the catalog / schema and after the table name or the reverse if possible...
        if (multiquery && moved && renamed) {
            // FIXME: try to move first
            fullchange = true;
            String name = DBTools.composeTableName(m_connection, component.getCatalog(), component.getSchema(), m_Name, false, rule);
            reversed = m_connection.getTablesInternal().hasByName(name);
            if (reversed) {
                // FIXME: try to rename first
                name = DBTools.composeTableName(m_connection, m_CatalogName, m_SchemaName, component.getTable(), false, rule);
            }
            else {
                name = newname;
            }
            duplicate = m_connection.getTablesInternal().hasByName(name);
        }
        if (duplicate) {
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource + 5, oldname, newname);
            throw new SQLException(msg, this, StandardSQLState.SQL_TABLE_OR_VIEW_EXISTS.text(), 0, Any.VOID);
        }
        Object[] args = DBTools.getRenameTableArguments(m_connection, component, this,
                                                        oldname, reversed, rule, isCaseSensitive());
        List<String> queries = m_connection.getProvider().getRenameTableQueries(reversed, args);
        // FIXME: Any driver capable of changing catalog or schema must have at least 2 commands...
        // FIXME: If the driver can do this in a single command (like MariaDB) then it is necessary
        // FIXME: to provide a second empty command that will not be processed.
        if (queries.size() == 1 && moved) {
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource + 6, oldname);
            throw new SQLException(msg, this, StandardSQLState.SQL_FUNCTION_NOT_SUPPORTED.text(), 0, Any.VOID);
        }
        boolean changed = true;
        boolean skipped = true;
        if (fullchange) {
            changed &= DBTools.executeDDLQueries(m_connection, queries, m_logger,
                                                 this.getClass().getName(), "rename", resource, newname);
            skipped &= false;
        }
        else {
            if (!multiquery || moved) {
                changed &= DBTools.executeDDLQuery(m_connection, queries.get(0), m_logger,
                                                   this.getClass().getName(), "rename", resource, newname);
                skipped &= false;
            }
            if (multiquery && renamed) {
                changed &= DBTools.executeDDLQuery(m_connection, queries.get(1), m_logger,
                                                   this.getClass().getName(), "rename", resource, newname);
                skipped &= false;
            }
        }
        if (!changed || skipped) {
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource + 4, oldname);
            throw new SQLException(msg, this, StandardSQLState.SQL_OPERATION_CANCELED.text(), 0, Any.VOID);
        }
    }

    public String getCatalogName()
    {
        return m_CatalogName;
    }
    public String getSchemaName()
    {
        return m_SchemaName;
    }

    public String getCatalog()
    {
        return m_CatalogName.isEmpty() ? null : m_CatalogName;
    }
    public String getSchema()
    {
        return m_SchemaName.isEmpty() ? null : m_SchemaName;
    }

    protected ConnectionSuper getConnection()
    {
        return m_connection;
    }

}
