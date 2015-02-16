package com.ryansailor.aersiavipplayer.media;

import android.content.ContentUris;
import android.net.Uri;

public class LocalSong extends Song {
	
	public LocalSong(long id, String title, String creator) {
		super();
		meta.add("id", id);
		meta.add("title",title);
		meta.add("creator", creator);
	}

	@Override
	public Uri getUri() {
		return ContentUris.withAppendedId(
				android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
				getMetaLong("id"));
	}
}
