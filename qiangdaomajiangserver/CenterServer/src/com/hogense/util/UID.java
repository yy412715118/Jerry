package com.hogense.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class UID {
	private static AtomicInteger uniqueId = new AtomicInteger(0);
	public static long newUID()
	{
		try {
			long now=System.currentTimeMillis();
			SimpleDateFormat format=new SimpleDateFormat("yyyyMMddHH");
			Date dtDate=new Date();
			long time=dtDate.getTime();
			dtDate=format.parse("1900121200");
			long time1=dtDate.getTime();
			long time2=(time-time1)/(1000l);
			int v=uniqueId.incrementAndGet();
		    
			System.out.println(	String.format("%d", time2));
		} catch (Exception e) {
		}
		return 0;
	}
	public static String newCode()
	{
		return MD5Util.MD5(UUID.randomUUID().toString()+System.currentTimeMillis()).toLowerCase();
	}
	public static String newUUID()
	{
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	public static void main(String[] args) {
		System.out.println(newUID());
	}
}
