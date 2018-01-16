package com.lebin.game.module.data;

import com.google.gson.JsonObject;

public class Message extends SerializaableObject{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final byte type=0;
	public String from;
	public String what;
	public long msgid;
	public Object data;
	public Message setFrom(String from) {
		this.from = from;
		return this;
	}
	public Message setWhat(String what) {
		this.what = what;
		return this;
	}
	public Message setMsgid(long msgid) {
		this.msgid = msgid;
		return this;
	}
	public Message setData(JsonObject data) {
		this.data = data;
		return this;
	}

}
