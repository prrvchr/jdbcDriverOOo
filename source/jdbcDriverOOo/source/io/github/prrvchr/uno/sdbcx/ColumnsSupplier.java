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
package io.github.prrvchr.uno.sdbcx;

import java.util.ArrayList;
import java.util.List;

import com.sun.star.container.XNameAccess;
import com.sun.star.uno.TypeClass;

import schemacrawler.crawl.ResultsCrawler;
import schemacrawler.schema.ResultsColumn;


public final class ColumnsSupplier
{

    public static XNameAccess getColumns(java.sql.ResultSetMetaData metadata)
        throws java.sql.SQLException
    {
        String name = null;
        List<String> names = new ArrayList<String>();
        List<Column> columns = new ArrayList<Column>();
        for (int i = 1; i <= metadata.getColumnCount(); i++)
        {
            name = metadata.getColumnName(i);
            names.add(name);
            columns.add(new Column(metadata, i, name));
        }
        return new Container<Column>(columns, names, "com.sun.star.sdbcx.Column", TypeClass.SERVICE);
    }

    public static XNameAccess getColumns(java.sql.ResultSet resultset)
        throws java.sql.SQLException
    {
        String name = null;
        List<String> names = new ArrayList<String>();
        List<Column> columns = new ArrayList<Column>();
        ResultsCrawler crawler = new ResultsCrawler(resultset);
        for (ResultsColumn column : crawler.crawl())
        {
            name = column.getName();
            names.add(name);
            columns.add(new Column(column, name));
        }
        return new Container<Column>(columns, names, "com.sun.star.sdbcx.Column", TypeClass.SERVICE);
    }

    public static XNameAccess getColumns(ResultsCrawler result)
        throws java.sql.SQLException
    {
        String name = null;
        List<String> names = new ArrayList<String>();
        List<Column> columns = new ArrayList<Column>();
        for (ResultsColumn column : result.crawl())
        {
            name = column.getName();
            names.add(name);
            columns.add(new Column(column, name));
        }
        return new Container<Column>(columns, names, "com.sun.star.sdbcx.Column", TypeClass.SERVICE);
    }

}