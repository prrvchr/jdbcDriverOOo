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
package io.github.prrvchr.jdbcdriver;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DBQueryParser {

    private static final int NO_INDEX = -1;
    private static final String SPACE = " ";
    private static final String REGEX_SPACE = "\\s+";

    private static final String TOKEN_ORACLE_HINT_START = "/*+";
    private static final String TOKEN_ORACLE_HINT_END = "*/";
    private static final String TOKEN_SINGLE_LINE_COMMENT = "--";
    private static String TOKEN_NEWLINE = "\\r\\n|\\r|\\n|\\n\\r";
    private static final String TOKEN_SEMI_COLON = ";";
    private static final String TOKEN_PARAN_START = "(";
    private static final String TOKEN_COMMA = ",";
    private static final String TOKEN_SET = "set";
    private static final String TOKEN_OF = "of";
    private static final String TOKEN_DUAL = "dual";
    private static final String TOKEN_DELETE = "delete";
    private static final String TOKEN_INSERT = "insert";
    private static final String TOKEN_REPLACE = "replace";
    private static final String TOKEN_ASTERICK = "*";

    private static final String KEYWORD_INTO = "into";
    private static final String KEYWORD_FROM = "from";
    private static final String KEYWORD_UPDATE = "update";

    private static final List<String> concerned = Arrays.asList(KEYWORD_INTO, KEYWORD_FROM, KEYWORD_UPDATE);
    private static final List<String> ignored = Arrays.asList(TOKEN_PARAN_START, TOKEN_SET, TOKEN_OF, TOKEN_DUAL);
    private static final List<String> inserted = Arrays.asList(KEYWORD_UPDATE, TOKEN_INSERT, TOKEN_REPLACE, TOKEN_DELETE);

    private boolean update = false;
    private String table = "";

    /**
     * Extracts table name out of SQL if query is INSERT, UPDATE, REPLACE or DELETE
     * ie queries executed by: - java.sql.Statement.executeUpdate()
     *                         - java.sql.PreparedStatement.executeUpdate()
     * @param sql
     */
    public DBQueryParser(final String sql) {
        String nocomments = removeComments(sql);
        String normalized = normalized(nocomments);
        String cleansed = clean(normalized);
        String[] tokens = cleansed.split(REGEX_SPACE);
        int index = 0;

        String firstToken = tokens[index];
        if (isUpdate(firstToken)) {
            if (isOracleSpecialDelete(firstToken, tokens, index)) {
                handleOracleSpecialDelete(firstToken, tokens, index);
            }
            else {
                while (table.isEmpty() && moreTokens(tokens, index)) {
                    String currentToken = tokens[index++];
                    if (shouldProcess(currentToken)) {
                        String nextToken = tokens[index++];
                        considerInclusion(nextToken);
                    }
                }
            }
            update = true;
        }
    }

    /**
     * 
     * @return the table name extracted out of sql
     */
    public String getTable() {
        return table;
    }

    /**
     * 
     * @return is table name has been extracted out of sql
     */
    public boolean hasTable() {
        return !table.isEmpty();
    }

    /**
     * 
     * @return if query extracted out of sql is an executeUpdate() (ie: insert, updated, replace...)
     */
    public boolean isExecuteUpdateStatement() {
        return update;
    }

    private String removeComments(final String sql) {
        StringBuilder sb = new StringBuilder(sql);
        int nextCommentPosition = sb.indexOf(TOKEN_SINGLE_LINE_COMMENT);
        while (nextCommentPosition > -1) {
            int end = indexOfRegex(TOKEN_NEWLINE, sb.substring(nextCommentPosition));
            if (end == -1) {
                return sb.substring(0, nextCommentPosition);
            }
            else {
                sb.replace(nextCommentPosition, end + nextCommentPosition, "");
            }
            nextCommentPosition = sb.indexOf(TOKEN_SINGLE_LINE_COMMENT);
        }
        return sb.toString();
    }

    private int indexOfRegex(String regex, String string) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string);
        return matcher.find() ? matcher.start() : -1;
    }

    private String normalized(final String sql) {
        String normalized = sql.trim().replaceAll(TOKEN_NEWLINE, SPACE).replaceAll(TOKEN_COMMA, " , ")
                .replaceAll("\\(", " ( ").replaceAll("\\)", " ) ");
        if (normalized.endsWith(TOKEN_SEMI_COLON)) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String clean(final String normalized) {
        int start = normalized.indexOf(TOKEN_ORACLE_HINT_START);
        int end = NO_INDEX;
        if (start != NO_INDEX) {
            end = normalized.indexOf(TOKEN_ORACLE_HINT_END);
            if (end != NO_INDEX) {
                String firstHalf = normalized.substring(0, start);
                String secondHalf = normalized.substring(end + 2, normalized.length());
                return firstHalf.trim() + SPACE + secondHalf.trim();
            }
        }
        return normalized;
    }

    private boolean isOracleSpecialDelete(final String currentToken, final String[] tokens, int index) {
        index++;// Point to next token
        if (TOKEN_DELETE.equals(currentToken)) {
            if (moreTokens(tokens, index)) {
                String nextToken = tokens[index++];
                if (!KEYWORD_FROM.equals(nextToken) && !TOKEN_ASTERICK.equals(nextToken)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void handleOracleSpecialDelete(final String currentToken, final String[] tokens, int index) {
        String tableName = tokens[index + 1];
        considerInclusion(tableName);
    }

    private boolean isUpdate(final String currentToken) {
        return inserted.contains(currentToken.toLowerCase());
    }

    private boolean shouldProcess(final String currentToken) {
        return concerned.contains(currentToken.toLowerCase());
    }

    private boolean moreTokens(final String[] tokens, int index) {
        return index < tokens.length;
    }

    private void considerInclusion(final String token) {
        if (!ignored.contains(token.toLowerCase()) && table.isEmpty()) {
            table = token;
        }
    }
}

