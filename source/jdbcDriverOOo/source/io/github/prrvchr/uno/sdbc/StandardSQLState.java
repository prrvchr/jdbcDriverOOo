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
package io.github.prrvchr.uno.sdbc;

public enum StandardSQLState
{
    SQL_WRONG_PARAMETER_NUMBER("07001"),
    SQL_INVALID_DESCRIPTOR_INDEX("07009"),
    SQL_UNABLE_TO_CONNECT("08001"),
    SQL_NUMERIC_OUT_OF_RANGE("22003"),
    SQL_INVALID_DATE_TIME("22007"),
    SQL_INVALID_CURSOR_STATE("24000"),
    SQL_TABLE_OR_VIEW_EXISTS("42S01"),
    SQL_TABLE_OR_VIEW_NOT_FOUND("42S02"),
    SQL_INDEX_EXISTS("42S11"),
    SQL_INDEX_NOT_FOUND("42S12"),
    SQL_COLUMN_EXISTS("42S21"),
    SQL_COLUMN_NOT_FOUND("42S22"),
    SQL_GENERAL_ERROR("HY000"),
    SQL_INVALID_SQL_DATA_TYPE("HY004"),
    SQL_OPERATION_CANCELED("HY008"),
    SQL_FUNCTION_SEQUENCE_ERROR("HY010"),
    SQL_INVALID_CURSOR_POSITION("HY109"),
    SQL_INVALID_BOOKMARK_VALUE("HY111"),
    SQL_FEATURE_NOT_IMPLEMENTED("HYC00"),
    SQL_FUNCTION_NOT_SUPPORTED("IM001"),
    SQL_CONNECTION_DOES_NOT_EXIST("08003"),
    SQL_ERROR_UNSPECIFIED("");


    private String text;

    private StandardSQLState(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }


}
