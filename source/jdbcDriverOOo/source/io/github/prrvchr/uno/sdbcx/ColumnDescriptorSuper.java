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
package io.github.prrvchr.uno.sdbcx;

import java.util.Map;

import com.sun.star.beans.PropertyAttribute;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.Type;

import io.github.prrvchr.jdbcdriver.PropertyIds;
import io.github.prrvchr.uno.helper.PropertyWrapper;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertyGetter;
import io.github.prrvchr.uno.helper.PropertySetAdapter.PropertySetter;

public abstract class ColumnDescriptorSuper
    extends ColumnDescriptorBase
{

    private String m_Catalog;
    private String m_Schema;
    private String m_Table;
    protected String m_AutoIncrementCreation = "";

    // The constructor method:
    public ColumnDescriptorSuper(String service,
                                 String[] services,
                                 String catalog,
                                 String schema,
                                 String table,
                                 boolean sensitive)
    {
        super(service, services, sensitive);
        m_Catalog = catalog;
        m_Schema = schema;
        m_Table = table;
        System.out.println("sdbcx.descriptors.ColumnDescriptorSuper()");
    }

    @Override
    protected void registerProperties(Map<String, PropertyWrapper> properties) {
        short readonly = PropertyAttribute.READONLY;

        // FIXME: Although these properties are not in the UNO API, they are claimed by
        // FIXME: LibreOffice/Base and necessary to obtain tables whose contents can be edited in Base
        properties.put(PropertyIds.CATALOGNAME.getName(),
                       new PropertyWrapper(Type.STRING, readonly,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_Catalog;
                                               }
                                           },
                                           null));

        properties.put(PropertyIds.SCHEMANAME.getName(),
                       new PropertyWrapper(Type.STRING, readonly,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_Schema;
                                               }
                                           },
                                           null));

        properties.put(PropertyIds.TABLENAME.getName(),
                       new PropertyWrapper(Type.STRING, readonly,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_Table;
                                               }
                                           },
                                           null));


        properties.put(PropertyIds.AUTOINCREMENTCREATION.getName(),
                       new PropertyWrapper(Type.STRING,
                                           new PropertyGetter() {
                                               @Override
                                               public Object getValue() throws WrappedTargetException
                                               {
                                                   return m_AutoIncrementCreation;
                                               }
                                           },
                                           new PropertySetter() {
                                               @Override
                                               public void setValue(Object value) throws PropertyVetoException,
                                                                                         IllegalArgumentException,
                                                                                         WrappedTargetException
                                               {
                                                   m_AutoIncrementCreation = (String) value;
                                               }
                                           }));

        super.registerProperties(properties);
    }

}
