package com.ryansailor.aersiavipplayer.media;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.os.AsyncTask;

public class VIPRosterMediaRetriever extends MediaRetriever {

	String _url;
	
	private class RetrieveTask extends AsyncTask<Void, Void, ArrayList<Media>> {

		protected ArrayList<Media> doInBackground(Void... args) {
			Document dom = getDom(_url);
			if(dom == null)
				return new ArrayList<Media>();
			Element root = getRootNode(dom);
			if(root == null)
				return new ArrayList<Media>();
			return resolveTracks(root);
		}
		
		protected void onPostExecute(ArrayList<Media> list) {
			notifyObservers(list);
		}
	}
	
	public VIPRosterMediaRetriever(String url) {
		super();
		_url = url;
	}
	
	@Override
	public void retrieve() {
		new RetrieveTask().execute();
	}
	
	private Document getDom(String url) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document dom = null;
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			dom = db.parse(new InputSource(new URL(url).openStream()));
		} catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch(SAXException se) {
			se.printStackTrace();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		return dom;
	}
	
	private Element getRootNode(Document document) {
		return document.getDocumentElement();
	}
	
	private ArrayList<Media> resolveTracks(Element root) {
		NodeList tracklist = root.getElementsByTagName("track");
		ArrayList<Media> mediaList = new ArrayList<Media>();
		if(tracklist != null && tracklist.getLength() > 0) {
			for(int i = 0; i < tracklist.getLength(); i++) {
				mediaList.add(resolveTrack((Element)tracklist.item(i)));
			}
		}
		return mediaList;
	}
	
	private Song resolveTrack(Element elem) {
		String title = getChildText(elem, "title");
		String location = getChildText(elem, "location");
		String creator = getChildText(elem, "creator");
		return new VIPRosterSong(location, title, creator);
	}
	
	private String getChildText(Element elem, String name) {
		NodeList nodes = elem.getElementsByTagName(name);
		String text = null;
		if(nodes != null && nodes.getLength() > 0) {
			Element el = (Element)nodes.item(0);
			text = el.getFirstChild().getNodeValue();
		}
		return text;
	}



}
