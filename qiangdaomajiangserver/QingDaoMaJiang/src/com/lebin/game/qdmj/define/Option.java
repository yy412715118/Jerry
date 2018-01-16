package com.lebin.game.qdmj.define;

public class Option extends SerializaableObject{
	public byte op;
	public OpItem item[];
	public static final byte OP_NONE=0;
	public static final byte OP_HU=1;
	public static final byte OP_GANG=2;
	public static final byte OP_PENG=3;
	public static final byte OP_TING=4;
	public static final byte OP_CHI=5;
	public static final byte OP_ZHIDUI=6;
	//public static final byte OP_NIU=6;//扭牌
	//public static final byte OP_TIAO=7;//调牌
}
