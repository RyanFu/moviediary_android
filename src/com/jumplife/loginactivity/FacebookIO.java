package com.jumplife.loginactivity;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.android.Util;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;

public class FacebookIO {

	private static Activity mActivity;
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	
	public final String tag = "FacebookIO"; 
		
	public FacebookIO(Activity activity) {
		new SharePreferenceIO(activity);
		mActivity = activity;
	}
	
	public void photo(Bitmap bitmap, String message) {
		Log.d(tag, "photo");
		if (hasPublishPermission()) {
			Request request = Request.newUploadPhotoRequest(Session.getActiveSession(), bitmap, new Request.Callback() {
                public void onCompleted(Response response) { }
            });
            Bundle params = request.getParameters();
			params.putString("message", message);
			request.executeAsync();
        } else {
        	Session session = Session.getActiveSession();
        	if (session == null) 
                session = new Session(mActivity);        	
        	if(!session.isOpened())
        		session = Session.openActiveSessionFromCache(mActivity);
        	
        	Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(mActivity, PERMISSIONS);
            session.requestNewPublishPermissions(newPermissionsRequest);
            return;
        }
	}
	
	private boolean hasPublishPermission() {
        Session session = Session.getActiveSession();
        return session != null && session.getPermissions().contains("publish_actions");
    }
    
    public void requestDialog(String message) {
    	Log.d(tag, "Request");
    	Bundle params = new Bundle();   
        params.putString("message", message);
        WebDialog requestsDialog = (new WebDialog.RequestsDialogBuilder(
        		mActivity, Session.getActiveSession(), params))
                .setOnCompleteListener(new OnCompleteListener() {
                    public void onComplete(Bundle values,
							FacebookException error) {
                    	if(values != null && values.containsKey("request")) {
                    	final String requestId = values.getString("request");
	                        if (requestId != null) {
	                            Toast.makeText(mActivity.getApplicationContext(), 
	                                "送出",  
	                                Toast.LENGTH_LONG).show();
	                        } else {
	                            Toast.makeText(mActivity.getApplicationContext(), 
	                                "取消", 
	                                Toast.LENGTH_LONG).show();
	                        }
                    	} else {
	                        Toast.makeText(mActivity.getApplicationContext(), 
	                                "取消", 
	                                Toast.LENGTH_LONG).show();
	                    }
                    }

                }).build();
        requestsDialog.show();
	}
        
    @SuppressWarnings("deprecation")
	public int caluateAge(String birthday) {
		int age = 0;
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		try {
			age = date.getYear() - sdf.parse(birthday).getYear();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			age = 0;
		}
		//Log.d(tag, "age : " + age);
		return age;
	}
	
	@SuppressWarnings("deprecation")
	public Map<String, String> getUserName(String[] getArray) {
		//把資料加入ArrayList中
		int flag = 0;
		int nameCount = 0;
		String accountList = "";
		String[] nameArray = getArray; 
        String[] userAccount = new String[100];
        Map<String, String> map = 
                new HashMap<String, String>();

		for(int i=0; i< nameArray.length; i++){			
	 		flag = 1;
	 		for ( int j=0 ; j<nameCount; j++){
	 			if (nameArray[i].equals(userAccount[j])){
	 				flag = 0;
	 				break;
	 			}
	 		}
	 		if (flag == 1 && !nameArray[i].equals("Guest")){
	 			userAccount[nameCount] = nameArray[i];
	 			nameCount++;
	 			if (i == 0)
	 				accountList = nameArray[i];
	 			else
	 				accountList = accountList + "," + nameArray[i] ;
	 		}
		} 	
    	
		String url = 
    			"https://graph.facebook.com/fql?q=SELECT%20name%20FROM%20user%20WHERE%20uid%20IN%20("+accountList+")";
    	String result = "";
    	String[] JSONName = new String[getArray.length];
    	JSONArray JSONResult = new JSONArray();
    	
    	try{
    		HttpClient httpClient = new DefaultHttpClient();
    		HttpGet method = new HttpGet(new URI(url));
    		HttpResponse response = httpClient.execute(method);
    		if (response != null) {
    			result= EntityUtils.toString((response.getEntity()));
    			JSONObject objStream;
    			try{
    				objStream = Util.parseJson(result);
    				JSONResult = objStream.getJSONArray( "data" );
    				for(int i=0; i<nameCount; i++){
    					JSONName[i] = JSONResult.getJSONObject(i).getString("name");
    					map.put(userAccount[i], JSONName[i]);
    					//Log.d("JSONName", userAccount[i] + "JSONName" + i +" "+JSONName[i]);
    				}
    				return map; 
    			}catch(Exception e1){
    				Log.e("LocateFail", e1.toString());
    				return null; 
    			}
    		}
    	}catch(Exception e){
    		Log.e("HttpConnFail", e.toString());
    		return null;
    	}
		return null; 		
 	}
}


