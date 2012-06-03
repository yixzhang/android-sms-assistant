package com.zhangyi.sms.assistant2;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.zhangyi.sms.assistant2.common.BaseVoiceRecognitionActivity;
import com.zhangyi.sms.assistant2.common.Constant;

public class SMSSearchActivity extends BaseVoiceRecognitionActivity{

	private static final int VOICE_RECOGNITION_RESULT_NUM = 3;

	EditText text;
	ImageButton voice;
	Button reset;
	Button search;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sms_search);

		text = (EditText)findViewById(R.id.v_query);
		voice = (ImageButton)findViewById(R.id.bt_voice);
		voice.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				startVoiceRecognitionActivity(VOICE_RECOGNITION_RESULT_NUM);
			}

		});
		reset = (Button)findViewById(R.id.bt_reset);
		reset.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				text.setText("");
			}
		});
		search = (Button)findViewById(R.id.bt_search);	
		search.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(text.getText() != null && text.getText().toString() != null &&
						text.getText().toString().length() > 0){
					ArrayList<String> matches = new ArrayList<String>();
					matches.add(text.getText().toString());
					startSMSSearchResultActivity(matches);
				}
				else{
					Toast.makeText(SMSSearchActivity.this, getText(R.string.kw_empty), 
							Toast.LENGTH_LONG);
				}
			}
		});
	}

	@Override
	protected void processVoiceRecognitionResults(int requestCode,
			int resultCode, Intent data, ArrayList<String> matches) {
		startSMSSearchResultActivity(matches);
	}

	private void startSMSSearchResultActivity(ArrayList<String> matches){
		if(matches.size() > 0){
			Intent i = new Intent(this, SMSSearchResultActivity.class);
			i.putExtra(Constant.SMS_SEARCH_RESULT, matches);
			startActivity(i);
		}
	}
}
