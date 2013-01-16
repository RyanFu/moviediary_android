/*
 * Copyright 2004 - Present Facebook, Inc.
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.Util;
import com.jumplife.loginactivity.SessionEvents.AuthListener;
import com.jumplife.loginactivity.SessionEvents.LogoutListener;
import com.jumplife.moviediary.R;
import com.jumplife.moviediary.ServerUtilities;
import com.jumplife.moviediary.api.MovieAPI;
import com.jumplife.moviediary.entity.User;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

public class LoginActivity extends Activity {

    /*
     * Your Facebook Application ID must be set before running this example See
     * http://www.facebook.com/developers/createapp.php
     */
    private LoginButton fbLoginButton;
    private ProgressDialog progressdialogInit;
    private SharePreferenceIO sharepre;
    
    public final static int LOGIN_ACTIVITY_REQUEST_CODE = 90;
    public final static int LOGIN_ACTIVITY_RESULT_CODE_SUCCESS = 95;
    public final static int LOGIN_ACTIVITY_RESULT_CODE_FAIL = 96;
    public final static int LOGIN_ACTIVITY_REQUEST_CODE_LIKE = 100;
    
    final static int AUTHORIZE_ACTIVITY_RESULT_CODE = 0;

    public static final String APP_ID = "399748073422920";
    public final static String[] permission_normal = { "user_photos", "user_birthday" };
	public final static String[] permission_publish = { "publish_stream", "user_photos", "user_birthday" };
	
	public final String TAG = "LoginActivity"; 
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (APP_ID == null) {
            Util.showAlert(this, "Warning", "Facebook Applicaton ID must be "
                    + "specified before running this example: see FbAPIs.java");
            return;
        }

        setContentView(R.layout.activity_login);
        
        // Create the Facebook Object using the app id.
        Utility.mFacebook = new Facebook(APP_ID);
        // Instantiate the asynrunner object for asynchronous api calls.
        Utility.mAsyncRunner = new AsyncFacebookRunner(Utility.mFacebook);

        fbLoginButton = (LoginButton) findViewById(R.id.login);
        
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
        		LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.topMargin = screenHeight*2/3;
        fbLoginButton.setLayoutParams(params);
        
        // restore session if one exists
        SessionStore.restore(Utility.mFacebook, this);
        SessionEvents.addAuthListener(new FbAPIsAuthListener());
        SessionEvents.addLogoutListener(new FbAPIsLogoutListener());

        /*
         * Source Tag: login_tag
         */
        fbLoginButton.init(this, AUTHORIZE_ACTIVITY_RESULT_CODE, Utility.mFacebook, permission_publish);

        sharepre = new SharePreferenceIO(LoginActivity.this);
        		
        progressdialogInit = new ProgressDialog(this);
        progressdialogInit.setTitle("Load");
        progressdialogInit.setMessage("Loading…");
        progressdialogInit.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface arg0) {
                finish();
            }
        });        
        progressdialogInit.setCanceledOnTouchOutside(false);
        
        if (Utility.IsSessionValid(LoginActivity.this)) {
        	progressdialogInit.show();
            requestUserData();
        }
        
    }

    @Override
    public void onResume() {
        super.onResume();
        if(Utility.mFacebook != null) {
            if (!Utility.mFacebook.isSessionValid()) {
            	Utility.usrId = null;
                Utility.usrName = null;
                Utility.usrImg = null;
            } else {
                Utility.mFacebook.extendAccessTokenIfNeeded(this, null);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	progressdialogInit.show();
        switch (requestCode) {
        	case AUTHORIZE_ACTIVITY_RESULT_CODE: {
                Utility.mFacebook.authorizeCallback(requestCode, resultCode, data);
                break;
            }
        }
    }

    /*
     * Callback for fetching current user's name, picture, uid.
     */
    public class UserRequestListener extends BaseRequestListener {

        public void onComplete(final String response, final Object state) {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(response);

                Utility.usrId = jsonObject.getString("id");
                Utility.usrName = jsonObject.getString("name");
                
                JSONObject picture = jsonObject.getJSONObject("picture");
                JSONObject data = picture.getJSONObject("data");
                String Url = data.getString("url");
                Utility.usrImg = Utility.getBitmap(Url);
                if(Utility.usrImg == null)
                	Utility.usrImg = BitmapFactory.decodeResource(LoginActivity.this.getResources(), R.drawable.stub);
                
                String birthdayStr = null;
                DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                if(jsonObject.has("birthday")) {
                	birthdayStr = jsonObject.getString("birthday");
                	try {
						Utility.usrBirth = formatter.parse(birthdayStr);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }
                
                if(jsonObject.has("gender"))
                	Utility.usrGender = jsonObject.getString("gender");
                
                sharepre.SharePreferenceI("fbID", Utility.usrId);
                sharepre.SharePreferenceI("fbName", Utility.usrName);
                sharepre.SharePreferenceI("fbPICURL", Url);
                sharepre.SharePreferenceI("fbBIRTH", birthdayStr);
                sharepre.SharePreferenceI("fbGENDER", Utility.usrGender);
                
                int count = 0;
                MovieAPI movieAPI = new MovieAPI();                
                User user = new User(Utility.usrId, Utility.usrName, Utility.usrGender, Utility.usrBirth);                
                while(!movieAPI.createUser(user, Utility.mFacebook.getAccessToken(), ServerUtilities.regGcmId) && count < 200) 
                	count++;

                if(LoginActivity.this != null && !LoginActivity.this.isFinishing() 
            			&& progressdialogInit != null && progressdialogInit.isShowing())
            		progressdialogInit.dismiss();
                
                if(count < 200 && Utility.mFacebook.isSessionValid() && Utility.usrId != null ) {
                	Intent intent = LoginActivity.this.getIntent();
                    setResult(LOGIN_ACTIVITY_RESULT_CODE_SUCCESS, intent);
                	LoginActivity.this.finish();
                	Log.d(TAG, "Login Success count : " + count);
                } else {
                	LoginActivity.this.runOnUiThread(new Runnable() {
                		public void run() {
                			Toast.makeText(LoginActivity.this, "登入失敗 請重新登入", Toast.LENGTH_LONG).show();
                		}
                	});
                    Log.d(TAG, "Login Fail count : " + count);
                }
                
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    /*
     * The Callback for notifying the application when authorization succeeds or
     * fails.
     */

    public class FbAPIsAuthListener implements AuthListener {

        public void onAuthSucceed() {
            requestUserData();
        }

        public void onAuthFail(String error) {
        }
    }

    /*
     * The Callback for notifying the application when log out starts and
     * finishes.
     */
    public class FbAPIsLogoutListener implements LogoutListener {
        public void onLogoutBegin() {
        }

        public void onLogoutFinish() {
        	Utility.usrId = null;
            Utility.usrName = null;
            Utility.usrImg = null;
            Utility.usrGender = null;
            Utility.usrBirth = null;
            
            sharepre.SharePreferenceI("fbID", null);
            sharepre.SharePreferenceI("fbName", null);
            sharepre.SharePreferenceI("fbPICURL", null);
            sharepre.SharePreferenceI("fbBIRTH", null);
            sharepre.SharePreferenceI("fbGENDER", null);
        }
    }

    /*
     * Request user name, and picture to show on the main screen.
     */
    public void requestUserData() {
        Bundle params = new Bundle();
        params.putString("fields", "id, name, picture, birthday, gender");
        Utility.mAsyncRunner.request("me", params, new UserRequestListener());
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
        	Intent intent = LoginActivity.this.getIntent();
            setResult(LOGIN_ACTIVITY_RESULT_CODE_FAIL, intent);
            LoginActivity.this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
