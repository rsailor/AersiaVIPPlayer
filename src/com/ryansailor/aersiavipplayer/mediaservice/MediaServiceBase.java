package com.ryansailor.aersiavipplayer.mediaservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import java.util.ArrayList;

import com.ryansailor.aersiavipplayer.MainActivity;
import com.ryansailor.aersiavipplayer.R;
import com.ryansailor.aersiavipplayer.R.drawable;
import com.ryansailor.aersiavipplayer.media.Media;
import com.ryansailor.aersiavipplayer.media.Song;

import android.content.ContentUris;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.MediaController.MediaPlayerControl;

import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;

public class MediaServiceBase extends MediaServiceImplementation implements
MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener,
AudioManager.OnAudioFocusChangeListener {

	private MediaPlayer player;
	private boolean preparing = false;
	
	private ArrayList<Media> mediaList;
	private int mediaIndex;
	private final IBinder mediaBinder = new MediaBinder();
	private String mediaTitle = "";
	private static final int NOTIFY_ID = 1;
	public static final Intent ACTION_PAUSE = new Intent();
	private boolean shuffle = false;
	private Random rand;
	private AudioManager audioManager;

	private ArrayList<BufferListener> bufferListeners;
	private ArrayList<OnPlayListener> onPlayListeners;
	private OnPauseListener onPauseListener;
	
	public interface BufferListener {
		public void onBuffer(int percent);
	}
	
	public interface OnPlayListener {
		public void onPlay();
	}
	
	public interface OnPauseListener {
		public void onPlayerPause();
	}
	
	public void onCreate() {
		super.onCreate();
		mediaIndex = 0;
		bufferListeners = new ArrayList<BufferListener>();
		onPlayListeners = new ArrayList<OnPlayListener>();
		rand = new Random();
		audioManager = (AudioManager) getApplicationContext().getSystemService(AUDIO_SERVICE);
		int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
			    AudioManager.AUDIOFOCUS_GAIN);
		if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

		} else {
			initMediaPlayer();
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mediaBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		player.stop();
		player.release();
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		preparing = false;
		mp.start();
		notifyOnPlayListeners();
		Intent notIntent = new Intent(this, MainActivity.class);
		notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		Notification.Builder builder = new Notification.Builder(this);
		
		Media currentMedia = mediaList.get(mediaIndex);
		builder.setContentIntent(pendingIntent)
			.setSmallIcon(R.drawable.ic_action_play)
			.setTicker(mediaTitle)
			.setOngoing(true)
			.setContentTitle("Playing")
			.setContentText(mediaTitle);
		Notification notification = builder.build();
		
		startForeground(NOTIFY_ID, notification);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if(getPosition() > 0) {
			mp.reset();
			if(shuffle) {
				playNextRandom();
			} else {
				playNext();
			}
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		mp.reset();
		return false;
	}
	
	@Override
	public void onDestroy() {
		player.release();
		player = null;
		stopForeground(true);
	}
	
	public void initMediaPlayer() {
		if (player == null)
			player = new MediaPlayer();
		player.setVolume(1f, 1f);
		player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setOnPreparedListener(this);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
		player.setOnBufferingUpdateListener(this);
	}
	
	public void releaseMediaPlayer() {	
		if (player != null) {
			onPauseListener.onPlayerPause(); 
//			if (!preparing && player.isPlaying())
//				player.stop();
			player.release();
			player = null;
		}
	}
	
	public void setList(ArrayList<Media> mediaList) {
		this.mediaList = mediaList;
	}
	
	public int getNowPlayingIndex() {
		return mediaIndex;
	}
	
	public class MediaBinder extends Binder {
		public MediaServiceImplementation getService() {
			return MediaServiceBase.this;
		}
	}
	
	public void playMedia() {
		player.reset();
		Media playMedia = mediaList.get(mediaIndex);
		mediaTitle = (String)playMedia.getMeta("String", "title");
		Uri mediaUri = playMedia.getUri();
		try {
			player.setDataSource(getApplicationContext(), mediaUri);
		} catch (Exception e) {
			Log.e("Aersia MEDIA SERVICE", "Error setting data source", e);
		}
		preparing = true;
		player.prepareAsync();
	}
	
	public void setMedia(int index) {
		mediaIndex = index;
	}
	
	public int getPosition() {
		return player.getCurrentPosition();
	}
	
	public int getDuration() {
		return player.getDuration();
	}
	
	public boolean isPlaying() {
		return player.isPlaying();
	}
	
	public void pause() {
		player.pause();
	}
	
	public void seekTo(int position) {
		player.seekTo(position);
	}
	
	public void start() {
		player.start();
	}
	
	public void stop() {
		player.reset();
		player.stop();
	}
	
	public void playPrevious() {
		mediaIndex--;
		if(mediaIndex < 0) mediaIndex = mediaList.size() - 1;
		playMedia();
	}
	
	public void playNext() {
		mediaIndex++;
		if(mediaIndex >= mediaList.size()) mediaIndex = 0;
		playMedia();
	}
	
	public void playNextRandom() {
		int nextMedia = mediaIndex;
		while(nextMedia == mediaIndex) {
			nextMedia = rand.nextInt(mediaList.size());
		}
		mediaIndex = nextMedia;
		playMedia();
	}
	
	public void setShuffle() {
		shuffle = !shuffle;
	}
	
	public void addBufferListener(BufferListener listener) {
		bufferListeners.add(listener);
	}
	
	public void removeBufferListener(BufferListener listener) {
		bufferListeners.remove(listener);
	}
	
	private void notifyBufferListeners(int percent) {
		for(BufferListener listener : bufferListeners) {
			listener.onBuffer(percent);
		}
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		notifyBufferListeners(percent);
	}
	
	public void addOnPlayListener(OnPlayListener listener) {
		onPlayListeners.add(listener);
	}
	
	public void removeOnPlayListener(OnPlayListener listener) {
		onPlayListeners.remove(listener);
	}
	
	private void notifyOnPlayListeners() {
		for(OnPlayListener listener : onPlayListeners) {
			listener.onPlay();
		}
	}
	
	public void setOnPauseListener(OnPauseListener listener) {
		onPauseListener = listener;
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		switch (focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN:
				initMediaPlayer();
				break;
			case AudioManager.AUDIOFOCUS_LOSS:
				releaseMediaPlayer();
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				if (isPlaying())
					pause();
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				if (isPlaying())
					player.setVolume(0.1f, 0.1f);
				break;
		}
	}

	@Override
	public boolean isShuffle() {
		return shuffle;
	}
		
}
