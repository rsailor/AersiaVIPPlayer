package com.ryansailor.aersiavipplayer.polling;

import java.util.ArrayList;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public abstract class Poller {

	ArrayList<PollObserver> observers = new ArrayList<PollObserver>();
	private int interval = 1000;
	private Handler handler = new Handler(Looper.getMainLooper());
	private final Runnable r = new Runnable() {
		public void run() {
			poll();
			handler.postDelayed(r, interval);
		}
	};
	
	public void addObserver(PollObserver observer) {
		observers.add(observer);
	}
	
	public void removeObserver(PollObserver observer) {
		observers.remove(observer);
	}
	
	public void setInterval(int newInterval) {
		interval = newInterval;
	}
	
	public int getInterval() {
		return interval;
	}
	
	protected abstract void notifyObservers(Object o);
	protected abstract void poll();
	
	public void start() {
		handler.post(r);
	}
	
	public void halt() {
		handler.removeCallbacks(r);
	}
	
}
