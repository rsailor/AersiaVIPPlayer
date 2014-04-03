package com.ryansailor.aersiavipplayer;

import wseemann.media.FFmpegMediaMetadataRetriever;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity implements 
	OnItemClickListener, OnMagicLinkParserReadyListener, OnComplexMediaPlayerListener, OnTouchListener, OnBufferingUpdateListener {

	public final static String TAG = "AersiaVIPPlayer";
	
	/* Media Player */
	private ComplexMediaPlayer mp;
	
	/* UI */
	
	private MenuItem playButton;
	private TextView trackTime;
	private ListView trackList;
	private int selectedTrack;
	
	private SeekBar seekBarProgress;
	private final Handler seekHandler = new Handler();
	
	
	private class SelectionArrayAdapter extends ArrayAdapter<String> {
		private final Context context;
		private int resource;
		private int selection;
		
		public SelectionArrayAdapter(Context context, int resource) {
			super(context, R.layout.music_listing_item, resource);
			this.context = context;
			this.resource = resource;
			this.selection = -1;
		}
		
		public void setSelection(int pos) {
			selection = pos;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			TextView rowView = (TextView) inflater.inflate(this.resource, parent, false);
			rowView.setText(this.getItem(position));
			if(position == selection) {
				rowView.setBackgroundColor(getResources().getColor(R.color.music_list_item_bg_sel));
				rowView.setTextColor(getResources().getColor(R.color.music_list_item_text_sel));
			} else {
				rowView.setBackgroundColor(getResources().getColor(R.color.music_list_item_bg));
				rowView.setTextColor(getResources().getColor(R.color.music_list_item_text));
			}
			return rowView;
		}
		
	}
	
	
	SelectionArrayAdapter musicListAdapter;
	
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
		
		mp.setOnComplexMediaPlayerListener(this);
		mp.setOnBufferingUpdateListener(this);
		// Setup static folder path

		
		// UI Setup	
		trackTime = (TextView) findViewById(R.id.music_time);
		trackList = (ListView) findViewById(R.id.music_list);
		selectedTrack = -1;
		
		seekBarProgress = (SeekBar) findViewById(R.id.seekbar);
		seekBarProgress.setMax(99);
		seekBarProgress.setOnTouchListener(this);
				
		// Song List Setup
		musicListAdapter = new SelectionArrayAdapter(this, R.layout.music_listing_item);
		trackList.setAdapter(musicListAdapter);
		trackList.setOnItemClickListener(this);
		
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
		playButton = menu.findItem(R.id.action_play);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
	    switch (item.getItemId()) {
	    	case R.id.action_play:
	    		onPlayPress();
	    		return true;
	    	case R.id.action_next:
	    		onNextPress();
	    		return true;
	    	case R.id.action_prev:
	    		onPrevPress();
	    		return true;
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
		if(mp.isPlaying()) {
			trackTimeHandler.post(r);
		}
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
	
	public void onPlayPress() {
		primarySeekBarProgressUpdater();
		if(mp.isPlaying()) {
			setPause();
		} else {
			setPlay();
		}
	}
	
	public void onPrevPress() {
		trackTimeHandler.removeCallbacks(r);
		mp.playPrevious();
		updateUINowPlaying();
		setUIPlay();
	}
	
	public void onNextPress() {
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
		setUIPlay();
	}
	
	@Override
	public void onMagicLinkParserReady(String[] results) {
		mp.loadURLs(results);
	}
	
	@Override
	public void onComplexMediaPlayerBeginStream() {
		updateUINowPlaying();
		trackTimeHandler.post(r);
		primarySeekBarProgressUpdater();
	}
	
	@Override
	public void onComplexMediaPlayerLoaded() {
		musicListAdapter.addAll(mp.getMusicNames());
		
		// Select random music
		trackTimeHandler.removeCallbacks(r);
		mp.playRandom();
		setUIPlay();
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

		// update selected and change color
		selectedTrack = mp.getNowPlayingIndex();
		musicListAdapter.setSelection(selectedTrack);
		musicListAdapter.notifyDataSetChanged();
		
		// scroll to selected
		trackList.smoothScrollToPositionFromTop(selectedTrack,0,1000);
	}
	
	private void setUIPlay() {
		playButton.setIcon(getResources().getDrawable(R.drawable.ic_action_pause));
	}
	
	private void setUIPause() {
		playButton.setIcon(getResources().getDrawable(R.drawable.ic_action_play));
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		seekBarProgress.setSecondaryProgress(percent);
	}



	
}
