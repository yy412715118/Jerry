package com.hogense.game.server.context;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.DispatcherType;

import org.apache.log4j.Logger;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.HandlerContainer;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import com.hogense.game.server.annotation.HttpService;
import com.hogense.game.server.annotation.WebSocketService;
import com.hogense.game.server.websocket.EndpointServlet;
import com.lebin.pakageutil.PackageScanner;

public class ServerContext extends WebAppContext{
	private static final Logger LOG = Logger.getLogger(ServerContext.class.getName());
	public ServerContext() {
		super();
	}
	public ServerContext(HandlerContainer parent, String contextPath, SessionHandler sessionHandler,
			SecurityHandler securityHandler, ServletHandler servletHandler, ErrorHandler errorHandler, int options) {
		super(parent, contextPath, sessionHandler, securityHandler, servletHandler, errorHandler, options);
		// TODO Auto-generated constructor stub
	}

	public ServerContext(HandlerContainer parent, String webApp, String contextPath) {
		super(parent, webApp, contextPath);
		// TODO Auto-generated constructor stub
	}

	public ServerContext(SessionHandler sessionHandler, SecurityHandler securityHandler,
			ServletHandler servletHandler, ErrorHandler errorHandler) {
		super(sessionHandler, securityHandler, servletHandler, errorHandler);
		// TODO Auto-generated constructor stub
	}

	public ServerContext(String webApp, String contextPath) {
		super(webApp, contextPath);
		// TODO Auto-generated constructor stub
	}
	public void addEndpointServlet(Class<?> cls,String pathSpec) 
	{
		addServlet(new ServletHolder(new EndpointServlet(cls)), pathSpec);  
	}
	public void addEndpointServlet(Class<?> cls,int idletimeout,String pathSpec) 
	{
		addServlet(new ServletHolder(new EndpointServlet(cls,idletimeout)), pathSpec);  
	}
	public void addEndpointServlet(String clsname,String pathSpec) throws ClassNotFoundException 
	{
		addServlet(new ServletHolder(new EndpointServlet(Class.forName(clsname))), pathSpec);  
	}
	public void addEndpointServlet(String clsname,int idletimeout,String pathSpec) throws ClassNotFoundException 
	{
		addServlet(new ServletHolder(new EndpointServlet(Class.forName(clsname),idletimeout)), pathSpec);  
	}
	public void addFilter(FilterHolder filterHolder, String pathSpec,String[] dispatches) {
		EnumSet<DispatcherType>set=EnumSet.noneOf(DispatcherType.class);
		if(dispatches!=null)
		{
			for(String s:dispatches)
				set.add(DispatcherType.valueOf(s));
		}
		super.addFilter(filterHolder, pathSpec, set);
	}
	public void addFilter(FilterHolder filterHolder, String pathSpec) {
		EnumSet<DispatcherType>set=EnumSet.allOf(DispatcherType.class);
		super.addFilter(filterHolder, pathSpec, set);
	}
	public void addServletFromScanner(String[] scanpackages)
	{
		if(scanpackages!=null)
		{
			Set<Class<?>>set=new HashSet<>();
			for(String scanpackage:scanpackages)
			{
				PackageScanner scn=new PackageScanner(scanpackage);
				scn.setAnnocation(HttpService.class,WebSocketService.class);
				try {
					Set<Class<?>>cls=scn.scan();
					if(cls!=null)
						set.addAll(cls);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			for(Class<?>cls:set)
			{
				HttpService httpService=cls.getAnnotation(HttpService.class);
				if(httpService!=null)
				{
					LOG.info("注册 HttpService:"+httpService.value());
					addServlet(new HttpServletHolder((Class)cls), httpService.value());
				}
				WebSocketService socketService=cls.getAnnotation(WebSocketService.class);
				if(socketService!=null)
				{
					LOG.info("注册 WebSocketService:"+socketService.value());
					addServlet(new ServletHolder(new EndpointServlet(cls,socketService.idletimeout())), socketService.value()); 
				}
			}
		}
	}
}
