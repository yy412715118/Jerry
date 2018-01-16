package com.lebin.server.netty.server.services;

import com.hogense.roomgame.service.interfaces.AccountService;
import com.hogense.roomgame.utils.ServiceHelper;
import com.lebin.game.module.data.Player;
import com.lebin.server.util.ImpClassMapping;

public class UserService {
	public static Player login(long uid,String token,int gameid,long reqtime)
	{
		AccountService ac= ServiceHelper.getAccountService();
		try {
			String data=ac.loginByUid(gameid, uid, token, reqtime);
			if(data==null)
				return null;
			return (Player) Player.create(data, ImpClassMapping.get(Player.class));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}
}
