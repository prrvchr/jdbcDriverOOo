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
package io.github.prrvchr.uno.sdbcx;


import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XIndexAccess;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.uno.UnoRuntime;

public final class ContainerEnumeration
    extends WeakBase
    implements XEnumeration,
               XEventListener {

    private boolean mIsListening;
    private XIndexAccess mElements;
    private int[] mOrders;
    private int mIndex;


    public ContainerEnumeration(XIndexAccess elements) {
        this(elements, null);
    }

    public ContainerEnumeration(XIndexAccess elements, int[] orders) {
        mElements = elements;
        mOrders = orders;
        startDisposeListening();
    }

    @Override
    public void disposing(EventObject event) {
        synchronized (this) {
            if (event.Source == mElements) {
                dispose();
            }
        }
    }

    @Override
    public boolean hasMoreElements() {
        boolean hasmore = false;
        synchronized (this) {
            if (mElements != null) {
                if (mIndex < mElements.getCount()) {
                    hasmore = true;
                } else {
                    stopDisposeListening();
                    dispose();
                }
            }
            return hasmore;
        }
    }

    @Override
    public Object nextElement()
        throws NoSuchElementException,
               WrappedTargetException {
        Object value = null;
        synchronized (this) {
            if (mElements != null) {
                if (mIndex < mElements.getCount()) {
                    try {
                        int index = getIndex();
                        value = mElements.getByIndex(index);
                    } catch (com.sun.star.lang.IndexOutOfBoundsException e) {
                        // can't happen
                    }
                }
                if (mIndex >= mElements.getCount()) {
                    stopDisposeListening();
                    dispose();
                }
            }
        }
        if (value == null) {
            throw new NoSuchElementException();
        }
        return value;
    }

    private void startDisposeListening() {
        synchronized (this) {
            if (mIsListening) {
                return;
            }
            XComponent component = UnoRuntime.queryInterface(XComponent.class, mElements);
            if (component != null) {
                component.addEventListener(this);
                mIsListening = true;
            }
        }
    }

    private void stopDisposeListening() {
        synchronized (this) {
            if (!mIsListening) {
                return;
            }
            XComponent component = UnoRuntime.queryInterface(XComponent.class, mElements);
            if (component != null) {
                component.removeEventListener(this);
                mIsListening = false;
            }
        }
    }

    private int getIndex() {
        int index;
        if (mOrders != null) {
            index = mOrders[mIndex++];
        } else {
            index = mIndex++;
        }
        return index;
    }

    private void dispose() {
        mElements = null;
        mOrders = null;
    }

}
