package com.ryansailor.aersiavipplayer;

import java.io.IOException;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class VIPMediaService extends Service implements MediaPlayer.OnPreparedListener, 
														  MediaPlayer.OnErrorListener, 
														  MediaPlayer.OnBufferingUpdateListener {

	private static final String LOG_TAG = "VIPMediaService";
	private static final String ACTION_PLAY = "PLAY";
    private static String mUrl;
    private static VIPMediaService mInstance = null;

    private MediaPlayer mMediaPlayer = null;    // The Media Player
    private int mBufferPosition;
    private static String mSongTitle;
	
    NotificationManager mNotificationManager;
    Builder mNotificationBuilder = null;
    final int NOTIFICATION_ID = 1;
    
    enum State {
        Retrieving, // the MediaRetriever is retrieving music
        Stopped, // media player is stopped and not prepared to play
        Preparing, // media player is preparing...
        Playing, // playback active (media player ready!). (but the media player may actually be
                    // paused in this state if we don't have audio focus. But we stay in this state
                    // so that we know we have to resume playback once we get focus back)
        Paused
        // playback paused (media player ready!)
    };
	
	State mState = State.Retrieving;
	
	public class MusicBinder extends Binder {
	    VIPMediaService getService() {
	        return VIPMediaService.this;
	    }
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		mInstance = this;
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flag, int startId) {
		Bundle extras = intent.getExtras();
		mUrl = extras.getString("url");
		mSongTitle = extras.getString("songTitle");
		if (intent.getAction().equals(ACTION_PLAY)) {
			initMediaPlayer();
		}
		return START_STICKY;
	}
	
	private void initMediaPlayer() {
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnPreparedListener(this);
		mMediaPlayer.setOnErrorListener(this);
		mMediaPlayer.setOnBufferingUpdateListener(this);
		mMediaPlayer.setWakeMode(getApplicationContext(),
				PowerManager.PARTIAL_WAKE_LOCK);
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	}
	
	private void startAndPrepareMediaPlayer() {
		try {
			mMediaPlayer.setDataSource(mUrl);
		} catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "startMediaPlayer", e);
        } catch (IllegalStateException e) {
        	Log.e(LOG_TAG, "startMediaPlayer", e);
        } catch (IOException e) {
        	Log.e(LOG_TAG, "startMediaPlayer", e);
        }
		
		try {
            mMediaPlayer.prepareAsync(); // prepare async to not block main thread
        } catch (IllegalStateException e) {
        	Log.e(LOG_TAG, "startMediaPlayer", e);
        }
        mState = State.Preparing;
	}
	
	public void restartMusic() {
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.start();
    }

    protected void setBufferPosition(int progress) {
        mBufferPosition = progress;
    }

    /** Called when MediaPlayer is ready */
    @Override
    public void onPrepared(MediaPlayer player) {
        mState = State.Paused;
        startMusic();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mState = State.Stopped;
        return false;
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        mState = State.Retrieving;
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public void pauseMusic() {
        if (mState.equals(State.Playing)) {
            mMediaPlayer.pause();
            mState = State.Paused;
            updateNotification(mSongTitle + "(paused)");
        }
    }

    public void startMusic() {
        if (!mState.equals(State.Preparing) &&!mState.equals(State.Retrieving)) {
            mMediaPlayer.start();
            mState = State.Playing;
            updateNotification(mSongTitle + "(playing)");
        }
    }
    
    public void stopMusic() {
    	if (mState.equals(State.Playing)) {
    		mMediaPlayer.stop();
    		mNotificationManager.cancel(NOTIFICATION_ID);
    	}
    }

    public boolean isPlaying() {
        if (mState.equals(State.Playing)) {
            return true;
        }
        return false;
    }

    public int getMusicDuration() {
    	if (isState(State.Paused, State.Playing)) {
    		return mMediaPlayer.getDuration();
    	}
    	return 0;
    }

    public int getCurrentPosition() {
    	if (isState(State.Playing, State.Paused)) {
    		return mMediaPlayer.getCurrentPosition();
    	}
    	return 0;
    }

    public int getBufferPercentage() {
        return mBufferPosition;
    }

    public void seekMusicTo(int pos) {
        if (isState(State.Playing, State.Paused)) {
        	mMediaPlayer.seekTo(pos);
        }
    }

    public static VIPMediaService getInstance() {
        return mInstance;
    }

    public static void setSong(String url, String title) {
        mUrl = url;
        mSongTitle = title;
    }

    public String getSongTitle() {
        return mSongTitle;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        setBufferPosition(percent * getMusicDuration() / 100);
    }

    /** Updates the notification. */
    void updateNotification(String text) {
        // Notify NotificationManager of new intent
    	mNotificationBuilder.setContentText(text);
    	mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    /**
     * Configures service as a foreground service. A foreground service is a service that's doing something the user is
     * actively aware of (such as playing music), and must appear to the user as a notification. That's why we create
     * the notification here.
     */
    void setUpAsForeground(String text) {
    	Intent intent = new Intent(this, MainActivity.class);
    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationBuilder = new Notification.Builder(getApplicationContext())
        	.setContentTitle(getText(R.string.app_name))
        	.setContentText(text)
        	.setSmallIcon(R.drawable.ic_launcher)
        	.setContentIntent(pi)
        	.setOngoing(true);
        // mNotification.icon = R.drawable.ic_mshuffle_icon;
        startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
    }
	
    public boolean isState(State... states) {
    	for (State state : states)
    		if (mState.equals(state))
    			return true;
    	return false;
    }
    
}
