package com.need2.turnitup.beta;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class TIUdialogs {

	static EditText firstname = null;
	static EditText lastname = null;
	static EditText emailInput = null;

	protected static void startEmailPledge(final SharedPreferences sharedPref,
			final String PLEDGETAG, final Activity context,
			final Preference settingPledgePref) {
		final Boolean isRequired = true;
		final Boolean notRequired = false;

		// create pledge custom dialog
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
		dialog.setContentView(R.layout.pledge_inputdialog);
		dialog.setTitle("Noise Maker Pledge");

		// set the custom dialog components - firstname, lastname, email, Later
		// and Pledge button
		TextView text = (TextView) dialog.findViewById(R.id.pledge_message);
		text.setMovementMethod(new ScrollingMovementMethod());

		// Create firstname editText
		firstname = (EditText) dialog.findViewById(R.id.firstname_text);

		// Create lastname editText
		lastname = (EditText) dialog.findViewById(R.id.lastname_text);

		// Create email editText
		emailInput = (EditText) dialog.findViewById(R.id.email_text);
		emailInput.requestFocus();

		// create Pledge button
		Button btnPledge = (Button) dialog.findViewById(R.id.btn_pledge);
		btnPledge.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final String FIRSTNAME_TAG = "firstname";
				final String LASTNAME_TAG = "lastname";
				final String EMAIL_TAG = "email";
				boolean inputError = false;
				boolean closeDialog = false;
				String fNameStr = null;
				String lNameStr = null;
				String emailStr = null;

				// Record pledge displayed/ pledge later into the
				// SharedPreferences
				// file
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString(PLEDGETAG, "yes");
				editor.putString("pledgeLater", "no");
				editor.commit();

				// Check if the firstname is valid, then get it
				// Otherwise inputError is set
				if (validateEachInput(firstname, notRequired, FIRSTNAME_TAG)) {
					fNameStr = firstname.getEditableText().toString().trim();
				} else {
					inputError = true;
				}

				// Check if the lastname is valid, then get it
				// Otherwise inputError is set
				if (validateEachInput(lastname, notRequired, LASTNAME_TAG)) {
					lNameStr = lastname.getEditableText().toString().trim();
				} else {
					inputError = true;
				}

				// Check if the email is valid, then get it
				// Otherwise inputError is set
				if (validateEachInput(emailInput, isRequired, EMAIL_TAG)) {
					emailStr = emailInput.getEditableText().toString().trim();
				} else {
					inputError = true;
				}

				// Insert pledge inputs into User class, close the
				// dialog and enter TurnItUpActivity main screen
				if (!inputError) {
					InsertIntoClassUser(FIRSTNAME_TAG, LASTNAME_TAG, EMAIL_TAG,
							fNameStr, lNameStr, emailStr, sharedPref,
							PLEDGETAG, context);

					// Change dialog status to ready to be closed
					closeDialog = true;

				} else {
					Toast.makeText(context, "Please provide valid input",
							Toast.LENGTH_LONG).show();
				}

				// Close dialog when all inputs are submitted
				// successfully without any error
				if (closeDialog) {

					// display TurnItUpActivity after pledging if user pledge
					// from inside-app pledge dialog
					if (sharedPref.getString("fromSettingPledge", null) == "no") {
						Intent tiuIntent = new Intent(context,
								TurnItUpActivity.class);
						context.startActivity(tiuIntent);

					}

					// Close Pledge Dialog
					dialog.dismiss();

				}

			}
		});

		// create Later button
		Button btnLater = (Button) dialog.findViewById(R.id.btn_later);
		btnLater.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// Record pledge displayed into the SharedPreferences
				// file
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString(PLEDGETAG, "yes");
				editor.putString("pledgeLater", "yes");
				editor.commit();

				// display TurnItUpActivity after selecting pledge "later" if it
				// is from inside-app pledge dialog
				if (sharedPref.getString("fromSettingPledge", null) == "no") {
					// Start TIU main activity screen
					Intent tiuIntent = new Intent(context,
							TurnItUpActivity.class);
					context.startActivity(tiuIntent);
				}

				// Close Pledge Dialog
				dialog.dismiss();

			}
		});

		dialog.show();
		dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				R.drawable.tiu_logo);

	}

	// This method will validate pledge inputs from a user
	protected static boolean validateEachInput(EditText editTextBox,
			Boolean required, String tag) {
		boolean ret = true;

		// Validate email which is required and must match general email pattern
		if (tag.equals("email")) {
			if (!Validation.isEmailAddress(editTextBox, required)) {
				ret = false;
			}

			// Validate first/last name which is optional and must match general
			// name patter
		} else {
			if (!required && !Validation.hasText(editTextBox)) {
				ret = true;
			} else {
				if (!Validation.isName(editTextBox, required)) {
					ret = false;
				}
			}
		}

		return ret;

	}

	// This method will insert Pledge signup information of each user object
	// into User class on Parse
	protected static void InsertIntoClassUser(final String key1,
			final String key2, final String key3, final String dataToInsert1,
			final String dataToInsert2, final String dataToInsert3,
			final SharedPreferences sharedPref, final String PLEDGETAG,
			final Activity context) {

		final String LOGTAG = "InsertUserClass";
		final String USERID = "UID";

		// Retrieve userID again before inserting to ensure success of
		// retrieving an object
		MainActivity.userID = sharedPref.getString(USERID, null);

		ParseQuery<ParseObject> userQuery = ParseQuery.getQuery("User");

		// Retrieve the object by userID
		userQuery.getInBackground(MainActivity.userID,
				new GetCallback<ParseObject>() {

					@Override
					public void done(ParseObject user, ParseException e) {
						if (e == null) {
							// The insert was successful
							Log.d(LOGTAG, "Data insert saved!");
							user.put(key1, dataToInsert1);
							user.put(key2, dataToInsert2);
							user.put(key3, dataToInsert3);
							user.saveInBackground();

							// Record pledge displayed into the
							// SharedPreferences file
							final SharedPreferences.Editor editor = sharedPref
									.edit();
							editor.putString(PLEDGETAG, "yes");
							editor.commit();

							// Display a temporary message to notify user that
							// their
							// email has
							// been collected
							String successMsg = dataToInsert3
									+ " has signed up successfully";
							Toast.makeText(context, successMsg,
									Toast.LENGTH_LONG).show();
						} else {
							// The insert failed,
							Log.d(LOGTAG, "Data insert error: " + e);
							String successMsg = dataToInsert3
									+ " has failed to signup";

							// Alert user that the insert failed
							Toast.makeText(context, successMsg,
									Toast.LENGTH_LONG).show();

						}

					}
				});

	}

	protected static void startOfflineDeviceDialog(final Context context) {

		// create offline device dialog
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
		dialog.setContentView(R.layout.offline_device_dialog);
		dialog.setTitle("You are offline");

		TextView text = (TextView) dialog.findViewById(R.id.offline_msg);
		text.setMovementMethod(new ScrollingMovementMethod());

		Button btnOfflineOK = (Button) dialog.findViewById(R.id.okoffline_btn);
		btnOfflineOK.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				if(MainActivity.fromMainAct){
					
					// Start TIU main activity screen
					Intent tiuIntent = new Intent(context,
							TurnItUpActivity.class);
					context.startActivity(tiuIntent);
					
					MainActivity.fromMainAct = false;
				}
				
				dialog.dismiss();

			}
		});

		dialog.show();
		dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				R.drawable.tiu_logo);

	}

}
