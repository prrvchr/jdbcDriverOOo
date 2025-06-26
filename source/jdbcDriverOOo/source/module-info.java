module io.github.prrvchr.jdbcdriver {
    requires java.xml;
    requires java.logging;
    requires java.sql;
    requires java.sql.rowset;
    requires transitive io.github.prrvchr.unohelper;
    requires io.github.prrvchr.unologger;
    requires io.github.prrvchr.java.instrument;
    requires io.github.prrvchr.rowsetprovider;

    exports io.github.prrvchr.uno.driver;
    exports io.github.prrvchr.uno.sdb;
    exports io.github.prrvchr.uno.sdbc;
    exports io.github.prrvchr.uno.sdbcx;

    uses java.sql.Driver;
}