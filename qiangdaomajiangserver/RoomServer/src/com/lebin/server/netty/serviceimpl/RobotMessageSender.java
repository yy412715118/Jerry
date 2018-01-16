package com.lebin.server.netty.serviceimpl;

import java.rmi.Naming;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.Gson;
import com.hogense.wechathelper.rmi.WechatRmiService;
import com.lebin.server.util.Config;

public class RobotMessageSender {
	static RobotMessageSender sender=new RobotMessageSender();
	static Gson gson=new  Gson();
	private BlockingQueue<Msg>MSGLIST=new LinkedBlockingQueue();
	private String savepath;
	ExecutorService fixedThreadPool;
	private RobotMessageSender()
	{
		savepath=Config.getProperty("robot_msg_savepath");
		fixedThreadPool = Executors.newFixedThreadPool(Config.getInt("robot_msg_send_thread", 10));  
	}
	public void sendMessage(String address,String robotid,String gid,String content)
	{
		if(robotid==null||gid==null)
			return;
		Msg msg=new Msg();
		msg.to=address;
		msg.id=System.currentTimeMillis();
		msg.msg=content;
		msg.gid=gid;
		msg.robotid=robotid;
		try {
			MSGLIST.put(msg);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	Runnable runnable=new Runnable() {
		
		@Override
		public void run() {
			while(true)
			{
				try {
					Msg record=MSGLIST.take();
					if(record!=null)
						handler(record);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	Thread thread;
	public synchronized void start()
	{
		if(thread!=null)
			return;
		thread=new Thread(runnable);
		thread.start();
	}
	public static RobotMessageSender getIntance()
	{
		return sender;
	}
	private void handler(final Msg msg)
	{
		fixedThreadPool.execute(new Runnable() {
			
			@Override
			public void run() {
				send(msg);
				
			}
		});
	}
	private void send(Msg msg)
	{
		boolean rs=false;
		long now=System.currentTimeMillis();
		if(now-msg.lasttime<10000)
		{
			try {
				MSGLIST.put(msg);
				return;
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		try {
			WechatRmiService service=(WechatRmiService)Naming.lookup(msg.to);
			rs=service.sendMessage(msg.id, msg.robotid,msg.gid, msg.msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(!rs)
		{
			msg.time++;
			if(msg.time>60)
				return;
			msg.lasttime=System.currentTimeMillis();
			try {
				MSGLIST.put(msg);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	static class Msg
	{
		long id;
		String to;
		String robotid;
		String gid;
		String msg;
		long lasttime;
		int time;
	}
}
