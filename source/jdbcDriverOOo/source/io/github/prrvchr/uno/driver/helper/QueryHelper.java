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
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Ultra light, Ultra fast parser to extract table name out SQLs, supports oracle dialect SQLs as well.
 * @author Nadeem Mohammad
 *
 */
package io.github.prrvchr.uno.driver.helper;

import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.star.sdbc.SQLException;

import io.github.prrvchr.uno.driver.helper.DBTools.NamedComponents;
import io.github.prrvchr.uno.driver.provider.ComposeRule;
import io.github.prrvchr.uno.driver.provider.Provider;
import io.github.prrvchr.uno.helper.UnoHelper;

public final class QueryHelper {

    public static final String TOKEN_NEWLINE = "\\r\\n|\\r|\\n|\\n\\r";
    public static final String SPACE = " ";

    private static final int NO_INDEX = -1;

    private static final String REGEX_SPACE = "\\s+";
    private static final String TOKEN_MULTI_LINE_COMMENT_START = "/*+";
    private static final String TOKEN_MULTI_LINE_COMMENT_END = "*/";
    private static final String TOKEN_SINGLE_LINE_COMMENT = "--";
    private static final String TOKEN_SEMI_COLON = ";";
    private static final String TOKEN_COMMA = ",";

    private static final String KEYWORD_INTO = "into";
    private static final String KEYWORD_UPDATE = "update";
    private static final String KEYWORD_FROM = "from";
    private static final String KEYWORD_JOIN = "join";
    private static final String KEYWORD_UNION = "union";

    private static final String SQL_INSERT = "insert";
    private static final String SQL_UPDATE = "update";
    private static final String SQL_SELECT = "select";

    private static final List<String> SQL_COMMANDS = List.of(SQL_INSERT, SQL_UPDATE, SQL_SELECT);

    private String mCatalog = null;
    private String mSchema = null;
    private String mTable = null;
    private String mTableName = null;
    private String mIdentifier = null;
    private String mQuery = null;
    private String mType = null;

    private Boolean mPrimaryKey = null;
    private Boolean mSingleTableSelect = false;

    private DatabaseMetaData mMetaData = null;

    /**
     * Extracts table name out of SQL if query is INSERT, UPDATE or SELECT.
     * ie queries executed by: - java.sql.Statement.executeUpdate()
     *                         - java.sql.Statement.executeQuery()
     *                         - java.sql.PreparedStatement.executeUpdate()
     *                         - java.sql.PreparedStatement.executeQuery()
     * @param provider
     * @param query
     * @throws com.sun.star.sdbc.SQLException 
     */
    public QueryHelper(final Provider provider, final String query)
        throws SQLException {
        mQuery = query;

        String table = getTableName(query);
        if (table != null) {
            try {
                ComposeRule rule = ComposeRule.InDataManipulation;
                NamedComponents tablename = DBTools.qualifiedNameComponents(provider, table, rule, true);
                mCatalog = tablename.getCatalog();
                mSchema = tablename.getSchema();
                mTable = tablename.getTable();
                mIdentifier = table;
                mTableName = DBTools.composeTableName(provider, tablename, rule, false);
                mMetaData = provider.getConnection().getMetaData();
            } catch (java.sql.SQLException e) {
                throw UnoHelper.getSQLException(e);
            }
        }
    }

    /**
     * 
     * @return the SQL command
     */
    public String toString() {
        return mQuery;
    }

    /**
     * 
     * @return the SQL command
     */
    public String getQuery() {
        return mQuery;
    }

    /**
     * 
     * @return the catalog name extracted out of SQL command
     */
    public String getCatalog() {
        return mCatalog;
    }

    /**
     * 
     * @return the schema name extracted out of SQL command
     */
    public String getSchema() {
        return mSchema;
    }

    /**
     * 
     * @return the table name extracted out of SQL command
     */
    public String getTable() {
        return mTable;
    }

    /**
     * 
     * @return the full quoted table name extracted out of SQL command
     */
    public String getTableIdentifier() {
        return mIdentifier;
    }

    /**
     * 
     * @return the full unquoted table name extracted out of SQL command
     */
    public String getTableName() {
        return mTableName;
    }

    /**
     * 
     * @return if table name has been extracted out of SQL command
     */
    public boolean hasTable() {
        return mTable != null;
    }

    /**
     * 
     * @return if SQL command is an INSERT command
     */
    public boolean isInsertCommand() {
        return SQL_INSERT.equals(mType);
    }

    /**
     * 
     * @return if SQL command is an UPDATE command
     */
    public boolean isUpdateCommand() {
        return SQL_UPDATE.equals(mType);
    }

    /**
     * 
     * @return if SQL command is an SELECT command
     */
    public boolean isSelectCommand() {
        return SQL_SELECT.equals(mType);
    }

    /**
     * 
     * @return if SQL command is an SELECT command on a single table
     */
    public boolean isSingleTableSelect() {
        return mSingleTableSelect;
    }

    private String getTableName(String query) {
        String table = null;
        String nocomments = removeComments(query);
        String normalized = normalize(nocomments);
        String cleaned = clean(normalized);
        String token;
        String[] tokens = cleaned.split(REGEX_SPACE);
        if (tokens.length > 0 && isCommand(tokens[0].toLowerCase())) {
            int index = 1;
            while (index < tokens.length) {
                token = tokens[index++];
                if (shouldProcess(token.toLowerCase())) {
                    table = tokens[index];
                    break;
                }
            }
            if (table != null && isSelectCommand()) {
                mSingleTableSelect = isLastTable(tokens, ++index);
            }
        }
        return table;
    }

    private boolean isLastTable(String[] tokens, int index) {
        String token, keyword;
        boolean last = true;
        while (index < tokens.length) {
            token = tokens[index++];
            keyword = token.toLowerCase();
            if (TOKEN_COMMA.equals(token) ||
                KEYWORD_JOIN.equals(keyword) ||
                KEYWORD_UNION.equals(keyword)) {
                last = false;
                break;
            }
        }
        return last;
    }

    private String removeComments(final String sql) {
        String query = null;
        StringBuilder builder = new StringBuilder(sql);
        int position = builder.indexOf(TOKEN_SINGLE_LINE_COMMENT);
        while (position != NO_INDEX) {
            int end = indexOfRegex(TOKEN_NEWLINE, builder.substring(position));
            if (end == NO_INDEX) {
                query = builder.substring(0, position);
                break;
            }
            builder.replace(position, end + position, "");
            position = builder.indexOf(TOKEN_SINGLE_LINE_COMMENT);
        }
        if (query == null) {
            query = builder.toString();
        }
        return query;
    }

    private String normalize(final String sql) {
        String normalized = sql.trim().replaceAll(TOKEN_NEWLINE, SPACE)
                                      .replaceAll(TOKEN_COMMA, " , ")
                                      .replaceAll("\\(", " ( ")
                                      .replaceAll("\\)", " ) ");
        if (normalized.endsWith(TOKEN_SEMI_COLON)) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String clean(final String normalized) {
        String cleaned = null;
        StringBuilder builder = new StringBuilder(normalized);
        int position = builder.indexOf(TOKEN_MULTI_LINE_COMMENT_START);
        while (position != NO_INDEX) {
            int end = builder.substring(position).indexOf(TOKEN_MULTI_LINE_COMMENT_END);
            if (end == NO_INDEX) {
                cleaned = builder.substring(0, position);
                break;
            }
            builder.replace(position, position + end + TOKEN_MULTI_LINE_COMMENT_END.length(), "");
            position = builder.indexOf(TOKEN_MULTI_LINE_COMMENT_START);
        }
        if (cleaned == null) {
            cleaned = builder.toString();
        }
        return cleaned;
    }

    private int indexOfRegex(String regex, String string) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string);
        int index;
        if (matcher.find()) {
            index = matcher.start();
        } else {
            index = NO_INDEX;
        }
        return index;
    }

    private boolean isCommand(final String token) {
        boolean iscommand = false;
        if (SQL_COMMANDS.contains(token)) {
            mType = token;
            iscommand = true;
        }
        return iscommand;
    }

    private boolean shouldProcess(final String token) {
        boolean process = false;
        switch (mType) {
            case SQL_INSERT:
                process = KEYWORD_INTO.equals(token);
                break;
            case SQL_UPDATE:
                process = KEYWORD_UPDATE.equals(token);
                break;
            case SQL_SELECT:
                process = KEYWORD_FROM.equals(token);
                break;
        }
        return process;
    }

    public boolean hasPrimaryKeys() {
        if (mPrimaryKey == null) {
            boolean has = false;
            if (isSelectCommand() && hasTable()) {
                try (java.sql.ResultSet rs = mMetaData.getPrimaryKeys(mCatalog, mSchema, mTable)) {
                    if (rs.next()) {
                        has = true;
                    }
                } catch (java.sql.SQLException e) { }
            }
            mPrimaryKey = has;
        }
        return mPrimaryKey;
    }

}
