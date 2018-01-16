package com.lebin.game.module.data;

import com.google.gson.JsonObject;

public class Request extends SerializaableObject{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String from;
	public String to;
	public String what;
	public long msgid;
	public JsonObject data;
	public Request setFrom(String from) {
		this.from = from;
		return this;
	}
	public Request setTo(String to) {
		this.to = to;
		return this;
	}
	public Request setWhat(String what) {
		this.what = what;
		return this;
	}
	public Request setMsgid(long msgid) {
		this.msgid = msgid;
		return this;
	}
	public Request setData(JsonObject data) {
		this.data = data;
		return this;
	}
}
