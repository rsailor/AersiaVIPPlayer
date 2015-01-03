package com.ryansailor.aersiavipplayer.media;

import java.util.ArrayList;

import android.widget.BaseAdapter;

public abstract class MediaRetriever {	
	
	ArrayList<MediaRetrieverObserver> observers;
	
	public MediaRetriever() {
		observers = new ArrayList<MediaRetrieverObserver>();
	}
	
	public void subscribe(MediaRetrieverObserver observer) {
		observers.add(observer);
	}
	
	public void remove(MediaRetrieverObserver observer) {
		observers.remove(observer);
	}
	
	public void notifyObservers(ArrayList<Media> mediaList) {
		for(MediaRetrieverObserver observer : observers) {
			observer.updateMediaList(mediaList);
		}
	}
	
	public abstract void retrieve();
}
