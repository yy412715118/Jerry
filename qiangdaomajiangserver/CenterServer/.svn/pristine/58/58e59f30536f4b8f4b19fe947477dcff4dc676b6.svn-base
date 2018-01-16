package com.hogense.game.server.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hogense.data.ResponseData;
import com.hogense.game.server.annotation.HttpService;
import com.hogense.game.server.sevice.RoomServerManager;
import com.hogense.server.define.ServerInfo;
import com.hogense.server.define.Status;
import com.hogense.util.StringUtil;
@HttpService("/api/server/reg")
public class ServerRegServlet extends BaseServerHttpServlet{

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
		if(StringUtil.isNullOrEmpty(host,port,serverid,gameid))
			rsp.setStatus(Status.ERROR);
		else 
		{
			ServerInfo info=new ServerInfo(serverid, host, port);
			if(RoomServerManager.getManager().add(info))
			{
				rsp.setStatus(Status.OK);
			}
			else rsp.setStatus(Status.SERVER_REG_FAIL).setMsg("服务id已存在");
		}
	}
}
