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

import java.util.logging.Level;

import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.LoadOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.tools.utility.SchemaCrawlerUtility;
import us.fatehi.utility.LoggingConfig;
import io.github.prrvchr.uno.sdb.Connection;
import io.github.prrvchr.uno.sdbcx.TableContainer;


public final class SchemaCrawler

{
    public static Catalog getCatalog(java.sql.Connection connection)
    throws java.sql.SQLException
    {
        try {
            System.out.println("SchemaCrawler.getCatalog() 1");
            new LoggingConfig(Level.ALL);
            final LimitOptionsBuilder limit = LimitOptionsBuilder.builder()
                .includeSchemas(new IncludeAll())
                .includeTables(new IncludeAll());
            final LoadOptionsBuilder load = LoadOptionsBuilder.builder()
                .withSchemaInfoLevel(SchemaInfoLevelBuilder.standard());
            final SchemaCrawlerOptions options = SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                .withLimitOptions(limit.toOptions())
                .withLoadOptions(load.toOptions());
            System.out.println("SchemaCrawler.getCatalog() 2");
            //final SchemaRetrievalOptions retrieval = SchemaCrawlerUtility.matchSchemaRetrievalOptions(connection);
            //schemacrawler.crawl.SchemaCrawler crawler = new schemacrawler.crawl.SchemaCrawler(connection, retrieval, options);
            //final Catalog catalog = crawler.crawl();
            return SchemaCrawlerUtility.getCatalog(connection, options);
        } catch (java.lang.Exception e) {
            System.out.println("SchemaCrawler.getCatalog() 3 ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            System.out.println("SchemaCrawler.getCatalog() 4");

        }
        return null;
    }


    public static TableContainer getTables(DriverProvider provider,
                                           Connection connection)
    throws java.sql.SQLException
    {
        try {
            System.out.println("SchemaCrawler.getTables() 1");
            new LoggingConfig(Level.CONFIG);
            final LimitOptionsBuilder limit = LimitOptionsBuilder.builder()
                .includeSchemas(new IncludeAll())
                .includeTables(new IncludeAll());
            final LoadOptionsBuilder load = LoadOptionsBuilder.builder()
                .withSchemaInfoLevel(SchemaInfoLevelBuilder.standard());
            final SchemaCrawlerOptions options = SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                .withLimitOptions(limit.toOptions())
                .withLoadOptions(load.toOptions());
            System.out.println("SchemaCrawler.getTables() 2");
            final SchemaRetrievalOptions retrieval = SchemaCrawlerUtility.matchSchemaRetrievalOptions(connection.getProvider().getConnection());
            schemacrawler.crawl.SchemaCrawler crawler = new schemacrawler.crawl.SchemaCrawler(connection.getProvider().getConnection(), retrieval, options);
            final Catalog catalog = crawler.crawl();
            //final Catalog catalog = getCatalog(connection, options);
            System.out.println("SchemaCrawler.getTables() 3: " + catalog);
            //return new TableContainer(connection, catalog);
            return null;
        } catch (java.lang.Exception e) {
            System.out.println("SchemaCrawler.getTables() 4 ********************************* ERROR: " + e);
            for (StackTraceElement trace : e.getStackTrace())
            {
                System.out.println(trace);
            }
            System.out.println("SchemaCrawler.getTables() 5");

        }
        return null;
    }


}
