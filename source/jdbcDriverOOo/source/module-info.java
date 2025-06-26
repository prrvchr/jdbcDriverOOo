module io.github.prrvchr.jdbcdriver {
    requires transitive io.github.prrvchr.unohelper;
    requires java.xml;
    requires java.logging;
    requires java.sql;
    requires java.sql.rowset;
    requires io.github.prrvchr.unologger;
    requires io.github.prrvchr.javainstrumentation;
    requires io.github.prrvchr.javarowset;
    requires org.libreoffice.uno;

    exports io.github.prrvchr.uno.driver;
    exports io.github.prrvchr.uno.sdb;
    exports io.github.prrvchr.uno.sdbc;
    exports io.github.prrvchr.uno.sdbcx;

    uses java.sql.Driver;
}