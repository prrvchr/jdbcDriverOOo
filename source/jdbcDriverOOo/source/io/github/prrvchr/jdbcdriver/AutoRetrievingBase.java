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

import io.github.prrvchr.uno.helper.UnoHelper;
import io.github.prrvchr.uno.sdbc.ConnectionBase;


public class AutoRetrievingBase
{
    // contains the statement which should be used when query for automatically generated values
    private String autoRetrievingStatement;
    // set to when we should allow to query for generated values
    private boolean autoRetrievingEnabled;

    public boolean isAutoRetrievingEnabled()
    {
        return autoRetrievingEnabled;
    }

    public String getAutoRetrievingStatement()
    {
        return autoRetrievingStatement;
    }

    public void setAutoRetrievingEnabled(boolean autoRetrievingEnabled)
    {
        this.autoRetrievingEnabled = autoRetrievingEnabled;
    }

    public void setAutoRetrievingStatement(String autoRetrivingStatement)
    {
        this.autoRetrievingStatement = autoRetrivingStatement;
    }

    /** transform the statement to query for auto generated values
     * @param  insertStatement
     *     The "INSERT" statement, is used to query for column and table names
     * @return
     *     The transformed generated statement.
     */
    public String getTransformedGeneratedStatement(ConnectionBase connection,
                                                   String insertStatement)
    {
        UnoHelper.ensure(autoRetrievingEnabled, "Illegal call here. isAutoRetrievingEnabled() is false!", connection.getLogger());
        insertStatement = insertStatement.toUpperCase();
        String statement = "";
        if (insertStatement.startsWith("INSERT")) {
            statement = autoRetrievingStatement;
            
            int index = 0;
            index = statement.indexOf("$column");
            if (index != -1) {
                // we need a column
                // FIXME: do something?
            }
            
            index = statement.indexOf("$table");
            if (index != -1) {
                // we need a table
                int intoIndex = insertStatement.indexOf("INTO ");
                insertStatement = insertStatement.substring(intoIndex + 5);
                
                int firstNonSpace;
                for (firstNonSpace = 0; firstNonSpace < insertStatement.length();) {
                    int ch = insertStatement.codePointAt(firstNonSpace);
                    if (ch != ' ') {
                        break;
                    }
                    firstNonSpace += Character.charCount(ch);
                }
                insertStatement = insertStatement.substring(firstNonSpace);
                
                int nextSpace = insertStatement.indexOf(' ');
                String tableName;
                if (nextSpace >= 0) {
                    tableName = insertStatement.substring(0, nextSpace);
                }
                else {
                    tableName = "";
                }
                
                statement = statement.substring(0, index) + tableName + statement.substring(index + 6);
            }
        }
        return statement;
    }

}
