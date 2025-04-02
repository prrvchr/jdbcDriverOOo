module io.github.prrvchr.jdbcdriver {
    uses java.sql.Driver;

    requires java.logging;
    requires transitive io.github.prrvchr.unohelper;
    requires io.github.prrvchr.unologger;
    requires transitive org.libreoffice.uno;
    requires io.github.prrvchr.unoagent;
    requires java.xml;

    exports io.github.prrvchr.driver;
    exports io.github.prrvchr.uno.sdb;
    exports io.github.prrvchr.uno.sdbc;
    exports io.github.prrvchr.uno.sdbcx;
}