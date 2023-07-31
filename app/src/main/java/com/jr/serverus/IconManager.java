package com.jr.serverus;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import java.io.File;
import java.io.*;
import android.content.pm.*;
import com.jr.serverus.http.*;

public class IconManager
{
    private int THUMBSIZE = 180;
    private Context context;

    public IconManager(Context context) {
        Context context2 = context;
        this.context = context2;
    }

	public void sendIcon(File file, HTTP http) throws IOException
	{
		if (!file.isDirectory()) {
			String ext = MimeType.getExt(file.getName());
            if (ext.equals("jpg")) {
				sendBitmap( getImgThumb(file), http);
			}else if (ext.equals("png")) {
				sendBitmap( getImgThumb(file), http);
			}
			else if (ext.equals("bmp")) {
				sendBitmap( getImgThumb(file), http);
			}
			else if (ext.equals("webp")) {
				sendBitmap( getImgThumb(file), http);
			}
			else if (ext.equals("txt")) {
				http.redirectTemp("asset?p=icon/text.png");
			}
			else if (ext.equals("html")) {
				http.redirectTemp("asset?p=icon/html.png");
			}
			else if (ext.equals("mp3")) {
				sendBitmap( getAlbum(file), http);
			}
			else if (ext.equals("mp4")) {
				sendBitmap( getVidThumb(file), http);
			}
			else if (ext.equals("gelse if")) {
				sendBitmap( getImgThumb(file), http);
			}
			else if (ext.equals("ogg")) {
				sendBitmap( getVidThumb(file), http);
			}
			else if (ext.equals("rar")) {
				http.redirectTemp("asset?p=icon/archive.png");
			}
			else if (ext.equals("zip")) {
				http.redirectTemp("asset?p=icon/archive.png");
			}
			else if (ext.equals("7z")) {
				http.redirectTemp("asset?p=icon/archive.png");
			}
			else {
				http.redirectTemp("asset?p=icon/unknown.png");
			}
        } else if (file.getParent().endsWith("Android/data")) {
			try {
                sendBitmap( ((BitmapDrawable) this.context.getPackageManager().getApplicationIcon(file.getName())).getBitmap(), http);
            } catch (NameNotFoundException e){
				http.redirectTemp("asset?p=icon/folder.png");
			}
			catch (Exception e) {
                http.redirectTemp("asset?p=icon/folder.png");
				throw new IOException(e.toString());
            }
        } else {
            http.redirectTemp("asset?p=icon/folder.png");
        }
	}
	
	private void sendBitmap(Bitmap bmp, HTTP http) throws IOException
	{
		OutputStream out = http.getOutputStream(MimeType.MIME_TYPE_PNG);
		boolean comp = bmp.compress(Bitmap.CompressFormat.PNG, 20, out);
		out.close();
		//icon.recycle();
	}
	
    public Bitmap getIcon(File file)
	{
        if (!file.isDirectory()) {
			String ext = MimeType.getExt(file.getName());
            if (ext.equals("jpg")) {
				return getImgThumb(file);
			}else if (ext.equals("png")) {
				return getImgThumb(file);
			}
			else if (ext.equals("bmp")) {
				return getImgThumb(file);
			}
			else if (ext.equals("webp")) {
				return getImgThumb(file);
			}
			else if (ext.equals("txt")) {
				return getBitmapForId(R.drawable.text);
			}
			else if (ext.equals("html")) {
				return getBitmapForId(R.drawable.html);
			}
			else if (ext.equals("mp3")) {
				return getAlbum(file);
			}
			else if (ext.equals("mp4")) {
				return getVidThumb(file);
			}
			else if (ext.equals("gelse if")) {
				return getImgThumb(file);
			}
			else if (ext.equals("ogg")) {
				return getVidThumb(file);
			}
			else if (ext.equals("rar")) {
				return getBitmapForId(R.drawable.archive);
			}
			else if (ext.equals("zip")) {
				return getBitmapForId(R.drawable.archive);
			}
			else if (ext.equals("7z")) {
				return getBitmapForId(R.drawable.archive);
			}
			else {
				return getBitmapForId(R.drawable.unknown);
			}
        } else if (file.getParent().endsWith("Android/data")) {
			try {
                return ((BitmapDrawable) this.context.getPackageManager().getApplicationIcon(file.getName())).getBitmap();
            } catch (NameNotFoundException e2) {
                return getBitmapForId(R.drawable.folder);
            }
        } else {
            return getBitmapForId(R.drawable.folder);
        }
    }

    public Bitmap getBitmapForId(int i) {
        return ((BitmapDrawable) this.context.getResources().getDrawable(i)).getBitmap();
    }

    public Bitmap getImgThumb(File file) {
        try {
            return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(file.getAbsolutePath()), this.THUMBSIZE, this.THUMBSIZE);
        } catch (Exception e) {
            return getBitmapForId(R.drawable.unknown);
        }
    }

    public Bitmap getVidThumb(File file) {
        return ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), 3);
    }

    public Bitmap getAlbum(File file) {
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(file.getPath());
            byte[] embeddedPicture = mmr.getEmbeddedPicture();
            return BitmapFactory.decodeByteArray(embeddedPicture, 0, embeddedPicture.length);
        } catch (Exception e) {
            return getBitmapForId(R.drawable.unknown);
        }
    }
}
