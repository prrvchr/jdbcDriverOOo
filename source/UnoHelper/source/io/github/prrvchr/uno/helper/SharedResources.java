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
package io.github.prrvchr.uno.helper;

import com.sun.star.lang.NullPointerException;
import com.sun.star.uno.XComponentContext;

/**
 * helper class for accessing resources shared by different libraries
 * in the connectivity module.
 */
public class SharedResources {
    private static SharedResources sINSTANCE;
    private static OfficeResourceBundle sBUNDLE;
    private static int sREFCOUNT = 0;

    // The private constructor method:
    private SharedResources(XComponentContext context,
                            String identifier,
                            String path,
                            String basename) {
        try {
            sBUNDLE = new OfficeResourceBundle(context, identifier, path, basename);
        } catch (NullPointerException nullPointerException) { }
    }

    // XXX: the C++ implementation gets the XComponentContext using
    // XXX: ::comphelper::getProcessServiceFactory(), we don't.
    public static synchronized void registerClient(XComponentContext context,
                                                   String identifier,
                                                   String path,
                                                   String basename) {
        if (sINSTANCE == null) {
            sINSTANCE = new SharedResources(context, identifier, path, basename);
        }
        ++sREFCOUNT;
    }

    public static synchronized void revokeClient() {
        if (--sREFCOUNT == 0) {
            sBUNDLE.close();
            sINSTANCE = null;
        }
    }

    public static synchronized SharedResources getInstance() {
        return sINSTANCE;
    }

    /** loads a string from the shared resource file.
     * @param  id the resource ID of the string
     * @return
     *       the string from the resource file
     */
    public String getResource(int id) {
        return loadStringMessage(id);
    }

    /** loads a string from the shared resource file, and replaces all substitutes.
     * @param  id the resource ID of the string to load
     * @param  substitutes A varargs String of substitutions.
     * @return
     *       the string from the resource file, with applied string substitution
     */
    public String getResourceWithSubstitution(int id,
                                              Object... substitutes) {
        return loadStringMessage(id, substitutes);
    }

    private String loadStringMessage(int id,
                                     Object... substitutes) {
        String message = "";
        try {
            message = sBUNDLE.loadString(id);
            if (substitutes.length > 0) {
                message = String.format(message, substitutes);
            }
        } catch (java.lang.Exception e) {
            message = String.format("<invalid event resource: '%s:%d'>", sBUNDLE.getBaseName(), id);
        }
        return message;
    }

}
