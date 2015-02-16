package com.ryansailor.aersiavipplayer.mediaservice;

import android.app.Service;

public abstract class MediaServiceImplementation extends Service implements MediaService {

	public void setBound() {
		throw new UnsupportedOperationException();
	}
	
	public void setUnbound() {
		throw new UnsupportedOperationException();
	}
	
	public boolean isBound() {
		throw new UnsupportedOperationException();
	}
	
}
