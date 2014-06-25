package com.need2.turnitup.beta;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public abstract class BaseActivity extends Activity {
	public static boolean isDeviceConnected = false;

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		isDeviceConnected = detectDeviceActiveNetwork();
	}

	/*
	 * This method will detect whether a user's device is connected to an active
	 * network
	 */
	private  boolean detectDeviceActiveNetwork() {

		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		// check if a user's device is connected if there is active network
		// available
		if (netInfo != null) {
			return netInfo.isConnected();
		}

		return false;
	}
}
