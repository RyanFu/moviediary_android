package com.jumplife.dialog;

import java.io.InputStream;

import java.util.Date;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

import com.facebook.android.DialogError;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.TrackedActivity;
import com.jumplife.imageload.ImageLoader;
import com.jumplife.imageprocess.ImageProcess;
import com.jumplife.loginactivity.BaseRequestListener;
import com.jumplife.loginactivity.FacebookIO;
import com.jumplife.loginactivity.LoginActivity;
import com.jumplife.loginactivity.SessionEvents;
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
    private Bitmap       userDrawable;
    private RadioGroup   radio_group;
    private CheckBox     facebook;
    private ImageView    poster;
    private ImageView    user_img;
    private EditText     comment;
    private Button       collect;
    private int          score;
    private boolean      facebook_check;
    private String       commentStr;
    private FacebookIO   fbIO;
    private ImageLoader  imageLoader;
    private LoadDataTask tast;
    private SharePreferenceIO shIO;
    private Handler mHandler;
    
    public static String TAG = "CollectDialog";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_record);

        mHandler = new Handler();
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

    private void setViews() {
        poster.setImageBitmap(posterDrawable);
        user_img.setImageBitmap(userDrawable);
        comment.setText(shIO.SharePreferenceO("comment_tmp", ""));
    }

    private void findViews() {
        poster = (ImageView) findViewById(R.id.imageview_movie_poster);
        radio_group = (RadioGroup) findViewById(R.id.myRadioGroup);
        facebook = (CheckBox) findViewById(R.id.facebook);
        user_img = (ImageView) findViewById(R.id.imageview_userimg);
        comment = (EditText) findViewById(R.id.textview_descripe);
        collect = (Button) findViewById(R.id.button_collect);
    }

    private void intentTrans() {

        Bundle extra = getIntent().getExtras();
        String posterUrl = extra.getString("posterUrl");
        //posterUrl = posterUrl.replaceFirst("mpost2", "mpost");
        posterDrawable = imageLoader.getBitmapFromURL(posterUrl);
        if (getIntent().hasExtra("userImage")) {
            userDrawable = BitmapFactory.decodeByteArray(getIntent().getByteArrayExtra("userImage"), 0, getIntent().getByteArrayExtra("userImage").length);
        }

        movie = new Movie();
        movie.setId(extra.getInt("id", 0));
        movie.setChineseName(extra.getString("chineseName"));
        movie.setPosterUrl(posterUrl);
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

        @Override
        protected void onPreExecute() {
            progressdialogInit = ProgressDialog.show(CollectDialog.this, "上傳", "上傳中…");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
            MovieAPI movieAPI = new MovieAPI();
            User user = new User();

            //String fb_id = FacebookIO.usrId;
            String fb_id = Utility.usrId;
            if (fb_id == null) {
                SharePreferenceIO sharepre = new SharePreferenceIO(CollectDialog.this);
                fb_id = sharepre.SharePreferenceO("fbID", fb_id);
            }

            user.setAccount(fb_id);
            Record record = new Record(-1, new Date(), score, commentStr, user, movie, 0, false);
            if ((!facebook_check && Utility.IsSessionValid(CollectDialog.this)) || 
            	( facebook_check && Utility.IsSessionValid(CollectDialog.this) &&
            	  Utility.currentPermissions.containsKey("publish_actions") && 
            	  Utility.currentPermissions.get("publish_actions").equals("1"))
            	) {
            	int recordResult = movieAPI.recordMovie(record); 
	            if (recordResult == 1) {
	            	EasyTracker.getTracker().trackEvent("電影打卡", "收藏", movie.getChineseName(), (long)0);
	
	                if (facebook_check) {
	                	EasyTracker.getTracker().trackEvent("電影打卡", "分享FB", movie.getChineseName() + " and FB ID = " + fb_id, (long)0);
	
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
	                    Bitmap sec = BitmapFactory.decodeStream(is);
	                    sec = Bitmap.createScaledBitmap(sec, sec.getWidth(), sec.getHeight(), true);
	                    Bitmap fst = imageLoader.getBitmapFromURL(movie.getPosterUrl().replaceFirst("mpost2", "mpost"));
	                    fst = Bitmap.createScaledBitmap(fst, 291, 418, true);
	
	                    fbIO.photo(ImageProcess.mergeBitmap(fst, sec, 103, 84), commentStr);
	                }                    
                    return "progress end";
                    
	            } else if (recordResult == 2)
	            	return "double check";
	            else
	                return "progress false";
            } else {
            	Bundle bundle = new Bundle();
            	bundle.putString("access_token", Utility.mFacebook.getAccessToken());
                Utility.mAsyncRunner.request("me/permissions", bundle,
                        new permissionsRequestListener());
            	return "facebook error";
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("progress end")) {
                Toast toast = Toast.makeText(CollectDialog.this, "電影櫃打卡成功", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Intent intent = new Intent(CollectDialog.this, MovieShowActivity.class);
                setResult(MovieShowActivity.CHECK_SUCESS, intent);
                CollectDialog.this.finish();
            } else if (result.equals("double check")) {
                Toast toast = Toast.makeText(CollectDialog.this, "已有相同打卡  可至電影櫃修改內容", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            } else if (result.equals("facebook error")){
                Toast toast = Toast.makeText(CollectDialog.this, "電影櫃打卡失敗 需要Facebook張貼權限", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
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
    
    /*
     * Callback for the permission OAuth Dialog
     */
    public class permissionsRequestListener extends BaseRequestListener {

        public void onComplete(final String response, final Object state) {
        	/*
             * Clear the current permission list and repopulate with new
             * permissions. This is used to mark assigned permission green and
             * unclickable.
             */
            Utility.currentPermissions.clear();
            
            try {
                JSONObject jsonObject = new JSONObject(response).getJSONArray("data")
                        .getJSONObject(0);
                Iterator<?> iterator = jsonObject.keys();
                
                int permissionInt;
                String permissionStr;
                String permissionBool = null;
                String permissionName = null;
                
                while (iterator.hasNext()) {
                	permissionStr = (String) iterator.next();
                	permissionInt = jsonObject.getInt(permissionStr);
                	permissionName = permissionStr + ",";
                	permissionBool = permissionInt + ",";
                	Utility.currentPermissions.put(permissionStr, String.valueOf(permissionInt));
                }
            	permissionName = permissionName.substring(0, permissionName.length()-1);
                permissionBool = permissionBool.substring(0, permissionBool.length()-1);
                SharePreferenceIO sharepre= new SharePreferenceIO(CollectDialog.this);
                sharepre.SharePreferenceI("fbPERMISSIONNAME", permissionName);
                sharepre.SharePreferenceI("fbPERMISSIONBOOL", permissionBool);
            } catch (JSONException e) {
            }
            mHandler.post(new Runnable() {
                public void run() {
                    Utility.mFacebook.authorize(CollectDialog.this, LoginActivity.permission_publish, new LoginDialogListener());
                }
            });
        }

        public void onFacebookError(FacebookError error) {
        }

    }

    /*
     * Callback when user has authorized the app with the new permissions
     */
    private final class LoginDialogListener implements DialogListener {
        public void onComplete(Bundle values) {
            // Inform the parent loginlistener so it can update the user's
            // profile pic and name on the home screen.
            SessionEvents.onLoginSuccess();
        }

        public void onFacebookError(FacebookError error) {
        }

        public void onError(DialogError error) {
        }

        public void onCancel() {
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
