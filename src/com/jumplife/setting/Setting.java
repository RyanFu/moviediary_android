package com.jumplife.setting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.facebook.Session;
import com.google.analytics.tracking.android.TrackedActivity;
//import com.jumplife.loginactivity.FacebookIO;
import com.jumplife.loginactivity.Utility;
import com.jumplife.moviediary.MovieTabActivities;
import com.jumplife.moviediary.R;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;

public class Setting extends TrackedActivity {

    private Button             buttonFans;
    private Button             buttonLogout;
    private ToggleButton       togglebuttonNotify;
    private SharePreferenceIO  shIO;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
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
            	Session session = Session.getActiveSession();
                if (!session.isClosed()) {
                	session.closeAndClearTokenInformation();
                	Utility.usrId = null;
                    Utility.usrName = null;
                    Utility.usrBirth = null;
                    Utility.usrGender = null;
                    
                    shIO.SharePreferenceI("fbID", null);
                    shIO.SharePreferenceI("fbName", null);
                    shIO.SharePreferenceI("fbBIRTH", null);
                    shIO.SharePreferenceI("fbGENDER", null);
                    
                    shIO.SharePreferenceI("notification_key", false);
              	    Intent intent = new Intent(Setting.this, MovieTabActivities.class);
               	    setResult(MovieTabActivities.FBLOGOUT, intent);
               	    Setting.this.finish();
                } else
               	    Toast.makeText(Setting.this, "登出失敗 請重新登出", Toast.LENGTH_LONG).show();
            }
        });

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
            if (shareKey)
                togglebuttonNotify.setChecked(true);
            else
                togglebuttonNotify.setChecked(false);
        } else {
            togglebuttonNotify.setClickable(false);
            togglebuttonNotify.setChecked(false);
        }

        if (Utility.IsSessionValid(Setting.this))
            buttonLogout.setVisibility(View.VISIBLE);
        else
            buttonLogout.setVisibility(View.GONE);
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
