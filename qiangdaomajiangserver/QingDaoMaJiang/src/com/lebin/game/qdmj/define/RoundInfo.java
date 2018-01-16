package com.lebin.game.qdmj.define;

public class RoundInfo extends SerializaableObject{
	/**
	 * 
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public byte dianshu1;
	public byte dianshu2;
	public byte zhuangjiachairid=-1;
	public byte startchairid;
	public byte startpos;//起牌位置
	public short frontremove;//牌首移除数量
	public short tailremove;//尾牌移除数量
	public short leftcount;//剩余牌数量
	public byte curchairid=-1;//当前操作玩家座位号
	public short curoutcard=-1;
	public int lefttime=0;//剩余时间
	public byte curaction=Action.ACT_NONE;
	public byte curround=0;
	public byte curcircle=0;
	public short difen=1;
	public byte lianzhuang=0;//连庄
	public byte huangzhuang=0;//荒庄
	public String des;
}
