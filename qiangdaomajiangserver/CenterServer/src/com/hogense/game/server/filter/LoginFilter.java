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

public class LoginFilter implements Filter{
	Set<String>allow=new HashSet<>();
	String filter;
	String forward;
	public void destroy() {
		
	}
	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
			throws IOException, ServletException {
		if(filter==null)
		{
			arg2.doFilter(arg0, arg1);
			return;
		}
		HttpServletRequest request=(HttpServletRequest)arg0;
		String uri=request.getRequestURI();
		if(uri.matches(filter))
		{
			if(!isAllow(uri)&&request.getSession().getAttribute("user")==null)
			{
				if(this.forward!=null&&!this.forward.isEmpty())
					((HttpServletResponse)arg1).sendRedirect(this.forward);
				else 
					((HttpServletResponse)arg1).sendError(HttpServletResponse.SC_FORBIDDEN);
			}
			else
				arg2.doFilter(arg0, arg1);
		}
		else 
			arg2.doFilter(arg0, arg1);
	}
	private boolean isAllow(String uri)
	{
		for(String i:allow)
		{
			if(uri.matches(i))
				return true;
		}
		return false;
	}
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}
	public void setAllow(String...allows)
	{
		if(allows!=null)
		{
			for(String ip:allows)
			{
				ip=ip.trim();
				if(ip.length()>0)
					allow.add(ip);
			}
		}
	}
	public void setForward(String forward) {
		this.forward = forward;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
	public static void main(String[] args) {
		System.out.println("/admin/1.htmlx".matches("/admin/.*\\.html$"));
	}
}
