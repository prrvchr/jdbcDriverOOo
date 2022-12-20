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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class ReaderInputStream
    extends InputStream
{

    private final Reader m_reader;
    private int m_nextbyte = -1;
    
    public ReaderInputStream(Reader reader)
    {
        m_reader = reader;
    }

    @Override
    public void close()
        throws IOException
    {
        m_reader.close();
    }

    @Override
    public int read()
        throws IOException
    {
        if (m_nextbyte >= 0) {
            int currentByte = m_nextbyte;
            m_nextbyte = -1;
            return currentByte;
        }
        else {
            int c = m_reader.read();
            if (c < 0) {
                return c;
            }
            m_nextbyte = (byte) ((c >>> 8) & 0xff);
            return c & 0xff;
        }
    }
    

    @Override
    public int read(byte[] b, int off, int len)
        throws IOException
    {
        if ((off < 0) || (len < 0) || (off + len > b.length)) {
            throw new IndexOutOfBoundsException();
        }
        else if (len == 0) {
            return 0;
        }
        else if (len == 1) {
            int next = read();
            if (next < 0) {
                return next;
            }
            b[off] = (byte) next;
            return 1;
        }
        else {
            int charCount = len / 2;
            char[] chars = new char[charCount];
            int charsRead = m_reader.read(chars);
            if (charsRead < 0) {
                return charsRead;
            }
            int byteLength = len & ~1;
            for (int i = 0; i < byteLength; i++) {
                b[off + i] = (byte)(chars[i/2] >>> (8*(i&1)));
            }
            return byteLength;
        }
    }


}
