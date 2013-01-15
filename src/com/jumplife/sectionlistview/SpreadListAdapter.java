package com.jumplife.sectionlistview;

import java.util.ArrayList;

import com.jumplife.imageload.ImageLoader;
import com.jumplife.moviediary.R;
import com.jumplife.moviediary.entity.Spread;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class SpreadListAdapter extends BaseAdapter{
	
    public static final int ALL=-1; //全部需要  
    public static final int FILTER_HEAD=0;//从头开始过滤  
    public static final int FILTER_FOOT=1;//从后开始过滤  
    public static final int FILTER_BODY=2;//任意过滤  
    
    private Activity mActivity;
    private ArrayList<Spread> spreads;
	private ImageLoader imageLoader;
	
	public SpreadListAdapter(Activity mActivity, ArrayList<Spread> spreadList){
		this.spreads = spreadList;
		this.mActivity = mActivity;
		imageLoader = new ImageLoader(mActivity, 0, R.drawable.post_background);
	}

	public int getCount() {
		
		return spreads.size();
	}

	public Object getItem(int position) {

		return spreads.get(position);
	}

	public long getItemId(int position) {
	
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		
		LayoutInflater myInflater = LayoutInflater.from(mActivity);
		View converView = myInflater.inflate(R.layout.listview_spread, null);
		
		ImageView poster = (ImageView)converView.findViewById(R.id.spread_image);
		DisplayMetrics displayMetrics = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        imageLoader.DisplayImage(spreads.get(position).getImageUrl(), poster, displayMetrics.widthPixels);
		
		return converView;

	}
}
