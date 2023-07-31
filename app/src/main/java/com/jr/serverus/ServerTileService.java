package com.jr.serverus;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

public class ServerTileService extends TileService
{

	@Override
	public void onStartListening()
	{
		super.onStartListening();
		Tile tile = getQsTile();
		if(isServiceRunning(ServerService.class)){
			tile.setLabel(ServerService.getIpAddress() + ":" + ServerService.PORT);
			tile.setState(Tile.STATE_ACTIVE);
		}else{
			tile.setLabel("Start");
			tile.setState(Tile.STATE_INACTIVE);
		}
		tile.updateTile();
	}

	@Override
	public void onTileAdded()
	{
		super.onTileAdded();
		Tile tile = getQsTile();
		tile.setState(Tile.STATE_INACTIVE);
		tile.updateTile();
	}

	@Override
	public void onClick()
	{
		super.onClick();
		
		Tile tile = getQsTile();
		boolean isRunning = isServiceRunning(ServerService.class);
		if(!isRunning){
			ServerService.startServer(this);
			tile.setLabel(ServerService.getIpAddress() + ":" + ServerService.PORT);
			tile.setState(Tile.STATE_ACTIVE);
		}else{
			ServerService.stopServer(this);
			tile.setLabel("Start");
			tile.setState(Tile.STATE_INACTIVE);
		}
		tile.updateTile();
	}
	
	private boolean isServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
	
}
