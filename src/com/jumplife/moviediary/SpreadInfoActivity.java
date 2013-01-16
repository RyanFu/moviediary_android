package com.jumplife.moviediary;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.google.analytics.tracking.android.TrackedActivity;
import com.jumplife.imageload.ImageLoader;
import com.jumplife.moviediary.api.MovieAPI;
import com.jumplife.moviediary.entity.Spread;

public class SpreadInfoActivity extends TrackedActivity {

    private TextView spreadTitle;
    private TextView spreadTitleContent;
    private TextView spreadTime;
    private TextView spreadTimeContent;
    private TextView spreadMethod;
    //private TextView spreadMethodResult;
    private TextView spreadGift;
    private TextView spreadGiftContent;
    private TextView spreadNotify;
    private TextView spreadNotifyContent;
    private TextView topbarText;
    private ImageView ivSpreadPoster;  
    private ImageView ivSpreadTime;
    private ImageView ivSpreadMethod;   
    private ImageView ivSpreadMethodStep;
    private ImageView ivSpreadGift;   
    private ImageView ivSpreadGiftContent;
    private ImageView ivSpreadNotify;
    private Button buttonJoin;
    private LinearLayout llMethodContent;
    private LinearLayout llMethodResult;
    
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
        
        topbarText = (TextView) findViewById(R.id.topbar_text);
        topbarText.setText("活動內容");

        imageLoader = new ImageLoader(SpreadInfoActivity.this, 0, R.drawable.post_background);
        
        spreadTitle = (TextView) findViewById(R.id.textview_title);
        spreadTitleContent = (TextView) findViewById(R.id.textview_title_content);
        spreadTime = (TextView) findViewById(R.id.textview_time);
        spreadTimeContent = (TextView) findViewById(R.id.textview_time_content);
        spreadMethod = (TextView) findViewById(R.id.textview_method);
        //spreadMethodResult = (TextView) findViewById(R.id.textview_method_result);
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
        
        buttonJoin = (Button) findViewById(R.id.button_join);
        buttonJoin.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if(functionFlag == FLAG_CURRENT) {
					Intent newAct = new Intent();
					newAct.putExtra("movie_id", spread.getMovieId());					
					newAct.setClass(SpreadInfoActivity.this, MovieShowActivity.class);
					startActivity(newAct);
                 } else if(functionFlag == FLAG_RESULT) {
                	 Uri uri = Uri.parse("http://www.facebook.com/movietalked");
                     Intent it = new Intent(Intent.ACTION_VIEW, uri);
                     startActivity(it);
                 }
            }
        });
        
        llMethodContent = (LinearLayout) findViewById(R.id.ll_method_content);
        llMethodResult =  (LinearLayout) findViewById(R.id.ll_method_result);
    }
    
    @SuppressWarnings("deprecation")
	private void setViews() {
        spreadTitle.setText(spread.getSpreadTitle());        
        DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        imageLoader.DisplayImage(spread.getSpreadPosterUrl(), ivSpreadPoster, displayMetrics.widthPixels);
        
        if(spread.getSpreadTitleContent() != null && !spread.getSpreadTitleContent().equals("")) {
	        spreadTitleContent.setText(spread.getSpreadTitleContent());
	        spreadTitleContent.setVisibility(View.VISIBLE);
        } else
        	spreadTitleContent.setVisibility(View.GONE);
        
        if(spread.getSpreadTimeContent() != null && !spread.getSpreadTimeContent().equals("")) {
	        spreadTime.setVisibility(View.VISIBLE);
            ivSpreadTime.setVisibility(View.VISIBLE);
            String date = spread.getSpreadTimeContent().substring(0, 10);
        	spreadTimeContent.setText("即日起, 至" + date + "止");
        	spreadTimeContent.setVisibility(View.VISIBLE);
        } else {
        	spreadTime.setVisibility(View.GONE);
            ivSpreadTime.setVisibility(View.GONE);
        	spreadTimeContent.setVisibility(View.GONE);
        }
        	
        
        if((spread.getSpreadMethodStep() != null && !spread.getSpreadMethodStep().equals("")) || 
        		(spread.getSpreadMethodStepUrl() != null && !spread.getSpreadMethodStepUrl().equals("")) || 
        		(spread.getSpreadMethodResult() != null && !spread.getSpreadMethodResult().equals(""))) {
	        spreadMethod.setVisibility(View.VISIBLE);
	        ivSpreadMethod.setVisibility(View.VISIBLE);
	        
	        if(spread.getSpreadMethodResult() != null && !spread.getSpreadMethodResult().equals("")) {
		        //spreadMethodResult.setText(spread.getSpreadMethodResult());
		        //spreadMethodResult.setVisibility(View.VISIBLE);
	        	LayoutInflater myInflater = LayoutInflater.from(SpreadInfoActivity.this);
	        	llMethodResult.removeAllViews();
	        	View converView = myInflater.inflate(R.layout.item_spread_method, null);
	    		TextView textviewNo = (TextView) converView.findViewById(R.id.textview_no);
	    		TextView textviewContent = (TextView) converView.findViewById(R.id.textview_content);
	    		textviewNo.setText(spread.getSpreadMethodResult());	    		
	    		textviewContent.setVisibility(View.GONE);
	    		llMethodResult.addView(converView, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
	        	llMethodResult.setVisibility(View.VISIBLE);
	        }
	        
	        if(spread.getSpreadMethodStepUrl() != null && !spread.getSpreadMethodStepUrl().equals("")) {
	        	imageLoader.DisplayImage(spread.getSpreadMethodStepUrl(), ivSpreadMethodStep, displayMetrics.widthPixels);
		        ivSpreadMethodStep.setVisibility(View.VISIBLE);
	        }
        } else {
        	spreadMethod.setVisibility(View.GONE);
	        ivSpreadMethod.setVisibility(View.GONE);
	        //spreadMethodResult.setVisibility(View.GONE);
	        llMethodResult.setVisibility(View.GONE);
	        ivSpreadMethodStep.setVisibility(View.GONE);
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
		        imageLoader.DisplayImage(spread.getSpreadGiftUrl(), ivSpreadGiftContent, displayMetrics.widthPixels);
		        ivSpreadGiftContent.setVisibility(View.VISIBLE);
	        }
        } else {
        	spreadGift.setVisibility(View.GONE);
        	ivSpreadGift.setVisibility(View.GONE);
        	spreadGiftContent.setVisibility(View.GONE);
        	ivSpreadGiftContent.setVisibility(View.GONE);
        }
        
        if(spread.getSpreadNotifyContent() != null && !spread.getSpreadNotifyContent().equals("")) {
	        spreadNotify.setVisibility(View.VISIBLE);
	        ivSpreadNotify.setVisibility(View.VISIBLE);	        
	        spreadNotifyContent.setText(spread.getSpreadNotifyContent());
	        spreadNotifyContent.setVisibility(View.VISIBLE);
        } else {
        	spreadNotify.setVisibility(View.GONE);
        	spreadNotifyContent.setVisibility(View.GONE);
        	spreadNotifyContent.setVisibility(View.INVISIBLE);
        }
    }

    private void setListener() {               
    	
    }

    @SuppressWarnings("deprecation")
	private void setMethodContent() {
    	if(spread.getSpreadMethodStep() != null && !spread.getSpreadMethodStep().equals("")) {
    		llMethodContent.setVisibility(View.VISIBLE);
	    	String[] method = spread.getSpreadMethodStep().split("#");
	    	LayoutInflater myInflater = LayoutInflater.from(SpreadInfoActivity.this);
	    	llMethodContent.removeAllViews();
	    	
	    	if(method.length > 0) {
		    	for(int i=0; i<method.length; i++) {
		    		View converView = myInflater.inflate(R.layout.item_spread_method, null);
		    		TextView textviewNo = (TextView) converView.findViewById(R.id.textview_no);
		    		TextView textviewContent = (TextView) converView.findViewById(R.id.textview_content);		    		
		    		textviewNo.setText(i+1 + ".");
		    		textviewContent.setText(method[i]);
		    		llMethodContent.addView(converView, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		    	}
	    	} else {
	    		View converView = myInflater.inflate(R.layout.item_spread_method, null);
	    		TextView textviewNo = (TextView) converView.findViewById(R.id.textview_no);
	    		TextView textviewContent = (TextView) converView.findViewById(R.id.textview_content);
	    		textviewNo.setText(spread.getSpreadMethodResult());	    		
	    		textviewContent.setVisibility(View.GONE);
	    		llMethodContent.addView(converView, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
	    	}
    	} 
    }
    
    private void fetchData() {
    	MovieAPI movieAPI = new MovieAPI();
    	if(functionFlag == FLAG_CURRENT) {
            spread = movieAPI.getCurrentSpread(spreadId);
    	} else if(functionFlag == FLAG_RESULT) {
            spread = movieAPI.getResultSpread(spreadId);
    	}   	
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
            if (functionFlag == FLAG_CURRENT) {
            	if(spread.getSpreadPosterUrl() != null && spread.getMovieId() != 0) {
            		setViews();
                    setListener();
                    setMethodContent();
            	} else
            		showReloadDialog(SpreadInfoActivity.this);
            } else if(functionFlag == FLAG_RESULT) {
            	if(spread.getSpreadPosterUrl() != null) {
            		setViews();
                    setListener();
                    setMethodContent();
            	} else
            		showReloadDialog(SpreadInfoActivity.this);
            } else 
            	showReloadDialog(SpreadInfoActivity.this);
            
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
