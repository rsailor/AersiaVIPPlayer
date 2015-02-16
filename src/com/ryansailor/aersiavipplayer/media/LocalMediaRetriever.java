package com.ryansailor.aersiavipplayer.media;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class LocalMediaRetriever extends MediaRetriever {
	
	private Context _context;
	
	public LocalMediaRetriever(Context context) {
		super();
		_context = context;
	}
	
	@Override
	public void retrieve() {
		ArrayList<Media> mediaList = new ArrayList<Media>();
		ContentResolver mediaResolver = _context.getContentResolver();
		Uri mediaUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor mediaCursor = mediaResolver.query(mediaUri,null,null,null,null);
		
		if(mediaCursor != null && mediaCursor.moveToFirst()) {
			int titleCol = mediaCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
			int idCol = mediaCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
			int artistCol = mediaCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
			do {
				long thisId = mediaCursor.getLong(idCol);
				String thisTitle = mediaCursor.getString(titleCol);
				String thisArtist = mediaCursor.getString(artistCol);
				mediaList.add(new LocalSong(thisId, thisTitle, thisArtist));
			} while (mediaCursor.moveToNext());
		}
		
		notifyObservers(mediaList);
	}

}
