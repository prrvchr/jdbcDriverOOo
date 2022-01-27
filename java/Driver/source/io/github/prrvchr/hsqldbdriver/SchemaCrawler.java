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
package io.github.prrvchr.hsqldbdriver;

import java.util.ArrayList;
import java.util.List;

import com.sun.star.container.XNameAccess;

import io.github.prrvchr.hsqldbdriver.sdbcx.Container;
import io.github.prrvchr.hsqldbdriver.sdbcx.Table;

import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.LoadOptions;
import schemacrawler.schemacrawler.LoadOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.tools.utility.SchemaCrawlerUtility;


public final class SchemaCrawler

{
	public static XNameAccess getTables(java.sql.Connection connection)
	throws java.sql.SQLException
	{
		try {
			System.out.println("SchemaCrawler.getTables() 1");
			List<String> names = new ArrayList<String>();
			List<Table> tables = new ArrayList<Table>();
			System.out.println("SchemaCrawler.getTables() 2");

			//final LimitOptionsBuilder limit = LimitOptionsBuilder.builder();
			//SchemaInfoLevel level = SchemaInfoLevelBuilder.builder()
			//	.setRetrieveUserDefinedColumnDataTypes(false)
			//	.toOptions();
			LoadOptions load = LoadOptionsBuilder.builder()
				.withSchemaInfoLevel(SchemaInfoLevelBuilder.minimum())
				.toOptions();

			//DatabaseServerType type = new DatabaseServerType("hsqldb", "HyperSQL DataBase");
			//final LimitOptions limit = LimitOptionsBuilder.builder().toOptions();
			//final LoadOptions load = LoadOptionsBuilder.builder().toOptions();
			System.out.println("SchemaCrawler.getTables() 3");
			final SchemaCrawlerOptions options = SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
				.withLoadOptions(load);

			System.out.println("SchemaCrawler.getTables() 4");
			//InformationSchemaViews info = InformationSchemaViewsBuilder.builder().fromResourceFolder("/hsqldb.information_schema").toOptions();
			//SchemaRetrievalOptions retrieval = SchemaRetrievalOptionsBuilder.builder()
			//	.fromConnnection(connection)
			//	.withInformationSchemaViews(info)
			//	.withDatabaseServerType(new DatabaseServerType("hsqldb", "HyperSQL DataBase"))
			//	.toOptions();

			final Catalog catalog;
			//SchemaRetrievalOptions retrieval = SchemaCrawlerUtility.matchSchemaRetrievalOptions(connection);
			System.out.println("SchemaCrawler.getTables() 5: " + connection);

			catalog = SchemaCrawlerUtility.getCatalog(connection, options);
			//catalog = SchemaCrawlerUtility.getCatalog(connection, retrieval, options, new Config());
			System.out.println("SchemaCrawler.getTables() 6");
			for (final schemacrawler.schema.Table t : catalog.getTables())
			{
				System.out.println("SchemaCrawler.getTables() 7");
				Table table = new Table(t);
				tables.add(table);
				names.add(table.getName());
				System.out.println("SchemaCrawler.getTables() 8: " + table.getName());
			}
			connection.close();
			System.out.println("SchemaCrawler.getTables() 9");
			return new Container<Table>(tables, names);
		} catch (java.lang.Exception e) {
			e.getStackTrace();
		}
		return null;
	}


}
