package com.ryansailor.aersiavipplayer.media;

import android.net.Uri;

public class VIPRosterSong extends Song {

	public VIPRosterSong(String location, String title, String creator) {
		super();
		meta.add("url", location);
		meta.add("title",title);
		meta.add("creator", creator);
	}
	
	@Override
	public Uri getUri() {
		return Uri.parse(getMetaString("url"));
	}

}
