package com.hogense.game.server.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hogense.data.ResponseData;
import com.hogense.game.server.annotation.Autowired;
import com.hogense.game.server.annotation.HttpService;
import com.hogense.game.server.sevice.UserService;
import com.hogense.server.define.Status;
import com.hogense.util.StringUtil;
@HttpService("/api/server/accountlock")
public class AccountLockServlet extends BaseServerHttpServlet{
	@Autowired
	UserService service;
	@Override
	protected void handler(HttpServletRequest request, HttpServletResponse response, ResponseData rsp) {
		try {
			Integer gameid=StringUtil.toInt(request.getParameter("gameid"));
			Long uid=StringUtil.toLong(request.getParameter("uid"));
			boolean lock=Boolean.parseBoolean(request.getParameter("lock"));
			boolean rs=service.changeUserLockState(gameid, uid, lock);
			if(rs)
				rsp.setStatus(Status.OK);
			else 
				rsp.setStatus(Status.ERROR);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
	}

}
