package com.lebin.game.module.data;

import java.io.Serializable;

import com.google.gson.Gson;

public class SerializaableObject implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Gson GSON=new Gson();
	public static <T> T create(String json,Class<T>clss)
	{
		return GSON.fromJson(json, clss);
	}
	@Override
	public String toString() {
		return GSON.toJson(this);
	}
	
}
