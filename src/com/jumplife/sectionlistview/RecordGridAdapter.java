package com.jumplife.sectionlistview;

import java.util.ArrayList;

import com.jumplife.imageload.ImageLoader;
import com.jumplife.moviediary.R;
import com.jumplife.moviediary.entity.Record;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RecordGridAdapter extends BaseAdapter{

	private ArrayList<Record> records;
	private Context mContext;
	private ImageLoader imageLoader;

	public RecordGridAdapter(Context mContext, ArrayList<Record> records){
		this.records = records;
		this.mContext = mContext;
		imageLoader=new ImageLoader(mContext);
	}
	
	
	public int getCount() {
		if(records != null)
			return records.size();
		else
			return 0;
	}

	public Object getItem(int position) {
		return records.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater myInflater = LayoutInflater.from(mContext);
		View converView = myInflater.inflate(R.layout.gridview_record, null);
		
		ImageView poster = (ImageView)converView.findViewById(R.id.movie_poster);
		TextView name = (TextView)converView.findViewById(R.id.movie_name);
		TextView like = (TextView)converView.findViewById(R.id.textview_like);

		name.setText(records.get(position).getMovie().getChineseName());
		like.setText(String.valueOf(records.get(position).getLoveCount()));
		imageLoader.DisplayImage(records.get(position).getMovie().getPosterUrl(), poster);
		
		return converView;
	}

}
