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
package io.github.prrvchr.uno.driver.provider;

import java.util.concurrent.atomic.AtomicInteger;

import io.github.prrvchr.uno.helper.ResourceBasedEventLogger;


public class ConnectionLog
    extends ResourceBasedEventLogger {

    private static final AtomicInteger[] UNIQUE_IDS;

    static {
        UNIQUE_IDS = new AtomicInteger[LoggerObjectType.values().length];
        for (int i = 0; i < UNIQUE_IDS.length; i++) {
            UNIQUE_IDS[i] = new AtomicInteger(1);
        }
    }

    private final String mId;

    public ConnectionLog(ResourceBasedEventLogger logger,
                         LoggerObjectType type) {
        super(logger);
        mId = String.format("<%s #%s>", type.getName(), UNIQUE_IDS[type.ordinal()].getAndIncrement());
    }

    public String getObjectId() {
        return mId;
    }

    @Override
    public boolean logrb(int level,
                         int id,
                         Object... arguments) {
        return super.logrb(level, id, _getArgs(arguments));
    }

    @Override
    public boolean logprb(int level,
                          int id,
                          Object... arguments) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        return super.logprb(level, caller, id, _getArgs(arguments));
    }

    @Override
    public boolean logprb(int level,
                          String cls,
                          String method,
                          int id,
                          Object... arguments) {
        return super.logprb(level, cls, method, id, _getArgs(arguments));
    }

    @Override
    public String getStringResource(int id, Object... arguments) {
        return super.getStringResource(id, _getArgs(arguments));
    }

    private Object[] _getArgs(Object[] arguments) {
        Object[] args = new Object[arguments.length + 1];
        args[0] = mId;
        System.arraycopy(arguments, 0, args, 1, arguments.length);
        return args;
    }

}
