//package com.ryansailor.aersiavipplayer;
//
//import com.ryansailor.aersiavipplayer.VIPMediaService.MusicBinder;
//
//import android.app.Activity;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.media.AudioManager;
//import android.media.AudioManager.OnAudioFocusChangeListener;
//import android.media.MediaPlayer;
//import android.media.MediaPlayer.OnBufferingUpdateListener;
//import android.os.Binder;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.Looper;
//import android.text.format.Time;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.View.OnTouchListener;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.ArrayAdapter;
//import android.widget.ListView;
//import android.widget.SeekBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//public class CopyOfMainActivity extends Activity implements OnItemClickListener, OnAudioFocusChangeListener, OnTouchListener, OnBufferingUpdateListener {
//
//	/* Debug */
//	public final static String TAG = "AersiaVIPPlayer";
//	
//	
//	/* UI */
//	private MenuItem playButton;
//	private TextView trackTime;
//	private TextView trackTotalTime;
//	private ListView trackList;
//	private TextView nowPlaying;
//	
//	private SeekBar seekBar;
//	private final Handler seekHandler = new Handler(); 
//	
//	SelectionArrayAdapter musicListAdapter;
//	
//	/* Media Player Service */
//	private Intent serviceIntent;
//	private VIPMediaService service;
//	private boolean serviceBound=false;
//		
//	private ServiceConnection musicConnection = new ServiceConnection(){	  
//		@Override
//	    public void onServiceConnected(ComponentName name, IBinder service) {
//	        MusicBinder binder = (MusicBinder)service;
//	        service = binder.getService();
//	        serviceBound = true;
//	    }
//	 
//	    @Override
//	    public void onServiceDisconnected(ComponentName name) {
//		    serviceBound = false;
//	    }
//	};
//
//	/* Play List */
//	private PlayList currentPlayList;
//	int currentIndex;
//	
//	/* System Services */
//	AudioManager audioManager; // TODO: Move to controller
//	
//	/* Track Time Tracker */
//	private Handler trackTimeHandler = new Handler(Looper.getMainLooper()); // TODO: Move to controller
//	private final Runnable r = new Runnable() {
//		public void run() {
//			int cursecs = mp.getTrackCurrentTime()/1000;
//			int totalsecs = mp.getTrackTotalTime()/1000;
//			String Scursecs = ((cursecs%60) < 10) ? "0" + (cursecs%60) : "" + (cursecs%60);
//			String Stotalsecs = ((totalsecs%60) < 10) ? "0" + (totalsecs%60) : "" + (totalsecs%60);
//			String curtime = (cursecs/60) + ":" + Scursecs;
//			String totaltime = (totalsecs/60) + ":" + Stotalsecs;
//			trackTime.setText(curtime + "/" + totaltime);
//			trackTimeHandler.postDelayed(r, 100);
//		}
//	};
//	
//	// trackTimeHandler.removeCallbacks(r);
//	// trackTimeHandler.post(r);
//	
//	
//	/*
//	 * 
//	 * 
//	 * CLASSES AND HANDLERS
//	 * 
//	 * 
//	 */
//	
//	private class SelectionArrayAdapter extends ArrayAdapter<String> {
//		private final Context context;
//		private int resource;
//		private int selection;
//		
//		public SelectionArrayAdapter(Context context, int resource) {
//			super(context, R.layout.music_listing_item, resource);
//			this.context = context;
//			this.resource = resource;
//			this.selection = -1;
//		}
//		
//		public void setSelection(int pos) {
//			selection = pos;
//		}
//		
//		public String getSelection() {
//			return this.getItem(selection);
//		}
//		
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//			TextView rowView = (TextView) inflater.inflate(this.resource, parent, false);
//			rowView.setText(this.getItem(position));
//			if(position == selection) {
//				rowView.setBackgroundColor(getResources().getColor(R.color.music_list_item_bg_sel));
//				rowView.setTextColor(getResources().getColor(R.color.music_list_item_text_sel));
//			} else {
//				rowView.setBackgroundColor(getResources().getColor(R.color.music_list_item_bg));
//				rowView.setTextColor(getResources().getColor(R.color.music_list_item_text));
//			}
//			return rowView;
//		}
//		
//	}
//	
//	
//	
//	
//
//	
//	/*
//	 * 
//	 * 
//	 * LIFECYCLE METHODS
//	 * 
//	 * 
//	 */
//	
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//
//		// Audio Manager 
//		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//		int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
//		
//		if(result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//			// TODO: If Audio Focus is not granted at startup
//		}
//		
//		// UI Setup	
//		trackTime = (TextView) findViewById(R.id.music_time);
//		trackTotalTime = (TextView) findViewById(R.id.music_time_total);
//		trackList = (ListView) findViewById(R.id.music_list);
//		nowPlaying = (TextView) findViewById(R.id.trackname);
//		
//		seekBar = (SeekBar) findViewById(R.id.seekbar);
//		seekBar.setMax(99);
//		seekBar.setOnTouchListener(this);
//				
//		// Song List Setup
//		musicListAdapter = new SelectionArrayAdapter(this, R.layout.music_listing_item);
//		trackList.setAdapter(musicListAdapter);
//		trackList.setOnItemClickListener(this);
//		
//	}
//	
//	@Override
//	public void onStart() {
//		super.onStart();
//		if(serviceIntent == null) {
//			serviceIntent = new Intent(this, VIPMediaService.class);
//			bindService(serviceIntent, musicConnection, Context.BIND_AUTO_CREATE);
//			startService(serviceIntent);
//		}
//		// Get URL list
////		startNewPlaylist("Now Playing VIP original", new PlayList(PLAYLISTID.VIP, "http://vip.aersia.net/mu/", "VIP"));
//	}
//	
//	@Override
//	public void finish() { // TODO: I want to remove this
//		trackTimeHandler.removeCallbacks(r);
//		super.finish();
//	}
//	
//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//		trackTimeHandler.removeCallbacks(r);
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		playButton = menu.findItem(R.id.action_play);
//		return true;
//	}	
//	
//	@Override
//	public void onStop() {
//		super.onStop();
//		trackTimeHandler.removeCallbacks(r);
//	}
//	
//
//	
//	/*
//	 * 
//	 * MUSIC CONTROL METHODS
//	 * 
//	 * 
//	 */
//	// TODO: Move all to controller
//	public void oldsetPlay() {
//		trackTimeHandler.removeCallbacks(r);
//		mp.play();
//		setUIPlay();
//		trackTimeHandler.post(r);
//	}
//
//	/*
//	 * 
//	 * 
//	 * UI LISTENERS
//	 * 
//	 * 
//	 * 
//	 */
//	
//	@Override
//	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
//		trackTimeHandler.removeCallbacks(r);
//		mp.playThis(pos);
//		setUIPlay();
//	}
//	
////	@Override 
////	public void onMagicLinkParserReady(String[] results) { // TODO: Move to controller
////		parsedMusicPaths = results;
////		mp.loadURLs(results);
////	}
//	
////	@Override
////	public void onComplexMediaPlayerBeginStream() { // TODO: Move to controller
////		updateUINowPlaying();
////		trackTimeHandler.post(r);
////		primarySeekBarProgressUpdater();
////	}
//	
////	@Override
////	public void onComplexMediaPlayerLoaded() { // TODO: Move to controller
////		musicListAdapter.clear();
////		musicListAdapter.addAll(mp.getMusicNames());
////		
////		// Select random music
////		trackTimeHandler.removeCallbacks(r);
////		mp.playRandom();
////		setUIPlay();
////	}
//	
//	@Override
//	public boolean onTouch(View v, MotionEvent event) {
//		if(v.getId() == R.id.seekbar) {
//			SeekBar sb = (SeekBar)v;
//			int ms = (mp.getTrackTotalTime()/100) * sb.getProgress();
//			mp.seekTo(ms);
//		}
//		return false;
//	}
//	
//	@Override
//	public void onBufferingUpdate(MediaPlayer mp, int percent) { // TODO: Move to controller
//		seekBar.setSecondaryProgress(percent);
//	}
//	
//	/*
//	 * 
//	 * 
//	 * UI Methods
//	 * 
//	 * 
//	 */
//	// TODO: Move to controller
//	private void primarySeekBarProgressUpdater() {
//		seekBar.setProgress((int)(((float)mp.getTrackCurrentTime()/mp.getTrackTotalTime())*100));
//		if (mp.isPlaying()) {
//			Runnable notification = new Runnable() {
//				public void run() {
//					primarySeekBarProgressUpdater();
//				}
//			};
//			seekHandler.postDelayed(notification,1000);
//		}
//	}
//	
//	private void updateUINowPlaying() { // TODO: Move to controller
//
//		// update selected and change color
//		selectedTrack = mp.getNowPlayingIndex();
//		musicListAdapter.setSelection(selectedTrack);
//		musicListAdapter.notifyDataSetChanged();
//		
//		nowPlaying.setText(musicListAdapter.getSelection());
//		
//		// scroll to selected
//		int currentPosition = trackList.getFirstVisiblePosition();
//		int positionDifference = selectedTrack - currentPosition;
//		// To prevent excess scrolling, skip some if track position different is large
//		int delta = 80; // TODO: figure this out programmatically
//		if(Math.abs(positionDifference) >= delta) {
//			// If we move up, 
//			if(positionDifference < 0) {
//				trackList.smoothScrollByOffset(-10);
//				trackList.setSelection(selectedTrack + delta);
//			} else {
//				trackList.smoothScrollByOffset(10);
//				trackList.setSelection(selectedTrack - delta);
//			}
//		}
//		trackList.smoothScrollToPositionFromTop(selectedTrack,0,1000);
//	}
//	
//	private void setUIPlay() { 
//		playButton.setIcon(getResources().getDrawable(R.drawable.ic_action_pause));
//	}
//	
//	private void setUIPause() {
//		playButton.setIcon(getResources().getDrawable(R.drawable.ic_action_play));
//	}
//
//	/*
//	 * 
//	 * 
//	 * UI INTERFACE
//	 * 
//	 * 
//	 */
//	
//	public void setPlay() {
//		playButton.setIcon(getResources().getDrawable(R.drawable.ic_action_pause));
//	}
//	
//	public void setPause() {
//		playButton.setIcon(getResources().getDrawable(R.drawable.ic_action_play));
//	}
//	
//	public void setNext() {
//		
//	}
//	
//	public void setPrev() {
//		
//	}
//	
//	public void setTrackTime(int seconds) {
//		int minutes = (int) Math.floor(seconds / 60);
//		int secs = seconds % 60;
//		trackTime.setText(String.format("%d:%02d",minutes,secs));
//	}
//	
//	public void setTrackBar(int percent) {
//		seekBar.setProgress(percent);
//	}
//	
//	public void setTrackBuffer(int percent) {
//		seekBar.setSecondaryProgress(percent);
//	}
//	
//	public void setTotalTrackTime(int seconds) {
//		int minutes = (int) Math.floor(seconds / 60);
//		int secs = seconds % 60;
//		trackTotalTime.setText(String.format("%d:%02d",minutes,secs));
//	}
//	
//	public void selectTrackIndex(int index) {
//		musicListAdapter.setTrack(index);
//		stopMusicService();
//		String url = currentPlayList.getTrackPath(index);
//		String title = currentPlayList.getTrackName(index);
//		startMusicService(url, title);
//	}
//	
//	public void fillTrackList(String[] trackList) {
//		
//	}
//	
//	/*
//	 * 
//	 * 
//	 * CONTROLLER
//	 * 
//	 * 
//	 */
//	
//	
//	
//	public void startMusicService(String url, String title) {
//		Intent intent = new Intent(getApplicationContext(), VIPMediaService.class);
//		intent.putExtra("url", url);
//		intent.putExtra("songTitle", title);
//		startService(intent);
//	}
//	
//	public void stopMusicService() {
//		Intent intent = new Intent(getApplicationContext(), VIPMediaService.class);
//		stopService(intent);
//	}
//	
//	public boolean onOptionsItemSelected(MenuItem item) { 
//		// Handle presses on the action bar items
//	    switch (item.getItemId()) {
//	    	case R.id.action_play:
//	    		setPlay();
//	    		
//	    		return true;
//	    	case R.id.action_next:
//	    		setNext();
//	    		
//	    		return true;
//	    	case R.id.action_prev:
//	    		setPrev();
//	    		
//	    		return true;
//	    	case R.id.playlist_vip:
//	    		if(currentPlayList.getId() != PLAYLISTID.VIP) {
//		    		startNewPlaylist("Now Playing VIP Original",
//		    				new PlayList(PLAYLISTID.VIP,
//		    				"http://vip.aersia.net/mu/",
//		    				"VIP"));
//	    		}
//	    		return true;
//	    	case R.id.playlist_mellow:
//	    		if(currentPlayList.getId() != PLAYLISTID.MELLOW) {
//	    			startNewPlaylist("Now Playing VIP Mellow",
//	    					new PlayList(PLAYLISTID.MELLOW,
//	    					"http://vip.aersia.net/mu/mellow/",
//	    					"VIP Mellow"));
//	    		}
//	    		return true;
//	    	case R.id.playlist_exiled:
//	    		if(currentPlayList.getId() != PLAYLISTID.EXILED) {
//	    			startNewPlaylist("Now Playing VIP Exiled",
//	    					new PlayList(PLAYLISTID.EXILED,
//	    					"http://vip.aersia.net/mu/exiled/",
//	    					"VIP Exiled"));
//	    		}
//	    		return true;
//	    	case R.id.playlist_source:
//	    		if(currentPlayList.getId() != PLAYLISTID.SOURCE) {
//	    			startNewPlaylist("Now Playing VIP Source",
//	    					new PlayList(PLAYLISTID.SOURCE,
//	    					"http://vip.aersia.net/mu/source/",
//	    					"VIP Source"));
//	    		}
//	    		return true;
//	        default:
//	            return super.onOptionsItemSelected(item);
//	    }
//	}
//
//	@Override
//	public void onAudioFocusChange(int arg0) {
//		// TODO Auto-generated method stub
//		
//	}	
//	
//	private void startNewPlaylist(String msg, PlayList pl) {// TODO: Move to controller
//		mp.stop();
//		currentPlayList = pl;
//		// Get URL list
//		MagicLinkParser mlp = new MagicLinkParser();
//		mlp.setOnComplexMediaUpdateListener(this);
//		mlp.parse(pl.getLocation());
//		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
//		getActionBar().setTitle(pl.getName());
//	}
//	
//	
//}
