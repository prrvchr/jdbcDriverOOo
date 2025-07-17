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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class ReaderInputStream
    extends InputStream {

    private static final int BYTE_SIZE = 8;
    private static final int FULL_BYTE = 0xff;
    private final Reader mReader;
    private int mNextbyte = -1;
    
    public ReaderInputStream(Reader reader) {
        mReader = reader;
    }

    @Override
    public void close()
        throws IOException {
        mReader.close();
    }

    @Override
    public int read()
        throws IOException {
        int count;
        if (mNextbyte >= 0) {
            int currentByte = mNextbyte;
            mNextbyte = -1;
            count = currentByte;
        } else {
            int c = mReader.read();
            if (c < 0) {
                count = c;
            } else {
                mNextbyte = (byte) ((c >>> BYTE_SIZE) & FULL_BYTE);
                count = c & FULL_BYTE;
            }
        }
        return count;
    }
    

    @Override
    public int read(byte[] b, int off, int len)
        throws IOException {
        int count;
        if (off < 0 || len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            count = 0;
        } else if (len == 1) {
            int next = read();
            if (next < 0) {
                count = next;
            } else {
                b[off] = (byte) next;
                count = 1;
            }
        } else {
            int charCount = len / 2;
            char[] chars = new char[charCount];
            int charsRead = mReader.read(chars);
            if (charsRead < 0) {
                count = charsRead;
            } else {
                int byteLength = len & ~1;
                for (int i = 0; i < byteLength; i++) {
                    b[off + i] = (byte)(chars[i / 2] >>> (BYTE_SIZE * (i & 1)));
                }
                count = byteLength;
            }
        }
        return count;
    }


}
