package com.ryansailor.aersiavipplayer;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity implements 
	OnItemClickListener, OnMagicLinkParserReadyListener, OnComplexMediaPlayerPreparedListener, OnTouchListener, OnBufferingUpdateListener {

	public final static String TAG = "AersiaVIPPlayer";
	
	/* Media Player */
	ComplexMediaPlayer mp;
	
	/* UI */
	TextView nowPlaying;
	Button playButton;
	TextView trackTime;
	
	private SeekBar seekBarProgress;
	private final Handler seekHandler = new Handler();
	
	ArrayAdapter<String> musicListAdapter;
	
	private Handler trackTimeHandler = new Handler(Looper.getMainLooper());
	private final Runnable r = new Runnable() {
		public void run() {
			int cursecs = mp.getTrackCurrentTime()/1000;
			int totalsecs = mp.getTrackTotalTime()/1000;
			String Scursecs = ((cursecs%60) < 10) ? "0" + (cursecs%60) : "" + (cursecs%60);
			String Stotalsecs = ((totalsecs%60) < 10) ? "0" + (totalsecs%60) : "" + (totalsecs%60);
			String curtime = (cursecs/60) + ":" + Scursecs;
			String totaltime = (totalsecs/60) + ":" + Stotalsecs;
			trackTime.setText(curtime + "/" + totaltime);
			trackTimeHandler.postDelayed(r, 100);
		}
	};
	
	/*
	 * 
	 * 
	 * LIFECYCLE METHODS
	 * 
	 * 
	 */
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Build Media Player
		mp = new ComplexMediaPlayer();
		
		mp.setOnComplexMediaPlayerPreparedListener(this);
		mp.setOnBufferingUpdateListener(this);
		// Setup static folder path
		//String musicPath = "/Music/Europa Universalis IV/";
		
		//mp.loadLocalDirectory(musicPath);
		
		// UI Setup
		playButton = (Button) findViewById(R.id.music_play);		
		nowPlaying = (TextView) findViewById(R.id.music_file_name);
		trackTime = (TextView) findViewById(R.id.music_time);
		
		seekBarProgress = (SeekBar) findViewById(R.id.seekbar);
		seekBarProgress.setMax(99);
		seekBarProgress.setOnTouchListener(this);
		
		//TODO: target for resource control
		
				
		// Song List Setup
		musicListAdapter = new ArrayAdapter<String>(this, R.layout.music_listing_item);
		ListView listView = (ListView) findViewById(R.id.music_list);
		listView.setAdapter(musicListAdapter);
		listView.setOnItemClickListener(this);
		
		// Get URL list
		MagicLinkParser mlp = new MagicLinkParser();
		mlp.setOnComplexMediaUpdateListener(this);
		mlp.parse("http://vip.aersia.net/mu/");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		trackTimeHandler.removeCallbacks(r);
		mp.destroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
	    switch (item.getItemId()) {
	        
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public void onStop() {
		super.onStop();
		trackTimeHandler.removeCallbacks(r);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		trackTimeHandler.post(r);
	}
	
	/*
	 * 
	 * MUSIC CONTROL METHODS
	 * 
	 * 
	 */
	
	public void setPlay() {
		trackTimeHandler.removeCallbacks(r);
		mp.play();
		setUIPlay();
		trackTimeHandler.post(r);
	}
	
	public void setPause() {
		trackTimeHandler.removeCallbacks(r);
		mp.pause();
		setUIPause();
	}
	
	public void onPlayPress(View view) {
		primarySeekBarProgressUpdater();
		if(mp.isPlaying()) {
			setPause();
		} else {
			setPlay();
		}
	}
	
	public void onPrevPress(View view) {
		trackTimeHandler.removeCallbacks(r);
		mp.playPrevious();
		updateUINowPlaying();
		setUIPlay();
	}
	
	public void onNextPress(View view) {
		trackTimeHandler.removeCallbacks(r);
		mp.playNext();
		updateUINowPlaying();
		setUIPlay();
	}
	
	/*
	 * 
	 * 
	 * UI LISTENERS
	 * 
	 * 
	 * 
	 */
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		trackTimeHandler.removeCallbacks(r);
		mp.playThis(pos);
		updateUINowPlaying();
		setUIPlay();
	}
	
	@Override
	public void onMagicLinkParserReady(String[] results) {
		mp.loadURLs(results);
		
		musicListAdapter.addAll(mp.getMusicNames());
		
		// Select random music
		trackTimeHandler.removeCallbacks(r);
		mp.playRandom();
		updateUINowPlaying();
		setUIPlay();
	}
	
	@Override
	public void onComplexMediaPlayerPrepared() {
		updateUINowPlaying();
		trackTimeHandler.post(r);
		primarySeekBarProgressUpdater();
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(v.getId() == R.id.seekbar) {
			SeekBar sb = (SeekBar)v;
			int ms = (mp.getTrackTotalTime()/100) * sb.getProgress();
			mp.seekTo(ms);
		}
		return false;
	}
	
	/*
	 * 
	 * 
	 * UI Methods
	 * 
	 * 
	 */
	
	private void primarySeekBarProgressUpdater() {
		seekBarProgress.setProgress((int)(((float)mp.getTrackCurrentTime()/mp.getTrackTotalTime())*100));
		if (mp.isPlaying()) {
			Runnable notification = new Runnable() {
				public void run() {
					primarySeekBarProgressUpdater();
				}
			};
			seekHandler.postDelayed(notification,1000);
		}
	}
	
	private void updateUINowPlaying() {
		nowPlaying.setText(mp.getNowPlayingName());
	}
	
	private void setUIPlay() {
		playButton.setText(R.string.music_pause);
	}
	
	private void setUIPause() {
		playButton.setText(R.string.music_play);
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		seekBarProgress.setSecondaryProgress(percent);
	}



	
}
