package com.ryansailor.aersiavipplayer.polling;

import com.ryansailor.aersiavipplayer.mediaservice.MediaService;

public class MediaServicePoller extends Poller {

	MediaService service;
	
	public MediaServicePoller(MediaService service) {
		this.service = service;
	}

	@Override
	protected void notifyObservers(Object o) {
		for(PollObserver observer : observers) {
			observer.onPoll(o);
		}
	}

	@Override
	protected void poll() {
		int cursecs = service.getPosition()/1000;
		notifyObservers(cursecs);
	}
	
}
