package com.lebin.game.qdmj.define;

public class Define extends com.lebin.game.module.data.Define{
	
	//游戏状态
	/*
	 * 等待状态
	 */
	public static final int GAME_STATE_REAY=0;
	
	/*
	 * 掷骰子中
	 */
	public static final int GAME_STATE_DICE=1;
	
	/*
	 * 游戏中
	 */
	public static final int GAME_STATE_PLAY=2;
	


	/*
	 * 游戏回合结束
	 */
	public static final int GAME_STATE_ROUND_OVER=3;
	
	
	/*
	 * 游戏结束状态
	 */
	public static final int GAME_STATE_OVER=4;
	
	//消息类型
	
	/**
	 * 进牌
	 */
	public final static int MSG_IN_CARD=0;
	
	/**
	 * 出牌
	 */
	public final static int MSG_OUT_CARD=1;
	/**
	 * 操作牌
	 */
	public final static int MSG_OP_CARD=2;
	
	/**
	 * 同步场景
	 */
	public final static int MSG_SYN_SCENE=3;
	
	/**
	 * 准备就绪
	 */
	public final static int MSG_READY=4;
	
	/**
	 * 应答
	 */
	public final static int MSG_RESPONSE=5;
	
	/**
	 * 聊天信息
	 */
	public final static int MSG_CHAT=127;
	
	/**
	 * 解散房间
	 */
	public final static int MSG_FREE_ROOM=99;
	
	//动作定义
	

	/**
	 * 下一步
	 */
	
	public final static byte OP_READY=-1;
	
	/**
	 * 无动作
	 */
	public final static byte OP_NONE=0;
	/**
	 * 进牌
	 */
	public final static byte OP_IN_CARD=1;
	/**
	 * 出牌
	 */
	public final static byte OP_OUT_CARD=2;
	/**
	 * 出组合牌
	 */
	public final static byte OP_WEAVECARD=3;
	/**
	 * 选择
	 */
	public final static byte OP_SELECT=4;
	
	
	/**
	 * 投票
	 */
	public final static byte OP_VOTE=5;
	
	
	
	/**
	 * 一局结束
	 */
	public final static byte OP_ROUND_OVER=100;
	
	/**
	 * 游戏结束
	 */
	public final static byte OP_GAME_OVER=101;
	
	
	//胡牌定义
	/*
	 * 非胡牌
	 */
	public final static int HPK_NONE=0;
	/*
	 * 胡牌
	 */
	public final static int HPK_HU=1;
	
	// 大胡
	/**
	 * 门清
	 */
	public final static int HP_MEN_QING=0x00000001;
	
	
	/**
	 * 摸宝胡
	 */
	public final static int HP_MO_BAO_HU=0x00000002;
	
	/**
	 * 海底捞
	 */
	public final static int HP_HAI_DI_LAO=0x00000004;
	
	/**
	 * 海底炮
	 */
	public final static int HP_HAI_DI_PAO=0x00000008;
	
	/**
	 * 大胡
	 */
	public final static int HP_DA_HU=0x00000010;
	
	/**
	 * 大风胡
	 */
	public final static int HP_DA_FENG_HU=0x00000020;
	
	/**
	 * 红中满天飞
	 */
	public final static int HP_HONG_ZHONG_MAN_TIAN_FEI=0x00000040;
	
	/**
	 * 宝中宝
	 */
	public final static int HP_BAO_ZHONG_BAO=0x00000080;
	
	/**
	 * 小胡
	 */
	public final static int HP_XIAOHU=0x00000100;
	
	
	/**
	 * 漏宝
	 */
	public final static int HP_LOU_BAO=0x00100000;
	
	/**
	 * 下大雨
	 */
	public final static int HP_XIA_DA_YU=0x00200000;
	
	/**
	 * 单吊夹
	 */
	public final static int HP_DAN_DIAO_JIA=0x00400000;
	
	/**
	 * 支对
	 */
	public final static int HP_ZHI_DUI=0x00800000;
	
	/**
	 * 宝边
	 */
	public final static int HP_BAO_BIAN=0x01000000;
	
	/**
	 * 三七夹
	 */
	public final static int HP_SAN_QI_JIA=0x02000000;
	
	/**
	 * 扣听
	 */
	public final static int HP_KOUTING=0x04000000;
	
	/**
	 * 自摸
	 */
	public final static int HP_ZIMO=0x10000000;
	
	/**
	 * 吃胡
	 */
	public final static int HP_CHIHU=0x20000000;
	

	/**
	 * 点炮
	 */
	public final static int HP_DIANPAO=0x40000000;
	
	/**
	 * 一炮三响
	 */
	//public final static int HP_YI_PAO_SANXIANG=0x80000000;
	
	
	/**
	 * 听牌
	 * 
	 */
	public final static byte PLAY_STATE_TING=0x01;
	
	/**
	 * 自摸
	 * 
	 */
	public final static byte PLAY_STATE_ZIMO=0x02;
	
	/**
	 * 吃胡
	 * 
	 */
	public final static byte PLAY_STATE_CHIHU=0x04;
	
	/**
	 * 点炮
	 * 
	 */
	public final static int PLAY_STATE_DIANPAO=0x08;
	
	/**
	 * 支对
	 * 
	 */
	public final static int PLAY_STATE_ZHIDUI=0x12;
	
	
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
