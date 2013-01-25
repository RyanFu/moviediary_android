package com.jumplife.setting;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.analytics.tracking.android.TrackedActivity;
import com.jumplife.loginactivity.FacebookIO;
import com.jumplife.loginactivity.Utility;
import com.jumplife.moviediary.MovieTabActivities;
import com.jumplife.moviediary.R;
import com.jumplife.moviediary.api.MovieAPI;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;

public class Setting extends TrackedActivity {

    private Button             buttonFans;
    private Button             buttonLogout;
    private ToggleButton       togglebuttonNotify;
    private String             fbId;
    private FacebookIO         fbIO;
    private SharePreferenceIO  shIO;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        fbIO = new FacebookIO(this);
        shIO = new SharePreferenceIO(this);
        initView();
    }

    private void initView() {
        TextView topbar_text;
        topbar_text = (TextView) findViewById(R.id.topbar_text);
        topbar_text.setText("設定");

        buttonFans = (Button) findViewById(R.id.button_fans);
        buttonFans.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.facebook.com/movietalked");
                Intent it = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(it);
            }
        });

        buttonLogout = (Button) findViewById(R.id.button_logout);
        buttonLogout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                FBLogoutTask task = new FBLogoutTask();
                task.execute();
            }
        });
        if (Utility.IsSessionValid(Setting.this))
            buttonLogout.setVisibility(View.VISIBLE);
        else
            buttonLogout.setVisibility(View.GONE);

        togglebuttonNotify = (ToggleButton) findViewById(R.id.togglebutton_notificationkey);
        togglebuttonNotify.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                if (arg1)
                    shIO.SharePreferenceI("notification_key", true);
                else
                    shIO.SharePreferenceI("notification_key", false);
            }
        });

        if (Utility.IsSessionValid(Setting.this)) {
            boolean shareKey = true;
            shareKey = shIO.SharePreferenceO("notification_key", shareKey);
            togglebuttonNotify.setClickable(true);
            fbId = Utility.usrId;
            if (shareKey)
                togglebuttonNotify.setChecked(true);
            else
                togglebuttonNotify.setChecked(false);
        } else {
            togglebuttonNotify.setClickable(false);
            togglebuttonNotify.setChecked(false);
        }
    }

    class FBLogoutTask extends AsyncTask<Integer, Integer, String> {
    	private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
		      public void onCancel(DialogInterface arg0) {
		    	  FBLogoutTask.this.cancel(true);
		    	  finish();
		      }
		  };

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(Setting.this);
            progressdialogInit.setTitle("Load");
            progressdialogInit.setMessage("Loading…");
            progressdialogInit.setOnCancelListener(cancelListener);
            progressdialogInit.setCanceledOnTouchOutside(false);
            progressdialogInit.show();

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
            fbIO.facebookLogout();
            while (true) {
                if (!Utility.IsSessionValid(Setting.this)) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        progressdialogInit.dismiss();
                        return "progress failed";
                    }
                    break;
                }
            }
            MovieAPI movieAPI = new MovieAPI();
            if(!movieAPI.logoutUser(fbId))
            	return "progress failed";
            
            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("progress end")) {
            	progressdialogInit.dismiss();
                shIO.SharePreferenceI("notification_key", false);

                Intent intent = new Intent(Setting.this, MovieTabActivities.class);
                setResult(MovieTabActivities.FBLOGOUT, intent);
                Setting.this.finish();
            } else
                Toast.makeText(Setting.this, "登出失敗 請重新登出", Toast.LENGTH_LONG).show();

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

    @Override
    protected void onResume() {
        super.onResume();
    }
}
