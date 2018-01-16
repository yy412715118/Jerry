package com.hogense.game.server.sevice;

import com.hogense.data.ResponseData;
import com.hogense.roomgame.utils.ServiceHelper;
import com.hogense.server.define.Status;

import atg.taglib.json.util.JSONObject;

public class SignService {
	public void getSignInfo(long uid,int gameid,ResponseData rsp)
	{
		try {
			String str=ServiceHelper.getDaoService().callProfun("db_reward.pro_signinfo(?,?)", uid,gameid);
			if(str==null)
				rsp.setStatus(Status.ERROR);
			else 
			{	JSONObject rs=new JSONObject(str);
				int status=rs.getInt("status");
				String msg=rs.getString("msg");
				rs.remove("status");
				rs.remove("msg");
				rsp.getStatus().msg=msg;
				rsp.getStatus().code=status;
				rsp.setData(rs);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			rsp.setStatus(Status.ERROR);
		}
	}
	public void sign(long uid,int gameid,String token,String ip,ResponseData rsp)
	{
		try {
			String str=ServiceHelper.getDaoService().callProfun("db_reward.pro_sign(?,?,?,?)", token,uid,gameid,ip);
			if(str==null)
				rsp.setStatus(Status.ERROR);
			else 
			{	JSONObject rs=new JSONObject(str);
				int status=rs.getInt("status");
				String msg=rs.getString("msg");
				rs.remove("status");
				rs.remove("msg");
				rsp.getStatus().msg=msg;
				rsp.getStatus().code=status;
				rsp.setData(rs);
				return;
			}
		} catch (Exception e) {
			rsp.setStatus(Status.ERROR);
			e.printStackTrace();
		}
	}
}
