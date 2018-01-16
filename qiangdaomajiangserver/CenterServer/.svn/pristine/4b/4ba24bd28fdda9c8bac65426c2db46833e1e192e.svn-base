package com.hogense.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.hogense.server.define.Status;

import atg.taglib.json.util.JSONObject;

public class ResponseData {
	private Status status=Status.ERROR;
	private JSONObject data;
	private Map<String, Object>extras=new HashMap<>();
	public ResponseData setStatus(Status status) {
		this.status=status;
		return this;
	}
	public ResponseData setData(JSONObject data) {
		this.data = data;
		return this;
	}
	public Status getStatus() {
		return status;
	}
	public JSONObject getData() {
		return data;
	}
	public ResponseData setExtraData(String key,Object data)
	{
		extras.put(key, data);
		return this;
	}
	public Object getExtraData(String key)
	{
		return extras.get(key);
	}
	public Object removeExtraData(String key)
	{
		return extras.remove(key);
	}
	public Map<String, Object> getExtras() {
		return extras;
	}
	public void clearExtraData()
	{
		extras.clear();
	}
	public void reset()
	{
		status=Status.ERROR;
		data=null;
		extras.clear();
	}
	public JSONObject toJson()
	{
		JSONObject rs=new JSONObject();
		try {
			for(Entry<String, Object>en:extras.entrySet())
				rs.put(en.getKey(), en.getValue());
			rs.put("status", status.code);
			rs.put("msg", status.msg);
			rs.put("data", data);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return rs;
	}
	public ResponseData setStatus(int code)
	{
		status.code=code;
		return this;
	}
	public ResponseData setMsg(String msg)
	{
		status.msg=msg;
		return this;
	}
	
	
}
