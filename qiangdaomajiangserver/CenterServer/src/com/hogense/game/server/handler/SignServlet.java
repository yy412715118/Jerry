package com.hogense.game.server.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hogense.data.RequestData;
import com.hogense.data.ResponseData;
import com.hogense.game.server.annotation.Autowired;
import com.hogense.game.server.annotation.HttpService;
import com.hogense.game.server.sevice.SignService;
import com.hogense.util.IpUtils;
@HttpService("/api/sign")
public class SignServlet extends CheckTokenServlet	{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Autowired
	SignService sevice;
	@Override
	protected void handler(long uid, String gameid, String token, HttpServletRequest request,
			HttpServletResponse response, RequestData reqData, ResponseData rsp) {
		Integer op=reqData.getParameterInt("op");
		if(op==null||op!=1)//查询
		{
			sevice.getSignInfo(uid, Integer.parseInt(gameid), rsp);;
		}
		else //领取
		{
			sevice.sign(uid, Integer.parseInt(gameid), token,IpUtils.getIpAddr(request),rsp);
		}
		
	}

}
