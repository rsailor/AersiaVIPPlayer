package com.ryansailor.aersiavipplayer;

import java.util.ArrayList;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.ryansailor.aersiavipplayer.media.Media;
import com.ryansailor.aersiavipplayer.media.MediaRetriever;
import com.ryansailor.aersiavipplayer.media.MediaRetrieverObserver;
import com.ryansailor.aersiavipplayer.media.SongTextFormatter;
import com.ryansailor.aersiavipplayer.media.VIPRosterMediaRetriever;
import com.ryansailor.aersiavipplayer.mediaservice.MediaService;
import com.ryansailor.aersiavipplayer.mediaservice.MediaServiceAdapterBase;
import com.ryansailor.aersiavipplayer.mediaservice.MediaServiceBase;
import com.ryansailor.aersiavipplayer.mediaservice.MediaServiceBase.MediaBinder;
import com.ryansailor.aersiavipplayer.mediaservice.MediaServiceBase.OnPauseListener;
import com.ryansailor.aersiavipplayer.mediaservice.MediaServiceBase.OnPlayListener;
import com.ryansailor.aersiavipplayer.polling.MediaServicePoller;
import com.ryansailor.aersiavipplayer.polling.PollObserver;
import com.ryansailor.aersiavipplayer.polling.Poller;

public class MainActivity extends Activity implements MediaRetrieverObserver,
	OnItemClickListener, OnSeekBarChangeListener, PollObserver, MediaServiceBase.BufferListener,
	OnPlayListener, OnPauseListener {

	/* Debug */
	public final static String TAG = "AersiaVIPPlayer";
	
	private MediaRetriever mediaRetriever;
	private ArrayList<Media> mediaList;
	private MediaAdapter mediaAdapter;
	private MediaService mediaService;
	private Poller poller;
	private Intent playIntent;
	
	private ListView mediaListView;
	private SeekBar seekbarView;
	private TextView currentTimeView;
	private TextView totalTimeView;
	private MenuItem playButton;
	private TextView trackNameView;
	
	private BroadcastReceiver noisyReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(context, "Headphones disconnected.", Toast.LENGTH_SHORT).show();
			if(mediaService.isPlaying()) {
				playButton.setIcon(getResources().getDrawable(R.drawable.ic_action_play));
				poller.halt();
				mediaService.pause();
			}
		}
	};
		
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mediaList = new ArrayList<Media>();
		mediaListView = (ListView) findViewById(R.id.music_list);
		mediaAdapter = new MediaAdapter(this, mediaList);
		mediaListView.setAdapter(mediaAdapter);
		mediaListView.setOnItemClickListener(this);
		
		seekbarView = (SeekBar) findViewById(R.id.seekbar);
		seekbarView.setMax(99);
		seekbarView.setOnSeekBarChangeListener(this);
		currentTimeView = (TextView) findViewById(R.id.music_time);
		totalTimeView = (TextView) findViewById(R.id.music_time_total);
		trackNameView = (TextView) findViewById(R.id.trackname);	
	}
	
	public void startPlayList(String url) {
		mediaRetriever = new VIPRosterMediaRetriever(url);
		mediaRetriever.subscribe(this);
		mediaRetriever.retrieve();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(playIntent == null) {
			playIntent = new Intent(this, MediaServiceBase.class);
			bindService(playIntent, mediaConnection, Context.BIND_AUTO_CREATE);
			startService(playIntent);
		}
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		if(mediaService.isPlaying())
			poller.start();
	}
	
	@Override
	protected void onStop() {
	  poller.halt();
	  super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		stopService(playIntent);
		unbindService(mediaConnection);
		getApplicationContext().unregisterReceiver(noisyReceiver);
		mediaService = null;
		super.onDestroy();
	}
	
	@Override
	public void updateMediaList(ArrayList<Media> newMediaList) {
		mediaList.clear();
		mediaList = newMediaList;
		mediaAdapter.setList(newMediaList);
		mediaAdapter.notifyDataSetChanged();
		if(mediaService.isBound()) {
			mediaService.stop();
			mediaService.setList(mediaList);
			if(mediaService.isShuffle()) {
				Random rand = new Random();
				mediaService.setMedia(rand.nextInt(mediaList.size()));
			} else {
				mediaService.setMedia(0);
			}
			mediaService.playMedia();
		}
	}
	
	private ServiceConnection mediaConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			MediaBinder binder = (MediaBinder)service;
			mediaService = new MediaServiceAdapterBase(binder.getService());
			mediaService.setList(mediaList);
			mediaService.setBound();
			mediaService.setShuffle();
						
			poller = new MediaServicePoller(mediaService);
			poller.setInterval(1000);
			poller.addObserver(MainActivity.this);
			
			mediaService.addBufferListener(MainActivity.this);
			mediaService.addOnPlayListener(MainActivity.this);
			mediaService.setOnPauseListener(MainActivity.this);
			getApplicationContext().registerReceiver(noisyReceiver, new IntentFilter(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY));
			startPlayList("http://vip.aersia.net/roster.xml");
			getActionBar().setTitle(R.string.playlist_vip);
		}
		public void onServiceDisconnected(ComponentName name) {
			mediaService.setUnbound();
		}
	};

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		mediaService.setMedia(pos);
		mediaService.playMedia();
		SongTextFormatter song = new SongTextFormatter(mediaList.get(pos));
		trackNameView.setText(song.presentationName());
		poller.start();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		playButton = menu.findItem(R.id.action_play);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_play:
			if(mediaService.isPlaying()) {
				playButton.setIcon(getResources().getDrawable(R.drawable.ic_action_play));
				poller.halt();
				mediaService.pause();
			} else {
				playButton.setIcon(getResources().getDrawable(R.drawable.ic_action_pause));
				poller.start();
				mediaService.start();
			}
    		break;
    	case R.id.action_next:
    		mediaService.playNext();
    		break;
    	case R.id.action_prev:
    		mediaService.playPrevious();
    		break;
		case R.id.action_end:
			stopService(playIntent);
			mediaService = null;
			System.exit(0);
			break;
		case R.id.playlist_exiled:
			startPlayList("http://vip.aersia.net/roster-exiled.xml");
			Toast.makeText(getApplicationContext(), "VIP Exhiled", Toast.LENGTH_LONG).show();
			getActionBar().setTitle(R.string.playlist_exiled);
			break;
		case R.id.playlist_vip:
			startPlayList("http://vip.aersia.net/roster.xml");
			Toast.makeText(getApplicationContext(), "VIP Original", Toast.LENGTH_LONG).show();
			getActionBar().setTitle(R.string.playlist_vip);
			break;
		case R.id.playlist_mellow:
			startPlayList("http://vip.aersia.net/roster-mellow.xml");
			Toast.makeText(getApplicationContext(), "VIP Mellow", Toast.LENGTH_LONG).show();
			getActionBar().setTitle(R.string.playlist_mellow);
			break;
		case R.id.playlist_source:
			startPlayList("http://vip.aersia.net/roster-source.xml");
			Toast.makeText(getApplicationContext(), "VIP Source", Toast.LENGTH_LONG).show();
			getActionBar().setTitle(R.string.playlist_source);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	public void onPoll(Object o) {
		int seconds = (Integer) o;
		currentTimeView.setText(formatTime(seconds));
		int duration = mediaService.getDuration()/1000;
		if(mediaService.isPlaying()) {
			if(duration > 0)
				seekbarView.setProgress(seconds*100/duration);
			else
				seekbarView.setProgress(0);
		}
	}
	
	@SuppressLint("DefaultLocale")
	private String formatTime(int seconds) {
		int minutes = seconds / 60;
		int secs = seconds % 60;
		return String.format("%02d:%02d", minutes, secs);
	}

	@Override
	public void onBuffer(int percent) {
		seekbarView.setSecondaryProgress(percent);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		int ms = mediaService.getDuration() * seekBar.getProgress()/100;
		mediaService.seekTo(ms);
	}

	@Override
	public void onPlay() {
		poller.start();
		int index = mediaService.getNowPlayingIndex();
		SongTextFormatter song = new SongTextFormatter(mediaList.get(index));
		playButton.setIcon(getResources().getDrawable(R.drawable.ic_action_pause));
		mediaAdapter.setSelected(index);
		mediaAdapter.notifyDataSetChanged();
		betterSmoothScroll(index);
		trackNameView.setText(song.presentationName());
		totalTimeView.setText(formatTime(mediaService.getDuration()/1000));
	}
	
	public void onPlayerPause() {
		poller.halt();
		playButton.setIcon(getResources().getDrawable(R.drawable.ic_action_play));
	}
	
	public void betterSmoothScroll(final int pos) {
		int current = mediaListView.getFirstVisiblePosition();
		final int diff = pos - current;
		final int direction = diff >= 0 ? 1 : -1;
		mediaListView.post(new Runnable() {
			@Override
			public void run() {
				if(Math.abs(diff) > 100) {
					mediaListView.setSelection(pos - (100*direction));
				}
				mediaListView.smoothScrollToPositionFromTop(pos, 0, 1000);
			}
		});
	}
	
		

	
}