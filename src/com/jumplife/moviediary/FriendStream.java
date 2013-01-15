package com.jumplife.moviediary;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.analytics.tracking.android.TrackedActivity;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.jumplife.jome.entity.Comment;
import com.jumplife.loginactivity.FacebookIO;
import com.jumplife.loginactivity.Utility;
import com.jumplife.moviediary.api.MovieAPI;
import com.jumplife.moviediary.entity.Record;
import com.jumplife.moviediary.promote.PromoteAPP;
import com.jumplife.sectionlistview.FriendStreamAdapter;

public class FriendStream extends TrackedActivity {

    // private PullDownRenewListview recordListView;
    private PullToRefreshListView recordListView;
    private ArrayList<Object>     objectList;
    private Button                buttonInviteFriend;
    private ImageButton           imageButtonRefresh;
    private LoadDataTask          loadDataTask;
    private String                fb_id;
    private int                           loginState = 0;
    
    private int page = 1;
    //private LinearLayout pullMore;
    private FriendStreamAdapter friendStreamAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_stream);
        findViews();
        if(Utility.mFacebook != null && Utility.IsSessionValid(FriendStream.this)) {
            LoadDataTask tast = new LoadDataTask();
            if(Build.VERSION.SDK_INT < 11)
            	tast.execute();
            else
            	tast.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
            loginState = 1;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(Utility.IsSessionValid(FriendStream.this) && loginState == 0) {
            LoadDataTask tast = new LoadDataTask();
            if(Build.VERSION.SDK_INT < 11)
            	tast.execute();
            else
            	tast.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
            loginState = 1;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
        	
        	PromoteAPP promoteAPP = new PromoteAPP(FriendStream.this);
        	if(!promoteAPP.isPromote) {
	        	new AlertDialog.Builder(this).setTitle("- 離開程式? -")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					// do something when the button is clicked
					public void onClick(DialogInterface arg0, int arg1) {
						FriendStream.this.finish();
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

    private void setView() {
        if (objectList != null && Utility.IsSessionValid(FriendStream.this)) {
            recordListView.setVisibility(View.VISIBLE);
            imageButtonRefresh.setVisibility(View.GONE);
        } else {
            recordListView.setVisibility(View.GONE);
            imageButtonRefresh.setVisibility(View.VISIBLE);
        }
    }

    private void setListener() {
        buttonInviteFriend.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                FacebookIO fbIO = new FacebookIO(FriendStream.this);
                // test
                // FacebookIO.usrName = null;
                // if(FacebookIO.usrName != null)
                fbIO.requestDialog(Utility.usrName + "邀請你一起來用電影櫃 (一個收藏與分享電影的好Android APP : http://goo.gl/XWlP6)");
                /*
                 * else { Bundle params = new Bundle(); params.putString("fields", "name, picture, gender, birthday"); FacebookIO.mAsyncRunner.request("me",
                 * params, new UserRequestListener()); }
                 */
            }

        });

        recordListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {
           public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
            	new RefreshTask().execute();
            }

            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
            	new NextPageTask().execute();
            }
        });
        
        recordListView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int pos = position - 1;
                Intent newAct = new Intent();
                if (objectList.get(pos) instanceof Record) {
                    Record tmp = (Record) objectList.get(pos);
                    newAct.putExtra("record_id", tmp.getId());
                } else {
                    Comment tmp = (Comment) objectList.get(pos);
                    newAct.putExtra("record_id", tmp.getRecordId());
                }
                newAct.putExtra("Owner", false);
                newAct.setClass(FriendStream.this, MovieRecord.class);
                startActivity(newAct);
            }

        });

        /*recordListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
			public void onLastItemVisible() {
				// TODO Auto-generated method stub
				new NextPageTask().execute();
			}			
		});
        
        recordListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @SuppressWarnings("deprecation")
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                recordListView.setLastUpdatedLabel(DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL));

                // Do work to refresh the list here.
                new RefreshTask().execute();
            }
        });*/
    }

    private void setListAdatper() {
    	friendStreamAdapter = new FriendStreamAdapter(FriendStream.this, objectList, fb_id);
        recordListView.setAdapter(friendStreamAdapter);
    }

    private void findViews() {
        recordListView = (PullToRefreshListView) findViewById(R.id.listview_record);
        buttonInviteFriend = (Button) findViewById(R.id.button_invite_friend);
        //pullMore = (LinearLayout)findViewById(R.id.progressBar_pull_more);
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

    private void FetchData() {
        // friend stream api
        MovieAPI movieAPI = new MovieAPI();
        if(Utility.IsSessionValid(FriendStream.this))
        	fb_id = Utility.usrId;

        objectList = movieAPI.getFriendStatusListWithPage(fb_id, page);
    }
    
    class LoadDataTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
	          public void onCancel(DialogInterface arg0) {
	              LoadDataTask.this.cancel(true);
	              recordListView.setVisibility(View.GONE);
	              imageButtonRefresh.setVisibility(View.VISIBLE);
	          }
	      };

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(FriendStream.this);
            progressdialogInit.setTitle("Load");
            progressdialogInit.setMessage("Loading…");
            progressdialogInit.setOnCancelListener(cancelListener);
            progressdialogInit.setCanceledOnTouchOutside(false);
            progressdialogInit.show();
            recordListView.setVisibility(View.GONE);
            imageButtonRefresh.setVisibility(View.GONE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            if (Utility.IsSessionValid(FriendStream.this))
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            if (Utility.IsSessionValid(FriendStream.this))
                FetchData();
            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            setView();
            if (progressdialogInit != null && progressdialogInit.isShowing())
                progressdialogInit.dismiss();
            if (objectList != null && Utility.IsSessionValid(FriendStream.this)) {
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
            MovieAPI movieAPI = new MovieAPI();
            objectList = movieAPI.getFriendStatusListWithPage(fb_id, page);
            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            setView();
            if (objectList != null) {
            	setListAdatper();
                setListener();
            	page += 1;
            }
            recordListView.onRefreshComplete();
			
            super.onPostExecute(result);
        }
    }

    class NextPageTask  extends AsyncTask<Integer, Integer, String>{

		@Override  
        protected void onPreExecute() {
			//pullMore.setVisibility(View.VISIBLE);
			super.onPreExecute();  
        }  
        @Override
		protected String doInBackground(Integer... params) {
        	MovieAPI movieAPI = new MovieAPI();
        	ArrayList<Object> tmpList = movieAPI.getFriendStatusListWithPage(fb_id, page);
        	objectList.addAll(tmpList);
			return "progress end";
		}
		@Override  
        protected void onProgressUpdate(Integer... progress) {    
            super.onProgressUpdate(progress);  
        } 
		protected void onPostExecute(String result) {
			//pullMore.setVisibility(View.GONE);
			if(objectList != null && objectList.size() > 0){
				friendStreamAdapter.notifyDataSetChanged();
				page += 1;
	        	Log.d(null, "size : " + objectList.size());
    			// Call onRefreshComplete when the list has been refreshed.
        	}
            recordListView.onRefreshComplete();
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
        // tracker.stopSession();
    }

    @Override
    protected void onDestroy() {
        if (loadDataTask != null && loadDataTask.getStatus() != AsyncTask.Status.FINISHED)
            loadDataTask.cancel(true);
        super.onDestroy();
    }
}
