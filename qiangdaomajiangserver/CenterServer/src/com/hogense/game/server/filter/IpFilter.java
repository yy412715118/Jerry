package com.hogense.game.server.filter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.hogense.util.IpUtils;

public class IpFilter implements Filter{
	private static final Logger LOG = Logger.getLogger(IpFilter.class.getName());
	Set<String>ipDeny=new HashSet<>();
	Set<String>ipAllow=new HashSet<>();
	@Override
	public void destroy() {
		
		
	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
			throws IOException, ServletException {
		//LOG.debug(((HttpServletRequest)arg0).getRequestURI());
		if(ipDeny.isEmpty())
		{
			arg2.doFilter(arg0, arg1);
			return;
		}
		String ip=IpUtils.getIpAddr((HttpServletRequest)arg0);
		boolean allow=false;
		if(ipDeny.contains("*"))//拒绝所有
		{
			if(ipAllow.contains(ip))
				allow=true;
		}
		else if(!ipDeny.contains(ip))//不在拒绝名单
			allow=true;
		if(allow)
		{
			arg2.doFilter(arg0, arg1);
		//	LOG.info("允许ip："+ip+"访问");
		}
		else
		{
			((HttpServletResponse)arg1).sendError(HttpServletResponse.SC_FORBIDDEN);
		//	LOG.info("拒绝ip："+ip+"访问");
		}
	}
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}
	public void setDeny(String...denys)
	{
		if(denys!=null)
		{
			for(String ip:denys)
			{
				ip=ip.trim();
				if(ip.length()>0)
					ipDeny.add(ip);
			}
		}
	}
	public void setAllow(String...allows)
	{
		if(allows!=null)
		{
			for(String ip:allows)
			{
				ip=ip.trim();
				if(ip.length()>0)
					ipAllow.add(ip);
			}
		}
	}
}
