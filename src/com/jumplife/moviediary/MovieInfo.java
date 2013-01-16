package com.jumplife.moviediary;

import java.io.ByteArrayOutputStream;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.TrackedActivity;
import com.jumplife.dialog.CollectDialog;
import com.jumplife.imageload.ImageLoader;
import com.jumplife.loginactivity.LoginActivity;
import com.jumplife.loginactivity.Utility;
import com.jumplife.moviediary.api.MovieAPI;
import com.jumplife.moviediary.entity.Movie;
import com.jumplife.moviediary.entity.Record;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;

public class MovieInfo extends TrackedActivity {

    private TextView                topbar_text;
    private ImageView               poster;
    private TextView                chinese_name;
    private TextView                english_name;
    private ImageView               level;
    private TextView                runningtime;
    private Button                  play_btn;
    private Button                  schedule_btn;
    private Button                  buttonCheck;
    /*
     * private ImageView imageView_friend1; private ImageView imageView_friend2; private ImageView imageView_friend3; private ImageView imageView_friend4;
     */
    private TextView                textView_time;
    private TextView                textView_drama;
    private TextView                textView_director;
    private TextView                textView_actor;
    // private TextView textView_score_section;
    // private LinearLayout linearLayout_score;
    private ImageLoader             imageLoader;
    private int                     movie_id;
    private Movie                   movie;
    /*
     * private ImageView[] friendsViews = { imageView_friend1, imageView_friend2, imageView_friend3, imageView_friend4 };
     */
    private Bitmap                  uerImg;
    private LoadDataTask            taskLoad;
    
    //private static String           TAG = "MovieInfo";
    public static ArrayList<Record> records;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movieinfo);
        Bundle extras = getIntent().getExtras();
        movie_id = extras.getInt("movie_id");
        imageLoader = new ImageLoader(MovieInfo.this);
        findViews();

        taskLoad = new LoadDataTask();
        if(Build.VERSION.SDK_INT < 11)
        	taskLoad.execute();
        else
        	taskLoad.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.d(TAG, "onActivityResult");
        //Utility.mFacebook.authorizeCallback(requestCode, resultCode, data);
        switch (requestCode) {
	        case LoginActivity.LOGIN_ACTIVITY_REQUEST_CODE:
	        	if (Utility.IsSessionValid(MovieInfo.this) && resultCode == LoginActivity.LOGIN_ACTIVITY_RESULT_CODE_SUCCESS) {
	        		ClickMovieTask task = new ClickMovieTask();
	                task.execute();
	        }
        }
    }

    private void setViews() {
        uerImg = Utility.usrImg;
        topbar_text.setText("電影資訊");
        imageLoader.DisplayImage(movie.getPosterUrl(), poster);
        chinese_name.setText(movie.getChineseName());
        english_name.setText(movie.getEnglishName());
        /*
         * if(FacebookIO.mFacebook.isSessionValid()) { if(movie.getRecordList() != null && movie.getRecordList().size() > 0) {
         * textView_score_section.setText("已經有 " + movie.getRecordList().size() + " 個朋友打卡評價了"); } else { textView_score_section.setText("尚未有朋友打卡評價"); } } else
         * textView_score_section.setText("朋友打卡評價");
         */

        imageLoader.DisplayImage(movie.getLevelUrl(), level);
        if (movie.getRunningTime() != -1)
            runningtime.setText("片長 : " + movie.getRunningTime() + "分");
        else
            runningtime.setText("片長 : 尚未提供");

        DateFormat createFormatter = new SimpleDateFormat("yyyy/MM/dd");
        if (movie.getReleaseDate() != null)
            textView_time.setText(createFormatter.format(movie.getReleaseDate()));
        else
            textView_time.setText("未提供上映日期");

        textView_drama.setText(movie.getInttroduction());

        String directors = "";
        for (String s : movie.getDirectors()) {
            directors += s + ", ";
        }

        String actors = "";
        for (String s : movie.getActors()) {
            actors += s + ", ";
        }

        textView_director.setText(directors);
        textView_actor.setText(actors);

        records = movie.getRecordList();

        // TextView textviewFBLoginPrompt = (TextView)findViewById(R.id.textView_fb_login);
        /*
         * if(!FacebookIO.mFacebook.isSessionValid()) { friendsViews[0].setImageResource(R.drawable.facebook_icon_v2);
         * friendsViews[0].setVisibility(View.VISIBLE); textviewFBLoginPrompt.setVisibility(View.VISIBLE); for(int i=1;i<friendsViews.length;i++)
         * friendsViews[i].setVisibility(View.INVISIBLE); } else { textviewFBLoginPrompt.setVisibility(View.GONE); for(int i=0;i<friendsViews.length;i++){
         * if(i<records.size()){ friendsViews[i].setVisibility(View.VISIBLE); imageLoader.DisplayImage(records.get(i).getUser().getIconUrl(), friendsViews[i]);
         * }else{ friendsViews[i].setVisibility(View.INVISIBLE); } } }
         */

        buttonCheck.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                EasyTracker.getTracker().trackEvent("電影資訊", "打卡", "", (long)0);
                EasyTracker.getTracker().trackView("url_path");
                if (Utility.IsSessionValid(MovieInfo.this)) {
                    // createGooglePlayDialog();
                    // alertDialog.show();
                    ClickMovieTask task = new ClickMovieTask();
                    task.execute();
                } else {
                	Intent newAct = new Intent(); 
                	newAct.setClass( MovieInfo.this, LoginActivity.class );
                	MovieInfo.this.startActivityForResult(newAct, LoginActivity.LOGIN_ACTIVITY_REQUEST_CODE);
                }
            }
        });
    }

    private void setListener() {
        /*
         * if(FacebookIO.mFacebook.isSessionValid()) { if(records.size() > 0 ){ linearLayout_score.setOnClickListener(new OnClickListener() { public void
         * onClick(View v) {
         * 
         * HashMap<String, String> parameters = new HashMap<String, String>(); parameters.put("records num", records.size()+"");
         * FlurryAgent.logEvent(FlurryLog.FRIEND_RECORDS_CLICK_LOG, parameters);
         * 
         * Intent newAct = new Intent(); newAct.putExtra("movie_id", movie_id); newAct.setClass( MovieInfo.this, FriendRecords.class ); startActivity( newAct );
         * } }); } } else { linearLayout_score.setOnClickListener(new OnClickListener() { public void onClick(View v) { FBLoginDialog(1); } }); }
         */

        if (movie.getYoutubeVideoUrl() == null) {
            play_btn.setVisibility(View.GONE);
            View viewLine = findViewById(R.id.view_line0);
            viewLine.setVisibility(View.GONE);
        }

        schedule_btn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	EasyTracker.getTracker().trackEvent("電影資訊", "時刻表", "", (long)0);
                Intent intentSchedule = new Intent();
                intentSchedule.putExtra("movieId", movie_id);
                intentSchedule.setClass(MovieInfo.this, MovieScheduleActivity.class);
                startActivity(intentSchedule);
            }
        });

        play_btn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(movie.getYoutubeVideoUrl())));
            }
        });
    }

    private void findViews() {
        topbar_text = (TextView) findViewById(R.id.topbar_text);
        poster = (ImageView) findViewById(R.id.imageview_movie_poster);
        chinese_name = (TextView) findViewById(R.id.textView_chinese_name);
        english_name = (TextView) findViewById(R.id.textView_english_name);
        level = (ImageView) findViewById(R.id.imageView_level);
        runningtime = (TextView) findViewById(R.id.textView_runningtime);
        play_btn = (Button) findViewById(R.id.button_video);
        schedule_btn = (Button) findViewById(R.id.button_schedule);
        buttonCheck = (Button) findViewById(R.id.button_check);
        /*
         * imageView_friend1 = (ImageView)findViewById(R.id.imageView_friend1); imageView_friend2 = (ImageView)findViewById(R.id.imageView_friend2);
         * imageView_friend3 = (ImageView)findViewById(R.id.imageView_friend3); imageView_friend4 = (ImageView)findViewById(R.id.imageView_friend4);
         */
        textView_time = (TextView) findViewById(R.id.textView_time);
        textView_drama = (TextView) findViewById(R.id.textView_drama);
        textView_director = (TextView) findViewById(R.id.textView_director);
        textView_actor = (TextView) findViewById(R.id.textView_actor);
        // linearLayout_score = (LinearLayout)findViewById(R.id.linearLayout_score);
        // textView_score_section = (TextView) findViewById(R.id.textView_section_score);
        /*
         * friendsViews[0] = imageView_friend1; friendsViews[1] = imageView_friend2; friendsViews[2] = imageView_friend3; friendsViews[3] = imageView_friend4;
         */

    }

    private void fetchData() {
        MovieAPI movieAPI = new MovieAPI();
        String fb_id = null;
        if(Utility.IsSessionValid(MovieInfo.this))
        	fb_id = Utility.usrId;
        movie = movieAPI.getMovieMoreInfo(movie_id, fb_id);
    }

    class LoadDataTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
                                                          public void onCancel(DialogInterface arg0) {
                                                              Log.d("", "loadDataTask.getStatus() != AsyncTask.Status.FINISHED");
                                                              LoadDataTask.this.cancel(true);
                                                              finish();
                                                          }
                                                      };

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(MovieInfo.this);
            progressdialogInit.setTitle("Load");
            progressdialogInit.setMessage("Loading…");
            progressdialogInit.setOnCancelListener(cancelListener);
            progressdialogInit.setCanceledOnTouchOutside(false);
            progressdialogInit.show();
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
            if (movie == null) {
                showReloadDialog(MovieInfo.this);
            } else {
                setViews();
                setListener();
            }
            super.onPostExecute(result);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (taskLoad != null && taskLoad.getStatus() != AsyncTask.Status.FINISHED)
            taskLoad.cancel(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        // tracker.stopSession();
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();
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

    class ClickMovieTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
                                                          public void onCancel(DialogInterface arg0) {
                                                              Log.d("", "loadDataTask.getStatus() != AsyncTask.Status.FINISHED");
                                                              ClickMovieTask.this.cancel(true);
                                                          }
                                                      };

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(MovieInfo.this);
            progressdialogInit.setTitle("Load");
            progressdialogInit.setMessage("Loading…");
            progressdialogInit.setOnCancelListener(cancelListener);
            progressdialogInit.setCanceledOnTouchOutside(false);
            progressdialogInit.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
            ByteArrayOutputStream bsUser = new ByteArrayOutputStream();

            Intent intentMain = new Intent();
            if (uerImg != null) {
                uerImg.compress(Bitmap.CompressFormat.PNG, 50, bsUser);
                intentMain.putExtra("userImage", bsUser.toByteArray());
            }

            intentMain.putExtra("id", movie.getId());
            intentMain.putExtra("chineseName", movie.getChineseName());
            intentMain.putExtra("posterUrl", movie.getPosterUrl());

            intentMain.setClass(MovieInfo.this, CollectDialog.class);
            startActivity(intentMain);
            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            progressdialogInit.dismiss();
            super.onPostExecute(result);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
        	SharePreferenceIO shIO = new SharePreferenceIO(this);
        	shIO.SharePreferenceI("comment_tmp", "");
        }
        return super.onKeyDown(keyCode, event);
    }
}
