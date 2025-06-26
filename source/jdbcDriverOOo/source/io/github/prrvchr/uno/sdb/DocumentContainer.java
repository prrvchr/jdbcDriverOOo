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
package io.github.prrvchr.uno.sdb;


import com.sun.star.beans.NamedValue;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.sdb.XOfficeDatabaseDocument;
import com.sun.star.sdbc.XDataSource;
import com.sun.star.uno.Exception;
import com.sun.star.uno.Type;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.uno.helper.ServiceInfo;
import io.github.prrvchr.uno.helper.UnoHelper;


public final class DocumentContainer
     extends ComponentBase
     implements XServiceInfo,
                XInitialization,
                XNameAccess {


    protected static final String[] m_serviceNames1 = {"io.github.prrvchr.jdbcdriver.sdb.DocumentContainer"};

    private static final String mImplementationName = "io.github.prrvchr.jdbcDriverOOo.DocumentContainer";
    private static final String[] mServiceNames = {"com.sun.star.sdb.DocumentContainer"};

    private static XDataSource sDataSource;

    @SuppressWarnings("unused")
    private XComponentContext mContext;

    // The constructor method:
    public DocumentContainer(XComponentContext ctx) {
        mContext = ctx;
        System.out.println("sdb.DocumentContainer() 1");
    }

    @Override
    public void initialize(Object[] args) throws Exception {
        System.out.println("sdb.DocumentContainer.initialize() 1");
        try {
            if (args != null) {
                for (Object arg : args) {
                    System.out.println("sdb.DocumentContainer.initialize() 2");
                    if (arg instanceof NamedValue) {
                        NamedValue property = (NamedValue) arg;
                        if (property.Name.equals("DatabaseDocument")) {
                            System.out.println("sdb.DocumentContainer.initialize() 3");
                            XOfficeDatabaseDocument doc = UnoRuntime.queryInterface(XOfficeDatabaseDocument.class,
                                                                                    property.Value);
                            sDataSource = doc.getDataSource();
                            System.out.println("sdb.DocumentContainer.initialize() 4");
                        }
                    }
                }
            }
        } catch (Throwable e) {
            System.out.println(UnoHelper.getStackTrace(e));
        }
    }

    protected static XDataSource getDataSource() {
        return sDataSource;
    }

    // com.sun.star.lang.XServiceInfo:
    @Override
    public String getImplementationName() {
        return ServiceInfo.getImplementationName(mImplementationName);
    }

    @Override
    public String[] getSupportedServiceNames() {
        return ServiceInfo.getSupportedServiceNames(mServiceNames);
    }

    @Override
    public boolean supportsService(String service) {
        return ServiceInfo.supportsService(mServiceNames, service);
    }

    // com.sun.star.container.XNameAccess:
    @Override
    public Type getElementType() {
        return null;
    }

    @Override
    public boolean hasElements() {
        return false;
    }

    @Override
    public Object getByName(String query) throws NoSuchElementException, WrappedTargetException {
        return null;
    }

    @Override
    public String[] getElementNames() {
        return new String[0];
    }

    @Override
    public boolean hasByName(String query) {
        return false;
    }

}
