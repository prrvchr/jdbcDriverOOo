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
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;


public abstract class TableMain
    extends Descriptor
    implements XRename
{

    protected final ConnectionSuper m_connection;
    protected final ConnectionLog m_logger; 
    protected String m_CatalogName = "";
    protected String m_SchemaName = "";

    protected abstract ConnectionSuper getConnection();

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
        m_logger = new ConnectionLog(connection.getProvider().getLogger(), logtype);
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
    protected void rename(NameComponents cpt, String oldname, String newname,
                          ComposeRule rule, int offset)
        throws SQLException
    {
        boolean moved = !m_CatalogName.equals(cpt.getCatalog()) || !m_SchemaName.equals(cpt.getSchema());
        boolean renamed = !m_Name.equals(cpt.getTable());
        if (!moved && !renamed) {
            int resource = Resources.STR_LOG_TABLE_RENAME_OPERATION_CANCELLED_ERROR + offset;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, oldname);
            throw new SQLException(msg, this, StandardSQLState.SQL_OPERATION_CANCELED.text(), 0, Any.VOID);
        }
        // FIXME: Any driver capable of changing catalog or schema must have at least 2 commands...
        // FIXME: If the driver can do this in a single command (like MariaDB) then it is necessary
        // FIXME: to warn by leaving an empty field after the first two that the SQL query requests
        if (moved && !m_connection.getProvider().canRenameAndMove()) {
            int resource = Resources.STR_LOG_TABLE_RENAME_UNSUPPORTED_FUNCTION_ERROR + offset;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, oldname);
            throw new SQLException(msg, this, StandardSQLState.SQL_FUNCTION_NOT_SUPPORTED.text(), 0, Any.VOID);
        }

        String fname = newname;
        boolean reversed = false;
        boolean fullchange = false;
        boolean multiquery = m_connection.getProvider().hasMultiRenameQueries();
        boolean changed = true;
        boolean skipped = true;
        int resource;

        try {
            // FIXME: We have 2 commands for moving and renaming and we need to find the right order to execute queries
            // FIXME: ie: change first the catalog / schema and after the table name or the reverse if possible...
            if (multiquery && moved && renamed) {
                // FIXME: try to move first
                fullchange = true;
                String name = DBTools.buildName(m_connection.getProvider(), cpt.getCatalog(), cpt.getSchema(), m_Name, rule, false);
                reversed = m_connection.getTablesInternal().hasByName(name);
                if (reversed) {
                    // FIXME: try to rename first
                    fname = DBTools.buildName(m_connection.getProvider(), m_CatalogName, m_SchemaName, cpt.getTable(), rule, false);
                }
            }

            // FIXME: If the move action is not atomic (performed by 2 commands) then it may not be possible
            // FIXME: since adjacent actions may encounter a conflict of already existing names.
            if (m_connection.getTablesInternal().hasByName(fname)) {
                resource = Resources.STR_LOG_TABLE_RENAME_DUPLICATE_TABLE_NAME_ERROR + offset;
                String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, oldname, newname);
                throw new SQLException(msg, this, StandardSQLState.SQL_TABLE_OR_VIEW_EXISTS.text(), 0, Any.VOID);
            }

            Object[] parameters = DBTools.getRenameTableArguments(m_connection.getProvider(), cpt, this, oldname, 
                                                                  reversed, rule, isCaseSensitive(), true);
            List<String> queries = m_connection.getProvider().getRenameTableQueries(reversed, parameters);
            resource = Resources.STR_LOG_TABLE_RENAME_QUERY + offset;
            if (fullchange) {
                changed &= DBTools.executeDDLQueries(m_connection.getProvider(), queries, m_logger,
                                                     this.getClass().getName(), "rename", resource, newname);
                skipped &= false;
            }
            else {
                if (!multiquery || moved) {
                    changed &= DBTools.executeDDLQuery(m_connection.getProvider(), queries.get(0), m_logger,
                                                       this.getClass().getName(), "rename", resource, newname);
                    skipped &= false;
                }
                if (multiquery && renamed) {
                    changed &= DBTools.executeDDLQuery(m_connection.getProvider(), queries.get(1), m_logger,
                                                       this.getClass().getName(), "rename", resource, newname);
                    skipped &= false;
                }
            }
        }
        catch (java.sql.SQLException e) {
            throw UnoHelper.getSQLException(e, this);
        }
        if (!changed || skipped) {
            resource = Resources.STR_LOG_TABLE_RENAME_OPERATION_CANCELLED_ERROR + offset;
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, oldname);
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

}
