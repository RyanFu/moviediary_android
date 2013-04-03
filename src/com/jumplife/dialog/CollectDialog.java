package com.jumplife.dialog;

import java.io.InputStream;

import java.util.Date;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.widget.ProfilePictureView;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.TrackedActivity;
import com.jumplife.imageload.ImageLoader;
import com.jumplife.imageprocess.ImageProcess;
/*import com.jumplife.loginactivity.BaseRequestListener;
import com.jumplife.loginactivity.FacebookIO;
import com.jumplife.loginactivity.LoginActivity;
import com.jumplife.loginactivity.SessionEvents;
import com.jumplife.loginactivity.Utility;*/
import com.jumplife.loginactivity.FacebookIO;
import com.jumplife.loginactivity.Utility;
import com.jumplife.moviediary.MovieShowActivity;
import com.jumplife.moviediary.R;
import com.jumplife.moviediary.api.MovieAPI;
import com.jumplife.moviediary.entity.Movie;
import com.jumplife.moviediary.entity.Record;
import com.jumplife.moviediary.entity.User;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;

public class CollectDialog extends TrackedActivity implements OnClickListener {

    private Movie        movie;
    private Bitmap       posterDrawable;
    private RadioGroup   radio_group;
    private CheckBox     facebook;
    private ImageView    poster;
    private ProfilePictureView user_img;
    private User 		 user;
    private EditText     comment;
    private Button       collect;
    private int          score;
    private boolean      facebook_check;
    private String       commentStr;
    private FacebookIO   fbIO;
    private ImageLoader  imageLoader;
    private LoadDataTask tast;
    private SharePreferenceIO shIO;
    
    public static String TAG = "CollectDialog";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_record);

        fbIO = new FacebookIO(this);
        imageLoader = new ImageLoader(this);
        tast = new LoadDataTask();
        shIO = new SharePreferenceIO(this);
        
        if(Build.VERSION.SDK_INT < 11)
        	tast.execute();
        else
        	tast.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);        
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FacebookIO.REAUTH_ACTIVITY_CODE) {
        	Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
        }        
    }

    @Override
    protected void onDestroy() {
        if (tast != null && tast.getStatus() != AsyncTask.Status.FINISHED)
            tast.cancel(true);
        super.onDestroy();
    }

    private void setListener() {
        collect.setOnClickListener(this);
        comment.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            	String tmp = comment.getText().toString();
            	shIO.SharePreferenceI("comment_tmp", tmp);
            }
        });
    }

    private void intentTrans() {

        Bundle extra = getIntent().getExtras();
        String posterUrl = extra.getString("posterUrl");
        posterDrawable = imageLoader.getBitmapFromURL(posterUrl);
        
        movie = new Movie();
        movie.setId(extra.getInt("id", 0));
        movie.setChineseName(extra.getString("chineseName"));
        movie.setPosterUrl(posterUrl);
        
        String usrId = Utility.usrId;
        if(usrId == null) 
			usrId = shIO.SharePreferenceO("fbID", null);
        String usrName = Utility.usrName;
		if(usrName == null)
			usrName = shIO.SharePreferenceO("fbName", null);
		String usrGender = Utility.usrGender;
		if(usrGender == null)
			usrGender = shIO.SharePreferenceO("fbGENDER", null);
		String usrBirth = Utility.usrBirth;
		if(usrBirth == null)
			usrBirth = shIO.SharePreferenceO("fbBIRTH", null);		
		user = new User(usrId, usrName, usrGender, usrBirth); 
        
    }

    private void findViews() {
        poster = (ImageView) findViewById(R.id.imageview_movie_poster);
        radio_group = (RadioGroup) findViewById(R.id.myRadioGroup);
        facebook = (CheckBox) findViewById(R.id.facebook);
        user_img = (ProfilePictureView) findViewById(R.id.imageview_userimg);
        comment = (EditText) findViewById(R.id.textview_descripe);
        collect = (Button) findViewById(R.id.button_collect);
    }

    private void setViews() {    	
    	poster.setImageBitmap(posterDrawable);        
        comment.setText(shIO.SharePreferenceO("comment_tmp", ""));
		
        if(user.getAccount() != null)
        	user_img.setProfileId(user.getAccount());
        else
        	user_img.setProfileId(null);
    }

    public void onClick(View v) {

        int score_id = radio_group.getCheckedRadioButtonId();
        score = MovieAPI.SCORE_GOOD;
        switch (score_id) {
        case R.id.myRadioButton1:
            score = MovieAPI.SCORE_GOOD;
            break;
        case R.id.myRadioButton2:
            score = MovieAPI.SCORE_NORMAL;
            break;
        case R.id.myRadioButton3:
            score = MovieAPI.SCORE_BAD;
            break;
        }
        facebook_check = facebook.isChecked();
        commentStr = comment.getText().toString();
        PostRecordTask task = new PostRecordTask();
        task.execute();
    }

    class PostRecordTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog progressdialogInit;
        private Bitmap fst, sec;

        @Override
        protected void onPreExecute() {
            progressdialogInit = ProgressDialog.show(CollectDialog.this, "上傳", "上傳中…");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
            MovieAPI movieAPI = new MovieAPI();
            Record record = new Record(-1, new Date(), score, commentStr, user, movie, 0, false);

            int recordResult = movieAPI.recordMovie(record); 
			if (recordResult == 1) {
				EasyTracker.getTracker().trackEvent("電影打卡_V2", "收藏", movie.getChineseName(), (long)0);
			
			    if (facebook_check) {
			    	EasyTracker.getTracker().trackEvent("電影打卡_V2", "分享FB", movie.getChineseName() + " and FB ID = " + user.getAccount(), (long)0);

                    Log.d(TAG, "facebook is checked");
                    int drawableId = R.drawable.fbgood;
                    if (score == MovieAPI.SCORE_GOOD) {
                        drawableId = R.drawable.fbgood;
                    } else if (score == MovieAPI.SCORE_NORMAL) {
                        drawableId = R.drawable.fbsoso;
                    } else if (score == MovieAPI.SCORE_BAD) {
                        drawableId = R.drawable.fbbad;
                    }

                    InputStream is = CollectDialog.this.getResources().openRawResource(drawableId);
                    sec = BitmapFactory.decodeStream(is);
                    sec = Bitmap.createScaledBitmap(sec, sec.getWidth(), sec.getHeight(), true);
                    fst = imageLoader.getBitmapFromURL(movie.getPosterUrl().replaceFirst("mpost2", "mpost"));
                    fst = Bitmap.createScaledBitmap(fst, 291, 418, true);
                }                    
                return "progress end";
                
            } else if (recordResult == 2)
            	return "double check";
            else
                return "progress false";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("progress end")) {
            	if (facebook_check) {
            		if(fst != null && sec != null && fbIO.photo(ImageProcess.mergeBitmap(fst, sec, 103, 84), commentStr)) {                
		                Toast toast = Toast.makeText(CollectDialog.this, "電影櫃打卡成功", Toast.LENGTH_LONG);
		                toast.setGravity(Gravity.CENTER, 0, 0);
		                toast.show();
		                Intent intent = new Intent(CollectDialog.this, MovieShowActivity.class);
		                setResult(MovieShowActivity.CHECK_SUCESS, intent);
		                CollectDialog.this.finish();
            		} else {
            			Toast toast = Toast.makeText(CollectDialog.this, "電影櫃打卡成功 Facebook分享失敗 請再分享一次", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
            		}
            	} else {
            		Toast toast = Toast.makeText(CollectDialog.this, "電影櫃打卡成功", Toast.LENGTH_LONG);
	                toast.setGravity(Gravity.CENTER, 0, 0);
	                toast.show();
	                Intent intent = new Intent(CollectDialog.this, MovieShowActivity.class);
	                setResult(MovieShowActivity.CHECK_SUCESS, intent);
	                CollectDialog.this.finish();
            	}
            } else if (result.equals("double check")) {
            	if (facebook_check) {
            		if(fst != null && sec != null && fbIO.photo(ImageProcess.mergeBitmap(fst, sec, 103, 84), commentStr)) {                
		                Toast toast = Toast.makeText(CollectDialog.this, "電影櫃打卡成功", Toast.LENGTH_LONG);
		                toast.setGravity(Gravity.CENTER, 0, 0);
		                toast.show();
		                Intent intent = new Intent(CollectDialog.this, MovieShowActivity.class);
		                setResult(MovieShowActivity.CHECK_SUCESS, intent);
		                CollectDialog.this.finish();
            		} else {
                        Toast toast = Toast.makeText(CollectDialog.this, "電影櫃打卡成功 Facebook分享失敗 請再分享一次", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
            		}
            	} else {
                    Toast toast = Toast.makeText(CollectDialog.this, "已有相同打卡  可至電影櫃修改內容", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
        		}
            } else {
            	Toast toast = Toast.makeText(CollectDialog.this, "電影櫃打卡失敗", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
            progressdialogInit.dismiss();
            super.onPostExecute(result);
        }

    }

    class LoadDataTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog progressdialogInit;

        @Override
        protected void onPreExecute() {
            progressdialogInit = ProgressDialog.show(CollectDialog.this, "Load", "Loading…");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            intentTrans();
            findViews();
            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            setViews();
            setListener();
            progressdialogInit.dismiss();
            super.onPostExecute(result);
        }

    }
    
	@Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(this);
      EasyTracker.getTracker().trackView("/movie/checkin");
    }
    
    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
