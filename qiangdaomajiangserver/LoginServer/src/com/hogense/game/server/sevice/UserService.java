package com.hogense.game.server.sevice;

import com.hogense.roomgame.service.interfaces.AccountService;
import com.hogense.roomgame.utils.ServiceHelper;
import com.hogense.util.StringUtil;

import atg.taglib.json.util.JSONObject;

public class UserService {
	public JSONObject login(String ip,String gameid,String devicdeid,JSONObject userinfo,int type)
	{
		try {
			String openid=userinfo.getString("openid");
			String nickname=userinfo.getString("nickname");
			String head=userinfo.getString("head");
			String sex=userinfo.getString("sex");
			return login(ip, gameid, devicdeid, openid, nickname, head, type,sex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public JSONObject login(String ip,String gameid,String devicdeid,String openid,String nickname,String head,int type,String sex)
	{
		try {
			AccountService accountService=ServiceHelper.getAccountService();
			String rsp=accountService.loginByThirdPart(Integer.parseInt(gameid), openid, nickname, head, Integer.parseInt(sex), ip, devicdeid, type);
			System.out.println(rsp);
			if(!StringUtil.isNullOrEmpty(rsp))
			{
				JSONObject user= new JSONObject(rsp);
				if(user.getInt("status")==200)
					return user;
//				JSONObject curroom=getCurRoomServerAddress(rs.getLong("uid"), gameid);
//				if(curroom!=null&&curroom.has("roomid"))
//				{
//					rs.put("cur", curroom);
//				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public JSONObject getCurRoomServerAddress(long uid,String gameid)
	{
//		JSONObject rs=dao.findUniqueJsonBySql("select t_gameinfo.serverid,t_gameinfo.host,"
//				+" t_gameinfo.port,t_room.roomid from db_room.t_room left join db_gamedata.t_gameinfo"
//			+" on t_gameinfo.serviceid=t_room.id where t_room.id=(select lockserviceid from db_room.t_roomlock where uid=? and gameid=?) and t_gameinfo.freestate=0", uid,gameid);
//		return rs;
		return null;
	}
	public JSONObject getGuestInfo(String openid)
	{
		try {
			AccountService accountService=ServiceHelper.getAccountService();
			String rsp=accountService.getUserByOpenId(openid, 0);
			if(!StringUtil.isNullOrEmpty(rsp))
				return new JSONObject(rsp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public JSONObject getAccountInfo(String uid,String psw)
	{
		try {
			AccountService accountService=ServiceHelper.getAccountService();
			String rsp=accountService.getUserByAccount(uid,psw);
			if(!StringUtil.isNullOrEmpty(rsp))
				return new JSONObject(rsp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
