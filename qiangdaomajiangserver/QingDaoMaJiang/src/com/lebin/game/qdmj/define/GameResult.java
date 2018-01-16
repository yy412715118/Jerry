package com.lebin.game.qdmj.define;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GameResult extends SerializaableObject{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int score;
	public byte[] count=new byte[19];
	public int maxwinid=-1;
	public int maxwinscore;
	public int maxloseid=-1;
	public int maxlosescore;
	public long uid;
	public long maxloseuid;
	public long maxwinuid;
	public static final Item[] ITEMS=new Item[19];
	public static final Item ZIMO=Item.$(0,"自摸");
	public static final Item ZHUOPAO=Item.$(1,"捉炮");
	public static final Item DIANPAO=Item.$(2,"点炮");
	public static final Item BAOZHONGBAO=Item.$(3,"宝中宝");
	public static final Item MAOBAOHU=Item.$(4,"摸宝胡");
	public static final Item JIAHU=Item.$(5,"夹胡");
	public static final Item DAFENGHU=Item.$(6,"大风胡");
	public static final Item HONGZHONG=Item.$(7,"红中满天飞");
	public static final Item PINGHU=Item.$(8,"平胡");
	//public static final Item KOUTING=Item.$(10,"扣听");
	//public static final Item DUIDUIHE=Item.$(9,"对对胡");
	public static final Item LOUBAO=Item.$(9,"漏宝");
	public static final Item XIADAYU=Item.$(10,"下大雨");
	public static final Item DANDIAO=Item.$(11,"单吊夹");
	public static final Item ZHIDUI=Item.$(12,"支对");
	public static final Item BAOBIAN=Item.$(13,"宝边");
	public static final Item SANQIJIA=Item.$(14,"37夹");
	public static final Item MENQING=Item.$(15,"门清");

	//public static final Item TIANHU=Item.$(3,"天胡");
	//public static final Item DIHU=Item.$(4,"地胡");
	//public static final Item QIDUI=Item.$(5,"小七对");
	//public static final Item HH_QIDUI=Item.$(6,"豪华七对");
	//public static final Item CHH_QIDUI=Item.$(7,"超豪华七对");
	//public static final Item CCHH_QIDUI=Item.$(8,"超超豪华七对");
	//public static final Item HAIDILAO=Item.$(9,"海底捞");
	//public static final Item GANGKAI=Item.$(10,"杠上开花");
	
	//public static final Item SHOUBAIYI=Item.$(13,"手把一");
	//public static final Item YIPAOSANXIANG=Item.$(14,"一炮三响");
	//public static final Item MINGGANG=Item.$(15,"明杠");
	//public static final Item ANGANG=Item.$(16,"暗杠");
	//public static final Item QIANGGANG=Item.$(17,"抢杠");
	//public static final Item FANGMINGGANG=Item.$(18,"被明杠");
	private Map<String, Integer>desCounts=new HashMap<>();
	public void addDes(String des)
	{
		addDes(des, 1);
	}
	public void addDes(String des,int v)
	{
		des=des.trim();
		if(des.isEmpty())
			return;
		Integer count=desCounts.get(des);
		if(count==null)
			count=0;
		count+=v;
		desCounts.put(des, count);
	}
	public static class  Item
	{
		
		public final String name;
		public final int index;
		private Item(int index, String name) {
			this.name = name;
			this.index = index;
		}
		public static Item $(int index, String name)
		{
			Item item= new Item(index,name);
			ITEMS[index]=item;
			return item;
		}
	}
	public void add(RoundResult result)
	{
		for(int i=0;i<this.count.length;i++)
			this.count[i]+=result.count[i];
		this.score+=result.score;
	}
	public CountItem[] toItems()
	{
		List<CountItem>items=new  ArrayList<>();
		for(Entry<String, Integer>en:desCounts.entrySet())
		{
			CountItem item=new CountItem();
			item.name=en.getKey();
			item.count=en.getValue();
			items.add(item);
		}
		for(int i=0;i<3;i++)
		{
			if(this.count[i]==0)
			{
				CountItem item=new CountItem();
				item.name=ITEMS[i].name;
				item.count=this.count[i];
				items.add(item);
			}
		}
		for(int i=15;i<this.count.length;i++)
		{
			if(i!=17&&(this.count[i]>0||i<3))
			{
				CountItem item=new CountItem();
				item.name=ITEMS[i].name;
				item.count=this.count[i];
				items.add(item);
			}
		}
		return items.toArray(new CountItem[]{});
	}
}
