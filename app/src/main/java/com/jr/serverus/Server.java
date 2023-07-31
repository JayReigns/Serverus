package com.jr.serverus;

import android.content.*;
import java.io.*;
import java.net.*;
import android.graphics.*;
import android.util.*;
import org.json.*;
import java.util.*;
import android.os.*;
import android.net.*;
import com.jr.serverus.http.*;
import android.content.pm.*;

public class Server implements Runnable
{
	private Socket mSocket;
	private Context mContext;
	private IconManager mIconManager;
	private HTTP mHTTP;

	public static void start(final int PORT, Context context) throws IOException
	{
        ServerSocket socket = new ServerSocket(PORT);
		
		while (!Thread.interrupted()) {
			Server server = new Server(socket.accept(), context);
			new Thread(server).start();
		}
    }
	
	public Server(Socket socket, Context context)
	{
        mSocket = socket;
		mContext = context;
		mIconManager = new IconManager(mContext);
    }
	
	@Override
	public void run()
	{try{
		mHTTP = new HTTP(mSocket);
		HTTPRequest request = mHTTP.getRequest();
		
		switch (request.getMethod())
		{
			case "GET":
				handleGetMethod(request, true);
				break;
				
			case "HEAD":
				handleGetMethod(request, false);
				break;
				
			case "POST":
				handlePostMethod(request);
				break;
			default:
				log("unsupported method: "+request.getMethod());
				mHTTP.sendError(HTTP.HTTP_NOT_IMPLEMENTED);
				break;
		}
		}catch(Exception e){
			log("Thread: " +e.toString());
		}finally{
			try{
				mHTTP.close();
			}
			catch (IOException e){
				log("exception in closing");
			}
		}
	}
	
	
	
	public static String REQUEST_TYPE_JSON = "json";
	public static String REQUEST_TYPE_THUMB = "thumb";
	public static String REQUEST_TYPE_ASSET = "asset";
	public static String REQUEST_TYPE_DOWNLOAD = "download";
	
	private void handleGetMethod(HTTPRequest request, boolean sendBody) throws Exception
	{
		String type = request.getRequest(HTTPRequest.TYPE);
		
		switch (type)
		{
			case "json":
				sendJSON(request);
				break;

			case "thumb":
				sendThumb(request);
				break;

			case "asset":
				sendAsset(request);
				break;
				
			case "download":
				sendFile(request);
				break;
				
			case "default":
				String path = request.getRequest("p");
				if(path.equals(""))
					mHTTP.redirect("/asset?p=home.html");
				else
					mHTTP.redirectTemp("/asset?p="+path);
				break;
				
			default:
				log("unsupported type: "+type);
				mHTTP.sendError(HTTP.HTTP_NOT_FOUND);
				break;
		}
	}
	
	Comparator sortByName = new Comparator<File>(){
		@Override
		public int compare(File file1, File file2){
			return file1.getName().compareToIgnoreCase(file2.getName());
		}
	};
	Comparator sortBySize = new Comparator<File>(){
		@Override
		public int compare(File file1, File file2){
			return String.valueOf(file1.length()).compareToIgnoreCase(String.valueOf(file2.length()));
		}
	};
	Comparator sortByDate = new Comparator<File>(){
		@Override
		public int compare(File file1, File file2){
			return String.valueOf(file1.lastModified()).compareToIgnoreCase(String.valueOf(file2.lastModified()));
		}
	};
	Comparator sortByKind = new Comparator<File>(){
		@Override
		public int compare(File file1, File file2){
			return (file1.isDirectory() ? "a" : "b").compareToIgnoreCase(file2.isDirectory() ? "a" : "b");
		}
	};
	
	private void sendJSON(HTTPRequest request) throws IOException, JSONException
	{
		String request_path = request.getRequest("p");
		File file = null;
		
		if(request_path.startsWith("*")){
			switch (request_path.substring(1)){
				case "home" :
					file = Environment.getExternalStorageDirectory();
					break;
				case "dcim" :
					file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
					break;
				case "pictures" :
					file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
					break;
				case "downloads" :
					file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
					break;
			}
		}else{
			file = new File(request_path);
		}
		
		if(!file.exists() && !file.isDirectory()){
			mHTTP.sendError(HTTP.HTTP_NOT_FOUND);
			throw new IOException("json: "+request.getRequest("p")+" not found");
		}
		
		Comparator sortType = sortByName;
		
		String sort = request.getRequest("s");
		if(sort != null){
			if(sort.equals("name"))
				sortType = sortByName;
			else if(sort.equals("size"))
				sortType = sortBySize;
			else if(sort.equals("date"))
				sortType = sortByDate;
		}
		
		
		File[] fileList = file.listFiles();
		Arrays.sort(fileList, sortType);
		Arrays.sort(fileList, sortByKind);
		
		JSONObject data = new JSONObject();
		JSONArray list = new JSONArray();
		
		data.put("name", Uri.encode(file.getName()));
		data.put("parent", Uri.encode(file.getParent()));
		
		for(File f : fileList)
		{
			JSONObject item = new JSONObject();
			String name = f.getName();
			item.put("name", Uri.encode(name));
			if(f.isDirectory())
				item.put("type", "dir");
			else
				item.put("type", MimeType.getMimeFromName(name).split("/")[0]);
			list.put(item);
		}
		data.put("files", list);
		
		//String data = "<body><video autoplay controls src='/download?p="+file.getAbsolutePath()+"'/></body>";
		
		byte[] bytes = data.toString().getBytes();
		
		HTTPResponse response = mHTTP.getResponse();
		response.setStatus(HTTP.HTTP_STATUS_OK);
		response.setHeader(HTTPResponse.CONTENT_TYPE, "text/html");
		response.setHeader(HTTPResponse.CONTENT_LENGTH, String.valueOf(bytes.length));
		
		OutputStream out = response.getOutputStream();
		out.write(bytes);
		out.flush();
		out.close();
	}
	
	private void getAllApps() throws JSONException, IOException
	{
		PackageManager pm = mContext.getPackageManager();
		List<ApplicationInfo> apps = pm.getInstalledApplications(0);
		
		JSONObject data = new JSONObject();
		JSONArray list = new JSONArray();
		
		for(ApplicationInfo app : apps)
		{
			JSONObject item = new JSONObject();
			String name = (String) app.loadLabel(pm);
			item.put("name", Uri.encode(name));
			item.put("type", "apk");
			list.put(item);
		}
		data.put("files", list);

		//String data = "<body><video autoplay controls src='/download?p="+file.getAbsolutePath()+"'/></body>";

		byte[] bytes = data.toString().getBytes();

		HTTPResponse response = mHTTP.getResponse();
		response.setStatus(HTTP.HTTP_STATUS_OK);
		response.setHeader(HTTPResponse.CONTENT_TYPE, "text/html");
		response.setHeader(HTTPResponse.CONTENT_LENGTH, String.valueOf(bytes.length));

		OutputStream out = response.getOutputStream();
		out.write(bytes);
		out.flush();
		out.close();
		
	}
	
	private void sendThumb(HTTPRequest request) throws IOException
	{
		File file = new File(request.getRequest("p"));
		if(!file.exists()){
			mHTTP.sendError(HTTP.HTTP_NOT_FOUND);
			throw new IOException("Thumb: "+request.getRequest("p")+" not found");
		}
		mIconManager.sendIcon(file, mHTTP);
	}
	
	private void sendAsset(HTTPRequest request) throws IOException
	{
		String path = request.getRequest("p");
		try{
			mHTTP.sendStream( mContext.getAssets().open(path), MimeType.getMimeFromName(path));
		}catch(IOException e){
			throw new IOException("Asset: " + e.toString());
		}
	}
	
	private void sendFile(HTTPRequest request) throws Exception
	{
		File file = new File(request.getRequest("p"));
		if(!file.exists()){
			mHTTP.sendError(HTTP.HTTP_NOT_FOUND);
			throw new IOException("download: "+request.getRequest("p")+" not found");
		}
		
		mHTTP.sendFile(file);
	}
	
	private void handlePostMethod(HTTPRequest request)
	{

	}
	
	private void log(String text)
	{
		//((MainActivity) mContext).showLog(text);
	}
}
