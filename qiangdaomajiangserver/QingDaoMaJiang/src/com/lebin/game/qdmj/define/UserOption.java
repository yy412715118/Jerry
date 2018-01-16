package com.lebin.game.qdmj.define;

public class UserOption {
	public final byte chairid;
	public final byte op;
	public final OpItem item;
	public UserOption(byte chairid,byte op,OpItem item) {
		this.chairid=chairid;
		this.item=item;
		this.op=op;
	}
}
