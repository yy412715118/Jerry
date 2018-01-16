package com.lebin.game.module.data;

public class Define {
	
	
	
	//玩家状态: 3个标识位  投票状态位|掉线状态位|准备状态位
	
	/**
	 * 玩家未准备
	 */
	public final static byte USER_STATE_NOTREADY=0x00;
	
	/**
	 * 玩家准备
	 */
	public final static byte USER_STATE_READY=0x01;
	
	/**
	 * 玩家掉线
	 */
	public final static byte USER_STATE_OFFLINE=0x02;
	
	/**
	 * 玩家同意
	 */
	public final static byte USER_STATE_AGREE=0x04;
	
	
	//玩家游戏状态
	
	/**
	 * 无状态
	 * 
	 */
	public final static byte PLAY_STATE_NONE=0;
	
	
	
	
	public static void main(String[] args) {
		byte state=0;
		state|=USER_STATE_READY;
		state|=USER_STATE_OFFLINE;
		state|=USER_STATE_AGREE;
		//state^=USER_STATE_OFFLINE;
		state^=USER_STATE_AGREE;
		if((state&USER_STATE_AGREE)==USER_STATE_AGREE)
			System.out.println("用户同意");
		if((state&USER_STATE_OFFLINE)==USER_STATE_OFFLINE)
			System.out.println("用户掉线");
		if((state&USER_STATE_READY)==USER_STATE_READY)
			System.out.println("用户已准备");
	}
}
