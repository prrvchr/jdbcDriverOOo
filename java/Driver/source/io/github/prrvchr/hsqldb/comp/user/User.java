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
package io.github.prrvchr.hsqldb.comp.user;

import java.sql.Connection;
import java.util.Map;

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertyChangeListener;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.beans.XVetoableChangeListener;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbcx.XUser;

import io.github.prrvchr.hsqldb.comp.helper.ServiceHelper;


public class User extends ServiceHelper
implements XPropertySet,
           XUser
{
	private static final String m_name = User.class.getName();
	private static final String[] m_services = {"com.sun.star.sdbcx.User"};
	@SuppressWarnings("unused")
	private final java.sql.Connection m_Connection;
	@SuppressWarnings("unused")
	private Map<String, String> m_users;

	// The constructor method:
	public User(Connection connection)
	{
		super(m_name, m_services);
		m_Connection = connection;
	}


	// com.sun.star.sdbcx.XAuthorizable <- XUser:
	@Override
	public int getGrantablePrivileges(String arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getPrivileges(String arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void grantPrivileges(String arg0, int arg1, int arg2) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void revokePrivileges(String arg0, int arg1, int arg2) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	
	
	
	// com.sun.star.beans.XPropertySet:
	@Override
	public void addPropertyChangeListener(String arg0, XPropertyChangeListener arg1)
			throws UnknownPropertyException, WrappedTargetException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void addVetoableChangeListener(String arg0, XVetoableChangeListener arg1)
			throws UnknownPropertyException, WrappedTargetException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public XPropertySetInfo getPropertySetInfo() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Object getPropertyValue(String arg0) throws UnknownPropertyException, WrappedTargetException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void removePropertyChangeListener(String arg0, XPropertyChangeListener arg1)
			throws UnknownPropertyException, WrappedTargetException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void removeVetoableChangeListener(String arg0, XVetoableChangeListener arg1)
			throws UnknownPropertyException, WrappedTargetException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setPropertyValue(String arg0, Object arg1)
			throws UnknownPropertyException, PropertyVetoException, IllegalArgumentException, WrappedTargetException {
		// TODO Auto-generated method stub
		
	}


	// com.sun.star.sdbcx.XUser:
	@Override
	public void changePassword(String arg0, String arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}





}
