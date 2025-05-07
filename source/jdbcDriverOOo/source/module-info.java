module io.github.prrvchr.jdbcdriver {
    uses java.sql.Driver;

    requires java.xml;
    requires java.logging;
    requires transitive io.github.prrvchr.unohelper;
    requires io.github.prrvchr.unologger;
    requires io.github.prrvchr.unoagent;

    exports io.github.prrvchr.driver;
    exports io.github.prrvchr.uno.sdb;
    exports io.github.prrvchr.uno.sdbc;
    exports io.github.prrvchr.uno.sdbcx;
}