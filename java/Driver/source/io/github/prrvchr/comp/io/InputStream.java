package io.github.prrvchr.comp.io;

import com.sun.star.io.BufferSizeExceededException;
import com.sun.star.io.IOException;
import com.sun.star.io.NotConnectedException;
import com.sun.star.lib.uno.helper.WeakBase;

public class InputStream
extends WeakBase
implements com.sun.star.io.XInputStream
{

	private java.io.InputStream m_Input;
	public InputStream(java.io.InputStream input)
	{
		m_Input = input;
	}


	// com.sun.star.io.XInputStream:
	@Override
	public int available()
	throws NotConnectedException, IOException
	{
		try
		{
			return m_Input.available();
		}
		catch (java.io.IOException e)
		{
			throw new IOException();
		}
	}

	@Override
	public void closeInput()
	throws NotConnectedException, IOException
	{
		try
		{
			m_Input.close();
		}
		catch (java.io.IOException e)
		{
			throw new IOException();
		}
	}

	@Override
	public int readBytes(byte[][] arg0, int arg1)
	throws NotConnectedException, BufferSizeExceededException, IOException
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int readSomeBytes(byte[][] arg0, int arg1)
	throws NotConnectedException, BufferSizeExceededException, IOException
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void skipBytes(int arg0)
	throws NotConnectedException, BufferSizeExceededException, IOException
	{
		// TODO Auto-generated method stub
		
	}


}

