package com.hogense.game.server.handler;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.hogense.data.RequestData;
import com.hogense.data.ResponseData;
import com.hogense.game.server.Config;
import com.hogense.server.define.Status;
import com.hogense.util.SignUtil;
import com.hogense.util.StringUtil;

import atg.taglib.json.util.JSONObject;

public abstract class ServerHttpServrlet extends BaseServerHttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(ServerHttpServrlet.class.getName());
	/*@Override
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
			JSONObject json=new JSONObject(body);
			RequestData reqdata=new RequestData(request, json);
			LOG.debug(json);
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
			String secretkey=null;
			CacheData cacheData=CACHE.get(key);
			if(cacheData!=null)
			{
				if(cacheData.expiretime<System.currentTimeMillis())
					CACHE.remove(key);
				else 
					secretkey=(String)cacheData.data;
			}
			if(secretkey==null)
			{
				LOG.debug("get secretkey from database");
				secretkey=(String)dao.findUniqueBySql("select secretkey from db_game.t_game where gameid=?", gameid);
				if(secretkey!=null)
				{
					cacheData=new CacheData();
					cacheData.data=secretkey;
					cacheData.expiretime=System.currentTimeMillis()+600000;//十分钟过期
					CACHE.put(key, cacheData);
				}
				else
				{
					rsp.setStatus(Status.ACCESS_DENY);
					return;
				}
			}
			String sign=request.getHeader("sign");
			Object[] code1=(Object[])request.getSession().getAttribute("code_1");
			if(code1==null)
				code1=new Object[]{secretkey,0};
			Object[] code2=(Object[])request.getSession().getAttribute("code_2");
			String newcode=null;
			boolean pass=false;
			if(code2!=null)//优先使用最新code
			{
				if(vervify(request,body, sign, secretkey,(String)code2[0]))//最新验证通过
				{
					code1[0]=code2[0];
					code1[1]=1;
					newcode=UID.newCode();
					code2[0]=newcode;
					code2[1]=0;
					pass=true;
					LOG.debug("code2 验证通过，次数"+1);
				}
				else 
				{
					if(vervify(request,body, sign, secretkey,(String)code1[0]))//旧code验证通过
					{
						int c=(Integer)code1[1];
						if(c<3)
						{
							pass=true;
							code1[1]=c+1;
							newcode=(String)code2[0];
							LOG.debug("code1 验证通过，次数"+code1[1]);
						}
					}
				}
			}
			else 
			{
				newcode=UID.newCode();
				code2=new Object[]{newcode,0};
				if(vervify(request, body,sign,secretkey,(String)code1[0]))//旧code验证通过
				{
					int c=(Integer)code1[1];
					if(c<3)
					{
						pass=true;
						code1[1]=c+1;
						LOG.debug("第一次code1 验证通过，次数"+code1[1]);
					}
				}
			}
			if(!pass)
			{
				rsp.setStatus(Status.SIGN_ERR);
			}
			else 
				doService(request, response,reqdata,rsp);
			request.getSession().setAttribute("code_1", code1);
			request.getSession().setAttribute("code_2", code2);
			rsp.setExtraData("code", newcode);
		} catch (Exception e) {
			e.printStackTrace();
			rsp.setStatus(Status.ERROR);
		}
	}*/
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
			JSONObject json=new JSONObject(body);
			RequestData reqdata=new RequestData(request, json);
			LOG.info(json);
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
			String sign=request.getHeader("sign");
			boolean pass=vervify(request, body, sign, secretkey, null);
			if(!pass)
			{
				rsp.setStatus(Status.SIGN_ERR);
			}
			else 
				doService(request, response,reqdata,rsp);
		} catch (Exception e) {
			e.printStackTrace();
			rsp.setStatus(Status.ERROR);
		}
	}
	@Override
	protected void onResponse(Object data, HttpServletResponse response) {
		LOG.info("response:"+data);
	}
	protected boolean vervify(HttpServletRequest request,String body,String sign,String secretkey,String code)
	{
		if(code==null)
			return SignUtil.vervify(sign,body+"&secretkey="+secretkey);
		return SignUtil.vervify(sign,body+"&secretkey="+secretkey+"&code="+code);
	}
	protected boolean checkToken(HttpServletRequest request,HttpServletResponse response,RequestData reqData,ResponseData rsp)
	{
		return true;
	}
	protected abstract void doService(HttpServletRequest request, HttpServletResponse response,RequestData reqData, ResponseData rsp);

}
