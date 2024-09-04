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

import java.util.ArrayList;
import java.util.List;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.container.ElementExistException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XRename;
import com.sun.star.uno.Any;
import com.sun.star.uno.Type;

import io.github.prrvchr.jdbcdriver.ComposeRule;
import io.github.prrvchr.jdbcdriver.ConnectionLog;
import io.github.prrvchr.jdbcdriver.helper.ParameterHelper;
import io.github.prrvchr.jdbcdriver.helper.DBTools;
import io.github.prrvchr.jdbcdriver.helper.DBTools.NamedComponents;
import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.jdbcdriver.Resources;
import io.github.prrvchr.jdbcdriver.StandardSQLState;
import io.github.prrvchr.jdbcdriver.LoggerObjectType;
import io.github.prrvchr.uno.helper.SharedResources;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;


public abstract class TableMain
    extends Descriptor
    implements XRename
{

    private final ConnectionSuper m_connection;
    private final ConnectionLog m_logger; 
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

    protected String getCatalogName()
    {
        return m_CatalogName;
    }
    protected String getSchemaName()
    {
        return m_SchemaName;
    }
    protected String getName()
    {
        return super.getName();
    }

    protected ConnectionLog getLogger()
    {
        return m_logger;
    }

    protected ConnectionSuper getConnection()
    {
        return m_connection;
    }

    // com.sun.star.sdbcx.XRename
    // TODO: see: https://github.com/LibreOffice/core/blob/6361a9398584defe9ab8db1e3383e02912e3f24c/
    // TODO: connectivity/source/drivers/postgresql/pq_xtable.cxx#L136
    @Override
    public abstract void rename(String name) throws SQLException, ElementExistException;

    // Here we execute the SQL command allowing you to move and/or rename a table or view
    protected boolean rename(NamedComponents component,
                             String oldname,
                             String newname,
                             boolean isview,
                             ComposeRule rule)
        throws SQLException
    {
        boolean moved = !m_CatalogName.equals(component.getCatalogName()) || !m_SchemaName.equals(component.getSchemaName());
        boolean renamed = !getName().equals(component.getTableName());

        if (!moved && !renamed) {
            int resource = getRenameTableCanceledResource(isview);
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, oldname);
            throw new SQLException(msg, this, StandardSQLState.SQL_OPERATION_CANCELED.text(), 0, Any.VOID);
        }
        // FIXME: Any driver capable of changing catalog or schema must have at least 2 commands...
        // FIXME: If the driver can do this in a single command (like MariaDB) then it is necessary
        // FIXME: to warn by leaving an empty field after the first two that the SQL query requests
        if (moved && !m_connection.getProvider().canRenameAndMove()) {
            int resource = getRenameTableUnsupportedResource(isview);
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, oldname);
            throw new SQLException(msg, this, StandardSQLState.SQL_FUNCTION_NOT_SUPPORTED.text(), 0, Any.VOID);
        }

        String fname = newname;
        boolean reversed = false;
        boolean fullchange = false;
        boolean multiquery = m_connection.getProvider().hasMultiRenameQueries();
        boolean changed = true;
        boolean skipped = true;
        List<String> queries = new ArrayList<String>();

        try {
            // FIXME: We have 2 commands for moving and renaming and we need to find the right order to execute queries
            // FIXME: ie: change first the catalog / schema and after the table name or the reverse if possible...
            if (multiquery && moved && renamed) {
                // FIXME: try to move first
                fullchange = true;
                String name = DBTools.buildName(m_connection.getProvider(), component.getCatalogName(), component.getSchemaName(), getName(), rule, false);
                reversed = m_connection.getTablesInternal().hasByName(name);
                if (reversed) {
                    // FIXME: try to rename first
                    fname = DBTools.buildName(m_connection.getProvider(), m_CatalogName, m_SchemaName, component.getTableName(), rule, false);
                }
            }

            // FIXME: If the move action is not atomic (performed by 2 commands) then it may not be possible
            // FIXME: since adjacent actions may encounter a conflict of already existing names.
            if (m_connection.getTablesInternal().hasByName(fname)) {
                int resource = getRenameTableDuplicateResource(isview);
                String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, oldname, newname);
                throw new SQLException(msg, this, StandardSQLState.SQL_TABLE_OR_VIEW_EXISTS.text(), 0, Any.VOID);
            }

            Object[] parameters = ParameterHelper.getRenameTableArguments(m_connection.getProvider(), component,
                                                                            getNamedComponents(), oldname, reversed, rule, isCaseSensitive());
            queries = m_connection.getProvider().getRenameTableQueries(reversed, parameters);
            int resource = getRenameTableResource(isview, false);
            if (fullchange) {
                if (!queries.isEmpty()) {
                    String query = String.join("> <", queries);
                    getLogger().logprb(LogLevel.INFO, resource, newname, query);
                    changed &= DBTools.executeSQLQueries(m_connection.getProvider(), queries);
                    skipped &= false;
                }
            }
            else {
                if (!multiquery || moved) {
                    String query = queries.get(0);
                    getLogger().logprb(LogLevel.INFO, resource, newname, query);
                    changed &= DBTools.executeSQLQuery(m_connection.getProvider(), query);
                    skipped &= false;
                }
                if (multiquery && renamed) {
                    String query = queries.get(1);
                    getLogger().logprb(LogLevel.INFO, resource, newname, query);
                    changed &= DBTools.executeSQLQuery(m_connection.getProvider(), query);
                    skipped &= false;
                }
            }
        }
        catch (java.sql.SQLException e) {
            int resource = getRenameTableResource(isview, true);
            String query = String.join("> <", queries);
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, newname, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
        if (!changed || skipped) {
            int resource = getRenameTableCanceledResource(isview);
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, oldname);
            throw new SQLException(msg, this, StandardSQLState.SQL_OPERATION_CANCELED.text(), 0, Any.VOID);
        }
        return moved || renamed;
    }

    protected int getRenameTableResource(boolean isview, boolean error)
    {
        if (isview) {
            return error ?
                    Resources.STR_LOG_VIEW_RENAME_QUERY_ERROR :
                    Resources.STR_LOG_VIEW_RENAME_QUERY;
        }
        return error ?
                Resources.STR_LOG_TABLE_RENAME_QUERY_ERROR :
                Resources.STR_LOG_TABLE_RENAME_QUERY;
    }
    protected int getRenameTableCanceledResource(boolean isview)
    {
        return isview ?
                Resources.STR_LOG_VIEW_RENAME_OPERATION_CANCELLED_ERROR :
                Resources.STR_LOG_TABLE_RENAME_OPERATION_CANCELLED_ERROR;
    }
    protected int getRenameTableUnsupportedResource(boolean isview)
    {
        return isview ?
                Resources.STR_LOG_VIEW_RENAME_UNSUPPORTED_FUNCTION_ERROR :
                Resources.STR_LOG_TABLE_RENAME_UNSUPPORTED_FUNCTION_ERROR;
    }
    protected int getRenameTableNotImplementedResource(boolean isview)
    {
        return isview ?
                Resources.STR_LOG_VIEW_RENAME_FEATURE_NOT_IMPLEMENTED :
                Resources.STR_LOG_TABLE_RENAME_FEATURE_NOT_IMPLEMENTED;
    }
    protected int getRenameTableDuplicateResource(boolean isview)
    {
        return isview ?
                Resources.STR_LOG_VIEW_RENAME_DUPLICATE_VIEW_NAME_ERROR :
                Resources.STR_LOG_TABLE_RENAME_DUPLICATE_TABLE_NAME_ERROR;
    }

    protected NamedComponents getNamedComponents()
    {
        return new NamedComponents(m_CatalogName, m_SchemaName, getName());
    }

}
