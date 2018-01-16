package com.lebin.game.qdmj.define;

import java.util.ArrayList;
import java.util.List;

public class CardData {
	/**
	 * 普通牌
	 */
	public final ArrayList<Short> norcard=new ArrayList<Short>();
	
	/**
	 * 出牌数据
	 */
	public final ArrayList<Short> out=new ArrayList<Short>();
	
	/**
	 * 组合牌数据
	 */
	public final List<WeaveCard>weavecard=new ArrayList<>();
	
	/**
	 * 玩家操作
	 */
	public final List<Option> ops=new ArrayList<>();
	
	public Short incard=null;//玩家进牌
	
	public Short winCard=null;//玩家胡牌
	
	public short[] lmtid;//限制出牌编号
	public short lastout=-1;//上次出的牌
	public void reset()
	{
		norcard.clear();
		ops.clear();
		weavecard.clear();
		out.clear();
		incard=null;
		winCard=null;
		lmtid=null;
		lastout=-1;
	}
}
