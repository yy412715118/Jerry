package com.hogense.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.hogense.redis.RedisClientTemplate;


public class Server {

	public String sayHello(String name) {
		// TODO Auto-generated method stub
		return name;
	}
	public static void main(String[] args) throws IOException {
		Logger logger=LoggerFactory.getLogger(Server.class);
		FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext("config/config.xml");
        context.start();
        RedisClientTemplate template=(RedisClientTemplate) context.getBean("redisClientTemplate");
       Object object= template.getAllShardInfo();
       if(object==null)
       {
    	   logger.error("redis服务器无法连接");
    	   return;
       }
       logger.info("服务已启动");
       while(true)
       {
    	   
       }
	}
}
