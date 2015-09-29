package com.jfinal.weixin.sdk.kit;

import java.util.Calendar;
import java.util.Date;

public class MyDateKit {
	

	public static Date getDateBefore(Date d, int day) {
		Calendar now = Calendar.getInstance();
		now.setTime(d);
		now.set(Calendar.DATE, now.get(Calendar.DATE) - day);
		return now.getTime();
	}
	
	public static void main(String[] args) {
		
	}
}
