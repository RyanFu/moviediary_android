package com.jumplife.moviediary;

import com.google.analytics.tracking.android.EasyTracker;
import com.jumplife.moviediary.promote.PromoteAPP;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class AboutUsActivity extends Activity {
	
	private LinearLayout llFeedback;
	private LinearLayout llFacebook;
	private LinearLayout llNews;
	private LinearLayout llMovietime;
	private LinearLayout llTvDrama;
	private LinearLayout llTvVariety;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_aboutme);
		initView();
		setClickListener();
	}
	
	@Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(this);
    }
    
    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(this);
    }

	private void initView(){
		llFeedback = (LinearLayout)findViewById(R.id.ll_feedback);
		llFacebook = (LinearLayout)findViewById(R.id.ll_facebook);
		llNews = (LinearLayout)findViewById(R.id.ll_declare);
		llMovietime = (LinearLayout)findViewById(R.id.ll_movietime);
		llTvDrama = (LinearLayout)findViewById(R.id.ll_tvdrama);
		llTvVariety = (LinearLayout)findViewById(R.id.ll_tvvariety);
	}
	
	private void setClickListener(){
		llFeedback.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				EasyTracker.getTracker().trackEvent("關於我們", "建議回饋", "", (long)0);
				Uri uri = Uri.parse("mailto:jumplives@gmail.com");  
				String[] ccs={"abooyaya@gmail.com, chunyuko85@gmail.com, raywu07@gmail.com, supermfb@gmail.com, form.follow.fish@gmail.com"};
				Intent it = new Intent(Intent.ACTION_SENDTO, uri);
				it.putExtra(Intent.EXTRA_CC, ccs); 
				it.putExtra(Intent.EXTRA_SUBJECT, "[電影櫃] 建議回饋"); 
				startActivity(it);  
			}			
		});
		llFacebook.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				EasyTracker.getTracker().trackEvent("關於我們", "FB粉絲團", "", (long)0);
				Uri uri = Uri.parse("http://www.facebook.com/movietalked");
                Intent it = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(it);
			}			
		});
		llNews.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				Intent newAct = new Intent();
				newAct.setClass( AboutUsActivity.this, NewsActivity.class );
                startActivity( newAct );
			}			
		});
		llMovietime.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				PackageManager pm = AboutUsActivity.this.getPackageManager();
			    Intent appStartIntent = pm.getLaunchIntentForPackage("com.jumplife.movieinfo");
			    if(null != appStartIntent) {
			    	appStartIntent.addCategory("android.intent.category.LAUNCHER");
			    	appStartIntent.setComponent(new ComponentName("com.jumplife.movieinfo",
				    		"com.jumplife.movieinfo.MovieTime"));
			    	appStartIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    	AboutUsActivity.this.startActivity(appStartIntent);
			    } else
			    	startActivity(new Intent(Intent.ACTION_VIEW, 
				    		Uri.parse("market://details?id=com.jumplife.movieinfo")));
			}			
		});
		llTvDrama.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				EasyTracker.getTracker().trackEvent("關於我們", "電視連續劇", "", (long)0);
				PackageManager pm = AboutUsActivity.this.getPackageManager();
			    Intent appStartIntent = pm.getLaunchIntentForPackage("com.jumplife.tvdrama");
			    if(null != appStartIntent) {
			    	appStartIntent.addCategory("android.intent.category.LAUNCHER");
			    	appStartIntent.setComponent(new ComponentName("com.jumplife.tvdrama",
				    		"com.jumplife.tvdrama.TvDrama"));
			    	appStartIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    	AboutUsActivity.this.startActivity(appStartIntent);
			    } else
			    	startActivity(new Intent(Intent.ACTION_VIEW, 
				    		Uri.parse("market://details?id=com.jumplife.tvdrama")));
			}			
		});
		llTvVariety.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				EasyTracker.getTracker().trackEvent("關於我們", "電視連續劇", "", (long)0);
				PackageManager pm = AboutUsActivity.this.getPackageManager();
			    Intent appStartIntent = pm.getLaunchIntentForPackage("com.jumplife.tvvariety");
			    if(null != appStartIntent) {
			    	appStartIntent.addCategory("android.intent.category.LAUNCHER");
			    	appStartIntent.setComponent(new ComponentName("com.jumplife.tvvariety",
				    		"com.jumplife.tvvariety.TvVariety"));
			    	appStartIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    	AboutUsActivity.this.startActivity(appStartIntent);
			    } else
			    	startActivity(new Intent(Intent.ACTION_VIEW, 
				    		Uri.parse("market://details?id=com.jumplife.tvvariety")));
			}			
		});
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

        	PromoteAPP promoteAPP = new PromoteAPP(AboutUsActivity.this);
        	if(!promoteAPP.isPromote) {
	        	new AlertDialog.Builder(this).setTitle("- 離開程式? -")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					// do something when the button is clicked
					public void onClick(DialogInterface arg0, int arg1) {
						AboutUsActivity.this.finish();
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
}
