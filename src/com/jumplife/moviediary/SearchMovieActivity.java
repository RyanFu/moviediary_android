package com.jumplife.moviediary;

import java.util.ArrayList;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import com.google.analytics.tracking.android.TrackedActivity;
import com.jumplife.moviediary.entity.Movie;
import com.jumplife.moviediary.promote.PromoteAPP;
import com.jumplife.sectionlistview.MovieListAdapter;
import com.jumplife.sqlite.SQLiteMovieDiary;

public class SearchMovieActivity extends TrackedActivity {

    public final static int  SETTING   = 1;

    private ArrayList<Movie> movieList = new ArrayList<Movie>();
    private ArrayList<Movie> arraySort = new ArrayList<Movie>();
    private ListView         movieListView;
    private LoadDataTask     tast;
    private EditText         edittextSearchBar;
    private MovieListAdapter movieAdapter;

    private static String    TAG       = "SearchMovieActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchmovie);
        findViews();
        tast = new LoadDataTask();
        if(Build.VERSION.SDK_INT < 11)
        	tast.execute();
        else
        	tast.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
    }

    private void setListListener() {
        movieListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int post = position - 1;
                Movie movie = arraySort.get(post);
                Intent newAct = new Intent();
                newAct.putExtra("movie_id", movie.getId());
                // newAct.setClass(SearchMovieActivity.this, MovieInfo.class );
                newAct.setClass(SearchMovieActivity.this, MovieShowActivity.class);
                startActivity(newAct);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void fetchData() {
        SQLiteMovieDiary sqlMovie = new SQLiteMovieDiary(SearchMovieActivity.this);
        movieList = sqlMovie.getMovieList();
        arraySort = (ArrayList<Movie>) movieList.clone();
    }

    private void setListAdatper() {
        movieAdapter = new MovieListAdapter(SearchMovieActivity.this, arraySort);
        movieListView.setAdapter(movieAdapter);
        movieListView.setTextFilterEnabled(true);
        setSearchBar();
    }

    private void findViews() {
        movieListView = (ListView) findViewById(R.id.listview_movie);
        View header = View.inflate(this, R.layout.searchbar, null);
        movieListView.addHeaderView(header);
        movieListView.setFastScrollEnabled(true);
        edittextSearchBar = (EditText) findViewById(R.id.edittext_searchbar);

        /*
         * setting = (ImageButton)findViewById(R.id.imagebutton_setting); setting.setOnClickListener(new OnClickListener() { public void onClick(View v) {
         * Intent intentCreate = new Intent().setClass(SearchMovieActivity.this, Setting.class); startActivityForResult(intentCreate, SETTING); } });
         */
    }

    private void setSearchBar() {
        edittextSearchBar.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            	boolean hasItem = false;
                int textlength = edittextSearchBar.getText().length();
                arraySort.clear();
                for (int i = 0; i < movieList.size(); i++) {
                    Movie movie;
                    movie = movieList.get(i);
                    String nameCH = movie.getChineseName();
                    String nameEN = movie.getEnglishName();
                    if (textlength <= nameCH.length()) {
                    	for(int j=0; j<=(nameCH.length()-textlength); j++)
    						if(edittextSearchBar.getText().toString().equalsIgnoreCase((String) nameCH.subSequence(j, j+textlength))) 
    							hasItem = true;
                    } else if (textlength <= nameEN.length() && !hasItem) {
                    	for(int j=0; j<=(nameEN.length()-textlength); j++)
    						if(edittextSearchBar.getText().toString().equalsIgnoreCase((String) nameEN.subSequence(j, j+textlength))) 
    							hasItem = true;
                    }
                	if(hasItem) {
						arraySort.add(movie);
						hasItem = false;
					}
                }
                movieAdapter.notifyDataSetChanged();
            }
        });
    }

    class LoadDataTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog progressdialogInit;

        @Override
        protected void onPreExecute() {
            progressdialogInit = ProgressDialog.show(SearchMovieActivity.this, "Load", "Loading…");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            fetchData();
            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            progressdialogInit.dismiss();
            if (movieList == null) {
                showReloadDialog(SearchMovieActivity.this);
            } else {
                setListAdatper();
                setListListener();
            }
            super.onPostExecute(result);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tast != null && tast.getStatus() != AsyncTask.Status.FINISHED)
            tast.cancel(true);

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
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

        	PromoteAPP promoteAPP = new PromoteAPP(SearchMovieActivity.this);
        	if(!promoteAPP.isPromote) {
	        	new AlertDialog.Builder(this).setTitle("- 離開程式? -")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					// do something when the button is clicked
					public void onClick(DialogInterface arg0, int arg1) {
						SearchMovieActivity.this.finish();
					}
				})
				.setNegativeButton("否", null)
				.show();
		    } else
		    	promoteAPP.promoteAPPExe();

            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }

    public void showReloadDialog(final Context context) {
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(context);

        alt_bld.setMessage("是否重新載入資料？").setCancelable(true).setPositiveButton("確定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                LoadDataTask tast = new LoadDataTask();
                if(Build.VERSION.SDK_INT < 11)
                	tast.execute();
                else
                	tast.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
                dialog.dismiss();
            }
        });

        AlertDialog alert = alt_bld.create();
        // Title for AlertDialog
        alert.setTitle("讀取錯誤");
        // Icon for AlertDialog
        // alert.setIcon(R.drawable.gnome_logout);
        alert.show();

    }
}
