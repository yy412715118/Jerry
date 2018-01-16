package com.lebin.server.util;

import java.util.HashMap;
import java.util.Map;

public class ImpClassMapping {
	private static final Map<Class<?>, Class<?>>MAP=new HashMap<>();
	public static void regist(Class<?>key,Class<?>value)
	{
		MAP.put(key, value);
	}
	public static Class<?>get(Class<?>key)
	{
		return MAP.get(key);
	}
}
