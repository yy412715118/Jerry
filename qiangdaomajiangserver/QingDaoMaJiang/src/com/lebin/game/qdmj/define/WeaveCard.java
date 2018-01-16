package com.lebin.game.qdmj.define;
/**
 * 组合牌
 * @author Gocy 
 *
 */
public class WeaveCard {
	/**
	 * 吃牌 
	 */
	public static final byte TYPE_CHI=0;
	
	/**
	 * 碰牌 
	 */
	public static final byte TYPE_PENG=1;
	
	/**
	 * 支对
	 */
	public static final byte TYPE_ZHIDUI=2;
	
	/**
	 * 明杠牌
	 */
	//public static final byte TYPE_MINGGANG=2;
	
	/**
	 * 加杠牌
	 */
	//public static final byte TYPE_JIAGANG=3;
	
	/**
	 * 暗杠牌
	 */
	//public static final byte TYPE_ANGANG=4;
	
	/**
	 * 东南鸡
	 */
	//public static final byte TYPE_DONG_NAN_JI=5;
	
	/**
	 * 中发白
	 */
	//public static final byte TYPE_ZHONG_FA_BAI=6;
	

	/**
	 * 东南西北
	 */
	//public static final byte TYPE_DONG_NAN_XI_BEI=7;
	
	
	
	
	public byte type;
	/**
	 * 组合牌数据
	 */
	public short card[]=new short[4];
	
	/**
	 * 供应座位号
	 */
	public byte chairid;
	
	/**
	 * 中心扑克
	 */
	public short centercard;
	
	public boolean flag=true;
}
