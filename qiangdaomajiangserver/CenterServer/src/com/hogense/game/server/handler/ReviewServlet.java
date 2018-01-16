package com.hogense.game.server.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hogense.data.RequestData;
import com.hogense.data.ResponseData;
import com.hogense.game.server.annotation.Autowired;
import com.hogense.game.server.annotation.HttpService;
import com.hogense.game.server.sevice.HistoryService;
import com.hogense.server.define.Status;

import atg.taglib.json.util.JSONArray;
import atg.taglib.json.util.JSONObject;
@HttpService("/api/review/list")
public class ReviewServlet extends BaseServerHttpServlet{
	@Autowired
	HistoryService service;

	@Override
	protected void handler(HttpServletRequest request, HttpServletResponse response, ResponseData rsp) {
		String serviceid=request.getParameter("serviceid");
		JSONArray array=service.getReviewData(serviceid);
		if(array==null||array.size()==0)
			rsp.setStatus(Status.ERROR);
		else 
		{
			try {
				JSONObject data=new JSONObject();
				data.put("list", array);
				rsp.setStatus(Status.OK).setData(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
	

	
}
