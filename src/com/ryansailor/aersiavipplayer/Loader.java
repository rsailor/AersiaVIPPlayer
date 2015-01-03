//package com.ryansailor.aersiavipplayer;
//
//import java.io.File;
//import java.util.ArrayList;
//
//import android.content.Context;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.os.Environment;
//
//import com.ryansailor.aersiavipplayer.VIPMediaPlayer.MusicFile;
//
//public class Loader {
//
//	public void loadLocalDirectory(Context context, String path) {
//		if(isExternalStorageReadable()) {
//			File musicDirectory = new File(Environment.getExternalStorageDirectory(), path);
//			parentPath = musicDirectory.getAbsolutePath() + "/";
//			File[] music_files = musicDirectory.listFiles(new MusicFileFilter());
//			music = new MusicFile[music_files.length];
//			for (int i = 0; i < music_files.length; i++) {
//				music[i] = new MusicFile(parentPath + music_files[i].getName(),music_files[i].getName());
//			}
//		}
//	}
//	
//	public void loadLocalFile(String path) {
//		if(isExternalStorageReadable() && MusicFileFilter.accept(path)) {
//			File file = new File(Environment.getExternalStorageDirectory(), path);
//			parentPath = file.getParent() + "/";
//			music = new MusicFile[1];
//			music[0] = new MusicFile(parentPath + file.getName(),file.getName());
//		}
//	}
//	
//	public void loadURL(String url) {
//		Uri uri = Uri.parse(url);
//		String urifull = uri.toString();
//		if(MusicFileFilter.accept(urifull)) {
//			parentPath = urifull.substring(0, urifull.length()-uri.getLastPathSegment().toString().length());
//			music = new MusicFile[1];
//			music[0] = new MusicFile(urifull, uri.getLastPathSegment());
//		}
//	}
//	
//	public void loadURLs(String[] urls) {
//		GetMetaData gmd = new GetMetaData();
//		gmd.execute(urls);
//	}
//	
//	private boolean isExternalStorageReadable() {
//    	String state = Environment.getExternalStorageState();
//    	if (Environment.MEDIA_MOUNTED.equals(state) ||
//    		Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
//    		return true;
//    	}
//    	return false;
//    }
//	
//	private class GetMetaData extends AsyncTask<String, Void, MusicFile[]> {
//		
//		@Override
//		protected MusicFile[] doInBackground(String... strings) {
//			String[] paths = strings;
//			
//			ArrayList<MusicFile> musicList = new ArrayList<MusicFile>();
//
//			//FFmpegMediaMetadataRetriever meta = new FFmpegMediaMetadataRetriever();
//			String path, trackname;
//			
//			for(int i = 0; i < paths.length; i++) {
//				path = paths[i];
//
//				if(MusicFileFilter.accept(path)) {
//					//meta.setDataSource(path);
//					//trackname = meta.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_TRACK);
//					trackname = Uri.parse(path).getLastPathSegment().toString();
//					musicList.add(new MusicFile(path,trackname));
//				}
//			}
//			
//			//meta.release();
//			MusicFile[] musicArray = musicList.toArray(new MusicFile[musicList.size()]);
//			
//			
//			return musicArray;
//		}
//		
//		protected void onPostExecute(MusicFile[] musicArray) {
//			music = musicArray;
//			if(mpListener != null) {
//				mpListener.onComplexMediaPlayerLoaded();
//			}
//		}
//	}
//}
