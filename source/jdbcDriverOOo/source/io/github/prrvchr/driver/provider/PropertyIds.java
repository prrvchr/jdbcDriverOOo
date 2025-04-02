/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-24 https://prrvchr.github.io                                  ║
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
package io.github.prrvchr.driver.provider;

public enum PropertyIds {

    QUERYTIMEOUT("QueryTimeOut"),
    MAXFIELDSIZE("MaxFieldSize"),
    MAXROWS("MaxRows"),
    CURSORNAME("CursorName"),
    RESULTSETCONCURRENCY("ResultSetConcurrency"),
    RESULTSETTYPE("ResultSetType"),
    FETCHDIRECTION("FetchDirection"),
    FETCHSIZE("FetchSize"),
    ESCAPEPROCESSING("EscapeProcessing"),
    USEBOOKMARKS("UseBookmarks"),
    NAME("Name"),
    TYPE("Type"),
    TYPENAME("TypeName"),
    PRECISION("Precision"),
    SCALE("Scale"),
    ISNULLABLE("IsNullable"),
    ISAUTOINCREMENT("IsAutoIncrement"),
    ISROWVERSION("IsRowVersion"),
    DESCRIPTION("Description"),
    DEFAULTVALUE("DefaultValue"),
    REFERENCEDTABLE("ReferencedTable"),
    UPDATERULE("UpdateRule"),
    DELETERULE("DeleteRule"),
    CATALOG("Catalog"),
    ISUNIQUE("IsUnique"),
    ISPRIMARYKEYINDEX("IsPrimaryKeyIndex"),
    ISCLUSTERED("IsClustered"),
    ISASCENDING("IsAscending"),
    SCHEMANAME("SchemaName"),
    CATALOGNAME("CatalogName"),
    COMMAND("Command"),
    CHECKOPTION("CheckOption"),
    PASSWORD("Password"),
    RELATEDCOLUMN("RelatedColumn"),
    FUNCTION("Function"),
    AGGREGATEFUNCTION("AggregateFunction"),
    TABLENAME("TableName"),
    REALNAME("RealName"),
    ISCURRENCY("IsCurrency"),
    ISBOOKMARKABLE("IsBookmarkable"),
    HY010("HY010"),
    DELIMITER("/"),
    FORMATKEY("FormatKey"),
    LOCALE("Locale"),
    AUTOINCREMENTCREATION("AutoIncrementCreation"),
    PRIVILEGES("Privileges"),
    HAVINGCLAUSE("HavingClause"),
    ISSIGNED("IsSigned"),
    ISSEARCHABLE("IsSearchable"),
    LABEL("Label"),
    APPLYFILTER("ApplyFilter"),
    FILTER("Filter"),
    MASTERFIELDS("MasterFields"),
    DETAILFIELDS("DetailFields"),
    FIELDTYPE("FieldType"),
    VALUE("Value"),
    ACTIVE_CONNECTION("ActiveConnection"),
    ALIGN("Align"),
    WIDTH("Width"),
    POSITION("Position"),
    HIDDEN("Hidden"),
    ORDER("Order"),
    FONTDESCRIPTOR("FontDescriptor"),
    ROWHEIGHT("RowHeight"),
    TEXTCOLOR("TextColor"),
    GROUPBY("GroupBy"),
    RELATIVEPOSITION("RelativePosition"),
    CONTROLMODEL("ControlModel"),
    HELPTEXT("HelpText"),
    CONTROLDEFAULT("ControlDefault"),
    CANUPDATEINSERTEDROWS("CanUpdateInsertedRows");

    private final String mName;

    private PropertyIds(final String name) {
        mName = name;
    }

    public final String getName() {
        return mName;
    }

}
