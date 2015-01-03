package com.ryansailor.aersiavipplayer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.ryansailor.aersiavipplayer.media.LocalVIPRosterMediaRetriever;
import com.ryansailor.aersiavipplayer.media.Media;
import com.ryansailor.aersiavipplayer.media.MediaRetriever;
import com.ryansailor.aersiavipplayer.media.MediaRetrieverObserver;
import com.ryansailor.aersiavipplayer.media.VIPRosterMediaRetriever;

public class MainActivity extends Activity implements MediaRetrieverObserver {

	/* Debug */
	public final static String TAG = "AersiaVIPPlayer";
	
	private MediaRetriever mediaRetriever;
	private ArrayList<Media> mediaList;
	private ListView mediaListView;
	private MediaAdapter mediaAdapter;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mediaList = new ArrayList<Media>();
		mediaListView = (ListView) findViewById(R.id.music_list);
		mediaAdapter = new MediaAdapter(this, mediaList);
		mediaListView.setAdapter(mediaAdapter);
		
		try {
			InputStream is = getApplicationContext().getAssets().open("roster.xml");
			mediaRetriever = new LocalVIPRosterMediaRetriever(is);
			mediaRetriever.subscribe(this);
			mediaRetriever.retrieve();
		} catch (IOException e) {
			e.printStackTrace();
		}
				
	}

	@Override
	public void updateMediaList(ArrayList<Media> newMediaList) {
		mediaList.clear();
		mediaList.addAll(newMediaList);
		mediaList = newMediaList;
		mediaAdapter.notifyDataSetChanged();
	}
	
}