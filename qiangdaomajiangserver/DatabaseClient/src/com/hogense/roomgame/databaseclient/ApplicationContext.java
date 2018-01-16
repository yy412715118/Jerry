package com.hogense.roomgame.databaseclient;

import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class ApplicationContext {
	private static AbstractXmlApplicationContext context;
	private static final Object LOCK=new Object();
	public  static void load(String file)
	{
		synchronized (LOCK) {
			if(context==null)
			{
				context=new FileSystemXmlApplicationContext(file);
				context.start();
			}
		}
	}
	public static Object getBean(String name)
	{
		return context.getBean(name);
	}
	public static Object getBean(Class<?> cls)
	{
		return context.getBean(cls);
	}
}
