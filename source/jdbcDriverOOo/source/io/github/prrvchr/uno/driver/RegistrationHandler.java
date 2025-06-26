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
package io.github.prrvchr.uno.driver;

import java.lang.reflect.Field;

import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;

/**
 * Component main registration class.
 * 
 * <p><strong>This class should not be modified.</strong></p>
 * 
 * @author Cedric Bosdonnat aka. cedricbosdo
 *
 */
public class RegistrationHandler {

    /**
    * Get a component factory for the implementations handled by this class.
    *
    * <p>This method retrieve the Class object associated with the class with the given
    * <code>implementation</code> String parameter. If this class has a <code>m_serviceNames</code>
    * static field then the list of its services will be used, otherwise the <code>implementation</code>
    * parameter will be the only entry in the list of supported services.
    * <strong>This method should not be modified.</strong></p>
    *
    * @param implementation the name of the implementation to create.
    *
    * @return the factory which can create the implementation.
    */
    public static XSingleComponentFactory __getComponentFactory(final String implementation) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(implementation);
        } catch (ClassNotFoundException e) {
            // Nothing to do: skip
            System.err.println("Error happened");
            e.printStackTrace();
        }
        XSingleComponentFactory factory = null;
        if (clazz != null) {
            String fieldName = "m_serviceNames";
            String [] services = null;
            try {
                int i = 0;
                Field[] fields = clazz.getDeclaredFields();
                while (i < fields.length && services == null) {
                    Field field = fields[i];
                    if (field.getName().equals(fieldName)) {
                        field.setAccessible(true);
                        services = (String[]) field.get(null);
                    }
                    i++;
                }
            } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
                // Nothing to do: skip
                System.err.println("Error happened");
                e.printStackTrace();
            }
            if (services == null) {
                services = new String[] {implementation};
                String template = "No <%s> static field, defaulting to: {%s}";
                System.err.println(String.format(template, fieldName, implementation));
            }
            factory = Factory.createComponentFactory(clazz, services);
        }
        return factory;
    }

}
