package com.need2.turnitup.beta;

import java.util.regex.Pattern;

import android.widget.EditText;

public class Validation {

	// Regular Expressions for email, name, and eventname
	private static final String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static final String NAME_REGEX = "^[a-zA-Z]+([-a-zA-Z]+)?$";
//	private static final String EVENTNAME_REGEX = "^[a-zA-Z0-9-!@#$%^&*_()\\s~`+={}|]+$";
	private static final String EVENTNAME_REGEX = ".+$";
	public static String[] swearWords = {"fuck", "damn", "bitch", "crap", "piss", "dick", "pussy", 
		"cock", "asshole", "bastard", "slut", "douche", "shit", "cunt"};

	// Error Messages
	private static final String REQUIRED_MSG = "required";
	private static final String EMAIL_MSG = "invalid email";
	private static final String NAME_MSG = "invalid name";
	private static final String EVENTNAME_MSG = "Please limit your text to 20 characters (no explicit language)";
	private static final int EVENTNAME_LENGTH = 20;

	// Call this method when you need to check email validation
	public static boolean isEmailAddress(EditText editText, boolean required) {
		return isValid(editText, EMAIL_REGEX, EMAIL_MSG, required);
	}

	// Call this method when you need to check firstname and lastname validation
	public static boolean isName(EditText editText, boolean notRequired) {
		return isValid(editText, NAME_REGEX, NAME_MSG, notRequired);
	}

	// Call this method when you need to check event name validation
	public static boolean isEventName(EditText editText, boolean required) {
		boolean isvalid = false;
		String eventStr = editText.getEditableText().toString();
		
		if(isValid(editText, EVENTNAME_REGEX, NAME_MSG, required)){
			if(eventStr.length() > EVENTNAME_LENGTH || containSwear(eventStr)){
				editText.setError(EVENTNAME_MSG);
				isvalid = false;
			}else{
				isvalid = true;
			}
		}else{
			isvalid = false;
		}
		
		return isvalid;
	}

	private static boolean containSwear(String eventName) {
//		boolean isSwear = false;
		String cleanEname = eventName.trim();
		
		for(String swearWord : swearWords){
			if(cleanEname.contains(swearWord)){
				return true;
			}
		}
		
		return false;
	}

	// Return true if the input field is valid, based on the parameter passed
	public static boolean isValid(EditText editText, String regex,
			String errMsg, boolean required) {

		String text = editText.getText().toString().trim();
		// Clearing the error, if it was previously set by some other values
		editText.setError(null);

		// Text required and editText is blank, so return false
		if (required && !hasText(editText)) {
			editText.setError(REQUIRED_MSG);
			return false;
		}

		// Pattern doesn't match so returning false
		if (!Pattern.matches(regex, text)) {
			editText.setError(errMsg);
			return false;
		}

		return true;
	}

	// Check the input field has any text or not
	// Return true if it contains text, otherwise false
	public static boolean hasText(EditText editText) {

		String text = editText.getText().toString().trim();

		// length of 0 means there's no text
		if (text.length() == 0) {
			return false;
		}

		return true;
	}

}
