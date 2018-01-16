package com.hogense.data;

import javax.servlet.http.HttpServletRequest;

import atg.taglib.json.util.JSONException;
import atg.taglib.json.util.JSONObject;

public class RequestData {
	private HttpServletRequest request;
	private Object data;
	public RequestData(HttpServletRequest request,Object data) {
		this.request=request;
		this.data=data;
	}
	public Object get(String key)
	{
		if(data==null)
			return request.getParameter(key);
		if(data instanceof JSONObject)
		{
			JSONObject js=(JSONObject)data;
			if(js.has(key))
				try {
					return js.get(key);
				} catch (JSONException e) {
					e.printStackTrace();
				}
		}
		return request.getParameter(key);
	}
	public String getParameter(String key)
	{
		if(data==null)
			return request.getParameter(key);
		if(data instanceof JSONObject)
		{
			JSONObject js=(JSONObject)data;
			if(js.has(key))
				try {
					return js.getString(key);
				} catch (JSONException e) {
					e.printStackTrace();
				}
		}
		return request.getParameter(key);
	}
	public Integer getParameterInt(String key)
	{
		String obj=getParameter(key);
		if(obj==null)
			return null;
		return Integer.parseInt(obj);
	}
	public Long getParameterLong(String key)
	{
		String obj=getParameter(key);
		if(obj==null)
			return null;
		return Long.parseLong(obj);
	}
	public Boolean getParameterBoolean(String key)
	{
		String obj=getParameter(key);
		if(obj==null)
			return null;
		return Boolean.parseBoolean(obj);
	}
	public Short getParameterShort(String key)
	{
		String obj=getParameter(key);
		if(obj==null)
			return null;
		return Short.parseShort(obj);
	}
	public Double getParameterDouble(String key)
	{
		String obj=getParameter(key);
		if(obj==null)
			return null;
		return Double.parseDouble(obj);
	}
	public Float getParameterFloat(String key)
	{
		String obj=getParameter(key);
		if(obj==null)
			return null;
		return Float.parseFloat(obj);
	}
	
	public Object getData() {
		return data;
	}
}
