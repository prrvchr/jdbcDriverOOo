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
package io.github.prrvchr.uno.sdbcx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.star.container.ElementExistException;
import com.sun.star.logging.LogLevel;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XRename;
import com.sun.star.uno.Any;

import io.github.prrvchr.uno.driver.config.ParameterDDL;
import io.github.prrvchr.uno.driver.helper.ComponentHelper;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedComponent;
import io.github.prrvchr.uno.driver.helper.ComponentHelper.NamedSupport;
import io.github.prrvchr.uno.driver.helper.DBTools;
import io.github.prrvchr.uno.driver.provider.ComposeRule;
import io.github.prrvchr.uno.driver.provider.ConnectionLog;
import io.github.prrvchr.uno.driver.provider.LoggerObjectType;
import io.github.prrvchr.uno.driver.provider.Resources;
import io.github.prrvchr.uno.driver.provider.StandardSQLState;
import io.github.prrvchr.uno.helper.SharedResources;


public abstract class TableBase
    extends TableMain
    implements XRename {

    protected final ConnectionSuper mConnection;
    private final ConnectionLog mLogger; 

    // The constructor method:
    protected TableBase(String service,
                        String[] services,
                        ConnectionSuper connection,
                        String catalog,
                        String schema,
                        boolean sensitive,
                        String name,
                        LoggerObjectType logtype) {
        super(service, services, catalog, schema, name, sensitive);
        mConnection = connection;
        mLogger = new ConnectionLog(connection.getProvider().getLogger(), logtype);
    }

    protected ConnectionLog getLogger() {
        return mLogger;
    }

    protected ConnectionSuper getConnection() {
        return mConnection;
    }

    // com.sun.star.sdbcx.XRename
    // XXX: see: https://github.com/LibreOffice/core/blob/6361a9398584defe9ab8db1e3383e02912e3f24c/
    // XXX: connectivity/source/drivers/postgresql/pq_xtable.cxx#L136
    @Override
    public abstract void rename(String name) throws SQLException, ElementExistException;

    // Here we execute the SQL command allowing you to move and/or rename a table or view
    protected boolean rename(NamedComponent component,
                             String oldname,
                             String newname,
                             boolean isview,
                             ComposeRule rule)
        throws SQLException {
        boolean moved = !mCatalogName.equals(component.getCatalogName()) ||
                        !mSchemaName.equals(component.getSchemaName());
        boolean renamed = !getName().equals(component.getTableName());

        if (!moved && !renamed) {
            int resource = getRenameTableCanceledResource(isview);
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, oldname);
            throw new SQLException(msg, this, StandardSQLState.SQL_OPERATION_CANCELED.text(), 0, Any.VOID);
        }
        // FIXME: Any driver capable of changing catalog or schema must have at least 2 commands...
        // FIXME: If the driver can do this in a single command (like MariaDB) then it is necessary
        // FIXME: to warn by leaving an empty field after the first two that the SQL query requests
        if (moved && !mConnection.getProvider().getConfigDDL().canRenameAndMove()) {
            int resource = getRenameTableUnsupportedResource(isview);
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, oldname);
            throw new SQLException(msg, this, StandardSQLState.SQL_FUNCTION_NOT_SUPPORTED.text(), 0, Any.VOID);
        }

        return renameTableOrView(component, oldname, newname, isview, rule, moved, renamed);
    }

    private boolean renameTableOrView(NamedComponent component,
                                      String oldname,
                                      String newname,
                                      boolean isview,
                                      ComposeRule rule,
                                      boolean moved,
                                      boolean renamed)
        throws SQLException {

        String fname = newname;
        boolean changed = true;
        boolean reversed = false;
        boolean multiquery = mConnection.getProvider().getConfigDDL().hasMultiRenameQueries();
        boolean fullchange = multiquery && moved && renamed;
        List<String> queries = new ArrayList<>();

        try {
            NamedSupport support = mConnection.getProvider().getNamedSupport(rule);
            // FIXME: We have 2 commands for moving and renaming and we need to find the right order
            // FIXME: to execute queries. Change first the catalog / schema (ie: moved) and after
            // FIXME: the table name (ie: renamed) or the reverse if possible...
            if (fullchange) {
                // FIXME: try to move first
                String name = ComponentHelper.buildName(support, component.getCatalogName(),
                                                        component.getSchemaName(), getName(), false);
                reversed = mConnection.getTablesInternal().hasByName(name);
                if (reversed) {
                    // FIXME: try to rename first
                    fname = ComponentHelper.buildName(support, mCatalogName,
                                                      mSchemaName, component.getTableName(), false);
                }
            }

            // FIXME: If the move action is not atomic (performed by 2 commands) then it may not be possible
            // FIXME: since adjacent actions may encounter a conflict of already existing names.
            if (mConnection.getTablesInternal().hasByName(fname)) {
                int resource = getRenameTableDuplicateResource(isview);
                String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, oldname, newname);
                throw new SQLException(msg, this, StandardSQLState.SQL_TABLE_OR_VIEW_EXISTS.text(), 0, Any.VOID);
            }

            Map<String, Object> arguments = ParameterDDL.getRenameTable(support, component, getNamedComponents(),
                                                                        oldname, reversed, isCaseSensitive());
            queries = mConnection.getProvider().getConfigDDL().getRenameTableCommands(arguments, reversed);
            changed = renameTableOrView(queries, newname, isview, moved, renamed, multiquery, fullchange);

        } catch (java.sql.SQLException e) {
            int resource = getRenameTableResource(isview, true);
            String query = String.join("> <", queries);
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, newname, query);
            throw DBTools.getSQLException(msg, this, StandardSQLState.SQL_GENERAL_ERROR.text(), 0, e);
        }
        if (!changed) {
            int resource = getRenameTableCanceledResource(isview);
            String msg = SharedResources.getInstance().getResourceWithSubstitution(resource, oldname);
            throw new SQLException(msg, this, StandardSQLState.SQL_OPERATION_CANCELED.text(), 0, Any.VOID);
        }
        return moved || renamed;
    }

    private boolean renameTableOrView(List<String> queries,
                                      String newname,
                                      boolean isview,
                                      boolean moved,
                                      boolean renamed,
                                      boolean multiquery,
                                      boolean fullchange)
        throws java.sql.SQLException {
        boolean changed = true;
        boolean skipped = true;
        int resource = getRenameTableResource(isview, false);
        if (fullchange) {
            if (!queries.isEmpty()) {
                String query = String.join("> <", queries);
                getLogger().logprb(LogLevel.INFO, resource, newname, query);
                changed &= DBTools.executeSQLQueries(mConnection.getProvider(), queries);
                skipped = false;
            }
        } else {
            if (!multiquery || moved) {
                String query = queries.get(0);
                getLogger().logprb(LogLevel.INFO, resource, newname, query);
                changed &= DBTools.executeSQLQuery(mConnection.getProvider(), query);
                skipped = false;
            }
            if (multiquery && renamed) {
                String query = queries.get(1);
                getLogger().logprb(LogLevel.INFO, resource, newname, query);
                changed &= DBTools.executeSQLQuery(mConnection.getProvider(), query);
                skipped = false;
            }
        }
        return changed && !skipped;
    }

    protected int getRenameTableResource(boolean isview, boolean error) {
        int resource;
        if (isview) {
            if (error) {
                resource = Resources.STR_LOG_VIEW_RENAME_QUERY_ERROR;
            } else {
                resource = Resources.STR_LOG_VIEW_RENAME_QUERY;
            }
        } else {
            if (error) {
                resource = Resources.STR_LOG_TABLE_RENAME_QUERY_ERROR;
            } else {
                resource = Resources.STR_LOG_TABLE_RENAME_QUERY;
            }
        }
        return resource;
    }
    protected int getRenameTableCanceledResource(boolean isview) {
        int resource;
        if (isview) {
            resource = Resources.STR_LOG_VIEW_RENAME_OPERATION_CANCELLED_ERROR;
        } else {
            resource = Resources.STR_LOG_TABLE_RENAME_OPERATION_CANCELLED_ERROR;
        }
        return resource;
    }

    protected int getRenameTableUnsupportedResource(boolean isview) {
        int resource;
        if (isview) {
            resource = Resources.STR_LOG_VIEW_RENAME_UNSUPPORTED_FUNCTION_ERROR;
        } else {
            resource = Resources.STR_LOG_TABLE_RENAME_UNSUPPORTED_FUNCTION_ERROR;
        }
        return resource;
    }

    protected int getRenameTableNotImplementedResource(boolean isview) {
        int resource;
        if (isview) {
            resource = Resources.STR_LOG_VIEW_RENAME_FEATURE_NOT_IMPLEMENTED;
        } else {
            resource = Resources.STR_LOG_TABLE_RENAME_FEATURE_NOT_IMPLEMENTED;
        }
        return resource;
    }

    protected int getRenameTableDuplicateResource(boolean isview) {
        int resource;
        if (isview) {
            resource = Resources.STR_LOG_VIEW_RENAME_DUPLICATE_VIEW_NAME_ERROR;
        } else {
            resource = Resources.STR_LOG_TABLE_RENAME_DUPLICATE_TABLE_NAME_ERROR;
        }
        return resource;
    }

}
