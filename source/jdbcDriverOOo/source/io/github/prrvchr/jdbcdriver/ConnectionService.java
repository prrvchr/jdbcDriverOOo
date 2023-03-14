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
package io.github.prrvchr.jdbcdriver;


public enum ConnectionService {
    CSS_SDBC_CONNECTION("com.sun.star.sdbc.Connection"),
    CSS_SDBCX_CONNECTION("com.sun.star.sdbcx.Connection"),
    CSS_SDB_CONNECTION("com.sun.star.sdb.Connection");
    
    private String service;
    
    private ConnectionService(String service)
    {
        this.service = service;
    }
    
    public String service()
    {
        return service;
    }
    
    public static ConnectionService fromString(String service)
    {
        for (ConnectionService connection : ConnectionService.values()) {
            if (connection.service.equalsIgnoreCase(service)) {
                return connection;
            }
        }
        // FIXME: By default we return a connection whose type can work with the two possible
        // FIXME: drivers (ie: com.sun.star.sdbc.Driver or com.sun.star.sdbcx.Driver)
        return ConnectionService.CSS_SDBCX_CONNECTION;
    }


}
