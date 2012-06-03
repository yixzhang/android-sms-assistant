package com.zhangyi.sms.assistant2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.zhangyi.sms.assistant2.common.CommonUtils;
import com.zhangyi.sms.assistant2.common.Constant;
import com.zhangyi.sms.assistant2.common.SMS;

public class SMSSearchResultActivity extends ListActivity{

	private static final String MESSAGE = "message";
	private static final String PERSON = "person";

	List<Map<String,String>> searchResult = new ArrayList<Map<String,String>>();
	ListView list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		list = getListView();
		list.setAdapter(new SimpleAdapter(this, searchResult,
				android.R.layout.simple_list_item_2,
				new String[]{PERSON, MESSAGE},
				new int[] {android.R.id.text1, android.R.id.text2}
		));

		ArrayList<String> matches= ((ArrayList<String>)getIntent().getExtras().get(Constant.SMS_SEARCH_RESULT));
		if(matches != null)
			new SearchTask().execute(matches.toArray(new String[]{}));
	}

	private final class SearchTask extends AsyncTask<String, String, List<Map<String, String>>>{

		@Override
		protected List<Map<String, String>> doInBackground(String... params) {
			List<Map<String,String>> result = new ArrayList<Map<String,String>>();
			for(int i = 0; i < params.length; i++){
				searchSMS(result, params[i]);
				if(result.size() > 0)
					break;
			}
			return result;
		}

		private void searchSMS(List<Map<String,String>> result, String keyword){
			//get all SMS messages
			List<List<String>> messageList = querySMSMessage(keyword);
			
			//create number to contact name map
			Map<String, String> numberToContactNameMap = CommonUtils.
				getPhoneNumberToContactNameMap(SMSSearchResultActivity.this);
			
			for(List<String> m : messageList){
				Map<String, String> r = new HashMap<String, String>();
				r.put(MESSAGE, m.get(0));
				String name = numberToContactNameMap.get(CommonUtils.normalizePhoneNumber(m.get(2)));
				if(name == null || name.length() <= 0)
					name = CommonUtils.normalizePhoneNumber(m.get(2));
				String sms_type = "";
				if(m.get(1).equals(""+SMS.TYPE_RECEIVED)){
					sms_type = getText(R.string.received).toString();
				}
				else if(m.get(1).equals(""+SMS.TYPE_SENT)){
					sms_type = getText(R.string.sent).toString();
				}
				r.put(PERSON, sms_type + name);
				result.add(r);
			}

		}

		private List<List<String>> querySMSMessage(String keyword) {
			Cursor c = getContentResolver().query(
					Uri.parse(SMS.ALL_CONTENT_URI), 
					new String[]{SMS.BODY, SMS.TYPE, SMS.ADDRESS}, 
					SMS.BODY + " like '%" + keyword + "%'", null, 
					SMS.TYPE + "," + SMS.DATE + " DESC");

			//body, type, address
			List<List<String>> messageList = new ArrayList<List<String>>();
			try{
				if(c.moveToFirst()){
					do{
						List<String> message = new ArrayList<String>(3);
						message.add(0,c.getString(c.getColumnIndex(SMS.BODY)));
						message.add(1,c.getString(c.getColumnIndex(SMS.TYPE)));
						message.add(2,c.getString(c.getColumnIndex(SMS.ADDRESS)));
						messageList.add(message);
					}
					while(c.moveToNext());
				}
			}finally{
				c.close();
			}
			return messageList;
		}

		@Override
		protected void onPostExecute(List<Map<String, String>> result) {
			if(result.size() > 0){
				searchResult.addAll(result);
				((SimpleAdapter)list.getAdapter()).notifyDataSetChanged();
			}

			super.onPostExecute(result);
		}
	}



}
