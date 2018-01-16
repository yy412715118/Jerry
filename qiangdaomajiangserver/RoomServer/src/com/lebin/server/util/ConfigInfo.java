package com.lebin.server.util;

import java.io.File;
import java.util.HashSet;

public class ConfigInfo {
	public final int serverid;//服务器id，全局唯一
	public final int gameid;//游戏id
	public final String host;//本机服务端口
	public final int port;//本机服务端口
	public final int start_roomid;//开始房间号
	public final int end_roomid;//结束房间号
	public final int maxroomcount;//最大房间数
	public final String centerhost;//中心服务器地址
	public final String recordpath;//记录保存路径
	public final int maxopenfile;//最多打开文件数
	public final boolean isremoterecord;//是否是远程记录
	public final String uploadrecordurl;
	public final boolean checkip;
	public final HashSet<String>robotserviceips=new HashSet<>();
	public ConfigInfo()
	{
		serverid=Config.getInt("serverid", -1);
		if(serverid<10||serverid>99)
			throw new RuntimeException("serverid无效,serverid范围：10-99");
		port=Config.getInt("port", -1);
		if(port==-1)
			throw new RuntimeException("port无效");
		host=Config.getProperty("host");
		if(StringUtil.isNullOrEmpty(host))
			throw new RuntimeException("host无效");
		gameid=Config.getInt("gameid", -1);
		if(gameid==-1)
			throw new RuntimeException("gameid无效");
		String centerhost=Config.getProperty("centerhost");
		if(StringUtil.isNullOrEmpty(centerhost))
			throw new RuntimeException("centerhost无效");
		if(centerhost.endsWith("/"))
			centerhost=centerhost.substring(0, centerhost.length()-1);
		this.centerhost=centerhost;
		start_roomid=serverid*10000;
		end_roomid=start_roomid+9999;
		if(end_roomid<1||end_roomid<=start_roomid)
			throw new RuntimeException("end_roomid无效");
		recordpath=Config.getProperty("recordpath");
		maxopenfile=Config.getInt("maxopenfile", 200);
		maxroomcount=end_roomid-start_roomid+1;
		isremoterecord=Boolean.parseBoolean(Config.getProperty("isremoterecord","false"));
		checkip=Boolean.parseBoolean(Config.getProperty("checkip","true"));
		uploadrecordurl=Config.getProperty("uploadrecordurl");
		if(!isremoterecord)
		{
			File file=new File(recordpath);
			if(!file.isDirectory()||!file.exists())
				file.mkdirs();
		}
		else if(uploadrecordurl==null)
			throw new RuntimeException("uploadrecordurl未配置");
		String robotserviceip=Config.getProperty("robotserviceip","127.0.0.1");
		String ips[]=robotserviceip.split(",");
		for(String s:ips)
		{
			s=s.trim();
			if(s.isEmpty())
				continue;
			robotserviceips.add(s);
			System.out.println(s+" is allow");
		}
	}
	@Override
	public String toString() {
		return "serverid："+serverid+"\n"+
				"gameid："+gameid+"\n"+
				"host："+host+"\n"+
				"port："+port+"\n"+
				"centerhost："+centerhost+"\n"+
				"start_roomid："+start_roomid+"\n"+
				"end_roomid："+end_roomid+"\n"+
				"maxroomcount："+maxroomcount+"\n"+
				(isremoterecord?("uploadrecordurl："+uploadrecordurl):
				("maxopenfile："+maxopenfile+"\n"+"recordpath："+recordpath));
				
	}

}
