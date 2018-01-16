package com.hogense.game.server.context;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.eclipse.jetty.servlet.ServletHolder;

import com.hogense.game.server.annotation.Autowired;
import com.hogense.game.server.annotation.Bean;

public class HttpServletHolder extends ServletHolder{

	public HttpServletHolder() {
		super();
		// TODO Auto-generated constructor stub
	}

	public HttpServletHolder(Class<? extends Servlet> servlet) {
		super(servlet);
		// TODO Auto-generated constructor stub
		
	}

	public HttpServletHolder(Servlet servlet) {
		super(servlet);
		// TODO Auto-generated constructor stub
	}

	public HttpServletHolder(org.eclipse.jetty.servlet.BaseHolder.Source creator) {
		super(creator);
		// TODO Auto-generated constructor stub
	}

	public HttpServletHolder(String name, Class<? extends Servlet> servlet) {
		super(name, servlet);
		// TODO Auto-generated constructor stub
	}

	public HttpServletHolder(String name, Servlet servlet) {
		super(name, servlet);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected Servlet newInstance() throws ServletException, IllegalAccessException, InstantiationException {
		
		Servlet servlet=super.newInstance();
		initObject(servlet);
		return servlet;
	}
	private void initObject(Object obj) throws IllegalArgumentException, IllegalAccessException, InstantiationException
	{
		if(obj==null)
			return;
		Class<?> cls=obj.getClass();
		while(cls!=Object.class)
		{
			Field[] fds= cls.getDeclaredFields();
			for(Field fd:fds)
			{
				Bean bean=fd.getAnnotation(Bean.class);
				if(bean!=null)
				{
					Object v=getServletHandler().getServer().getBean(fd.getType());
					if(v!=null)
					{
						fd.setAccessible(true);
						fd.set(obj, v);
					}
				}
				else 
				{
					Autowired autowired=fd.getAnnotation(Autowired.class);
					if(autowired!=null)
					{
						Object newObj=fd.getType().newInstance();
						fd.setAccessible(true);
						fd.set(obj, newObj);
						initObject(newObj);
					}
				}
				
			}
			cls=cls.getSuperclass();
		}
	}
}
