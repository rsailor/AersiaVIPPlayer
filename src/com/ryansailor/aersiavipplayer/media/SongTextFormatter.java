package com.ryansailor.aersiavipplayer.media;

import android.net.Uri;

public class SongTextFormatter {
	private Media media;
	public SongTextFormatter(Media media) {
		this.media = media;
	}
	
	public Uri getURI() {
		return media.getUri();
	}
	
	public String getTitle() {
		return (String) media.getMeta("String", "title");
	}
	
	public String getCreator() {
		return (String) media.getMeta("String", "creator");
	}
	
	public String presentationName() {
		return getCreator() + " - " + getTitle();
	}
}
