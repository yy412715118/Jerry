package com.hogense.game.server.websocket;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;  
   
@SuppressWarnings("serial")  
public class EndpointServlet extends WebSocketServlet {  
	private Class<?>cls;
	private long idletimeout;
	public EndpointServlet(Class<?>cls,long idletimeout) {
		this.cls=cls;
		this.idletimeout=idletimeout;
	}
	public EndpointServlet(Class<?>cls) {
		this(cls,35000);
	}
	public EndpointServlet(String clsname) throws ClassNotFoundException {
		this(Class.forName(clsname),35000);
	}
	public EndpointServlet(String clsname,long idletimeout) throws ClassNotFoundException {
		this(Class.forName(clsname),idletimeout);
	}
    @Override  
    public void configure(WebSocketServletFactory factory) {  
        factory.getPolicy().setIdleTimeout(idletimeout);  
        factory.register(cls);  
    }  
    
}  