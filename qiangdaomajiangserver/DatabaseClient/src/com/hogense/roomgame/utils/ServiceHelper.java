package com.hogense.roomgame.utils;

import com.hogense.roomgame.databaseclient.ApplicationContext;
import com.hogense.roomgame.service.interfaces.AccountService;
import com.hogense.roomgame.service.interfaces.DaoService;
import com.hogense.roomgame.service.interfaces.RoomService;

public class ServiceHelper {
	public static AccountService getAccountService()
	{
		return (AccountService) ApplicationContext.getBean("accountService");
	}
	public static RoomService getRoomService()
	{
		return (RoomService) ApplicationContext.getBean("roomService");
	}
	public static DaoService getDaoService()
	{
		return (DaoService) ApplicationContext.getBean("daoService");
	}
}
