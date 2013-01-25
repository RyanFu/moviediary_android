package com.jumplife.loginactivity;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.provider.MediaStore;

public class Utility extends Application {

    public static Facebook mFacebook;
    public static AsyncFacebookRunner mAsyncRunner;
    public static JSONObject mFriendsList;
	public static String usrId = null, 
						usrName = null, 
						usrGender = null;
    public static Bitmap usrImg = null;
	public static Date usrBirth = null;
    public static String objectID = null;
    public static FriendsGetProfilePics model;
    public static AndroidHttpClient httpclient = null;
    public static Hashtable<String, String> currentPermissions = new Hashtable<String, String>();
    private static Activity mActivity;
    private static int MAX_IMAGE_DIMENSION = 720;
    
    public static boolean IsSessionValid(Activity activity) {
    	mActivity = activity;
    	
    	SharePreferenceIO sharepre = new SharePreferenceIO(activity);
    	if(mFacebook == null || mAsyncRunner == null) {
    		mFacebook = new Facebook(LoginActivity.APP_ID);
    		mAsyncRunner = new AsyncFacebookRunner(mFacebook);
    		SessionStore.restore(Utility.mFacebook, activity);
    	}
    	
		if(mFacebook != null && mFacebook.isSessionValid()) {
    		
    		if(usrId == null) 
    			usrId = sharepre.SharePreferenceO("fbID", null);
    		
    		if(usrName == null)
    			usrName = sharepre.SharePreferenceO("fbName", null);
    		
    		if(currentPermissions == null || currentPermissions.isEmpty()) {
	    		currentPermissions.clear();
	    		String permissionBooltmp = sharepre.SharePreferenceO("fbPERMISSIONNAME", null);
	    		String permissionNametmp = sharepre.SharePreferenceO("fbPERMISSIONBOOL", null);
	    		if(permissionBooltmp != null && permissionNametmp != null) {
		    		String permissionBool[] = permissionBooltmp.split(",");
		            String permissionName[] = permissionNametmp.split(",");
		    		for(int i=0; i<permissionBool.length; i++)
		                currentPermissions.put(permissionName[i], permissionBool[i]);
	    		} else {
	    			Bundle bundle = new Bundle();
	            	bundle.putString("access_token", Utility.mFacebook.getAccessToken());
	            	mAsyncRunner = new AsyncFacebookRunner(mFacebook);
	                mAsyncRunner.request("me/permissions", bundle, new permissionsRequestListener());
	    		}
    		}

    		if(usrId != null && usrName != null)
    			return true;
    		else
    			return false;
    	} else
    		return false;
    }

    public static class permissionsRequestListener extends BaseRequestListener {

        public void onComplete(final String response, final Object state) {
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
                
                SharePreferenceIO sharepre= new SharePreferenceIO(mActivity);
                sharepre.SharePreferenceI("fbPERMISSIONNAME", permissionName);
                sharepre.SharePreferenceI("fbPERMISSIONBOOL", permissionBool);
            } catch (JSONException e) {
            }
        }
    }

	@SuppressLint("NewApi")
	public static Bitmap getBitmap(String url) {
    	Bitmap bitmap = null;
        try {
        	URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bitmap = BitmapFactory.decodeStream(new FlushedInputStream(is));
            bis.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
        return bitmap;
    }

    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break; // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }

    public static byte[] scaleImage(Context context, Uri photoUri) throws IOException {
        InputStream is = context.getContentResolver().openInputStream(photoUri);
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(context, photoUri);

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }

        Bitmap srcBitmap;
        is = context.getContentResolver().openInputStream(photoUri);
        if (rotatedWidth > MAX_IMAGE_DIMENSION || rotatedHeight > MAX_IMAGE_DIMENSION) {
            float widthRatio = ((float) rotatedWidth) / ((float) MAX_IMAGE_DIMENSION);
            float heightRatio = ((float) rotatedHeight) / ((float) MAX_IMAGE_DIMENSION);
            float maxRatio = Math.max(widthRatio, heightRatio);

            // Create the bitmap from file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = (int) maxRatio;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            srcBitmap = BitmapFactory.decodeStream(is);
        }
        is.close();

        /*
         * if the orientation is not 0 (or -1, which means we don't know), we
         * have to do a rotation.
         */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, true);
        }

        String type = context.getContentResolver().getType(photoUri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (type.equals("image/png")) {
            srcBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        } else if (type.equals("image/jpg") || type.equals("image/jpeg")) {
            srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        }
        byte[] bMapArray = baos.toByteArray();
        baos.close();
        return bMapArray;
    }
    
    public static int getOrientation(Context context, Uri photoUri) {
        /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }
}
