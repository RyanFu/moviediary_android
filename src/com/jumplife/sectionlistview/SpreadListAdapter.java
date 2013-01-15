package com.jumplife.sectionlistview;

import java.util.ArrayList;

import com.jumplife.imageload.ImageLoader;
import com.jumplife.moviediary.R;
import com.jumplife.moviediary.entity.Movie;
import com.jumplife.moviediary.entity.Spread;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class SpreadListAdapter extends BaseAdapter{
	
    public static final int ALL=-1; //全部需要  
    public static final int FILTER_HEAD=0;//从头开始过滤  
    public static final int FILTER_FOOT=1;//从后开始过滤  
    public static final int FILTER_BODY=2;//任意过滤  
    
    Context mContext;
    private ArrayList<Spread> spreads;
	private ImageLoader imageLoader;
	
	public SpreadListAdapter(Context mContext, ArrayList<Spread> spreadList){
		this.spreads = spreadList;
		this.mContext = mContext;
		imageLoader=new ImageLoader(mContext);
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
		
		LayoutInflater myInflater = LayoutInflater.from(mContext);
		View converView = myInflater.inflate(R.layout.listview_spread, null);
		
		ImageView poster = (ImageView)converView.findViewById(R.id.spread_image);
		imageLoader.DisplayImage(spreads.get(position).getImageUrl(), poster);
		
		return converView;

	}
}
