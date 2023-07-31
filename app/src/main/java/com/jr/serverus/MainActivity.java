package com.jr.serverus;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.net.*;
import java.util.*;

public class MainActivity extends Activity 
{
	TextView mTextView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		
		ScrollView sv = new ScrollView(this);
		mTextView = (TextView) new TextView(this);
		mTextView.setGravity(Gravity.CENTER);
		sv.addView(mTextView);
		//mTextView.setTextIsSelectable(true);
        setContentView(sv);
		
		ServerService.startServer(this);
		mTextView.setText(ServerService.getIpAddress() +":"+ ServerService.PORT +"\n");
		
		
		/*int coun = 0;
		String[] projection = { MediaStore.Video.VideoColumns.DATA ,MediaStore.Video.Media.DISPLAY_NAME};
		String sort = MediaStore.Video.Media.DATE_ADDED;
		Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, sort);
		try {showLog(cursor.getCount()+"");
			cursor.moveToFirst();
			do{coun++;
				showLog(coun +": "+cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
			}while(cursor.moveToNext());

			cursor.close(); 
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
		
    }

	@Override
	protected void onDestroy()
	{
		ServerService.stopServer(this);
		super.onDestroy();
	}
	
	public void showLog(final String text)
	{
		runOnUiThread(new Runnable(){

				@Override
				public void run()
				{
					mTextView.setText(mTextView.getText() + text + "\n");
				}
			});
		
	}
	
	
	
}
