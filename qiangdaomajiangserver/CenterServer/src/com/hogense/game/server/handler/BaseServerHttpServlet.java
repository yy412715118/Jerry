package com.hogense.game.server.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.hogense.data.ResponseData;

import atg.taglib.json.util.JSONObject;

public abstract class BaseServerHttpServlet  extends HttpServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(BaseServerHttpServlet.class.getName());
	@Override
	protected final void service(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
		arg1.setHeader("Access-Control-Allow-Origin", "*");
		Integer vInteger=(Integer)arg0.getSession().getAttribute("x");
		if(vInteger==null)
			vInteger=0;
		//System.out.println(vInteger);
		vInteger++;
		ResponseData responseData=new ResponseData();
		handler(arg0,arg1,responseData);
		JSONObject jsonObject=responseData.toJson();
		
		arg0.getSession().setAttribute("x", vInteger);
		arg1.getWriter().print(jsonObject);
		onResponse(jsonObject, arg1);
		
	}
	protected void onResponse(Object data,HttpServletResponse response)
	{
		
	}
	@Override
	public final void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.service(req, res);
	}
	@Override
	protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doGet(req, resp);
	}
	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doPost(req, resp);
	}
	@Override
	protected final void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doPut(req, resp);
	}
	protected abstract void handler(HttpServletRequest request,HttpServletResponse response,ResponseData rsp);
	public Map<String, String>getParameterMap(HttpServletRequest request)
	{
		Map<String, String[]>vs=request.getParameterMap();
		Map<String, String>rs=new HashMap<>();
		for(Entry<String, String[]>en:vs.entrySet())
			rs.put(en.getKey(), en.getValue()[0]);
		return rs;
	}
}
