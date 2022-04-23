package io.github.prrvchr.jdbcdriver;

import com.sun.star.beans.PropertyValue;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;
import io.github.prrvchr.uno.sdbc.ResultSetBase;
import io.github.prrvchr.uno.sdbc.StatementMain;

public interface DriverProvider
{

    public boolean acceptsURL(String url);

    public boolean supportWarningsSupplier();

    public boolean supportProperty(String property);

    public DatabaseMetaDataBase getDatabaseMetaData(XComponentContext context,
                                                    ConnectionBase connection,
                                                    java.sql.DatabaseMetaData metadata,
                                                    PropertyValue[] info,
                                                    String url);

    public ResultSetBase getResultSet(XComponentContext context,
                                      java.sql.ResultSet resultset,
                                      PropertyValue[] info);

    public ResultSetBase getResultSet(XComponentContext context,
                                      StatementMain statement,
                                      java.sql.ResultSet resultset,
                                      PropertyValue[] info);


}
