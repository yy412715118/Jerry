package com.hogense.game.server.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hogense.data.RequestData;
import com.hogense.data.ResponseData;
import com.hogense.game.server.annotation.Autowired;
import com.hogense.game.server.annotation.HttpService;
import com.hogense.game.server.sevice.RoomServerManager;
import com.hogense.game.server.sevice.RoomService;
import com.hogense.server.define.ServerInfo;
import com.hogense.server.define.Status;
import com.hogense.util.StringUtil;

import atg.taglib.json.util.JSONArray;
import atg.taglib.json.util.JSONObject;
@HttpService("/api/room")
public class RoomServlet extends CheckTokenServlet	{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Autowired
	RoomService service;
	@Override
	protected void handler(long uid, String gameid, String token, HttpServletRequest request,
			HttpServletResponse response, RequestData reqData,ResponseData rsp) {
		Integer op=reqData.getParameterInt("op");
		if(op==1)//建立房间，获取房间服务器地址和房间类型信息
			getCreateRoomServer(uid, Integer.parseInt(gameid), token, request, response, reqData, rsp);
		else if(op==2)//加入房间，获取房间服务器地址
			getJoinRoomServer(uid, gameid, token, request, response, reqData, rsp);
	}
	/**
	 * 获取房间服务器地址
	 * @param uid
	 * @param gameid
	 * @param token
	 * @param request
	 * @param response
	 * @param reqData
	 * @param rsp
	 */
	private void getCreateRoomServer(long uid, int gameid, String token, HttpServletRequest request,
			HttpServletResponse response, RequestData reqData,ResponseData rsp)
	{
		try {
			ServerInfo info=RoomServerManager.getManager().getServer();
			if(info==null)
			{
				rsp.setStatus(Status.NO_AVSERVER);
				return;
			}
			Integer limit=reqData.getParameterInt("limit");
			if(limit!=null&&limit>-1)
			{
				int time=service.getDailyPlayTime(gameid, uid);
				if(time>=limit)
				{
					rsp.setStatus(Status.PLAYTIME_LIMIT).setMsg(String.format("每日最多可玩%d局，\n请明日再来！",limit));
					return;
				}
			}
			JSONObject data=new JSONObject();
			data.put("serverid",info.serverid);
			data.put("host", info.host);
			data.put("port", info.port);
			data.put("type", 0);
			rsp.setStatus(Status.OK).setData(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void getJoinRoomServer(long uid, String gameid, String token, HttpServletRequest request,
			HttpServletResponse response, RequestData reqData,ResponseData rsp)
	{
		Integer limit=reqData.getParameterInt("limit");
		if(limit!=null)
		{
			int time=service.getDailyPlayTime(Integer.parseInt(gameid), uid);
			if(time>=limit&&limit>-1)
			{
				rsp.setStatus(Status.PLAYTIME_LIMIT).setMsg(String.format("每日最多可玩%d局，\n请明日再来！",limit));
				return;
			}
		}
		int roomid=reqData.getParameterInt("roomid");
		try {
			JSONObject data=service.getRoomServerAddress(roomid, StringUtil.toInt(gameid),uid);
			if(data!=null)
				rsp.setStatus(Status.OK).setData(data);
			else
				rsp.setStatus(Status.ROOM_NOTFOUND);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void exitRoom(long uid, String gameid, String token, HttpServletRequest request,
			HttpServletResponse response, RequestData reqData,ResponseData rsp)
	{
		
	}
	private void freeRoom(long uid, String gameid, String token, HttpServletRequest request,
			HttpServletResponse response, RequestData reqData,ResponseData rsp)
	{
		
	}
	
}
