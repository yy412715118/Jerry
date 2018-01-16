package com.hogense.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;

public class DateUtil {
	public static SimpleDateFormat fulldateformat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static SimpleDateFormat ymddateformat=new SimpleDateFormat("yyyy-MM-dd");
	public static String formatYMD(Date date)
	{
		return ymddateformat.format(date);
	}
	public static String formatYMDHMS(Date date)
	{
		return fulldateformat.format(date);
	}
	
	public static void main(String[] args) {
		
		BitSet set=new BitSet(1000000);
		for(int i=0;i<1000000;i++)
			set.set(i,true);
		for(int j=0;j<10000;j++)
		{
			for(int i=0;i<1000000;i++)
			{
				
			}
		}
		for(int j=0;j<10000;j++)
		{
			for(int i=0;i<1000000;i++)
			{
				
			}
		}
		System.out.println(set.get(999999));
		while(true);
		//	System.out.println(st);
		//System.out.println(999999*4/(1024*1024));
	}
}
