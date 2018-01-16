package com.hogense.game.server.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hogense.data.ResponseData;
import com.hogense.game.server.annotation.HttpService;
import com.hogense.game.server.sevice.RoomServerManager;
import com.hogense.server.define.ServerInfo;
import com.hogense.server.define.Status;
import com.hogense.util.StringUtil;
@HttpService("/api/server/syn")
public class ServerSynServlet extends BaseServerHttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void handler(HttpServletRequest request, HttpServletResponse response, ResponseData rsp) {
		String host=request.getParameter("host");
		Integer port=StringUtil.toInt(request.getParameter("port"));
		Integer serverid=StringUtil.toInt(request.getParameter("id"));
		Integer gameid=StringUtil.toInt(request.getParameter("gameid"));
		Integer roomcount=StringUtil.toInt(request.getParameter("roomcount"));
		Integer usercount=StringUtil.toInt(request.getParameter("usercount"));
		Integer maxroom=StringUtil.toInt(request.getParameter("maxroom"));
		if(StringUtil.isNullOrEmpty(host,port,serverid,gameid,roomcount,usercount,maxroom))
			rsp.setStatus(Status.ERROR);
		else 
		{
			ServerInfo info=new ServerInfo(serverid, host, port);
			info.roomcount=roomcount;
			info.usercount=usercount;
			info.maxroom=maxroom;
			info.alive=true;
			if(RoomServerManager.getManager().syn(info))
			{
				rsp.setStatus(Status.OK);
			}
			else rsp.setStatus(Status.ERROR);
		}
	}

}
