package com.ryansailor.aersiavipplayer.mediaservice;

import java.util.ArrayList;

import android.media.MediaPlayer.OnBufferingUpdateListener;

import com.ryansailor.aersiavipplayer.media.Media;
import com.ryansailor.aersiavipplayer.mediaservice.MediaServiceBase.BufferListener;
import com.ryansailor.aersiavipplayer.mediaservice.MediaServiceBase.OnPauseListener;
import com.ryansailor.aersiavipplayer.mediaservice.MediaServiceBase.OnPlayListener;

public class MediaServiceAdapterBase extends MediaServiceAdapter {
	
	MediaServiceImplementation instance;
	private boolean mediaBound = false;
	
	public MediaServiceAdapterBase() {
		instance = null;
	}
	
	public MediaServiceAdapterBase(MediaServiceImplementation service) {
		instance = service;
	}
	
	public int getNowPlayingIndex() {
		return instance.getNowPlayingIndex();
	}
	
	public int getPosition() {
		if(instance != null && mediaBound && instance.isPlaying())
			return instance.getPosition();
		else return 0;
	}
	
	public int getDuration() {
		if(instance != null && mediaBound && instance.isPlaying())
			return instance.getDuration();
		else return 0;
	}
	
	public boolean isPlaying() {
		if(instance != null && mediaBound)
			return instance.isPlaying();
		return false;
	}
	
	public void pause() {
		instance.pause();
	}
	
	public void stop() {
		instance.stop();
	}
	
	public void seekTo(int position) {
		instance.seekTo(position);
	}
	
	public void start() {
		instance.start();
	}

	public void playNext() {
		instance.playNext();
	}

	public void playPrevious() {
		instance.playPrevious();
	}

	public void setList(ArrayList<Media> mediaList) {
		instance.setList(mediaList);
	}

	public void setMedia(int pos) {
		instance.setMedia(pos);
	}

	public void playMedia() {
		instance.playMedia();
	}
	
	public void setShuffle() {
		instance.setShuffle();
	}

	public void setBound() {
		mediaBound = true;
	}

	public void setUnbound() {
		mediaBound = false;
	}
	
	public boolean isBound() {
		return mediaBound;
	}
	
	@Override
	public void addBufferListener(BufferListener listener) {
		instance.addBufferListener(listener);
	}

	@Override
	public void removeBufferListener(BufferListener listener) {
		instance.removeBufferListener(listener);
	}

	@Override
	public void addOnPlayListener(OnPlayListener listener) {
		instance.addOnPlayListener(listener);		
	}

	@Override
	public void removeOnPlayListener(OnPlayListener listener) {
		instance.removeOnPlayListener(listener);
	}

	@Override
	public boolean isShuffle() {
		return instance.isShuffle();
	}
	
	public void setOnPauseListener(OnPauseListener listener) {
		instance.setOnPauseListener(listener);
	}
}
