package com.jr.serverus.http;

import java.io.*;
import java.net.*;
import java.util.*;
import android.util.*;

public class HTTPRequest
{
	/*
	 example of header from chrome

	 GET /search HTTP/1.1
	 Host: localhost:8080
	 Connection: keep-alive
	 Save-Data: on
	 Upgrade-Insecure-Requests: 1
	 User-Agent: Mozilla/5.0 (Linux; Android 9; Mi A1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.79 Mobile Safari/537.36
	 Sec-Fetch-User: ?1
	 Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng;q=0.8,application/signed-exchange;v=b3;q=0.9
	 Sec-Fetch-Site: cross-site
	 Sec-Fetch-Mode: navigate
	 Accept-Encoding: gzip, deflate, br
	 Accept-Language: en-US,en;q=0.9
	 */
	/*
	 example of url
	 https://www.google.com/search?q=j&oq=j&aqs=chrome..69i57j
	 */
	 
	public static String TYPE = "type";
	
	private DataInputStream dataIn;
	
	private String[] requestLine;
	
	private HashMap<String, String> requestsMap = new HashMap<String, String>();
	private HashMap<String, String> headersMap = new HashMap<String, String>();


	public HTTPRequest(InputStream is) throws IOException
	{
		dataIn = new DataInputStream(is);
		String line = null;
		
		while(true){
			line = dataIn.readLine();
			if(line != null)
				break;
		}
		
		requestLine = line.split(" ");
		
		readRequest();
		readHeaders();
	}
	
	private void readHeaders() throws IOException
	{
		String line;
		while((line = dataIn.readLine()) != "" && line != null)
		{
			int idx = line.indexOf(": ");
			if(idx == -1)
				break;
			headersMap.put( line.substring(0, idx), line.substring(idx +2));
		}
	}
	
	private void readRequest()
	{
		String request = requestLine[1].substring(1);
		int idx = request.indexOf("?");
		if(idx == -1){
			requestsMap.put(TYPE, "default");
			requestsMap.put("p", URLDecoder.decode(request));
		}else{
			requestsMap.put(TYPE, URLDecoder.decode(request.substring(0,idx)));
			String[] attrs = request.substring(idx+1).split("&");
			for( String attr : attrs)
			{
				String[] split = attr.split("=");
				requestsMap.put(split[0], URLDecoder.decode(split[1]));
			}
		}
	}
	
	public String getMethod()
	{
		return requestLine[0];
	}

	public String getURL()
	{
		return requestLine[1];
	}
	
	public String getHTTPVersion()
	{
		return requestLine[2];
	}
	
	public String getRequest(String type)
	{
		return requestsMap.get(type);
	}
	
	public boolean containsHeader(String type)
	{
		return headersMap.containsKey(type);
	}
	
	public String getHeader(String type)
	{
		return headersMap.get(type);
	}

	public String getAllHeaders()
	{
		return headersMap.toString();
	}
	
}
