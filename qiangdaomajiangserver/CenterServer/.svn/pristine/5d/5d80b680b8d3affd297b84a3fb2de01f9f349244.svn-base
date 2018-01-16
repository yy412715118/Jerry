package com.hogense.game.server.sevice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hogense.server.define.WhiteList;

public class ServerFilter {
	private File file=new File("config/serverfiler.json");
	private static ServerFilter filter=new ServerFilter();
	private WhiteList whiteList;
	private ServerFilter() {
		load();
	}
	private void load()
	{
		if(file.exists())
		{
			FileInputStream fin=null;
			try {
				fin=new FileInputStream(file);
				byte[] data=new byte[fin.available()];
				int len=fin.read(data);
				fin.close();
				fin=null;
				if(len>2)
				{
					String str=new String(data, 0, len,"utf-8");
					Gson gson=new Gson();
					whiteList=gson.fromJson(str, WhiteList.class);
				}
				
			} catch (Exception e) {
				// TODO: handle exception
			}
			finally {
				if(fin!=null)
					try {
						fin.close();
					} catch (Exception e2) {
						// TODO: handle exception
					}
			}
			
		}
		if(whiteList==null)
		{
			whiteList=new WhiteList();
			whiteList.allow=new HashSet<>();
		}
	}
	private synchronized boolean save()
	{
		try {
			FileOutputStream fout=new FileOutputStream(file);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			fout.write(gson.toJson(whiteList).getBytes("utf-8"));
			fout.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public static boolean isAllow(Integer id)
	{
		return filter.whiteList.allow.contains(id);
	}
	public static void add(Integer...ids)
	{
		if(ids!=null)
		{
			for(Integer id:ids)
				filter.whiteList.allow.add(id);
			filter.save();
		}
	}
	public static void remove(Integer...ids)
	{
		if(ids!=null)
		{
			for(Integer id:ids)
				filter.whiteList.allow.remove(id);
			filter.save();
		}
	}
	public static Set<Integer>getWhiteList()
	{
		return filter.whiteList.allow;
	}
	public static void main(String[] args) {
		System.out.println(ServerFilter.isAllow(111));
	}
}
