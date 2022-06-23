/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020 https://prrvchr.github.io                                     ║
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
package io.github.prrvchr.uno.sdbc;

public enum PropertyIds {
    // Statement
    QUERYTIMEOUT(1, "QueryTimeOut"),
    MAXFIELDSIZE(2, "MaxFieldSize"),
    MAXROWS(3, "MaxRows"),
    CURSORNAME(4, "CursorName"),
    RESULTSETCONCURRENCY(5, "ResultSetConcurrency"),
    RESULTSETTYPE(6, "ResultSetType"),
    FETCHDIRECTION(7, "FetchDirection"),
    FETCHSIZE(8, "FetchSize"),
    ESCAPEPROCESSING(9, "EscapeProcessing"),
    USEBOOKMARKS (10, "UseBookmarks"),
    // Column
    NAME (11, "Name"),
    TYPE (12, "Type"),
    TYPENAME (13, "TypeName"),
    PRECISION (14, "Precision"),
    SCALE (15, "Scale"),
    ISNULLABLE (16, "IsNullable"),
    ISAUTOINCREMENT (17, "IsAutoIncrement"),
    ISROWVERSION (18, "IsRowVersion"),
    DESCRIPTION (19, "Description"),
    DEFAULTVALUE (20, "DefaultValue"),
    REFERENCEDTABLE (21, "ReferencedTable"),
    UPDATERULE (22, "UpdateRule"),
    DELETERULE (23, "DeleteRule"),
    CATALOG (24, "Catalog"),
    ISUNIQUE (25, "IsUnique"),
    ISPRIMARYKEYINDEX (26, "IsPrimaryKeyIndex"),
    ISCLUSTERED (27, "IsClustered"),
    ISASCENDING (28, "IsAscending"),
    SCHEMANAME (29, "SchemaName"),
    CATALOGNAME (30, "CatalogName"),
    COMMAND (31, "Command"),
    CHECKOPTION (32, "CheckOption"),
    PASSWORD (33, "Password"),
    RELATEDCOLUMN (34, "RelatedColumn"),
    FUNCTION (35, "Function"),
    TABLENAME (36, "TableName"),
    REALNAME (37, "RealName"),
    DBASEPRECISIONCHANGED (38, "DbasePrecisionChanged"),
    ISCURRENCY (39, "IsCurrency"),
    ISBOOKMARKABLE (40, "IsBookmarkable"),
    INVALID_INDEX (41, ""),
    HY010 (43, "HY010"),
    LABEL (44, "Label"),
    DELIMITER (45, "/"),
    FORMATKEY (46, "FormatKey"),
    LOCALE (47, "Locale"),
    IM001 (48, ""),
    AUTOINCREMENTCREATION (49, "AutoIncrementCreation"),
    PRIVILEGES (50, "Privileges"),
    HAVINGCLAUSE (51, "HavingClause"),
    ISSIGNED (52, "IsSigned"),
    AGGREGATEFUNCTION (53, "AggregateFunction"),
    ISSEARCHABLE (54, "IsSearchable"),
    APPLYFILTER (55, "ApplyFilter"),
    FILTER (56, "Filter"),
    MASTERFIELDS (57, "MasterFields"),
    DETAILFIELDS (58, "DetailFields"),
    FIELDTYPE (59, "FieldType"),
    VALUE (60, "Value"),
    ACTIVE_CONNECTION (61, "ActiveConnection"),

    ALIGN (62, "Align"),
    WIDTH (63, "Width"),
    POSITION (64, "Position"),
    HIDDEN (65, "Hidden"),
    ORDER(66, "Order"),
    FONTDESCRIPTOR (67, "FontDescriptor"),
    ROWHEIGHT (68, "RowHeight"),
    TEXTCOLOR (69, "TextColor"),
    GROUPBY (70, "GroupBy");
    
    PropertyIds(int id, String name) {
        this.id = id;
        this.name = name;
    }


    public final int id;
    public final String name;


}
