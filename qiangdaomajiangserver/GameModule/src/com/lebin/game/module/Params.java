package com.lebin.game.module;

public interface Params {
	public Object get(String key);
	public String getString(String key);
	public Integer getInt(String key);
	public int getInt(String key,int df);
	public Long getLong(String key);
	public long getLong(String key,long df);
	public Float getFloat(String key);
	public float getFloat(String key,float df);
	public Short getShort(String key);
	public short getShort(String key,short df);
	public Double getDouble(String key);
	public double getDouble(String key,double df);
}
