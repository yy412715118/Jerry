package com.hogense.game.server.sevice;

import com.hogense.roomgame.utils.ServiceHelper;

public class UserService {
	public boolean changeUserLockState(int gameid,long uid,boolean lock)
	{
		try {
		
			return ServiceHelper.getAccountService().changeUserLockState(gameid, uid, lock);
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
