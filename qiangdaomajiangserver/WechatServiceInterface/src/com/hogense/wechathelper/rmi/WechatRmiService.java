package com.hogense.wechathelper.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import javax.print.DocFlavor.STRING;

public interface WechatRmiService extends Remote{
	
	public boolean sendMessage(long id,String robotid,String gid,String msg)throws RemoteException;
	public String getLoginQrcode() throws RemoteException;
	public String getLoginStatus(String instanceid)throws RemoteException;
	public String load(String instanceid)throws RemoteException;
	public boolean start(String instanceid,String robotid,String config)throws RemoteException;
	public Map<String,Boolean> getStatus(String[]robotids)throws RemoteException;
	public boolean close(String robotid,String uid)throws RemoteException;
	public boolean updateConfig(String robotid,String uid,Map<String, Object>data)throws RemoteException;
	public boolean restartRobot(String robotid,String uid)throws RemoteException;
	public String startRongRobot(String gameid,String gameuid,String robotid,String nickname,String head,String config)throws RemoteException;;
	public String getRongRobotStatus(String instanceid,String robotid)throws RemoteException;;
	public Map<String,Boolean> getRongRobotStatus(String[]robotids)throws RemoteException;
	public boolean stopRongRobot(String robotid)throws RemoteException;;
	 
}
