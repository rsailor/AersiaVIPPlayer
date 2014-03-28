package com.ryansailor.aersiavipplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Environment;

public class ComplexMediaPlayer implements OnCompletionListener, OnPreparedListener, OnSeekCompleteListener{
	
	private final String TAG = "ComplexMediaPlayer";
	
	private MediaPlayer mp;
	
	private OnComplexMediaPlayerPreparedListener prepListener;
	
	private String parentPath;
	private MusicFile[] music;
	private int currentSelection;
	private boolean shuffle;
	
	private STATE state;
	
	enum STATE {
		IDLE,
		INITIALIZED,
		PREPARED,
		STARTED,
		STOPPED,
		PAUSED,
		PLAYBACK_COMPLETE,
		PREPARING
	}
	
	protected class MusicFile {
		private String path;
		private String name;
		
		public MusicFile(String _path, String _name) {
			path = _path;
			name = _name;
		}
		
		public String getPath() { return path; }
		public String getName() { return name; }
	};

	public ComplexMediaPlayer() {
		// Build Media Player
		mp = new MediaPlayer();
		mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mp.setOnCompletionListener(this);
		mp.setOnPreparedListener(this);
		mp.setOnSeekCompleteListener(this);
		music = new MusicFile[0];
		state = STATE.IDLE;
		// Property Setup
		currentSelection = -1;
		shuffle = true;
	}
	
	/*
	 * GETTERS
	 */
	
	public String[] getMusicNames() {
		ArrayList<String> temp = new ArrayList<String>();
		for(int i = 0; i < music.length; i++) {
			temp.add(music[i].getName());
		}
		
		String[] tempArray = temp.toArray(new String[temp.size()]);
		
		return tempArray;
	}
	
	public String getParentPath() {
		return parentPath;
	}
	
	public int getNowPlayingIndex() {
		return currentSelection;
	}
	
	public String getNowPlayingName() {
		return music[currentSelection].getName();
	}
	
	/*
	 * SETTERS
	 */
	
	public void setOnComplexMediaPlayerPreparedListener(OnComplexMediaPlayerPreparedListener listener) {
		prepListener = listener;
	}
	
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		mp.setOnBufferingUpdateListener(listener);
	}
	
	/*
	 * QUERY
	 */
	
	public boolean isPlaying() {
		if(state == STATE.PREPARING) {
			return false;
		} else {
			return mp.isPlaying();
		}
	}
	
	/*
	 * FILE LOADING
	 */
	
	public void loadLocalDirectory(String path) {
		if(isExternalStorageReadable()) {
			File musicDirectory = new File(Environment.getExternalStorageDirectory(), path);
			parentPath = musicDirectory.getAbsolutePath() + "/";
			File[] music_files = musicDirectory.listFiles(new MusicFileFilter());
			music = new MusicFile[music_files.length];
			for (int i = 0; i < music_files.length; i++) {
				music[i] = new MusicFile(parentPath + music_files[i].getName(),music_files[i].getName());
			}
		}
	}
	
	public void loadLocalFile(String path) {
		if(isExternalStorageReadable() && MusicFileFilter.accept(path)) {
			File file = new File(Environment.getExternalStorageDirectory(), path);
			parentPath = file.getParent() + "/";
			music = new MusicFile[1];
			music[0] = new MusicFile(parentPath + file.getName(),file.getName());
		}
	}
	
	public void loadURL(String url) {
		Uri uri = Uri.parse(url);
		String urifull = uri.toString();
		if(MusicFileFilter.accept(urifull)) {
			parentPath = urifull.substring(0, urifull.length()-uri.getLastPathSegment().toString().length());
			music = new MusicFile[1];
			music[0] = new MusicFile(urifull, uri.getLastPathSegment());
		}
	}
	
	public void loadURLs(String[] urls) {
		ArrayList<MusicFile> temp = new ArrayList<MusicFile>();
		Uri uri;
		String urifull;
		parentPath = null;
		for(int i = 0; i < urls.length; i++) {
			uri = Uri.parse(urls[i]);
			urifull = uri.toString();
			if(MusicFileFilter.accept(urifull)) {
				temp.add(new MusicFile(urifull,uri.getLastPathSegment()));
			}
		}
		
		MusicFile[] musicArray = temp.toArray(new MusicFile[temp.size()]);
		
		music = musicArray;
	}
	
	/**
	 * Used to load a page of music file links.
	 * Will attempt to capture every direct link by parsing file extension, 
	 * but does not check if link is proper music file.
	 * @param url
	 */
	public void loadLinkPage(String url) {
		
	}
	
	/*
	 * PLAYBACK CONTROL
	 */
	
	public void play() {
		if(state == STATE.PREPARED || state == STATE.STARTED
				|| state == STATE.PAUSED || state == STATE.PLAYBACK_COMPLETE) {
			mp.start();
			state = STATE.STARTED;
		}
	}
	
	public void pause() {
		if(state == STATE.STARTED || state == STATE.PAUSED || state == STATE.PLAYBACK_COMPLETE) {
			mp.pause();
			state = STATE.PAUSED;
		}
	}
	
	public void playNext() {
		if(++currentSelection == music.length) {
			beginStream(currentSelection = 0);
		} else {
			beginStream(currentSelection);
		}
	}
	
	public void playPrevious() {
		if(--currentSelection == -1) {
			beginStream(currentSelection = music.length-1);
		} else {
			beginStream(currentSelection);
		}	
	}
	
	public void playThis(int index) {
		if(index >= 0 && index < music.length && music.length > 0) {
			beginStream(index);
		}
	}
	
	public void playRandom() {
		if(music.length > 0) {
			beginStream(randomSelection());
		}
	}
	
	public boolean toggleShuffle() {
		shuffle = !shuffle;
		return shuffle;
	}
	
	public int getTrackTotalTime() {
		if(state == STATE.PREPARED || state == STATE.STARTED || state == STATE.PAUSED 
				|| state == STATE.PLAYBACK_COMPLETE || state == STATE.STOPPED) {
			return mp.getDuration();
		} else {
			return 0;
		}
	}
	
	public int getTrackCurrentTime() {
		if(state == STATE.PREPARED || state == STATE.STARTED || state == STATE.PAUSED 
				|| state == STATE.PLAYBACK_COMPLETE || state == STATE.STOPPED || state == STATE.IDLE
				|| state == STATE.INITIALIZED) {
			return mp.getCurrentPosition();
		} else {
			return 0;
		}
	}
	
	/**
	 * Seek to specific time in track. Will do nothing if msecs is outside of track time range.
	 * @param msecs: milliseconds to seek to
	 */
	public void seekTo(int msecs) {
		if(state == STATE.PREPARED || state == STATE.STARTED || state == STATE.PAUSED || state == STATE.PLAYBACK_COMPLETE) {
			if(msecs <= mp.getDuration() && msecs >= 0) {
				mp.seekTo(msecs);
			}
		}
	}
	
	private void beginStream(int index) {
		if(state != STATE.PREPARING) {
			String path = music[index].getPath();
			try {
				mp.reset();
				state = STATE.IDLE;
				mp.setDataSource(path);
				state = STATE.INITIALIZED;
				mp.prepareAsync();
				state = STATE.PREPARING;
				currentSelection = index;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/*
	 * LISTENER METHODS
	 */
	
	@Override
	public void onSeekComplete(MediaPlayer arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		state = STATE.PREPARED;
		play();
		prepListener.onComplexMediaPlayerPrepared();
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		state = STATE.PLAYBACK_COMPLETE;
		if(shuffle) {
			playRandom();
		} else {
			playNext();
		}
	}
	
	/*
	 * MISCELLANEOUS HELPERS
	 */
	
	private boolean isExternalStorageReadable() {
    	String state = Environment.getExternalStorageState();
    	if (Environment.MEDIA_MOUNTED.equals(state) ||
    		Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
    		return true;
    	}
    	return false;
    }
	
	private int randomSelection() {
		if(music.length == 0) return 0;
		if(music.length == 1) return 0;
		int rando = -1;
		while(rando == currentSelection || rando == -1) {
			rando = (int) Math.floor(Math.random() * music.length); 
		}
		return rando;
	}
	
	/*
	 * DESTRUCTION
	 */
	
	public void destroy() {
		mp.release();
	}
	
}
