package com.hogense.game.server.sevice;

import com.hogense.roomgame.utils.ServiceHelper;

import atg.taglib.json.util.JSONObject;

public class RoomService {
	
	public JSONObject getRoomServerAddress(int roomid,int gameid,long uid)
	{
		try {
			String rsp= ServiceHelper.getRoomService().getRoomInfo(gameid, roomid,uid,true);
			if(rsp==null)
				return null;
			return new JSONObject(rsp);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	public int getDailyPlayTime(int gameid,long uid)
	{
		try {
			String sql="SELECT COUNT(DISTINCT t_gameinfo.serviceid) as ct FROM db_gamedata.t_gameinfo WHERE freestate=1 AND serviceid IN("
				+"SELECT DISTINCT serviceid  FROM db_gamedata.t_gameresult WHERE createdate=CURDATE() AND gameid=? AND uid=?)";
			String rsp=ServiceHelper.getDaoService().queryUnique(sql, gameid,uid);
			if(rsp!=null)
				return new JSONObject(rsp).getInt("ct");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
}
