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
package io.github.prrvchr.java.rowset.internal;

import java.io.Serializable;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.Map.Entry;
import java.util.Objects;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.serial.SQLInputImpl;
import javax.sql.rowset.serial.SerialArray;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;
import javax.sql.rowset.serial.SerialStruct;

import io.github.prrvchr.java.rowset.JdbcRowSetResourceBundle;

public class TableWriter implements Serializable {

    private static final long serialVersionUID = 1224287243509406155L;

    private static final String DOT = ".";
    private static final String SEPARATOR = ", ";
    private static final String QUESTION_MARK = " = ?";

    private final String name;
    private final String description;
    private final String identifierQuote;
    private final boolean updateOnInsert;
    private final boolean hasPrimarykeys;
    private final boolean supportsGeneratedKeys;

    private final int[] keyColumns;
    private final int[] tableColumns;
    private final String[] autoColumns;

    private final String selectCommand;
    private final String updateCommand;
    private final String insertCommand;
    private final String deleteCommand;

    private final JdbcRowSetResourceBundle resBundle;
    private final transient Logger logger;

    TableWriter(ResultSetMetaData rsmd,
                DatabaseMetaData dbmd,
                Table table,
                List<Integer> keys,
                List<Integer> primaryKeys,
                JdbcRowSetResourceBundle bundle,
                Logger log)
        throws SQLException {

        this.name = buildTableName(dbmd, table);
        this.description = getDescription(bundle, rsmd, primaryKeys);
        String delimiter = dbmd.getIdentifierQuoteString();
        this.identifierQuote = delimiter;
        this.supportsGeneratedKeys = dbmd.supportsGetGeneratedKeys();
        this.resBundle = bundle;
        this.logger = log;

        List<String> names = new ArrayList<>();
        List<Integer> columns = getTableColumns(rsmd, table, names);
        this.updateOnInsert = !names.isEmpty();
        this.hasPrimarykeys = !columns.stream().filter(primaryKeys::contains).toList().isEmpty();
        this.keyColumns = columns.stream().filter(keys::contains).mapToInt(Integer::intValue).toArray();
        this.tableColumns = columns.stream().mapToInt(Integer::intValue).toArray();
        this.autoColumns = names.toArray(new String[0]);

        String tableName = buildTableName(dbmd, table, delimiter);
        this.selectCommand = composeSelectCommand(rsmd, columns, tableName, delimiter);
        this.updateCommand = composeUpdateCommand(tableName);
        this.insertCommand = composeInsertCommand(rsmd, columns, tableName, delimiter);
        this.deleteCommand = composeDeleteCommand(tableName);
    }

    protected String getName() {
        return name;
    }

    protected String getDescription() {
        return name + description;
    }

    protected List<Integer> getTableColumns() {
        return Arrays.stream(tableColumns).boxed().toList();
    }

    protected void updateResolvedConflictToDB(CachedRowSet crs, Connection connection)
        throws SQLException {

        ResultSetMetaData rsmd = crs.getMetaData();
        List<Entry<Object, Integer>> parameters = new ArrayList<>();

        StringBuilder update = new StringBuilder(updateCommand);
        update.append(getUpdateSetCmd(crs, rsmd, parameters));
        update.append(" WHERE ");
        update.append(getWherePredicate(crs, rsmd, parameters));

        executeUpdate(connection, update.toString(), parameters, Optional.empty());
    }

    protected void insertCurrentRow(Connection connection, ResultSetMetaData rsmd,
                                    CachedRowSet crs, int row)
        throws SQLException {

        // We update on insert only if we have auto-increment columns in the CachedRowSet
        boolean generatedKey = updateOnInsert && supportsGeneratedKeys;

        StringJoiner values = new StringJoiner(SEPARATOR);
        try (PreparedStatement stmt = getInsertPreparedStatement(connection, generatedKey)) {
            // XXX: Auto-increment columns are ignored during inserts (needed by PostGreSQL)
            int i = 0;
            for (int index : tableColumns) {
                if (isInsertableColumns(rsmd, index)) {
                    Object value = crs.getObject(index);
                    if (value != null) {
                        stmt.setObject(++i, value);
                        values.add(value.toString());
                    } else {
                        stmt.setNull(++i, rsmd.getColumnType(index));
                        values.add("NULL");
                    }
                }
            }

            int count = stmt.executeUpdate();
            if (count != 1) {
                String msg = resBundle.handleGetObject("crswriter.insert.cmd.error").toString();
                throw new SQLException(MessageFormat.format(msg, row, insertCommand, count));
            }

            if (generatedKey) {
                updateGeneratedKeys(connection, rsmd, crs, stmt);
            }
            log(Level.INFO, "crswriter.insert.cmd", row, insertCommand, values.toString());
        } catch (SQLException e) {
            log(Level.ERROR, "crswriter.insert.cmd", row, insertCommand, values.toString());
            throw e;
        }
    }

    protected String getDeleteQuery(Connection connection, ResultSetMetaData rsmd, ResultSet origVals,
                                    CachedRowSet crs, int row, List<Entry<Object, Integer>> parameters,
                                    int rsType, int rsConcurrency)
        throws SQLException {

        String predicate = buildWhereClause(rsmd, origVals, parameters);
        String query = selectCommand + predicate;

        try (PreparedStatement stmt = connection.prepareStatement(query, rsType, rsConcurrency)) {

            setStatementParameters(stmt, parameters);
            setStatementProperties(crs, stmt);

            if (hasPrimarykeys) {
                return deleteWithoutCheck(origVals, stmt, predicate, row);
            } else {
                return deleteWithCheck(origVals, stmt, predicate, row);
            }
        } catch (SQLException e) {
            List<Object> values = parameters.stream().map(Entry::getKey).toList();
            log(Level.ERROR, "crswriter.delete.cmd", row, query, values);
            throw e;
        }
    }

    protected void executeDeleteStatement(Connection connection, String query, int row,
                                          List<Entry<Object, Integer>> parameters)
        throws SQLException {
        StringJoiner values = new StringJoiner(SEPARATOR);
        try {
            int count = executeUpdate(connection, query, parameters, Optional.of(values));
            if (count != 1) {
                String msg = resBundle.handleGetObject("crswriter.delete.cmd.error").toString();
                throw new SQLException(MessageFormat.format(msg, row, query, count));
            }
            log(Level.INFO, "crswriter.delete.cmd", row, query, values.toString());
        } catch (SQLException e) {
            log(Level.ERROR, "crswriter.delete.cmd", row, query, values.toString());
            throw e;
        }
    }

    protected boolean isRowUpdated(CachedRowSet crs)
        throws SQLException {
        boolean updated = false;
        for (int index : tableColumns) {
            if (crs.columnUpdated(index)) {
                updated = true;
                break;
            }
        }
        return updated;
    }

    protected String getUpdateQuery(Connection connection, ResultSetMetaData rsmd, ResultSet origVals,
                                    CachedRowSet crs, int row, List<Entry<Object, Integer>> parameters,
                                    int rsType, int rsConcurrency)
        throws SQLException {

        List<Entry<Object, Integer>> params = new ArrayList<>();
        String predicate = buildWhereClause(rsmd, origVals, params);
        String query = selectCommand + predicate;

        try (PreparedStatement stmt = connection.prepareStatement(query, rsType, rsConcurrency)) {

            setStatementParameters(stmt, params);
            setStatementProperties(crs, stmt);
            StringJoiner updateSet = new StringJoiner(SEPARATOR);
            parameters.addAll(updateRow(connection, rsmd, crs, origVals, stmt, params, updateSet, row));
            return updateCommand + updateSet.toString() + predicate;

        } catch (SQLException e) {
            List<Object> values = parameters.stream().map(Entry::getKey).toList();
            log(Level.ERROR, "crswriter.update.cmd", row, query, values);
            throw e;
        }
    }

    protected void executeUpdateStatement(Connection connection, String query, int row,
                                          List<Entry<Object, Integer>> parameters)
        throws SQLException {

        int count;
        StringJoiner values = new StringJoiner(SEPARATOR);
        try {
            count = executeUpdate(connection, query, parameters, Optional.of(values));
        } catch (SQLException e) {
            log(Level.ERROR, "crswriter.update.cmd", row, query, values.toString());
            throw e;
        }
        /**
         * i should be equal to 1(row count), because we update
         * one row(returned as row count) at a time, if all goes well.
         * if 1 != 1, this implies we have not been able to
         * do updations properly i.e there is a conflict in database
         * versus what is in CachedRowSet for this particular row.
         **/
        if (count != 1) {
            String msg = resBundle.handleGetObject("crswriter.update.cmd.error").toString();
            throw new SQLException(MessageFormat.format(msg, row, query, count));
        }
        log(Level.INFO, "crswriter.update.cmd", row, query, values.toString());
    }

    protected record Table(Optional<String> catalog, Optional<String> schema, String name) {

        Table(ResultSetMetaData md, int index)
            throws java.sql.SQLException {
            this(Optional.ofNullable(md.getCatalogName(index)),
                 Optional.ofNullable(md.getSchemaName(index)),
                 md.getTableName(index));
        }
    }

    private int executeUpdate(Connection connection, String query,
                              List<Entry<Object, Integer>> parameters,
                              Optional<StringJoiner> values)
        throws SQLException {

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            int i = 0;
            for (Entry<Object, Integer> parameter : parameters) {
                Object value = parameter.getKey();
                int type = parameter.getValue();
                if (value == null) {
                    stmt.setNull(++i, type);
                    values.ifPresent(sj -> sj.add("NULL"));
                } else {
                    stmt.setObject(++i, value, type);
                    values.ifPresent(sj -> sj.add(value.toString()));
                }
            }
            return stmt.executeUpdate();
        }
    }

    private void updateGeneratedKeys(Connection connection, ResultSetMetaData rsmd,
                                     CachedRowSet crs, PreparedStatement stmt)
        throws SQLException {
        Optional<String> select = Optional.empty();
        List<Entry<Object, Integer>> parameters = new ArrayList<>();

        try (ResultSet rsKey = stmt.getGeneratedKeys()) {
            ResultSetMetaData mdKey = rsKey.getMetaData();
            if (mdKey.getColumnCount() < tableColumns.length) {
                select = buildSelectNewInsertedRow(rsmd, rsKey, mdKey , parameters);
            } else {
                updateCachedRowSet(rsmd, crs, rsKey);
            }
        }
        if (select.isPresent()) {
            executeSelectNewInsertedRow(connection, rsmd, crs, parameters, select.get ());
        }
    }

    private void executeSelectNewInsertedRow(Connection connection, ResultSetMetaData rsmd, CachedRowSet crs,
                                             List<Entry<Object, Integer>> parameters, String select)
        throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(select)) {
            int i = 0;
            for (Entry<Object, Integer> entry : parameters) {
                stmt.setObject(++i, entry.getKey(), entry.getValue());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                updateCachedRowSet(rsmd, crs, rs);
            }
        }
    }

    private Optional<String> buildSelectNewInsertedRow(ResultSetMetaData rsmd, ResultSet rsKey,
                                                       ResultSetMetaData mdKey, List<Entry<Object, Integer>> parameters)
        throws SQLException {
        Optional<String> select = Optional.empty();

        if (rsKey.next()) {
            int count = mdKey.getColumnCount();
            StringJoiner predicates = new StringJoiner(" AND ");
            for (int index : keyColumns) {
                setWherePredicate(rsmd, rsKey, mdKey, parameters, predicates, count, index);
            }

            // With MariaDB Column name can't be retrieved we need to retrieve the table's primary key
            if (predicates.length() == 0 && hasPrimarykeys) {
                for (int index : keyColumns) {
                    setWherePredicatePk(rsmd, rsKey, mdKey, parameters, predicates, count, index);
                }
            }
            if (predicates.length() > 0) {
                select = Optional.of(selectCommand + " WHERE " + predicates.toString());
            }
        }
        return select;
    }

    private void updateCachedRowSet(ResultSetMetaData rsmd, CachedRowSet crs, ResultSet rs)
        throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        if (rs.next()) {
            for (int i = 1; i <= md.getColumnCount(); i++) {
                int j = 0;
                try {
                    j = crs.findColumn(md.getColumnLabel(i));
                } catch (SQLException e) { }
                if (j > 0 && rsmd.getColumnType(j) == md.getColumnType(i)) {
                    Object keyval = rs.getObject(i);
                    if (keyval != null) {
                        crs.updateObject(j, keyval);
                    } else {
                        crs.updateNull(j);
                    }
                }
            }
        }
    }

    private PreparedStatement getInsertPreparedStatement(Connection connection, boolean generatedKey)
        throws SQLException {
        PreparedStatement stmt;
        if (generatedKey) {
            stmt = connection.prepareStatement(insertCommand, autoColumns);
        } else {
            stmt = connection.prepareStatement(insertCommand, Statement.NO_GENERATED_KEYS);
        }
        return stmt;
    }

    private void setWherePredicate(ResultSetMetaData rsmd, ResultSet rsKey,
                                   ResultSetMetaData mdKey, List<Map.Entry<Object, Integer>> parameters,
                                   StringJoiner predicates, int count, int index)
        throws SQLException {
        String predicate = null;
        String column = rsmd.getColumnName(index);
        int type = rsmd.getColumnType(index);
        for (int j = 1; j <= count; j++) {
            if (column.equals(mdKey.getColumnName(j))) {
                predicate = getWherePredicate(rsKey, parameters, j, column, type);
                break;
            }
        }
        if (predicate != null) {
            predicates.add(predicate);
        }
    }

    private String getWherePredicate(CachedRowSet crs, ResultSetMetaData rsmd,
                                     List<Entry<Object, Integer>> parameters)
        throws SQLException {
        StringJoiner predicates = new StringJoiner(" AND ");
        for (int index : keyColumns) {
            String column = rsmd.getColumnName(index);
            int type = rsmd.getColumnType(index);
            predicates.add(getWherePredicate(crs, parameters, index, column, type));
        }
        return predicates.toString();
    }

    private String getWherePredicate(ResultSet rs, List<Entry<Object, Integer>> parameters,
                                     int index, String column, int type)
        throws SQLException {
        Object value = rs.getObject(index);
        if (value != null) {
            parameters.add(new SimpleEntry<Object, Integer>(value, type));
            return quoted(column) + QUESTION_MARK;
        }
        return quoted(column) + " IS NULL";
    }

    private void setWherePredicatePk(ResultSetMetaData rsmd, ResultSet rsKey,
                                     ResultSetMetaData mdKey, List<Entry<Object, Integer>> parameters,
                                     StringJoiner predicates, int count, int index)
        throws SQLException {
        String predicate = null;
        String column = rsmd.getColumnName(index);
        int type = rsmd.getColumnType(index);
        for (int j = 1; j <= count; j++) {
            if (RowSetHelper.isSimilarType(mdKey.getColumnType(j), type)) {
                predicate = getWherePredicate(rsKey, parameters, j, column, type);
                break;
            }
        }
        if (predicate != null) {
            predicates.add(predicate);
        }
    }

    private String buildWhereClause(ResultSetMetaData rsmd, ResultSet rs,
                                    List<Entry<Object, Integer>> parameters)
        throws SQLException {
        StringJoiner predicates = new StringJoiner(" AND ");
        for (int index : keyColumns) {
            String column = rsmd.getColumnName(index);
            int type = rsmd.getColumnType(index);
            predicates.add(getWherePredicate(rs, parameters, index, column, type));
        }
        return " WHERE " + predicates.toString();
    }

    private String deleteWithoutCheck(ResultSet origVals, PreparedStatement stmt, String predicate, int row)
        throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return getdeleteQuery(origVals, rs, predicate, row);
            } else {
                // didn't find the row
                String msg = resBundle.handleGetObject("crswriter.delete.norow.error").toString();
                throw new SQLException(MessageFormat.format(msg, row));
            }
        }
    }

    private String deleteWithCheck(ResultSet origVals, PreparedStatement stmt, String predicate, int row)
        throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                if (rs.next()) {
                    // more than one row
                    int duplicate = 2;
                    while (rs.next()) {
                        duplicate++;
                    }
                    String msg = resBundle.handleGetObject("crswriter.delete.duplicate.error").toString();
                    throw new SQLException(MessageFormat.format(msg, row, duplicate));
                }
                if (rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
                    rs.first();
                    return getdeleteQuery(origVals, rs, predicate, row);
                }
            } else {
                String msg = resBundle.handleGetObject("crswriter.delete.norow.error").toString();
                throw new SQLException(MessageFormat.format(msg, row));
            }
        }

        // XXX: We need to close the ResultSet and open an other one for
        // XXX: database like SQLite supporting only ResultSet.TYPE_FORWARD_ONLY
        return deleteWithoutCheck(origVals, stmt, predicate, row);
    }

    private void setStatementParameters(PreparedStatement stmt, List<Entry<Object, Integer>> parameters)
        throws SQLException {
        int i = 0;
        for (Entry<Object, Integer> parameter : parameters) {
            stmt.setObject(++i, parameter.getKey(), parameter.getValue());
        }
    }

    private void setStatementProperties(CachedRowSet crs, PreparedStatement stmt) {
        try {
            stmt.setMaxRows(crs.getMaxRows());
            stmt.setMaxFieldSize(crs.getMaxFieldSize());
            stmt.setEscapeProcessing(crs.getEscapeProcessing());
            stmt.setQueryTimeout(crs.getQueryTimeout());
        } catch (Exception e) {
            // Older driver don't support these operations.
        }
    }

    private String getdeleteQuery(ResultSet origVals, ResultSet rs, String predicate, int row)
        throws SQLException {
        // Now check all the values in rs to be same in
        // db also before actually going ahead with deleting

        if (isCachedRowSetModified(origVals, rs)) {
            String msg = resBundle.handleGetObject("crswriter.delete.conflict.error").toString();
            throw new SQLException(MessageFormat.format(msg, row));
        }
        return deleteCommand + predicate;
    }

    private boolean isCachedRowSetModified(ResultSet origVals, ResultSet rs)
        throws SQLException {
        boolean modified = false;

        int i = 0;
        for (int index : tableColumns) {
            Object original = origVals.getObject(index);
            Object changed = rs.getObject(++i);

            if (original != null && changed != null ) {
                if (!(original.toString()).equals(changed.toString())) {
                    modified = true;
                    break;
                }
            }
        }
        return modified;
    }

    private List<Entry<Object, Integer>> updateRow(Connection connection, ResultSetMetaData rsmd,
                                                   CachedRowSet crs, ResultSet origVals,
                                                   PreparedStatement stmt,
                                                   List<Entry<Object, Integer>> parameters,
                                                   StringJoiner updateSet, int row)
        throws SQLException {

        if (hasPrimarykeys) {
            return updateWithoutCheck(connection, rsmd, crs, origVals, stmt, parameters, updateSet, row);
        } else {
            return updateWithCheck(connection, rsmd, crs, origVals, stmt, parameters, updateSet, row);
        }
    }

    private List<Entry<Object, Integer>> updateWithoutCheck(Connection connection, ResultSetMetaData rsmd,
                                                            CachedRowSet crs, ResultSet origVals,
                                                            PreparedStatement stmt,
                                                            List<Entry<Object, Integer>> parameters,
                                                            StringJoiner updateSet, int row)
        throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return getUpdateParameters(connection, rsmd, crs, origVals, rs, parameters, updateSet, row);
            } else {
                /**
                * Cursor will be here, if the ResultSet may not return even a single row
                * i.e. we can't find the row where to update because it has been deleted
                * etc. from the db.
                * Present the whole row as null to user, to force null to be sync'ed
                * and hence nothing to be synced.
                *
                * NOTE:
                * ------
                * In the database if a column that is mapped to java.sql.Types.REAL stores
                * a Double value and is compared with value got from ResultSet.getFloat()
                * no row is retrieved and will throw a SyncProviderException. For details
                * see bug Id 5053830
                **/
                String msg = resBundle.handleGetObject("crswriter.update.norow.error").toString();
                throw new SQLException(MessageFormat.format(msg, row));
            }
        }
    }

    private List<Entry<Object, Integer>> updateWithCheck(Connection connection, ResultSetMetaData rsmd,
                                                         CachedRowSet crs, ResultSet origVals,
                                                         PreparedStatement stmt,
                                                         List<Entry<Object, Integer>> parameters,
                                                         StringJoiner updateSet, int row)
        throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                if (rs.next()) {
                    /** More than one row conflict.
                    *  If rs has only one row we are able to
                    *  uniquely identify the row where update
                    *  have to happen else if more than one
                    *  row implies we cannot uniquely identify the row
                    *  where we have to do updates.
                    *  crs.setKeyColumns needs to be set to
                    *  come out of this situation.
                    */
                    int duplicate = 2;
                    while (rs.next()) {
                        duplicate++;
                    }
                    String msg = resBundle.handleGetObject("crswriter.update.duplicate.error").toString();
                    throw new SQLException(MessageFormat.format(msg, row, duplicate));
                }
                if (rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
                    rs.first();
                    return getUpdateParameters(connection, rsmd, crs, origVals, rs, parameters, updateSet, row);
                }
            } else {
                String msg = resBundle.handleGetObject("crswriter.update.norow.error").toString();
                throw new SQLException(MessageFormat.format(msg, row));
            }
        }

        // XXX: We need to close the ResultSet and open an other one for
        // XXX: database like SQLite supporting only ResultSet.TYPE_FORWARD_ONLY
        return updateWithoutCheck(connection, rsmd, crs, origVals, stmt, parameters, updateSet, row);
    }

    private List<Entry<Object, Integer>> getUpdateParameters(Connection connection, ResultSetMetaData rsmd,
                                                             CachedRowSet crs, ResultSet origVals, ResultSet rs,
                                                             List<Entry<Object, Integer>> params,
                                                             StringJoiner updateSet, int row)
        throws SQLException {

        Map<String, Class<?>> map;
        if (crs.getTypeMap() != null) {
            map = crs.getTypeMap();
        } else {
            map = connection.getTypeMap();
        }

        // how many fields need to be updated
        List<Entry<Object, Integer>> parameters = new ArrayList<>();
        int i = 0;
        for (int index : tableColumns) {
            updateCurrentRow(rsmd, crs, origVals, rs, index, map, updateSet, row, parameters, ++i);
        }

        if (parameters.isEmpty()) {
            // XXX: Normally this should never happen
            String msg = resBundle.handleGetObject("crswriter.update.error").toString();
            throw new SQLException(MessageFormat.format(msg, row));
        }

        parameters.addAll(params);
        return parameters;
    }

    private void updateCurrentRow(ResultSetMetaData rsmd, CachedRowSet crs, ResultSet origVals,
                                  ResultSet rs, int index, Map<String, Class<?>> map, StringJoiner updateSet,
                                  int row, List<Entry<Object, Integer>> parameters, int i)
        throws SQLException {
        Object orig = origVals.getObject(index);
        Object curr = crs.getObject(index);

        /**
         * the following block creates equivalent objects
         * that would have been created if this rs is populated
         * into a CachedRowSet so that comparison of the column values
         * from the ResultSet and CachedRowSet are possible
         */
        Object dbval = getResultSetValue(rsmd, rs, map, row, i);

        /** This additional checking has been added when the current value
         *  in the DB is null, but the DB had a different value when the
         *  data was actually fetched into the CachedRowSet.
         **/

        if (!Objects.equals(dbval, orig)) {
            // value in db has changed
            // don't proceed with synchronization
            // get the value in db and pass it to the resolver.
            String msg = resBundle.handleGetObject("crswriter.update.conflict.error").toString();
            throw new SQLException(MessageFormat.format(msg, row));
        }
        if (!Objects.equals(orig, curr)) {
            // When values from db and values in CachedRowSet are not equal,
            // if db value is same as before updation for each col in
            // the row before fetching into CachedRowSet,
            // only then we go ahead with updation, else we
            // throw SyncProviderException.

            // if value has changed in db after fetching from db
            // for some cols of the row and at the same time, some other cols
            // have changed in CachedRowSet, no synchronization happens

            // Synchronization happens only when data when fetching is
            // same or at most has changed in cachedrowset

            // check orig value with what is there in crs for a column
            // before updation in crs.

            if (crs.columnUpdated(index)) {
                // At this point we are sure that
                // the value updated in crs was from
                // what is in db now and has not changed

                String column = rsmd.getColumnName(index);
                int type = rsmd.getColumnType(index);
                updateSet.add(quoted(column) + QUESTION_MARK);
                parameters.add(new SimpleEntry<Object, Integer>(curr, type));
            } else {
                // XXX: Normally this should never happen
                String msg = resBundle.handleGetObject("crswriter.update.error").toString();
                throw new SQLException(MessageFormat.format(msg, row));
            }
        }
    }

    private Object getResultSetValue(ResultSetMetaData rsmd, ResultSet rs,
                                     Map<String, Class<?>> map, int row, int index)
        throws SQLException {
        // XXX: If we want to be able to compare numerical values,
        // XXX: it is necessary to convert them to ensure we have the same types.
        int type = rs.getMetaData().getColumnType(index);
        Object rsval;
        if (map == null || map.isEmpty()) {
            rsval = rs.getObject(index);
        } else {
            rsval = rs.getObject(index, map);
        }
        if (rsval != null && RowSetHelper.isNumeric(type)) {
            rsval = RowSetHelper.convertNumeric(resBundle, rsval, Types.NULL, type);
        } else if (rsval instanceof Struct) {
            Struct s = (Struct) rsval;
            // look up the class in the map
            Class<?> c = null;
            String typename = s.getSQLTypeName();
            c = map.get(typename);
            if (c != null) {
                // create new instance of the class
                SQLData obj = null;
                try {
                    Object tmp = c.getDeclaredConstructor().newInstance();
                    obj = (SQLData) tmp;
                } catch (Exception ex) {
                    String column = rsmd.getColumnName(index);
                    String msg = resBundle.handleGetObject("crswriter.update.struct.error").toString();
                    throw new SQLException(MessageFormat.format(msg, row, typename, column), ex);
                }
                // get the attributes from the struct
                Object attribs[] = s.getAttributes(map);
                // create the SQLInput "stream"
                SQLInputImpl sqlInput = new SQLInputImpl(attribs, map);
                // read the values...
                obj.readSQL(sqlInput, s.getSQLTypeName());
                rsval = obj;
            }
        } else if (rsval instanceof SQLData) {
            rsval = new SerialStruct((SQLData) rsval, map);
        } else if (rsval instanceof Blob) {
            rsval = new SerialBlob((Blob) rsval);
        } else if (rsval instanceof Clob) {
            rsval = new SerialClob((Clob) rsval);
        } else if (rsval instanceof java.sql.Array) {
            rsval = new SerialArray((java.sql.Array) rsval, map);
        }
        return rsval;
    }

    private String getUpdateSetCmd(CachedRowSet crs, ResultSetMetaData rsmd,
                                   List<Entry<Object, Integer>> parameters)
        throws SQLException {
        StringJoiner updateSet = new StringJoiner(SEPARATOR);
        for (int index : tableColumns) {
            if (crs.columnUpdated(index)) {
                String column = rsmd.getColumnName(index);
                Object value = crs.getObject(index);
                int type = rsmd.getColumnType(index);
                parameters.add(new SimpleEntry<Object, Integer>(value, type));
                updateSet.add(quoted(column) + QUESTION_MARK);
            }
        }
        return updateSet.toString();
    }

    private String quoted(String identifier) {
        return quoted(identifier, identifierQuote);
    }

    private void log(Level level, String resource, Object... args) {
        if (logger.isLoggable(level)) {
            String msg = resBundle.handleGetObject(resource).toString();
            logger.log(level, MessageFormat.format(msg, args));
        }
    }

    private static String getDescription(JdbcRowSetResourceBundle bundle,
                                         ResultSetMetaData rsmd,
                                         List<Integer> primaryKeys)
        throws SQLException {
        if (primaryKeys.isEmpty()) {
            return "";
        }
        StringJoiner description = new StringJoiner(SEPARATOR, "[", "]");
        for (int index : primaryKeys) {
            description.add(rsmd.getColumnName(index));
        }
        String msg = bundle.handleGetObject("crswriter.twriter.description").toString();
        return MessageFormat.format(msg, " ", description.toString());
    }

    private static List<Integer> getTableColumns(ResultSetMetaData rsmd, Table table, List<String> names)
        throws SQLException {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            if (table.equals(new Table(rsmd, i))) {
                if (rsmd.isAutoIncrement(i)) {
                    names.add(rsmd.getColumnName(i));
                }
                indexes.add(i);
            }
        }
        return indexes;
    }

    private static String buildTableName(DatabaseMetaData dbmd, Table table)
        throws SQLException {
        return buildTableName(dbmd, table, Optional.empty());
    }

    private static String buildTableName(DatabaseMetaData dbmd, Table table, String delimiter)
        throws SQLException {
        return buildTableName(dbmd, table, Optional.ofNullable(delimiter));
    }

    private static String buildTableName(DatabaseMetaData dbmd, Table table, Optional<String> delimiter)
        throws SQLException {

        StringBuilder tableBuilder = new StringBuilder();

        if (dbmd.isCatalogAtStart()) {
            if (table.catalog.isPresent()) {
                tableBuilder.append(quoted(table.catalog.get(), delimiter));
                tableBuilder.append(dbmd.getCatalogSeparator());
            }
            if (table.schema.isPresent()) {
                tableBuilder.append(quoted(table.schema.get(), delimiter));
                tableBuilder.append(DOT);
            }
            tableBuilder.append(quoted(table.name, delimiter));
        } else {
            if (table.schema.isPresent()) {
                tableBuilder.append(quoted(table.schema.get(), delimiter));
                tableBuilder.append(DOT);
            }
            tableBuilder.append(quoted(table.name, delimiter));
            if (table.catalog.isPresent()) {
                tableBuilder.append(dbmd.getCatalogSeparator());
                tableBuilder.append(quoted(table.catalog.get(), delimiter));
            }
        }
        return tableBuilder.toString();
    }

    private static String composeSelectCommand(ResultSetMetaData rsmd, List<Integer> columns,
                                               String tableName, String delimiter)
        throws SQLException {

        StringJoiner names = new StringJoiner(SEPARATOR) ;
        for (int index : columns) {
            String name = rsmd.getColumnName(index);
            names.add(quoted(name, delimiter));
        }

        return "SELECT " + names.toString() + " FROM " + tableName;
    }

    private static String composeUpdateCommand(String tableName) {
        return "UPDATE " + tableName + " SET ";
    }

    private static String composeInsertCommand(ResultSetMetaData rsmd, List<Integer> columns,
                                               String tableName, String delimiter)
        throws SQLException {

        int count = 0;
        StringJoiner names = new StringJoiner(SEPARATOR, " (", ")");
        for (int index : columns) {
            if (isInsertableColumns(rsmd, index)) {
                // XXX: Auto-increment columns are ignored during inserts (needed by PostGreSQL)
                String name = rsmd.getColumnName(index);
                names.add(quoted(name, delimiter));
                count++;
            }
        }

        StringJoiner values = new StringJoiner(SEPARATOR, "(", ")");
        for (int i = 0; i < count; i++) {
            values.add("?");
        }

        return "INSERT INTO " + tableName + names.toString() + " VALUES " + values.toString();
    }

    private static String composeDeleteCommand(String tableName) {
        return "DELETE FROM " + tableName;
    }

    private static boolean isInsertableColumns(ResultSetMetaData rsmd, int i)
        throws SQLException {
        return !rsmd.isAutoIncrement(i) && !rsmd.isReadOnly(i) && rsmd.isWritable(i);
    }

    private static String quoted(String value, Optional<String> delimiter) {
        return delimiter.map(quote -> quoted(value, quote)).orElse(value);
    }

    private static String quoted(String value, String delimiter) {
        return delimiter + value.replace(delimiter, delimiter + delimiter) + delimiter;
    }

}

