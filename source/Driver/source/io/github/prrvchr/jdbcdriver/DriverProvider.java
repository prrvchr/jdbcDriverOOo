package io.github.prrvchr.jdbcdriver;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import com.sun.star.beans.PropertyValue;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.XComponentContext;

import io.github.prrvchr.uno.sdbc.ConnectionBase;
import io.github.prrvchr.uno.sdbc.DatabaseMetaDataBase;
import io.github.prrvchr.uno.sdbc.ResultSetBase;
import io.github.prrvchr.uno.sdbc.StatementMain;

public interface DriverProvider
{

    static String m_protocol = "xdbc:";

    default String getProtocol()
    {
        return m_protocol;
    }
    default String getProtocol(String subprotocol)
    {
        return m_protocol + subprotocol;
    }

    public boolean acceptsURL(String url);

    public boolean supportWarningsSupplier();

    public java.sql.Connection getConnection(String url,
                                             PropertyValue[] info)
        throws SQLException;

    default Properties getConnectionProperties(List<String> list,
                                               PropertyValue[] info)
    {
        Properties properties = new Properties();
        for (PropertyValue property : info) {
            if (list.contains(property.Name))
            {
                System.out.println("DriverProvider.getConnectionProperties() 1 : " + property.Name + " - " + property.Value);
                properties.setProperty(property.Name, AnyConverter.toString(property.Value));
            }
        }
        return properties;
    }

    default void setSystemProperties()
    {
        // noop
    }


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
