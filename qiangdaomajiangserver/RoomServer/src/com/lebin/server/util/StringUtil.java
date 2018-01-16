package com.lebin.server.util;

public class StringUtil {
	public static boolean isNullOrEmpty(Object... objs)
	{
		if(objs==null||objs.length==0)
			return true;
		for(Object obj:objs)
		{
			if(obj==null||(obj instanceof String &&((String)obj).trim().isEmpty()))
				return true;
		}
		return false;
	}
	public static Long toLong(String v)
	{
		try {
			return Long.parseLong(v);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	public static Integer toInt(String v)
	{
		try {
			return Integer.parseInt(v);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	public static Integer toInt(Object v)
	{
		try {
			if(v instanceof Integer)
				return (Integer)v;
			return Integer.parseInt(v.toString());
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	public static String createRepeatString(String str,int repeatcount)
	{
		String rs="";
		for(int i=0;i<repeatcount;i++)
			rs+=str;
		return rs;
	}
	 public static String filterEmoji(String source) { 
		  if (source != null && source.length() > 0) { 
		   return source.replaceAll("[\ud800\udc00-\udbff\udfff\ud800-\udfff]", ""); 
		  } else { 
		   return source; 
		  } 
		 }
	public static void main(String[] args) {
		System.out.println(filterEmoji("\u4faf\u5d69\ud800\udfff"));
	}
}
