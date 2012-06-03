package com.zhangyi.sms.assistant2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog.Calls;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.zhangyi.sms.assistant2.common.CommonUtils;
import com.zhangyi.sms.assistant2.common.SMS;

public class BestFriendsActivity extends Activity{
	
	private static final String RANK = "rank";
	private static final String NAME = "name";
	
	private Map<Integer, UserInfo> positionToUser = new HashMap<Integer, UserInfo>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.best_friends);
		
		GridView gridview = (GridView)findViewById(R.id.gridView1);
		List<Map<String, String>> gridItemList = new ArrayList<Map<String, String>>();
		TreeSet<UserInfo> userInfoSet = getUserContactInfo();
		int i = 1;
		for(UserInfo u: userInfoSet){
			Map<String, String> item = new HashMap<String, String>();
			item.put(RANK, "NO." + i);
			item.put(NAME, u.name);
			positionToUser.put(i-1, u);
			gridItemList.add(item);
			i++;
		}
		SimpleAdapter sa = new SimpleAdapter(this, 
				gridItemList, 
                R.layout.best_friends_item,         
                new String[] {RANK, NAME},   
                new int[] {R.id.rank,R.id.name}); 
		gridview.setAdapter(sa);
		gridview.setOnItemClickListener(new AdapterView.OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				UserInfo u = positionToUser.get(Integer.valueOf(position));
				StringBuilder sb = new StringBuilder();
				sb.append(getText(R.string.incoming_call_num)).append("  " + u.numOfIncomingCall).append("\n").
				   append(getText(R.string.incoming_duration)).append("  " + (int)(u.incomingCallDuration / 60)).append(getText(R.string.minute)).append(u.incomingCallDuration % 60).append(getText(R.string.second)).append("\n").
				   append(getText(R.string.outgoing_call_num)).append("  " + u.numOfoutgoingCall).append("\n").
				   append(getText(R.string.outgoing_duration)).append("  " + (int)(u.outgoingCallDuration / 60)).append(getText(R.string.minute)).append(u.outgoingCallDuration % 60).append(getText(R.string.second)).append("\n").
				   append(getText(R.string.missed_call_num)).append("  " + u.numOfmissedCall).append("\n").
				   append(getText(R.string.message_received_num)).append("  " + u.numOfRecvMessage).append("\n").
				   append(getText(R.string.message_sent_num)).append("  " + u.numOfSentMessage);
				AlertDialog.Builder b = new AlertDialog.Builder(BestFriendsActivity.this);
				b.setTitle(getText(R.string.detail_info));
				b.setMessage(sb.toString());
				b.setPositiveButton(getText(R.string.close), new Dialog.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						//do nothing
					}
					
				});
				b.show();
//				Toast t = Toast.makeText(BestFriendsActivity.this, sb.toString(), Toast.LENGTH_LONG);
//				t.setDuration(10);
//				t.show();
			}
			
		});
	}

	private class UserInfo implements Comparable<UserInfo>{
		
		private String name;
		private String number;
		private int numOfIncomingCall = 0;
		private int incomingCallDuration = 0;
		private int numOfoutgoingCall = 0;
		private int outgoingCallDuration = 0;
		private int numOfmissedCall = 0;
		private int numOfRecvMessage = 0;
		private int numOfSentMessage = 0;
		
		public double score(){
			return (incomingCallDuration /60)* 15 + (outgoingCallDuration/60) * 10 + numOfmissedCall * 2
			       + numOfRecvMessage + numOfSentMessage * 0.5;
		}
		
		@Override
		public int compareTo(UserInfo another) {
			return - (int)(this.score() - another.score());
		}
	}

	private TreeSet<UserInfo> getUserContactInfo(){
		Map<String, UserInfo> numberToInfoMap = new HashMap<String, UserInfo>();
		addCallsInfo(numberToInfoMap);
		
		addSMSMessageInfo(numberToInfoMap);
		
		//create number to contact name map
		Map<String, String> numberToContactNameMap = CommonUtils.
			getPhoneNumberToContactNameMap(this);
		
		TreeSet<UserInfo> result = new TreeSet<UserInfo>();
		for(UserInfo u : numberToInfoMap.values()){
			String name = numberToContactNameMap.get(u.number);
			if(name == null || name.length() <= 0)
				continue;
			
			u.name = name;
			result.add(u);
		}
		return result;
	}

	private void addSMSMessageInfo(Map<String, UserInfo> numberToInfoMap) {
		Cursor c = getContentResolver().query(
				Uri.parse(SMS.ALL_CONTENT_URI), 
				new String[]{SMS.TYPE, SMS.ADDRESS}, 
				null, null, null);
		try{
			
			if(c.moveToFirst()){
				do{
					String number = c.getString(c.getColumnIndex(SMS.ADDRESS));
					if(number == null)
						continue;
					
					number = CommonUtils.normalizePhoneNumber(number);
					UserInfo u = numberToInfoMap.get(number);
					if(u == null){
						u = new UserInfo();
						u.number = number;
						numberToInfoMap.put(number, u);
					}
					int type = c.getInt(c.getColumnIndex(SMS.TYPE));
					//received
					if(type == 1){
						u.numOfRecvMessage ++;
					}
					else if(type == 2){
						u.numOfSentMessage ++;
					}
				}
				while(c.moveToNext());
			}
		}finally{
			c.close();
		}
	}

	private void addCallsInfo(Map<String, UserInfo> numberToInfoMap) {
		Cursor c = getContentResolver().query(
				Calls.CONTENT_URI, 
				new String[]{Calls.NUMBER, Calls.DURATION, Calls.TYPE}, 
				null, null, null);
		try{
			if(c.moveToFirst()){
				do{
					String number = c.getString(c.getColumnIndex(Calls.NUMBER));
					number = CommonUtils.normalizePhoneNumber(number);
					UserInfo u = numberToInfoMap.get(number);
					if(u == null){
						u = new UserInfo();
						u.number = number;
						numberToInfoMap.put(number, u);
					}
					int type = c.getInt(c.getColumnIndex(Calls.TYPE));
					int duration = c.getInt(c.getColumnIndex(Calls.DURATION));
					switch(type){
						case Calls.INCOMING_TYPE:
							u.numOfIncomingCall ++;
							u.incomingCallDuration += duration;
							break;
						case Calls.MISSED_TYPE:;
							u.numOfmissedCall ++;
							break;
						case Calls.OUTGOING_TYPE:
							u.numOfoutgoingCall ++;
							u.outgoingCallDuration += duration;
							break;
					}
				}while(c.moveToNext());
			}
		}finally{
			c.close();
		}
	}
}
