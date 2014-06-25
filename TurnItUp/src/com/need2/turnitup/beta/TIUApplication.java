package com.need2.turnitup.beta;

import com.parse.Parse;

import android.app.Application;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class TIUApplication extends Application {
//	public static boolean isDeviceConnected = false;

	@Override
	public void onCreate() {
		super.onCreate();
		
		Resources res = getResources();
		
		String appID = res.getString(R.string.app_id);
		String clientKey = res.getString(R.string.client_key);
		
		// Set your application id and client key for our Parse app
		Parse.initialize(this, appID, clientKey);
		
				
		registerActivityLifecycleCallbacks(new TIUAppLifecycleHandler());

	}
	
}
