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
package io.github.prrvchr.driver.query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.sdbcx.Privilege;

import io.github.prrvchr.driver.helper.DBTools;


public class DCLQuery extends DDLQuery {

    // java.sql.Statement DCL commands
    private static final String CREATE_USER_COMMAND = "CreateUserCommand";
    private static final String DROP_USER_COMMAND = "DropUserCommand";
    private static final String ALTER_USER_COMMAND = "AlterUserCommand";
    private static final String CREATE_ROLE_COMMAND = "CreateRoleCommand";
    private static final String DROP_ROLE_COMMAND = "DropRoleCommand";
    private static final String GRANT_ROLE_COMMAND = "GrantRoleCommand";
    private static final String REVOKE_ROLE_COMMAND = "RevokeRoleCommand";
    private static final String GRANT_PRIVILEGES_COMMAND = "GrantPrivilegesCommand";
    private static final String REVOKE_PRIVILEGES_COMMAND = "RevokePrivilegesCommand";

    // java.sql.PreparedStatement queries
    private static final String GET_USERS_QUERY = "GetUsersQuery";
    private static final String GET_GROUPS_QUERY = "GetGroupsQuery";
    private static final String GET_USER_GROUPS_QUERY = "GetUserGroupsQuery";
    private static final String GET_GROUP_USERS_QUERY = "GetGroupUsersQuery";
    private static final String GET_GROUP_ROLES_QUERY = "GetGroupRolesQuery";
    private static final String TABLE_PRIVILEGES_QUERY = "TablePrivilegesQuery";
    private static final String GRANTABLE_PRIVILEGES_QUERY = "GrantablePrivilegesQuery";

    private List<String> mPrivilegeNames = null;
    private List<Integer> mPrivilegeValues = null;

    // The constructor method:
    public DCLQuery(final XHierarchicalNameAccess config,
                    final PropertyValue[] infos,
                    final boolean generatedKeys,
                    final String subProtocol,
                    final String identifierQuote) throws SQLException {
        super(config, infos, generatedKeys, subProtocol, identifierQuote);
        if (mTablePrivileges != null) {
            mPrivilegeNames = new ArrayList<>();
            mPrivilegeValues  = new ArrayList<>();
            int count = DBTools.getEvenLength(mTablePrivileges.length);
            for (int i = 0; i < count; i += 2) {
                mPrivilegeNames.add((String) mTablePrivileges[i]);
                mPrivilegeValues.add(Integer.parseInt((String) mTablePrivileges[i + 1]));
            }
        } else {
            mPrivilegeNames = getDefaultPrivilegeNames();
            mPrivilegeValues = getDefaultPrivilegeValues();
        }
    }

    public boolean supportsCreateUser() {
        return  getCreateUserCommand() != null;
    }

    public String getCreateUserCommand(final String name, final String password) {
        String command = getCreateUserCommand();
        Map<String, Object> keys = Map.of("User", name,
                                          "Password", password);
        return format(command, keys);
    }

    public String getDropUserCommand(final String name) {
        String command = getDropUserCommand();
        Map<String, Object> keys = Map.of("User", name);
        return format(command, keys);
    }

    public String getAlterUserCommand(final String name, final String password) {
        String command = getAlterUserCommand();
        Map<String, Object> keys = Map.of("User", name,
                                          "Password", password);
        return format(command, keys);
    }

    public String getCreateRoleCommand(final String name) {
        String command = getCreateRoleCommand();
        Map<String, Object> keys = Map.of("Role", name);
        return format(command, keys);
    }

    public String getDropRoleCommand(final String name) {
        String command = getDropRoleCommand();
        Map<String, Object> keys = Map.of("Role", name);
        return format(command, keys);
    }

    public String getGrantRoleCommand(final String grantor, final String role, final String grantee) {
        Map<String, Object> keys = Map.of("Grantor", grantor,
                                          "Role", role,
                                          "Grantee", grantee);
        return getGrantRoleCommand(keys);
    }

    public String getGrantRoleCommand(Map<String, Object> keys) {
        String command = getGrantRoleCommand();
        return format(command, keys);
    }

    public String getRevokeRoleCommand(final String grantor, final String role, final String grantee) {
        Map<String, Object> keys = Map.of("Grantor", grantor,
                                          "Role", role,
                                          "Grantee", grantee);
        return getRevokeRoleCommand(keys);
    }

    public String getRevokeRoleCommand(Map<String, Object> keys) {
        String command = getRevokeRoleCommand();
        return format(command, keys);
    }

    public String getGrantPrivilegesCommand(final Map<String, Object> keys) {
        String command = getGrantPrivilegesCommand();
        return format(command, keys);
    }

    public String getRevokePrivilegesCommand(final Map<String, Object> keys) {
        String command = getRevokePrivilegesCommand();
        return format(command, keys);
    }

    public boolean supportsTablePrivileges() {
        return getTablePrivilegesQuery() != null;
    }

    public String getTablePrivilegesQuery(final Map<String, Object> parameters,
                                          List<Object> values) {
        String command = getTablePrivilegesQuery();
        return format(command, parameters, values, "?");
    }

    public boolean supportsGrantablePrivileges() {
        return getGrantablePrivilegesQuery() != null;
    }

    public String getGrantablePrivilegesQuery(final Map<String, Object> parameters,
                                              List<Object> values) {
        String command = getGrantablePrivilegesQuery();
        return format(command, parameters, values, "?");
    }

    public String getUsersQuery() {
        return getPropertyString(GET_USERS_QUERY);
    }

    public String getGroupsQuery() {
        return getPropertyString(GET_GROUPS_QUERY);
    }

    public String getRoleGroupsQuery(final String role,
                                     final boolean isrole,
                                     final List<Object> values) {
        String query = null;
        if (isrole) {
            query = getGroupRolesQuery(role, values);
        } else {
            query = getUserGroupsQuery(role, values);
        }
        return query;
    }

    public String getGroupUsersQuery(final String group, final List<Object> values) {
        String command = getGroupUsersQuery();
        Map<String, Object> keys = Map.of("Group", group);
        return format(command, keys, values, "?");
    }

    public boolean hasPrivilege(final String privilege) {
        return mPrivilegeNames.contains(privilege);
    }

    public String[] getPrivileges() {
        return mPrivilegeNames.toArray(new String[0]);
    }

    public int getPrivileges(final List<String> privileges) {
        int flags = 0;
        for (String privilege : privileges) {
            flags |= getPrivilege(privilege);
        }
        return flags;
    }

    public String[] getPrivileges(final int privilege) {
        List<String> flags = new ArrayList<>();
        for (int value: mPrivilegeValues) {
            if ((privilege & value) == value) {
                flags.add(mPrivilegeNames.get(mPrivilegeValues.indexOf(value)));
            }
        }
        return flags.toArray(new String[0]);
    }

    public int getPrivilege(final String privilege) {
        int flag = 0;
        if (mPrivilegeNames.contains(privilege)) {
            flag = mPrivilegeValues.get(mPrivilegeNames.indexOf(privilege));
        }
        return flag;
    }

    public int getMockPrivileges() {
        int privileges = 0;
        for (Integer value : mPrivilegeValues) {
            privileges += value;
        }
        return privileges;
    }

    private String getUserGroupsQuery(final String user, final List<Object> values) {
        String command = getUserGroupsQuery();
        Map<String, Object> keys = Map.of("User", user);
        return format(command, keys, values, "?");
    }

    private String getGroupRolesQuery(final String group, final List<Object> values) {
        String command = getGroupRolesQuery();
        Map<String, Object> keys = Map.of("Group", group);
        return format(command, keys, values, "?");
    }

    private List<String> getDefaultPrivilegeNames() {
        return List.of("SELECT",
                       "INSERT",
                       "UPDATE",
                       "DELETE",
                       "READ",
                       "CREATE",
                       "ALTER",
                       "REFERENCES",
                       "DROP");
    }

    private List<Integer> getDefaultPrivilegeValues() {
        return List.of(Privilege.SELECT,
                       Privilege.INSERT,
                       Privilege.UPDATE,
                       Privilege.DELETE,
                       Privilege.READ,
                       Privilege.CREATE,
                       Privilege.ALTER,
                       Privilege.REFERENCE,
                       Privilege.DROP);
    }

    private String getCreateUserCommand() {
        return getPropertyString(CREATE_USER_COMMAND);
    }

    private String getDropUserCommand() {
        return getPropertyString(DROP_USER_COMMAND);
    }

    private String getCreateRoleCommand() {
        return getPropertyString(CREATE_ROLE_COMMAND);
    }

    private String getDropRoleCommand() {
        return getPropertyString(DROP_ROLE_COMMAND);
    }

    private String getAlterUserCommand() {
        return getPropertyString(ALTER_USER_COMMAND);
    }

    private String getGrantRoleCommand() {
        return getPropertyString(GRANT_ROLE_COMMAND);
    }

    private String getRevokeRoleCommand() {
        return getPropertyString(REVOKE_ROLE_COMMAND);
    }

    private String getGrantPrivilegesCommand() {
        return getPropertyString(GRANT_PRIVILEGES_COMMAND);
    }

    private String getRevokePrivilegesCommand() {
        return getPropertyString(REVOKE_PRIVILEGES_COMMAND);
    }

    private String getUserGroupsQuery() {
        return getPropertyString(GET_USER_GROUPS_QUERY);
    }

    private String getGroupUsersQuery() {
        return getPropertyString(GET_GROUP_USERS_QUERY);
    }

    private String getGroupRolesQuery() {
        return getPropertyString(GET_GROUP_ROLES_QUERY);
    }

    private String getTablePrivilegesQuery() {
        return getPropertyString(TABLE_PRIVILEGES_QUERY);
    }

    private String getGrantablePrivilegesQuery() {
        return getPropertyString(GRANTABLE_PRIVILEGES_QUERY);
    }

}