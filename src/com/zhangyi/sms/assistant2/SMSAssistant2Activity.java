package com.zhangyi.sms.assistant2;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SMSAssistant2Activity extends ListActivity {

	List<String> funcNameList = new ArrayList<String>();
	List<Class> funcClassList = new ArrayList<Class>();
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addFunctions(R.string.sms_search, SMSSearchActivity.class);
		addFunctions(R.string.best_friends, BestFriendsActivity.class);
		addFunctions(R.string.spam_message_cleaner, SpamSMSActivity.class);
		//add more function here

		ListView listView = getListView();
		listView.setAdapter(new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, 
				funcNameList));
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent i = new Intent(SMSAssistant2Activity.this, funcClassList.get(position));
				startActivity(i);
			}
			
		});
	}
	
	private void addFunctions(int funcNameId, Class activityClass){
		funcNameList.add(getText(funcNameId).toString());
		funcClassList.add(activityClass);
	}
}