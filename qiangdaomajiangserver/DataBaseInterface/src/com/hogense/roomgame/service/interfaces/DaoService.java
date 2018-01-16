package com.hogense.roomgame.service.interfaces;

public interface DaoService {
	public String callProfun(String fun,Object...args);
	public String queryUnique(String sql,Object...args);
	public String queryList(String sql,Object...args);
}
