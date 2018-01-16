package com.lebin.server.util;

import java.io.FileInputStream;
import java.util.Properties;

public class Config {
	private static Properties properties=new Properties();
	static
	{
		try {
			FileInputStream fin=new FileInputStream("config/serverconfig.ini");
			properties.load(fin);
			fin.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static String getProperty(String key)
	{
		return properties.getProperty(key);
	}
	public static String getProperty(String key,String df)
	{
		return properties.getProperty(key,df);
	}
	public static int getInt(String key,int df)
	{
		String p=properties.getProperty(key);
		if(p==null)
			return df;
		try {
			return Integer.parseInt(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return df;
	}
	public static long getLong(String key,long df)
	{
		String p=properties.getProperty(key);
		if(p==null)
			return df;
		try {
			return Long.parseLong(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return df;
	}
}
