package com.lebin.game.qdmj.server;

import com.lebin.game.module.IRoom;
import com.lebin.game.module.data.Player;
import com.lebin.game.qdmj.Room;
import com.lebin.server.netty.server.RoomServer;
import com.lebin.server.util.ConfigInfo;
import com.lebin.server.util.ImpClassMapping;

public class Lanucher {
	public static void main(String[] args) throws Exception {
	
		ConfigInfo configInfo=new ConfigInfo();
		ImpClassMapping.regist(IRoom.class,Room.class);
		ImpClassMapping.regist(Player.class,Player.class);
		new RoomServer().run(configInfo); 
	}
}
