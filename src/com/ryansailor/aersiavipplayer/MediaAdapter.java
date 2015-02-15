package com.ryansailor.aersiavipplayer;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ryansailor.aersiavipplayer.media.Media;

public class MediaAdapter extends BaseAdapter {

	private ArrayList<Media> _mediaList;
	private LayoutInflater _mediaInflater;
	private int selected = -1;
	
	private static final int SEL_HOLDER = 1;
	private static final int NOR_HOLDER = 2;
	
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
	
	public void setList(ArrayList<Media> mediaList) {
		_mediaList = mediaList;
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
	
	public void setSelected(int pos) {
		selected = pos;
	}

	@SuppressLint("ViewTag")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		MediaAdapterViewHolder viewHolder;
		if (position == selected) {
			if(convertView == null || convertView.getTag(R.id.selected_listitem) == null) {
				view = _mediaInflater.inflate(R.layout.music_listing_item_selected, parent, false);
				viewHolder = new MediaAdapterViewHolder(view);
				view.setTag(R.id.selected_listitem, viewHolder);
			} else {
				viewHolder = (MediaAdapterViewHolder) view.getTag(R.id.selected_listitem);
			}
		} else {
			if(convertView == null || convertView.getTag(R.id.nonselected_listitem) == null) {
				view = _mediaInflater.inflate(R.layout.music_listing_item, parent, false);
				viewHolder = new MediaAdapterViewHolder(view);
				view.setTag(R.id.nonselected_listitem, viewHolder);
			} else {
				viewHolder = (MediaAdapterViewHolder) view.getTag(R.id.nonselected_listitem);
			}
		}
		Media media = _mediaList.get(position);
		String title = (String) media.getMeta("String", "title");
		String creator = (String) media.getMeta("String", "creator");
		viewHolder.mediaListItemView.setText(creator + " - " + title);
		return view;
	}
	
}
