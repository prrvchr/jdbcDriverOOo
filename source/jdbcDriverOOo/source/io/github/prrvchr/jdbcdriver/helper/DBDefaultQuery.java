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
/**************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 *************************************************************/
package io.github.prrvchr.jdbcdriver.helper;

public class DBDefaultQuery {

    public static final String STR_QUERY_EMPTY_RESULTSET                     = "SELECT 1 WHERE 0 = 1";
    public static final String STR_QUERY_METADATA_RESULTSET                  = "SELECT {0} FROM {1} WHERE 0 = 1";

    public static final String STR_QUERY_CREATE_VIEW                         = "CREATE VIEW {0} AS {1}";
    public static final String STR_QUERY_DROP_VIEW                           = "DROP VIEW {0}";
    public static final String STR_QUERY_ALTER_VIEW                          = "ALTER VIEW {0} AS {4}";

    public static final String STR_QUERY_CREATE_TABLE                        = "CREATE TABLE {0} ({1})";
    public static final String STR_QUERY_DROP_TABLE                          = "DROP TABLE {0}";

    public static final String STR_QUERY_ALTER_TABLE_ADD_COLUMN              = "ALTER TABLE {0} ADD COLUMN {1}";
    public static final String STR_QUERY_ALTER_TABLE_ADD_PRIMARY_KEY         = "ALTER TABLE {0} ADD PRIMARY KEY ({2})";
    public static final String STR_QUERY_ALTER_TABLE_ADD_FOREIGN_KEY         = "ALTER TABLE {0} ADD FOREIGN KEY ({2}) REFERENCES {3} ({4}) {5} {6}";
    public static final String STR_QUERY_ALTER_TABLE_ADD_INDEX               = "CREATE {0} INDEX {1} ON {2} ({3})";
    public static final String STR_QUERY_ALTER_TABLE_DROP_INDEX              = "DROP INDEX {1}";
    public static final String STR_QUERY_ALTER_TABLE_DROP_PRIMARY_KEY        = "ALTER TABLE {0} DROP PRIMARY KEY";
    public static final String STR_QUERY_ALTER_TABLE_DROP_CONSTRAINT         = "ALTER TABLE {0} DROP CONSTRAINT {1}";
    public static final String STR_QUERY_ALTER_TABLE_DROP_COLUMN             = "ALTER TABLE {0} DROP COLUMN {1}";
    public static final String STR_QUERY_ALTER_TABLE_ALTER_COLUMN            = "ALTER TABLE {0} ALTER COLUMN {1} {3} {4} {5} {6}";
    public static final String STR_QUERY_ALTER_TABLE_RENAME_COLUMN           = "ALTER TABLE {0} ALTER COLUMN {1} RENAME TO {2}";

    public static final String STR_QUERY_ALTER_COLUMN_SET_DEFAULT            = "ALTER TABLE {0} ALTER COLUMN {2} SET DEFAULT {4}";
    public static final String STR_QUERY_ALTER_COLUMN_DROP_DEFAULT           = "ALTER TABLE {0} ALTER COLUMN {2} DROP DEFAULT";
    public static final String STR_QUERY_ALTER_COLUMN_SET_NOT_NULL           = "ALTER TABLE {0} ALTER COLUMN {2} SET NOT NULL";
    public static final String STR_QUERY_ALTER_COLUMN_DROP_NOT_NULL          = "ALTER TABLE {0} ALTER COLUMN {2} DROP NOT NULL";
    public static final String STR_QUERY_ALTER_COLUMN_DROP_IDENTITY          = "ALTER TABLE {0} ALTER COLUMN {2} DROP IDENTITY";

    public static final String STR_QUERY_ADD_TABLE_COMMENT                   = "COMMENT ON TABLE {0} IS {1}";
    public static final String STR_QUERY_ADD_COLUMN_COMMENT                  = "COMMENT ON COLUMN {0} IS {1}";

    public static final String STR_QUERY_CREATE_USER                         = "CREATE USER {0} PASSWORD {1}";
    public static final String STR_QUERY_ALTER_USER                          = "ALTER USER {0} SET PASSWORD {1}";
    public static final String STR_QUERY_DROP_USER                           = "DROP USER {0}";

    public static final String STR_QUERY_CREATE_ROLE                         = "CREATE ROLE {0}";
    public static final String STR_QUERY_DROP_ROLE                           = "DROP ROLE {0}";
    public static final String STR_QUERY_GRANT_ROLE                          = "GRANT {0} TO {1}";
    public static final String STR_QUERY_REVOKE_ROLE                         = "REVOKE {0} FROM {1}";

    public static final String STR_QUERY_GRANT_PRIVILEGE                     = "GRANT {0} ON {1} TO {2}";
    public static final String STR_QUERY_REVOKE_PRIVILEGE                    = "REVOKE {0} ON {1} FROM {2}";

}
