package com.jumplife.sectionlistview;

import java.util.ArrayList;

import com.jumplife.imageload.ImageLoader;
import com.jumplife.moviediary.R;
import com.jumplife.moviediary.entity.Movie;
import com.jumplife.moviediary.entity.Record;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

public class MovieGridAdapter extends BaseAdapter{

	private ArrayList<Movie> movies;
	private Context mContext;
	private ImageLoader imageLoader;
	
	private int width;
	private int height;

	public MovieGridAdapter(Context mContext, ArrayList<Movie> movies){
		this.movies = movies;
		this.mContext = mContext;
		imageLoader=new ImageLoader(mContext);
		width = 80;
		height = 120;
	}
	
	public MovieGridAdapter(Context mContext, ArrayList<Movie> movies, int width, int height){
		this.movies = movies;
		this.mContext = mContext;
		imageLoader=new ImageLoader(mContext, width);
		this.width = width;
		this.height = height;
	}
	
	public int getCount() {
		return movies.size();
	}

	public Object getItem(int position) {
		return movies.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater myInflater = LayoutInflater.from(mContext);
		View converView = myInflater.inflate(R.layout.gridview_movie_item, null);
		
		ImageView poster = (ImageView)converView.findViewById(R.id.movie_poster);
		poster.setScaleType(ImageView.ScaleType.FIT_CENTER);
		TextView name = (TextView)converView.findViewById(R.id.movie_name);
		TextView loveCount = (TextView)converView.findViewById(R.id.loveCount);

		TextView recordsCount = (TextView)converView.findViewById(R.id.recordsCount);
		
		poster.getLayoutParams().height = height - 5;
		poster.getLayoutParams().width = width - 3;
		
		poster.setBackgroundResource(R.drawable.movie_waterfall_layout_shape);
		
		
		name.setText(movies.get(position).getChineseName());
		loveCount.setText(movies.get(position).getGoodCount()+"");
		imageLoader.DisplayImage(movies.get(position).getPosterUrl(), poster);
		
		recordsCount.setText(String.valueOf(movies.get(position).getRecordsCount()));
		
		
		return converView;
	}

}
