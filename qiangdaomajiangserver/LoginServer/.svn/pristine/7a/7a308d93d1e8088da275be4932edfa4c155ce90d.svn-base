package com.hogense.game.server.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hogense.data.ResponseData;
import com.hogense.game.server.annotation.HttpService;
import com.hogense.server.define.Status;
import com.hogense.util.UID;

@HttpService("/api/syncode")
public class CodeServlet extends BaseServerHttpServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Override
	protected void handler(HttpServletRequest request, HttpServletResponse response,ResponseData rsp) {
		try {
			String code=UID.newCode();
			request.getSession().setAttribute("code", code);
			rsp.setStatus(Status.OK).setExtraData("code", code);
		} catch (Exception e) {
			e.printStackTrace();
			rsp.setStatus(Status.ERROR);
		}
	}
}
