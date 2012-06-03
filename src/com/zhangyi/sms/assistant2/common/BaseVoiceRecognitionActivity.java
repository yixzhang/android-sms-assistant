package com.zhangyi.sms.assistant2.common;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;

public abstract class BaseVoiceRecognitionActivity extends Activity{
	
	 private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

	protected void startVoiceRecognitionActivity(int resultNumber) {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		// Specify the calling package to identify your application
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());

		// Display an hint to the user about what he should say.
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");

		// Given an hint to the recognizer about what the user is going to say
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

		// Specify how many results you want to receive. The results will be sorted
		// where the first result is the one with higher confidence.
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, resultNumber);


		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	abstract protected void processVoiceRecognitionResults(int requestCode, 
			int resultCode, Intent data, ArrayList<String> matches);


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
			// Fill the list view with the strings the recognizer thought it could have heard
			processVoiceRecognitionResults(requestCode, resultCode, data, 
					data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS));
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

}
