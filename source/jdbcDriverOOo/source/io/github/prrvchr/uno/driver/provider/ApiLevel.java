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
package io.github.prrvchr.uno.driver.provider;


public enum ApiLevel {

    COM_SUN_STAR_SDBC("com.sun.star.sdbc"),
    COM_SUN_STAR_SDBCX("com.sun.star.sdbcx"),
    COM_SUN_STAR_SDB("com.sun.star.sdb");


    private final String mService;

    private ApiLevel(final String service) {
        mService = service;
    }

    public final String service() {
        return mService;
    }

    public static ApiLevel fromString(String service) {
        ApiLevel level = ApiLevel.COM_SUN_STAR_SDBCX;
        for (ApiLevel value : ApiLevel.values()) {
            if (value.mService.equalsIgnoreCase(service)) {
                level = value;
            }
        }
        // FIXME: By default we return a connection whose type can work with the two possible
        // FIXME: drivers (ie: com.sun.star.sdbc.Driver or com.sun.star.sdbcx.Driver)
        return level;
    }

}
