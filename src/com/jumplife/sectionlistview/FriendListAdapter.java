package com.jumplife.sectionlistview;

import java.util.ArrayList;


import com.jumplife.imageload.ImageLoader;
import com.jumplife.imageload.UrlImageLoader;
import com.jumplife.moviediary.R;
import com.jumplife.moviediary.entity.Movie;
import com.jumplife.moviediary.entity.User;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendListAdapter extends BaseAdapter{
	
    Context mContext;
	private ArrayList<User> friends;
	private ImageLoader imageLoader;
	
	public FriendListAdapter(Context mContext, ArrayList<User> friendList){
		this.friends = friendList;
		this.mContext = mContext;
		imageLoader=new ImageLoader(mContext);
	}

	public int getCount() {
		
		return friends.size();
	}

	public Object getItem(int position) {

		return friends.get(position);
	}

	public long getItemId(int position) {
	
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		
		LayoutInflater myInflater = LayoutInflater.from(mContext);
		View converView = myInflater.inflate(R.layout.listview_friends, null);
		
		ImageView userIcon = (ImageView)converView.findViewById(R.id.fb_icon);
		TextView name = (TextView)converView.findViewById(R.id.user_name);
		TextView recordCount = (TextView)converView.findViewById(R.id.record_count);
		
		name.setText(friends.get(position).getName());
		
		recordCount.setText("目前打卡收藏了 " + String.valueOf(friends.get(position).getRecordCount()) + " 部電影");
//		poster.setImageBitmap(UrlImageLoader.returnBitMap(movies.get(position).getPosterUrl()));
		imageLoader.DisplayImage("http://graph.facebook.com/" + friends.get(position).getAccount() + "/picture?type=square", userIcon);
		
		return converView;

	}

	

}
