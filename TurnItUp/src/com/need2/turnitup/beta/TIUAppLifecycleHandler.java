package com.need2.turnitup.beta;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;
import android.util.Log;

//File MyLifecycleHandler.java
public class TIUAppLifecycleHandler implements ActivityLifecycleCallbacks {
	// I use two separate variables here. You can, of course, just use one and
	// increment/decrement it instead of using two and incrementing both.
	static int resumed = 0;
	static int stopped = 0;
	public static boolean isAppInBG = false;
	
	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onActivityStarted(Activity activity) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onActivityResumed(Activity activity) {
		++resumed;
		isAppInBG = compareResumedStopped();
		Log.d("111", "  " + resumed);
		
	}
	
	@Override
	public void onActivityPaused(Activity activity) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onActivityStopped(Activity activity) {
		++stopped;
		isAppInBG = compareResumedStopped();
        Log.d("222", "  " + stopped);
		
	}
	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onActivityDestroyed(Activity activity) {
		// TODO Auto-generated method stub
		
	}
	
	private boolean compareResumedStopped() {
		return (resumed == stopped);
	}

	
}
