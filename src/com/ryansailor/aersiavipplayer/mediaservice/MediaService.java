package com.ryansailor.aersiavipplayer.mediaservice;

import java.util.ArrayList;

import android.media.MediaPlayer.OnBufferingUpdateListener;

import com.ryansailor.aersiavipplayer.media.Media;
import com.ryansailor.aersiavipplayer.mediaservice.MediaServiceBase.BufferListener;
import com.ryansailor.aersiavipplayer.mediaservice.MediaServiceBase.OnPauseListener;
import com.ryansailor.aersiavipplayer.mediaservice.MediaServiceBase.OnPlayListener;

public interface MediaService {

	public void setList(ArrayList<Media> list);
	public void setBound();
	public void setUnbound();
	public boolean isBound();
	public void playMedia();
	public void setMedia(int index);
	public int getNowPlayingIndex();
	public int getPosition();
	public int getDuration();
	public boolean isPlaying();
	public void pause();
	public void playNext();
	public void setShuffle();
	public boolean isShuffle();
	public void playPrevious();
	public void seekTo(int position);
	public void start();
	public void stop();
	public void addBufferListener(BufferListener listener);
	public void removeBufferListener(BufferListener listener);
	public void addOnPlayListener(OnPlayListener listener);
	public void removeOnPlayListener(OnPlayListener listener);
	public void setOnPauseListener(OnPauseListener listener);
}
