package com.hogense.game.server.handler;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.hogense.data.RequestData;
import com.hogense.data.ResponseData;
import com.hogense.game.server.Config;
import com.hogense.server.define.Status;
import com.hogense.util.CacheData;
import com.hogense.util.SignUtil;
import com.hogense.util.StringUtil;

import atg.taglib.json.util.JSONException;
import atg.taglib.json.util.JSONObject;

public abstract class ServerHttpServrlet extends BaseServerHttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Map<String, CacheData>CACHE=new ConcurrentHashMap<>();
	private static final Logger LOG = Logger.getLogger(ServerHttpServrlet.class.getName());
	@Override
	protected final void handler(HttpServletRequest request, HttpServletResponse response,ResponseData rsp) {
		
		try {
			ByteArrayOutputStream bout=new ByteArrayOutputStream();
			int len;
			byte[] data=new byte[10240];
			InputStream in=request.getInputStream();
			while((len=in.read(data))!=-1)
				bout.write(data,0,len);
			in.close();
			bout.close();
			String body=new String(bout.toByteArray(),"utf-8");
			System.out.println(body);
			JSONObject json=new JSONObject(body);
			RequestData reqdata=new RequestData(request, json);
			if(!checkToken(request, response,reqdata, rsp))
			{
				rsp.setStatus(Status.TOKEN_TIMEOUT);
				return;
			}
			String gameid=reqdata.getParameter("gameid");
			if(StringUtil.isNullOrEmpty(gameid))
			{
				rsp.setStatus(Status.ARG_ERR);
				return;
			}
			String key="secretkey_"+gameid;
			String secretkey=Config.getProperty(key);
			if(secretkey==null)
			{
				rsp.setStatus(Status.ACCESS_DENY);
				return;
			}
			
			String code=(String)request.getSession().getAttribute("code");
			String sign=request.getHeader("sign");
			if(!vervify(request,body, sign, secretkey,code))
			{
				rsp.setStatus(Status.SIGN_ERR);
			}
			else 
			{
				request.getSession().removeAttribute("code");
				doService(request, response,reqdata,rsp);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rsp.setStatus(Status.ERROR);
		}
	}
	protected boolean vervify(HttpServletRequest request,String body,String sign,String secretkey,String code)
	{
		return SignUtil.vervify(sign,body+"&secretkey="+secretkey+"&code="+code);
	}
	protected boolean checkToken(HttpServletRequest request,HttpServletResponse response,RequestData reqdata,ResponseData rsp)
	{
		return true;
	}
	protected abstract void doService(HttpServletRequest request, HttpServletResponse response,RequestData reqdata,ResponseData rsp) throws JSONException;

}
