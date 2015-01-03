package com.ryansailor.aersiavipplayer;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ryansailor.aersiavipplayer.media.Media;

public class MediaAdapter extends BaseAdapter {

	private ArrayList<Media> _mediaList;
	private LayoutInflater _mediaInflater;
	
	private class MediaAdapterViewHolder {
		public TextView mediaListItemView;
		public MediaAdapterViewHolder(View view) {
			mediaListItemView = (TextView) view;
		}
	}
	
	public MediaAdapter(Context context, ArrayList<Media> mediaList) {
		_mediaList = mediaList;
		_mediaInflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		return _mediaList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		MediaAdapterViewHolder viewHolder;
		if(convertView == null) {
			view = _mediaInflater.inflate(R.layout.music_listing_item, null);
			viewHolder = new MediaAdapterViewHolder(view);
			view.setTag(viewHolder);
		} else {
			viewHolder = (MediaAdapterViewHolder) view.getTag();
		}
		Media media = _mediaList.get(position);
		viewHolder.mediaListItemView.setText((String) media.getMeta("String", "title"));		
		return view;
	}

}
