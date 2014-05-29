package com.ryansailor.aersiavipplayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.view.View;

public class ScreenShotCamera {

	private String mFilePath;
	private File mImageFolder;
	private File mImageFile;
	private OutputStream mFileOut;
	private CompressFormat mFormat;
	private int mQuality;
	
	/**
	 * Set up ScreenShotCamera with a folder path. This does not check if
	 * external storage is available.
	 * @param folderpath - Relative to root external storage directory
	 */
	public ScreenShotCamera(String folderpath) {
		mFilePath = Environment.getExternalStorageDirectory().toString() + "/" + folderpath;
		mImageFile = null;
		mFileOut = null;
		mFormat = Bitmap.CompressFormat.PNG;
		mQuality = 90;
		
		mImageFolder = new File(mFilePath);
		if(!mImageFolder.exists()) {
			mImageFolder.mkdir();
		}
	}
	
	public ScreenShotCamera(String folderpath, String format, int quality) {
		mFilePath = Environment.getExternalStorageDirectory().toString() + "/" + folderpath;
		mImageFile = null;
		mFileOut = null;
		mFormat = parseFormatString(format);
		mQuality = quality;
		
		mImageFolder = new File(mFilePath);
		if(!mImageFolder.exists()) {
			mImageFolder.mkdir();
		}
	}
	
	public void capture(View view, String filename) {
		
		view.setDrawingCacheEnabled(true);
		Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
		
		mImageFile = new File(mFilePath + "/" + filename + "." + mFormat.name());
		
		try {
			mFileOut = new FileOutputStream(mImageFile);
			bitmap.compress(mFormat, mQuality, mFileOut);
			mFileOut.flush();
			mFileOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setFormat(CompressFormat format) {
		mFormat = format;
	}
	
	public void setQuality(int newQuality) {
		mQuality = newQuality;
	}
	
	public void setFolderPath(String newpath) {
		mFilePath = newpath;
	}
	
	private CompressFormat parseFormatString(String format) {
		CompressFormat toreturn;
		if(format.equalsIgnoreCase("png")) {
			toreturn = CompressFormat.PNG;
		} else if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg")) {
			toreturn = CompressFormat.JPEG;
		} else {
			toreturn = CompressFormat.PNG;
		}
		return toreturn;
	}
	
}
