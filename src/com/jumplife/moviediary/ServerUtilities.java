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

import com.google.android.gcm.GCMRegistrar;

import android.content.Context;

/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtilities {

	public static String regGcmId = null;
    /**
     * Register this account/device pair within the server.
     *
     * @return whether the registration succeeded or not.
     */
    static boolean register(final Context context, final String regId) {
        //Log.i(TAG, "registering device (regId = " + regId + ")");
        regGcmId = regId;
        /*String serverUrl = SERVER_URL + "/register";
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);*/
        // Once GCM returns a registration id, we need to register it in the
        // demo server. As the server might be down, we will retry it a couple
        // times.
        /*for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            Log.d(TAG, "Attempt #" + i + " to register");*/
            /*displayMessage(context, context.getString(
			        R.string.server_registering, i, MAX_ATTEMPTS));*/
			//post(serverUrl, params);
			GCMRegistrar.setRegisteredOnServer(context, true);
			//String message = context.getString(R.string.server_registered);
			//String message = "server register";
			//CommonUtilities.displayMessage(context, message);
			return true;
        //}
        /*String message = context.getString(R.string.server_register_error,
                MAX_ATTEMPTS);*/
        /*String message = "server register error";
        CommonUtilities.displayMessage(context, message);
        return false;*/
    }

    /**
     * Unregister this account/device pair within the server.
     */
    static void unregister(final Context context, final String regId) {
        //Log.i(TAG, "unregistering device (regId = " + regId + ")");
        //String serverUrl = SERVER_URL + "/unregister";
        //Map<String, String> params = new HashMap<String, String>();
        //params.put("regId", regId);
        //post(serverUrl, params);
		GCMRegistrar.setRegisteredOnServer(context, false);
		//String message = context.getString(R.string.server_unregistered);
		//String message = "server unregistered";
		//CommonUtilities.displayMessage(context, message);
    }
}
