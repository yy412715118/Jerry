package com.hogense.server.define;

public class ServerInfo {
	public final String host;
	public final int port;
	public final int serverid;
	public boolean alive=true;
	public int roomcount=0;
	public int usercount=0;
	public int maxroom;
	public long lasttime;
	public ServerInfo(int serverid,String host,int port) {
		this.host=host;
		this.port=port;
		this.serverid=serverid;
	}
	@Override
	public int hashCode() {
		return serverid;
	}
	@Override
	public boolean equals(Object obj) {
		if(obj==null||!(obj instanceof ServerInfo))
			return false;
		return ((ServerInfo)obj).serverid==serverid;
	}
}
