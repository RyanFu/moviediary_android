package com.jumplife.sectionlistview;

import java.util.ArrayList;

import com.jumplife.imageload.ImageLoader;
import com.jumplife.moviediary.R;
import com.jumplife.moviediary.entity.Movie;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MovieGalleryAdapter extends BaseAdapter{

	private ArrayList<Movie> movies;
	private Context mContext;
	private ImageLoader imageLoader;
	private boolean showVideo;

	public MovieGalleryAdapter(Context mContext, ArrayList<Movie> movies, boolean showVideo){
		this.movies = movies;
		this.mContext = mContext;
		this.imageLoader=new ImageLoader(mContext);
		this.showVideo = showVideo;
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
		View converView = myInflater.inflate(R.layout.gallery_movie, null);
		
		ImageView poster = (ImageView)converView.findViewById(R.id.movie_poster);
		ImageView video = (ImageView)converView.findViewById(R.id.movie_video);
		ImageView play = (ImageView)converView.findViewById(R.id.movie_play);
		TextView name = (TextView)converView.findViewById(R.id.movie_name);
		
		name.setText(movies.get(position).getChineseName());
		play.setAlpha(150);
		imageLoader.DisplayImage(movies.get(position).getPosterUrl(), poster);
		
		if(showVideo) {
			imageLoader.DisplayImage(movies.get(position).getYoutubeVideoImage(), video);
			final int pos = position;
			play.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// TODO Auto-generated method stub
					/*Intent videoClient = new Intent(Intent.ACTION_VIEW);
					videoClient.setData(Uri.parse(movies.get(pos).getYoutubeVideoUrl()));
					videoClient.setClassName("com.google.anddroid.youtube", "com.google.android.youtube.WatchActivity");
					mContext.startActivity(videoClient);*/
	
					mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(movies.get(pos).getYoutubeVideoUrl())));
				}	          
		    });
		} else {
			video.setVisibility(View.GONE);
			play.setVisibility(View.GONE);
		}
		
		return converView;
	}

}
