package com.hogense.game.server.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.hogense.util.StringUtil;

public class CharacterEncodingFilter implements Filter{
	private String charset;
	@Override
	public void destroy() {
		
	}
	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
			throws IOException, ServletException {
		arg0.setCharacterEncoding(charset);
		arg1.setCharacterEncoding(charset);
		arg2.doFilter(arg0, arg1);
	}
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}
	public void setCharset(String charset) {
		if(StringUtil.isNullOrEmpty(charset))
			charset="utf-8";
		this.charset = charset;
	}

}
