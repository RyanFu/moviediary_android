package com.jumplife.sectionlistview;

import com.jumplife.moviediary.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class TheaterListAdapter extends BaseAdapter{
	
	String [] theaters;
    Context mContext;
	
	public TheaterListAdapter(Context mContext, String[] theaters){
		this.theaters = theaters;
		this.mContext = mContext;
	}

	public int getCount() {
		
		return theaters.length;
	}

	public Object getItem(int position) {

		return theaters[position];
	}

	public long getItemId(int position) {
	
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		
		LayoutInflater myInflater = LayoutInflater.from(mContext);
		View converView = myInflater.inflate(R.layout.listview_theater, null);
		TextView theater = (TextView)converView.findViewById(R.id.theater_name);
		theater.setText(theaters[position]);
		
		return converView;

	}

	

}
