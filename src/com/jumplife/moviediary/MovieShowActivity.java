package com.jumplife.moviediary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.TrackedActivity;
import com.jumplife.dialog.CollectDialog;
import com.jumplife.imageload.ImageLoader;
import com.jumplife.loginactivity.FacebookIO;
import com.jumplife.loginactivity.LoginActivity;
import com.jumplife.loginactivity.Utility;
import com.jumplife.moviediary.api.MovieAPI;
import com.jumplife.moviediary.entity.Movie;
import com.jumplife.moviediary.entity.Record;
import com.jumplife.sectionlistview.RecordListAdapter;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;

public class MovieShowActivity extends TrackedActivity {

    private ImageView         poster;
    private TextView          goodRate;
    private TextView          ezCheckText;
    private TextView          topbarText;
    private ListView          listviewShow;
    private View              viewHeader;
    private View              viewFooter;
    private Button            buttonFriend;
    private Button            buttonAll;
    private Button            buttonCheck;
	private ImageButton 	  imageAddFriend;
	private ImageButton 	  imageButtonRefresh;
    private ImageButton		  imageButtonMovieInfo;
    private ImageButton		  imageButtonEzCheck;
    private View			  viewFriend;
    private View			  viewAll;
    private LoadDataTask      loadDataTask;

    private String            fb_id;
    private Movie             movie;
    //private int               movie_id;
    private RecordListAdapter recordListAdapter;
    private ArrayList<Record> recordList;
    
    private int functionFlag = 0;
    private final int FLAG_ALL = 1;
    private final int FLAG_FRIEND = 2;
    
    public final static int   CHECK        = 1;
    public final static int   CHECK_SUCESS = 10;
    public final static int   CHECK_FAIL   = 11;
    
    public final static int   BUTTON_FRIEND = 15;
    public final static int   BUTTON_CHECK = 16;

    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movieshow);
        findViews();
        loadDataTask = new LoadDataTask();
        if(Build.VERSION.SDK_INT < 11)
        	loadDataTask.execute();
        else
        	loadDataTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
        
        
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
	        case CHECK:
	        	if (resultCode == CHECK_SUCESS) {
                    initBottonTextViewColor();
                	initBottonImageViewVisible();
                	functionFlag = FLAG_ALL;
                	setBottonView();
	        		LoadRecordTask loadRecordTask = new LoadRecordTask(false);
	                loadRecordTask.execute();
	            }
	            break;
	
	        case BUTTON_FRIEND:
	        	if (Utility.IsSessionValid(MovieShowActivity.this) && resultCode == LoginActivity.LOGIN_ACTIVITY_RESULT_CODE_SUCCESS) {
	        		initBottonTextViewColor();
                	initBottonImageViewVisible();
                	functionFlag = FLAG_FRIEND;
                	setBottonView();
	        		LoadRecordTask loadRecordTask = new LoadRecordTask(true);
	                loadRecordTask.execute();
		        } 
	        	break;
	        
	        case BUTTON_CHECK:
	        	if (Utility.IsSessionValid(MovieShowActivity.this) && resultCode == LoginActivity.LOGIN_ACTIVITY_RESULT_CODE_SUCCESS) {
	        		ClickMovieTask task = new ClickMovieTask();
                    task.execute();
		        } 
	        	break;
	        	
	        case MovieRecord.RECORD_LIKE_REQUEST_CODE:
	        	if (resultCode == MovieRecord.RECORD_LIKE_RESULT_CODE_SUCCESS) {
	        		initBottonTextViewColor();
                	initBottonImageViewVisible();
                	functionFlag = FLAG_ALL;
                	setBottonView();
	        		LoadRecordTask loadRecordTask = new LoadRecordTask(false);
	                loadRecordTask.execute();
		        }
	        	break;
	        	
	        case LoginActivity.LOGIN_ACTIVITY_REQUEST_CODE_LIKE:
	        	if (resultCode == LoginActivity.LOGIN_ACTIVITY_RESULT_CODE_SUCCESS) {
	        		initBottonTextViewColor();
                	initBottonImageViewVisible();
                	functionFlag = FLAG_ALL;
                	setBottonView();
	        		LoadRecordTask loadRecordTask = new LoadRecordTask(false);
	                loadRecordTask.execute();
		        }
	        	break;
        }
    }

    @Override
    protected void onDestroy() {
        if (loadDataTask != null && loadDataTask.getStatus() != AsyncTask.Status.FINISHED)
            loadDataTask.cancel(true);
        super.onDestroy();
    }

    private void findViews() {
    	Bundle extras = getIntent().getExtras();
        movie = new Movie();
        movie.setId(extras.getInt("movie_id"));
        
        listviewShow = (ListView) findViewById(R.id.listview_checks);
        buttonCheck = (Button) findViewById(R.id.button_check);

        viewHeader = LayoutInflater.from(MovieShowActivity.this).inflate(R.layout.listview_movieshow_header, null);
        viewFooter = LayoutInflater.from(MovieShowActivity.this).inflate(R.layout.listview_movieshow_footer, null);
        poster = (ImageView) viewHeader.findViewById(R.id.imageview_movie_poster);

        topbarText = (TextView) findViewById(R.id.topbar_text);

        goodRate = (TextView) viewHeader.findViewById(R.id.good_rate);
        ezCheckText = (TextView) viewHeader.findViewById(R.id.tv_ezcheck);
        buttonFriend = (Button) viewHeader.findViewById(R.id.button_friend);
        buttonAll = (Button) viewHeader.findViewById(R.id.button_all);
    	viewAll = (View) viewHeader.findViewById(R.id.view_1);
        viewFriend = (View) viewHeader.findViewById(R.id.view_2);
        
        imageButtonEzCheck = (ImageButton) viewHeader.findViewById(R.id.ivbtn_ezcheck);
        
        imageButtonMovieInfo = (ImageButton) viewHeader.findViewById(R.id.ivbtn_more);
        imageButtonMovieInfo.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				Intent newAct = new Intent();
                newAct.putExtra("movie_id", movie.getId());
                newAct.setClass(MovieShowActivity.this, MovieInfo.class);
                startActivity(newAct);
			}        	
        });
        
        imageAddFriend = (ImageButton)findViewById(R.id.imagebutton_addfriend);
        imageAddFriend.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				if (!Utility.IsSessionValid(MovieShowActivity.this)) {
                	Intent newAct = new Intent(); 
                	newAct.setClass( MovieShowActivity.this, LoginActivity.class );
                	MovieShowActivity.this.startActivityForResult(newAct, LoginActivity.LOGIN_ACTIVITY_REQUEST_CODE);
                } else {
                	FacebookIO fbIO = new FacebookIO(MovieShowActivity.this);
                	fbIO.requestDialog(Utility.usrName + "邀請你一起來用電影櫃 (一個收藏與分享電影的好Android APP : http://goo.gl/q1Zzu)");
                }
			}			
		});
		
        imageButtonRefresh = (ImageButton)findViewById(R.id.refresh);
		imageButtonRefresh.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				loadDataTask = new LoadDataTask();
				if(Build.VERSION.SDK_INT < 11)
					loadDataTask.execute();
		        else
		        	loadDataTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
			}			
		});
    }

    private void fetchData() {
        if(Utility.IsSessionValid(MovieShowActivity.this))
        	fb_id = Utility.usrId;
        
        /*if(movie.getChineseName() == null || movie.getPosterUrl() == null) {
        	Log.d("", "poster or name == null");
        	movie = movieAPI.getMovieMoreInfo(movie.getId(), fb_id);
        }*/
        MovieAPI movieAPI = new MovieAPI();
        //movie = movieAPI.getMovieInfo(movie);
        movie = movieAPI.getMovieMoreInfo(movie.getId(), fb_id);
        recordList = new ArrayList<Record>();
        recordList = movieAPI.getMovieRecordList(fb_id, movie.getId() + "");
    }

    private void setView() {

        topbarText.setText(movie.getChineseName());
        
        ImageLoader imageLoader = new ImageLoader(MovieShowActivity.this, 140);
        //imageLoader.DisplayImage(movie.getLevelUrl(), level);
        imageLoader.DisplayImage(movie.getPosterUrl(), poster);

        int rate = 0;
        int goodCount = 0;
        for(int i = 0; i < recordList.size(); i++)
        	if(recordList.get(i).getScore() == MovieAPI.SCORE_GOOD)
        		goodCount += 1;
        if(goodCount != 0 && recordList.size() != 0)
        	rate = goodCount * 100 / recordList.size();
        String styledText = "好評度 : <font color='red'>" + rate + "%</font>";
        goodRate.setText(Html.fromHtml(styledText), TextView.BufferType.SPANNABLE);
        
        poster.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent newAct = new Intent();
                newAct.putExtra("movie_id", movie.getId());
                newAct.setClass(MovieShowActivity.this, MovieInfo.class);
                startActivity(newAct);
            }
        });
        
        if(movie.getIsEzding()) {
        	ezCheckText.setTextColor(getResources().getColor(R.color.white2));
        	imageButtonEzCheck.setClickable(true);
        	imageButtonEzCheck.setImageDrawable(getResources().getDrawable(R.drawable.button_ezcheck));
			imageButtonEzCheck.setOnClickListener(new OnClickListener() {
		    	public void onClick(View v) {
		    		Intent newAct = new Intent();
	                newAct.setClass(MovieShowActivity.this, EzCheckActivity.class);
	                startActivity(newAct);
				}
			});
		}

        buttonFriend.setOnClickListener(new RelativeLayout.OnClickListener() {
            public void onClick(View v) {
            	EasyTracker.getTracker().trackEvent("電影打卡列表", "朋友打卡", "", (long)0);
                if (!Utility.IsSessionValid(MovieShowActivity.this)) {
                	Intent newAct = new Intent(); 
                	newAct.setClass( MovieShowActivity.this, LoginActivity.class );
                	MovieShowActivity.this.startActivityForResult(newAct, BUTTON_FRIEND);
                } else {
                	initBottonTextViewColor();
                	initBottonImageViewVisible();
                	functionFlag = FLAG_FRIEND;
                	setBottonView();
                    LoadRecordTask loadRecordTask = new LoadRecordTask(true);
                    loadRecordTask.execute();
                }
            }
        });

        buttonAll.setOnClickListener(new RelativeLayout.OnClickListener() {
            public void onClick(View v) {
                EasyTracker.getTracker().trackEvent("電影打卡列表", "所有打卡", "", (long)0);
                initBottonTextViewColor();
            	initBottonImageViewVisible();
            	functionFlag = FLAG_ALL;
            	setBottonView();
                LoadRecordTask loadRecordTask = new LoadRecordTask(false);
                loadRecordTask.execute();
            }
        });

        buttonCheck.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (Utility.IsSessionValid(MovieShowActivity.this)) {
                	ClickMovieTask task = new ClickMovieTask();
                    task.execute();
                } else {
                	Intent newAct = new Intent(); 
                	newAct.setClass( MovieShowActivity.this, LoginActivity.class );
                	MovieShowActivity.this.startActivityForResult(newAct, BUTTON_CHECK);
                }
            }
        });

        listviewShow.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	if(recordList.size() > 0) {
	                Intent newAct = new Intent();
	                newAct.putExtra("record_id", recordList.get(position - 1).getId());
	                newAct.putExtra("Owner", false);
	                newAct.setClass(MovieShowActivity.this, MovieRecord.class);
	                startActivityForResult(newAct, MovieRecord.RECORD_LIKE_REQUEST_CODE);
            	}
            }

        });

        sortRecordList();
        listviewShow.removeHeaderView(viewHeader);
        listviewShow.addHeaderView(viewHeader);
        recordListAdapter = new RecordListAdapter(MovieShowActivity.this, recordList, fb_id);
        listviewShow.setAdapter(recordListAdapter);

        if (recordList.size() == 0)
            listviewShow.addFooterView(viewFooter);
        else
            listviewShow.removeFooterView(viewFooter);
        
    	functionFlag = FLAG_ALL;
        initBottonTextViewColor();
    	initBottonImageViewVisible();
    	setBottonView();
    }

    private void initBottonTextViewColor() {
    	buttonAll.setTextColor(MovieShowActivity.this.getResources().getColor(R.color.fake_tab_button_text_normal));
    	buttonFriend.setTextColor(MovieShowActivity.this.getResources().getColor(R.color.fake_tab_button_text_normal));
    }
    
    private void initBottonImageViewVisible() {
    	viewAll.setVisibility(View.INVISIBLE);
    	viewFriend.setVisibility(View.INVISIBLE);
    }
    
    private void setBottonView() {
    	if(functionFlag == FLAG_ALL) {
    		buttonAll.setTextColor(MovieShowActivity.this.getResources().getColor(R.color.fake_tab_button_text_press));
    		viewAll.setVisibility(View.VISIBLE);
    	} else if(functionFlag == FLAG_FRIEND) {	    	
    		buttonFriend.setTextColor(MovieShowActivity.this.getResources().getColor(R.color.fake_tab_button_text_press));
    		viewFriend.setVisibility(View.VISIBLE);
    	}
    }

    class LoadDataTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
	          public void onCancel(DialogInterface arg0) {
	              LoadDataTask.this.cancel(true);
	              listviewShow.setVisibility(View.GONE);
                  imageButtonRefresh.setVisibility(View.VISIBLE);
	              finish();
	          }
	      };

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(MovieShowActivity.this);
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
            if (movie == null || recordList == null) {
            	listviewShow.setVisibility(View.GONE);
                imageButtonRefresh.setVisibility(View.VISIBLE);
            } else {
                setView();
                listviewShow.setVisibility(View.VISIBLE);
                imageButtonRefresh.setVisibility(View.GONE);
            }
            super.onPostExecute(result);
        }

    }

    class LoadRecordTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
	          public void onCancel(DialogInterface arg0) {
	              Log.d("", "loadDataTask.getStatus() != AsyncTask.Status.FINISHED");
	              listviewShow.setVisibility(View.GONE);
	              imageButtonRefresh.setVisibility(View.VISIBLE);
	              LoadRecordTask.this.cancel(true);
	          }
	      };

        private final boolean          onlyfriend;

        public LoadRecordTask(Boolean onlyfriend) {
            this.onlyfriend = onlyfriend;
        }

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(MovieShowActivity.this);
            progressdialogInit.setTitle("Load");
            progressdialogInit.setMessage("Loading…");
            progressdialogInit.setOnCancelListener(cancelListener);
            progressdialogInit.setCanceledOnTouchOutside(false);
            progressdialogInit.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
            MovieAPI movieAPI = new MovieAPI();
            recordList.clear();
            recordList = new ArrayList<Record>();
            
            if(Utility.IsSessionValid(MovieShowActivity.this))
            	fb_id = Utility.usrId;
            if (onlyfriend) {
                recordList = movieAPI.getMovieFriendRecordList(fb_id, movie.getId() + "");
            } else {
                recordList = movieAPI.getMovieRecordList(fb_id, movie.getId() + "");
            }

            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            progressdialogInit.dismiss();
            if (recordList == null) {
            	listviewShow.setVisibility(View.GONE);
                imageButtonRefresh.setVisibility(View.VISIBLE);
            } else {
                if (recordList.size() == 0) {
                    listviewShow.removeFooterView(viewFooter);
                    listviewShow.addFooterView(viewFooter);
                } else {
                    listviewShow.removeFooterView(viewFooter);
                    sortRecordList();
                    recordListAdapter = new RecordListAdapter(MovieShowActivity.this, recordList, fb_id);
                    listviewShow.setAdapter(recordListAdapter);
                }
                listviewShow.setVisibility(View.VISIBLE);
                imageButtonRefresh.setVisibility(View.GONE);
            }
            super.onPostExecute(result);
        }

    }

    public void showReloadDialog(final Context context) {
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(context);

        alt_bld.setMessage("是否重新載入資料？").setCancelable(true).setPositiveButton("確定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                loadDataTask = new LoadDataTask();
                if(Build.VERSION.SDK_INT < 11)
                	loadDataTask.execute();
                else
                	loadDataTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
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
            progressdialogInit = new ProgressDialog(MovieShowActivity.this);
            progressdialogInit.setTitle("Load");
            progressdialogInit.setMessage("Loading…");
            progressdialogInit.setOnCancelListener(cancelListener);
            progressdialogInit.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
            Intent intentMain = new Intent();
            
            EasyTracker.getTracker().trackEvent("電影打卡列表", "打卡", movie.getChineseName(), (long)0);

            intentMain.putExtra("id", movie.getId());
            intentMain.putExtra("chineseName", movie.getChineseName());
            intentMain.putExtra("posterUrl", movie.getPosterUrl());

            intentMain.setClass(MovieShowActivity.this, CollectDialog.class);
            startActivityForResult(intentMain, CHECK);

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

    private void sortRecordList() {
        ArrayList<Record> haveContent = new ArrayList<Record>(8);
        ArrayList<Record> nonhaveContent = new ArrayList<Record>(8);

        Collections.sort(recordList, new Comparator<Record>() {
            public int compare(Record record1, Record record2) {
                return record2.getLoveCount() - record1.getLoveCount();

            }
        });

        int size = recordList.size();
        for (int i = 0; i < size; i++) {
            if (recordList.get(i).getComment().equals(""))
                nonhaveContent.add(recordList.get(i));
            else
                haveContent.add(recordList.get(i));
        }
        recordList.clear();
        recordList = new ArrayList<Record>();
        
        for (int i = 0; i < haveContent.size(); i++)
            recordList.add(haveContent.get(i));
        for (int i = 0; i < nonhaveContent.size(); i++)
            recordList.add(nonhaveContent.get(i));
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
        	SharePreferenceIO shIO = new SharePreferenceIO(this);
        	shIO.SharePreferenceI("comment_tmp", "");
        }
        
        return super.onKeyDown(keyCode, event);
    }

	@Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(this);
      EasyTracker.getTracker().trackView("/campaign/checkin/list");
    }
    
    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
