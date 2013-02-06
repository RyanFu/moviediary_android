package com.jumplife.moviediary;

import java.util.ArrayList;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.TrackedActivity;
import com.jumplife.moviediary.api.MovieAPI;
import com.jumplife.moviediary.entity.Movie;
import com.jumplife.moviediary.promote.PromoteAPP;
import com.jumplife.sectionlistview.MovieGridAdapter;
import com.jumplife.setting.Setting;

public class MovieWaterfall extends TrackedActivity {
    public final static int  SETTING          = 1;

    private GridView         movieGridView;
    private TextView         title;
    private Button           selectButton;
    private ImageButton      setting;
    private ImageButton      search;
    private ImageButton      imageButtonRefresh;
    private QuickAction      quickAction;
    private LinearLayout     linearlayout;

    private int              screenWidth;
    
    private LoadDataTask     loadTask;

    private ArrayList<Movie> movieList;

    private final int        FLAG_FIRSTROUND  = 1;
    private final int        FLAG_SECONDROUND = 2;
    private final int        FLAG_THIS_WEEK   = 3;
    private final int        FLAG_RECENT      = 4;
    private final int        FLAG_TOP         = 5;

    private int              functionFlag     = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        movieList = new ArrayList<Movie>(30);

        setContentView(R.layout.activity_moviewaterfall);
        functionFlag = FLAG_TOP;

        initViews();

        loadTask = new LoadDataTask();
        if(Build.VERSION.SDK_INT < 11)
        	loadTask.execute();
        else
        	loadTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

        	PromoteAPP promoteAPP = new PromoteAPP(MovieWaterfall.this);
        	if(!promoteAPP.isPromote) {
	        	new AlertDialog.Builder(this).setTitle("- 離開程式? -")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					// do something when the button is clicked
					public void onClick(DialogInterface arg0, int arg1) {
						MovieWaterfall.this.finish();
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

    private void initViews() {
        setting = (ImageButton) findViewById(R.id.imagebutton_setting);
        search = (ImageButton) findViewById(R.id.imagebutton_search);

        ActionItem firstItem = new ActionItem(FLAG_FIRSTROUND, "首輪電影");
        ActionItem secondItem = new ActionItem(FLAG_SECONDROUND, "二輪電影");
        ActionItem thisweekItem = new ActionItem(FLAG_THIS_WEEK, "本周新片");
        ActionItem recentItem = new ActionItem(FLAG_RECENT, "近期上映");
        ActionItem topItem = new ActionItem(FLAG_TOP, "票房排行");

        quickAction = new QuickAction(this, QuickAction.VERTICAL);
        // quickAction = new QuickAction(this, QuickAction.HORIZONTAL);

        // add action items into QuickAction
        quickAction.addActionItem(firstItem);
        quickAction.addActionItem(secondItem);
        quickAction.addActionItem(thisweekItem);
        quickAction.addActionItem(recentItem);
        quickAction.addActionItem(topItem);

        // Set listener for action item clicked
        quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            public void onItemClick(QuickAction source, int pos, int actionId) {
                quickAction.getActionItem(pos);

                if (actionId == FLAG_FIRSTROUND) {
                	EasyTracker.getTracker().trackEvent("電影打卡瀑布流", "首輪電影", "", (long)0);
                    functionFlag = FLAG_FIRSTROUND;
                } else if (actionId == FLAG_SECONDROUND) {
                	EasyTracker.getTracker().trackEvent("電影打卡瀑布流", "二輪電影", "", (long)0);
                    functionFlag = FLAG_SECONDROUND;
                } else if (actionId == FLAG_THIS_WEEK) {
                	EasyTracker.getTracker().trackEvent("電影打卡瀑布流", "本周新片", "", (long)0);
                    functionFlag = FLAG_THIS_WEEK;
                } else if (actionId == FLAG_RECENT) {
                	EasyTracker.getTracker().trackEvent("電影打卡瀑布流", "近期上映", "", (long)0);
                    functionFlag = FLAG_RECENT;
                } else if (actionId == FLAG_TOP) {
                	EasyTracker.getTracker().trackEvent("電影打卡瀑布流", "票房排行", "", (long)0);
                    functionFlag = FLAG_TOP;
                }

                loadTask = new LoadDataTask();
                if(Build.VERSION.SDK_INT < 11)
                	loadTask.execute();
                else
                	loadTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
            }
        });

        // set listnener for on dismiss event, this listener will be called only if QuickAction dialog was dismissed
        // by clicking the area outside the dialog.
        quickAction.setOnDismissListener(new QuickAction.OnDismissListener() {
            public void onDismiss() {
                // Toast.makeText(getApplicationContext(), "Dismissed", Toast.LENGTH_SHORT).show();
            }
        });

        linearlayout = (LinearLayout) findViewById(R.id.waterfall_linear_layout);
        imageButtonRefresh = (ImageButton) findViewById(R.id.refresh);
        imageButtonRefresh.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                Log.d("", "Click imageButtonRefresh");
                LoadDataTask task = new LoadDataTask();
                if(Build.VERSION.SDK_INT < 11)
                	task.execute();
                else
                	task.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
            }
        });
    }

    // 設定畫面上的UI
    private void setViews() {

        title = (TextView) findViewById(R.id.topbar_text1);
        selectButton = (Button) findViewById(R.id.button_select);
        selectButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                quickAction.show(v);
            }
        });

        setting.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intentCreate = new Intent().setClass(MovieWaterfall.this, Setting.class);
                startActivityForResult(intentCreate, SETTING);
            }
        });
        
        search.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intentCreate = new Intent().setClass(MovieWaterfall.this, SearchMovieActivity.class);
                startActivity(intentCreate);
            }
        });

        // 抓取畫面大小
        DisplayMetrics displayMetrics = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        screenWidth = displayMetrics.widthPixels;

        // 建立gridview
        movieGridView = new GridView(this);

        // 設定gridview 格式
        movieGridView.setNumColumns(2);
        movieGridView.setHorizontalSpacing(15);
        movieGridView.setVerticalSpacing(15);
        movieGridView.setColumnWidth((screenWidth / 2));
        movieGridView.setGravity(Gravity.TOP);
        movieGridView.setSelector(R.drawable.button_background);

        // recordGridView.setAdapter(new RecordGridAdapter(MyMovieRecord.this, recordList));

        movieGridView.setAdapter(new MovieGridAdapter(MovieWaterfall.this, movieList, ((screenWidth / 2) - 40), (int) (((screenWidth / 2) - 40) * 1.5)));

        if (functionFlag == FLAG_FIRSTROUND) {
            title.setText("首輪電影");
        } else if (functionFlag == FLAG_SECONDROUND) {
            title.setText("二輪電影");
        } else if (functionFlag == FLAG_RECENT) {
            title.setText("近期上映");
        } else if (functionFlag == FLAG_THIS_WEEK) {
            title.setText("本周新片");
        } else if (functionFlag == FLAG_TOP) {
            title.setText("票房排行");
            for (int i = 0; i < movieList.size(); i++) {
                String movieName = movieList.get(i).getChineseName();
                movieList.get(i).setChineseName((i + 1) + ". " + movieName);
            }
        }

        movieGridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent newAct = new Intent();
                newAct.putExtra("movie_id", movieList.get(position).getId());

                newAct.setClass(MovieWaterfall.this, MovieShowActivity.class);
                startActivity(newAct);
            }
        });

        linearlayout.removeAllViews();

        linearlayout.addView(movieGridView);
    }

    private void fetchData() {
        MovieAPI movieAPI = new MovieAPI();

        if (functionFlag == FLAG_FIRSTROUND) {
            movieList = movieAPI.getMovieList(MovieAPI.FILTER_FIRST_ROUND);
        } else if (functionFlag == FLAG_SECONDROUND) {
            movieList = movieAPI.getMovieList(MovieAPI.FILTER_SECOND_ROUND);
        } else if (functionFlag == FLAG_THIS_WEEK) {
            movieList = movieAPI.getMovieList(MovieAPI.FILTER_THIS_WEEK);
        } else if (functionFlag == FLAG_RECENT) {
            movieList = movieAPI.getMovieList(MovieAPI.FILTER_RECENT);
        } else if (functionFlag == FLAG_TOP) {
            movieList = movieAPI.getMovieList(MovieAPI.FILTER_TOP_10);
        }
    }

    class LoadDataTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
                                                          public void onCancel(DialogInterface arg0) {
                                                              LoadDataTask.this.cancel(true);
                                                              linearlayout.removeAllViews();
                                                              imageButtonRefresh.setVisibility(View.VISIBLE);
                                                          }
                                                      };

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(MovieWaterfall.this);
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
        	if(MovieWaterfall.this != null && !MovieWaterfall.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();

            if (movieList == null) {
                // showReloadDialog(MovieWaterfall.this);
                linearlayout.removeAllViews();
                imageButtonRefresh.setVisibility(View.VISIBLE);
            } else {
                imageButtonRefresh.setVisibility(View.GONE);
                setViews();
            }

            super.onPostExecute(result);
        }

    }

    public void showReloadDialog(final Context context) {
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(context);

        alt_bld.setMessage("是否重新載入資料？").setCancelable(true).setPositiveButton("確定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                loadTask = new LoadDataTask();
                if(Build.VERSION.SDK_INT < 11)
                	loadTask.execute();
                else
                	loadTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
                dialog.dismiss();
            }
        });

        AlertDialog alert = alt_bld.create();
        alert.setTitle("讀取錯誤");
        alert.setCancelable(false);
        alert.show();

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
}
