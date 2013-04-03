package com.jumplife.moviediary;

import com.google.analytics.tracking.android.TrackedActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.widget.TextView;

public class EzCheckActivity extends TrackedActivity {
	
	private WebView webviewPic;
	
	@SuppressLint("SetJavaScriptEnabled")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_ezcheck);
		
		TextView topbar_text = (TextView)findViewById(R.id.topbar_text);
        topbar_text.setText("電影櫃 X EZ訂");
        
		webviewPic = (WebView) findViewById(R.id.webview_pic);
		
		webviewPic.loadUrl("http://www.ezding.com.tw/jumplife/mmb.do?campaign_code=moviediary_jl");		
		webviewPic.getSettings().setSupportZoom(true);
		webviewPic.getSettings().setJavaScriptEnabled(true);
		webviewPic.getSettings().setBuiltInZoomControls(true);
		webviewPic.getSettings().setDefaultZoom(ZoomDensity.FAR);
		webviewPic.setInitialScale(100);
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
	protected void onResume(){
        super.onResume();
   }

}
