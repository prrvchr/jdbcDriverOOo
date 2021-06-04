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
package io.github.prrvchr.comp.helper;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Clob;

import com.sun.star.io.XInputStream;
import com.sun.star.lib.uno.adapter.XInputStreamToInputStreamAdapter;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XClob;


public class XClobToClobAdapter
implements java.sql.Clob
{
	private XClob m_xClob;

	// The constructor method:
	public XClobToClobAdapter(XClob clob)
	{
		m_xClob = clob;
	}

	// java.sql.Clob:
	@Override
	public void free()
	throws java.sql.SQLException
	{
		// TODO Auto-generated method stub
	}

	@Override
	public InputStream getAsciiStream()
	throws java.sql.SQLException
	{
		try
		{
			XInputStream input = m_xClob.getCharacterStream();
			return new XInputStreamToInputStreamAdapter(input);
		}
		catch (SQLException e)
		{
			throw new java.sql.SQLException(e.getMessage());
		}
	}

	@Override
	public Reader getCharacterStream()
	throws java.sql.SQLException
	{
		XInputStream input;
		try
		{
			input = m_xClob.getCharacterStream();
		}
		catch (SQLException e)
		{
			throw new java.sql.SQLException(e.getMessage());
		}
		InputStream i = new XInputStreamToInputStreamAdapter(input);
		return new java.io.InputStreamReader(i);
	}

	@Override
	public Reader getCharacterStream(long arg0, long arg1) throws java.sql.SQLException {
		XInputStream input;
		try
		{
			input = m_xClob.getCharacterStream();
		}
		catch (SQLException e)
		{
			throw new java.sql.SQLException(e.getMessage());
		}
		InputStream i = new XInputStreamToInputStreamAdapter(input);
		return new java.io.InputStreamReader(i);
	}

	@Override
	public String getSubString(long position, int lenght)
	throws java.sql.SQLException
	{
		try
		{
			return m_xClob.getSubString(position, lenght);
		}
		catch (SQLException e)
		{
			throw new java.sql.SQLException(e.getMessage());
		}
	}

	@Override
	public long length()
	throws java.sql.SQLException
	{
		try
		{
			return m_xClob.length();
		}
		catch (SQLException e)
		{
			throw new java.sql.SQLException(e.getMessage());
		}
	}

	@Override
	public long position(String pattern, long start)
	throws java.sql.SQLException
	{
		try
		{
			int s = (int) start;
			return m_xClob.position(pattern, s);
		}
		catch (SQLException e)
		{
			throw new java.sql.SQLException(e.getMessage());
		}
	}

	@Override
	public long position(Clob clob, long start) throws java.sql.SQLException {
		try
		{
			XClob c = new ClobToXClobAdapter(clob);
			return m_xClob.positionOfClob(c, start);
		}
		catch (SQLException e)
		{
			throw new java.sql.SQLException(e.getMessage());
		}
	}

	@Override
	public OutputStream setAsciiStream(long arg0) throws java.sql.SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Writer setCharacterStream(long arg0) throws java.sql.SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int setString(long arg0, String arg1) throws java.sql.SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int setString(long arg0, String arg1, int arg2, int arg3) throws java.sql.SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void truncate(long arg0) throws java.sql.SQLException {
		// TODO Auto-generated method stub
	}


}
