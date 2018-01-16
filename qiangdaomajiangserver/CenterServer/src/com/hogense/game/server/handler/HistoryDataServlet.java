package com.hogense.game.server.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hogense.data.RequestData;
import com.hogense.data.ResponseData;
import com.hogense.game.server.annotation.Autowired;
import com.hogense.game.server.annotation.HttpService;
import com.hogense.game.server.sevice.HistoryService;
import com.hogense.game.server.sevice.SignService;
import com.hogense.server.define.Status;
import com.hogense.util.IpUtils;

import atg.taglib.json.util.JSONObject;
@HttpService("/api/history")
public class HistoryDataServlet extends CheckTokenServlet	{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Autowired
	HistoryService service;
	@Override
	protected void handler(long uid, String gameid, String token, HttpServletRequest request,
			HttpServletResponse response, RequestData reqData, ResponseData rsp) {
		Integer op=reqData.getParameterInt("op");
		if(op==null||op!=1)//查询
		{
			int i_gameid= Integer.parseInt(gameid);
			JSONObject rs=service.getWeekData(uid, i_gameid);
			if(rs==null)
				rsp.setStatus(Status.ERROR);
			else
			{
				JSONObject data=new JSONObject();
				try {
					data.put("weekdata", rs);
					data.put("record", service.getRecordData(uid,i_gameid));
				} catch (Exception e) {
					e.printStackTrace();
				}
				rsp.setStatus(Status.OK).setData(data);
			}
		}
		else //领取
		{
			
		}
		
	}

}
