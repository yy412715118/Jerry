package com.hogense.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Map.Entry;


public class HttpRequest {
	public static String post(String url,Map<String,String>heads,byte[] data)throws SocketTimeoutException,UnknownHostException
	{
		URL murl;
		try {
			murl = new URL(url);
			URLConnection uc=murl.openConnection();
			if(heads!=null)
			{
				for(Entry<String, String>et:heads.entrySet())
					uc.setRequestProperty(et.getKey(), et.getValue());
			}
			uc.setReadTimeout(30000);
			uc.setConnectTimeout(20000);
			uc.setDoInput(true);
			uc.setDoOutput(true);
			OutputStream out= uc.getOutputStream();
			out.write(data);
			out.flush();
			InputStream in=uc.getInputStream();
			ByteArrayOutputStream bout=new ByteArrayOutputStream();
			int len;
			byte[] rd=new byte[10240];
			while((len=in.read(rd))!=-1)
				bout.write(rd,0,len);
			in.close();
			bout.close();
			return new String(bout.toByteArray(),"utf-8");
		}
		catch(UnknownHostException e)
		{
			throw e;
		}
		catch (SocketTimeoutException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
		
	}
	public static String get(String url) throws SocketTimeoutException, UnknownHostException
	{
		return get(url, null);
	}
	public static String get(String url,Map<String, String>heads)throws SocketTimeoutException,UnknownHostException
	{
		URL murl;
		try {
			murl = new URL(url);
			URLConnection uc=murl.openConnection();
			if(heads!=null)
			{
				for(Entry<String, String>et:heads.entrySet())
					uc.setRequestProperty(et.getKey(), et.getValue());
			}
			uc.setReadTimeout(30000);
			uc.setConnectTimeout(20000);
			uc.setDoInput(true);
			uc.setDoOutput(false);
			InputStream in=uc.getInputStream();
			ByteArrayOutputStream bout=new ByteArrayOutputStream();
			int len;
			byte[] rd=new byte[10240];
			while((len=in.read(rd))!=-1)
				bout.write(rd,0,len);
			in.close();
			bout.close();
			return new String(bout.toByteArray(),"utf-8");
		}
		catch(UnknownHostException e)
		{
			throw e;
		}
		catch (SocketTimeoutException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
		
	}
	public static String post(String url,String data) throws SocketTimeoutException,UnknownHostException
	{
		try {
			return post(url,null, data.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static String post(String url,Map<String, String>heads,String data) throws SocketTimeoutException,UnknownHostException
	{
		try {
			return post(url,heads, data.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
}
