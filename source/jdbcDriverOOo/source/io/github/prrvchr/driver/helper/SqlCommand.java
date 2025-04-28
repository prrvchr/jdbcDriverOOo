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
package io.github.prrvchr.driver.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SqlCommand {

    private static final int NO_INDEX = -1;

    private static final String SPACE = " ";
    private static final String REGEX_SPACE = "\\s+";
    private static final String TOKEN_MULTI_LINE_COMMENT_START = "/*+";
    private static final String TOKEN_MULTI_LINE_COMMENT_END = "*/";
    private static final String TOKEN_SINGLE_LINE_COMMENT = "--";
    private static final String TOKEN_NEWLINE = "\\r\\n|\\r|\\n|\\n\\r";
    private static final String TOKEN_SEMI_COLON = ";";
    private static final String TOKEN_COMMA = ",";

    private static final String KEYWORD_INTO = "into";
    private static final String KEYWORD_FROM = "from";

    private static final String SQL_INSERT = "insert";
    private static final String SQL_SELECT = "select";
    
    private static final String[] SQL_COMMANDS = {SQL_INSERT, SQL_SELECT};


    private String mTable = "";
    private String mCommand = "";
    private String mType = "";

    /**
     * Extracts table name out of SQL if query is INSERT or SELECT.
     * ie queries executed by: - java.sql.Statement.executeUpdate()
     *                         - java.sql.Statement.executeQuery()
     *                         - java.sql.PreparedStatement.executeUpdate()
     *                         - java.sql.PreparedStatement.executeQuery()
     * @param sql
     */
    public SqlCommand(final String sql) {
        mCommand = sql;
        String nocomments = removeComments(sql);
        String normalized = normalize(nocomments);
        String cleaned = clean(normalized);
        String[] tokens = cleaned.split(REGEX_SPACE);

        if (tokens.length > 0 && isCommand(tokens[0])) {
            int index = 1;
            while (index < tokens.length) {
                String token = tokens[index++];
                if (shouldProcess(token)) {
                    mTable = tokens[index];
                    break;
                }
            }
        }
    }

    /**
     * 
     * @return the SQL command
     */
    public String toString() {
        return mCommand;
    }

    /**
     * 
     * @return the SQL command
     */
    public String getCommand() {
        return mCommand;
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
     * @return if table name has been extracted out of SQL command
     */
    public boolean hasTable() {
        return !mTable.isBlank();
    }

    /**
     * 
     * @return if SQL command is an SELECT command
     */
    public boolean isSelectCommand() {
        return mType.equals(SQL_SELECT);
    }

    /**
     * 
     * @return if SQL command is an INSERT command
     */
    public boolean isInsertCommand() {
        return mType.equals(SQL_INSERT);
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
        for (String type : SQL_COMMANDS) {
            if (type.equals(token.toLowerCase())) {
                mType = type;
                iscommand = true;
                break;
            }
        }
        return iscommand;
    }

    private boolean shouldProcess(final String token) {
        boolean process = false;
        if (mType.equals(SQL_INSERT)) {
            process =  KEYWORD_INTO.equals(token.toLowerCase());
        } else if (mType.equals(SQL_SELECT)) {
            process = KEYWORD_FROM.equals(token.toLowerCase());
        }
        return process;
    }

}
