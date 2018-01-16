package com.hogense.game.server.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hogense.data.RequestData;
import com.hogense.data.ResponseData;
import com.hogense.game.server.annotation.Autowired;
import com.hogense.game.server.sevice.UserService;

public abstract class CheckTokenServlet extends ServerHttpServrlet{
 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Autowired
	UserService service;
	@Override
	protected boolean checkToken(HttpServletRequest request, HttpServletResponse response,RequestData reqData, ResponseData rsp) {
		
		return true;
	}
	@Override
	protected final void doService(HttpServletRequest request, HttpServletResponse response,RequestData reqData, ResponseData rsp) {
		Long uid=reqData.getParameterLong("uid");
		String gameid=reqData.getParameter("gameid");
		String token=reqData.getParameter("token");
		handler(uid, gameid, token, request, response, reqData,rsp);
		
	}
	protected abstract void handler(long uid,String gameid,String token,HttpServletRequest request,HttpServletResponse response,RequestData reqData,ResponseData rsp);
}
