package com.ryansailor.aersiavipplayer.media;

import android.net.Uri;

public abstract class Media {
	
	public abstract Uri getUri();
	public abstract Object getMeta(String type, String key);
}
