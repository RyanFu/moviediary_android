package com.jumplife.moviediary;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.analytics.tracking.android.TrackedActivity;
import com.jumplife.imageload.ImageLoader;
import com.jumplife.loginactivity.Utility;
import com.jumplife.moviediary.api.MovieAPI;
import com.jumplife.moviediary.entity.Spread;

public class SpreadInfoActivity extends TrackedActivity {

    private TextView topbar_text;
    private TextView spreadTitle;
    private TextView spreadTitleContent;
    private TextView spreadTime;
    private TextView spreadTimeContent;
    private TextView spreadMethod;
    private TextView spreadMethodResult;
    private TextView spreadGift;
    private TextView spreadGiftContent;
    private TextView spreadNotify;
    private TextView spreadNotifyContent;
    private ImageView ivSpreadPoster;  
    private ImageView ivSpreadTime;
    private ImageView ivSpreadMethod;   
    private ImageView ivSpreadMethodStep;
    private ImageView ivSpreadGift;   
    private ImageView ivSpreadGiftContent;
    private ImageView ivSpreadNotify;   
    private LinearLayout llMethodContent;
    
    private ImageLoader imageLoader;
    
    private int spreadId;
    private Spread spread;
    private LoadDataTask taskLoad;
    
    private final int FLAG_CURRENT = 1;
    private final int FLAG_RESULT = 2;
    
    private int functionFlag = 0;
    
    //private static String           TAG = "MovieInfo";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spread_detail);
        findViews();

        taskLoad = new LoadDataTask();
        if(Build.VERSION.SDK_INT < 11)
        	taskLoad.execute();
        else
        	taskLoad.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
    }

    private void findViews() {
    	Bundle extras = getIntent().getExtras();
        spreadId = extras.getInt("spread_id");
        functionFlag = extras.getInt("spread_type");
        
        imageLoader = new ImageLoader(SpreadInfoActivity.this);
        
        topbar_text = (TextView) findViewById(R.id.topbar_text);
        spreadTitle = (TextView) findViewById(R.id.textview_title);
        spreadTitleContent = (TextView) findViewById(R.id.textview_title_content);
        spreadTime = (TextView) findViewById(R.id.textview_time);
        spreadTimeContent = (TextView) findViewById(R.id.textview_time_content);
        spreadMethod = (TextView) findViewById(R.id.textview_method);
        spreadMethodResult = (TextView) findViewById(R.id.textview_method_result);
        spreadGift = (TextView) findViewById(R.id.textview_gift);
        spreadGiftContent = (TextView) findViewById(R.id.textview_gift_content);
        spreadNotify = (TextView) findViewById(R.id.textview_notify);
        spreadNotifyContent = (TextView) findViewById(R.id.textview_notify_content);
        
        ivSpreadPoster = (ImageView) findViewById(R.id.imageview_poster);
        ivSpreadTime = (ImageView) findViewById(R.id.imageview_time);
        ivSpreadMethod = (ImageView) findViewById(R.id.imageview_method);
        ivSpreadMethodStep = (ImageView) findViewById(R.id.imageview_method_step);
        ivSpreadGift = (ImageView) findViewById(R.id.imageview_gift);
        ivSpreadGiftContent = (ImageView) findViewById(R.id.imageview_gift_content);
        ivSpreadNotify = (ImageView) findViewById(R.id.imageview_notify);
        
        llMethodContent = (LinearLayout) findViewById(R.id.ll_method_content);
    }
    
    private void setViews() {
        topbar_text.setText("電影資訊");
        spreadTitle.setText("電影資訊");        
        imageLoader.DisplayImage("", ivSpreadPoster);
        
        if(spread.getSpreadTitleContent() != null && !spread.getSpreadTitleContent().equals("")) {
	        spreadTitleContent.setText(spread.getSpreadTitleContent());
	        spreadTitleContent.setVisibility(View.VISIBLE);
        }
        
        if(spread.getSpreadTimeContent() != null && !spread.getSpreadTimeContent().equals("")) {
	        spreadTime.setVisibility(View.VISIBLE);
            ivSpreadTime.setVisibility(View.VISIBLE);
        	spreadTimeContent.setText(spread.getSpreadTimeContent());
        	spreadTimeContent.setVisibility(View.VISIBLE);
        }
        
        if((spread.getSpreadMethodStep() != null && !spread.getSpreadMethodStep().equals("")) || 
        		(spread.getSpreadMethodStepUrl() != null && !spread.getSpreadMethodStepUrl().equals("")) || 
        		(spread.getSpreadMethodResult() != null && !spread.getSpreadMethodResult().equals(""))) {
	        spreadMethod.setVisibility(View.VISIBLE);
	        ivSpreadMethod.setVisibility(View.VISIBLE);
	        
	        if(spread.getSpreadMethodResult() != null && !spread.getSpreadMethodResult().equals("")) {
		        spreadMethodResult.setText(spread.getSpreadMethodResult());
		        spreadMethodResult.setVisibility(View.VISIBLE);
	        }
	        
	        if(spread.getSpreadMethodStepUrl() != null && !spread.getSpreadMethodStepUrl().equals("")) {
		        imageLoader.DisplayImage(spread.getSpreadMethodStepUrl(), ivSpreadMethodStep);
		        ivSpreadMethodStep.setVisibility(View.VISIBLE);
	        }
        }
        
        if((spread.getSpreadGiftContent() != null && !spread.getSpreadGiftContent().equals("")) || 
        		(spread.getSpreadGiftUrl() != null && !spread.getSpreadGiftUrl().equals(""))) {
	        spreadGift.setVisibility(View.VISIBLE);
	        ivSpreadGift.setVisibility(View.VISIBLE);  
	        
	        if(spread.getSpreadGiftContent() != null && !spread.getSpreadGiftContent().equals("")) {
	        	spreadGiftContent.setText(spread.getSpreadGiftContent());
	        	spreadGiftContent.setVisibility(View.VISIBLE);
	        }
	        if(spread.getSpreadGiftUrl() != null && !spread.getSpreadGiftUrl().equals("")) {
		        imageLoader.DisplayImage(spread.getSpreadGiftUrl(), ivSpreadGiftContent);
		        ivSpreadGiftContent.setVisibility(View.VISIBLE);
	        }
        }
        
        if(spread.getSpreadNotifyContent() != null && spread.getSpreadNotifyContent().equals("")) {
	        spreadNotify.setVisibility(View.VISIBLE);
	        ivSpreadNotify.setVisibility(View.VISIBLE);	        
	        spreadNotifyContent.setText(spread.getSpreadNotifyContent());
	        spreadNotifyContent.setVisibility(View.VISIBLE);
        }
    }

    private void setListener() {
    	 /*if(functionFlag == FLAG_CURRENT) {
         	newAct.putExtra("spread_id", spreadCurrentList.get(position).getId());
         	newAct.putExtra("spread_type", FLAG_CURRENT);
             newAct.setClass(SpreadActivity.this, SpreadInfoActivity.class);
             startActivity(newAct);
         } else if(functionFlag == FLAG_RESULT) {
         	newAct.putExtra("spread_id", spreadResultList.get(position).getId());
         	newAct.putExtra("spread_type", FLAG_CURRENT);
             newAct.setClass(SpreadActivity.this, SpreadInfoActivity.class);
             startActivity(newAct);
         } */               
    }


    private void setMethodContent() {
    	llMethodContent.removeAllViews();
    	for(int i=0; i<0; i++) {
    		LayoutInflater myInflater = LayoutInflater.from(SpreadInfoActivity.this);
    		View converView = myInflater.inflate(R.layout.item_spread_method, null);
    		TextView textviewNo = (TextView) converView.findViewById(R.id.textview_no);
    		TextView textviewContent = (TextView) converView.findViewById(R.id.textview_content);
    		
    		textviewNo.setText(i+1 + ".");
    		textviewContent.setText("");
    		llMethodContent.addView(converView);
    	}
    }
    
    private void fetchData() {
    	MovieAPI movieAPI = new MovieAPI();
    }

    class LoadDataTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
		public void onCancel(DialogInterface arg0) {
				LoadDataTask.this.cancel(true);
			    finish();
		    }
		};

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(SpreadInfoActivity.this);
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
            progressdialogInit.dismiss();
            if (spread == null) {
                showReloadDialog(SpreadInfoActivity.this);
            } else {
                setViews();
                setListener();
                setMethodContent();
            }
            super.onPostExecute(result);
        }

        public void closeProgressDilog() {
        	if(SpreadInfoActivity.this != null && !SpreadInfoActivity.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (taskLoad != null && taskLoad.getStatus() != AsyncTask.Status.FINISHED) {
        	taskLoad.closeProgressDilog();
        	taskLoad.cancel(true);
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
