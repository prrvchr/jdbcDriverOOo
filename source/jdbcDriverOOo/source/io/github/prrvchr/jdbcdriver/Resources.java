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
package io.github.prrvchr.jdbcdriver;

public class Resources {
    public static final int STR_COMMON_BASE                               = 1200;
    // = resource ids for log messages
    public static final int STR_LOG_MESSAGE_BASE                          = 10000;
    public static final int STR_JDBC_LOG_MESSAGE_BASE                     = STR_LOG_MESSAGE_BASE + 1000;
    public static final int STR_JDBC_LOG_MESSAGE_TABLE_VIEW_OFFSET        = 200;

    public static final int STR_STRING_LENGTH_EXCEEDED                         = (STR_COMMON_BASE +  1);
    public static final int STR_CANNOT_CONVERT_STRING                          = (STR_COMMON_BASE +  2);
    public static final int STR_URI_SYNTAX_ERROR                               = (STR_COMMON_BASE +  3);
    public static final int STR_COULD_NOT_LOAD_FILE                            = (STR_COMMON_BASE +  4);
    public static final int STR_QUERY_TOO_COMPLEX                              = (STR_COMMON_BASE +  5);
    public static final int STR_OPERATOR_TOO_COMPLEX                           = (STR_COMMON_BASE +  6);
    public static final int STR_QUERY_INVALID_LIKE_COLUMN                      = (STR_COMMON_BASE +  7);
    public static final int STR_QUERY_INVALID_LIKE_STRING                      = (STR_COMMON_BASE +  8);
    public static final int STR_QUERY_NOT_LIKE_TOO_COMPLEX                     = (STR_COMMON_BASE +  9);
    public static final int STR_QUERY_LIKE_WILDCARD                            = (STR_COMMON_BASE + 10);
    public static final int STR_QUERY_LIKE_WILDCARD_MANY                       = (STR_COMMON_BASE + 11);
    public static final int STR_INVALID_COLUMNNAME                             = (STR_COMMON_BASE + 12);
    public static final int STR_NO_CLASSNAME                                   = (STR_COMMON_BASE + 13);
    public static final int STR_NO_CLASSNAME_PATH                              = (STR_COMMON_BASE + 14);
    public static final int STR_UNKNOWN_PARA_TYPE                              = (STR_COMMON_BASE + 15);
    public static final int STR_INVALID_COLUMN_SELECTION                       = (STR_COMMON_BASE + 16);
    public static final int STR_PARA_ONLY_PREPARED                             = (STR_COMMON_BASE + 17);
    public static final int STR_COLUMN_NOT_UPDATEABLE                          = (STR_COMMON_BASE + 18);
    public static final int STR_ROW_ALREADY_DELETED                            = (STR_COMMON_BASE + 19);
    public static final int STR_UNKNOWN_COLUMN_TYPE                            = (STR_COMMON_BASE + 20);
    public static final int STR_FORMULA_WRONG                                  = (STR_COMMON_BASE + 21);
    public static final int STR_NO_JAVA                                        = (STR_COMMON_BASE + 22);
    public static final int STR_NO_RESULTSET                                   = (STR_COMMON_BASE + 23);
    public static final int STR_NO_ROWCOUNT                                    = (STR_COMMON_BASE + 24);
    public static final int STR_ERRORMSG_SEQUENCE                              = (STR_COMMON_BASE + 25);
    public static final int STR_INVALID_INDEX                                  = (STR_COMMON_BASE + 26);
    public static final int STR_UNSUPPORTED_FUNCTION                           = (STR_COMMON_BASE + 27);
    public static final int STR_UNSUPPORTED_FEATURE                            = (STR_COMMON_BASE + 28);
    public static final int STR_UNKNOWN_COLUMN_NAME                            = (STR_COMMON_BASE + 29);
    public static final int STR_INVALID_PARA_COUNT                             = (STR_COMMON_BASE + 30);
    public static final int STR_PRIVILEGE_NOT_GRANTED                          = (STR_COMMON_BASE + 31);
    public static final int STR_PRIVILEGE_NOT_REVOKED                          = (STR_COMMON_BASE + 32);
    public static final int STR_INVALID_BOOKMARK                               = (STR_COMMON_BASE + 33);
    public static final int STR_NO_ELEMENT_NAME                                = (STR_COMMON_BASE + 34);
    public static final int STR_NO_INPUTSTREAM                                 = (STR_COMMON_BASE + 35);
    public static final int STR_INPUTSTREAM_WRONG_LEN                          = (STR_COMMON_BASE + 36);
    public static final int STR_WRONG_PARAM_INDEX                              = (STR_COMMON_BASE + 37);
    public static final int STR_NO_CONNECTION_GIVEN                            = (STR_COMMON_BASE + 38);

    public static final int STR_LOG_DRIVER_CONNECTING_URL                      = (STR_JDBC_LOG_MESSAGE_BASE +    1);
    public static final int STR_LOG_DRIVER_SUCCESS                             = (STR_JDBC_LOG_MESSAGE_BASE +    2);

    public static final int STR_LOG_NATIVE_SQL                                 = (STR_JDBC_LOG_MESSAGE_BASE +    9);
    public static final int STR_LOG_LOADING_DRIVER                             = (STR_JDBC_LOG_MESSAGE_BASE +   10);
    public static final int STR_LOG_NO_DRIVER_CLASS                            = (STR_JDBC_LOG_MESSAGE_BASE +   11);
    public static final int STR_LOG_CONN_SUCCESS                               = (STR_JDBC_LOG_MESSAGE_BASE +   12);
    public static final int STR_LOG_NO_SYSTEM_CONNECTION                       = (STR_JDBC_LOG_MESSAGE_BASE +   13);
    public static final int STR_LOG_GOT_JDBC_CONNECTION                        = (STR_JDBC_LOG_MESSAGE_BASE +   14);
    public static final int STR_LOG_SHUTDOWN_CONNECTION                        = (STR_JDBC_LOG_MESSAGE_BASE +   15);
    public static final int STR_LOG_GENERATED_VALUES                           = (STR_JDBC_LOG_MESSAGE_BASE +   16);
    public static final int STR_LOG_GENERATED_VALUES_FALLBACK                  = (STR_JDBC_LOG_MESSAGE_BASE +   17);
    public static final int STR_LOG_EXECUTE_STATEMENT                          = (STR_JDBC_LOG_MESSAGE_BASE +   18);
    public static final int STR_LOG_EXECUTE_QUERY                              = (STR_JDBC_LOG_MESSAGE_BASE +   19);

    public static final int STR_LOG_EXECUTE_UPDATE                             = (STR_JDBC_LOG_MESSAGE_BASE +   21);
    public static final int STR_LOG_UPDATE_COUNT                               = (STR_JDBC_LOG_MESSAGE_BASE +   22);

    public static final int STR_LOG_FETCH_DIRECTION                            = (STR_JDBC_LOG_MESSAGE_BASE +   25);
    public static final int STR_LOG_FETCH_SIZE                                 = (STR_JDBC_LOG_MESSAGE_BASE +   26);
    public static final int STR_LOG_SET_ESCAPE_PROCESSING                      = (STR_JDBC_LOG_MESSAGE_BASE +   27);
    public static final int STR_LOG_EXECUTING_PREPARED                         = (STR_JDBC_LOG_MESSAGE_BASE +   28);
    public static final int STR_LOG_EXECUTING_PREPARED_UPDATE                  = (STR_JDBC_LOG_MESSAGE_BASE +   29);
    public static final int STR_LOG_EXECUTING_PREPARED_QUERY                   = (STR_JDBC_LOG_MESSAGE_BASE +   30);
    public static final int STR_LOG_STRING_PARAMETER                           = (STR_JDBC_LOG_MESSAGE_BASE +   31);
    public static final int STR_LOG_BOOLEAN_PARAMETER                          = (STR_JDBC_LOG_MESSAGE_BASE +   32);
    public static final int STR_LOG_BYTE_PARAMETER                             = (STR_JDBC_LOG_MESSAGE_BASE +   33);
    public static final int STR_LOG_DATE_PARAMETER                             = (STR_JDBC_LOG_MESSAGE_BASE +   34);
    public static final int STR_LOG_TIME_PARAMETER                             = (STR_JDBC_LOG_MESSAGE_BASE +   35);
    public static final int STR_LOG_TIMESTAMP_PARAMETER                        = (STR_JDBC_LOG_MESSAGE_BASE +   36);
    public static final int STR_LOG_DOUBLE_PARAMETER                           = (STR_JDBC_LOG_MESSAGE_BASE +   37);
    public static final int STR_LOG_FLOAT_PARAMETER                            = (STR_JDBC_LOG_MESSAGE_BASE +   38);
    public static final int STR_LOG_INT_PARAMETER                              = (STR_JDBC_LOG_MESSAGE_BASE +   39);
    public static final int STR_LOG_LONG_PARAMETER                             = (STR_JDBC_LOG_MESSAGE_BASE +   40);
    public static final int STR_LOG_NULL_PARAMETER                             = (STR_JDBC_LOG_MESSAGE_BASE +   41);
    public static final int STR_LOG_OBJECT_NULL_PARAMETER                      = (STR_JDBC_LOG_MESSAGE_BASE +   42);
    public static final int STR_LOG_SHORT_PARAMETER                            = (STR_JDBC_LOG_MESSAGE_BASE +   43);
    public static final int STR_LOG_BYTES_PARAMETER                            = (STR_JDBC_LOG_MESSAGE_BASE +   44);
    public static final int STR_LOG_CHARSTREAM_PARAMETER                       = (STR_JDBC_LOG_MESSAGE_BASE +   45);
    public static final int STR_LOG_BINARYSTREAM_PARAMETER                     = (STR_JDBC_LOG_MESSAGE_BASE +   46);
    public static final int STR_LOG_CLEAR_PARAMETERS                           = (STR_JDBC_LOG_MESSAGE_BASE +   47);
    public static final int STR_LOG_META_DATA_METHOD                           = (STR_JDBC_LOG_MESSAGE_BASE +   48);
    public static final int STR_LOG_META_DATA_METHOD_ARG1                      = (STR_JDBC_LOG_MESSAGE_BASE +   49);
    public static final int STR_LOG_META_DATA_METHOD_ARG2                      = (STR_JDBC_LOG_MESSAGE_BASE +   50);
    public static final int STR_LOG_META_DATA_METHOD_ARG3                      = (STR_JDBC_LOG_MESSAGE_BASE +   51);
    public static final int STR_LOG_META_DATA_METHOD_ARG4                      = (STR_JDBC_LOG_MESSAGE_BASE +   52);
    public static final int STR_LOG_META_DATA_RESULT                           = (STR_JDBC_LOG_MESSAGE_BASE +   53);
    public static final int STR_LOG_META_DATA_SUCCESS                          = (STR_JDBC_LOG_MESSAGE_BASE +   54);
    public static final int STR_LOG_THROWING_EXCEPTION                         = (STR_JDBC_LOG_MESSAGE_BASE +   55);
    public static final int STR_LOG_SETTING_SYSTEM_PROPERTY                    = (STR_JDBC_LOG_MESSAGE_BASE +   56);

    // io.github.prrvchr.uno.sdbc.ConnectionMain:
    public static final int STR_LOG_CREATE_DATABASE_METADATA                   = (STR_JDBC_LOG_MESSAGE_BASE +  200);
    public static final int STR_LOG_CREATED_DATABASE_METADATA_ID               = (STR_JDBC_LOG_MESSAGE_BASE +  201);

    public static final int STR_LOG_DATABASE_METADATA_DRIVER_VERSION           = (STR_JDBC_LOG_MESSAGE_BASE +  250);

    // io.github.prrvchr.uno.sdbc.StatementMain:
    public static final int STR_LOG_CREATE_STATEMENT                           = (STR_JDBC_LOG_MESSAGE_BASE +  300);
    public static final int STR_LOG_CREATED_STATEMENT_ID                       = (STR_JDBC_LOG_MESSAGE_BASE +  301);
    public static final int STR_LOG_PREPARE_STATEMENT                          = (STR_JDBC_LOG_MESSAGE_BASE +  302);
    public static final int STR_LOG_PREPARED_STATEMENT_ID                      = (STR_JDBC_LOG_MESSAGE_BASE +  303);
    public static final int STR_LOG_PREPARE_CALL                               = (STR_JDBC_LOG_MESSAGE_BASE +  304);
    public static final int STR_LOG_PREPARED_CALL_ID                           = (STR_JDBC_LOG_MESSAGE_BASE +  305);
    public static final int STR_LOG_STATEMENT_CLOSING                          = (STR_JDBC_LOG_MESSAGE_BASE +  306);

    public static final int STR_LOG_STATEMENT_RESULTSET_CONCURRENCY            = (STR_JDBC_LOG_MESSAGE_BASE +  310);
    public static final int STR_LOG_STATEMENT_SET_RESULTSET_CONCURRENCY        = (STR_JDBC_LOG_MESSAGE_BASE +  311);
    public static final int STR_LOG_STATEMENT_SET_RESULTSET_CONCURRENCY_ERROR  = (STR_JDBC_LOG_MESSAGE_BASE +  312);

    public static final int STR_LOG_STATEMENT_RESULTSET_TYPE                   = (STR_JDBC_LOG_MESSAGE_BASE +  315);
    public static final int STR_LOG_STATEMENT_SET_RESULTSET_TYPE               = (STR_JDBC_LOG_MESSAGE_BASE +  316);
    public static final int STR_LOG_STATEMENT_SET_RESULTSET_TYPE_ERROR         = (STR_JDBC_LOG_MESSAGE_BASE +  317);

    public static final int STR_LOG_STATEMENT_EXECUTE                          = (STR_JDBC_LOG_MESSAGE_BASE +  320);
    public static final int STR_LOG_STATEMENT_EXECUTE_QUERY                    = (STR_JDBC_LOG_MESSAGE_BASE +  321);
    public static final int STR_LOG_STATEMENT_EXECUTE_UPDATE                   = (STR_JDBC_LOG_MESSAGE_BASE +  322);

    public static final int STR_LOG_STATEMENT_GENERATED_VALUES_TABLE           = (STR_JDBC_LOG_MESSAGE_BASE +  325);
    public static final int STR_LOG_STATEMENT_GENERATED_VALUES_QUERY           = (STR_JDBC_LOG_MESSAGE_BASE +  326);
    public static final int STR_LOG_STATEMENT_GENERATED_VALUES_RESULT          = (STR_JDBC_LOG_MESSAGE_BASE +  327);
    public static final int STR_LOG_STATEMENT_GENERATED_VALUES_ERROR           = (STR_JDBC_LOG_MESSAGE_BASE +  328);

    // io.github.prrvchr.uno.sdbc.PreparedStatementSuper:
    public static final int STR_LOG_STATEMENT_USEBOOKMARKS                     = (STR_JDBC_LOG_MESSAGE_BASE +  330);
    public static final int STR_LOG_STATEMENT_SET_USEBOOKMARKS                 = (STR_JDBC_LOG_MESSAGE_BASE +  331);

    // io.github.prrvchr.uno.sdbc.ResultSetBase:
    public static final int STR_LOG_CREATE_RESULTSET                           = (STR_JDBC_LOG_MESSAGE_BASE +  400);
    public static final int STR_LOG_CREATED_RESULTSET_ID                       = (STR_JDBC_LOG_MESSAGE_BASE +  401);
    public static final int STR_LOG_CREATE_METADATA_RESULTSET                  = (STR_JDBC_LOG_MESSAGE_BASE +  402);
    public static final int STR_LOG_CREATED_METADATA_RESULTSET_ID              = (STR_JDBC_LOG_MESSAGE_BASE +  403);
    public static final int STR_LOG_RESULTSET_CLOSING                          = (STR_JDBC_LOG_MESSAGE_BASE +  404);

    // com.sun.star.sdbc.XResultSet:
    public static final int STR_LOG_RESULTSET_CURSORNAME                       = (STR_JDBC_LOG_MESSAGE_BASE +  410);
    public static final int STR_LOG_RESULTSET_FETCH_DIRECTION                  = (STR_JDBC_LOG_MESSAGE_BASE +  411);
    public static final int STR_LOG_RESULTSET_SET_FETCH_DIRECTION              = (STR_JDBC_LOG_MESSAGE_BASE +  412);
    public static final int STR_LOG_RESULTSET_FETCH_SIZE                       = (STR_JDBC_LOG_MESSAGE_BASE +  413);
    public static final int STR_LOG_RESULTSET_SET_FETCH_SIZE                   = (STR_JDBC_LOG_MESSAGE_BASE +  414);
    public static final int STR_LOG_RESULTSET_CONCURRENCY                      = (STR_JDBC_LOG_MESSAGE_BASE +  415);
    public static final int STR_LOG_RESULTSET_TYPE                             = (STR_JDBC_LOG_MESSAGE_BASE +  416);
    public static final int STR_LOG_RESULTSET_ABSOLUTE                         = (STR_JDBC_LOG_MESSAGE_BASE +  417);
    public static final int STR_LOG_RESULTSET_NEXT                             = (STR_JDBC_LOG_MESSAGE_BASE +  418);
    public static final int STR_LOG_RESULTSET_RELATIVE                         = (STR_JDBC_LOG_MESSAGE_BASE +  419);
    public static final int STR_LOG_RESULTSET_ROW_DELETED                      = (STR_JDBC_LOG_MESSAGE_BASE +  420);
    public static final int STR_LOG_RESULTSET_ROW_INSERTED                     = (STR_JDBC_LOG_MESSAGE_BASE +  421);
    public static final int STR_LOG_RESULTSET_ROW_UPDATED                      = (STR_JDBC_LOG_MESSAGE_BASE +  422);
    public static final int STR_LOG_RESULTSET_GET_PARAMETER                    = (STR_JDBC_LOG_MESSAGE_BASE +  423);
    public static final int STR_LOG_RESULTSET_UPDATE_PARAMETER                 = (STR_JDBC_LOG_MESSAGE_BASE +  424);

    // com.sun.star.sdbc.XResultSetUpdate:
    public static final int STR_LOG_RESULTSET_INSERT_ROW                       = (STR_JDBC_LOG_MESSAGE_BASE +  430);
    public static final int STR_LOG_RESULTSET_UPDATE_ROW                       = (STR_JDBC_LOG_MESSAGE_BASE +  431);
    public static final int STR_LOG_RESULTSET_DELETE_ROW                       = (STR_JDBC_LOG_MESSAGE_BASE +  432);
    public static final int STR_LOG_RESULTSET_CANCEL_ROW_UPDATES               = (STR_JDBC_LOG_MESSAGE_BASE +  433);
    public static final int STR_LOG_RESULTSET_MOVE_TO_CURRENTROW               = (STR_JDBC_LOG_MESSAGE_BASE +  434);
    public static final int STR_LOG_RESULTSET_MOVE_TO_INSERTROW                = (STR_JDBC_LOG_MESSAGE_BASE +  435);

    // com.sun.star.sdbcx.XRowLocate:
    public static final int STR_LOG_RESULTSET_ISBOOKMARKABLE                   = (STR_JDBC_LOG_MESSAGE_BASE +  450);
    public static final int STR_LOG_RESULTSET_CANUPDATEINSERTEDROWS            = (STR_JDBC_LOG_MESSAGE_BASE +  451);
    public static final int STR_LOG_RESULTSET_COMPARE_BOOKMARKS                = (STR_JDBC_LOG_MESSAGE_BASE +  452);
    public static final int STR_LOG_RESULTSET_GET_BOOKMARK                     = (STR_JDBC_LOG_MESSAGE_BASE +  453);
    public static final int STR_LOG_RESULTSET_MOVE_TO_BOOKMARK                 = (STR_JDBC_LOG_MESSAGE_BASE +  454);
    public static final int STR_LOG_RESULTSET_MOVE_RELATIVE_TO_BOOKMARK        = (STR_JDBC_LOG_MESSAGE_BASE +  455);
    public static final int STR_LOG_RESULTSET_MOVE_TO_BOOKMARK_ON_INSERT       = (STR_JDBC_LOG_MESSAGE_BASE +  456);

    // io.github.prrvchr.uno.sdbcx.TableContainerBase:
    public static final int STR_LOG_CREATE_TABLES                              = (STR_JDBC_LOG_MESSAGE_BASE +  500);
    public static final int STR_LOG_CREATED_TABLES_ID                          = (STR_JDBC_LOG_MESSAGE_BASE +  501);
    public static final int STR_LOG_TABLES_DISPOSING                           = (STR_JDBC_LOG_MESSAGE_BASE +  502);

    public static final int STR_LOG_TABLES_CREATE_TABLE_QUERY                  = (STR_JDBC_LOG_MESSAGE_BASE +  510);
    public static final int STR_LOG_TABLES_CREATE_TABLE_QUERY_ERROR            = (STR_JDBC_LOG_MESSAGE_BASE +  511);
    public static final int STR_LOG_TABLES_REMOVE_TABLE_QUERY                  = (STR_JDBC_LOG_MESSAGE_BASE +  512);
    public static final int STR_LOG_TABLES_REMOVE_TABLE_QUERY_ERROR            = (STR_JDBC_LOG_MESSAGE_BASE +  513);

    // io.github.prrvchr.uno.sdbcx.TableSuper:
    public static final int STR_LOG_CREATE_TABLE                               = (STR_JDBC_LOG_MESSAGE_BASE +  600);
    public static final int STR_LOG_CREATED_TABLE_ID                           = (STR_JDBC_LOG_MESSAGE_BASE +  601);
    public static final int STR_LOG_TABLE_DISPOSING                            = (STR_JDBC_LOG_MESSAGE_BASE +  602);

    public static final int STR_LOG_TABLE_RENAME_QUERY                         = (STR_JDBC_LOG_MESSAGE_BASE +  605);
    public static final int STR_LOG_TABLE_RENAME_QUERY_ERROR                   = (STR_JDBC_LOG_MESSAGE_BASE +  606);
    public static final int STR_LOG_TABLE_RENAME_UNSUPPORTED_FEATURE_ERROR     = (STR_JDBC_LOG_MESSAGE_BASE +  607);
    public static final int STR_LOG_TABLE_RENAME_TABLE_NOT_FOUND_ERROR         = (STR_JDBC_LOG_MESSAGE_BASE +  608);
    public static final int STR_LOG_TABLE_RENAME_OPERATION_CANCELLED_ERROR     = (STR_JDBC_LOG_MESSAGE_BASE +  609);
    public static final int STR_LOG_TABLE_RENAME_DUPLICATE_TABLE_NAME_ERROR    = (STR_JDBC_LOG_MESSAGE_BASE +  610);
    public static final int STR_LOG_TABLE_RENAME_FEATURE_NOT_IMPLEMENTED       = (STR_JDBC_LOG_MESSAGE_BASE +  611);
    public static final int STR_LOG_TABLE_RENAME_UNSUPPORTED_FUNCTION_ERROR    = (STR_JDBC_LOG_MESSAGE_BASE +  612);

    public static final int STR_LOG_TABLE_ALTER_COLUMN_QUERY                   = (STR_JDBC_LOG_MESSAGE_BASE +  615);
    public static final int STR_LOG_TABLE_ALTER_COLUMN_QUERY_ERROR             = (STR_JDBC_LOG_MESSAGE_BASE +  616);
    public static final int STR_LOG_ALTER_IDENTITY_UNSUPPORTED_FEATURE_ERROR   = (STR_JDBC_LOG_MESSAGE_BASE +  617);

    public static final int STR_LOG_COLUMN_ALTER_QUERY                         = (STR_JDBC_LOG_MESSAGE_BASE +  620);
    public static final int STR_LOG_COLUMN_ALTER_QUERY_ERROR                   = (STR_JDBC_LOG_MESSAGE_BASE +  621);
    public static final int STR_LOG_COLUMN_ALTER_UNSUPPORTED_FEATURE_ERROR     = (STR_JDBC_LOG_MESSAGE_BASE +  622);
    public static final int STR_LOG_COLUMN_REMOVE_QUERY                        = (STR_JDBC_LOG_MESSAGE_BASE +  623);
    public static final int STR_LOG_COLUMN_REMOVE_QUERY_ERROR                  = (STR_JDBC_LOG_MESSAGE_BASE +  624);

    public static final int STR_LOG_CREATE_KEYS                                = (STR_JDBC_LOG_MESSAGE_BASE +  630);
    public static final int STR_LOG_CREATED_KEYS_ID                            = (STR_JDBC_LOG_MESSAGE_BASE +  631);
    public static final int STR_LOG_KEYS_DISPOSING                             = (STR_JDBC_LOG_MESSAGE_BASE +  632);

    public static final int STR_LOG_KEYS_CREATE_PKEY_QUERY                     = (STR_JDBC_LOG_MESSAGE_BASE +  640);
    public static final int STR_LOG_KEYS_CREATE_PKEY_QUERY_ERROR               = (STR_JDBC_LOG_MESSAGE_BASE +  641);
    public static final int STR_LOG_KEYS_REMOVE_PKEY_QUERY                     = (STR_JDBC_LOG_MESSAGE_BASE +  642);
    public static final int STR_LOG_KEYS_REMOVE_PKEY_QUERY_ERROR               = (STR_JDBC_LOG_MESSAGE_BASE +  643);

    public static final int STR_LOG_PKEY_ADD_UNSUPPORTED_FEATURE_ERROR         = (STR_JDBC_LOG_MESSAGE_BASE +  645);
    public static final int STR_LOG_PKEY_REMOVE_UNSUPPORTED_FEATURE_ERROR      = (STR_JDBC_LOG_MESSAGE_BASE +  646);

    public static final int STR_LOG_KEYS_CREATE_FKEY_QUERY                     = (STR_JDBC_LOG_MESSAGE_BASE +  650);
    public static final int STR_LOG_KEYS_CREATE_FKEY_QUERY_ERROR               = (STR_JDBC_LOG_MESSAGE_BASE +  651);
    public static final int STR_LOG_KEYS_REMOVE_FKEY_QUERY                     = (STR_JDBC_LOG_MESSAGE_BASE +  652);
    public static final int STR_LOG_KEYS_REMOVE_FKEY_QUERY_ERROR               = (STR_JDBC_LOG_MESSAGE_BASE +  653);

    public static final int STR_LOG_FKEY_ADD_INVALID_COLUMN_TYPE_ERROR         = (STR_JDBC_LOG_MESSAGE_BASE +  655);
    public static final int STR_LOG_FKEY_ADD_UNSUPPORTED_FEATURE_ERROR         = (STR_JDBC_LOG_MESSAGE_BASE +  656);
    public static final int STR_LOG_FKEY_ADD_UNSPECIFIED_ERROR                 = (STR_JDBC_LOG_MESSAGE_BASE +  657);
    public static final int STR_LOG_FKEY_REMOVE_UNSUPPORTED_FEATURE_ERROR      = (STR_JDBC_LOG_MESSAGE_BASE +  658);

    public static final int STR_LOG_CREATE_INDEXES                             = (STR_JDBC_LOG_MESSAGE_BASE +  660);
    public static final int STR_LOG_CREATED_INDEXES_ID                         = (STR_JDBC_LOG_MESSAGE_BASE +  661);
    public static final int STR_LOG_INDEXES_DISPOSING                          = (STR_JDBC_LOG_MESSAGE_BASE +  662);

    public static final int STR_LOG_INDEXES_CREATE_INDEX_QUERY                 = (STR_JDBC_LOG_MESSAGE_BASE +  670);
    public static final int STR_LOG_INDEXES_CREATE_INDEX_QUERY_ERROR           = (STR_JDBC_LOG_MESSAGE_BASE +  671);
    public static final int STR_LOG_INDEXES_REMOVE_INDEX_QUERY                 = (STR_JDBC_LOG_MESSAGE_BASE +  672);
    public static final int STR_LOG_INDEXES_REMOVE_INDEX_QUERY_ERROR           = (STR_JDBC_LOG_MESSAGE_BASE +  673);

    public static final int STR_LOG_CREATE_VIEWS                               = (STR_JDBC_LOG_MESSAGE_BASE +  700);
    public static final int STR_LOG_CREATED_VIEWS_ID                           = (STR_JDBC_LOG_MESSAGE_BASE +  701);
    public static final int STR_LOG_VIEWS_DISPOSING                            = (STR_JDBC_LOG_MESSAGE_BASE +  702);

    public static final int STR_LOG_VIEWS_CREATE_VIEW_QUERY                    = (STR_JDBC_LOG_MESSAGE_BASE +  710);
    public static final int STR_LOG_VIEWS_CREATE_VIEW_QUERY_ERROR              = (STR_JDBC_LOG_MESSAGE_BASE +  711);
    public static final int STR_LOG_VIEWS_REMOVE_VIEW_QUERY                    = (STR_JDBC_LOG_MESSAGE_BASE +  712);
    public static final int STR_LOG_VIEWS_REMOVE_VIEW_QUERY_ERROR              = (STR_JDBC_LOG_MESSAGE_BASE +  713);

    public static final int STR_LOG_CREATE_VIEW                                = (STR_JDBC_LOG_MESSAGE_BASE +  800);
    public static final int STR_LOG_CREATED_VIEW_ID                            = (STR_JDBC_LOG_MESSAGE_BASE +  801);
    public static final int STR_LOG_VIEW_DISPOSING                             = (STR_JDBC_LOG_MESSAGE_BASE +  802);

    public static final int STR_LOG_VIEW_RENAME_QUERY                          = (STR_JDBC_LOG_MESSAGE_BASE +  805);
    public static final int STR_LOG_VIEW_RENAME_QUERY_ERROR                    = (STR_JDBC_LOG_MESSAGE_BASE +  806);
    public static final int STR_LOG_VIEW_RENAME_UNSUPPORTED_FEATURE_ERROR      = (STR_JDBC_LOG_MESSAGE_BASE +  807);
    public static final int STR_LOG_VIEW_RENAME_VIEW_NOT_FOUND_ERROR           = (STR_JDBC_LOG_MESSAGE_BASE +  808);
    public static final int STR_LOG_VIEW_RENAME_OPERATION_CANCELLED_ERROR      = (STR_JDBC_LOG_MESSAGE_BASE +  809);
    public static final int STR_LOG_VIEW_RENAME_DUPLICATE_VIEW_NAME_ERROR      = (STR_JDBC_LOG_MESSAGE_BASE +  810);
    public static final int STR_LOG_VIEW_RENAME_FEATURE_NOT_IMPLEMENTED        = (STR_JDBC_LOG_MESSAGE_BASE +  811);
    public static final int STR_LOG_VIEW_RENAME_UNSUPPORTED_FUNCTION_ERROR     = (STR_JDBC_LOG_MESSAGE_BASE +  812);
    public static final int STR_LOG_VIEW_RENAME_UNSPECIFIED_ERROR              = (STR_JDBC_LOG_MESSAGE_BASE +  812);

    public static final int STR_LOG_VIEW_ALTER_QUERY                           = (STR_JDBC_LOG_MESSAGE_BASE +  815);
    public static final int STR_LOG_VIEW_ALTER_QUERY_ERROR                     = (STR_JDBC_LOG_MESSAGE_BASE +  816);

    public static final int STR_LOG_CREATE_USERS                               = (STR_JDBC_LOG_MESSAGE_BASE + 1000);
    public static final int STR_LOG_CREATED_USERS_ID                           = (STR_JDBC_LOG_MESSAGE_BASE + 1001);
    public static final int STR_LOG_USERS_DISPOSING                            = (STR_JDBC_LOG_MESSAGE_BASE + 1002);

    public static final int STR_LOG_USERS_CREATE_USER_QUERY                    = (STR_JDBC_LOG_MESSAGE_BASE + 1010);
    public static final int STR_LOG_USERS_CREATE_USER_QUERY_ERROR              = (STR_JDBC_LOG_MESSAGE_BASE + 1011);
    public static final int STR_LOG_USERS_REMOVE_USER_QUERY                    = (STR_JDBC_LOG_MESSAGE_BASE + 1012);
    public static final int STR_LOG_USERS_REMOVE_USER_QUERY_ERROR              = (STR_JDBC_LOG_MESSAGE_BASE + 1013);

    public static final int STR_LOG_CREATE_GROUPS                              = (STR_JDBC_LOG_MESSAGE_BASE + 1100);
    public static final int STR_LOG_CREATED_GROUPS_ID                          = (STR_JDBC_LOG_MESSAGE_BASE + 1101);
    public static final int STR_LOG_GROUPS_DISPOSING                           = (STR_JDBC_LOG_MESSAGE_BASE + 1102);

    public static final int STR_LOG_GROUPS_CREATE_GROUP_QUERY                  = (STR_JDBC_LOG_MESSAGE_BASE + 1110);
    public static final int STR_LOG_GROUPS_CREATE_GROUP_QUERY_ERROR            = (STR_JDBC_LOG_MESSAGE_BASE + 1111);
    public static final int STR_LOG_GROUPS_REMOVE_GROUP_QUERY                  = (STR_JDBC_LOG_MESSAGE_BASE + 1112);
    public static final int STR_LOG_GROUPS_REMOVE_GROUP_QUERY_ERROR            = (STR_JDBC_LOG_MESSAGE_BASE + 1113);

    public static final int STR_LOG_CREATE_USER                                = (STR_JDBC_LOG_MESSAGE_BASE + 1200);
    public static final int STR_LOG_CREATED_USER_ID                            = (STR_JDBC_LOG_MESSAGE_BASE + 1201);
    public static final int STR_LOG_USER_DISPOSING                             = (STR_JDBC_LOG_MESSAGE_BASE + 1202);

    public static final int STR_LOG_USER_GRANT_PRIVILEGE_QUERY                 = (STR_JDBC_LOG_MESSAGE_BASE + 1210);
    public static final int STR_LOG_USER_GRANT_PRIVILEGE_QUERY_ERROR           = (STR_JDBC_LOG_MESSAGE_BASE + 1211);

    public static final int STR_LOG_USER_REVOKE_PRIVILEGE_QUERY                = (STR_JDBC_LOG_MESSAGE_BASE + 1220);
    public static final int STR_LOG_USER_REVOKE_PRIVILEGE_QUERY_ERROR          = (STR_JDBC_LOG_MESSAGE_BASE + 1221);

    public static final int STR_LOG_CREATE_GROUP                               = (STR_JDBC_LOG_MESSAGE_BASE + 1300);
    public static final int STR_LOG_CREATED_GROUP_ID                           = (STR_JDBC_LOG_MESSAGE_BASE + 1301);
    public static final int STR_LOG_GROUP_DISPOSING                            = (STR_JDBC_LOG_MESSAGE_BASE + 1302);

    public static final int STR_LOG_GROUP_GRANT_PRIVILEGE_QUERY                = (STR_JDBC_LOG_MESSAGE_BASE + 1310);
    public static final int STR_LOG_GROUP_GRANT_PRIVILEGE_QUERY_ERROR          = (STR_JDBC_LOG_MESSAGE_BASE + 1311);

    public static final int STR_LOG_GROUP_REVOKE_PRIVILEGE_QUERY               = (STR_JDBC_LOG_MESSAGE_BASE + 1320);
    public static final int STR_LOG_GROUP_REVOKE_PRIVILEGE_QUERY_ERROR         = (STR_JDBC_LOG_MESSAGE_BASE + 1321);

    public static final int STR_LOG_CREATE_USERROLES                           = (STR_JDBC_LOG_MESSAGE_BASE + 1400);
    public static final int STR_LOG_CREATED_USERROLES_ID                       = (STR_JDBC_LOG_MESSAGE_BASE + 1401);
    public static final int STR_LOG_USERROLES_DISPOSING                        = (STR_JDBC_LOG_MESSAGE_BASE + 1402);

    public static final int STR_LOG_USERROLES_GRANT_ROLE_QUERY                 = (STR_JDBC_LOG_MESSAGE_BASE + 1410);
    public static final int STR_LOG_USERROLES_GRANT_ROLE_QUERY_ERROR           = (STR_JDBC_LOG_MESSAGE_BASE + 1411);
    public static final int STR_LOG_USERROLES_REVOKE_ROLE_QUERY                = (STR_JDBC_LOG_MESSAGE_BASE + 1412);
    public static final int STR_LOG_USERROLES_REVOKE_ROLE_QUERY_ERROR          = (STR_JDBC_LOG_MESSAGE_BASE + 1413);

    public static final int STR_LOG_CREATE_GROUPROLES                          = (STR_JDBC_LOG_MESSAGE_BASE + 1500);
    public static final int STR_LOG_CREATED_GROUPROLES_ID                      = (STR_JDBC_LOG_MESSAGE_BASE + 1501);
    public static final int STR_LOG_GROUPROLES_DISPOSING                       = (STR_JDBC_LOG_MESSAGE_BASE + 1502);

    public static final int STR_LOG_GROUPROLES_GRANT_ROLE_QUERY                = (STR_JDBC_LOG_MESSAGE_BASE + 1510);
    public static final int STR_LOG_GROUPROLES_GRANT_ROLE_QUERY_ERROR          = (STR_JDBC_LOG_MESSAGE_BASE + 1511);
    public static final int STR_LOG_GROUPROLES_REVOKE_ROLE_QUERY               = (STR_JDBC_LOG_MESSAGE_BASE + 1512);
    public static final int STR_LOG_GROUPROLES_REVOKE_ROLE_QUERY_ERROR         = (STR_JDBC_LOG_MESSAGE_BASE + 1513);

}
