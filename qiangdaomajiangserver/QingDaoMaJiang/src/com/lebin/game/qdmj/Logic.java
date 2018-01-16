package com.lebin.game.qdmj;

import java.io.FileInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.hamcrest.Condition;
import org.slf4j.impl.StaticLoggerBinder;

import com.lebin.game.qdmj.define.CardData;
import com.lebin.game.qdmj.define.Define;
import com.lebin.game.qdmj.define.OpItem;
import com.lebin.game.qdmj.define.Option;
import com.lebin.game.qdmj.define.RoundInfo;
import com.lebin.game.qdmj.define.RoundResult;
import com.lebin.game.qdmj.define.UserOption;
import com.lebin.game.qdmj.define.WeaveCard;
import com.lebin.server.util.Config;

import atg.taglib.json.util.JSONArray;
import atg.taglib.json.util.JSONObject;

public class Logic {
	static Random random=new Random();
	private final int playercount;
	private int m_baopai;
	public static final int MAX_COUNT=112;//136;
	private Map<String,Boolean>m_playtype;//勾选的玩法
	private int[] m_zhidui;
	//private static int m_huType;
	//public static final short DONG=27;//27 28 29 30 31 32 33 18
	//public static final short NAN=28;
	//public static final short XI=29;
	//public static final short BEI=30;
	public static final short ZHONG=27;
	//public static final short FA=32;
	//public static final short BAI=33;
	//public static final short JI=18;
	//public static final short DONG_NAN_JI[]={JI,DONG,NAN};
	//public static final short ZHONG_FA_BAI[]={ZHONG,FA,BAI};
	//public static final short DONG_NAN_XI_BEI[]={DONG,NAN,XI,BEI};
	public Logic(int playercount)
	{
		this.playercount=playercount;
		m_zhidui = new int[4];
		for(int i = 0;i<4;i++){
			m_zhidui[i] = -1;
		}
	}

	/**
	 * 洗牌
	 * @return
	 */
	public static short[] shuffleCard(short[] cardata)
	{
		short[] cbCardData=new short[cardata.length];
		short[] cbCardDataTemp=new short[cardata.length];
		for(int i=0;i<cardata.length;i++)
			cbCardDataTemp[i]=cardata[i];
		int cbMaxCount=cardata.length;
		int cbRandCount=0,cbPosition=0;
		do
		{
			cbPosition=random.nextInt(cbMaxCount-cbRandCount);
			cbCardData[cbRandCount++]=cbCardDataTemp[cbPosition];
			cbCardDataTemp[cbPosition]=cbCardDataTemp[cbMaxCount-cbRandCount];
		} while (cbRandCount<cbMaxCount);
		return cbCardData;
	}
	public void shuffleCard(RoundInfo roundInfo,CardData[] cardDatas,List<Short>list,byte zhuangjia_chairid)
	{
		boolean debugable=false;
		if(debugable&&Config.getInt("debug", 0)==1)
			shuffleDebugCard(roundInfo, cardDatas, list, zhuangjia_chairid);
		else 
			shuffleCardQingDao(roundInfo, cardDatas, list, zhuangjia_chairid);
	}
	public void shuffleDebugCard(RoundInfo roundInfo,CardData[] cardDatas,List<Short>list,byte zhuangjia_chairid)
	{
		list.clear();
		List<Short>cardlist=new ArrayList<>();
		for(int i=0;i<playercount;i++)
		{
			cardDatas[i]=new CardData();
		}
		String string="";
		try {
			FileInputStream fin=new FileInputStream("config/debugcard.txt");
			byte[] data=new byte[fin.available()];
			int len=fin.read(data);
			string=new String(data, 0, len,"utf-8");
			fin.close();
			} catch (Exception e) {
				e.printStackTrace();
		}
		short[] hc={};
		try {
			JSONObject data=new JSONObject(string);
			zhuangjia_chairid=(byte)data.getInt("zhuangjiachairid");
			if(data.getInt("type")==1)
			{
				JSONArray array=data.getJSONArray("card");
				hc=new short[array.size()];
				for(int i=0;i<array.length();i++)
					hc[i]=(short)array.getInt(i);
			}
			else 
				hc=getCardids(data.getString("card"));
			roundInfo.zhuangjiachairid=zhuangjia_chairid;
			roundInfo.startchairid=(byte)data.getInt("startchairid");
			roundInfo.startpos=(byte)data.getInt("startpos");
			roundInfo.dianshu1=(byte)data.getInt("dianshu1");
			roundInfo.dianshu2=(byte)data.getInt("dianshu2");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<Short>ar=new ArrayList<>();
		for(short h:hc)
			ar.add(h);
	/*	for(int i=0;i<MAX_COUNT;i++)
		{
			if(!ar.contains(i))
				ar.add((short)i);
		}*/
		hc=new short[ar.size()];
		for(int i=0;i<ar.size();i++)
			hc[i]=ar.get(i);
		addCards(cardDatas[0].norcard, hc, 0, 13);
		addCards(cardDatas[1].norcard, hc, 13, 13);
		addCards(cardDatas[2].norcard, hc, 26, 13);
		addCards(cardDatas[3].norcard, hc, 39, 13);
		for(int i=52;i<hc.length;i++)
			cardlist.add(hc[i]);
		list.addAll(cardlist);
		roundInfo.leftcount=(short)cardlist.size();
		roundInfo.frontremove=(short)(playercount*13);
		roundInfo.curchairid=-1;
		roundInfo.tailremove=0;
	}
	public static void addCards(List<Short>list,short[]ids,int index,int len)
	{
		for(int i=index;i<index+len;i++)
		{
			list.add(ids[i]);
		}
	}
	/*
	 * 洗牌
	 */
	public void shuffleCardQingDao(RoundInfo roundInfo,CardData[] cardDatas,List<Short>list,byte zhuangjia_chairid)
	{
		list.clear();
		List<Short>cardlist=new ArrayList<>();
		for(int i=0;i<playercount;i++)
		{
			cardDatas[i]=new CardData();
		}
		//洗牌
		short[] cardata=new short[MAX_COUNT];
		for(int i=0;i<cardata.length;i++)
			cardata[i]=(short)i;
		short[] data=shuffleCard(cardata);
		for(short v:data)
			cardlist.add(v);
		//掷骰子
		byte[] dicedata={(byte)(random.nextInt(6)+1),(byte)(random.nextInt(6)+1)};
		byte value=(byte)(dicedata[0]+dicedata[1]);
		//定庄家
		if(zhuangjia_chairid==-1)
			zhuangjia_chairid=(byte)random.nextInt(playercount);
		
		//确定起牌玩家位置
		byte fromchairid=(byte)((zhuangjia_chairid+12-value+1)%4);
		byte startpos=value;//起牌位置，顺时针数


		
		//发牌
		for(int i=0;i<playercount;i++)
		{
			int chariid=(zhuangjia_chairid+i)%playercount;
			for(int j=0;j<3;j++)
				cardDatas[chariid].norcard.addAll(cardlist.subList(i*4+j*(playercount*4),(i+1)*4+j*(playercount*4)));
			cardDatas[chariid].norcard.add(cardlist.get(playercount*12+i));
		}
		cardlist=cardlist.subList(playercount*13, cardlist.size());
		list.addAll(cardlist);


		roundInfo.zhuangjiachairid=zhuangjia_chairid;
		roundInfo.startchairid=fromchairid;
		roundInfo.startpos=startpos;
		roundInfo.dianshu1=dicedata[0];
		roundInfo.dianshu2=dicedata[1];
		roundInfo.leftcount=(short)cardlist.size();
		roundInfo.frontremove=(short)(playercount*13);
		roundInfo.curchairid=-1;
		roundInfo.tailremove=0;
	}
	private static short[] sortCards(short...cards)
	{
		Arrays.sort(cards);
		return cards;
	}
	private static WeaveCard newWeaveCard(int type,int centercard,int card1,int card2,int card3,int chairid)
	{
		WeaveCard weaveCard=new WeaveCard();
		weaveCard.type=(byte)type;
		weaveCard.centercard=(short)centercard;
		weaveCard.chairid=(byte)chairid;
		weaveCard.card[0]=(short)centercard;
		weaveCard.card[1]=(short)card1;
		weaveCard.card[2]=(short)card2;
		weaveCard.card[3]=(short)card3;
		sortCards(weaveCard.card);
		return weaveCard;
	}
	private static WeaveCard newWeaveCard(int type,int centercard,int chairid,short...cardid)
	{
		WeaveCard weaveCard=new WeaveCard();
		weaveCard.type=(byte)type;
		weaveCard.centercard=(short)centercard;
		weaveCard.chairid=(byte)chairid;
		for(int i=0;i<4;i++)
		{
			if(i<cardid.length)
				weaveCard.card[i]=cardid[i];
			else 
				weaveCard.card[i]=-1;
		}
		sortCards(weaveCard.card);
		return weaveCard;
	}
	private static WeaveCard newPengPai(int pengcard,int card1,int card2,int chairid)
	{
		return newWeaveCard(WeaveCard.TYPE_PENG, pengcard, card1, card2, -1, chairid);
	}
	private static WeaveCard newZhiDui(int gangcard,int chairid,short...cardid)
	{
		return newWeaveCard(WeaveCard.TYPE_ZHIDUI, gangcard,chairid,cardid);
	}
//	private static WeaveCard newAnGang(int gangcard,int chairid,short...cardid)
//	{
//		return newWeaveCard(WeaveCard.TYPE_ANGANG, gangcard,chairid,cardid);
//	}
//	private static WeaveCard newMingGang(int gangcard, int chairid,short...cardid)
//	{
//		return newWeaveCard(WeaveCard.TYPE_MINGGANG, gangcard, chairid,cardid);
//	}
	public static void setCards(CardData cardData,short...ids)
	{
		for(short id:ids)
			cardData.norcard.add(id);
	}
	static Map<String, Short>idsMap=new HashMap<>();
	static
	{
		for(int i=0;i<9;i++)
			idsMap.put((i+1)+"万", (short)(i*4));
		for(int i=9;i<18;i++)
			idsMap.put((i-9+1)+"筒", (short)(i*4));
		for(int i=18;i<27;i++)
			idsMap.put((i-18+1)+"条", (short)(i*4));
		//String x="东南西北中發白";
		String x="中";
		idsMap.put(x.charAt(0)+"", (short)(108));
//		for(int i=0;i<x.length();i++)
//		{
//			idsMap.put(x.charAt(i)+"", (short)((27+i)*4));
//		}
	}
	public static short[] getCardids(String str)
	{
		String strs[]=str.split(",");
		short []rs=new short[strs.length];
		int index=0;
		Map<String, Short>ct=new HashMap<>();
		for(String s:strs)
		{
			s=s.trim();
			Short ix=ct.get(s);
			if(ix==null)
				ix=0;

			short v=idsMap.get(s);
			v+=ix;
			ix++;
			ct.put(s, ix);
			rs[index++]=v;
		}
		return rs;
	}

	public static void main(String[] args) {
		
	}
	

	public static void sortOption(List<UserOption>list)
	{
		Collections.sort(list, comparator);
	}
	static Comparator<UserOption> comparator=new Comparator<UserOption>() {

		@Override
		public int compare(UserOption op1, UserOption op2) {
			if(op1.op==op2.op)
				return 0;
			if(op1.op==Option.OP_NONE)
				return 1;
			else if(op2.op==Option.OP_NONE)
				return -1;
			if(op1.op<op2.op)
				return -1;
			return 1;
		}
			
	};
	public static int compare(Option op1,Option op2)
	{
		if(op1.op==op2.op)
			return 0;
		if(op1.op==Option.OP_NONE)
			return 1;
		else if(op2.op==Option.OP_NONE)
			return -1;
		if(op1.op<op2.op)
			return -1;
		return 1;
	}
	public void setBaoPai(int baopaiID) {
		m_baopai = baopaiID;
	}
	public int getBaoPai() {
		return m_baopai;
	}
	public void setPlayType(Map<String, Boolean> map) {
		m_playtype = map;
	}
	public Map<String, Boolean> getPlayType() {
		return m_playtype;
	}
	
	public WeaveCard peng(CardData cardData,byte curchairid,short curcard,UserOption option)
	{
		if(curchairid<0||curcard<0)
			return null;
		for(short id:option.item.rm)
		{
			if(id==curcard||(!cardData.norcard.contains(id)&&id!=cardData.incard))
				return null;
		}
		removeCard(cardData, option.item.rm);
		WeaveCard weaveCard=newWeaveCard(WeaveCard.TYPE_PENG, curcard, curchairid, option.item.info);
		cardData.weavecard.add(weaveCard);
		return weaveCard;
	}
	
	public Option getZhiDuiOption(CardData cardData)
	{
//		System.out.println("XXXXXXXX进入支对1111111");
//		if (true) {
//			System.out.println("XXXXXXXX进入支对2222222");
//			Option option1=new Option();
//			option1.op=Option.OP_ZHIDUI;
//			OpItem item1=new OpItem();
//			item1.id=0;
//			item1.info=new short[]{70,2};
//			item1.rm=new short[]{6,70};
//			option1.item=new OpItem[]{item1};
//			System.out.println("XXXXXXXX进入支对3333333");
//			return option1;	
//		}
		
		Map<String, Boolean> playt = getPlayType();
		if (!playt.get("zhidui")) {
			return null;
		} 
		List<Short>cards=new ArrayList<>();
		List<Short>nocards=new ArrayList<>();
		cards.addAll(cardData.norcard);
		//nocards.addAll(cardData.norcard);
		if(cardData.incard!=null&&!cards.contains(cardData.incard)){
			cards.add(cardData.incard);
			//nocards.add(cardData.incard);
		}
		
		boolean haveshun = false;
		boolean haveke = false;
		for(int j = 0;j<cardData.weavecard.size();j++){
			short[] pai =  cardData.weavecard.get(j).card;
			for(int k = 0;k<4;k++){
				if (pai[k] != -1) {
					if (pai[0] != pai[1]) {
						haveshun = true;
					}else {
						haveke = true;
					}
					nocards.add(pai[k]);
				}
			}
		}
		byte[] counts=count(cards);
		byte[] alnocards = count(nocards);
		boolean haveYaoPai = getYaoPai(alnocards,counts,haveshun,haveke);
		
		if (!haveYaoPai) {//无腰牌
			return null;
		}

		short num = 0;
		short[] jiang = new short[2];
		for(short j = 0;j<counts.length;j++){
			if (counts[j] == 2) {
				jiang[num>1?1:num] = j;
				num++;
			}
		}
		
		counts[jiang[0]] -=2;
		counts[jiang[1]] -=2;
		boolean rs = check(counts, 0,counts.length);
		counts[jiang[0]] +=2;
		counts[jiang[1]] +=2;
		if (!rs) 
			return null;
		
		if (num == 2) {//有俩对将
			Option option=new Option();
			option.op=Option.OP_ZHIDUI;
			OpItem item=new OpItem();
			item.id=0;
			item.info=jiang;
			item.rm=new short[]{jiang[0],jiang[1]};
			option.item=new OpItem[]{item};
			return option;	
		}
		
		return null;
	}
	
	public WeaveCard zhidui(CardData cardData,byte curchairid,short curcard,short cardid)
	{
		if(curchairid<0||curcard<0)
			return null;
//		for(short id:option.item.rm)
//		{
//			if(id==curcard||(!cardData.norcard.contains(id)&&id!=cardData.incard))
//				return null;
//		}
		removeCard(cardData, new short[]{cardid,cardid});
		WeaveCard weaveCard=newWeaveCard(WeaveCard.TYPE_ZHIDUI, curcard, curchairid, new short[]{cardid,cardid});
		cardData.weavecard.add(weaveCard);
		return weaveCard;
	}
	
	public static Option getPengOption(CardData cardData,short cardid)
	{
		if(cardData.norcard.size()!=4&&cardData.lastout!=-1&&cardData.lastout/4==cardid/4)//打什么不能碰什么
			return null;
		int v=cardid/4;
		short ids[]=new short[3];
		int index=0;
		for(int i=0;i<cardData.norcard.size();i++)
		{
			short v1=cardData.norcard.get(i);
			if(cardid!=v1&&v1/4==v)
			{
				ids[index++]=v1;
				if(index==2)
				{
					Option option=new Option();
					option.op=Option.OP_PENG;
					ids[2]=cardid;
					OpItem item=new OpItem();
					item.id=0;
					item.info=ids;
					item.rm=new short[]{ids[0],ids[1]};
					option.item=new OpItem[]{item};
					return option;
				}
			}
		}
		return null;
	}
	public static boolean getYaoPai(byte[] counts,byte[] handcard,boolean haveshun,boolean haveke) {
		int max=MAX_COUNT/4;
		System.out.println("xxxxxx检测腰牌1111111");
		if (counts[27]>1 || handcard[27]>1) {//红中可以忽略接下来的条件
			return true;
		}
		byte[] allcards = new byte[max];
		for(int i = 0;i<max;i++){
			if (counts[i]>0) {
				allcards[i]+=counts[i];
			}
			if (handcard[i]>0) {
				allcards[i]+=handcard[i];
			}
		}
		for (int i = 0; i < allcards.length; i++) {
			if (allcards[i]>0) {
				System.out.println("xxxxxxxx腰牌zzzzzz::"+i+"---"+allcards[i]);
			}
			
		}
		boolean isbaseyao = false;
		for(int i = 0;i<allcards.length;i++){
			if (allcards[i]>0) {
				if (i == 18 || i%9 == 8) {
					isbaseyao = true;
					break;
				}
			}
		}
		if (!isbaseyao){ //有腰牌(即需要有幺或者9)还需有顺牌,有顺牌还需检测有无刻牌，无刻牌检测能否胡对倒
			System.out.println("xxxxxx检测腰牌::无幺鸡或者9");
			return false;
		}
		boolean doubleColor = false;//必须有两种花色或以上才能听胡,红中不算色
		for(int k = 0;k<max-2;k++){
			if (allcards[k] >0) {
				for(int k2 = k+1;k2<max-1;k2++){
					if (allcards[k2]>0 &&k/9 != k2/9 ) {
						doubleColor = true;
					}
				}
				break;
			}
		}
		if (doubleColor) {
			int ty1 = 0;
			int ty2 = 0;
			int ty3 = 0;
			int t1 = 0;
			int t2 = 0;
			int t3 = 0;
			for(int i = 0;i<max-1;i++){
				if (allcards[i]>0) {
					if (i>=0 && i<9) {
						t1 = 1;
						ty1 += allcards[i];
					}
					if (i>=9 && i<18) {
						t2 = 1;
						ty2 += allcards[i];
					}
					if (i>=18 && i<27) {
						t3 = 1;
						ty3 += allcards[i];
					}
				}
			}
			if (t1+t2+t3<3) {
				if (ty1 == 1 || ty2 == 1 || ty3 == 1) {
					doubleColor = false;
				}
			}
			
		}
		if (!doubleColor) {
			System.out.println("xxxxxx检测腰牌::无两种颜色");
			return false;
		}
		System.out.println("xxxxxx检测腰牌444444");
		if (haveke && haveshun) {
			System.out.println("xxxxxx检测腰牌::有顺有课");
			return true;
		}
		//检测顺牌
		if (!haveshun) {
			int shunpai = 0;
			for(int i1 = 0;i1<9;i1++){
				if (handcard[i1]>0) {
					shunpai++;
					if (shunpai>=3) {
						return true;
					}
				}else {
					shunpai = 0;
				}
			}
			shunpai = 0;
			for(int i2 = 9;i2<18;i2++){
				if (handcard[i2]>0) {
					shunpai++;
					if (shunpai>=3) {
						return true;
					}
				}else {
					shunpai = 0;
				}
			}
			shunpai = 0;
			for(int i3 = 18;i3<27;i3++){
				if (handcard[i3]>0) {
					shunpai++;
					if (shunpai>=3) {
						return true;
					}
				}else {
					shunpai = 0;
				}
			}
		}
		if (!haveke) {
			int duidao = 0;
			for(int i = 0;i<max;i++){
				if (handcard[i]>=3) {//有刻牌
					return true;
				}
				
				if (handcard[i] == 2) {//无刻牌胡对倒
					duidao ++;
				}
				System.out.println("xxxxxxx刻牌数量::"+duidao);
				
			}
			if (duidao == 2) //大于二肯定不能听，因为不带七小对,等于1是将牌,所以只能等于2
				return true;
		}

		System.out.println("xxxxxx检测腰牌555555::无顺刻");
		return false;
	}
	
	public static byte[] count(List<Short>cards)
	{
		byte[] counts=new byte[MAX_COUNT/4];
		for(short c:cards)
			counts[c/4]++;
		return counts;
	}
	/**
	 * 获取听牌操作
	 * @param cardDatas
	 * @param chairid
	 * @return
	 */
	public static Option getTing(CardData[] cardDatas,byte chairid)
	{
		System.out.println("xxxxxxxxxTTTTTTTTT11111");
		int max=MAX_COUNT/4;
		//把手牌里的牌每次去掉一张，判断去掉后是否叫牌，叫牌判断需要把所有牌一个个加进来判断是否胡牌，胡了就算叫牌
		CardData carddata=cardDatas[chairid];
		List<Short>cards=new ArrayList<>();
		List<Short>alcards=new ArrayList<>();
		cards.addAll(carddata.norcard);
		//alcards.addAll(carddata.norcard);
		if(carddata.incard!=null&&!cards.contains(carddata.incard)){
			cards.add(carddata.incard);
			//alcards.add(carddata.incard);
		}
		System.out.println("xxxxxxxxxTTTTTTTTT22222");
		boolean haveshun = false;
		boolean haveke = false;
		for(int j = 0;j<carddata.weavecard.size();j++){
			short[] pai =  carddata.weavecard.get(j).card;
			System.out.println("xxxxxx获取的组合牌为：："+pai[0]+"//"+pai[1]+"//"+pai[2]+"//"+pai[3]);
			if (pai[1]/4 != pai[2]/4) {
				
				haveshun = true;
			}else {
				haveke = true;
			}
			for(int k = 0;k<4;k++){
				alcards.add(pai[k]);
			}
		}
		byte[] counts=count(cards);
		byte[] allcard=count(alcards);
		boolean haveYaoPai = getYaoPai(allcard,counts,haveshun,haveke);
		if (!haveYaoPai) {
			System.out.println("xxxxxxxxxTTTTTTTTT223444333");
			return null;
		}
		System.out.println("xxxxxxxxxTTTTTTTTT33333");
		byte[] outcounts=getCount(cardDatas,chairid);
		
		List<OpItem>items=new ArrayList<>();
		byte id=0;
		//boolean check[]=new boolean[max];
		for(short cid:cards)
		{
			int v=cid/4;
			//if(check[v])
			//	continue;
			//check[v]=true;
			counts[v]-=1;//去掉一张
			System.out.println("xxxxxxxxxTTTTTTTTTzzzzzzz::"+cid+"///"+v);
			for(short i=0;i<max;i++)
			{
				if(counts[i]>=4)
					continue;
				counts[i]+=1;//加一张
				int win=checkHu(counts)?1:0;//getWinType(carddata, counts);
				
				if(win!=0)
				{
					System.out.println("xxxxxxxxxTTTTTTTTT0000::----/"+win);
					OpItem item=new OpItem();
					byte leftcount=(byte)(4-outcounts[i]);
					item.info=new short[]{cid,(short)(i*4),leftcount<0?0:leftcount};
					item.id=id++;
					items.add(item);
				}
				counts[i]-=1;//恢复
			}
			counts[v]+=1;//恢复
		}
		System.out.println("xxxxxxxxxTTTTTTTTT444444");
		if(items.size()>0)
		{
			Option option=new Option();
			option.op=Option.OP_TING;
			option.item=items.toArray(new OpItem[]{});
			System.out.println("xxxxxxxxxTTTTTTTTT55555");
			return option;
		}
		return null;
	}
	public static byte[] getCount(CardData[] carddata,byte chairid)
	{
		byte[] counts=new byte[MAX_COUNT/4];
		for(int i=0;i<carddata.length;i++)
		{
			CardData data=carddata[i];
			if(i==chairid)//自己的牌，算手牌
			{
				for(short cid:data.norcard)
					counts[cid/4]++;
				if(carddata[i].incard!=null)
					counts[data.incard/4]++;
			}
			for(short cid:data.out)//算出牌
				counts[cid/4]++;
			for(WeaveCard weaveCard:data.weavecard)//算吃碰杠扭
			{
				for(short cid:weaveCard.card)
				{
					if(cid>-1)
						counts[cid/4]++;
				}
			}
		}
		return counts;
	}
	/*
	public static Option getNiuOption(CardData cardDatas,boolean gangable,List<Short>notincludes)
	{
		short[] cards=new short[8];
		Arrays.fill(cards, (short)-1);
		for(short id:cardDatas.norcard)
		{
			if(notincludes.contains(id))
				continue;
			int v=id/4;
			if(v==JI)
			{
				cards[0]=id;
			}
			int index=v-DONG;
			if(index>-1&&index<7)
				cards[index+1]=id;
		}
		if(cardDatas.incard!=null&&!notincludes.contains(cardDatas.incard))
		{
			int v=cardDatas.incard/4;
			if(v==JI)
			{
				cards[0]=cardDatas.incard;
			}
			int index=v-DONG;
			if(index>-1&&index<7)
				cards[index+1]=cardDatas.incard;
		}
		List<OpItem>items=new ArrayList<>();
		byte id=0;
		//判断东南
		if(cards[1]!=-1&&cards[2]!=-1)
		{
			if(cards[0]!=-1)//东南鸡
			{
				OpItem item=new OpItem();
				item.id=id++;
				item.info=new short[]{cards[0],cards[1],cards[2]};
				item.type=WeaveCard.TYPE_DONG_NAN_JI;
				items.add(item);
			}
			if(gangable&&cards[3]!=-1&&cards[4]!=-1)//东南西北
			{
				OpItem item=new OpItem();
				item.id=id++;
				item.info=new short[]{cards[1],cards[2],cards[3],cards[4]};
				item.type=WeaveCard.TYPE_DONG_NAN_XI_BEI;
				items.add(item);
			}
		}
		if(cards[5]!=-1&&cards[6]!=-1&&cards[7]!=-1)
		{
			OpItem item=new OpItem();
			item.id=id++;
			item.info=new short[]{cards[5],cards[6],cards[7]};
			item.type=WeaveCard.TYPE_ZHONG_FA_BAI;
			items.add(item);
		}
		if(items.size()>0)
		{
			Option option=new Option();
			option.op=Option.OP_NIU;
			option.item=items.toArray(new OpItem[]{});
			return option;
		}
		return null;
	}
	public static Option getTiaoOption(CardData cardDatas,boolean ting)
	{
		List<Short>ids=new ArrayList<>();
		boolean has[]=new boolean[8];
		Arrays.fill(has, false);
		for(WeaveCard weaveCard:cardDatas.weavecard)
		{
			if(weaveCard.type==WeaveCard.TYPE_DONG_NAN_JI)
			{
				has[0]=true;
				has[1]=true;
				has[2]=true;
			}
			else if(weaveCard.type==WeaveCard.TYPE_DONG_NAN_XI_BEI)
			{
				has[1]=true;
				has[2]=true;
				has[3]=true;
				has[4]=true;
			}
			else if(weaveCard.type==WeaveCard.TYPE_ZHONG_FA_BAI)
			{
				has[5]=true;
				has[6]=true;
				has[7]=true;
			}
		}
		int index=0;
		List<Short>cardids=new ArrayList<>();
		if(!ting)//听牌只能调进牌
			cardids.addAll(cardDatas.norcard);
		if(cardDatas.incard!=null)
			cardids.add(cardDatas.incard);
		for(short id:cardids)
		{
			index=-1;
			int v=id/4;
			if(v==JI)
				index=0;
			else if(v>=DONG)
				index=1+v-DONG;
			if(index>7||index<0)
				continue;
			if(has[index])
				ids.add(id);
		}
		if(ids.size()>0)
		{
			Option option=new Option();
			option.op=Option.OP_TIAO;
			OpItem item=new OpItem();
			item.id=0;
			item.info=new short[ids.size()];
			for(int i=0;i<item.info.length;i++)
				item.info[i]=ids.get(i);
			option.item=new OpItem[]{item};
			return option;
		}
		return null;
	}
	*/
	/*
	public static WeaveCard niupai(CardData cardData,byte chairid,UserOption option)
	{
		if(chairid<0)
			return null;
		for(short id:option.item.info)
		{
			if(!cardData.norcard.contains(id)&&id!=cardData.incard)
				return null;
		}
		removeCard(cardData,option.item.info);
		WeaveCard weaveCard=newWeaveCard(option.item.type, -1, chairid, option.item.info);
		cardData.weavecard.add(weaveCard);
		return weaveCard;
	}
	*/
	public static void removeCard(CardData cardData,short...ids)
	{
		removeCardOnly(cardData, ids);
		if(cardData.incard!=null)
		{
			cardData.norcard.add(cardData.incard);
			cardData.incard=null;
		}
	}
	public static void removeCardOnly(CardData cardData,short...ids)
	{
		for(short id:ids)
		{
			if(!cardData.norcard.remove((Object)id))
			{
				if(cardData.incard!=null&&cardData.incard==id)
				{
					cardData.incard=null;
				}
			}
			
		}
		
	}
	public static short[] getLimitCardid(short[]ids,int... indexs)
	{
		short[] buf=new short[indexs.length];
		Arrays.fill(buf, (short)-1);
		int i=0;
		for(int index:indexs)
		{
			if(ids[index]!=-1)
				buf[i++]=ids[index];
		}
		if(i==0)
			return null;
		return Arrays.copyOf(buf, i);
	}
	public static short[] getOtherCardid(List<Short>cards,short[] limit)
	{
		List<Short>rs=new ArrayList<>();
		for(Short id:cards)
		{
			if(id==null||id<0)
				continue;
			int v=id/4;
			boolean flag=false;
			if(limit!=null)
			{
				for(short lm:limit)
				{
					if(lm<0)
						continue;
					if(lm/4==v)
					{
						flag=true;
					break;
					}
				}
			}
			if(!flag)
				rs.add(id);
		}
		if(rs.isEmpty())
			return null;
		short[] rt=new short[rs.size()];
		for(int i=0;i<rs.size();i++)
			rt[i]=rs.get(i);
		return rt;
	}
	/*
 	public static Option getBaYiChiOption(CardData cardData,short cardid,int count)
	{
		int v=cardid/4;
		if(v>=DONG||count>2||cardData.norcard.size()!=4)
			return null;
		int a=cardid/36;
		int start=a*9-1;
		int end=(a+1)*9;
		short ids[]={-1,-1,-1,-1,-1,-1,-1};
		boolean flag=count>1;
		for(short id:cardData.norcard)
		{
			int v1=id/4;
			if(v1>start&&v1<end)
			{
				int vx=Math.abs(v-v1);
				if(vx>=0&&vx<4)
					ids[v1-v+3]=id;
			}
		}
		List<OpItem>items=new ArrayList<>();
		byte id=0;
		if(ids[1]!=-1&&ids[2]!=-1)
		{
			short[] lmtid=flag?getLimitCardid(ids,0,3):null;
			if(!flag||lmtid!=null)
			{
				OpItem item=new OpItem();
				item.id=id++;
				item.info=new short[]{ids[1],ids[2],cardid};
				item.rm=new short[]{ids[1],ids[2]};
				item.lmtid=lmtid;
				items.add(item);
			}
		}
		if(ids[2]!=-1&&ids[4]!=-1)
		{
			short[] lmtid=flag?getLimitCardid(ids,3):null;
			if(!flag||lmtid!=null)
			{
				OpItem item=new OpItem();
				item.id=id++;
				item.info=new short[]{ids[2],cardid,ids[4]};
				item.rm=new short[]{ids[2],ids[4]};
				item.lmtid=lmtid;
				items.add(item);
			}
		}
		if(ids[4]!=-1&&ids[5]!=-1)
		{
			short[] lmtid=flag?getLimitCardid(ids,3,6):null;
			if(!flag||lmtid!=null)
			{
				OpItem item=new OpItem();
				item.id=id++;
				item.info=new short[]{cardid,ids[4],ids[5]};
				item.rm=new short[]{ids[4],ids[5]};
				item.lmtid=lmtid;
				items.add(item);
			}
			
		}
		if(items.size()>0)
		{
			Option option=new Option();
			option.op=Option.OP_CHI;
			option.item=items.toArray(new OpItem[]{});
			return option;
		}
		return null;
	}
	*/
	public static Option getChiOption(CardData cardData,short cardid)
	{
		int v=cardid/4;
//		if(v>=DONG||count>2||(count==2&&cardData.norcard.size()!=4))
//			return null;
//		if(cardData.norcard.size()==4)
//			return getBaYiChiOption(cardData, cardid, count);
		short lastoutcard=cardData.lastout;
		if(lastoutcard!=-1&&lastoutcard/4==cardid/4)//打什么就不能吃什么
			return null;
		int lastv=lastoutcard<0?-2:lastoutcard/4;
		int a=cardid/36;
		int start=a*9-1;
		int end=(a+1)*9;
		short ids[]={-1,-1,-1,-1,-1,-1,-1};
		int indexs[]={-1,-1,-1,-1,-1,-1,-1};
		for(short id:cardData.norcard)
		{
			int v1=id/4;
			if(v1>start&&v1<end)
			{
				int vx=Math.abs(v-v1);
				if(vx>=0&&vx<4)
					ids[v1-v+3]=id;
			}
		}
		for(int i=0;i<indexs.length;i++)
		{
			int v1=cardid/4-3+i;
			if(v1>start&v1<end)
				indexs[i]=v1;
		}
		List<OpItem>items=new ArrayList<>();
		byte id=0;
		if(ids[1]!=-1&&ids[2]!=-1&&(lastv!=indexs[0]))
		{
			short[] lmtid=getOtherCardid(cardData.norcard,getLimitCardid(ids,0,3));
			if(lmtid!=null)
			{
				OpItem item=new OpItem();
				item.id=id++;
				item.info=new short[]{ids[1],ids[2],cardid};
				item.rm=new short[]{ids[1],ids[2]};
				item.lmtid=lmtid;
				items.add(item);
			}
		}
		if(ids[2]!=-1&&ids[4]!=-1)
		{
			short[] lmtid=getOtherCardid(cardData.norcard,getLimitCardid(ids,3));
			if(lmtid!=null)
			{
				OpItem item=new OpItem();
				item.id=id++;
				item.info=new short[]{ids[2],cardid,ids[4]};
				item.rm=new short[]{ids[2],ids[4]};
				item.lmtid=lmtid;
				items.add(item);
			}
		}
		if(ids[4]!=-1&&ids[5]!=-1&&lastv!=indexs[6])
		{
			short[] lmtid=getOtherCardid(cardData.norcard,getLimitCardid(ids,3,6));
			if(lmtid!=null)
			{
				OpItem item=new OpItem();
				item.id=id++;
				item.info=new short[]{cardid,ids[4],ids[5]};
				item.rm=new short[]{ids[4],ids[5]};
				item.lmtid=lmtid;
				items.add(item);
			}
			
		}
		if(items.size()>0)
		{
			Option option=new Option();
			option.op=Option.OP_CHI;
			option.item=items.toArray(new OpItem[]{});
			return option;
		}
		return null;
	}
	public static WeaveCard chi(CardData cardData,byte chairid,short cardid,UserOption option)
	{
		int v=cardid/4;
//		if(v>=DONG)
//			return null;
		for(short c:option.item.rm)
		{
			if(!cardData.norcard.contains(c)&&c!=cardData.incard)
				return null;
		}
		removeCard(cardData, option.item.rm);
		WeaveCard weaveCard=newWeaveCard(WeaveCard.TYPE_CHI, cardid, chairid, option.item.info);
		cardData.weavecard.add(weaveCard);
		return weaveCard;
	}
	/*
	public static Option getGangOption(CardData cardData,byte chairid,short cardid,boolean self)
	{
		if(self)
		{
			cardid=cardData.incard==null?-1:cardData.incard;
			byte[] counts=count(cardData.norcard);
			if(cardid!=-1)
				counts[cardid/4]++;
			List<OpItem>items=new ArrayList<>();
			byte id=0;
			for(int i=0;i<counts.length;i++)
			{
				if(counts[i]==4)
				{
					OpItem item=new OpItem();
					item.id=id++;
					item.info=new short[]{(short)(i*4),(short)(i*4+1),(short)(i*4+2),(short)(i*4+3)};
					item.rm=item.info;
					item.type=WeaveCard.TYPE_ANGANG;
					item.gang=null;
					items.add(item);
				}
			}
			List<Short>cardids=new ArrayList<>();
			cardids.addAll(cardData.norcard);
			if(cardid!=-1&&!cardids.contains(cardid))
				cardids.add(cardid);
			if(cardids.size()>0)
			{
				//判断碰过的牌可不可以杠，加杠
				for(WeaveCard weaveCard:cardData.weavecard)
				{
					if(weaveCard.type==WeaveCard.TYPE_PENG)
					{
						int v1=weaveCard.centercard/4;
						for(short cid:cardids)
						{
							int v=cid/4;
							if(v!=v1)
								continue;
							int index=0;
							short ids[]=new short[4];
							for(short c:weaveCard.card)
							{
								if(c==cid)//牌已经碰过的
									break;
								if(c>-1&&c/4==v)
									ids[index++]=c;
							}
							if(index==3)
							{
								ids[3]=cid;
								Option option=new Option();
								option.op=Option.OP_GANG;
								OpItem item=new OpItem();
								item.id=id++;
								item.info=ids;
								item.rm=new short[]{cid};
								item.type=WeaveCard.TYPE_JIAGANG;
								item.gang=cid;
								items.add(item);
							}
						}
					}
				}
			}
			if(items.size()>0)
			{
				Option option=new Option();
				option.op=Option.OP_GANG;
				option.item= items.toArray(new OpItem[]{});
				return option;
			}
		}
		else
		{
			if(cardData.incard!=null&&cardid==cardData.incard)//杠别人的牌不能是自己的牌
				return null;
			int v=cardid/4;
			short[] ids=new short[4];
			int index=0;
			for(int i=0;i<cardData.norcard.size();i++)
			{
				short v1=cardData.norcard.get(i);
				if(v1!=cardid&&v1/4==v)
					ids[index++]=v1;
			}
			if(index==3)//明杠
			{
				Option option=new Option();
				option.op=Option.OP_GANG;
				OpItem item=new OpItem();
				ids[3]=cardid;
				item.id=0;
				item.info=ids;
				item.type=WeaveCard.TYPE_MINGGANG;
				item.rm=new short[]{ids[0],ids[1],ids[2]};
				option.item=new OpItem[]{item};
				item.gang=cardid;
				return option;
			}
		}
		return null;
	}
	
	
	public static WeaveCard gang(CardData cardData,byte chairid,byte curchairid,short cardid,UserOption option)
	{
		if(curchairid<0)
			return null;
		for(short c:option.item.rm)
		{
			if(!cardData.norcard.contains(c)&&c!=cardData.incard)
				return null;
		}
		if(option.item.type==WeaveCard.TYPE_ANGANG)
		{
			removeCard(cardData,option.item.rm);
			WeaveCard weaveCard= newAnGang(option.item.info[0], curchairid,option.item.info);
			cardData.weavecard.add(weaveCard);
			return weaveCard;
		}
		else if(option.item.type==WeaveCard.TYPE_JIAGANG)
		{
			int v=option.item.info[0]/4;
			for(WeaveCard weaveCard:cardData.weavecard)
			{
				if(weaveCard.type==WeaveCard.TYPE_PENG&&weaveCard.centercard/4==v)
				{
					weaveCard.type=WeaveCard.TYPE_JIAGANG;
					weaveCard.card=option.item.info;
					removeCard(cardData,option.item.rm);
					return weaveCard;
				}
			}
		}
		else if(option.item.type==WeaveCard.TYPE_MINGGANG)
		{
			WeaveCard weaveCard= newMingGang(cardid, curchairid,option.item.info);
			cardData.weavecard.add(weaveCard);
			removeCard(cardData,option.item.rm);
			return weaveCard;
		}
		return null;
			
	}
	*/
	public static Option getHuOption(CardData cardData,int incard,Map<String, Boolean> plyt,int baopai)
	{
		System.out.println("xxxxxxxxx进入虎牌11111");
		//如果未听不能胡
		int v=getWinType(cardData, incard,plyt,baopai);
		if(v==0)
			return null;
		System.out.println("xxxxxxxxx进入虎牌2");
		Option option=new Option();
		option.op=Option.OP_HU;
		OpItem item=new OpItem();
		item.id=0;
		option.item=new OpItem[]{item};
		System.out.println("xxxxxxxxx进入虎牌33333");
		return option;
		
	}
	public static boolean check(int v,int flag)
	{
		return (v&flag)==flag;
	}
	//夹胡
	public void setZhidui(int chairid,int zhiduiID){
		Map<String, Boolean> playt = getPlayType();
		if (playt.get("zhidui")) {
			m_zhidui[chairid] = zhiduiID;
		}else {
			m_zhidui[chairid] = -1;
		}
		
	}
	public int getZhidui(int chairid){
		if (m_zhidui  == null) {
			return -1;
		}
		return m_zhidui[chairid];
	}
	public static int getDaHu(byte[] counts,int incard,int zimo,Map<String, Boolean> plyt,int baopai)
	{
		Map<String, Boolean> playtype = plyt;
		boolean dandiaoable = playtype.get("dandiaojia");
		int max = MAX_COUNT/4;
		if(checkHu(counts)){//糊了，接下来判定是否夹胡
			byte[] tmpcounts = counts.clone();//new byte[max];//counts;
			//tmpcounts = counts.clone();
			//tmpcounts[incard/4] -=1;//先从牌中减去最后一张
			if (dandiaoable) {
				if (tmpcounts[incard/4] == 2) {
					return Define.HP_DAN_DIAO_JIA;//单吊胡
				}
			}
			//tmpcounts[incard/4] +=1;
//			if (playtype.get("zhidui")) {
//				int zhiduihupai = getZhidui(1);//支对胡的花色是哪一张牌的id，假设为23
//				if (zhiduihupai == incard/4) {//tmpcounts[incard/4] == 2 &&
//					return Define.HP_ZHI_DUI;//支对
//				}else {
//					return 0;
//				}
//			}
			for(int i = 0;i<max;i++){
				if (tmpcounts[i]>=2) {
					tmpcounts[i]-=2;//去掉一个对子
					byte[] nohu = checkNoHupai(tmpcounts);//剩余的牌型不符合的牌
					tmpcounts[i]+=2;
					int twopai = 0;
					for(int k = 0;k<max;k++){//因为红中跟谁也不能夹胡，所以如果k大于最大ID-1-2，那么就不是夹胡
						if (nohu[k]!=1 && nohu[k]!= 0) {
							twopai = 0;
							//dandiaojia = 0;
							break;
						}
						if (k<max-3) {
							if (nohu[k]==1) {
								twopai++;
							}
						}
//						if (nohu[k]==1) {
//							dandiaojia ++;
//						}
						
						
					}
//					for(int k2 = 0;k2<max;k2++){
//						if (nohu[k2]!=2 && nohu[k2] != 0) {
//							zhidui = 0;
//							break;
//						}
//						if (nohu[k2] == 2) {//必须只剩下一个对子,因为前面已经减去一个对子，而且必须与压下的对子id一致
//							zhidui++;
//						}
//					}
//					if (zhidui==1 && playtype.get("zhidui") && zhiduihupai != -1) {//支对
//						if (incard/4 == zhiduihupai) {
//							return 1;
//						}else {
//							return 0;
//						}
//					}
					
//					if (dandiaojia==1 && dandiaoable) {//单调夹
//						for(int k2 = 0;k2<max;k2++){
//							if (nohu[k2]==1 && incard/4 == k2) {//单调夹
//								return 1;
//							}
//						}
//					}
					if (twopai==2) {
						for (int j = 0; j < max-3; j++) {
							if (nohu[j]==1) {
								if (incard/4 == j+1 && nohu[j+2] == 1) {
									//组合的夹牌未判定
									if (isBaoPai(incard/4,baopai,plyt)&& zimo == -1) {//自摸到的最后一张是不是宝牌,宝中宝(必须要自摸才称为宝中宝)或者漏宝
										if (playtype.get("loubao")) {
											return Define.HP_LOU_BAO;//漏宝
										}
										return Define.HP_BAO_ZHONG_BAO;//宝中宝
									}
									return Define.HP_DA_HU;//夹胡,即胡中间那张
								}
							}
							int cardid = incard/4;
							if (cardid%9==2 && playtype.get("37jia")) {//37夹,
								if (nohu[cardid-1] == 1 && nohu[cardid-2] == 1) {
									if (cardid == j) {
										return Define.HP_SAN_QI_JIA;
									}
								}
								
							}else if(cardid%9==6){
								if (nohu[cardid+1] == 1 && nohu[cardid+2] == 1) {
									if (cardid == j) {
										return Define.HP_SAN_QI_JIA;
									}
								}
							}
						}
					}
				}
			}
		}
		return 0;
	}
	public static byte[] checkNoHupai(byte[] counts) {
		int start = 0;
		int end = counts.length;
		for(int j=start;j<counts.length;j++)//每次都减去一组符合牌型的，然后再重新遍历其他手牌是否符合牌型，如果不符合返回false
		{
			int c=counts[j];
			if(c==0)
				continue;
			if(c==1||c==2)//判断是否能和后面的牌构成顺子
			{
				if(j>=27)//东南西北中发白
					continue;
				if(!checkHua(j,j+1,j+2))
					continue;
				if(!checkCount(counts,j+1,end,c))
					continue;
				if(!checkCount(counts,j+2,end,c))
					continue;
				
				counts[j]-=c;
				counts[j+1]-=c;
				counts[j+2]-=c;
				boolean rs=check(counts, start,end);
				//不还原
//				counts[j]+=c;
//				counts[j+1]+=c;
//				counts[j+2]+=c;
				continue;
			}
			else if(c==3)
			{
				counts[j]-=3;//作为刻
				
				boolean rs=check(counts, start, end);
				counts[j]+=3;
				if(rs)
					continue;
				if(j>=27)//东南西北中发白
					continue;
				if(!checkHua(j,j+1,j+2))
					continue;
				//作为顺子
				if(!checkCount(counts,j+1,end,c))
					continue;
				if(!checkCount(counts,j+2,end,c))
					continue;
				counts[j]-=c;
				counts[j+1]-=c;
				counts[j+2]-=c;
				rs=check(counts, start,end);
				//不还原
//				counts[j]+=c;
//				counts[j+1]+=c;
//				counts[j+2]+=c;
				continue;
			}
			else if(c==4)
			{
			
				if(j>=27)//东南西北中发白
					continue;
				if(!checkHua(j,j+1,j+2))
					continue;
				//一个作为顺子
				if(!checkCount(counts,j+1,end,1))
					continue;
				if(!checkCount(counts,j+2,end,1))
					continue;
				counts[j]-=1;
				counts[j+1]-=1;
				counts[j+2]-=1;
			
				boolean rs=check(counts, start,end);
//				//不还原
//				counts[j]+=1;
//				counts[j+1]+=1;
//				counts[j+2]+=1;
				continue;
			}
			return counts;
		}
		return counts;//在糊的情况下检测放入最后一张之前没胡的是哪两张
		
	}
	/*
	public static int getXiaoQiDui(byte[] counts)
	{
		int c=0;
		int t=0;
		for(int count:counts)
		{
			if(count==4)
				c++;
			else if(count%2!=0)
				return 0;
			t+=count;
		}
		if(t!=14)
			return 0;
		if(c==0)
			return Define.HP_XIAO_QI_DUI;
		if(c==1)
			return Define.HP_HAOHUA_QI_XIAO_DUI;
		if(c==2)
			return Define.HP_CHAO_HAOHUA_QI_XIAO_DUI;
		if(c==3)
			return Define.HP_CHAO_CHAO_HAOHUA_QI_XIAO_DUI;
		return 0;
	}
	*/
	public static boolean isBaoPai(int cardid,int baopai,Map<String, Boolean> plyt) {
		System.out.println("宝牌是::"+baopai+"传牌是::"+cardid);
		if (baopai == 27 && cardid == 27) {
			return true;
		}
		Map<String, Boolean> playtype = plyt;
		if (playtype.get("shanggunbao")) {
			if (baopai%9 == 8) {
				if (cardid == baopai || cardid == baopai-8) {
					return true;
				}
			}else {
				if (cardid == baopai || cardid == baopai+1) {
					return true;
				}
			}
		}
		if (playtype.get("xiagunbao")) {
			if (baopai%9 == 0) {
				if (cardid == baopai || cardid == baopai+8) {
					return true;
				}
			}else {
				if (cardid == baopai || cardid == baopai-1) {
					return true;
				}
			}
		}
		if (playtype.get("tongbao")) {
			if (cardid%9 == baopai%9) {
				return true;
			}
		}
		return false;
	}
	/*
	public static int getPengPengHu(byte[] counts,CardData data)
	{
		//检查手牌
		int c=0;
		int cardcount=0;
		for(int count:counts)
		{
			if(count==4||count==1)
				return 0;
			if(count==2)
				c++;
			if(c>1)
				return 0;
			cardcount+=count;
		}
		//检查是否有吃牌
		for(WeaveCard weaveCard:data.weavecard)
		{
			if(weaveCard.type==WeaveCard.TYPE_CHI)
				return 0;
			//cardcount+=3;
		}
		if(cardcount==14)
			return Define.HP_PENGPENG_HU;//碰碰胡：2+(3[+1])*4
		return 0;
	}
	*/
	public int getXiaDaYuHu(byte[] counts,int incard){
		int max = MAX_COUNT/4;
		for(int i= 0;i<max;i++){
//			if (counts[i] == 3 && isBaoPai(i)) {
//				return Define.HP_XIA_DA_YU;
//			}
		}
		return 0;
	}
	public static int getDaFengHu(byte[] counts,int incard,Map<String, Boolean> plyt)
	{
		int cardid = incard/4;
		Map<String, Boolean> playtype = plyt;
		if (playtype.get("shanggundafeng")) {
			if (cardid%9 == 0) {
				
			}else {
				if (counts[cardid-1]==3) {
					return Define.HP_DA_FENG_HU;
				}
			}
		}
		if (playtype.get("xiagundafeng")) {
			if (cardid%9 == 8 || cardid == 27) {
				
			}else {
				if (counts[cardid+1]==3) {
					return Define.HP_DA_FENG_HU;
				}
			}
		}
		if (counts[cardid] == 4) {
			return Define.HP_DA_FENG_HU;//大风胡
		}
		//宝牌是否可以大风胡,bukeyi
		
		return 0;
	}
	
	public int getHongZhongHu(byte[] counts,int incard)
	{
		Map<String, Boolean> playtype = getPlayType();
		if (playtype.get("hzmtf"))
		{
		if (incard/4== 27) {
			return Define.HP_HONG_ZHONG_MAN_TIAN_FEI;//红中
		}
		}
		return 0;
	}
	
	public static int getXiaoHu(byte[] counts,int incard,int zimo,Map<String, Boolean> plyt,int baopai)
	{
		System.out.println("xxxxxxx进入小胡判定11111111");
		
		if (zimo == -1) {
			System.out.println("xxxxxxx进入小胡判定2222");
			if (isBaoPai(incard/4,baopai,plyt)) {
				System.out.println("xxxxxxx进入小胡判定999999");
				if (checkHu(counts)) {
					System.out.println("xxxxxxx进入小胡判定333333");
					if (counts[incard/4] == 1) {
						System.out.println("xxxxxxx进入小胡判定444444");
						return Define.HP_MO_BAO_HU;//摸宝胡，不算宝边
					}
					System.out.println("xxxxxxx进入小胡判定55555555");
					return Define.HP_BAO_BIAN;//宝边
				}
				System.out.println("xxxxxxx进入小胡判定6666666");
				return Define.HP_MO_BAO_HU;//摸宝胡
			}else {
				System.out.println("xxxxxxx进入小胡判定zzzzzzz");
				if (checkHu(counts)) {
					System.out.println("xxxxxxx进入小胡判定000000");
					
					return Define.HP_XIAOHU;//宝边
				}
			}
		}else {
			System.out.println("xxxxxxx进入小胡判定7777777");
			for(int i = 0;i<counts.length;i++){
				if (counts[i]!= 0) {
					System.out.println("玩家最终小胡牌是::"+i+"数量为::"+counts[i]);
				}
			}
			if(checkHu(counts)){
				System.out.println("xxxxxxx进入小胡判定888888");
				return Define.HP_XIAOHU;
			}
		}
		return 0;
	}
	/*
	public static int getQingYise(byte[] counts,CardData cardData)
	{
		int max=36*3;
		int flag=-1;
		for(int i=0;i<max;i++)
		{
			if(counts[i]>0)
			{
				int v=i/36;
				if(flag==-1)
					flag=v;
				else if(flag!=v)
					return 0;
			}
		}
		for(WeaveCard w:cardData.weavecard)
		{
			if(w.centercard>=max)
				return 0;
			if(flag==-1)
				flag=w.centercard/36;
			else if(flag!=w.centercard/36)
				return 0;
		}
		return Define.HP_QING_YI_SE;
	}
	*/
	public static boolean checkHu(byte[] counts)
	{
		return checkHu(counts, 0, counts.length);
		/*int index=27;
		int end=MAX_COUNT/4;
		int c=0;
		//检查东南西北，中发白
		for(int i=index;i<end;i++)
		{
			if(counts[i]%2==0)
				c+=counts[i]/2;
			if(counts[i]==1||c>1)//单张东南西北，中发白或者2个对子
				return false;
		}
		//检查万子
		if(!checkHu(counts, 0, 9))
			return false;
		//检查筒
		if(!checkHu(counts, 9, 18))
			return false;
		//检查条
		if(!checkHu(counts, 18, 27))
			return false;
		return true;*/
	}
	public static boolean checkHua(int... vs)
	{
		int v1=vs[0]/9;
		for(int v:vs)
		{
			if(v/9!=v1)
				return false;
		}
		return true;
	}
	public static boolean check(byte[] counts,int start,int end)
	{
		for(int j=start;j<end;j++)//每次都减去一组符合牌型的，然后再重新遍历其他手牌是否符合牌型，如果不符合返回false
		{
			int c=counts[j];
			if(c==0)
				continue;
			if(c==1||c==2)//判断是否能和后面的牌构成顺子
			{
				if(j>=27)//东南西北中发白
					return false;
				if(!checkHua(j,j+1,j+2))
					return false;
				if(!checkCount(counts,j+1,end,c))
					return false;
				if(!checkCount(counts,j+2,end,c))
					return false;
				
				counts[j]-=c;
				counts[j+1]-=c;
				counts[j+2]-=c;
				boolean rs=check(counts, start,end);
				//还原
				counts[j]+=c;
				counts[j+1]+=c;
				counts[j+2]+=c;
				return rs;
			}
			else if(c==3)
			{
				counts[j]-=3;//作为刻
				
				boolean rs=check(counts, start, end);
				counts[j]+=3;
				if(rs)
					return true;
				if(j>=27)//东南西北中发白
					return false;
				if(!checkHua(j,j+1,j+2))
					return false;
				//作为顺子
				if(!checkCount(counts,j+1,end,c))
					return false;
				if(!checkCount(counts,j+2,end,c))
					return false;
				counts[j]-=c;
				counts[j+1]-=c;
				counts[j+2]-=c;
				rs=check(counts, start,end);
				//还原
				counts[j]+=c;
				counts[j+1]+=c;
				counts[j+2]+=c;
				return rs;
			}
			else if(c==4)
			{
			
				if(j>=27)//东南西北中发白
					return false;
				if(!checkHua(j,j+1,j+2))
					return false;
				//一个作为顺子
				if(!checkCount(counts,j+1,end,1))
					return false;
				if(!checkCount(counts,j+2,end,1))
					return false;
				counts[j]-=1;
				counts[j+1]-=1;
				counts[j+2]-=1;
			
				boolean rs=check(counts, start,end);
				//还原
				counts[j]+=1;
				counts[j+1]+=1;
				counts[j+2]+=1;
				return rs;
			}
			return false;
		}
		return true;
	}
	public static boolean checkHu(byte[] counts,int start,int end)
	{
		int c=0;
		for(int i=start;i<end;i++)
		{
			c+=counts[i];
			if(counts[i]>=2)
			{
				counts[i]-=2;//去掉一个对子
				boolean rs=check(counts,start,end);
				counts[i]+=2;//还原
				if(rs)
					return true;
			}
		}
		return c==0;
	}
	public static boolean checkCount(byte[] counts,int index,int end,int count)
	{
		if(index>=end)
			return false;
		return counts[index]>=count;
	}
	public static int getWinType(CardData cardData,byte[] counts,Map<String, Boolean> plyt,int baopai)
	{
//		int v=0;
//		if(v==0)
//			v=getPengPengHu(counts,cardData);
//		if(v==0)
		
//		for(int i = 0;i<counts.length;i++){
//			System.out.println("xxxxx数组第"+i+"位为：："+counts[i]);
//		}
		short zimo = -1;
		short va = 100;
		if (cardData.incard == null) {
			zimo = 0;
		}else {
			va = (short) cardData.incard.shortValue();
		}
		System.out.println("xxxxx小胡判定进入的牌是"+va);
		int	v=getXiaoHu(counts,va,zimo,plyt,baopai);
		/*if(v!=0)
			v|=getQingYise(counts,cardData);*/
		return v;
	}
	public static int getWinType(CardData cardData,int incard,Map<String, Boolean> plyt,int baopai)//进牌
	{
		byte[] counts=new byte[MAX_COUNT/4];
		Map<String, Boolean> playtype = plyt;
		int tmpincard = incard;
		if(incard!=-1){//incard =-1 代表自摸,也代表cardData.incard不为空
			counts[incard/4]=1;
		}else {
			tmpincard = cardData.incard;
		}
		if(cardData.incard!=null)
			counts[cardData.incard/4]+=1;
		for(short st:cardData.norcard)
		{
			int v=st/4;
			counts[v]++;
		}
		for(int i = 0;i<counts.length;i++){
			if (counts[i]!= 0) {
				System.out.println("玩家手里的牌是::"+i+"数量为::"+counts[i]);
			}
		}
		
		int v=getDaHu(counts, tmpincard,incard,plyt,baopai);
		//if (playtype.get("hzmtf")) //红中满天飞
			//v = getHongZhongHu(counts,tmpincard);
//		if (playtype.get("zhidui")) {//如果为胡支对
//			return v;
//		}
		if(v==0 && playtype.get("dafeng") && incard == -1)//刮大风
			v=getDaFengHu(counts,tmpincard,plyt);//getPengPengHu(counts,cardData);
		
		if(v==0)
			v=getXiaoHu(counts,tmpincard,incard,plyt,baopai);
		/*if(v!=0)
			v|=getQingYise(counts,cardData);*/
		return v;
	}
	public static void subScore(int[] scores,int score,int selfchairid,int playerNum)
	{
		for(int i=0;i<playerNum;i++)
		{
			if(i!=selfchairid)
				scores[i]-=score;
		}
	}
	public static RoundResult[] countResult(RoundResult[] totalResults,int difen,CardData[] cardDatas,int winflags[],int wintypes[],int fangpaochairid,int wincount,int zhuang,byte[] iskaimen,int playerNum)
	{
		if(difen>2)
			difen=2;
		RoundResult[] results=new RoundResult[cardDatas.length];
		for(int i=0;i<results.length;i++)
			results[i]=new RoundResult();
		//统计得分
		int scores[]=new int[results.length];
		for(int i=0;i<results.length;i++)
			countScore(totalResults,results,difen, cardDatas[i], scores, winflags[i], wintypes[i], i, fangpaochairid, wincount,zhuang,iskaimen,playerNum);
		for(int i=0;i<results.length;i++)
			results[i].score=scores[i];
		return results;
	}
	public static void countScore(RoundResult[] totalResults,RoundResult[] results,int difen,CardData cardData,int scores[],int flag,int wintype,int chairid,int fangpaochairid,int wincount,int zhuang,byte[] iskaimen,int playerNum)
	{
		RoundResult result=results[chairid];
		result.result=flag;
		String des="";
		int score=difen;
		int v=wintype;
		int socrebuf[]=new int[scores.length];
		System.out.println("xxxxxx计算分33333333");
		if(check(flag, Define.HP_ZIMO))
		{
			result.result|=v;
			if(v!=0)
			{
				int add=0;
				result.count[RoundResult.ZIMO.index]++;
					score*=2;
					des="自摸 ";
//					
					if(check(flag, Define.HP_BAO_ZHONG_BAO))
					{
						des+="宝中宝";
						score*=16;
						result.count[RoundResult.BAOZHONGBAO.index]++;
					}
					if(check(flag, Define.HP_MO_BAO_HU))
					{
						des+="摸宝胡";
						score*=2;
						result.count[RoundResult.MAOBAOHU.index]++;
					}
					if(check(flag, Define.HP_DA_HU))
					{
						des+="大胡";
						score*=2;
						result.count[RoundResult.JIAHU.index]++;
					}
					if(check(flag, Define.HP_DA_FENG_HU))
					{
						des+="大风胡";
						score*=8;
						result.count[RoundResult.DAFENGHU.index]++;
					}
					if(check(flag, Define.HP_HONG_ZHONG_MAN_TIAN_FEI))
					{
						des+="红中满天飞";
						score*=8;
						result.count[RoundResult.HONGZHONG.index]++;
					}
					if(check(flag, Define.HP_XIAOHU))
					{
						des+="平胡";
						score*=1;
						result.count[RoundResult.PINGHU.index]++;
					}
					if(check(flag, Define.HP_LOU_BAO))
					{
						des+="漏宝";
						score*=16;
						result.count[RoundResult.LOUBAO.index]++;
					}
					if(check(flag, Define.HP_XIA_DA_YU))
					{
						des+="下大雨";
						score*=8;
						result.count[RoundResult.XIADAYU.index]++;
					}
					if(check(flag, Define.HP_DAN_DIAO_JIA))
					{
						des+="单吊夹";
						score*=2;
						result.count[RoundResult.DANDIAO.index]++;
					}
					if(check(flag, Define.HP_ZHI_DUI))
					{
						des+="支对";
						score*=2;
						result.count[RoundResult.ZHIDUI.index]++;
					}
					if(check(flag, Define.HP_BAO_BIAN))
					{
						des+="宝边";//应该不算摸宝胡，要不然漏宝也算摸宝了
						score*=2;
						result.count[RoundResult.BAOBIAN.index]++;
					}
					if(check(flag, Define.HP_SAN_QI_JIA))
					{
						des+="37夹";
						score*=2;
						result.count[RoundResult.SANQIJIA.index]++;
					}
//					
//				if(check(v, Define.HP_PENGPENG_HU)){
//					des+="对碰";
//					score*=2;
//					result.count[RoundResult.DUIDUIHE.index]++;
//				}
//				else 
//					des+="平胡";
//				if(huangzhuang>0)
//					des+=" 荒庄翻倍";
				if (zhuang == chairid) {//庄家得分翻倍
					des+=" 庄家翻倍";
					score*=2;
				}
				score+=add;
				socrebuf[chairid]+=score*(playerNum-1);
				subScore(socrebuf, score, chairid,playerNum);
				
				//门清
				for(int j = 0;j<playerNum;j++){
					if (iskaimen[j] == 0) {
						socrebuf[j]-= score;
						if (socrebuf[j]>-64) {
							socrebuf[chairid]+=score;
						}else {
							socrebuf[j]+= score;
						}
						
					}
					
				}
				
			}
			totalResults[chairid].addDes(des);
			for(int i=0;i<totalResults.length;i++)
			{
				if(i!=chairid)
					totalResults[i].addDes("被自摸");
			}
		}
		else if(check(flag, Define.HP_CHIHU))//||check(flag, Define.HP_QIANG_GANG_HU))
		{
			System.out.println("xxxxxx计算分44444444");
			result.count[RoundResult.ZHUOPAO.index]++;
			result.result|=v;
			String desprefix="";
			String desprefixlose="";
			if(v!=0)
			{
				int add=0;
				boolean baozimo=true;
						if (fangpaochairid != -1) {
							desprefix="捉炮 ";
							desprefixlose="点炮 ";
						}
						if(check(flag, Define.HP_DA_HU))
						{
							des+="大胡";
							score*=2;
							result.count[RoundResult.JIAHU.index]++;
						}
						
						if(check(flag, Define.HP_XIAOHU))
						{
							des+="平胡";
							score*=1;
							result.count[RoundResult.PINGHU.index]++;
						}
						if(check(flag, Define.HP_XIA_DA_YU))
						{
							des+="下大雨";
							score*=8;
							result.count[RoundResult.XIADAYU.index]++;
						}
						if(check(flag, Define.HP_DAN_DIAO_JIA))
						{
							des+="单吊夹";
							score*=2;
							result.count[RoundResult.DANDIAO.index]++;
						}
						if(check(flag, Define.HP_ZHI_DUI))
						{
							des+="支对";
							score*=2;
							result.count[RoundResult.ZHIDUI.index]++;
						}
						if(check(flag, Define.HP_SAN_QI_JIA))
						{
							des+="37夹";
							score*=2;
							result.count[RoundResult.SANQIJIA.index]++;
						}
						
						
						
//					des+="平胡";
				/*if(wincount==3)//一炮三响
				{
					result.result|=Define.HP_YI_PAO_SANXIANG;
					des+=" 一炮三响";
					score*=2;
					result.count[RoundResult.YIPAOSANXIANG.index]++;
				}*/
//				if(baozimo)//包自摸
//					score*=3;
				if (zhuang == chairid) {//庄家得分翻倍
					des+=" 庄家翻倍";
					score*=2;
				}		
				if (fangpaochairid != -1) {
					socrebuf[fangpaochairid]-=score*(playerNum-1);
					socrebuf[chairid]+=(score*(playerNum-1))+add;
					//门清
					for(int j = 0;j<playerNum;j++){
						if (iskaimen[j] == 0) {
							socrebuf[fangpaochairid]-= score;
							socrebuf[chairid]+=score;
						}
					}
					if (socrebuf[fangpaochairid]<-64) {
						socrebuf[fangpaochairid] = -64;
						socrebuf[chairid]+=64;
					}
					
				}else {
					score+=add;
					socrebuf[chairid]+=score*(playerNum-1);
					subScore(socrebuf, score, chairid,playerNum);
					//门清
					for(int j = 0;j<playerNum;j++){
						if (iskaimen[j] == 0) {
							socrebuf[j]-= score;
							if (socrebuf[j]>-64) {
								socrebuf[chairid]+=score;
							}else {
								socrebuf[j]+= score;
							}
							
						}
						
					}
				}
			}
//			if(huangzhuang>0)
//				des+=" 荒庄翻倍";
			
			totalResults[chairid].addDes(desprefix+des);
			if (fangpaochairid != -1) {
				totalResults[fangpaochairid].addDes(desprefixlose+des);
			}
			des=desprefix+des;
			System.out.println("xxxxxx计算分5555555");
		}
		else if(check(flag, Define.HP_DIANPAO))
		{
			des="点炮";
			
			result.count[RoundResult.DIANPAO.index]+=wincount;
		}
		
		if (check(flag, Define.HP_MEN_QING)) {
			des+=" 门清";
			result.count[RoundResult.MENQING.index]++;
		}
		System.out.println("xxxxxx计算分66666666666");
		result.des=des;

		for(int i=0;i<scores.length;i++)
			scores[i]+=socrebuf[i];
		
		if(result.des==null)
			result.des="";
		for(int i=0;i<socrebuf.length;i++)
		{
			int c=socrebuf[i];
			if(i!=chairid)
			{
				if(c<0)
				{
					int abc=-c;
					if(abc>results[i].maxlosescore)
					{
						results[i].maxlosescore=abc;
						results[i].maxloseid=chairid;
					}
					if(abc>result.maxwinscore)
					{
						result.maxwinscore=abc;
						result.maxwinid=i;
					}
				}
			}
		}
		System.out.println("xxxxxx计算分7777777");
	}
	public static Short[] toResultCardid(CardData cardData)
	{
		List<Short>ids=new ArrayList<>();
		for(short id:cardData.norcard)
			ids.add(id);
		Collections.sort(ids);
		//添加组合牌
		for(WeaveCard weaveCard:cardData.weavecard)
		{
			if(ids.size()>0)
				ids.add((short)-8);//-8作为分割
			for(short id:weaveCard.card)
			{
				if(id!=-1)
					ids.add(id);
			}
		}
		//添加进牌
		if(cardData.incard!=null)
			ids.add(cardData.incard);
		else 
			ids.add((short)-1);
		return ids.toArray(new Short[]{});
	}
	/*
	public static boolean isNiuCard(CardData cardData,short cardid)
	{
		int v=cardid/4;
		if(v==DONG||v==NAN||v==XI||v==BEI||v==ZHONG||v==FA||v==BAI||v==JI)
		{
			for(WeaveCard weaveCard:cardData.weavecard)
			{
				if(weaveCard.type==WeaveCard.TYPE_DONG_NAN_JI)
				{
					if(v==DONG||v==NAN||v==JI)
						return true;
				}
				else if(weaveCard.type==WeaveCard.TYPE_DONG_NAN_XI_BEI)
				{
					if(v==DONG||v==NAN||v==XI||v==BEI)
						return true;
				}
				else if(weaveCard.type==WeaveCard.TYPE_ZHONG_FA_BAI)
				{
					if(v==ZHONG||v==FA||v==BAI)
						return true;
				}
			}
		}
		return false;
	}
	*/
	/*public static boolean[] checkPengOption(short card,List<Short>hisopts)
	{
		boolean rs[]=new boolean[]{true,true};
		for(short[] st:hisopts)
		{
			if(st[0]==Option.OP_PENG)
			{
				if(st[1]/4==card/4)
					rs[1]=false;
			}
			else if(st[0]==Option.OP_HU)
				rs[0]=false;
		}
		return rs;
	}*/
	public List<WeaveCard>getShowWeaveCards(List<WeaveCard>weaveCards)
	{
		return weaveCards;
		/*
		List<WeaveCard>weaveCards2=new ArrayList<>();
		for(WeaveCard wCard:weaveCards)
		{
			if(wCard.type==WeaveCard.TYPE_ANGANG)//暗杠，不显示
			{
				WeaveCard w2=new WeaveCard();
				w2.type=WeaveCard.TYPE_ANGANG;
				w2.card[0]=-1;
				w2.card[1]=-1;
				w2.card[2]=-1;
				w2.card[3]=-1;
				w2.centercard=-1;
				w2.chairid=wCard.chairid;
				weaveCards2.add(w2);
			}
			else 
			{
				weaveCards2.add(wCard);
			}
		}
		return weaveCards2;*/
	}
}
