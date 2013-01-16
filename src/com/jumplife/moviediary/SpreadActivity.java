package com.jumplife.moviediary;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.TrackedActivity;
import com.jumplife.moviediary.api.MovieAPI;
import com.jumplife.moviediary.entity.Spread;
import com.jumplife.moviediary.promote.PromoteAPP;
import com.jumplife.sectionlistview.SpreadListAdapter;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;

public class SpreadActivity extends TrackedActivity {

    private ArrayList<Spread> spreadCurrentList = null;
    private ArrayList<Spread> spreadResultList = null;
    private ListView spreadListView;
    private Button buttonCurrent;
    private Button buttonResult;
    private View viewCurrent;
    private View viewResult;
	private ImageButton imageButtonRefresh;
    private LoadDataTask tast;
    private SpreadListAdapter spreadAdapter;

    private SharePreferenceIO shIO;
    
    private final int FLAG_CURRENT = 1;
    private final int FLAG_RESULT = 2;
    
    private int functionFlag = 0;
    //private static String TAG = "SpreadActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spread_list);
        
       
        
        shIO = new SharePreferenceIO(this);
        functionFlag = shIO.SharePreferenceO("tvchannel_flag", 1);
        
        findViews();
        
        tast = new LoadDataTask();
        if(Build.VERSION.SDK_INT < 11)
        	tast.execute();
        else
        	tast.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
    }

    private void setListListener() {
    	spreadListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent newAct = new Intent();
                
                if(functionFlag == FLAG_CURRENT && spreadCurrentList != null) {
                	EasyTracker.getTracker().trackEvent("活動列表", "進入活動介紹", "活動 id:" + spreadCurrentList.get(position).getId(), (long)0);
                	
                	newAct.putExtra("spread_id", spreadCurrentList.get(position).getId());
                	newAct.putExtra("spread_type", functionFlag);
	                newAct.setClass(SpreadActivity.this, SpreadInfoActivity.class);
	                startActivity(newAct);
                } else if(functionFlag == FLAG_RESULT && spreadResultList != null) {
                	EasyTracker.getTracker().trackEvent("活動列表", "進入名單公布", "活動 id:" + spreadResultList.get(position).getId(), (long)0);
                	newAct.putExtra("spread_id", spreadResultList.get(position).getId());
                	newAct.putExtra("spread_type", functionFlag);
	                newAct.setClass(SpreadActivity.this, SpreadInfoActivity.class);
	                startActivity(newAct);
                }                
            }
        });
    }

    private void fetchData() {
    	MovieAPI movieAPI = new MovieAPI();
    	if(functionFlag == FLAG_CURRENT && spreadCurrentList == null) {
            spreadCurrentList = movieAPI.getCurrentSpreadList();
    	} else if(functionFlag == FLAG_RESULT && spreadResultList == null) {
            spreadResultList = movieAPI.getResultSpreadList();
    	}
    }

    private void setListAdatper() {
    	if(functionFlag == FLAG_CURRENT) 
    		spreadAdapter = new SpreadListAdapter(SpreadActivity.this, spreadCurrentList);
    	else if(functionFlag == FLAG_RESULT)
    		spreadAdapter = new SpreadListAdapter(SpreadActivity.this, spreadResultList);
        
        spreadListView.setAdapter(spreadAdapter);
    }

    private void findViews() {
    	spreadListView = (ListView) findViewById(R.id.listview_spread);
    	viewCurrent = (View)findViewById(R.id.view_1);
    	viewResult = (View)findViewById(R.id.view_2);
    	
		imageButtonRefresh = (ImageButton)findViewById(R.id.refresh);
		imageButtonRefresh.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				tast = new LoadDataTask();
		        if(Build.VERSION.SDK_INT < 11)
		        	tast.execute();
		        else
		        	tast.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
			}			
		});
		buttonCurrent = (Button)findViewById(R.id.button_spread_current);
		buttonCurrent.setOnClickListener(new OnClickListener() {
            @SuppressLint("NewApi")
			public void onClick(View arg0) {
            	initBottonTextViewColor();
            	initBottonImageViewVisible();
            	functionFlag = FLAG_CURRENT;
            	setBottonView();
            	shIO.SharePreferenceI("tvchannel_flag", functionFlag);
            	
            	if (tast != null && tast.getStatus() != AsyncTask.Status.FINISHED) {
                	tast.closeProgressDilog();
                    tast.cancel(true);
                }
            	tast = new LoadDataTask();
                if(Build.VERSION.SDK_INT < 11)
                	tast.execute();
                else
                	tast.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
            }
        });
		buttonResult = (Button)findViewById(R.id.button_spread_result);
		buttonResult.setOnClickListener(new OnClickListener() {
            @SuppressLint("NewApi")
			public void onClick(View arg0) {
            	initBottonTextViewColor();
            	initBottonImageViewVisible();
            	functionFlag = FLAG_RESULT;
            	setBottonView();
            	shIO.SharePreferenceI("tvchannel_flag", functionFlag);

            	if (tast != null && tast.getStatus() != AsyncTask.Status.FINISHED) {
                	tast.closeProgressDilog();
                    tast.cancel(true);
                }
            	tast = new LoadDataTask();
                if(Build.VERSION.SDK_INT < 11)
                	tast.execute();
                else
                	tast.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
            }
        });
		
		initBottonTextViewColor();
    	initBottonImageViewVisible();
    	setBottonView();
    }

    private void initBottonTextViewColor() {
    	buttonCurrent.setTextColor(SpreadActivity.this.getResources().getColor(R.color.fake_tab_button_text_normal));
    	buttonResult.setTextColor(SpreadActivity.this.getResources().getColor(R.color.fake_tab_button_text_normal));
    }
    
    private void initBottonImageViewVisible() {
    	viewCurrent.setVisibility(View.INVISIBLE);
    	viewResult.setVisibility(View.INVISIBLE);
    }
    
    private void setBottonView() {
    	if(functionFlag == FLAG_CURRENT) {
    		buttonCurrent.setTextColor(SpreadActivity.this.getResources().getColor(R.color.fake_tab_button_text_press));
    		viewCurrent.setVisibility(View.VISIBLE);
    	} else if(functionFlag == FLAG_RESULT) {	    	
    		buttonResult.setTextColor(SpreadActivity.this.getResources().getColor(R.color.fake_tab_button_text_press));
    		viewResult.setVisibility(View.VISIBLE);
    	}
    }

    class LoadDataTask extends AsyncTask<Integer, Integer, String> {

    	private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
              public void onCancel(DialogInterface arg0) {
            	  LoadDataTask.this.cancel(true);
            	  spreadListView.setVisibility(View.GONE);
                  imageButtonRefresh.setVisibility(View.VISIBLE);
              }
          };

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(SpreadActivity.this);
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
        	closeProgressDilog();
        	
        	if(functionFlag == FLAG_CURRENT && spreadCurrentList == null) { 
        		spreadListView.setVisibility(View.GONE);
                imageButtonRefresh.setVisibility(View.VISIBLE);
        	} else if(functionFlag == FLAG_RESULT && spreadResultList == null) {
        		spreadListView.setVisibility(View.GONE);
                imageButtonRefresh.setVisibility(View.VISIBLE);
        	} else {
                setListAdatper();
                setListListener();
                spreadListView.setVisibility(View.VISIBLE);
                imageButtonRefresh.setVisibility(View.GONE);
            }
            super.onPostExecute(result);
        }
        
        public void closeProgressDilog() {
        	if(SpreadActivity.this != null && !SpreadActivity.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tast != null && tast.getStatus() != AsyncTask.Status.FINISHED) {
        	tast.closeProgressDilog();
            tast.cancel(true);
        }

    }

	@Override
    public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
		EasyTracker.getTracker().trackView("/campaign/index");
		EasyTracker.getTracker().trackEvent("內部宣傳活動", "活動列表", null, (long)0);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

        	PromoteAPP promoteAPP = new PromoteAPP(SpreadActivity.this);
        	if(!promoteAPP.isPromote) {
	        	new AlertDialog.Builder(this).setTitle("- 離開程式? -")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					// do something when the button is clicked
					public void onClick(DialogInterface arg0, int arg1) {
						SpreadActivity.this.finish();
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
