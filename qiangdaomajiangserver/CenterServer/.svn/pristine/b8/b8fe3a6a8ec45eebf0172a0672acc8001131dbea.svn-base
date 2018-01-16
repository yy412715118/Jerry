package com.hogense.game.server.sevice;

import com.hogense.roomgame.utils.ServiceHelper;

import atg.taglib.json.util.JSONArray;
import atg.taglib.json.util.JSONObject;

public class HistoryService {
	public JSONObject getWeekData(long uid,int gameid)
	{
		try {
			String rsp=ServiceHelper.getDaoService().callProfun("db_gamedata.fun_week_info(?,?)", uid,gameid);
			if(rsp!=null)
				return new JSONObject(rsp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public JSONArray getRecordData(long uid,int gameid)
	{
		try {
			String sql="SELECT roomid,DATE_FORMAT(createtime,'%Y-%m-%d %T') as time,t_gameinfo.resultdata,t_gameinfo.serviceid FROM"
					+" (SELECT DISTINCT serviceid from db_gamedata.t_gameresult WHERE" 
					+" createdate>DATE_ADD(NOW(),INTERVAL -7 DAY) and gameid=? and uid=? ) b"
					+" INNER JOIN db_gamedata.t_gameinfo on t_gameinfo.serviceid=b.serviceid ORDER BY createtime desc LIMIT 30";
			String rsp=ServiceHelper.getDaoService().queryList(sql, gameid,uid);
			System.out.println(rsp);
			if(rsp!=null)
				return new JSONArray(rsp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public JSONArray getReviewData(String serviceid)
	{
		try {
			String sql="SELECT circle,round,roomid,time,data,url from db_gamedata.t_record WHERE serviceid=? ORDER BY round asc;";
			String rsp=ServiceHelper.getDaoService().queryList(sql, serviceid);
			if(rsp!=null)
				return new JSONArray(rsp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public JSONObject getRoomResult(String gameid,String roomid,String master)
	{
		try {
			String sql="SELECT DATE_FORMAT(starttime,'%H:%i') stime,DATE_FORMAT(endtime,'%H:%i') etime,resultdata,master,roomid,nickname,ruleid,rulename FROM db_gamedata.t_gameinfo INNER JOIN db_account.t_user ON t_user.uid=t_gameinfo.`master` WHERE createdate>DATE_SUB(CURDATE(),INTERVAL 1 DAY)"
						+" AND endtime>DATE_SUB(NOW(),INTERVAL 30 MINUTE) AND gameid=? AND master=? AND roomid=? AND freestate!=0 LIMIT 1; ";
			String rsp=ServiceHelper.getDaoService().queryList(sql, gameid,master,roomid);
			if(rsp!=null)
			{
				JSONArray array=new JSONArray(rsp);
				if(array.size()>0)
					return array.getJSONObject(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
