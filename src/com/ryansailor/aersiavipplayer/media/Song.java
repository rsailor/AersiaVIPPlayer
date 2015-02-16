package com.ryansailor.aersiavipplayer.media;

import java.util.HashMap;

import android.net.Uri;


public abstract class Song extends Media {
	protected MetaData meta;

	protected class MetaData {
		HashMap<String,Object> map = new HashMap<String,Object>();
		
		protected void add(String key, Integer value) {
			map.put(key, value);
		}
		
		protected void add(String key, String value) {
			map.put(key, value);
		}
		
		protected void add(String key, Long value) {
			map.put(key, value);
		}
		
		protected Integer getInteger(String key) {
			return (Integer) map.get(key);
		}
		
		protected String getString(String key) {
			return (String) map.get(key);
		}
		
		protected Long getLong(String key) {
			return (Long) map.get(key);
		}
	}
	
	public Song() {
		meta = new MetaData();
	}
	
	public abstract Uri getUri();
	
	public Object getMeta(String type, String key) {
		if(type == "Integer") {
			return meta.getInteger(key);
		} else if(type == "String") {
			return meta.getString(key);
		} else if(type == "Long") {
			return meta.getLong(key);
		}
		return null;
	}

	public Integer getMetaInteger(String key) {
		return meta.getInteger(key);
	}
	
	public String getMetaString(String key) {
		return meta.getString(key);
	}
	
	public Long getMetaLong(String key) {
		return meta.getLong(key);
	}
	
}
