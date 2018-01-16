package com.hogense.util;

import java.net.SocketTimeoutException;

import com.hogense.data.ResponseData;
import com.hogense.game.server.Config;
import com.hogense.server.define.Status;

import atg.taglib.json.util.JSONObject;

public class WeiXinUtil {
	
	public static JSONObject getUserInfo(String token)
	{
		ResponseData rsp=new ResponseData();
		try {
			String url=Config.getProperty("test_weixin_login_url","http://127.0.0.1/weixin/get");
			String data=HttpRequest.get(url+"?token="+token);
			if(data==null)
			{
				rsp.setStatus(Status.GET_USERINFO_FAIL);
			}
			else 
			{
				JSONObject js= new JSONObject(data);
				if(js.getInt("status")==Status.OK.code)
				{
					rsp.setStatus(Status.OK);
					rsp.setData(js.getJSONObject("data"));
				}
				else 
					rsp.setStatus(Status.GET_USERINFO_FAIL);
			}
		
		} catch (SocketTimeoutException e) {
			rsp.setStatus(Status.TIMEOUT);
		} catch (Exception e) {
			rsp.setStatus(Status.ERROR);
			e.printStackTrace();
		}
		return rsp.toJson();
	}
}
