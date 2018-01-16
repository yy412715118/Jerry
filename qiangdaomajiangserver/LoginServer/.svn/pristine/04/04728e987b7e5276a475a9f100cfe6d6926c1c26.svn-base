package com.hogense.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.Random;

import atg.taglib.json.util.JSONArray;
import atg.taglib.json.util.JSONObject;

public class GuestInfo {
	public String nickname;
	public String head;
	public int sex;
	static JSONObject config;
	static
	{
		FileInputStream fin=null;
		try {
			File file=new File("config/guestinfo.json");
			fin=new FileInputStream(file);
			byte[]data=new byte[(int)file.length()];
			int len=fin.read(data);
			config=new JSONObject(new String(data,0,len,"utf-8"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(fin!=null)
		{
			try {
				fin.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	static Random random=new Random();
	public static GuestInfo newGuestInfo()
	{
		GuestInfo info=new GuestInfo();
		try {
			int sex=random.nextInt(2)+1;
			String host=config.getString("host");
			JSONArray array=config.getJSONObject("heads").getJSONArray(sex+"");
			String url=array.getString(random.nextInt(array.length()));
			array=config.getJSONArray("nicknames");
			String nickname=array.getString(random.nextInt(array.length()));
			info.sex=sex;
			info.head=url.startsWith("http[s]*:")?url:(host+url);
			info.nickname=nickname;
		} catch (Exception e) {
			e.printStackTrace();
			info.sex=1;
			info.head="";
			info.nickname="游客"+(random.nextInt(10000)+1);
		}
		return info;
	}
}
