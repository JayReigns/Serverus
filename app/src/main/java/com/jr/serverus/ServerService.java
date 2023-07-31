package com.jr.serverus;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import android.content.Context;
import android.widget.Toast;

public class ServerService extends Service
{
	public static int PORT = 8080;
	
	PowerManager mPowerManager;
	WakeLock mWakeLock;
	Thread serverThread;
	
	public static void startServer(Context c){
		c.startService(new Intent(c, ServerService.class));
	}
	
	public static void stopServer(Context c){
		c.stopService(new Intent(c, ServerService.class));
	}
	
	public static String getIpAddress() { 
		String str = "";
		try{
			Enumeration networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				Enumeration inetAddresses = ((NetworkInterface) networkInterfaces.nextElement()).getInetAddresses(); 
				while (inetAddresses.hasMoreElements()) {
					InetAddress inetAddress = (InetAddress) inetAddresses.nextElement(); 
					if (inetAddress.isSiteLocalAddress()) {
						str = inetAddress.getHostAddress();
					}
				}
			}
		}catch(SocketException e){

		}
		return str;
	} 
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
		serverThread = new Thread(new Runnable(){

				@Override
				public void run()
				{
					try
					{
						mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "tag");
						mWakeLock.acquire();
						
						// blocking method, dont put anything after it
						Server.start(PORT, ServerService.this);
					}
					catch (Exception e)
					{slog(e.toString());
						ServerService.this.stopSelf();
					}
				}
			});
		serverThread.start();
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		// TODO: Implement this method
		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{try{
		if(serverThread != null) serverThread.interrupt();
		if(mWakeLock != null) mWakeLock.release();
	}catch(Exception e){
		slog(e.toString());
	}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent p1)
	{
		// TODO: Implement this method
		return null;
	}
	
	public void slog(String t){
		Toast.makeText(this, t, 0).show();
	}
}
