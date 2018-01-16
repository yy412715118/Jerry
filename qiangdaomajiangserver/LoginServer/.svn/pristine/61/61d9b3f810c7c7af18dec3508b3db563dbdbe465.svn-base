package com.hogense.game.server;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.xml.XmlConfiguration;

import com.hogense.roomgame.databaseclient.ApplicationContext;
public class Main {
	  public static void main(String[] args) throws Exception {  
			PropertyConfigurator.configure("config/log4j.properties");
			ApplicationContext.load("config/dubbo.xml");
	        Resource resource = Resource.newResource("config/config.xml");  
	        XmlConfiguration configuration = new XmlConfiguration(resource.getInputStream());  
	        Server server = (Server) configuration.configure();  
	        server.start();  
	        server.join(); 
	    }  
}
