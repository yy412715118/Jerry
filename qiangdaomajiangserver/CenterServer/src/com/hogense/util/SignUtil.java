package com.hogense.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.hogense.game.server.context.ServerContext;

public class SignUtil {
	private static final Logger LOG = Logger.getLogger(SignUtil.class.getName());
	public static String sign(String...args)
	{
		String str="";
		for(String arg:args)
			str+=arg;
		LOG.debug(str);
		return MD5Util.MD5(str);
	}
	public static String orderAndSign(String...args)
	{
		Arrays.sort(args);
		return sign(args);
	}
	public static boolean vervify(String sign,String...args)
	{
		if(sign==null)
			return false;
		return sign.equalsIgnoreCase(sign(args));
	}
	public static boolean orderAndVervify(String sign,String...args)
	{
		if(sign==null)
			return false;
		return sign.equalsIgnoreCase(orderAndSign(args));
	}
	public static boolean orderByKeyAndVervify(String sign,Map<String, String>args,String...notincludes)
	{
		if(sign==null)
			return false;
		return sign.equalsIgnoreCase(orderByKeyAndSign(args,notincludes));
	}
	static Comparator<Entry<String, String>>comparator1= new Comparator<Entry<String, String>>() {   
	    public int compare(Entry<String, String> o1, Entry<String, String> o2) {      
	        return (o1.getKey()).toString().compareTo(o2.getKey());
	    }
	};
	private static List<Entry<String, String>> orderByKey(Map<String, String>arg,String...notincludes)
	{
		Map<String, String>buf=new HashMap<>(arg);
		if(notincludes!=null&&notincludes.length>0)
		{
			buf=new HashMap<>(arg);
			for(String key:notincludes)
				buf.remove(key);
		}
		else
			buf=arg;
		List<Entry<String, String>>list=new ArrayList<>(buf.entrySet());
		Collections.sort(list,comparator1);
		return list;
	}
	public static String orderByKeyAndJoin(Map<String, String>arg,String...notincludes)
	{
		List<Entry<String, String>>list=orderByKey(arg,notincludes);
		String str=null;
		for(Entry<String, String>en:list)
		{
			if(str==null)
				str=en.getKey()+"="+en.getValue();
			else
				str+="&"+en.getKey()+"="+en.getValue();
		}
		return str;
	}
	public static String orderByKeyAndJoinValue(Map<String, String>arg,String...notincludes)
	{
		List<Entry<String, String>>list=orderByKey(arg,notincludes);
		String str="";
		for(Entry<String, String>en:list)
			str+=en.getValue();
		return str;
	}
	public static String orderByKeyAndSign(Map<String, String>arg,String...notincludes)
	{
		List<Entry<String, String>>list=orderByKey(arg,notincludes);
		String str=null;
		for(Entry<String, String>en:list)
		{
			if(str==null)
				str=en.getKey()+"="+en.getValue();
			else
				str+="&"+en.getKey()+"="+en.getValue();
		}
		return MD5Util.MD5(str);
	}
	public static String signLowerCase(String...args)
	{
		return sign(args).toLowerCase();
	}
	public static void main(String[] args) {
		Map<String, String>arg=new HashMap<>();
		arg.put("1x", "1");
		arg.put("3", "3");
		arg.put("2", "2");
		arg.put("4", "4");
		System.out.println(orderByKeyAndSign(arg,"2","3"));
	}
}
