package com.ryansailor.aersiavipplayer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.AsyncTask;

public class MagicLinkParser {
	/**
	 * This class is built to parse the /mu/ pages of the aersia playlists.
	 */
	
	private final int TIMEOUTMILLIS = 5000;
	
	private OnMagicLinkParserReadyListener updateListener;
	
	private class GetURLsTask extends AsyncTask<String, Void, Document> {
		
		URL url;
		
		@Override
		protected Document doInBackground(String... strings) {
			Document doc = null;
			
			try {
				url = new URL(strings[0]);
				doc = Jsoup.parse(url,TIMEOUTMILLIS);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return doc;
		}
		
		protected void onPostExecute(Document doc) {
			// Select all links on the page
			Elements linklist = doc.select("a[href]");
			
			ArrayList<String> templist = new ArrayList<String>();
			// Iterate through list
			for(Element link: linklist) {
				// Check if link is a music link
				String href = link.attr("href");
				if(MusicFileFilter.accept(href)) {
					templist.add(url.toString() + href);
				}
			}
			
			String[] linkArray = templist.toArray(new String[templist.size()]);
			
			if(updateListener != null) {
				updateListener.onMagicLinkParserReady(linkArray);
			}
		}
		
	}
	
	public void parse(String _url) {
		new GetURLsTask().execute("http://vip.aersia.net/mu/");	
	}
	
	public void setOnComplexMediaUpdateListener(OnMagicLinkParserReadyListener listener) {
		updateListener = listener;
	}
	
}
