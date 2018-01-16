package com.lebin.game.module;

public interface ISession {
	public String getId();
	public void setAttribute(Object key,Object value);
	public Object getAttribute(Object key);
	public Object removeAttribute(Object key);
	public void removeAllAttribute();
	public void close();
	public boolean isClosed();
	public void send(Object obj) throws Exception ;
	public void addListener(ISessionListener listener);
	public void removeListener(ISessionListener listener);
}
