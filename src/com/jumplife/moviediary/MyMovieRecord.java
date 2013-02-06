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
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.widget.ProfilePictureView;
import com.google.analytics.tracking.android.TrackedActivity;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
/*import com.jumplife.loginactivity.FacebookIO;
import com.jumplife.loginactivity.Utility;*/
import com.jumplife.loginactivity.FacebookIO;
import com.jumplife.loginactivity.Utility;
import com.jumplife.moviediary.api.MovieAPI;
import com.jumplife.moviediary.entity.Record;
import com.jumplife.moviediary.entity.User;
import com.jumplife.moviediary.promote.PromoteAPP;
import com.jumplife.sectionlistview.FriendListAdapter;
import com.jumplife.sectionlistview.RecordGridAdapter;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;

public class MyMovieRecord extends TrackedActivity {

    private ArrayList<Record>     recordList      = null;
    private ArrayList<User>       friendList      = null;
    private User                  user            = null;
    private String                fbId;
    private boolean               avoidSessionChecked = false;

    private TextView              topbarText;
    private ProfilePictureView    imageviewUserIcon;
    private TextView              textviewName;
    private TextView              textviewRecordCount;
    private TextView              textviewFriendCount;

    //private LinearLayout 		  pullMore;
    private PullToRefreshGridView recordGridView;
    private PullToRefreshListView listView;
    private RecordGridAdapter	  recordGridAdapter;
    private Button                buttonCheckins;
    private Button                buttonFriends;
    private ImageButton           imageButtonRefresh;

    private LoadDataTask          loadDataTask;
    
    private int 				  page = 1;
    private int                   functionFlag;
    private final int             FLAG_CHECKIN    = 1;
    private final int             FLAG_FRIENDLIST = 2;
    private Bundle                extras;

    int                           loginState      = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myprofile);
        findViews();
        functionFlag = FLAG_CHECKIN;

        if (avoidSessionChecked) {
        	loadDataTask = new LoadDataTask();
            if(Build.VERSION.SDK_INT < 11)
            	loadDataTask.execute();
            else
            	loadDataTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
        } else if (Utility.IsSessionValid(MyMovieRecord.this)) {
        	loadDataTask = new LoadDataTask();
            if(Build.VERSION.SDK_INT < 11)
            	loadDataTask.execute();
            else
            	loadDataTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
            loginState = 1;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Utility.IsSessionValid(MyMovieRecord.this) && loginState == 0 && !avoidSessionChecked) {
        	loadDataTask = new LoadDataTask();
            if(Build.VERSION.SDK_INT < 11)
            	loadDataTask.execute();
            else
            	loadDataTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
            loginState = 1;
        }

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (loadDataTask != null && loadDataTask.getStatus() != AsyncTask.Status.FINISHED)
            loadDataTask.cancel(true);
        super.onDestroy();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (extras == null) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

            	PromoteAPP promoteAPP = new PromoteAPP(MyMovieRecord.this);
            	if(!promoteAPP.isPromote) {
    	        	new AlertDialog.Builder(this).setTitle("- 離開程式? -")
    				.setPositiveButton("是", new DialogInterface.OnClickListener() {
    					// do something when the button is clicked
    					public void onClick(DialogInterface arg0, int arg1) {
    						MyMovieRecord.this.finish();
    					}
    				})
    				.setNegativeButton("否", null)
    				.show();
    		    } else
    		    	promoteAPP.promoteAPPExe();

                return true;
            }

        }

        return super.onKeyDown(keyCode, event);
    }

    private void setGridViewListener() {
        recordGridView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent newAct = new Intent();
                newAct.putExtra("record_id", recordList.get(position).getId());

                String fbIdOwner = null;
                if(Utility.IsSessionValid(MyMovieRecord.this))
                	fbIdOwner = Utility.usrId;
                if (fbId.equals(fbIdOwner))
                    newAct.putExtra("Owner", true);
                else
                    newAct.putExtra("Owner", false);
                newAct.setClass(MyMovieRecord.this, MovieRecord.class);
                startActivity(newAct);
            }
        });
        recordGridView.setOnRefreshListener(new OnRefreshListener2<GridView>() {
            @SuppressWarnings("deprecation")
			public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
            	recordGridView.setLastUpdatedLabel(DateUtils.formatDateTime(getApplicationContext(),
						System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL));
                functionFlag = FLAG_CHECKIN;
                new RefreshTask().execute();
            }

            @SuppressWarnings("deprecation")
			public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
            	recordGridView.setLastUpdatedLabel(DateUtils.formatDateTime(getApplicationContext(),
						System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL));
                functionFlag = FLAG_CHECKIN;
                new NextPageTask().execute();
            }
        });
    }

    private void setFriendListViewAdapter() {
        listView.setAdapter(new FriendListAdapter(MyMovieRecord.this, friendList));
    }

    private void setFriendListViewListner() {
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent newAct = new Intent();
                User user = friendList.get(position - 1);
                newAct.putExtra("fb_id", user.getAccount());
                newAct.putExtra("avoid_check_session", true);
                newAct.setClass(MyMovieRecord.this, MyMovieRecord.class);
                startActivity(newAct);
            }
        });
        listView.setOnRefreshListener(new OnRefreshListener<ListView>() {
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
            	// Do work to refresh the list here.
                functionFlag = FLAG_FRIENDLIST;
                new RefreshTask().execute();
                // 啟動非同步任務 拿取朋友清單
            }
        });
    }

	private void setGridViewAdapter() {
		recordGridAdapter = new RecordGridAdapter(MyMovieRecord.this, recordList);
		recordGridView.setAdapter(recordGridAdapter);
	}
	
	//設定Grid View 或是 List View
	@SuppressWarnings("deprecation")
	private void setViews() {
		LinearLayout linearlayout = (LinearLayout)findViewById(R.id.linearlayout_content);
		//先remove view
		linearlayout.removeAllViews();		
		LayoutInflater inflater = LayoutInflater.from(this);
		
		if (fbId != null)
			imageviewUserIcon.setProfileId(fbId);
		else 
			imageviewUserIcon.setProfileId(null);
		
		//如果要取得的是打卡紀錄
		if(functionFlag == FLAG_CHECKIN) {
			recordGridView = (PullToRefreshGridView)inflater.inflate(R.layout.gridview_records, null, false);
			LayoutParams llp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			recordGridView.setLayoutParams(llp);
			linearlayout.addView(recordGridView);
			
			//ImageLoader imageLoader = new ImageLoader(this);
			
			//imageLoader.DisplayImage("http://graph.facebook.com/" + fbId + "/picture?type=square", imageviewUserIcon);

			if(user.getName() != null)
				textviewName.setText(user.getName());
			else
				textviewName.setText("");
			textviewRecordCount.setText(String.valueOf(user.getRecordCount()));
			textviewFriendCount.setText(String.valueOf(user.getFriendCount()));
		}
		//如果要拿的是朋友列表
		else if (functionFlag == FLAG_FRIENDLIST){
			listView = new PullToRefreshListView(this);
			listView.setMode(Mode.DISABLED);
			View viewFooter = LayoutInflater.from(MyMovieRecord.this).inflate(R.layout.listview_mymovierecord_footer, null);
			Button buttonInviteFriend = (Button)viewFooter.findViewById(R.id.button_invite_friend);
			buttonInviteFriend.setOnClickListener(new OnClickListener() {

				public void onClick(View arg0) {
					FacebookIO fbIO = new FacebookIO(MyMovieRecord.this);
					fbIO.requestDialog(Utility.usrName + "邀請你一起來用電影櫃 (一個收藏與分享電影的好Android APP : http://goo.gl/XWlP6)");
				}
				
			});
			linearlayout.addView(listView);
		}
		
	}

	private void findViews() {
		
		LinearLayout topbarLayout;
		topbarLayout = (LinearLayout)findViewById(R.id.topBar);
		topbarText = (TextView) findViewById(R.id.topbar_text);
		topbarText.setText("電影櫃");
		
		//先check 是否有既有的fb_id
		extras = getIntent().getExtras();		
		if(extras != null) {
			topbarLayout.setVisibility(View.VISIBLE);
			avoidSessionChecked = extras.getBoolean("avoid_check_session");
		} else {
			topbarLayout.setVisibility(View.GONE);
		}
		
		//pullMore = (LinearLayout)findViewById(R.id.progressBar_pull_more);
		recordGridView = (PullToRefreshGridView)findViewById(R.id.refreshgridview_mymovierecord);
		buttonCheckins = (Button)findViewById(R.id.button_checkin);
		buttonFriends = (Button)findViewById(R.id.button_friend);
		
		imageviewUserIcon = (ProfilePictureView) findViewById(R.id.imageView_person);
		textviewName = (TextView) findViewById(R.id.textView_name);
		textviewRecordCount = (TextView) findViewById(R.id.textView_checkin_number);
		textviewFriendCount = (TextView) findViewById(R.id.textView_friend_number);
		
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
		
		buttonCheckins.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0){
				functionFlag = FLAG_CHECKIN;
				if (recordList == null) {
					loadDataTask = new LoadDataTask();
		            if(Build.VERSION.SDK_INT < 11)
		            	loadDataTask.execute();
		            else
		            	loadDataTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
				} else {
					setViews();
					setGridViewAdapter();
	            	setGridViewListener();
	            	recordGridView.setVisibility(View.VISIBLE);
	    			imageButtonRefresh.setVisibility(View.GONE);
				}				
			}
		});
		
		buttonFriends.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0){
				functionFlag = FLAG_FRIENDLIST;
				if (friendList == null) {
					Log.d(null, "friend list is null");
					loadDataTask = new LoadDataTask();
		            if(Build.VERSION.SDK_INT < 11)
		            	loadDataTask.execute();
		            else
		            	loadDataTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
				} else {
					Log.d(null, "friend list is not null");
					setViews();
					setFriendListViewAdapter();
	        		setFriendListViewListner();
	        		listView.setVisibility(View.VISIBLE);
	    			imageButtonRefresh.setVisibility(View.GONE);
				}
			}
		});
		


	}

	private void fetchData() {
		MovieAPI movieAPI = new MovieAPI();
		if(extras != null) {
			//要看的是朋友的Profile
			if(extras.getString("fb_id") != null) {
				fbId = extras.getString("fb_id");
			}
		}
		//要看的是自己的profile
		else {
			String fb_id = null;
			if(Utility.IsSessionValid(MyMovieRecord.this))
				fb_id = Utility.usrId;
			if(fb_id == null){
				try {
					Thread.sleep(1500);
					fb_id = Utility.usrId;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(fb_id == null) {
				SharePreferenceIO sharepre = new SharePreferenceIO(MyMovieRecord.this);
				fb_id = sharepre.SharePreferenceO("fbID", "Error");
				
				if(fb_id.equalsIgnoreCase("Error")) {
					user = null;
					return;
				}
			}
			
			fbId = fb_id;
		}
		
		if(user == null)
			user = movieAPI.getUserData(fbId);
		if (functionFlag == FLAG_CHECKIN) 
			recordList = movieAPI.getRecordListWithPage(fbId, page);
		else if (functionFlag == FLAG_FRIENDLIST) {
			friendList = movieAPI.getFriendList(fbId);
		}
	}
	
	class LoadDataTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
		      public void onCancel(DialogInterface arg0) {
		          LoadDataTask.this.cancel(true);
		          if (recordGridView != null)
		              recordGridView.setVisibility(View.GONE);
		          if (listView != null)
		              listView.setVisibility(View.GONE);
		          imageButtonRefresh.setVisibility(View.VISIBLE);
		      }
		  };

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(MyMovieRecord.this);
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
            if (Utility.IsSessionValid(MyMovieRecord.this) || avoidSessionChecked)
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            if (Utility.IsSessionValid(MyMovieRecord.this) || avoidSessionChecked)
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
            
            // 如果要取得的是打卡紀錄
            if (functionFlag == FLAG_CHECKIN) {
            	if ((recordList == null) || (user == null) || !Utility.IsSessionValid(MyMovieRecord.this)) {
                    recordGridView.setVisibility(View.GONE);
                    imageButtonRefresh.setVisibility(View.VISIBLE);
                } else if (avoidSessionChecked) {
                    setViews();
                    setGridViewAdapter();
                    setGridViewListener();
                    recordGridView.setVisibility(View.VISIBLE);
                    imageButtonRefresh.setVisibility(View.GONE);
                    page += 1;
                } else {
                    setViews();
                    setGridViewAdapter();
                    setGridViewListener();
                    recordGridView.setVisibility(View.VISIBLE);
                    imageButtonRefresh.setVisibility(View.GONE);
                    page += 1;
                }
            }
            // 如果要拿的是朋友列表
            else if (functionFlag == FLAG_FRIENDLIST) {
            	if (friendList == null || !Utility.IsSessionValid(MyMovieRecord.this)) {
                	if(listView != null)
                		listView.setVisibility(View.GONE);
                    imageButtonRefresh.setVisibility(View.VISIBLE);
                } else if (avoidSessionChecked) {
                    setViews();
                    setFriendListViewAdapter();
                    setFriendListViewListner();
                    listView.setVisibility(View.VISIBLE);
                    imageButtonRefresh.setVisibility(View.GONE);
                } else {
                    setViews();
                    setFriendListViewAdapter();
                    setFriendListViewListner();
                    listView.setVisibility(View.VISIBLE);
                    imageButtonRefresh.setVisibility(View.GONE);
                }

            }
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
        	ArrayList<Record> tmpList = movieAPI.getRecordListWithPage(fbId, page);
        	recordList.addAll(tmpList);
			return "progress end";
		}
		@Override  
        protected void onProgressUpdate(Integer... progress) {    
            super.onProgressUpdate(progress);  
        } 
		protected void onPostExecute(String result) {
			//pullMore.setVisibility(View.GONE);
			if(recordList != null && recordList.size() > 0){
				recordGridAdapter.notifyDataSetChanged();
				page += 1;
    			// Call onRefreshComplete when the list has been refreshed.
        	}
			recordGridView.onRefreshComplete();
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
            fetchData();
            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            // 如果要取得的是打卡紀錄
            if (functionFlag == FLAG_CHECKIN) {
            	if ((recordList == null) || (user == null) || !Utility.IsSessionValid(MyMovieRecord.this)) {
                    recordGridView.setVisibility(View.GONE);
                    imageButtonRefresh.setVisibility(View.VISIBLE);
                } else if (avoidSessionChecked) {
                    setViews();
                    setGridViewAdapter();
                    setGridViewListener();
                    recordGridView.setVisibility(View.VISIBLE);
                    imageButtonRefresh.setVisibility(View.GONE);
                    page += 1;
                } else {
                    setViews();
                    setGridViewAdapter();
                    setGridViewListener();
                    recordGridView.setVisibility(View.VISIBLE);
                    imageButtonRefresh.setVisibility(View.GONE);
                    page += 1;
                }
            	recordGridView.onRefreshComplete();
            }
            // 如果要拿的是朋友列表
            else if (functionFlag == FLAG_FRIENDLIST) {
                if (friendList == null || !Utility.IsSessionValid(MyMovieRecord.this)) {
                	if(listView != null)
                		listView.setVisibility(View.GONE);
                    imageButtonRefresh.setVisibility(View.VISIBLE);
                } else if (avoidSessionChecked) {
                    setViews();
                    setFriendListViewAdapter();
                    setFriendListViewListner();
                    listView.setVisibility(View.VISIBLE);
                    imageButtonRefresh.setVisibility(View.GONE);
                } else {
                    setViews();
                    setFriendListViewAdapter();
                    setFriendListViewListner();
                    listView.setVisibility(View.VISIBLE);
                    imageButtonRefresh.setVisibility(View.GONE);
                }
                listView.onRefreshComplete();
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
}
