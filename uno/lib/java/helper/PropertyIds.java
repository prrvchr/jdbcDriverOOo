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
package io.github.prrvchr.uno.helper;

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
    AGGREGATEFUNCTION (36, "Function"),
    TABLENAME (37, "TableName"),
    REALNAME (38, "RealName"),
    TABLETYPE (39, "Type"),
    ISCURRENCY (40, "IsCurrency"),
    ISBOOKMARKABLE (41, "IsBookmarkable"),
    HY010 (42, "HY010"),
    DELIMITER (43, "/"),
    FORMATKEY (44, "FormatKey"),
    LOCALE (45, "Locale"),
    AUTOINCREMENTCREATION (46, "AutoIncrementCreation"),
    PRIVILEGES (47, "Privileges"),
    HAVINGCLAUSE (48, "HavingClause"),
    ISSIGNED (49, "IsSigned"),
    ISSEARCHABLE (50, "IsSearchable"),
    LABEL (51, "Label"),
    APPLYFILTER (52, "ApplyFilter"),
    FILTER (53, "Filter"),
    MASTERFIELDS (54, "MasterFields"),
    DETAILFIELDS (55, "DetailFields"),
    FIELDTYPE (56, "FieldType"),
    VALUE (57, "Value"),
    ACTIVE_CONNECTION (58, "ActiveConnection"),

    ALIGN (59, "Align"),
    WIDTH (60, "Width"),
    POSITION (61, "Position"),
    HIDDEN (62, "Hidden"),
    ORDER(63, "Order"),
    FONTDESCRIPTOR (64, "FontDescriptor"),
    ROWHEIGHT (65, "RowHeight"),
    TEXTCOLOR (66, "TextColor"),
    GROUPBY (67, "GroupBy");
    
    PropertyIds(int id, String name) {
        this.id = id;
        this.name = name;
    }


    public final int id;
    public final String name;


}
