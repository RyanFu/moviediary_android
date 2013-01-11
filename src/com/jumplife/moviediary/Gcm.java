package com.jumplife.moviediary;

import static com.jumplife.moviediary.CommonUtilities.EXTRA_MESSAGE;
import static com.jumplife.moviediary.CommonUtilities.SENDER_ID;
import static com.jumplife.moviediary.CommonUtilities.SERVER_URL;
import static com.jumplife.moviediary.CommonUtilities.TAG;

import com.google.android.gcm.GCMRegistrar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class Gcm {
	AsyncTask<Void, Void, Void> mRegisterTask;
	
	public Gcm(Context c) {
		checkNotNull(SERVER_URL, "SERVER_URL");
        checkNotNull(SENDER_ID, "SENDER_ID");
        // Make sure the device has the proper dependencies.
        GCMRegistrar.checkDevice(c);
        // Make sure the manifest was properly set - comment out this line
        // while developing the app, then uncomment it when it's ready.
        GCMRegistrar.checkManifest(c);
        Log.d(TAG, "GCM Check Finish");
        /*c.registerReceiver(mHandleMessageReceiver,
                new IntentFilter(DISPLAY_MESSAGE_ACTION));*/
        final String regId = GCMRegistrar.getRegistrationId(c);
        if (regId.equals("")) {
            // Automatically registers application on startup.
            GCMRegistrar.register(c, SENDER_ID);
        } else {
            // Device is already registered on GCM, check server.
            if (GCMRegistrar.isRegisteredOnServer(c)) {
                // Skips registration.
                //mDisplay.append(getString(R.string.already_registered) + "\n");
            	ServerUtilities.regGcmId = regId;
            } else {
                // Try to register again, but not in the UI thread.
                // It's also necessary to cancel the thread onDestroy(),
                // hence the use of AsyncTask instead of a raw thread.
                final Context context = c;
                mRegisterTask = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        boolean registered =
                                ServerUtilities.register(context, regId);
                        // At this point all attempts to register with the app
                        // server failed, so we need to unregister the device
                        // from GCM - the app will try to register again when
                        // it is restarted. Note that GCM will send an
                        // unregistered callback upon completion, but
                        // GCMIntentService.onUnregistered() will ignore it.
                        if (!registered) {
                            GCMRegistrar.unregister(context);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        mRegisterTask = null;
                    }

                };
                mRegisterTask.execute(null, null, null);
                Log.d(TAG, "GCM Register Finish");
            }
        }
	}
	
	private void checkNotNull(Object reference, String name) {
        if (reference == null) {
        	throw new NullPointerException(
                    "error");
        }
    }

    private final BroadcastReceiver mHandleMessageReceiver =
            new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
            //mDisplay.append(newMessage + "\n");
        }
    };
}
