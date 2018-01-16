package com.hogense.roomgame.service.interfaces;

public interface AccountService {
	public String loginByThirdPart(int gameid,String openid,String nickname,String head,int sex,String ip,String deviceinfo,int type);
	public String loginByUid(int gameid,long uid,String sign,long reqtime);
	public String getUserByOpenId(String openid,int type);
	public String getUserByAccount(String account,String psw);
	public boolean changeUserLockState(int gameid,long uid,boolean lock);
}
