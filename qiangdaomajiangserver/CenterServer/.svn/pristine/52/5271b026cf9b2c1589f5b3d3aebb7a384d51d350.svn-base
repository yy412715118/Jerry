package com.hogense.game.server.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hogense.data.ResponseData;
import com.hogense.game.server.annotation.Autowired;
import com.hogense.game.server.annotation.HttpService;
import com.hogense.game.server.sevice.HistoryService;
import com.hogense.server.define.Status;

import atg.taglib.json.util.JSONObject;
@HttpService("/api/server/getroomresult")
public class RoomResultServlet extends BaseServerHttpServlet{
	@Autowired
	HistoryService service;
	@Override
	protected void handler(HttpServletRequest request, HttpServletResponse response, ResponseData rsp) {
		try {
			String gameid=request.getParameter("gameid");
			String master=request.getParameter("master");
			String roomid=request.getParameter("roomid");
			JSONObject rs=service.getRoomResult(gameid, roomid, master);
			if(rs==null)
				rsp.setStatus(Status.ROOM_RESULT_NOTFOUND);
			else 
				rsp.setStatus(Status.OK).setData(rs);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
	}

}
