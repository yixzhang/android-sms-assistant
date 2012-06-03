package com.zhangyi.sms.assistant2.common;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;

public class CommonUtils {
	
	public static String normalizePhoneNumber(String phoneNumber){
		if(phoneNumber.startsWith("+86"))
			return phoneNumber.substring(3);

		if(phoneNumber.startsWith("86"))
			return phoneNumber.substring(2);

		return phoneNumber;
	}
	
	public static Map<String, String> getPhoneNumberToContactNameMap(Context context) {
		Map<String, String> contactIdToNameMap = getContactIdToNameMappings(context);
		Map<String, String> numberTocontactIdMap = getPhoneNumberToContactIdMappings(context);
			
		Map<String, String> numberToContactNameMap = new HashMap<String, String>();
		for(String number : numberTocontactIdMap.keySet()){
			String contactId = numberTocontactIdMap.get(number);
			if(contactId == null || contactId.length() <= 0)
				continue;
			
			String name = contactIdToNameMap.get(contactId);
			numberToContactNameMap.put(normalizePhoneNumber(number), name);
		}
		return numberToContactNameMap;
	}

	private static Map<String, String> getPhoneNumberToContactIdMappings(Context context) {
		Cursor c;
		c = context.getContentResolver().query(
				Phone.CONTENT_URI, 
				new String[]{Phone.CONTACT_ID, Phone.DATA1, Phone.DATA2}, 
				null, null, null);
		Map<String, String> numberTocontactIdMap = new HashMap<String, String>();
		try{
			if(c.moveToFirst()){
				do{
					String contactId = c.getString(c.getColumnIndex(Phone.CONTACT_ID));
					numberTocontactIdMap.put(
							c.getString(c.getColumnIndex(Phone.DATA1)),
							contactId);

					String number2 = c.getString(c.getColumnIndex(Phone.DATA2));
					if(number2 != null && number2.length() > 0){
						numberTocontactIdMap.put(
								number2,
								contactId);
					}
				}
				while(c.moveToNext());
			}
		}finally{
			c.close();
		}
		return numberTocontactIdMap;
	}

	private static Map<String, String> getContactIdToNameMappings(Context context) {
		Cursor c;
		c = context.getContentResolver().query(
				Contacts.CONTENT_URI, 
				new String[]{Contacts._ID, Contacts.DISPLAY_NAME}, 
				Contacts.HAS_PHONE_NUMBER + "=1", null, 
				null);
		Map<String, String> contactIdToNameMap = new HashMap<String, String>();
		try{
			if(c.moveToFirst()){
				do{
					contactIdToNameMap.put(
							c.getString(c.getColumnIndex(Contacts._ID)),
							c.getString(c.getColumnIndex(Contacts.DISPLAY_NAME)));
				}
				while(c.moveToNext());
			}
		}finally{
			c.close();
		}
		return contactIdToNameMap;
	}

}
