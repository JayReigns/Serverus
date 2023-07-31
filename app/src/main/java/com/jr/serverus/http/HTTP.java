package com.jr.serverus.http;

import android.util.Log;
import com.jr.serverus.MimeType;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class HTTP
{
	public static String HTTP_STATUS_OK = "200 OK";
	public static String HTTP_STATUS_PARTIAL = "206 Partial Content";
	public static String HTTP_ERROR_RANGE = "416 Range Not Satisfiable";
	public static String HTTP_REDIRECT = "301 Moved Permanently";
	public static String HTTP_TEMP_REDIRECT = "307 Temporary Redirect";
	public static String HTTP_NOT_FOUND = "404 Not Found";
	public static String HTTP_NOT_IMPLEMENTED = "501 Not Implemented";
	
	private Socket mSocket;
	
	private InputStream mInputStream;
	private OutputStream mOutputStream;
	
	private HTTPRequest mHTTPRequest;
	private HTTPResponse mHTTPResponse;
	
	public HTTP(Socket socket) throws IOException
	{
		mSocket = socket;
		
		mInputStream = mSocket.getInputStream();
		mOutputStream = mSocket.getOutputStream();
		
		mHTTPRequest = new HTTPRequest(mInputStream);
		mHTTPResponse = new HTTPResponse(mOutputStream);
	}
	
	public void close() throws IOException
	{
		mInputStream.close();
		mOutputStream.close();
		mSocket.close();
	}
	
	public void closeStream() throws IOException
	{
		mInputStream.close();
		mOutputStream.close();
	}
	
	public HTTPRequest getRequest()
	{
		return mHTTPRequest;
	}
	
	public HTTPResponse getResponse()
	{
		return mHTTPResponse;
	}
	
	/* simplify */

	public void sendError(String status)
	{
		mHTTPResponse.setStatus(status);
		mHTTPResponse.close();
	}
	
	public OutputStream getOutputStream(String mime){
        return getOutputStream(HTTP_STATUS_OK, -1, mime);
    }

    public OutputStream getOutputStream(long length, String mime){
        return getOutputStream(HTTP_STATUS_OK, length, mime);
    }

    public OutputStream getOutputStream(String status, long length, String mime)
	{
		mHTTPResponse.setStatus(status);
		mHTTPResponse.setHeader(HTTPResponse.CONTENT_TYPE, mime);
		if(length != -1) mHTTPResponse.setHeader(HTTPResponse.CONTENT_LENGTH, String.valueOf(length));
		
		return mHTTPResponse.getOutputStream();
    }
	
	/* redirect */
	
	public void redirect(String path)
	{
		mHTTPResponse.setStatus(HTTP_REDIRECT);
		mHTTPResponse.setHeader("Location", path);
		mHTTPResponse.close();
	}
	
	public void redirectTemp(String path)
	{
		mHTTPResponse.setStatus(HTTP_TEMP_REDIRECT);
		mHTTPResponse.setHeader("Location", path);
		mHTTPResponse.close();
	}
	
	/* methods for sending data */
	
	public void sendStream(InputStream stream, String mime) throws IOException
	{
		int size = stream.available();

		mHTTPResponse.setStatus(HTTP_STATUS_OK);
		mHTTPResponse.setHeader(HTTPResponse.CONTENT_TYPE, mime);
		mHTTPResponse.setHeader(HTTPResponse.CONTENT_LENGTH, String.valueOf(size));
		
		OutputStream out = mHTTPResponse.getOutputStream();
		byte[] BUFFER = new byte[size];
		
		try{
			int read = stream.read(BUFFER);
			out.write(BUFFER, 0, read);
			out.flush();
		}catch(IOException e){
			throw new IOException("Stream: " + e.toString());
		}finally{
			out.close();
			stream.close();
		}
	}
	
	public void sendFile(File file) throws IOException
	{
		String fileName = file.getName();
		boolean partial = mHTTPRequest.containsHeader("Range");
		long start, end;
		String rnge = "";
		if(partial)
		{
			rnge = mHTTPRequest.getHeader("Range");Log.e("range", rnge);
			String[] range = rnge.split("=")[1].split("-");
			start = Long.decode(range[0]);
			if(range.length > 1)
				end = Long.decode(range[1]) +1;
			else
				end = file.length();
		}else{
			start = 0;
			end = file.length();
		}
		
		if(partial)
			mHTTPResponse.setStatus(HTTP_STATUS_PARTIAL);
		else
			mHTTPResponse.setStatus(HTTP_STATUS_OK);
		
		mHTTPResponse.setHeader("Accept-Ranges", "bytes");
		mHTTPResponse.setHeader(HTTPResponse.CONTENT_TYPE, MimeType.getMimeFromName(fileName));
		
		if(partial)
		{
			mHTTPResponse.setHeader(HTTPResponse.CONTENT_LENGTH, String.valueOf(end - start));
			mHTTPResponse.setHeader("Content-Range", "bytes "+ start + "-" + (end -1) + "/" + file.length());
		}else
		{
			mHTTPResponse.setHeader("content-disposition", "attachment; filename=" + fileName);
			mHTTPResponse.setHeader(HTTPResponse.CONTENT_LENGTH, String.valueOf(file.length()));
		}
		writeFile(mHTTPResponse.getOutputStream(), file, start, end);
	}
	
	public void sendDirectory(File file) throws IOException
	{
		final int BUFFER = 2048;

		/*File sourceFile = new File(sourcePath);
		
			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream(toLocation);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
														  dest));
			if (sourceFile.isDirectory()) {
				zipSubFolder(out, sourceFile, sourceFile.getParent().length());
			} else {
				byte data[] = new byte[BUFFER];
				FileInputStream fi = new FileInputStream(sourcePath);
				origin = new BufferedInputStream(fi, BUFFER);
				ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath));
				entry.setTime(sourceFile.lastModified()); // to keep modification time after unzipping
				out.putNextEntry(entry);
				int count;
				while ((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}
			}
			out.close();*/
		
	}
	
	private void zipSubFolder(ZipOutputStream out, File folder,
							  int basePathLength) throws IOException {

		final int BUFFER = 2048;

		File[] fileList = folder.listFiles();
		BufferedInputStream origin = null;
		for (File file : fileList) {
			if (file.isDirectory()) {
				zipSubFolder(out, file, basePathLength);
			} else {
				byte data[] = new byte[BUFFER];
				String unmodifiedFilePath = file.getPath();
				String relativePath = unmodifiedFilePath
                    .substring(basePathLength);
				FileInputStream fi = new FileInputStream(unmodifiedFilePath);
				origin = new BufferedInputStream(fi, BUFFER);
				ZipEntry entry = new ZipEntry(relativePath);
				entry.setTime(file.lastModified()); // to keep modification time after unzipping
				out.putNextEntry(entry);
				int count;
				while ((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}
				origin.close();
			}
		}
	}

	/*
	 * gets the last path component
	 * 
	 * Example: getLastPathComponent("downloads/example/fileToZip");
	 * Result: "fileToZip"
	 */
	public String getLastPathComponent(String filePath) {
		String[] segments = filePath.split("/");
		if (segments.length == 0)
			return "";
		String lastPathComponent = segments[segments.length - 1];
		return lastPathComponent;
	}
	
	public void writeFile(OutputStream out , File file, long start, long end) throws IOException 
	{
		byte[] BUFFER = new byte[1024*1];
		RandomAccessFile randmFile = new RandomAccessFile(file, "r");
		try{
			while (true)
			{
				if(start >= end)
				{
					out.flush();
					out.close();
					randmFile.close();
					return;
				}

				randmFile.seek(start);

				int read = randmFile.read(BUFFER);
				if(start + read > end)
					read = (int)(end - start);

				out.write(BUFFER, 0, read);
				start += read;Log.e("vid",start+ "   "+end);
			}
		}catch(IOException e){
			throw new IOException("File: "+e.toString());
		}finally{
			try{
				randmFile.close();
				out.close();
			}catch(Exception e){}
		}
    }
	
}
