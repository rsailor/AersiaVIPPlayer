package com.ryansailor.aersiavipplayer;

import android.annotation.SuppressLint;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;

public class MusicFileFilter implements FilenameFilter {

	private static String extensions[] = {
			"ogg",
			"m4a",
			"mp3",
			"aac"
	};
	
	@SuppressLint("DefaultLocale")
	@Override
	public boolean accept(File dir, String filename) {
		String normed = filename.toLowerCase(Locale.ENGLISH);
		String ext;
		for(int i = 0, n = extensions.length; i < n; i++) {
			ext = extensions[i];
			if(normed.endsWith("." + ext)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Static single file version
	 */
	public static boolean accept(String filename) {
		String normed = filename.toLowerCase(Locale.ENGLISH);
		String ext;
		for(int i = 0, n = extensions.length; i < n; i++) {
			ext = extensions[i];
			if(normed.endsWith("." + ext)) {
				return true;
			}
		}
		return false;
	}
}
