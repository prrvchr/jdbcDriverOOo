/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-25 https://prrvchr.github.io                                  ║
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
package io.github.prrvchr.uno.driver.property;

// XXX: see: libreoffice/dbaccess/source/inc/stringconstants.hxx
public enum PropertyID {

    NAME("Name", 7),
    SCHEMANAME("SchemaName", 8),
    CATALOGNAME("CatalogName", 9),
    PRIVILEGES("Privileges", 10),
    ESCAPEPROCESSING("EscapeProcessing", 11),
    COMMAND("Command", 12),
    TYPE("Type", 13),
    TYPENAME("TypeName", 14),
    PRECISION("Precision", 15),
    SCALE("Scale", 16),
    ISNULLABLE("IsNullable", 17),
    ISAUTOINCREMENT("IsAutoIncrement", 18),
    ISROWVERSION("IsRowVersion", 19),
    DESCRIPTION("Description", 20),
    DEFAULTVALUE("DefaultValue", 21),
    NUMBERFORMAT("NumberFormat", 22),
    QUERYTIMEOUT("QueryTimeOut", 23),
    MAXFIELDSIZE("MaxFieldSize", 24),
    MAXROWS("MaxRows", 25),
    CURSORNAME("CursorName", 26),
    RESULTSETCONCURRENCY("ResultSetConcurrency", 27),
    RESULTSETTYPE("ResultSetType", 28),
    FETCHDIRECTION("FetchDirection", 29),
    FETCHSIZE("FetchSize", 30),
    USEBOOKMARKS("UseBookmarks", 31),
    ISSEARCHABLE("IsSearchable", 32),
    ISCURRENCY("IsCurrency", 33),
    ISSIGNED("IsSigned", 34),
    DISPLAYSIZE("DisplaySize", 35),
    LABEL("Label", 36),
    ISREADONLY("IsReadOnly", 37),
    ISWRITABLE("IsWritable", 38),
    ISDEFINITELYWRITABLE("IsDefinitelyWritable", 39),
    VALUE("Value", 40),
    TABLENAME("TableName", 41),
    ISCASESENSITIVE("IsCaseSensitive", 42),
    SERVICENAME("ServiceName", 43),
    ISBOOKMARKABLE("IsBookmarkable", 44),
    CANUPDATEINSERTEDROWS("CanUpdateInsertedRows", 45),
    NUMBERFORMATSSUPPLIER("NumberFormatSupplier", 48),
    DATASOURCENAME("DataSourceName", 50),
    TRANSACTIONISOLATION("TransactionIsolation", 51),
    TYPEMAP("TypeMap", 52),
    USER("User", 53),
    PASSWORD("Password", 54),
    COMMANDTYPE("CommandType", 55),
    ACTIVECOMMAND("ActiveCommand", 56),
    ACTIVE_CONNECTION("ActiveConnection", 57),
    FILTER("Filter", 58),
    APPLYFILTER("ApplyFilter", 59),
    ORDER("Order", 60),
    ISMODIFIED("IsModified", 61),
    ISNEW("IsNew", 62),
    ROWCOUNT("RowCount", 63),
    ISROWCOUNTFINAL("IsRowCountFinal", 64),
    REALNAME("RealName", 66),
    HIDDEN("Hidden", 67),
    ALIGN("Align", 68),
    WIDTH("Width", 69),
    TABLETYPEFILTER("TableTypeFilter", 70),
    FONT("Font", 72),
    ROWHEIGHT("RowHeight", 73),
    TEXTCOLOR("TextColor", 74),
    UPDATE_TABLENAME("UpdateTableName", 75),
    UPDATE_SCHEMANAME("UpdateSchemaName", 76),
    UPDATE_CATALOGNAME("UpdateCatalogName", 77),
    CONTROLMODEL("ControlModel", 78),
    RELATIVEPOSITION("RelativePosition", 79),
    ISASCENDING("IsAscending", 80),
    RELATEDCOLUMN("RelatedColumn", 81),
    ISUNIQUE("IsUnique", 82),
    ISPRIMARYKEYINDEX("IsPrimaryKeyIndex", 83),
    IGNORERESULT("IgnoreResult", 84),
    DELETERULE("DeleteRule", 85),
    UPDATERULE("UpdateRule", 86),
    REFERENCEDTABLE("ReferencedTable", 87),
    SQLEXCEPTION("SQLException", 90),
    SUPPRESSVERSIONCL("SuppressVersionColumn", 92),
    LAYOUTINFORMATION("LayoutInformation", 93),

    HELPTEXT("HelpText", 97),
    CONTROLDEFAULT("ControlDefault", 98),
    AUTOINCREMENTCREATION("AutoIncrementCreation", 99),

    HAVINGCLAUSE("HavingClause", 126),
    GROUPBY("GroupBy", 127),

    CATALOG("Catalog", 150),
    ISCLUSTERED("IsClustered", 151),
    CHECKOPTION("CheckOption", 152),
    FORMATKEY("FormatKey", 153),
    POSITION("Position", 154),
    FONTDESCRIPTOR("FontDescriptor", 155);

    private final String mName;
    private final int mId;

    private PropertyID(final String name, final int id) {
        mName = name;
        mId = id;
    }

    public final String getName() {
        return mName;
    }

    public final int getId() {
        return mId;
    }

}
