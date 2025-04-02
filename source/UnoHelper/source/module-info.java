module io.github.prrvchr.unohelper {
    requires transitive java.sql;
    requires transitive org.libreoffice.uno;

    exports io.github.prrvchr.uno.helper;
}