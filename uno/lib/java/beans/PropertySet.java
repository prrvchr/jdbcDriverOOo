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
package io.github.prrvchr.uno.beans;

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
import com.sun.star.uno.Type;

import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.beans.PropertySetAdapter.PropertySetter;


public abstract class PropertySet
    extends ComponentBase
    implements XPropertySet,
               XFastPropertySet,
               XMultiPropertySet
{

    private final PropertySetAdapter m_adapter;

    protected PropertySet() {
        m_adapter = new PropertySetAdapter(this, this);
    }

    @Override
    protected void postDisposing() {
        m_adapter.dispose();
    }

    public void registerProperty(String propertyName, Type type, PropertyGetter getter, PropertySetter setter) {
        registerProperty(propertyName, type, (short)0, getter, setter);
    }
    public void registerProperty(String propertyName, int handle, Type type, PropertyGetter getter, PropertySetter setter) {
        registerProperty(propertyName, handle, type, (short)0, getter, setter);
    }
    public void registerProperty(String propertyName, int handle, Type type, short attributes, PropertyGetter getter, PropertySetter setter) {
        m_adapter.registerProperty(propertyName, handle, type, attributes, getter, setter);
    }
    public void registerProperty(String propertyName, Type type, short attributes, PropertyGetter getter, PropertySetter setter) {
        m_adapter.registerProperty(propertyName, type, attributes, getter, setter);
    }

    public synchronized void addPropertyChangeListener(String propertyName, XPropertyChangeListener listener)
            throws UnknownPropertyException, WrappedTargetException {
        // only add listeners if you are not disposed
        if (!bDisposed) {
            m_adapter.addPropertyChangeListener(propertyName, listener);
        }
    }

    public synchronized void addVetoableChangeListener(String propertyName, XVetoableChangeListener listener)
            throws UnknownPropertyException, WrappedTargetException {
        // only add listeners if you are not disposed
        if (!bDisposed) {
            m_adapter.addVetoableChangeListener(propertyName, listener);
        }
    }

    public synchronized void addPropertiesChangeListener(String[] propertyNames, XPropertiesChangeListener listener) {
        // only add listeners if you are not disposed
        if (!bDisposed) {
            m_adapter.addPropertiesChangeListener(propertyNames, listener);
        }
    }

    public XPropertySetInfo getPropertySetInfo() {
        return m_adapter.getPropertySetInfo();
    }

    public synchronized Object getPropertyValue(String propertyName) throws UnknownPropertyException, WrappedTargetException {
        checkDisposed();
        return m_adapter.getPropertyValue(propertyName);
    }

    @Override
    public synchronized Object getFastPropertyValue(int handle) throws UnknownPropertyException, WrappedTargetException {
        checkDisposed();
        return m_adapter.getFastPropertyValue(handle);
    }

    public synchronized Object[] getPropertyValues(String[] propertyNames) {
        checkDisposed();
        return m_adapter.getPropertyValues(propertyNames);
    }

    public synchronized void removePropertyChangeListener(String propertyName, XPropertyChangeListener listener)
            throws UnknownPropertyException, WrappedTargetException {
        // all listeners are automatically released in a dispose call
        if (!bDisposed) {
            m_adapter.removePropertyChangeListener(propertyName, listener);
        }
    }

    public synchronized void removeVetoableChangeListener(String propertyName, XVetoableChangeListener listener)
            throws UnknownPropertyException, WrappedTargetException {
        // all listeners are automatically released in a dispose call
        if (!bDisposed) {
            m_adapter.removeVetoableChangeListener(propertyName, listener);
        }
    }

    public synchronized void removePropertiesChangeListener(XPropertiesChangeListener listener) {
        // all listeners are automatically released in a dispose call
        if (!bDisposed) {
            m_adapter.removePropertiesChangeListener(listener);
        }
    }

    public synchronized void setPropertyValue(String propertyName, Object value)
            throws UnknownPropertyException, PropertyVetoException, IllegalArgumentException, WrappedTargetException {
        checkDisposed();
        m_adapter.setPropertyValue(propertyName, value);
    }

    public synchronized void setFastPropertyValue(int handle, Object value)
            throws UnknownPropertyException, PropertyVetoException, IllegalArgumentException, WrappedTargetException {
        checkDisposed();
        m_adapter.setFastPropertyValue(handle, value);
    }

    public synchronized void setPropertyValues(String[] propertyNames, Object[] values)
            throws PropertyVetoException, IllegalArgumentException, WrappedTargetException {
        checkDisposed();
        m_adapter.setPropertyValues(propertyNames, values);
    }

    public synchronized void firePropertiesChangeEvent(String[] propertyNames, XPropertiesChangeListener listener) {
        checkDisposed();
        m_adapter.firePropertiesChangeEvent(propertyNames, listener);
    }

    /** Checks whether this component (which you should have locked, prior to this call, and until you are done using) is disposed, throwing DisposedException if it is. */
    protected synchronized final void checkDisposed()
    {
        if (bInDispose || bDisposed) {
            System.out.println("beans.PropertySet()checkDisposed() ERROR: **************************" + this.getClass().getName());
            throw new DisposedException();
        }
    }


}
