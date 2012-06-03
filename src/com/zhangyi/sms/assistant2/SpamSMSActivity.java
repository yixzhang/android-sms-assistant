package com.zhangyi.sms.assistant2;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.zhangyi.sms.assistant2.common.CommonUtils;
import com.zhangyi.sms.assistant2.common.SMS;

public class SpamSMSActivity extends Activity{
	
	private static final String NUMBER = "number";
	private static final String MESSAGE = "message";
	private static final String ID = "id";
	private static final String TO_BE_DELETED = "to be deleted";
	
	private static final int MAX_NUMBER_LENGTH = 11;
	private static final int MIN_NUMBER_LENGTH = 11;
	
	Button delete;
	Button delete_all;
	ListView listView;
	List<Map<String, String>> spamMsgList = new LinkedList<Map<String, String>>();
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spam_msg_cleaner);
		
		delete = (Button)findViewById(R.id.bt_delete);
		delete.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				AlertDialog.Builder b = new AlertDialog.Builder(SpamSMSActivity.this);
				b.setTitle(getText(R.string.confirm));
				b.setMessage(getText(R.string.delete_confirm));
				b.setPositiveButton(getText(R.string.yes), new Dialog.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						deleteMessage(spamMsgList);
					}
					
				});
				b.setNegativeButton(getText(R.string.no), new Dialog.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						//do nothing
					}
					
				});
				b.show();
			}
			
		});
		
		delete_all = (Button)findViewById(R.id.bt_delete_all);
		delete_all.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				AlertDialog.Builder b = new AlertDialog.Builder(SpamSMSActivity.this);
				b.setTitle(getText(R.string.confirm));
				b.setMessage(getText(R.string.delete_confirm));
				b.setPositiveButton(getText(R.string.yes), new Dialog.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						deleteAllMessages(spamMsgList);
					}
					
				});
				b.setNegativeButton(getText(R.string.no), new Dialog.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						//do nothing
					}
					
				});
				b.show();
			}
			
		});
		listView = (ListView)findViewById(R.id.spam_msg_list);
		listView.setAdapter(new SpamSimpleAdapter(this, spamMsgList,
				android.R.layout.simple_list_item_2,
				new String[]{NUMBER, MESSAGE},
				new int[] {android.R.id.text1, android.R.id.text2}));
		
		List<Map<String, String>> spamMsgResult = getAllSpamMessages();
		if(spamMsgResult.size() > 0){
			spamMsgList.addAll(spamMsgResult);
		}
		((SimpleAdapter)listView.getAdapter()).notifyDataSetChanged();	
		
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Map<String, String> message = spamMsgList.get(position);
				boolean isClicked = message.get(TO_BE_DELETED) == null? false: true;
				if(isClicked){
					message.remove(TO_BE_DELETED);
					//set background to normal color
					view.setBackgroundColor(android.R.color.background_light);
					
				}else{
					message.put(TO_BE_DELETED, "true");
					//set background to special color;
					view.setBackgroundColor(R.color.blue);
				}
			}
		});
	}
	
	private void deleteMessage(List<Map<String, String>> list){
		List<Map<String, String>> deletedList = new LinkedList<Map<String, String>>();
		for(int i = 0; i < list.size(); i++){
			Map<String,String> m = list.get(i);
			boolean delete = m.get(TO_BE_DELETED) == null ? false : true;
			String id = m.get(ID);
			
			if(delete && id != null && id.length() > 0){
				getContentResolver().delete(Uri.parse(SMS.ALL_CONTENT_URI), 
						SMS.ID + "="+id, null);
				deletedList.add(m);
			}	
		}
		for(Map<String,String> d:deletedList){
			list.remove(d);
		}
		((SimpleAdapter)listView.getAdapter()).notifyDataSetChanged();
	}
	
	private void deleteAllMessages(List<Map<String, String>> list){
		getContentResolver().delete(Uri.parse(SMS.ALL_CONTENT_URI), 
				null, null);
		list.clear();
		((SimpleAdapter)listView.getAdapter()).notifyDataSetChanged();
	}

	private List<Map<String, String>> getAllSpamMessages(){
		Cursor c = getContentResolver().query(
				Uri.parse(SMS.ALL_CONTENT_URI), 
				new String[]{SMS.ID,SMS.BODY, SMS.ADDRESS, SMS.PERSON}, 
				SMS.TYPE + "=" + SMS.TYPE_RECEIVED, null, SMS.ADDRESS + " DESC");

		//body, type, address
		List<Map<String,String>> messageList = new LinkedList<Map<String,String>>();
		try{
			if(c.moveToFirst()){
				do{
					Map<String, String> message = new HashMap<String, String>();
					String address = c.getString(c.getColumnIndex(SMS.ADDRESS));
					String body = c.getString(c.getColumnIndex(SMS.BODY));
					String person = c.getString(c.getColumnIndex(SMS.PERSON));
					if(isSpamMsg(address, body, person)){
						message.put(NUMBER, address);
						message.put(MESSAGE, body);
						message.put(ID, c.getString(c.getColumnIndex(SMS.ID)));
						messageList.add(message);
					}
				}
				while(c.moveToNext());
			}
		}finally{
			c.close();
		}
		return messageList;
	}
	
	private boolean isSpamMsg(String address, String message, String person){
		String number = CommonUtils.normalizePhoneNumber(address);
		if(number.length() > MAX_NUMBER_LENGTH  || number.length() < MIN_NUMBER_LENGTH){
			return true;
		}
		if(person == null || person.length() <= 0){
			return true;
		}
		//Add more logic here
		
		return false;
	}
	
	private class SpamSimpleAdapter extends SimpleAdapter{
		
		List<Map<String, String>> list;

		public SpamSimpleAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to) {
			super(context, data, resource, from, to);
			list = (List<Map<String, String>>)data;
		}
		
	    public View getView(int position, View convertView, ViewGroup parent) {
	        View view = super.getView(position, convertView, parent);
	        Map<String,String> message = list.get(position);
	        boolean isClicked = message.get(TO_BE_DELETED) == null? false: true;
			if(!isClicked){
				view.setBackgroundColor(android.R.color.background_light);
				
			}else{
				view.setBackgroundColor(R.color.blue);
			}
			return view;
	    }
	}
}
