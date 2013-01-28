/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jumplife.moviediary;

import static com.jumplife.moviediary.CommonUtilities.SENDER_ID;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.jumplife.moviediary.R;
import com.jumplife.moviediary.api.MovieAPI;
import com.jumplife.moviediary.entity.Movie;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;
import com.jumplife.sqlite.SQLiteMovieDiary;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

    private static final String TAG = "GCMIntentService";

    public GCMIntentService() {
        super(SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
        //displayMessage(context, getString(R.string.gcm_registered));
        ServerUtilities.register(context, registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        //displayMessage(context, getString(R.string.gcm_unregistered));
        if (GCMRegistrar.isRegisteredOnServer(context)) {
            ServerUtilities.unregister(context, registrationId);
        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            Log.i(TAG, "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
        int type, record_id;
        
        String typeTmp = intent.getStringExtra("type");
        if(typeTmp != null)
        	type = Integer.valueOf(typeTmp);
        else
        	type = 0;
        
        String record_idTmp = intent.getStringExtra("record_id");
        if(record_idTmp != null )
        	record_id = Integer.valueOf(record_idTmp);
        else 
        	record_id = 0;
        
        String userId = intent.getStringExtra("user_id");
        String userName = intent.getStringExtra("user_name");
        String movieName = intent.getStringExtra("movie_name");
        // notifies user
        String message;
        SharePreferenceIO shIO = new SharePreferenceIO(this);
        boolean shareKey = true;
        String shareUsrId = null;
        shareKey = shIO.SharePreferenceO("notification_key", shareKey);
        shareUsrId = shIO.SharePreferenceO("fbID", shareUsrId);

        if(shareKey && shareUsrId != null) {
        	/* type
        	 * 1 : 朋友打卡通知	導引至朋友打卡
        	 * 2 : 留言通知		導引至record頁面
        	 */
        	if(type == 1) {
	        	message = userName + "給 " + movieName + " 一則評價";
	        	generateCheckNotification(context, message);
        	} else if(type == 2  && userId != null && record_id != 0) {
	        	message = userName + "回應了在 " + movieName + " 的留言";
	        	generateCommentNotification(context, message, record_id);
        	}
        }
        
        if(type == 4) {
        	Log.d(TAG, "Refresh Search Data Base");
        	LoadDataTask tast = new LoadDataTask();
            tast.execute();
        }
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        //String message = getString(R.string.gcm_deleted, total);
        String message = "Gcm delete";
        //displayMessage(context, message);
        // notifies user
        generateCheckNotification(context, message);
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
        /*displayMessage(context, getString(R.string.gcm_error, errorId));*/
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        /*displayMessage(context, getString(R.string.gcm_recoverable_error,
                errorId));*/
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    @SuppressWarnings("deprecation")
	private static void generateCheckNotification(Context context, String message) {
        int icon = R.drawable.movie_64;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        String title = context.getString(R.string.app_name);
        Intent notificationIntent = new Intent(context, MovieTabActivities.class);
        notificationIntent.putExtra("tabNo", 2);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }

    @SuppressWarnings("deprecation")
	private static void generateCommentNotification(Context context, String message, int recordId) {
        int icon = R.drawable.movie_64;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        String title = context.getString(R.string.app_name);
        Intent notificationIntent = new Intent(context, MovieRecord.class);
        notificationIntent.putExtra("record_id", recordId);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }
    
    class LoadDataTask extends AsyncTask<Integer, Integer, String>{  
        
		ArrayList<Movie> movieList = new ArrayList<Movie>();
		
		@Override  
        protected void onPreExecute() {
        	super.onPreExecute();  
        }  
          
        @Override  
        protected String doInBackground(Integer... params) {
        	MovieAPI movieAPI = new MovieAPI();
    		movieList = movieAPI.getMovieList(MovieAPI.FILTER_ALL);
    		
            return "progress end";  
        }  
  
        @Override  
        protected void onProgressUpdate(Integer... progress) {    
            super.onProgressUpdate(progress);  
        }  
  
        @Override  
        protected void onPostExecute(String result) {
        	
        	if(result.equals("progress end")) {
        		SQLiteMovieDiary.deleteMovie_lst();
        		SQLiteMovieDiary.createMovie();
        		SQLiteMovieDiary.addMovieList(movieList);
        	}
        	
        	super.onPostExecute(result);
        }  
          
    }
}
