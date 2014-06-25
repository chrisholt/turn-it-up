package com.need2.turnitup.beta;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class SettingsFragment extends PreferenceFragment {

	EditText firstname = null;
	EditText lastname = null;
	EditText emailInput = null;
	final Boolean isRequired = true;
	final Boolean notRequired = false;
	private String userID = null;
	Activity settingActivity = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		settingActivity = getActivity();
		final SharedPreferences sharedPref = settingActivity
				.getSharedPreferences("UserInfoFile", Context.MODE_PRIVATE);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.pref_general);

		final Preference settingPledgePref = (Preference) findPreference("setting_pledge");

		settingPledgePref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						// display offline dialog to notify to users that their
						// device is not connected to the network
						if (!BaseActivity.isDeviceConnected) {
							TIUdialogs
									.startOfflineDeviceDialog(settingActivity);
						} else {

							if (sharedPref.getString("pledgeLater", null) != "no") {

								// Record pledge-from-Settings flag into the
								// SharedPreferences
								// file to handle the cases properly after
								// closing down the pledge dialog
								SharedPreferences.Editor editor = sharedPref
										.edit();
								editor.putString("fromSettingPledge", "yes");
								editor.commit();

								TIUdialogs.startEmailPledge(sharedPref,
										"pledgeDisplayed", settingActivity,
										settingPledgePref);

							} else {

								Toast.makeText(
										settingActivity,
										"You have already signed up for Noise Maker Pledge.",
										Toast.LENGTH_LONG).show();
							}

						}

						return false;
					}
				});

	}

}
