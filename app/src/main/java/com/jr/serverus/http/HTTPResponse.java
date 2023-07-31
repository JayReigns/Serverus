package com.jr.serverus.http;

import java.io.*;

public class HTTPResponse
{
	public static String CONTENT_TYPE = "Content-type";
	public static String CONTENT_LENGTH = "Content-length";
	
	private OutputStream oStream;
	private PrintWriter printer;
	
	public HTTPResponse(OutputStream os)
	{
		oStream = os;
		printer = new PrintWriter(oStream);
		
	}
	
	public void setLine(String line)
	{
		printer.println(line);
	}
	
	public void setStatus(String status)
	{
		printer.println("HTTP/1.1 " + status);
	}
	
	public void setHeader(String type, String value)
	{
		printer.println(type + ": " + value);
	}
	
	public OutputStream getOutputStream()
	{
		printer.println();
		printer.flush();
		
		return oStream;
	}
	
	public void close()
	{
		printer.println();
		printer.flush();
		printer.close();
	}
}
