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

import java.util.Arrays;
import java.util.List;

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
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
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
    private LoginButton buttonLoginLogout;
    private ProgressDialog progressdialogInit;
    private CreateUserTask task;
	private static final List<String> READPERMISSIONS = Arrays.asList("user_birthday");
	private UiLifecycleHelper uiHelper;

    //private Session.StatusCallback statusCallback = new SessionStatusCallback();

    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        buttonLoginLogout = (LoginButton)findViewById(R.id.login);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
        		LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.topMargin = screenHeight*2/3;
        buttonLoginLogout.setLayoutParams(params);
        
        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);
        
        buttonLoginLogout.setReadPermissions(READPERMISSIONS);
        buttonLoginLogout.setBackgroundDrawable(getResources().getDrawable(R.drawable.login_button));
        buttonLoginLogout.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            public void onUserInfoFetched(GraphUser user) {
            	Log.d("","Enter User Info Change CallBack.");
            	session = Session.getActiveSession();
            	if(session != null && session.isOpened() && user != null) {
                	task = new CreateUserTask(user);
                	task.execute();
                }
            }
        });
        
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
    public void onResume() {
    	super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
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
    
    class CreateUserTask extends AsyncTask<Integer, Integer, String> {

        private GraphUser user;
        private User userEnity;
        private int count;
        
        public CreateUserTask(GraphUser user) {
	    	this.user = user;
	    }
	    
        @Override
        protected void onPreExecute() {
        	if(LoginActivity.this != null && !LoginActivity.this.isFinishing() 
        			&& progressdialogInit != null && !progressdialogInit.isShowing())
        		progressdialogInit.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        	int count = 0;
            MovieAPI movieAPI = new MovieAPI();
            if(user.getProperty("gender") != null)
            	userEnity = new User(user.getId(), user.getName(), user.getProperty("gender").toString(), user.getBirthday());
            else
            	userEnity = new User(user.getId(), user.getName(), "", user.getBirthday());
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
                
                Utility.usrId = userEnity.getAccount();
                Utility.usrName = userEnity.getName();
                Utility.usrBirth = userEnity.getBirthday();
                Utility.usrGender = userEnity.getSex();
                
                sharepre.SharePreferenceI("fbID", Utility.usrId);
                sharepre.SharePreferenceI("fbName", Utility.usrName);
                sharepre.SharePreferenceI("fbBIRTH", Utility.usrBirth);
                sharepre.SharePreferenceI("fbGENDER", Utility.usrGender);
                
                Intent intent = LoginActivity.this.getIntent();
                setResult(LOGIN_ACTIVITY_RESULT_CODE_SUCCESS, intent);
                Log.d("Login", "Create Success");
            	LoginActivity.this.finish();
            } else {
            	
            	Utility.usrId = null;
                Utility.usrName = null;
                Utility.usrBirth = null;
                Utility.usrGender = null;
                
                sharepre.SharePreferenceI("fbID", null);
                sharepre.SharePreferenceI("fbName", null);
                sharepre.SharePreferenceI("fbBIRTH", null);
                sharepre.SharePreferenceI("fbGENDER", null);
                
            	LoginActivity.this.runOnUiThread(new Runnable() {
            		public void run() {
            			Toast.makeText(LoginActivity.this, "登入失敗 請重新登入", Toast.LENGTH_LONG).show();
            		}
            	});
            }
        }

    }
}
