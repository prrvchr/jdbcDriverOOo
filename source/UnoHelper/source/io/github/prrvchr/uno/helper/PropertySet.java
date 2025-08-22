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

import java.util.Map;

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XFastPropertySet;
import com.sun.star.beans.XMultiPropertySet;
import com.sun.star.beans.XPropertiesChangeListener;
import com.sun.star.beans.XPropertyChangeListener;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.beans.XVetoableChangeListener;
import com.sun.star.lang.DisposedException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lib.uno.helper.ComponentBase;


public abstract class PropertySet
    extends ComponentBase
    implements XPropertySet,
               XFastPropertySet,
               XMultiPropertySet {

    private final PropertySetAdapter mAdapter;

    protected PropertySet() {
        mAdapter = new PropertySetAdapter(this, this);
    }

    protected void registerProperties(Map<String, PropertyWrapper> properties) {
        mAdapter.registerProperties(properties);
    }


    @Override
    protected void postDisposing() {
        super.postDisposing();
        mAdapter.dispose();
    }

    public synchronized void addPropertyChangeListener(String name,
                                                       XPropertyChangeListener listener)
        throws UnknownPropertyException,
               WrappedTargetException {
        // XXX: Only add listeners if you are not disposed
        if (!bDisposed) {
            mAdapter.addPropertyChangeListener(name, listener);
        }
    }

    public synchronized void addVetoableChangeListener(String name,
                                                       XVetoableChangeListener listener)
        throws UnknownPropertyException,
               WrappedTargetException {
        // XXX: Only add listeners if you are not disposed
        if (!bDisposed) {
            mAdapter.addVetoableChangeListener(name, listener);
        }
    }

    public synchronized void addPropertiesChangeListener(String[] names,
                                                         XPropertiesChangeListener listener) {
        // XXX: Only add listeners if you are not disposed
        if (!bDisposed) {
            mAdapter.addPropertiesChangeListener(names, listener);
        }
    }

    public XPropertySetInfo getPropertySetInfo() {
        return mAdapter.getPropertySetInfo();
    }

    public synchronized Object getPropertyValue(String name)
        throws UnknownPropertyException,
               WrappedTargetException {
        checkDisposed();
        return mAdapter.getPropertyValue(name);
    }

    @Override
    public synchronized Object getFastPropertyValue(int handle)
        throws UnknownPropertyException,
               WrappedTargetException {
        checkDisposed();
        return mAdapter.getFastPropertyValue(handle);
    }

    public synchronized Object[] getPropertyValues(String[] names) {
        checkDisposed();
        return mAdapter.getPropertyValues(names);
    }

    public synchronized void removePropertyChangeListener(String name,
                                                          XPropertyChangeListener listener)
        throws UnknownPropertyException,
               WrappedTargetException {
        // XXX: All listeners are automatically released in a dispose call
        if (!bDisposed) {
            mAdapter.removePropertyChangeListener(name, listener);
        }
    }

    public synchronized void removeVetoableChangeListener(String name,
                                                          XVetoableChangeListener listener)
        throws UnknownPropertyException,
               WrappedTargetException {
        // XXX: All listeners are automatically released in a dispose call
        if (!bDisposed) {
            mAdapter.removeVetoableChangeListener(name, listener);
        }
    }

    public synchronized void removePropertiesChangeListener(XPropertiesChangeListener listener) {
        // XXX: All listeners are automatically released in a dispose call
        if (!bDisposed) {
            mAdapter.removePropertiesChangeListener(listener);
        }
    }

    public synchronized void setPropertyValue(String name,
                                              Object value)
        throws UnknownPropertyException,
               PropertyVetoException,
               IllegalArgumentException,
               WrappedTargetException {
        checkDisposed();
        mAdapter.setPropertyValue(name, value);
    }

    public synchronized void setFastPropertyValue(int handle,
                                                  Object value)
        throws UnknownPropertyException,
               PropertyVetoException,
               IllegalArgumentException,
               WrappedTargetException {
        checkDisposed();
        mAdapter.setFastPropertyValue(handle, value);
    }

    public synchronized void setPropertyValues(String[] names,
                                               Object[] values)
        throws PropertyVetoException,
               IllegalArgumentException,
               WrappedTargetException {
        checkDisposed();
        mAdapter.setPropertyValues(names, values);
    }

    public synchronized void firePropertiesChangeEvent(String[] names,
                                                       XPropertiesChangeListener listener) {
        checkDisposed();
        mAdapter.firePropertiesChangeEvent(names, listener);
    }

    // XXX: Checks whether this component (which you should have locked, prior to this call,
    // XXX: and until you are done using) is disposed, throwing DisposedException if it is. */
    protected final synchronized void checkDisposed() {
        if (bInDispose || bDisposed) {
            throw new DisposedException();
        }
    }

}
