package com.lebin.server.netty.server.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.hogense.roomgame.utils.ServiceHelper;
import com.lebin.game.module.IAccount;
import com.lebin.game.module.IDaoService;
import com.lebin.server.util.StringUtil;
import com.lebin.server.util.TaskExecutor;
import com.lebin.server.util.UpdateTask;

import atg.taglib.json.util.JSONObject;

public class RoomService implements IDaoService{
	static Logger LOG=LoggerFactory.getLogger(RoomService.class);
	public JSONObject createRoom(int roomid,String token,long reqtime,int gameid,long uid,String groupid,int type,String host,int port,int serverid)
	{
		try {
			String rsp=ServiceHelper.getRoomService().createRoom(gameid, uid, token, reqtime, type, host, port, serverid, roomid);
			if(StringUtil.isNullOrEmpty(rsp))
				return null;
			return new JSONObject(rsp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public JSONObject autoCreateRoom(int roomid,int gameid,long uid,String groupid,int type,String host,int port,int serverid)
	{
		try {
			String rsp=ServiceHelper.getRoomService().autoCreateRoom(roomid, gameid, uid, groupid, type, host, port, serverid);
			if(StringUtil.isNullOrEmpty(rsp))
				return null;
			return new JSONObject(rsp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public JSONObject joinRoom(int roomid,String token,long reqtime,int gameid,long uid)
	{
		try {
			String rsp=ServiceHelper.getRoomService().joinRoom(gameid, uid, token, reqtime, roomid);
			if(StringUtil.isNullOrEmpty(rsp))
				return null;
			return new JSONObject(rsp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public boolean freeRooms(int gameid,int serverid)
	{

		try {
			return ServiceHelper.getRoomService().freeRooms(gameid,serverid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	private JSONObject doFreeRoom(int gameid,  int roomid,int freetype)
	{
		try {
			String rsp=ServiceHelper.getRoomService().freeRoom(gameid,roomid, freetype);
			if(StringUtil.isNullOrEmpty(rsp))
				return null;
			return new JSONObject(rsp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * @param roomid
	 * @param freetype '0：未解散 1:结束解散，2：投票解散 3：自动解散 4：维护解散 5：加入房间失败解散 6房主解散',
	 * @return
	 */
	public boolean freeRoom(int gameid,  int roomid,int freetype)
	{
		TaskExecutor.getExcuteService().execute(new UpdateTask() {
			@Override
			public void run() {
				trytime++;
				LOG.debug("第"+(trytime)+"尝试解散房间"+roomid);
				JSONObject rs=doFreeRoom(gameid, roomid, freetype);
				try {
					if(rs!=null&&(rs.getInt("status")==200||rs.getInt("status")==606))
					{
						LOG.debug(roomid+" 已从数据库解散，"+rs);
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error(roomid+" 从数据库解散失败，"+rs+" "+e.getMessage());
				}
				if(trytime<10)
				{
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					TaskExecutor.getExcuteService().execute(this);
				}
			}
		});
		return true;
	}
	public boolean freeJoinFailRoom(int gameid,int roomid)
	{
		return freeRoom(gameid,roomid, 5);
	}
	/**
	 * 结束解散
	 * @param roomid
	 * @return
	 */
	public boolean freeFinishRoom(int gameid,int roomid)
	{
		return freeRoom(gameid,roomid, 1);
	}
	/**
	 * 投票解散
	 * @param roomid
	 * @return
	 */
	public boolean freeRoomByVote(int gameid,int roomid)
	{
		return freeRoom(gameid,roomid, 2);
	}

	public boolean exitRoom(long uid, int roomid, int gameid) {
		try {
			return ServiceHelper.getRoomService().exitRoom(uid, roomid, gameid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public JSONObject doPayRoom(int gameid,int roomid,String serviceid)
	{
		try {
			String rsp=ServiceHelper.getRoomService().payRoom(gameid,roomid, serviceid);
			if(StringUtil.isNullOrEmpty(rsp))
				return null;
			return new JSONObject(rsp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public JSONObject payRoom(int gameid,int roomid,String serviceid)
	{
		TaskExecutor.getExcuteService().execute(new UpdateTask() {
			@Override
			public void run() {
				trytime++;
				LOG.debug("第"+(trytime)+"尝试结算房间"+roomid);
				JSONObject rs=doPayRoom(gameid, roomid, serviceid);
				try {
					if(rs!=null&&rs.getInt("status")!=608)
					{
						LOG.debug("房间"+roomid+",已结算");
						if(rs.getInt("status")==200)
						{
							IAccount account=new IAccount();
							account.uid=rs.getLong("uid");
							account.account=rs.getLong("account");
						}
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				LOG.debug("结算房间"+roomid+"失败"+rs);
				if(trytime<10)
				{
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					TaskExecutor.getExcuteService().execute(this);
				}
			}
		});
		return null;
	}
	public int[] doExcuteBatch(String sql,List<Object[]>args) {
		try {
			return ServiceHelper.getRoomService().batchUpdate(sql, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public int[] excuteBatch(String sql,List<Object[]>args) {
		TaskExecutor.getExcuteService().execute(new UpdateTask() {
			@Override
			public void run() {
				trytime++;
				LOG.debug("第"+(trytime)+"次批量更新");
				if(doExcuteBatch(sql, args)!=null)
					return;
				if(trytime<10)
				{
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					TaskExecutor.getExcuteService().execute(this);
				}
			}
		});
		return null;
	}
	public int doExcuteUpdate(String sql, Object... args) {
		try {
			return ServiceHelper.getRoomService().excuteupdate(sql, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	@Override
	public int excuteUpdate(String sql, Object... args) {
		TaskExecutor.getExcuteService().execute(new UpdateTask() {
			@Override
			public void run() {
				trytime++;
				LOG.debug("第"+(trytime)+"次更新");
				if(doExcuteUpdate(sql, args)!=-1)
					return;
				if(trytime<10)
				{
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					TaskExecutor.getExcuteService().execute(this);
				}
			}
		});
		return 1;
	}
}
