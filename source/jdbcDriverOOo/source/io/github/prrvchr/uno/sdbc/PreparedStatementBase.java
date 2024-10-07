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
package io.github.prrvchr.uno.sdbc;

import java.sql.PreparedStatement;

import io.github.prrvchr.jdbcdriver.helper.SqlCommand;

public abstract class PreparedStatementBase
    extends PreparedStatementMain
{

    // The constructor method:
    // XXX: Constructor called from methods:
    // XXX: - io.github.prrvchr.uno.sdbc.PreparedStatement()
    // XXX: - io.github.prrvchr.uno.sdbc.PreparedStatementSuper()
    public PreparedStatementBase(String service,
                                 String[] services,
                                 ConnectionBase connection,
                                 String sql)
    {
        super(service, services, connection);
        m_Sql = new SqlCommand(sql);
    }

    @Override
    protected PreparedStatement getJdbcStatement()
        throws java.sql.SQLException
    {
        checkDisposed();
        if (m_Statement == null) {
            PreparedStatement statement;
            String sql = m_Sql.getCommand();
            if (m_Sql.isInsertCommand()) {
                int option = m_Connection.getProvider().getGeneratedKeysOption();
                statement = m_Connection.getProvider().getConnection().prepareStatement(sql, option);
            }
            else if (m_ResultSetType != java.sql.ResultSet.TYPE_FORWARD_ONLY || m_ResultSetConcurrency != java.sql.ResultSet.CONCUR_READ_ONLY) {
                int holdability = java.sql.ResultSet.HOLD_CURSORS_OVER_COMMIT;
                //int holdability = java.sql.ResultSet.CLOSE_CURSORS_AT_COMMIT;
                statement = m_Connection.getProvider().getConnection().prepareStatement(sql,
                                                                                        m_ResultSetType,
                                                                                        m_ResultSetConcurrency,
                                                                                        holdability);
            }
            else {
                statement = m_Connection.getProvider().getConnection().prepareStatement(sql);
            }
            m_Statement = setStatement(statement);
        }
        return (PreparedStatement) m_Statement;
    }

}
