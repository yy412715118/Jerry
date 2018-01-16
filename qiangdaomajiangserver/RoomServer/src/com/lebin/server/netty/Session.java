package com.lebin.server.netty;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lebin.game.module.ISession;
import com.lebin.game.module.ISessionListener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class Session implements ISession{
	private static final Logger LOG = LoggerFactory.getLogger(Session.class.getName());
	public final String sessionid;
	private final Map<Object, Object>map=Collections.synchronizedMap(new HashMap<Object,Object>());
	private final ChannelHandlerContext ctx;
	private boolean closed=false;
	private final Object lock=new Object();
	private Set<ISessionListener> listeners=Collections.synchronizedSet(new HashSet<>()) ;
	public Session(ChannelHandlerContext ctx) {
		this.sessionid=UUID.randomUUID().toString();
		this.ctx=ctx;
		
	}
	@Override
	public String getId() {
		return sessionid;
	}
	public void setAttribute(Object key,Object value)
	{
		map.put(key, value);
	}
	public Object getAttribute(Object key)
	{
		return map.get(key);
	}
	public Object removeAttribute(Object key)
	{
		return map.remove(key);
	}
	public void removeAllAttribute()
	{
		map.clear();
	}
	public void close()
	{
		synchronized (lock) {
			if(closed)
				return;
			closed=true;
		}
		try {
			ctx.close();
		} catch (Exception e) {
		}
		synchronized (listeners) {
			for(ISessionListener listener:listeners)
				listener.onClosed(this);
			listeners.clear();
		}
	
	
	}
	public boolean isClosed()
	{
		return closed;
	}
	public void send(Object obj) throws Exception {
		send(obj.toString());
	}
	public void send(String data) throws Exception {
		if (this.ctx == null || this.ctx.isRemoved()) {
			throw new Exception("尚未握手成功，无法向客户端发送WebSocket消息");
		}
		this.ctx.channel().write(new TextWebSocketFrame(data));
		this.ctx.flush();
		LOG.debug(data);
	}
	public ChannelHandlerContext getChannelHandlerContext() {
		return ctx;
	}
	@Override
	public void addListener(ISessionListener listener) {
		listeners.add(listener);
		
	}
	@Override
	public void removeListener(ISessionListener listener) {
		listeners.remove(listener);
		
	}
}
