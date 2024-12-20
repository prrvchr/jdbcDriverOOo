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
package io.github.prrvchr.jdbcdriver.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SqlCommand {

    private static final int NO_INDEX = -1;

    private static final String SPACE = " ";
    private static final String REGEX_SPACE = "\\s+";
    private static final String TOKEN_ORACLE_HINT_START = "/*+";
    private static final String TOKEN_ORACLE_HINT_END = "*/";
    private static final String TOKEN_SINGLE_LINE_COMMENT = "--";
    private static final String TOKEN_NEWLINE = "\\r\\n|\\r|\\n|\\n\\r";
    private static final String TOKEN_SEMI_COLON = ";";
    private static final String TOKEN_COMMA = ",";

    private static final String KEYWORD_INTO = "into";
    private static final String KEYWORD_FROM = "from";

    private static final String SQL_INSERT = "insert";
    private static final String SQL_SELECT = "select";
    
    private static final String[] SQL_COMMANDS = {SQL_INSERT, SQL_SELECT};


    private String m_Table = "";
    private String m_Command = "";
    private String m_Type = "";

    /**
     * Extracts table name out of SQL if query is INSERT or SELECT
     * ie queries executed by: - java.sql.Statement.executeUpdate()
     *                         - java.sql.Statement.executeQuery()
     *                         - java.sql.PreparedStatement.executeUpdate()
     *                         - java.sql.PreparedStatement.executeQuery()
     * @param sql
     */
    public SqlCommand(final String sql)
    {
        m_Command = sql;
        String nocomments = removeComments(sql);
        String normalized = normalize(nocomments);
        String cleansed = clean(normalized);
        String[] tokens = cleansed.split(REGEX_SPACE);

        int index = 1;
        if (tokens.length > 0) {
            String token = tokens[0];
            if (isToken(token)) {
                while (index < tokens.length) {
                    token = tokens[index++];
                    if (shouldProcess(token)) {
                        m_Table = tokens[index];
                        break;
                    }
                }
            }
        }
    }

    /**
     * 
     * @return the SQL command
     */
    public String toString() {
        return m_Command;
    }

    /**
     * 
     * @return the SQL command
     */
    public String getCommand() {
        return m_Command;
    }

    /**
     * 
     * @return the table name extracted out of SQL command
     */
    public String getTable() {
        return m_Table;
    }

    /**
     * 
     * @return if table name has been extracted out of SQL command
     */
    public boolean hasTable() {
        return !m_Table.isBlank();
    }

    /**
     * 
     * @return if SQL command is an SELECT command
     */
    public boolean isSelectCommand() {
        return m_Type.equals(SQL_SELECT);
    }

    /**
     * 
     * @return if SQL command is an INSERT command
     */
    public boolean isInsertCommand() {
        return m_Type.equals(SQL_INSERT);
    }

    private String removeComments(final String sql)
    {
        StringBuilder builder = new StringBuilder(sql);
        int position = builder.indexOf(TOKEN_SINGLE_LINE_COMMENT);
        while (position > -1) {
            int end = indexOfRegex(TOKEN_NEWLINE, builder.substring(position));
            if (end == -1) {
                return builder.substring(0, position);
            }
            else {
                builder.replace(position, end + position, "");
            }
            position = builder.indexOf(TOKEN_SINGLE_LINE_COMMENT);
        }
        return builder.toString();
    }

    private int indexOfRegex(String regex, String string)
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string);
        return matcher.find() ? matcher.start() : -1;
    }

    private String normalize(final String sql)
    {
        String normalized = sql.trim().replaceAll(TOKEN_NEWLINE, SPACE)
                                      .replaceAll(TOKEN_COMMA, " , ")
                                      .replaceAll("\\(", " ( ")
                                      .replaceAll("\\)", " ) ");
        if (normalized.endsWith(TOKEN_SEMI_COLON)) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String clean(final String normalized)
    {
        int start = normalized.indexOf(TOKEN_ORACLE_HINT_START);
        int end = NO_INDEX;
        if (start != NO_INDEX) {
            end = normalized.indexOf(TOKEN_ORACLE_HINT_END);
            if (end != NO_INDEX) {
                String first = normalized.substring(0, start);
                String second = normalized.substring(end + 2, normalized.length());
                return first.trim() + SPACE + second.trim();
            }
        }
        return normalized;
    }

    private boolean isToken(final String token)
    {
        for (String type : SQL_COMMANDS) {
            if (type.equals(token.toLowerCase())) {
                m_Type = type;
                return true;
            }
        }
        return false;
    }

    private boolean shouldProcess(final String token)
    {
        boolean process = false;
        if (m_Type.equals(SQL_INSERT)) {
            process =  KEYWORD_INTO.equals(token.toLowerCase());
        }
        else if (m_Type.equals(SQL_SELECT)) {
            process = KEYWORD_FROM.equals(token.toLowerCase());
        }
        return process;
    }

}

