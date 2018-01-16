package com.hogense.server.define;


import java.util.Map;

import com.google.gson.JsonObject;
import com.lebin.game.module.data.SerializaableObject;

public class Response extends SerializaableObject{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final byte type=1;
	public String from;
	public String to;
	public String what;
	public long msgid;
	public int status;
	public String msg;
	public Object data;
	
	public Response setFrom(String from) {
		this.from = from;
		return this;
	}
	public Response setTo(String to) {
		this.to = to;
		return this;
	}
	public Response setWhat(String what) {
		this.what = what;
		return this;
	}
	public Response setMsgid(long msgid) {
		this.msgid = msgid;
		return this;
	}
	public Response setData(Object data) {
		this.data = data;
		return this;
	}
	public Response setMsg(String msg) {
		this.msg = msg;
		return this;
	}
	public Response setStatus(int status) {
		this.status = status;
		return this;
	}
	public Response setStatus(Status status)
	{
		this.status=status.code;
		this.msg=status.msg;
		return this;
	}
	
}
