package com.lebin.game.module.data;

import java.util.Collection;
import java.util.Iterator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class EntityUtil {
	private static Gson gson = new Gson();
	public static String toJson(Object obj)
	{
		return gson.toJson(obj);
	}
	public static <T>T create(String json,Class<T>clss)
	{
		return gson.fromJson(json, clss);
	}
	public static  JsonArray toJsonArray(Collection<?>collection)
	{
		JsonArray rs=new JsonArray();
		Iterator<?>iterator=collection.iterator();
		while(iterator.hasNext())
		{
			rs.add(gson.toJsonTree(iterator.next()));
		}
		return rs;
	}
	public static  JsonElement toJsonObject(Object obj)
	{
		return gson.toJsonTree(obj);
	}
}
