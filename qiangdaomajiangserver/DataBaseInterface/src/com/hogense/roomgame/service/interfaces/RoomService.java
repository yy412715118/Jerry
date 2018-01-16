package com.hogense.roomgame.service.interfaces;

import java.util.List;

public interface RoomService {
	public String createRoom(int gameid,long uid,String sign,long reqtime,int type,String host,int port,int serverid,int roomid);
	public String joinRoom(int gameid,long uid,String sign,long reqtime,int roomid);
	public String autoCreateRoom(int roomid,int gameid,long uid,String groupid,int type,String host,int port,int serverid);
	public boolean freeRooms(int gameid, int serverid);
	/**
	 * 
	 * @param roomid
	 * @param freetype '0：未解散 1:结束解散，2：投票解散 3：自动解散 4：维护解散 5：加入房间失败解散 6房主解散',
	 * @return
	 */
	public String freeRoom(int gameid, int roomid,int freetype);
	public boolean exitRoom(long uid, int roomid, int gameid);
	public String payRoom(int gameid, int roomid,String serviceid);
	public int[] batchUpdate(String sql,List<Object[]>args);
	public int excuteupdate(String sql,Object...args);
	public String getCurRoomInfo(int gameid,long uid);
	public String getRoomInfo(int gameid,int roomid,long uid,boolean unlockifnotexit);
}
