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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.PropertyChangeEvent;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XFastPropertySet;
import com.sun.star.beans.XMultiPropertySet;
import com.sun.star.beans.XPropertiesChangeListener;
import com.sun.star.beans.XPropertyChangeListener;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.beans.XVetoableChangeListener;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lib.uno.helper.InterfaceContainer;
import com.sun.star.lib.uno.helper.MultiTypeInterfaceContainer;
import com.sun.star.uno.Any;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Type;
import com.sun.star.uno.TypeClass;
import com.sun.star.uno.XInterface;


public class PropertySetAdapter
    implements XPropertySet,
               XFastPropertySet,
               XMultiPropertySet {

    private static final Comparator<Property> PROPERTYNAMECOMPARATOR = new Comparator<Property>() {
        @Override
        public int compare(Property first,
                           Property second) {
            return first.Name.compareTo(second.Name);
        }
    };

    protected final MultiTypeInterfaceContainer mBoundListeners = new MultiTypeInterfaceContainer();
    protected final MultiTypeInterfaceContainer mVetoableListeners = new MultiTypeInterfaceContainer();
    protected final InterfaceContainer mPropertiesChangeListeners = new InterfaceContainer();

    private final Object mLock;
    private final Object mEventSource;
    // XXX: After registerListeners(), these are read-only:
    private final Map<String, PropertyData> mPropertiesByName = new HashMap<String, PropertyData>();
    private final Map<Integer, PropertyData> mPropertiesByHandle = new HashMap<Integer, PropertyData>();
    private AtomicInteger mNextHandle = new AtomicInteger(1);
    // XXX: Interface containers are locked internally:
    private final PropertySetInfo mPropertySetInfo = new PropertySetInfo();

    public static interface PropertyGetter {
        Object getValue() throws WrappedTargetException;
    }

    public static interface PropertySetter {
        void setValue(Object value) throws PropertyVetoException, IllegalArgumentException, WrappedTargetException;
    }

    private static class PropertyData {
        Property mProperty;
        PropertyGetter mGetter;
        PropertySetter mSetter;
        
        PropertyData(Property property, PropertyGetter getter, PropertySetter setter) {
            mProperty = property;
            mGetter = getter;
            mSetter = setter;
        }
    }

    private class PropertySetInfo implements XPropertySetInfo {
        @Override
        public Property[] getProperties() {
            Property[] properties = new Property[mPropertiesByName.size()];
            int next = 0;
            for (Map.Entry<String, PropertyData> entry : mPropertiesByName.entrySet()) {
                properties[next++] = entry.getValue().mProperty;
            }
            Arrays.sort(properties, PROPERTYNAMECOMPARATOR);
            return properties;
        }

        @Override
        public Property getPropertyByName(String propertyName)
            throws UnknownPropertyException {
            PropertyData propertyData = getPropertyData(propertyName);
            return propertyData.mProperty;
        }

        @Override
        public boolean hasPropertyByName(String name) {
            boolean value = mPropertiesByName.containsKey(name);
            if (!value) {
                String msg = "beans.PropertySetAdapter.hasPropertyByName() ERROR \n";
                msg += mLock.getClass().getName() + " : " + name;
                System.out.println(msg);
            }
            return value;
        }
    }

    /**
     * Creates a new instance.
     * @param lock the lock that will be held while calling the getters and setters
     * @param eventSource the com.sun.star.lang.EventObject Source field, to use in events sent to listeners
     */
    public PropertySetAdapter(Object lock,
                              Object eventSource) {
        this.mLock = lock;
        this.mEventSource = eventSource;
    }

    public void dispose() {
        // XXX: Create an event with this as sender
        EventObject event = new EventObject(mEventSource);
        
        // XXX: Inform all listeners to release this object
        mBoundListeners.disposeAndClear(event);
        mVetoableListeners.disposeAndClear(event);
    }

    public void registerProperties(Map<String, PropertyWrapper> properties) {
        List<String> names = new ArrayList<String>(properties.keySet());
        Collections.sort(names);
        for (String name: names) {
            PropertyWrapper property = properties.get(name);
            // XXX: registerProperty() should only be called from one thread, but just in case:
            int handle = mNextHandle.getAndIncrement();
            registerProperty(name, handle, property.getType(), property.getAttribute(),
                             property.getGetter(), property.getSetter());
        }
    }

    private void registerProperty(String name,
                                 int handle,
                                 Type type,
                                 short attributes,
                                 PropertyGetter getter,
                                 PropertySetter setter) {
        Property property = new Property(name, handle, type, attributes);
        PropertyData data = new PropertyData(property, getter, setter);
        mPropertiesByName.put(name, data);
        mPropertiesByHandle.put(property.Handle, data);
    }

    @Override
    public void addPropertyChangeListener(String name,
                                          XPropertyChangeListener listener)
        throws UnknownPropertyException,
               WrappedTargetException {
        PropertyData data = getPropertyData(name);
        if ((data.mProperty.Attributes & PropertyAttribute.BOUND) != 0) {
            mBoundListeners.addInterface(name, listener);
        } // XXX: else ignore silently
    }

    @Override
    public void addVetoableChangeListener(String name,
                                          XVetoableChangeListener listener)
        throws UnknownPropertyException,
               WrappedTargetException {
        PropertyData data = getPropertyData(name);
        if ((data.mProperty.Attributes & PropertyAttribute.CONSTRAINED) != 0) {
            mVetoableListeners.addInterface(name, listener);
        } // else ignore silently
    }

    @Override
    public void addPropertiesChangeListener(String[] names,
                                            XPropertiesChangeListener listener) {
        mPropertiesChangeListeners.add(listener);
    }

    @Override
    public XPropertySetInfo getPropertySetInfo() {
        return mPropertySetInfo;
    }

    private PropertyData getPropertyData(String name)
        throws UnknownPropertyException {
        PropertyData data = mPropertiesByName.get(name);
        if (data == null) {
            System.out.println("beans.PropertySetAdapter.getPropertyData() ERROR Property Name: " + name);
            throw new UnknownPropertyException(name);
        }
        return data;
    }

    private PropertyData getPropertyData(int handle)
        throws UnknownPropertyException {
        PropertyData data = mPropertiesByHandle.get(handle);
        if (data == null) {
            System.out.println("beans.PropertySetAdapter.getPropertyData() ERROR Property handle: " + handle);
            throw new UnknownPropertyException(Integer.toString(handle));
        }
        return data;
    }

    private Object getPropertyValue(PropertyData data)
        throws WrappedTargetException {
        Object value;
        synchronized (mLock) {
            value = data.mGetter.getValue();
        }
        
        // XXX: null must not be returned. Either a void any is returned
        // XXX: or an any containing an interface type and a null reference.
        if (value == null) {
            if (data.mProperty.Type.getTypeClass() == TypeClass.INTERFACE) {
                value = new Any(data.mProperty.Type, null);
            } else {
                value = new Any(new Type(void.class), null);
            }
        }
        return value;
    }

    @Override
    public Object getPropertyValue(String name)
        throws UnknownPropertyException,
               WrappedTargetException {
        PropertyData propertyData = getPropertyData(name);
        return getPropertyValue(propertyData);
    }

    @Override
    public Object getFastPropertyValue(int handle)
        throws UnknownPropertyException,
               WrappedTargetException {

        PropertyData propertyData = getPropertyData(handle);
        return getPropertyValue(propertyData);
    }

    @Override
    public Object[] getPropertyValues(String[] names) {
        Object[] values = new Object[names.length];
        for (int i = 0; i < names.length; i++) {
            Object value = null;
            try {
                value = getPropertyValue(names[i]);
            } catch (UnknownPropertyException | WrappedTargetException e) {
                System.out.println("beans.PropertySetAdapter.getPropertyValues() ERROR\n" + UnoHelper.getStackTrace(e));
            }
            values[i] = value;
        }
        return values;
    }

    @Override
    public void removePropertyChangeListener(String name,
                                             XPropertyChangeListener listener)
        throws UnknownPropertyException,
               WrappedTargetException {
        // XXX: Check existence:
        getPropertyData(name);
        mBoundListeners.removeInterface(name, listener);
    }

    @Override
    public synchronized void removeVetoableChangeListener(String name,
                                                          XVetoableChangeListener listener)
        throws UnknownPropertyException,
               WrappedTargetException {
        // XXX: Check existence:
        getPropertyData(name);
        mVetoableListeners.removeInterface(name, listener);
    }

    @Override
    public void removePropertiesChangeListener(XPropertiesChangeListener listener) {
        mPropertiesChangeListeners.remove(listener);
    }

    @Override
    public void setPropertyValue(String name,
                                 Object value)
        throws UnknownPropertyException,
               PropertyVetoException,
               IllegalArgumentException,
               WrappedTargetException {
        PropertyData propertyData = getPropertyData(name);
        setPropertyValue(propertyData, value);
    }

    @Override
    public void setFastPropertyValue(int handle,
                                     Object value)
        throws UnknownPropertyException,
               PropertyVetoException,
               IllegalArgumentException,
               WrappedTargetException {
        PropertyData propertyData = getPropertyData(handle);
        setPropertyValue(propertyData, value);
    }

    private void setPropertyValue(PropertyData data,
                                  Object value)
        throws UnknownPropertyException,
               PropertyVetoException,
               IllegalArgumentException,
               WrappedTargetException {
        if ((data.mProperty.Attributes & PropertyAttribute.READONLY) != 0) {
            throw new PropertyVetoException();
        }
        // XXX: The value may be null only if MAYBEVOID attribute is set         
        boolean isvoid = false;
        if (value instanceof Any) {
            isvoid = ((Any) value).getObject() == null;
        } else {
            isvoid = value == null;
        }
        if (isvoid && (data.mProperty.Attributes & PropertyAttribute.MAYBEVOID) == 0) {
            String msg = "beans.PropertySetAdapter.setPropertyValue() ERROR: Property Name: ";
            System.out.println(msg + data.mProperty.Name);
            throw new IllegalArgumentException("The property must have a value; the MAYBEVOID attribute is not set!");
        }

        // XXX: Check if the argument is allowed
        boolean isValueOk = false;
        if (value instanceof Any) {
            isValueOk = checkType(((Any) value).getObject());
        } else {
            isValueOk = checkType(value);
        }
        if (!isValueOk) {
            throw new IllegalArgumentException("No valid UNO type");
        }

        Object[] futureValue = new Object[] { AnyConverter.toObject(data.mProperty.Type, value) };
        Object[] currentValue = new Object[] { getPropertyValue(data.mProperty.Name) };
        Property[] properties = new Property[] { data.mProperty };
        
        fire(properties, currentValue, futureValue, false);
        synchronized (mLock) {
            data.mSetter.setValue(futureValue[0]);
        }
        fire(properties, currentValue, futureValue, true);
    }

    @Override
    public void setPropertyValues(String[] names,
                                  Object[] values)
        throws PropertyVetoException,
               IllegalArgumentException,
               WrappedTargetException {
        for (int i = 0; i < names.length; i++) {
            try {
                setPropertyValue(names[i], values[i]);
            } catch (UnknownPropertyException e) {
                continue;
            }
        }
    }

    private boolean checkType(Object obj) {
        boolean checked = false;
        if (obj == null ||
            obj instanceof Boolean ||
            obj instanceof Character) {
            checked = true;
        } else if (obj instanceof Number || 
                   obj instanceof String ||
                   obj instanceof XInterface) {
            checked = true;
        } else if (obj instanceof Type ||
                   obj instanceof com.sun.star.uno.Enum ||
                   obj.getClass().isArray()) {
            checked = true;
        }
        return checked;
    }

    @Override
    public void firePropertiesChangeEvent(String[] names,
                                          XPropertiesChangeListener listener) {
        PropertyChangeEvent[] events = new PropertyChangeEvent[names.length];
        int count = 0;
        for (String name : names) {
            try {
                PropertyData data = getPropertyData(name);
                Object value = getPropertyValue(name);
                events[count++] = new PropertyChangeEvent(mEventSource, name, false,
                                                          data.mProperty.Handle, value, value);
            } catch (UnknownPropertyException e) {
            } catch (WrappedTargetException e) {
            }
        }
        if (count > 0) {
            if (events.length != count) {
                PropertyChangeEvent[] tmp = new PropertyChangeEvent[count];
                System.arraycopy(events, 0, tmp, 0, count);
                events = tmp;
            }
            listener.propertiesChange(events);
        }
    }

    private void fire(Property[] properties,
                      Object[] oldvalues,
                      Object[] newvalues,
                      boolean changed)
        throws PropertyVetoException {
        PropertyChangeEvent[] events = new PropertyChangeEvent[properties.length];
        int count = 0;
        for (int i = 0; i < properties.length; i++) {
            if (isConstrainedProperty(properties, i, changed) || isBoundProperty(properties, i, changed)) {
                events[count++] = new PropertyChangeEvent(mEventSource, properties[i].Name, false,
                                                          properties[i].Handle, oldvalues[i], newvalues[i]);
            }
        }
        for (int i = 0; i < count; i++) {
            fireListeners(changed, events[i].PropertyName, events[i]);
            fireListeners(changed, "", events[i]);
        }
        if (changed && count > 0) {
            if (count != events.length) {
                PropertyChangeEvent[] tmp = new PropertyChangeEvent[count];
                System.arraycopy(events, 0, tmp, 0, count);
                events = tmp;
            }
            for (Iterator<?> it = mPropertiesChangeListeners.iterator(); it.hasNext();) {
                XPropertiesChangeListener listener = (XPropertiesChangeListener) it.next();
                listener.propertiesChange(events);
            }
        }
    }

    private boolean isConstrainedProperty(Property[] properties,
                                          int index,
                                          boolean changed) {
        return !changed && (properties[index].Attributes & PropertyAttribute.CONSTRAINED) != 0;
    }

    private boolean isBoundProperty(Property[] properties,
                                    int index,
                                    boolean changed) {
        return changed && (properties[index].Attributes & PropertyAttribute.BOUND) != 0;
    }

    private void fireListeners(boolean changed,
                               String key,
                               PropertyChangeEvent event)
        throws PropertyVetoException {
        InterfaceContainer listeners;
        if (changed) {
            listeners = mBoundListeners.getContainer(key);
        } else {
            listeners = mVetoableListeners.getContainer(key);
        }
        if (listeners != null) {
            Iterator<?> it = listeners.iterator();
            while (it.hasNext()) {
                Object listener = it.next();
                if (changed) {
                    ((XPropertyChangeListener)listener).propertyChange(event);
                } else {
                    ((XVetoableChangeListener)listener).vetoableChange(event);
                }
            }
        }
    }

}
