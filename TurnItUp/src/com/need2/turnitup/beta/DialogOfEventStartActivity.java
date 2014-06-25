package com.need2.turnitup.beta;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;

public class DialogOfEventStartActivity extends Activity {
	private Activity thisActivity = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Fire up dialog when the MON event start
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Event starts!");

		alertDialogBuilder
				.setMessage(
						"Your event has started! \n"
								+ "Would you like to enter the event now? \n")
				.setNegativeButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						// check whether if joiner enter MON Act. too late
						if (!CountdownMoNService.eventIsOver) {
							// Display MON event screen
							Intent joinMonIntent = new Intent(
									DialogOfEventStartActivity.this,
									MinuteOfNoiseActivity.class);
							startActivity(joinMonIntent);
							
						}else{
							displayEventOverMsg();
						}

						dialog.dismiss();
						thisActivity.finish();

					}
				}).setCancelable(false);

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();

	}

	@Override
	public void onBackPressed() {
		// disable device back button for this DialogOfEventStartActivity
	}

	protected void displayEventOverMsg() {
		Toast.makeText(
				this,
				"Event was already completed \n"
						+ "Please try again in next upcoming event",
				Toast.LENGTH_LONG).show();
	}

}
