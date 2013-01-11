package com.jumplife.moviediary;

import java.util.ArrayList;

import java.util.HashMap;

import com.google.analytics.tracking.android.TrackedActivity;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.jumplife.moviediary.api.MovieAPI;
import com.jumplife.moviediary.entity.News;
import com.jumplife.sectionlistview.NewsAdapter;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class NewsActivity extends TrackedActivity {
	private PullToRefreshListView newsListView;
	private ArrayList<News> newsList;
	private ImageButton imageButtonRefresh;
	private LinearLayout pullMore;
	private NewsAdapter newsAdapter;
	private int page = 1;
	public static final String TAG = "NewsActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        findViews();
        LoadDataTask task = new LoadDataTask();
        if(Build.VERSION.SDK_INT < 11)
			task.execute();
        else
        	task.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
    }

    private void findViews() {
    	TextView topbar_text = (TextView)findViewById(R.id.topbar_text);
        topbar_text.setText("電影報");
        
    	newsListView = (PullToRefreshListView)findViewById(R.id.listview_news);
		pullMore = (LinearLayout)findViewById(R.id.progressBar_pull_more);
		imageButtonRefresh = (ImageButton)findViewById(R.id.refresh);
		imageButtonRefresh.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				LoadDataTask task = new LoadDataTask();
				if(Build.VERSION.SDK_INT < 11)
					task.execute();
	            else
	            	task.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
			}			
		});
    }

    private void setView() {
    	if(newsList != null && newsList.size() > 0) {
			newsListView.setVisibility(View.VISIBLE);
			imageButtonRefresh.setVisibility(View.GONE);
		} else {
			newsListView.setVisibility(View.GONE);
			imageButtonRefresh.setVisibility(View.VISIBLE);
		}
    }

    private void FetchData() {
    	MovieAPI movieAPI = new MovieAPI();
		newsList = movieAPI.getNewsList(page);
    }

    private void setListener() {
    	newsListView.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				HashMap<String, String> parameters = new HashMap<String, String>();
				int pos = position - 1;
				News news = newsList.get(pos);
				
				if(news.getType() == News.TYPE_LINK) {
					parameters.put("TYPE", "LINK");
					
					Uri uri = Uri.parse(news.getLink());  
		    		Intent it = new Intent(Intent.ACTION_VIEW, uri);
		    		startActivity(it);
		    		
				}
				else if(news.getType() == News.TYPE_PIC) {
					parameters.put("TYPE", "PIC");
					Intent intent = new Intent();
					intent.putExtra("picture_url", news.getPictureUrl());
					intent.putExtra("content", news.getContent());
					intent.setClass( NewsActivity.this, NewsPic.class );
			        startActivity( intent );
				}
			}
		});
		
		newsListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@SuppressWarnings("deprecation")
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				newsListView.setLastUpdatedLabel(DateUtils.formatDateTime(getApplicationContext(),
						System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL));

				// Do work to refresh the list here.
				new RefreshTask().execute();
			}
		});
		
		newsListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
			public void onLastItemVisible() {
				// TODO Auto-generated method stub
				new NextPageTask().execute();
			}			
		});
    }

    private void setListAdatper() {
    	newsAdapter = new NewsAdapter(NewsActivity.this, newsList);
		newsListView.setAdapter(newsAdapter);
    }

    class LoadDataTask extends AsyncTask<Integer, Integer, String> {

    	private ProgressDialog progressdialogInit;
    	private OnCancelListener cancelListener = new OnCancelListener(){
		    public void onCancel(DialogInterface arg0){
		    	LoadDataTask.this.cancel(true);
		    	newsListView.setVisibility(View.GONE);
				imageButtonRefresh.setVisibility(View.VISIBLE);
		    }
    	};
    	
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(NewsActivity.this);
            progressdialogInit.setTitle("Load");
            progressdialogInit.setMessage("Loading…");
            progressdialogInit.setOnCancelListener(cancelListener);
            progressdialogInit.setCanceledOnTouchOutside(false);
            progressdialogInit.show();
            newsListView.setVisibility(View.VISIBLE);
            imageButtonRefresh.setVisibility(View.GONE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            FetchData();
            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {

        	if(progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();
        	if(newsList != null && newsList.size() > 0){
        		Log.d(TAG, "List Size: " + newsList.size());
        		setListAdatper();
            	setListener();
            	page += 1;
        	}
        	super.onPostExecute(result);

        }
    }

    class RefreshTask extends AsyncTask<Integer, Integer, String> {

        @Override
        protected void onPreExecute() {
			page = 1;
        	super.onPreExecute();  
        }  
        @Override
		protected String doInBackground(Integer... params) {
        	FetchData();
			return "progress end";
		}
		@Override  
        protected void onProgressUpdate(Integer... progress) {    
            super.onProgressUpdate(progress);  
        } 
		protected void onPostExecute(String result) {
			setView();		
			if(newsList != null && newsList.size() > 0){
        		setListAdatper();
            	setListener();
            	page += 1;
        	}
			newsListView.onRefreshComplete();
        	super.onPostExecute(result);
        }
	}
	
	class NextPageTask  extends AsyncTask<Integer, Integer, String>{

		@Override  
        protected void onPreExecute() {
			pullMore.setVisibility(View.VISIBLE);
			super.onPreExecute();  
        }  
        @Override
		protected String doInBackground(Integer... params) {
        	MovieAPI movieAPI = new MovieAPI();
        	ArrayList<News> tmpList = movieAPI.getNewsList(page);
        	newsList.addAll(tmpList);
			return "progress end";
		}
		@Override  
        protected void onProgressUpdate(Integer... progress) {    
            super.onProgressUpdate(progress);  
        } 
		protected void onPostExecute(String result) {
			pullMore.setVisibility(View.GONE);
			if(newsList != null && newsList.size() > 0){
				newsAdapter.notifyDataSetChanged();
				page += 1;
    			// Call onRefreshComplete when the list has been refreshed.
        	}
			super.onPostExecute(result);
        }
	}
	
	@Override
	protected void onStart() {
	    super.onStart();
	}
	@Override
	protected void onStop() {
	    super.onStop();
    }
	@Override
	protected void onResume(){
        super.onResume();
   }
}
