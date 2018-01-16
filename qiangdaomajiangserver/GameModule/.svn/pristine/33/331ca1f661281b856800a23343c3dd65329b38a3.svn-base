package com.lebin.game.module;

import java.util.List;
import java.util.Map;

import com.lebin.game.module.data.Message;

public interface IRoom {
	public void init(String config);
	public void onCreate(IRoomManager manager,IRecorder recorder);
	public int getRoomid();
	public Object getMasterId();
	public void setMasterId(Object id);
	public void setRoomid(int id);
	public boolean send(IUser user, Object data);
	public void sendAll(Object data);
	public boolean onMessage(Object data,IUser user,ISession session);
	public int joinRoom(int chairId,IUser user,ISession session);
	public boolean sendGameScene(int chairId,IUser user,Object data);
	public boolean sendGameStart(int chairId,IUser user,Object data);
	public boolean sendGameData(int chairId,IUser user,Object data);
	public boolean exitRoom(IUser user,ISession session);
	public boolean onUserOffLine(IUser user,ISession session);
	public boolean onUserConnect(IUser user,ISession session);
	public boolean sendMessage(int chairId,IUser user,Message msg);
	public int getChairId(IUser user);
	public void destroy();
	public IUser getUser(int chairId);
	public List<IUser>getPlayers();
	public Object getRoomInfo();
	public String getServiceId();
	public void onGameOver(Object result,int type,int votechairid);
	public void onGameStart();
	public void onRoundOver(int circle,int round,Object result,Object record,boolean needpay);
	public long getExpiryTime();
	public boolean isActive();
	public int getNeedPlayerCount();
	public int getCurPlayerCount();
	public Object getUserData(String key);
	public void setUserData(String key,Object value);
	public void notifyBack(List<IUser>users);
	public void clock(long time);
}
