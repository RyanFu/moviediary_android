/**
 * Copyright 2012 Facebook
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jumplife.loginactivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;
import com.jumplife.moviediary.R;
import com.jumplife.moviediary.ServerUtilities;
import com.jumplife.moviediary.api.MovieAPI;
import com.jumplife.moviediary.entity.User;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;

public class LoginActivity extends Activity {
    

    public final static int LOGIN_ACTIVITY_REQUEST_CODE = 90;
    public final static int LOGIN_ACTIVITY_RESULT_CODE_SUCCESS = 95;
    public final static int LOGIN_ACTIVITY_RESULT_CODE_FAIL = 96;
    public final static int LOGIN_ACTIVITY_REQUEST_CODE_LIKE = 100;
    public static Session session;
    private ImageButton buttonLoginLogout;
    private ProgressDialog progressdialogInit;
    private CreateUserTask task;
    
    private Session.StatusCallback statusCallback = new SessionStatusCallback();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        buttonLoginLogout = (ImageButton)findViewById(R.id.login);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
        		LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.topMargin = screenHeight*2/3;
        buttonLoginLogout.setLayoutParams(params);
        
        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

        session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
            }
            if (session == null) {
                session = new Session(this);
            }
            Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
            }
        }

        buttonLoginLogout.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
	            if (!session.isOpened() && !session.isClosed()) {
	                session.openForRead(new Session.OpenRequest(LoginActivity.this).setCallback(statusCallback));
	            } else {
	                Session.openActiveSession(LoginActivity.this, true, statusCallback);
	            }}
        });
        /*.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            public void onUserInfoFetched(GraphUser user) {
            	Session session = Session.getActiveSession();
            	if(session.isOpened()) {
	                SharePreferenceIO sharepre;
	                sharepre = new SharePreferenceIO(LoginActivity.this);
	                
	                Utility.usrId = user.getId();
	                Utility.usrName = user.getName();
	                Utility.usrBirth = user.getBirthday();
	                
	                sharepre.SharePreferenceI("fbID", Utility.usrId);
	                sharepre.SharePreferenceI("fbName", Utility.usrName);
	                sharepre.SharePreferenceI("fbBIRTH", Utility.usrBirth);
	                
	                LoginActivity.this.finish();
            	}
            }
        });*/
        
        progressdialogInit = new ProgressDialog(LoginActivity.this);
        progressdialogInit.setTitle("Load");
        progressdialogInit.setMessage("Loading…");
        progressdialogInit.setOnCancelListener(cancelListener);
        progressdialogInit.setCanceledOnTouchOutside(false);
    }
    
    private final OnCancelListener cancelListener = new OnCancelListener() {
        public void onCancel(DialogInterface arg0) {
        	if(task!= null && task.getStatus() != AsyncTask.Status.FINISHED)
        		task.cancel(true);
        	Intent intent = LoginActivity.this.getIntent();
            setResult(LOGIN_ACTIVITY_RESULT_CODE_FAIL, intent);
            LoginActivity.this.finish();
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        Session.getActiveSession().addCallback(statusCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
        Session.getActiveSession().removeCallback(statusCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
        	Intent intent = LoginActivity.this.getIntent();
            setResult(LOGIN_ACTIVITY_RESULT_CODE_FAIL, intent);
            LoginActivity.this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }
    
    private class SessionStatusCallback implements Session.StatusCallback {
        public void call(Session session, SessionState state, Exception exception) {
        	if (session != null && session.isOpened()) {
                // Get the user's data.
                progressdialogInit.show();
                makeMeRequest(session);
            }
        }
    }
    
    private void makeMeRequest(final Session session) {
        // Make an API call to get user data and define a 
        // new callback to handle the response.
        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
            public void onCompleted(GraphUser user, Response response) {
				Session session = Session.getActiveSession();
            	if(session.isOpened() && user.getId() != null && user.getName() != null ) {
	                task = new CreateUserTask(user);
	                task.execute();
            	} else {
            		if(LoginActivity.this != null && !LoginActivity.this.isFinishing() 
                			&& progressdialogInit != null && progressdialogInit.isShowing())
                		progressdialogInit.dismiss();
            	}
            		
                if (response.getError() != null) {
                    // Handle errors, will do so later.
                }
			}
        });
        request.executeAsync();
    }
    
    class CreateUserTask extends AsyncTask<Integer, Integer, String> {

        private GraphUser user;
        private int count;
        
        public CreateUserTask(GraphUser user) {
	    	this.user = user;
	    }
	    
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        	int count = 0;
            MovieAPI movieAPI = new MovieAPI();                
            User userEnity = new User(user.getId(), user.getName(), "", user.getBirthday());                
            Session session = Session.getActiveSession();        	
            while(!movieAPI.createUser(userEnity, session.getAccessToken(), ServerUtilities.regGcmId) && count < 200) 
            	count++;
            
            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            
        	if(LoginActivity.this != null && !LoginActivity.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();

        	SharePreferenceIO sharepre = new SharePreferenceIO(LoginActivity.this);
        	if(count < 200) {
                
                Utility.usrId = user.getId();
                Utility.usrName = user.getName();
                Utility.usrBirth = user.getBirthday();
                
                sharepre.SharePreferenceI("fbID", Utility.usrId);
                sharepre.SharePreferenceI("fbName", Utility.usrName);
                sharepre.SharePreferenceI("fbBIRTH", Utility.usrBirth);
                
                Intent intent = LoginActivity.this.getIntent();
                setResult(LOGIN_ACTIVITY_RESULT_CODE_SUCCESS, intent);
                Log.d("Login", "Create Success");
            	LoginActivity.this.finish();
            } else {
            	
            	Utility.usrId = null;
                Utility.usrName = null;
                Utility.usrBirth = null;
                
                sharepre.SharePreferenceI("fbID", null);
                sharepre.SharePreferenceI("fbName", null);
                sharepre.SharePreferenceI("fbBIRTH", null);
                
            	LoginActivity.this.runOnUiThread(new Runnable() {
            		public void run() {
            			Toast.makeText(LoginActivity.this, "登入失敗 請重新登入", Toast.LENGTH_LONG).show();
            		}
            	});
            }
        }

    }
}
