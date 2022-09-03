/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020 https://prrvchr.github.io                                     ║
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
package io.github.prrvchr.uno.sdbc;

import java.util.concurrent.atomic.AtomicInteger;


public class ConnectionLog
    extends ResourceBasedEventLogger
{
    public static enum ObjectType
    {
        CONNECTION,
        STATEMENT,
        RESULT
    }

    private static final AtomicInteger[] uniqueIds;

    static
    {
        uniqueIds = new AtomicInteger[ObjectType.values().length];
        for (int i = 0; i < uniqueIds.length; i++) {
            uniqueIds[i] = new AtomicInteger(0);
        }
    }

    private final int m_id;

    public ConnectionLog(ResourceBasedEventLogger logger,
                         ObjectType type)
    {
        super(logger);
        m_id = uniqueIds[type.ordinal()].getAndIncrement();
    }

    public int getObjectId() {
        return m_id;
    }

    @Override
    public boolean log(int level,
                       int id,
                       Object... arguments)
    {
        Object[] args = new Object[arguments.length + 1];
        args[0] = m_id;
        System.arraycopy(arguments, 0, args, 1, arguments.length);
        return super.log(level, id, args);
    }

    @Override
    public boolean logp(int level,
                        int id,
                        Object... arguments)
    {
        Object[] args = new Object[arguments.length + 1];
        args[0] = m_id;
        System.arraycopy(arguments, 0, args, 1, arguments.length);
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        return super.logp(level, caller, id, args);
    }

}
