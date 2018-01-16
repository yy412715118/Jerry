package com.hogense.game.server.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hogense.data.ResponseData;
import com.hogense.game.server.annotation.HttpService;
import com.hogense.game.server.sevice.RoomServerManager;
import com.hogense.server.define.ServerInfo;
import com.hogense.server.define.Status;

import atg.taglib.json.util.JSONObject;
@HttpService("/api/server/getroomserver")
public class RoomServerServlet extends BaseServerHttpServlet{

	@Override
	protected void handler(HttpServletRequest request, HttpServletResponse response, ResponseData rsp) {
		ServerInfo info=RoomServerManager.getManager().getServer();
		if(info==null)
		{
			rsp.setStatus(Status.NO_AVSERVER);
			return;
		}
		JSONObject data=new JSONObject();
		try {
			data.put("serverid",info.serverid);
			data.put("host", info.host);
			data.put("port", info.port);
		} catch (Exception e) {
			// TODO: handle exception
		}
		rsp.setStatus(Status.OK).setData(data);
		
	}

}
