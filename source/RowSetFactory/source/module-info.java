module io.github.prrvchr.javarowset {
    provides javax.sql.rowset.RowSetFactory with io.github.prrvchr.java.rowset.RowSetFactoryImpl;

    requires transitive java.sql.rowset;
    requires transitive java.sql;
    requires java.base;
    requires java.xml;
    requires java.naming;

    exports io.github.prrvchr.java.rowset.providers;
    exports io.github.prrvchr.java.rowset;
}