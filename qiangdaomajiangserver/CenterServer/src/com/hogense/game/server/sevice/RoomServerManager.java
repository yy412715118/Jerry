package com.hogense.game.server.sevice;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.hogense.game.server.Config;
import com.hogense.server.define.ServerInfo;

public class RoomServerManager {
	private static final Logger LOG = Logger.getLogger(RoomServerManager.class.getName());
	private LinkedList<ServerInfo>serverInfos=new LinkedList<>();;
	private static RoomServerManager manager=new RoomServerManager();
	private final long timeout=Config.getLong("servertimeout",60000);
	private final long timeouttoremove=Config.getLong("servertimeouttoremove",120000);//移出不响应服务器时间
	private int index=0;
	private RoomServerManager()
	{
		Timer timer = new Timer();
	    timer.scheduleAtFixedRate(new TimerTask() {
	      public void run() {
	    	  checkAlive();
	      }
	    }, 30000, 30000);
	}
	private void checkAlive()
	{
		synchronized (serverInfos) {
			long now=System.currentTimeMillis();
			List<ServerInfo>remove=new ArrayList<>();
			for(ServerInfo info:serverInfos)
			{
				long v=now-info.lasttime;
				if(v>=timeout)//超过1分钟没信息
				{
					LOG.warn("服务器:"+info.serverid+",host:"+info.host+",port:"+info.port+"超过"+(v/1000)+"s无响应");
					info.alive=false;
					if(timeouttoremove<=v)
					{
						remove.add(info);
						LOG.warn("服务器:"+info.serverid+",host:"+info.host+",port:"+info.port+"超过"+(v/1000)+"s无响应,从服务器列表中移除");
					}
				}
			}
			serverInfos.removeAll(remove);
		}
	}
	public static RoomServerManager getManager() {
		return manager;
	}
	public boolean add(ServerInfo serverInfo)
	{
		synchronized (serverInfos) {
			if(serverInfos.contains(serverInfo))
			{
				ServerInfo info=getServerById(serverInfo.serverid);
				if(info!=null&&info.host.equals(serverInfo.host)&&info.port==serverInfo.port)
					return true;
				return false;
			}
			serverInfo.alive=false;
			serverInfo.lasttime=System.currentTimeMillis();
			serverInfos.add(serverInfo);
			return true;
		}
	}
	public ServerInfo getServerById(int id)
	{
		synchronized (serverInfos) {
			for(ServerInfo info:serverInfos)
				if(info.serverid==id)
					return info;
			return null;
		}
	}
	public boolean syn(ServerInfo serverInfo)
	{
		synchronized (serverInfos) {
			if(serverInfos.contains(serverInfo))
			{
				for(ServerInfo info:serverInfos)
				{
					if(info.serverid==serverInfo.serverid)
					{
						info.alive=true;
						info.lasttime=System.currentTimeMillis();
						info.usercount=serverInfo.usercount;
						info.roomcount=serverInfo.roomcount;
						info.maxroom=serverInfo.maxroom;
						//LOG.info("服务器:"+info.serverid+",最大房间数:"+info.maxroom+",在线房间数:"+info.roomcount+",人数:"+info.usercount);
						return true;
					}
				}
				return false;
			}
			serverInfo.lasttime=System.currentTimeMillis();
			serverInfo.alive=true;
			serverInfos.add(serverInfo);
			return true;
		}
	}
	public ServerInfo remove(int serverid)
	{
		synchronized (serverInfos) {
			for(ServerInfo info:serverInfos)
				if(info.serverid==serverid)
				{ 
					serverInfos.remove(info);
					return info;
				}
			return null;
		}
		
	}
	public ServerInfo getServer()
	{
		synchronized (serverInfos) {
			ServerInfo rs=null;
			int from=index;
			if(from<0)
				from=0;
			int size=serverInfos.size();
			for(int i=0;i<size;i++)
			{
				int p=(i+from)%size;
				ServerInfo info=serverInfos.get(p);
				if(ServerFilter.isAllow(info.serverid)&&info.alive&&info.roomcount<info.maxroom)
				{
					if(rs==null)
						rs=info;
					else 
						if(rs.roomcount*1f/rs.maxroom>info.roomcount*1f/info.maxroom)
							rs=info;
				}
			}
			if(rs!=null)
			{
				index++;
				if(index>=size)
					index=0;
			}
			return rs;
		}
	}
	public LinkedList<ServerInfo> getServerInfos() {
		return serverInfos;
	}
}
