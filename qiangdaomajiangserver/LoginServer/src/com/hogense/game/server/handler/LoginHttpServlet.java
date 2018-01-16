package com.hogense.game.server.handler;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.hogense.data.RequestData;
import com.hogense.data.ResponseData;
import com.hogense.game.server.Config;
import com.hogense.game.server.annotation.Autowired;
import com.hogense.game.server.annotation.HttpService;
import com.hogense.game.server.sevice.UserService;
import com.hogense.server.define.Status;
import com.hogense.util.GuestInfo;
import com.hogense.util.IpUtils;
import com.hogense.util.LoginType;
import com.hogense.util.StringUtil;
import com.hogense.util.WeiXinUtil;

import atg.taglib.json.util.JSONException;
import atg.taglib.json.util.JSONObject;

@HttpService("/api/login")
public class LoginHttpServlet extends ServerHttpServrlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(LoginHttpServlet.class.getName());
	@Autowired
	UserService service;
	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response,RequestData reqdata,ResponseData rspdata) throws JSONException {
		
		String token=reqdata.getParameter("token");
		String gameid=reqdata.getParameter("gameid");
		String deviceid=reqdata.getParameter("deviceid");
		System.out.println(reqdata.getParameter("type"))
;		Integer type=reqdata.getParameterInt("type");
		try {
			JSONObject rsp;
			if(type==LoginType.WEIXIN.code)
				rsp=WeiXinUtil.getUserInfo(token);
			else if(type==LoginType.GUEST.code&&Config.getInt("allow_guest", 0)==1)
			{
				rsp=guestLogin(token);
			}
			else if(type==LoginType.ACCOUNT.code)
			{
				rsp=accountLogin(reqdata.getParameter("account"), reqdata.getParameter("psw"));
			}
			else
			{
				rspdata.setStatus(Status.LOGIN_NOTSUPPORT);
				return;
			}
			LOG.info("rec "+rsp);
			if(rsp.getInt("status")==Status.OK.code)
			{
				JSONObject user=service.login(IpUtils.getIpAddr(request),gameid,deviceid, rsp.getJSONObject("data"),type);
				LOG.debug(user);
				if(user==null||user.getInt("status")!=200)
					rspdata.setStatus(Status.GET_USERINFO_FAIL);
				else 
				{
					//request.getSession().setAttribute("accesstoken", user.get("accesstoken"));
					JSONObject rs=new JSONObject();
					user.remove("status");
					user.remove("msg");
					rs.put("user", user);
					rs.put("guestid", token);
					rs.put("time", System.currentTimeMillis());
					rs.put("server", Config.getProperty("centerserver_host", "http://127.0.0.1"));
					rspdata.setStatus(Status.OK).setData(rs);
				}
			}
			else 
			{
				rspdata.setStatus(Status.ACCESS_DENY).setMsg(rsp.getString("msg"));;
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			rspdata.setStatus(Status.ERROR);
			
		}
	}
	static List<String>GUEST_TOKENS;
	static int m_index=0;
	static final Object LOCK=new Object();
	private String newGuestToken()
	{
		synchronized (LOCK) {
			if(GUEST_TOKENS==null)
			{
				GUEST_TOKENS=new ArrayList<>();
				String tkstr=Config.getProperty("guesttokens");
				if(tkstr!=null)
				{
					String tks[]=tkstr.split(",");
					for(String i:tks)
					{
						i=i.trim();
						if(i.isEmpty())
							continue;
						GUEST_TOKENS.add(i);
					}
				}
			}
			if(m_index>=GUEST_TOKENS.size())
				m_index=0;
			if(m_index<GUEST_TOKENS.size())
				return GUEST_TOKENS.get(m_index++);
			return null;
		}
		
	}
	private JSONObject guestLogin(String token)
	{
		JSONObject rs=new JSONObject();
		try {
			if(StringUtil.isNullOrEmpty(token))
			{
				rs.put("status", Status.ACCESS_DENY.code);
				rs.put("msg", Status.ACCESS_DENY.msg);
			}
			else 
			{
				rs.put("status", 200);
				rs.put("msg", "成功");
				JSONObject data=new JSONObject();
				String openid="hg_gst_"+token;
				data=service.getGuestInfo(openid);
				if(data==null)
				{
					data=new JSONObject();
					GuestInfo guestInfo=GuestInfo.newGuestInfo();
					data.put("sex", guestInfo.sex);
					data.put("head", guestInfo.head);
					data.put("nickname", guestInfo.nickname);
				}
				data.put("openid",openid);
				rs.put("data", data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}
	private JSONObject accountLogin(String account,String psw)
	{
		JSONObject rs=new JSONObject();;
		try {
			JSONObject data=service.getAccountInfo(account, psw);
			if(data==null)
			{
				rs=new JSONObject();
				rs.put("status", Status.ACCESS_DENY.code);
				rs.put("msg", "密码不正确或账号不存在！");
				return rs;
			}
			rs.put("status", Status.OK.code);
			rs.put("msg", "成功");
			rs.put("data", data);
		} catch (Exception e) {
			rs=new JSONObject();
			try {
				rs.put("status", Status.ACCESS_DENY.code);
				rs.put("msg","登录失败，请重试！");
			} catch (Exception e2) {
				// TODO: handle exception
			}
			
		}
		return rs;
	}
	
	/*@Override
	protected boolean vervify(HttpServletRequest request, String sign, String secretkey, String code) {
		return SignUtil.vervify(sign,SignUtil.orderByKeyAndJoin(getParameterMap(request),"sign"),"&secretkey="+secretkey);
	}*/
	
}
