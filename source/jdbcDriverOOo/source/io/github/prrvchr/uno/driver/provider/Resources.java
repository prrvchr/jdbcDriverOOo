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
/**************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"; you may not use this file except in compliance
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
package io.github.prrvchr.uno.driver.provider;

public class Resources {
    public static final int COMMON_BASE                                                     = 1000;
    // = resource ids for log messages
    public static final int LOGGER_BASE                                                     = 10000;

    public static final int STR_STRING_LENGTH_EXCEEDED                                      = COMMON_BASE +    1;
    public static final int STR_CANNOT_CONVERT_STRING                                       = COMMON_BASE +    2;
    public static final int STR_URI_SYNTAX_ERROR                                            = COMMON_BASE +    3;
    public static final int STR_COULD_NOT_LOAD_FILE                                         = COMMON_BASE +    4;
    public static final int STR_QUERY_TOO_COMPLEX                                           = COMMON_BASE +    5;
    public static final int STR_OPERATOR_TOO_COMPLEX                                        = COMMON_BASE +    6;
    public static final int STR_QUERY_INVALID_LIKE_COLUMN                                   = COMMON_BASE +    7;
    public static final int STR_QUERY_INVALID_LIKE_STRING                                   = COMMON_BASE +    8;
    public static final int STR_QUERY_NOT_LIKE_TOO_COMPLEX                                  = COMMON_BASE +    9;
    public static final int STR_QUERY_LIKE_WILDCARD                                         = COMMON_BASE +   10;
    public static final int STR_QUERY_LIKE_WILDCARD_MANY                                    = COMMON_BASE +   11;
    public static final int STR_INVALID_COLUMNNAME                                          = COMMON_BASE +   12;
    public static final int STR_NO_CLASSNAME                                                = COMMON_BASE +   13;
    public static final int STR_NO_CLASSNAME_PATH                                           = COMMON_BASE +   14;
    public static final int STR_UNKNOWN_PARA_TYPE                                           = COMMON_BASE +   15;
    public static final int STR_INVALID_COLUMN_SELECTION                                    = COMMON_BASE +   16;
    public static final int STR_PARA_ONLY_PREPARED                                          = COMMON_BASE +   17;
    public static final int STR_COLUMN_NOT_UPDATEABLE                                       = COMMON_BASE +   18;
    public static final int STR_ROW_ALREADY_DELETED                                         = COMMON_BASE +   19;
    public static final int STR_UNKNOWN_COLUMN_TYPE                                         = COMMON_BASE +   20;
    public static final int STR_FORMULA_WRONG                                               = COMMON_BASE +   21;
    public static final int STR_NO_JAVA                                                     = COMMON_BASE +   22;
    public static final int STR_NO_RESULTSET                                                = COMMON_BASE +   23;
    public static final int STR_NO_ROWCOUNT                                                 = COMMON_BASE +   24;
    public static final int STR_ERRORMSG_SEQUENCE                                           = COMMON_BASE +   25;
    public static final int STR_INVALID_INDEX                                               = COMMON_BASE +   26;
    public static final int STR_UNSUPPORTED_FUNCTION                                        = COMMON_BASE +   27;
    public static final int STR_UNSUPPORTED_FEATURE                                         = COMMON_BASE +   28;
    public static final int STR_UNKNOWN_COLUMN_NAME                                         = COMMON_BASE +   29;
    public static final int STR_INVALID_PARA_COUNT                                          = COMMON_BASE +   30;
    public static final int STR_PRIVILEGE_NOT_GRANTED                                       = COMMON_BASE +   31;
    public static final int STR_PRIVILEGE_NOT_REVOKED                                       = COMMON_BASE +   32;
    public static final int STR_INVALID_BOOKMARK                                            = COMMON_BASE +   33;
    public static final int STR_NO_ELEMENT_NAME                                             = COMMON_BASE +   34;
    public static final int STR_NO_INPUTSTREAM                                              = COMMON_BASE +   35;
    public static final int STR_INPUTSTREAM_WRONG_LEN                                       = COMMON_BASE +   36;
    public static final int STR_WRONG_PARAM_INDEX                                           = COMMON_BASE +   37;
    public static final int STR_NO_CONNECTION_GIVEN                                         = COMMON_BASE +   38;

    // io.github.prrvchr.uno.sdbc.DriverBase:
    public static final int STR_LOG_DRIVER_CONNECTING_URL                                   = LOGGER_BASE +    1;
    public static final int STR_LOG_DRIVER_SETTING_SYSTEM_PROPERTIES                        = LOGGER_BASE +    2;
    public static final int STR_LOG_DRIVER_CONNECT_WITH_URL                                 = LOGGER_BASE +    3;
    public static final int STR_LOG_DRIVER_ARCHIVE_LOADING                                  = LOGGER_BASE +    4;
    public static final int STR_LOG_DRIVER_SUCCESS                                          = LOGGER_BASE +    5;

    public static final int STR_LOG_DRIVER_UNSUPPORTED_JAVA_VERSION                         = LOGGER_BASE +   10;
    public static final int STR_LOG_DRIVER_CLASS_NOT_FOUND                                  = LOGGER_BASE +   11;
    public static final int STR_LOG_DRIVER_JAVA_CLASS_NOT_SUPPORTED                         = LOGGER_BASE +   12;
    public static final int STR_LOG_DRIVER_CLASS_PATH_NO_ARCHIVE                            = LOGGER_BASE +   13;
    public static final int STR_LOG_DRIVER_CLASS_PATH_EMPTY                                 = LOGGER_BASE +   14;
    public static final int STR_LOG_DRIVER_CLASS_PATH_ERROR                                 = LOGGER_BASE +   15;
    public static final int STR_LOG_DRIVER_UNEXPECTED_LOADING_ERROR                         = LOGGER_BASE +   16;
    public static final int STR_LOG_DRIVER_SETTING_SYSTEM_PROPERTIES_ERROR                  = LOGGER_BASE +   17;
    public static final int STR_LOG_NO_SYSTEM_CONNECTION                                    = LOGGER_BASE +   18;
    public static final int STR_LOG_DRIVER_REGISTRATION_ERROR                               = LOGGER_BASE +   19;
    public static final int STR_LOG_CONFIGURATION_LOADING_ERROR                             = LOGGER_BASE +   20;
    public static final int STR_LOG_DRIVER_JAVA_INSTRUMENTATION_ERROR                       = LOGGER_BASE +   21;

    public static final int STR_LOG_CONNECTION_ESTABLISHED                                  = LOGGER_BASE +  100;
    public static final int STR_LOG_CONNECTION_SHUTDOWN                                     = LOGGER_BASE +  101;

    // io.github.prrvchr.uno.sdbc.ConnectionMain:
    public static final int STR_LOG_CREATE_DATABASE_METADATA                                = LOGGER_BASE +  200;
    public static final int STR_LOG_CREATED_DATABASE_METADATA_ID                            = LOGGER_BASE +  201;
    // io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase:
    public static final int STR_LOG_DATABASE_METADATA_CREATE_RESULTSET                      = LOGGER_BASE +  210;
    public static final int STR_LOG_DATABASE_METADATA_CREATED_RESULTSET_ID                  = LOGGER_BASE +  211;
    public static final int STR_LOG_DATABASE_METADATA_CREATE_RESULTSET_ERROR                = LOGGER_BASE +  212;
    public static final int STR_LOG_DATABASE_METADATA_DRIVER_VERSION                        = LOGGER_BASE +  213;
    public static final int STR_LOG_DATABASE_METADATA_IDENTIFIER_QUOTE                      = LOGGER_BASE +  214;
    public static final int STR_LOG_DATABASE_METADATA_EXTRA_NAME_CHARACTERS                 = LOGGER_BASE +  215;
    public static final int STR_LOG_DATABASE_METADATA_MAX_TABLE_NAME_LENGTH                 = LOGGER_BASE +  216;
    public static final int STR_LOG_DATABASE_METADATA_MAX_COLUMN_NAME_LENGTH                = LOGGER_BASE +  217;
    public static final int STR_LOG_DATABASE_METADATA_SUPPORT_MIXED_CASE_ID                 = LOGGER_BASE +  218;
    public static final int STR_LOG_DATABASE_METADATA_SUPPORT_MIXED_CASE_QUOTED_ID          = LOGGER_BASE +  219;
    public static final int STR_LOG_DATABASE_METADATA_SUPPORT_ALTER_TABLE_WITH_ADD_COLUMN   = LOGGER_BASE +  220;
    public static final int STR_LOG_DATABASE_METADATA_SUPPORT_ALTER_TABLE_WITH_DROP_COLUMN  = LOGGER_BASE +  221;

    public static final int STR_LOG_CONNECTION_EXECUTE_QUERY                                = LOGGER_BASE +  250;

    // io.github.prrvchr.uno.sdbc.StatementMain:
    public static final int STR_LOG_CREATE_STATEMENT                                        = LOGGER_BASE +  300;
    public static final int STR_LOG_CREATED_STATEMENT_ID                                    = LOGGER_BASE +  301;
    public static final int STR_LOG_PREPARE_STATEMENT                                       = LOGGER_BASE +  302;
    public static final int STR_LOG_PREPARED_STATEMENT_ID                                   = LOGGER_BASE +  303;
    public static final int STR_LOG_PREPARE_CALL                                            = LOGGER_BASE +  304;
    public static final int STR_LOG_PREPARED_CALL_ID                                        = LOGGER_BASE +  305;
    public static final int STR_LOG_STATEMENT_CLOSING                                       = LOGGER_BASE +  306;

    public static final int STR_LOG_STATEMENT_RESULTSET_CONCURRENCY                         = LOGGER_BASE +  310;
    public static final int STR_LOG_STATEMENT_SET_RESULTSET_CONCURRENCY                     = LOGGER_BASE +  311;
    public static final int STR_LOG_STATEMENT_SET_RESULTSET_CONCURRENCY_ERROR               = LOGGER_BASE +  312;
    public static final int STR_LOG_STATEMENT_RESULTSET_TYPE                                = LOGGER_BASE +  315;
    public static final int STR_LOG_STATEMENT_SET_RESULTSET_TYPE                            = LOGGER_BASE +  316;
    public static final int STR_LOG_STATEMENT_SET_RESULTSET_TYPE_ERROR                      = LOGGER_BASE +  317;

    public static final int STR_LOG_STATEMENT_EXECUTE                                       = LOGGER_BASE +  320;
    public static final int STR_LOG_STATEMENT_EXECUTE_QUERY                                 = LOGGER_BASE +  321;
    public static final int STR_LOG_STATEMENT_EXECUTE_UPDATE                                = LOGGER_BASE +  322;

    public static final int STR_LOG_STATEMENT_GENERATED_VALUES_TABLE                        = LOGGER_BASE +  325;
    public static final int STR_LOG_STATEMENT_GENERATED_VALUES_QUERY                        = LOGGER_BASE +  326;
    public static final int STR_LOG_STATEMENT_GENERATED_VALUES_RESULT                       = LOGGER_BASE +  327;
    public static final int STR_LOG_STATEMENT_GENERATED_VALUES_ERROR                        = LOGGER_BASE +  328;

    // io.github.prrvchr.uno.sdbc.PreparedStatementSuper:
    public static final int STR_LOG_STATEMENT_USEBOOKMARKS                                  = LOGGER_BASE +  330;
    public static final int STR_LOG_STATEMENT_SET_USEBOOKMARKS                              = LOGGER_BASE +  331;

    // io.github.prrvchr.uno.sdbc.ResultSetBase:
    public static final int STR_LOG_CREATE_RESULTSET                                        = LOGGER_BASE +  400;
    public static final int STR_LOG_CREATED_RESULTSET_ID                                    = LOGGER_BASE +  401;
    public static final int STR_LOG_RESULTSET_CLOSING                                       = LOGGER_BASE +  402;

    // com.sun.star.sdbc.XResultSet:
    public static final int STR_LOG_RESULTSET_CURSORNAME                                    = LOGGER_BASE +  410;
    public static final int STR_LOG_RESULTSET_FETCH_DIRECTION                               = LOGGER_BASE +  411;
    public static final int STR_LOG_RESULTSET_SET_FETCH_DIRECTION                           = LOGGER_BASE +  412;
    public static final int STR_LOG_RESULTSET_FETCH_SIZE                                    = LOGGER_BASE +  413;
    public static final int STR_LOG_RESULTSET_SET_FETCH_SIZE                                = LOGGER_BASE +  414;
    public static final int STR_LOG_RESULTSET_CONCURRENCY                                   = LOGGER_BASE +  415;
    public static final int STR_LOG_RESULTSET_TYPE                                          = LOGGER_BASE +  416;
    public static final int STR_LOG_RESULTSET_ABSOLUTE                                      = LOGGER_BASE +  417;
    public static final int STR_LOG_RESULTSET_NEXT                                          = LOGGER_BASE +  418;
    public static final int STR_LOG_RESULTSET_RELATIVE                                      = LOGGER_BASE +  419;
    public static final int STR_LOG_RESULTSET_ROW_DELETED                                   = LOGGER_BASE +  420;
    public static final int STR_LOG_RESULTSET_ROW_INSERTED                                  = LOGGER_BASE +  421;
    public static final int STR_LOG_RESULTSET_ROW_UPDATED                                   = LOGGER_BASE +  422;
    public static final int STR_LOG_RESULTSET_GET_PARAMETER                                 = LOGGER_BASE +  423;
    public static final int STR_LOG_RESULTSET_UPDATE_PARAMETER                              = LOGGER_BASE +  424;

    // com.sun.star.sdbcx.XRowLocate:
    public static final int STR_LOG_RESULTSET_ISBOOKMARKABLE                                = LOGGER_BASE +  430;
    public static final int STR_LOG_RESULTSET_CANUPDATEINSERTEDROWS                         = LOGGER_BASE +  431;
    public static final int STR_LOG_RESULTSET_COMPARE_BOOKMARKS                             = LOGGER_BASE +  432;
    public static final int STR_LOG_RESULTSET_GET_BOOKMARK                                  = LOGGER_BASE +  433;
    public static final int STR_LOG_RESULTSET_MOVE_TO_BOOKMARK                              = LOGGER_BASE +  434;
    public static final int STR_LOG_RESULTSET_MOVE_RELATIVE_TO_BOOKMARK                     = LOGGER_BASE +  435;
    public static final int STR_LOG_RESULTSET_MOVE_TO_BOOKMARK_ON_INSERT                    = LOGGER_BASE +  436;

    // com.sun.star.sdbc.XResultSetUpdate:
    public static final int STR_LOG_RESULTSET_INSERT_ROW                                    = LOGGER_BASE +  440;
    public static final int STR_LOG_RESULTSET_UPDATE_ROW                                    = LOGGER_BASE +  441;
    public static final int STR_LOG_RESULTSET_DELETE_ROW                                    = LOGGER_BASE +  442;
    public static final int STR_LOG_RESULTSET_CANCEL_ROW_UPDATES                            = LOGGER_BASE +  443;
    public static final int STR_LOG_RESULTSET_MOVE_TO_CURRENT_ROW                           = LOGGER_BASE +  444;
    public static final int STR_LOG_RESULTSET_MOVE_TO_INSERT_ROW                            = LOGGER_BASE +  445;

    // com.sun.star.sdbc.XResultSetUpdate:
    public static final int STR_LOG_CACHED_RESULTSET_POSITIONED_UPDATE                      = LOGGER_BASE +  450;
    public static final int STR_LOG_CACHED_RESULTSET_VISIBILITY                             = LOGGER_BASE +  451;
    public static final int STR_LOG_CACHED_RESULTSET_INSERT_ROW                             = LOGGER_BASE +  452;
    public static final int STR_LOG_CACHED_RESULTSET_UPDATE_ROW                             = LOGGER_BASE +  453;
    public static final int STR_LOG_CACHED_RESULTSET_DELETE_ROW                             = LOGGER_BASE +  454;
    public static final int STR_LOG_CACHED_RESULTSET_CANCEL_ROW_UPDATES                     = LOGGER_BASE +  455;
    public static final int STR_LOG_CACHED_RESULTSET_MOVE_TO_CURRENT_ROW                    = LOGGER_BASE +  456;
    public static final int STR_LOG_CACHED_RESULTSET_MOVE_TO_INSERT_ROW                     = LOGGER_BASE +  457;

    // io.github.prrvchr.uno.sdbcx.TableContainerBase:
    public static final int STR_LOG_CREATE_TABLES                                           = LOGGER_BASE +  500;
    public static final int STR_LOG_CREATED_TABLES_ID                                       = LOGGER_BASE +  501;
    public static final int STR_LOG_TABLES_DISPOSING                                        = LOGGER_BASE +  502;

    public static final int STR_LOG_TABLES_CREATE_TABLE_QUERY                               = LOGGER_BASE +  510;
    public static final int STR_LOG_TABLES_CREATE_TABLE_QUERY_ERROR                         = LOGGER_BASE +  511;
    public static final int STR_LOG_TABLES_REMOVE_TABLE_QUERY                               = LOGGER_BASE +  512;
    public static final int STR_LOG_TABLES_REMOVE_TABLE_QUERY_ERROR                         = LOGGER_BASE +  513;

    // io.github.prrvchr.uno.sdbcx.TableSuper:
    public static final int STR_LOG_CREATE_TABLE                                            = LOGGER_BASE +  600;
    public static final int STR_LOG_CREATED_TABLE_ID                                        = LOGGER_BASE +  601;
    public static final int STR_LOG_TABLE_DISPOSING                                         = LOGGER_BASE +  602;

    public static final int STR_LOG_TABLE_RENAME_QUERY                                      = LOGGER_BASE +  605;
    public static final int STR_LOG_TABLE_RENAME_QUERY_ERROR                                = LOGGER_BASE +  606;
    public static final int STR_LOG_TABLE_RENAME_UNSUPPORTED_FEATURE_ERROR                  = LOGGER_BASE +  607;
    public static final int STR_LOG_TABLE_RENAME_TABLE_NOT_FOUND_ERROR                      = LOGGER_BASE +  608;
    public static final int STR_LOG_TABLE_RENAME_OPERATION_CANCELLED_ERROR                  = LOGGER_BASE +  609;
    public static final int STR_LOG_TABLE_RENAME_DUPLICATE_TABLE_NAME_ERROR                 = LOGGER_BASE +  610;
    public static final int STR_LOG_TABLE_RENAME_FEATURE_NOT_IMPLEMENTED                    = LOGGER_BASE +  611;
    public static final int STR_LOG_TABLE_RENAME_UNSUPPORTED_FUNCTION_ERROR                 = LOGGER_BASE +  612;

    public static final int STR_LOG_TABLE_ALTER_COLUMN_QUERY                                = LOGGER_BASE +  615;
    public static final int STR_LOG_TABLE_ALTER_COLUMN_QUERY_ERROR                          = LOGGER_BASE +  616;
    public static final int STR_LOG_ALTER_IDENTITY_UNSUPPORTED_FEATURE_ERROR                = LOGGER_BASE +  617;

    public static final int STR_LOG_COLUMN_ALTER_QUERY                                      = LOGGER_BASE +  620;
    public static final int STR_LOG_COLUMN_ALTER_QUERY_ERROR                                = LOGGER_BASE +  621;
    public static final int STR_LOG_COLUMN_ALTER_UNSUPPORTED_FEATURE_ERROR                  = LOGGER_BASE +  622;
    public static final int STR_LOG_COLUMN_REMOVE_QUERY                                     = LOGGER_BASE +  623;
    public static final int STR_LOG_COLUMN_REMOVE_QUERY_ERROR                               = LOGGER_BASE +  624;

    public static final int STR_LOG_CREATE_KEYS                                             = LOGGER_BASE +  630;
    public static final int STR_LOG_CREATED_KEYS_ID                                         = LOGGER_BASE +  631;
    public static final int STR_LOG_KEYS_DISPOSING                                          = LOGGER_BASE +  632;

    public static final int STR_LOG_KEYS_CREATE_PKEY_QUERY                                  = LOGGER_BASE +  640;
    public static final int STR_LOG_KEYS_CREATE_PKEY_QUERY_ERROR                            = LOGGER_BASE +  641;
    public static final int STR_LOG_KEYS_REMOVE_PKEY_QUERY                                  = LOGGER_BASE +  642;
    public static final int STR_LOG_KEYS_REMOVE_PKEY_QUERY_ERROR                            = LOGGER_BASE +  643;

    public static final int STR_LOG_PKEY_ADD_UNSUPPORTED_FEATURE_ERROR                      = LOGGER_BASE +  645;
    public static final int STR_LOG_PKEY_REMOVE_UNSUPPORTED_FEATURE_ERROR                   = LOGGER_BASE +  646;

    public static final int STR_LOG_KEYS_CREATE_FKEY_QUERY                                  = LOGGER_BASE +  650;
    public static final int STR_LOG_KEYS_CREATE_FKEY_QUERY_ERROR                            = LOGGER_BASE +  651;
    public static final int STR_LOG_KEYS_REMOVE_FKEY_QUERY                                  = LOGGER_BASE +  652;
    public static final int STR_LOG_KEYS_REMOVE_FKEY_QUERY_ERROR                            = LOGGER_BASE +  653;

    public static final int STR_LOG_FKEY_ADD_INVALID_COLUMN_TYPE_ERROR                      = LOGGER_BASE +  655;
    public static final int STR_LOG_FKEY_ADD_UNSUPPORTED_FEATURE_ERROR                      = LOGGER_BASE +  656;
    public static final int STR_LOG_FKEY_ADD_UNSPECIFIED_ERROR                              = LOGGER_BASE +  657;
    public static final int STR_LOG_FKEY_REMOVE_UNSUPPORTED_FEATURE_ERROR                   = LOGGER_BASE +  658;

    public static final int STR_LOG_CREATE_INDEXES                                          = LOGGER_BASE +  660;
    public static final int STR_LOG_CREATED_INDEXES_ID                                      = LOGGER_BASE +  661;
    public static final int STR_LOG_INDEXES_DISPOSING                                       = LOGGER_BASE +  662;

    public static final int STR_LOG_INDEXES_CREATE_INDEX_QUERY                              = LOGGER_BASE +  670;
    public static final int STR_LOG_INDEXES_CREATE_INDEX_QUERY_ERROR                        = LOGGER_BASE +  671;
    public static final int STR_LOG_INDEXES_REMOVE_INDEX_QUERY                              = LOGGER_BASE +  672;
    public static final int STR_LOG_INDEXES_REMOVE_INDEX_QUERY_ERROR                        = LOGGER_BASE +  673;

    public static final int STR_LOG_CREATE_VIEWS                                            = LOGGER_BASE +  700;
    public static final int STR_LOG_CREATED_VIEWS_ID                                        = LOGGER_BASE +  701;
    public static final int STR_LOG_VIEWS_DISPOSING                                         = LOGGER_BASE +  702;

    public static final int STR_LOG_VIEWS_CREATE_VIEW_QUERY                                 = LOGGER_BASE +  710;
    public static final int STR_LOG_VIEWS_CREATE_VIEW_QUERY_ERROR                           = LOGGER_BASE +  711;
    public static final int STR_LOG_VIEWS_REMOVE_VIEW_QUERY                                 = LOGGER_BASE +  712;
    public static final int STR_LOG_VIEWS_REMOVE_VIEW_QUERY_ERROR                           = LOGGER_BASE +  713;

    public static final int STR_LOG_CREATE_VIEW                                             = LOGGER_BASE +  800;
    public static final int STR_LOG_CREATED_VIEW_ID                                         = LOGGER_BASE +  801;
    public static final int STR_LOG_VIEW_DISPOSING                                          = LOGGER_BASE +  802;

    public static final int STR_LOG_VIEW_RENAME_QUERY                                       = LOGGER_BASE +  805;
    public static final int STR_LOG_VIEW_RENAME_QUERY_ERROR                                 = LOGGER_BASE +  806;
    public static final int STR_LOG_VIEW_RENAME_UNSUPPORTED_FEATURE_ERROR                   = LOGGER_BASE +  807;
    public static final int STR_LOG_VIEW_RENAME_VIEW_NOT_FOUND_ERROR                        = LOGGER_BASE +  808;
    public static final int STR_LOG_VIEW_RENAME_OPERATION_CANCELLED_ERROR                   = LOGGER_BASE +  809;
    public static final int STR_LOG_VIEW_RENAME_DUPLICATE_VIEW_NAME_ERROR                   = LOGGER_BASE +  810;
    public static final int STR_LOG_VIEW_RENAME_FEATURE_NOT_IMPLEMENTED                     = LOGGER_BASE +  811;
    public static final int STR_LOG_VIEW_RENAME_UNSUPPORTED_FUNCTION_ERROR                  = LOGGER_BASE +  812;
    public static final int STR_LOG_VIEW_RENAME_UNSPECIFIED_ERROR                           = LOGGER_BASE +  812;

    public static final int STR_LOG_VIEW_ALTER_QUERY                                        = LOGGER_BASE +  815;
    public static final int STR_LOG_VIEW_ALTER_QUERY_ERROR                                  = LOGGER_BASE +  816;

    public static final int STR_LOG_CREATE_USERS                                            = LOGGER_BASE + 1000;
    public static final int STR_LOG_CREATED_USERS_ID                                        = LOGGER_BASE + 1001;
    public static final int STR_LOG_USERS_DISPOSING                                         = LOGGER_BASE + 1002;

    public static final int STR_LOG_USERS_CREATE_USER_QUERY                                 = LOGGER_BASE + 1010;
    public static final int STR_LOG_USERS_CREATE_USER_QUERY_ERROR                           = LOGGER_BASE + 1011;
    public static final int STR_LOG_USERS_CREATE_USER_FEATURE_NOT_IMPLEMENTED               = LOGGER_BASE + 1012;

    public static final int STR_LOG_USERS_REMOVE_USER_QUERY                                 = LOGGER_BASE + 1015;
    public static final int STR_LOG_USERS_REMOVE_USER_QUERY_ERROR                           = LOGGER_BASE + 1016;

    public static final int STR_LOG_CREATE_GROUPS                                           = LOGGER_BASE + 1100;
    public static final int STR_LOG_CREATED_GROUPS_ID                                       = LOGGER_BASE + 1101;
    public static final int STR_LOG_GROUPS_DISPOSING                                        = LOGGER_BASE + 1102;

    public static final int STR_LOG_GROUPS_CREATE_GROUP_QUERY                               = LOGGER_BASE + 1110;
    public static final int STR_LOG_GROUPS_CREATE_GROUP_QUERY_ERROR                         = LOGGER_BASE + 1111;
    public static final int STR_LOG_GROUPS_REMOVE_GROUP_QUERY                               = LOGGER_BASE + 1112;
    public static final int STR_LOG_GROUPS_REMOVE_GROUP_QUERY_ERROR                         = LOGGER_BASE + 1113;

    public static final int STR_LOG_CREATE_USER                                             = LOGGER_BASE + 1200;
    public static final int STR_LOG_CREATED_USER_ID                                         = LOGGER_BASE + 1201;
    public static final int STR_LOG_USER_DISPOSING                                          = LOGGER_BASE + 1202;

    public static final int STR_LOG_USER_GRANT_PRIVILEGE_QUERY                              = LOGGER_BASE + 1210;
    public static final int STR_LOG_USER_GRANT_PRIVILEGE_QUERY_ERROR                        = LOGGER_BASE + 1211;

    public static final int STR_LOG_USER_REVOKE_PRIVILEGE_QUERY                             = LOGGER_BASE + 1220;
    public static final int STR_LOG_USER_REVOKE_PRIVILEGE_QUERY_ERROR                       = LOGGER_BASE + 1221;

    public static final int STR_LOG_USER_CHANGE_PASSWORD_QUERY                              = LOGGER_BASE + 1230;
    public static final int STR_LOG_USER_CHANGE_PASSWORD_QUERY_ERROR                        = LOGGER_BASE + 1231;

    public static final int STR_LOG_CREATE_GROUP                                            = LOGGER_BASE + 1300;
    public static final int STR_LOG_CREATED_GROUP_ID                                        = LOGGER_BASE + 1301;
    public static final int STR_LOG_GROUP_DISPOSING                                         = LOGGER_BASE + 1302;

    public static final int STR_LOG_GROUP_GRANT_PRIVILEGE_QUERY                             = LOGGER_BASE + 1310;
    public static final int STR_LOG_GROUP_GRANT_PRIVILEGE_QUERY_ERROR                       = LOGGER_BASE + 1311;

    public static final int STR_LOG_GROUP_REVOKE_PRIVILEGE_QUERY                            = LOGGER_BASE + 1320;
    public static final int STR_LOG_GROUP_REVOKE_PRIVILEGE_QUERY_ERROR                      = LOGGER_BASE + 1321;

    public static final int STR_LOG_CREATE_USERROLES                                        = LOGGER_BASE + 1400;
    public static final int STR_LOG_CREATED_USERROLES_ID                                    = LOGGER_BASE + 1401;
    public static final int STR_LOG_USERROLES_DISPOSING                                     = LOGGER_BASE + 1402;

    public static final int STR_LOG_USERROLES_GRANT_ROLE_QUERY                              = LOGGER_BASE + 1410;
    public static final int STR_LOG_USERROLES_GRANT_ROLE_QUERY_ERROR                        = LOGGER_BASE + 1411;
    public static final int STR_LOG_USERROLES_REVOKE_ROLE_QUERY                             = LOGGER_BASE + 1412;
    public static final int STR_LOG_USERROLES_REVOKE_ROLE_QUERY_ERROR                       = LOGGER_BASE + 1413;

    public static final int STR_LOG_CREATE_GROUPROLES                                       = LOGGER_BASE + 1500;
    public static final int STR_LOG_CREATED_GROUPROLES_ID                                   = LOGGER_BASE + 1501;
    public static final int STR_LOG_GROUPROLES_DISPOSING                                    = LOGGER_BASE + 1502;

    public static final int STR_LOG_GROUPROLES_GRANT_ROLE_QUERY                             = LOGGER_BASE + 1510;
    public static final int STR_LOG_GROUPROLES_GRANT_ROLE_QUERY_ERROR                       = LOGGER_BASE + 1511;
    public static final int STR_LOG_GROUPROLES_REVOKE_ROLE_QUERY                            = LOGGER_BASE + 1512;
    public static final int STR_LOG_GROUPROLES_REVOKE_ROLE_QUERY_ERROR                      = LOGGER_BASE + 1513;

}
