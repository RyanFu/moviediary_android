package com.jumplife.loginactivity;

import java.io.ByteArrayOutputStream;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;

public class FacebookIO {

	public static Activity mActivity;
	public final String tag = "FacebookIO"; 
		
	public FacebookIO(Activity activity) {
		new SharePreferenceIO(activity);
		mActivity = activity;
	}
	
	public void facebookLogout() {
		if(Utility.mFacebook.isSessionValid()) {
            SessionEvents.onLogoutBegin();
            AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(Utility.mFacebook);
            asyncRunner.logout(mActivity, new LogoutRequestListener());
        }
	}
	
	@SuppressWarnings("deprecation")
	public void photo(Bitmap bitmap, String message) {
		Log.d(tag, "photo");
    	byte[] data = null;  
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();              
    	bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);              
        data = baos.toByteArray();                
        Bundle parameters = new Bundle ();
        parameters.putByteArray("photo", data);
        parameters.putString("caption", message);
        parameters.putString("created_time", new Date().toGMTString());
        Utility.mAsyncRunner.request("me/photos", parameters, "POST", new PhotoUploadListener(), null);
	}
    
	public void setTag(String photo_id) {
        String relativePath = photo_id + "/tags/" + "100003827048365";
        Log.d(tag, "photo_id : " + photo_id);
        Bundle params = new Bundle();
        params.putString("x", "231");
        params.putString("y", "358");
        Utility.mAsyncRunner.request(relativePath, params, "POST", new TagPhotoRequestListener(),
                null);
    }
    
	public void requestDialog(String message) {
    	Log.d(tag, "Request");
    	Bundle params = new Bundle();
    	//post on user's wall. 
        //User Message 
        params.putString("message", message); 
        //link message 
        params.putString("caption", "Jome from JumpLife");
        params.putString("redirect_uri", "http://goo.gl/XWlP6");
        //Gray Comment 
        Utility.mFacebook.dialog(mActivity, "apprequests", params, 
        		new AppRequestsListener());
	}
	
    public void post(String message, JSONArray idArray) {
    	Log.d(tag, "Post");
    	
        Bundle params = new Bundle();
    	//post on user's wall. 
        //Log.d(tag, idArray.toString());
        params.putString("to", idArray.toString());
        //params.putString("message", message); 
        //params.putString("picture", R.drawable.jumplife);
        params.putString("caption", "Jome from JumpLife"); 
        params.putString("link","http://www.google.com.tw"); 
        params.putString("description","It's a good web site for searching");
        Utility.mFacebook.dialog(mActivity, "feed", params,  new DialogListener() { 
            public void onComplete(Bundle values) {}  
            public void onFacebookError(FacebookError error) {} 
            public void onError(DialogError e) {} 
            public void onCancel() {} 
         });
	}
    
    public ArrayList<HashMap<String, Object>> batchPost(String msg,  ArrayList<HashMap<String, Object>> inviterList) {
		try {
			for( int i=0; i<inviterList.size(); i++) {
	        	HashMap<String,Object> dataSet = null;
	    		dataSet = inviterList.get(i);
	        	String response = Utility.mFacebook.request("me");
				Bundle parameters = new Bundle();
				parameters.putString("message", msg);
				parameters.putString("picture", "http://dl.dropbox.com/u/39525666/bear/jome_app_icon_dark.png");
				parameters.putString("caption", "JoMe from JumpLife"); 
				parameters.putString("link","https://play.google.com/store/apps/details?id=com.jumplife.jomemain&feature=search_result#?t=W251bGwsMSwxLDEsImNvbS5qdW1wbGlmZS5qb21lbWFpbiJd"); 
				parameters.putString("description","快點按下連結~~透過Jome APP來參加活動吧!!");
		        //response = mFacebook.request((String)dataSet.get("id") + "/feed", parameters, "POST");
				response = Utility.mFacebook.request((String)dataSet.get("id") + "/feed", parameters, "POST");
				//Log.d(tag, "got response: " + response);
				
				if (response == null || response.equals("") || response.equals("false")) {
					Log.v(tag, "Blank response");
					return inviterList;
				} else
					inviterList.remove(dataSet);
			}
		} catch(Exception e) {
		    e.printStackTrace();
		}
		return inviterList;
    }
    
    private class LogoutRequestListener extends BaseRequestListener {
        public void onComplete(String response, final Object state) {
            SessionEvents.onLogoutFinish();
            Utility.usrId = null;
            Utility.usrName = null;
            Utility.usrImg = null;
            Utility.usrGender = null;
            Utility.usrBirth = null;
            Utility.currentPermissions.clear();
            
            SharePreferenceIO sharepre = new SharePreferenceIO(mActivity);
            sharepre.SharePreferenceI("fbID", null);
            sharepre.SharePreferenceI("fbName", null);
            sharepre.SharePreferenceI("fbPICURL", null);
            sharepre.SharePreferenceI("fbBIRTH", null);
            sharepre.SharePreferenceI("fbGENDER", null);
            sharepre.SharePreferenceI("fbPERMISSIONNAME", null);
            sharepre.SharePreferenceI("fbPERMISSIONBOOL", null);
        }
    }
	
	public class AppRequestsListener extends BaseDialogListener {
        public void onComplete(Bundle values) {
            Toast toast = Toast.makeText(mActivity, "App request sent",
                    Toast.LENGTH_SHORT);
            toast.show();
        }

        public void onFacebookError(FacebookError error) {
            Toast.makeText(mActivity, "Facebook Error: " + error.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }

        public void onCancel() {
            Toast toast = Toast.makeText(mActivity, "App request cancelled",
                    Toast.LENGTH_SHORT);
            toast.show();
        }

		public void onError(DialogError e) {}
    }
    
    public void setUserImage(Bitmap img) {
		Utility.usrImg = img;
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
	
	public class PhotoUploadListener extends BaseRequestListener {

        public void onComplete(final String response, final Object state) {
        	try {
				Util.parseJson(response);
			} catch (FacebookError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}        
        }

        public void onFacebookError(FacebookError error) {
            
        }
    }
	
	public class TagPhotoRequestListener extends BaseRequestListener {

        public void onComplete(final String response, final Object state) {
            if (response.equals("true")) {
                String message = "User tagged in photo at (5, 5)" + "\n";
                message += "Api Response: " + response;
                Log.d(tag, message);
            } else {
            	Log.d(tag, "User could not be tagged.");
            }
        }

        public void onFacebookError(FacebookError error) {
        	Log.d(tag, "Facebook Error");
        }
    }
}


