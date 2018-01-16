package com.lebin.game.qdmj;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lebin.game.module.IRecorder;
import com.lebin.game.module.IRoom;
import com.lebin.game.module.IUser;
import com.lebin.game.module.Table;
import com.lebin.game.module.data.Message;
import com.lebin.game.module.data.Player;
import com.lebin.game.qdmj.define.Action;
import com.lebin.game.qdmj.define.CardData;
import com.lebin.game.qdmj.define.Define;
import com.lebin.game.qdmj.define.GameConfig;
import com.lebin.game.qdmj.define.OpItem;
import com.lebin.game.qdmj.define.Option;
import com.lebin.game.qdmj.define.RoundInfo;
import com.lebin.game.qdmj.define.RoundResult;
import com.lebin.game.qdmj.define.TimerTaskMng;
import com.lebin.game.qdmj.define.UserOption;
import com.lebin.game.qdmj.define.Vote;
import com.lebin.game.qdmj.define.WeaveCard;

import atg.taglib.json.util.JSONException;
import atg.taglib.json.util.JSONObject;

public class MaJiangTable implements Table{
	private static final Logger LOG = LoggerFactory.getLogger(MaJiangTable.class.getName());
	//无清一色、十三幺、七小对
	private byte[] m_isKaimen;//玩家是否吃碰开门，否则不能听胡牌，即无天地胡、碰碰胡
	private byte[] m_wanFa;//勾选的玩法选项
	private int m_baopai;//宝牌
	private int[] m_needCard;//胡牌或者听牌需要有幺或者9或者红中
	private Map<String,Boolean>m_playtype;//勾选的玩法
	
	private GameConfig config;
	private Player[] m_players;
	private byte m_gameState;
	private IRoom m_room;
	private int m_chairCount;
	private CardData[] m_cardDatas;//玩家牌数据
	private List<Short>m_cardlist=new ArrayList<>();//当前剩余牌数据
	private byte m_zhuangjia_chairid=-1;//庄家座位号
	private boolean m_user_response[];//响应响应标识
	private RoundInfo m_roundInfo;
	private long m_progress=0;//游戏进度
	private int timeout=15;//超时时间
	private int op_timeout=15;//操作超时时间
	private Logic m_logic;
	private TimerTask m_task=null;
	private TimerTask m_optask=null;
	private List<UserOption> m_Options=new ArrayList<>();
	private Random random;
	//private byte m_cur_gangchairid=-1;//当前杠的玩家座位号
	//private short m_cur_gangcard=-1;//当前杠的牌
	//private byte m_cur_gangtype=-1;//当前杠的类型
	//private byte m_kouting[];//扣听状态
	//private boolean m_niu[];//扭牌状态
	//private boolean m_tiandihu[];//天地胡状态
	private byte m_ting[];
	private int m_jiaohu[];//叫胡状态
	//private boolean m_haidilao=false;
	//private boolean m_op_lock[];
	//private byte[] m_chicount;//吃牌次数
	private Map<String,Object>m_result;
	private RoundResult m_PlayerResults[];
	private byte sendTailChairid=-1;
	private Vote m_vote;
	private boolean vote=false;
	private int VOTETIMEOUT=180000;
	private long m_voteendtime;
	private ScheduledFuture m_voteTask;
	private int m_round=0;
	private byte m_winner=-1;
	private List<Short>m_userPengCards[];
	private List<Short>m_notincludes[];
	private String recordfile;
	private static SimpleDateFormat format=new SimpleDateFormat("yyyyMMdd");
	private static SimpleDateFormat format2=new SimpleDateFormat("HHmmss");
	private static SimpleDateFormat format3=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private long m_userPregress[];
	private static Gson GSON=new Gson();
	private String savepath;
	StringBuffer recordbuffer;
	private IRecorder recorder;
	private int opsize=0;
	private int m_votecharidid=-1;
	private long syntime=0;
	private boolean isover=false;
//	private short m_curincard=-1;//当前进牌
	@Override
	public void onInit(IRoom room,Object params) {
		config=GameConfig.create(params.toString(), GameConfig.class);
		this.m_room=room;
		//m_chairCount=config.minplayer;
		random = new Random();
		
		try {
			JSONObject par=new JSONObject(params.toString());
			JSONObject playt=new JSONObject(par.getString("extra"));
			String[] playstr = playt.getString("playtp").split(",");
			m_chairCount=new JSONObject(par.getString("extra")).getInt("playerNum");//几人房
			m_logic=new Logic(m_chairCount);
			m_playtype = new HashMap<>();
			m_playtype.put("dafeng", false);//刮大风
			m_playtype.put("hzmtf", false);//红中满天飞
			m_playtype.put("zhidui", false);//支对
			m_playtype.put("loubao", false);//漏宝
			m_playtype.put("dandiaojia", false);//单调夹
			m_playtype.put("xiadayu", false);//下大雨
			m_playtype.put("shanggunbao", false);//上滚宝
			m_playtype.put("xiagunbao", false);//下滚宝
			m_playtype.put("tongbao", false);//通宝
			m_playtype.put("shanggundafeng", false);//上滚大风
			m_playtype.put("xiagundafeng", false);//下滚大风
			m_playtype.put("37jia", false);//37jia
			m_logic.setPlayType(m_playtype);
			for(String t:playstr){
				if (t.equals("0")) 
					m_playtype.put("dafeng", true);
				if (t.equals("1")) 
					m_playtype.put("hzmtf", true);
				if (t.equals("2")) 
					m_playtype.put("zhidui", true);
				if (t.equals("3")) 
					m_playtype.put("loubao", true);
				if (t.equals("4")) 
					m_playtype.put("dandiaojia", true);
				if (t.equals("5")) 
					m_playtype.put("xiadayu", true);
				if (t.equals("6")) 
					m_playtype.put("shanggunbao", true);
				if (t.equals("7")) 
					m_playtype.put("xiagunbao", true);
				if (t.equals("8")) 
					m_playtype.put("tongbao", true);
				if (t.equals("9")) 
					m_playtype.put("shanggundafeng", true);
				if (t.equals("10")) 
					m_playtype.put("xiagundafeng", true);
				if (t.equals("11")) 
					m_playtype.put("37jia", true);
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_players=new Player[m_chairCount];
		m_gameState=Define.GAME_STATE_REAY;//设置当前游戏状态等待状态
		
		m_roundInfo=new RoundInfo();
		m_user_response=new boolean[m_chairCount];
		m_cardDatas=new CardData[m_chairCount];
		m_ting = new byte[m_chairCount];
		//m_niu=new boolean[m_chairCount];
		//m_tiandihu=new boolean[m_chairCount];
		//m_kouting=new byte[m_chairCount];
		//m_chicount=new byte[m_chairCount];
		m_isKaimen = new byte[m_chairCount];
		m_jiaohu=new int[m_chairCount];
		m_userPregress=new long[m_chairCount];
		m_PlayerResults=new RoundResult[m_chairCount];
		m_vote=new Vote(m_chairCount);
		m_userPengCards=new List[m_chairCount];
		m_notincludes=new List[m_chairCount];
		//m_op_lock=new boolean[m_chairCount];
		for(int i=0;i<m_chairCount;i++)
		{
			m_PlayerResults[i]=new RoundResult();
			m_userPengCards[i]=new ArrayList<>();
			m_notincludes[i]=new ArrayList<>();
		}	
		Date date=new Date();
		savepath=format.format(date)+"/"+room.getRoomid()+format2.format(date);
	}
	public void setRecorder(IRecorder recorder)
	{
		this.recorder=recorder;
	}
	@Override
	public boolean onUserSitDown(int chairId, IUser user) {
		if(m_gameState==Define.GAME_STATE_REAY&&checkEmptyChair(chairId))
		{
			m_players[chairId]=(Player)user;
			m_players[chairId].state=Define.USER_STATE_NOTREADY;
			return true;
		}
		return false;
	}
	@Override
	public boolean onUserStandUp(int chairId, IUser user) {
		if(!checkChair(chairId, user))
			return false;
		m_players[chairId]=null;
		return true;
	}

	@Override
	public int getChairCount() {
		return m_players==null?0:m_chairCount;
	}
	@Override
	public IUser createUser(Object data) {
		return Player.create(data.toString(), Player.class);
	}
	/**
	 * 检查座位用户是否一致
	 * @param chairId
	 * @param user
	 * @return
	 */
	private boolean checkChair(int chairId,IUser user)
	{
		if(chairId<0||chairId>m_chairCount-1||m_players[chairId]==null||!user.getId().equals(user.getId()))
			return false;
		return true;
	}
	/**
	 * 检查座位是否是空的
	 * @param chairId
	 * @return
	 */
	private boolean checkEmptyChair(int chairId)
	{
		if(chairId<0||chairId>m_chairCount-1||m_players[chairId]!=null)
			return false;
		return true;
	}
	/**
	 * 玩家准备
	 */
	@Override
	public boolean onReady(long progress,int chairId, IUser user) {
		if(!checkChair(chairId,user)||progress!=m_progress)
			return false;
		try {
			m_players[chairId].state|=Define.USER_STATE_READY;
			return true;
		} finally {
			onUserReady(progress,chairId,user);
		}
		
	}
	/**
	 * 玩家取消准备
	 */
	@Override
	public boolean onCancelReady(int chairId, IUser user) {
		if(!checkChair(chairId,user))
			return false;
		if((m_players[chairId].state&Define.USER_STATE_READY)==Define.USER_STATE_READY)
			m_players[chairId].state^=Define.USER_STATE_READY;
		return true;
	}
	/**
	 * 玩家退出
	 */
	@Override
	public boolean onUserExit(int chairId, IUser user) {
		if(m_gameState!=Define.GAME_STATE_REAY||!checkChair(chairId, user))
			return false;
		m_players[chairId]=null;
		return true;
	}
	/**
	 * 玩家掉线
	 */
	@Override
	public boolean onUserOffLine(int chairId, IUser user) {
		if(!checkChair(chairId,user))
			return false;
		m_players[chairId].state|=Define.USER_STATE_OFFLINE;
		return true;
	}
	/**
	 * 玩家重入
	 */
	@Override
	public boolean onUserConnect(int chairId, IUser user) {
		if(!checkChair(chairId,user))
			return false;
		if((m_players[chairId].state&Define.USER_STATE_OFFLINE)==Define.USER_STATE_OFFLINE)
			m_players[chairId].state^=Define.USER_STATE_OFFLINE;
		return true;
	}
	@Override
	public IUser getUser(int chairId) {
		if(chairId<0||chairId>m_chairCount-1)
			return null;
		return m_players[chairId];
	}
	private  void onUserReady(long progress,int charid,IUser user)
	{
		List<IUser>users=new ArrayList<>();
		synchronized (m_players) {
			for(int i=0;i<m_players.length;i++)
			{
				if(m_players[i]==null)
				{
					LOG.debug("人员未就位");
					return;//人员未就位
				}
				if(m_players[i].isOffline())
					users.add(m_players[i]);
			}
		}
		LOG.debug("人员已就位");
		if(!users.isEmpty())
		{
			LOG.debug("人员不在线");
			m_room.notifyBack(users);
		}
		if(m_gameState==Define.GAME_STATE_REAY)
		{
			onGameStart();
			m_room.onGameStart();
		}
	}
	
	private boolean setAndReturnOpReady(long progress,int chairid,IUser user)
	{
		if(progress!=m_progress)
		{
			LOG.warn(user.getId()+"状态不同步");
			sendGameScene(progress, chairid, user);
			return false;
		}
		m_user_response[chairid]=true;
		for(int i=0;i<m_user_response.length;i++)
		{
			if(!m_user_response[i])
				return false;
		}
		return true;
	}
	private void resetResponse()
	{
		Arrays.fill(m_user_response, false);
	}
	private  void onUserOpReady(long progress,int chairid,IUser user,JsonObject data)
	{
		switch (m_gameState) {
		/*case Define.GAME_STATE_DICE://投骰子中
		{
			if(!setAndReturnOpReady(progress, chairid, user))
			{
				LOG.info("人员未响应");
				return;
			}
			startDispath();//发牌
		}
			break;*/
		case Define.GAME_STATE_PLAY://游戏中
			handlerResponse(progress,chairid,user,data);
			break;
		case Define.GAME_STATE_ROUND_OVER:
			synchronized (m_user_response) {
				if(!setAndReturnOpReady(progress, chairid, user))
				{
					LOG.debug("人员未响应");
					return;
				}
				onGameStart();
			}
			break;
		default:
			break;
		}
	}
	private void cancelVoteTask()
	{
		if(m_voteTask!=null)
		{
			m_voteTask.cancel(true);
			m_voteTask=null;
		}
	}
	private void startVoteTask()
	{
		cancelVoteTask();
		Runnable task=new Runnable() {
			
			@Override
			public void run() {
				vote=false;
				m_voteTask=null;
				LOG.debug("投票长时间无结果，自动解散房间");
				voteFreeRoom();
			}
		};
		m_voteTask=TimerTaskMng.schedule(task, VOTETIMEOUT);
	}
	private void onVote(int chairid,boolean agree)
	{
		if(isover||m_gameState==Define.GAME_STATE_OVER||m_gameState==Define.GAME_STATE_REAY)
			return;
		if(!vote)
		{
			vote=true;
			m_vote.start();
			m_voteendtime=System.currentTimeMillis()+VOTETIMEOUT;
			startVoteTask();
			m_votecharidid=chairid;
			LOG.info("["+m_room.getRoomid()+"]"+m_players[chairid].uid+"-"+m_players[chairid].nickname+" 发起投票");
		}
		m_vote.vote(chairid, agree);
		m_vote.lefttime=(m_voteendtime-System.currentTimeMillis())/1000;
		if(m_vote.lefttime<0)
			m_vote.lefttime=0;
		m_progress++;
		Map<String, Object>data=new HashMap<>();
		data.put("op", Define.OP_VOTE);
		data.put("progress", m_progress);
		data.put("vote",m_vote);
		data.put("chairid",chairid);
		if(m_vote.state==1)
		{
			cancelVoteTask();
			LOG.debug("投票结果：同意解散房间");
		}
		else if(m_vote.state==-1)
		{
			m_votecharidid=-1;
			cancelVoteTask();
			LOG.debug("投票结果：拒绝解散房间");
		}
		else 
		{
			LOG.debug("还有人没表决");
		}
		recordAction(data,false,false);
		for(int i=0;i<m_players.length;i++)
			sendPlayMessage(i, data);
		if(m_vote.state==1)
		{
			if(m_gameState!=Define.GAME_STATE_REAY)
			{
				voteFreeRoom();
			}
		}
		else if(m_vote.state==-1)
		{
			vote=false;
			m_votecharidid=-1;
		}
	}
	private void voteFreeRoom()
	{
		if(isover)
			return;
		isover=true;
		LOG.debug("解散房间");
		cancelOpTimeTask();
		cancelTimeCount();
		cancelVoteTask();
		if(m_gameState==Define.GAME_STATE_ROUND_OVER)
		{
			m_gameState=Define.GAME_STATE_OVER;
			Map<String, Object>resultdata=new HashMap<>();
			resultdata.put("progress", m_progress);
			resultdata.put("op", Define.OP_GAME_OVER);
			Map<String, Object>reulutitems[]=new HashMap[m_chairCount];
			for(int i=0;i<m_chairCount;i++)
			{
				Map<String, Object>item=new HashMap<>();
				item.put("detail", m_PlayerResults[i].toItems());
				item.put("chairid", i);
				item.put("score", m_players[i].score);
				item.put("master", m_players[i].getId().equals(m_room.getMasterId()));
				reulutitems[i]=item;
			}
			resultdata.put("result", reulutitems);
			m_result=resultdata;
			recordAction(resultdata,false,false);
			for(int i=0;i<m_players.length;i++)
			{
				sendPlayMessage(i, resultdata);
			}
			m_room.onGameOver(m_PlayerResults,2,m_votecharidid);
			return;
		}
		m_gameState=Define.GAME_STATE_OVER;
		List<UserOption>huoptions=new ArrayList<>();
		for(UserOption userOption:m_Options)
		{
			if(userOption.op==Option.OP_HU)
				huoptions.add(userOption);
		}
		m_Options.clear();
		boolean flag=false;
		if(huoptions.size()>0)
		{
			//胡了
			LOG.debug("胡了"+huoptions.size());
			flag=hupai(huoptions,true);
		}
		if(!flag)
		{
			m_roundInfo.curaction=Action.ACT_NONE;
			m_gameState=Define.GAME_STATE_OVER;
			m_progress++;
			countScore(new int[m_chairCount], new int[m_chairCount], 0, (byte)-1,true);
		}
	}
	private boolean isXiaZhuang()
	{
		return m_winner!=-1&&m_winner!=m_roundInfo.zhuangjiachairid;
	}
	private boolean initGame()
	{
		if(m_round>config.maxround)
		{
			
			return false;
		}
		else if(m_round==config.maxround)
		{
			if(isXiaZhuang())//下庄
				return false;
		}
		m_progress++;
		m_roundInfo.curround++;
		m_logic.setBaoPai(random.nextInt(28));
		Arrays.fill(m_user_response, false);
		//Arrays.fill(m_niu, false);//可以扭牌
		//Arrays.fill(m_tiandihu, false);//可以天地胡
		//Arrays.fill(m_kouting, (byte)0);//可扣听
		//Arrays.fill(m_chicount,(byte)0);
		Arrays.fill(m_ting,(byte)0);
		Arrays.fill(m_isKaimen,(byte)0);
		Arrays.fill(m_jiaohu, 0);
		//Arrays.fill(m_op_lock,false);
		for(int i=0;i<m_players.length;i++)
		{
			m_players[i].reset();
			m_userPengCards[i].clear();
			m_notincludes[i].clear();
		}
		cancelTimeCount();
		cancelOpTimeTask();
		//m_haidilao=false;
		//resetGangInfo();
		if(m_roundInfo.curround==1)//第一局
		{
			m_round++;
			m_zhuangjia_chairid=-1;
			m_roundInfo.difen=1;
			m_roundInfo.lianzhuang=0;
			m_roundInfo.huangzhuang=0;
		}
		else if(m_winner==-1||m_winner==m_roundInfo.zhuangjiachairid)//荒庄或者连庄
		{
			if(m_winner==-1)
			{
				m_roundInfo.difen=2;//荒庄，底分翻倍
				m_roundInfo.huangzhuang++;
			}
			m_roundInfo.lianzhuang++;
		}
		else 
		{
			m_roundInfo.difen=1;
			m_round++;
			m_zhuangjia_chairid=m_zhuangjia_chairid==-1?-1:(byte)((m_roundInfo.zhuangjiachairid+1)%m_chairCount);
			m_roundInfo.lianzhuang=0;
			m_roundInfo.huangzhuang=0;
		}
		m_roundInfo.curcircle=(byte)((m_round-1)/4+1);
		m_roundInfo.des=String.format("第%d圈(%d/%d) 第%d局", m_roundInfo.curcircle,(m_round-1)%4+1,4,m_roundInfo.curround);
		if(m_roundInfo.lianzhuang>0)
			m_roundInfo.des+=String.format(" 连庄%d次", m_roundInfo.lianzhuang);
		if(m_roundInfo.huangzhuang>0)
			m_roundInfo.des+=String.format(" 荒庄%d次", m_roundInfo.huangzhuang);
		m_logic.shuffleCard(m_roundInfo,m_cardDatas,m_cardlist,m_zhuangjia_chairid);
		m_zhuangjia_chairid=m_roundInfo.zhuangjiachairid;
		m_roundInfo.curoutcard=-1;
		m_roundInfo.curchairid=-1;
		m_Options.clear();
		m_roundInfo.curaction=Action.ACT_SEND_DICE;
		return true;
	}
	private void onGameStart()
	{
		cancelOpTimeTask();
		cancelTimeCount();
		//cancelVoteTask();
		m_gameState=Define.GAME_STATE_DICE;//进入投骰子状态
		if(initGame())
		{
			if(sendStart())
			{
				startRecord();
				TimerTaskMng.schedule(new TimerTask() {
					
					@Override
					public void run() {
						startDispath();//发牌
						
					}
				}, 3000);
				
			}
		}
	}
	
	/**
	 * 游戏开始，发送骰子和庄家数据
	 */
	private boolean sendStart()
	{
		if(m_roundInfo.curaction!=Action.ACT_SEND_DICE)
			return false;
		Map<String, Object>data=new HashMap<>();
		Map<String, Object>dice=new HashMap<>();
		dice.put("zhuangjiachairid", m_zhuangjia_chairid);
		dice.put("dian1", m_roundInfo.dianshu1);
		dice.put("dian2", m_roundInfo.dianshu2);
		dice.put("curround", m_roundInfo.curround);
		dice.put("curcircle", m_roundInfo.curcircle);
		dice.put("des", m_roundInfo.des);
		data.put("progress", m_progress);
		data.put("dice", dice);
		Message msg=new Message();
		msg.what="gamestart";
		msg.data=data;
		for(int i=0;i<m_chairCount;i++)
		{
			m_user_response[i]=false;//设置响应
			m_room.sendMessage(i, m_players[i], msg);
		}
		return true;
	}
	@Override
	public void onMessage(int chairid, IUser user, Object data) {
		JsonObject json=(JsonObject)data;
		int msg=json.get("msg").getAsInt();
		switch(msg)
		{
			case Define.OP_READY://已就绪，执行下一步
				onUserOpReady(json.get("progress").getAsLong(), chairid, user,json);
			break;
			case Define.OP_OUT_CARD://出牌
				onUserOutCard(chairid,user,json);
				break;
			case Define.OP_SELECT://做出选择
				onUserSelect(chairid,user,json);
				break;
			case Define.OP_VOTE:
				onVote(chairid,json.get("agree").getAsBoolean());
				break;
		}
	}
	/**
	 * 发送场景数据
	 * @param progress 当前客户端进度
	 * @param charid
	 * @param user
	 */
	@Override
	public boolean sendGameScene(long progress,int chairid, IUser user) {
		
		/*if(progress!=0&&progress==m_progress)
			return false;*/
		Map<String, Object> data=null;
		switch(m_gameState)
		{
			case Define.GAME_STATE_REAY:
				data=getReadySceneData(chairid, user);
				break;
			case Define.GAME_STATE_DICE:
				data=getDiceSceneData(chairid, user);
				break;
			case Define.GAME_STATE_PLAY:
				data=getPlayingSceneData(chairid, user);
				break;
			case Define.GAME_STATE_ROUND_OVER:
				data=getRoundOverSceneData(chairid, user);
				break;
			case Define.GAME_STATE_OVER:
				data=getGameOverSceneData(chairid, user);
				break;
			default:
				break;
		}
		if(data!=null)
		{
			data.put("progress", m_progress);
			if(vote)
			{
				
				long lefttime=(m_voteendtime-System.currentTimeMillis())/1000;
				if(lefttime<0)
					lefttime=0;
				m_vote.lefttime=lefttime;
				data.put("isvote", vote);
				data.put("vote", m_vote);
			}
			return m_room.sendGameScene(chairid, user, data);
		}
		return false;
	}
	private List<Player>getPlayers()
	{
		List<Player>players=new ArrayList<>();
		for(Player player:m_players)
		{
			if(player!=null)
				players.add(player);
		}
		return players;
	}
	private Map<String, Object> getReadySceneData(int chairid,IUser user)
	{
		Map<String, Object>data=new HashMap<>();
		data.put("gamestate", Define.GAME_STATE_REAY);
		data.put("users", getPlayers());
		return data;
	}
	private Map<String, Object> getDiceSceneData(int chairid,IUser user)
	{
		Map<String, Object>data=new HashMap<>();
		data.put("gamestate", Define.GAME_STATE_DICE);
		data.put("users", getPlayers());
		Map<String, Object>dice=new HashMap<>();
		dice.put("zhuangjiachairid", m_zhuangjia_chairid);
		dice.put("dian1", m_roundInfo.dianshu1);
		dice.put("dian2", m_roundInfo.dianshu2);
		dice.put("curround", m_roundInfo.curround);
		dice.put("curcircle", m_roundInfo.curcircle);
		dice.put("des", m_roundInfo.des);
		data.put("progress", m_progress);
		data.put("dice", dice);
		return data;
	}
	private Map<String, Object> getPlayingSceneData(int chairid,IUser user)
	{
		Map<String, Object>data=new HashMap<>();
		data.put("gamestate", Define.GAME_STATE_PLAY);
		data.put("users", getPlayers());
		data.put("roundinfo", m_roundInfo);
		List<Map<String, Object>>carddatas=new ArrayList<>();
		data.put("cards", carddatas);
		for(int j=0;j<m_chairCount;j++)
		{
			Map<String, Object>otdata=new HashMap<>();
			if(j==chairid)
			{
				otdata.put("norcard",m_cardDatas[j].norcard);
				otdata.put("weavecard", m_cardDatas[j].weavecard);
				/*Option option=new Option();
				option.id=1;
				option.info=1;
				m_cardDatas[j].ops.add(option);*/
				otdata.put("ops", m_cardDatas[j].ops);
				otdata.put("lmtid", m_cardDatas[j].lmtid);
				otdata.put("in", m_cardDatas[j].incard);
			}
			else 
			{
				short[] cards=new short[m_cardDatas[j].norcard.size()];
				Arrays.fill(cards, (short)-1);
				otdata.put("norcard",cards);
				otdata.put("weavecard", m_logic.getShowWeaveCards(m_cardDatas[j].weavecard));
				otdata.put("in", m_cardDatas[j].incard!=null?-1:null);
			}
			otdata.put("out", m_cardDatas[j].out);
			otdata.put("chairid", j);
			carddatas.add(otdata);
		}
		return data;
	}
	private Map<String, Object> getRoundOverSceneData(int chairid,IUser user)
	{
		Map<String, Object>data=new HashMap<>();
		data.put("users", getPlayers());
		data.put("gamestate", Define.GAME_STATE_ROUND_OVER);
		data.put("result", m_result);
		return data;
	}
	private Map<String, Object> getGameOverSceneData(int chairid,IUser user)
	{
		Map<String, Object>data=new HashMap<>();
		data.put("users", getPlayers());
		data.put("gamestate", Define.GAME_STATE_OVER);
		data.put("result", m_result);
		return data;
	}
	/**
	 * 发送手牌
	 */
	private void sendHandCard()
	{
		for(int i=0;i<m_chairCount;i++)
		{
			Message message=new Message();
			message.what="dispatch";
			Map<String, Object>data=new HashMap<>();
			message.data=data;
			data.put("roundinfo", m_roundInfo);
			List<Map<String, Object>>carddatas=new ArrayList<>();
			data.put("cards", carddatas);
			data.put("progress", m_progress);
			for(int j=0;j<m_chairCount;j++)
			{
				Map<String, Object>carddata=new HashMap<>();
				if(i==j)
					carddata.put("norcard",m_cardDatas[i].norcard);
				else 
				{
					short[] cards=new short[m_cardDatas[j].norcard.size()];
					Arrays.fill(cards, (short)-1);
					carddata.put("norcard",cards);
				}
				carddata.put("out",m_cardDatas[i].out);
				carddata.put("chairid", j);
				carddata.put("weavecard",m_cardDatas[i].weavecard);
				carddatas.add(carddata);
			}
			m_room.sendMessage(i, m_players[i], message);
			
		}
		
	}
	private void handlerResponse(long progress, int chairid, IUser user, JsonObject data) {
		if(progress!=m_progress)
		{
			LOG.warn(user.getId()+"状态不同步，重新同步场景");
			sendGameScene(progress, chairid, user);
			return;
		}
		
	}
	private void startDispath()
	{
		resetResponse();
		m_gameState=Define.GAME_STATE_PLAY;//进入游戏状态
		m_progress++;
		m_roundInfo.curchairid=-1;
		sendHandCard();
		dipatchNext();
	}
	private void dipatchNext()
	{
		dispatchCard();
	/*	TimerTaskMng.schedule(new TimerTask() {
			
			@Override
			public void run() {
				dispatchCard();
			}
		}, 300);*/
	}
	private void dipatchTailDelay(final byte chairid)
	{
		dispatchTailCard(chairid);
		/*TimerTaskMng.schedule(new TimerTask() {
			
			@Override
			public void run() {
				dispatchTailCard(chairid);
			}
		}, 300);*/
		
	}
	private synchronized void cancelTimeCount()
	{
		if(m_task!=null)
		{
			m_task.cancel();
			m_task=null;
		}
	}
	private synchronized void startTimeCount()
	{
		/*cancelTimeCount();
		m_task=new TimerTask() {
			
			@Override
			public void run() {
				m_roundInfo.lefttime-=1;
				if(m_roundInfo.lefttime<=0)
				{
					m_roundInfo.lefttime=0;
					cancelTimeCount();
				}
				
			}
		};
		TimerTaskMng.TIMER.schedule(m_task, 1000, 1000);*/
	}
	private synchronized void startOpTimeTask()
	{
		/*cancelOpTimeTask();
		m_optask=new TimerTask() {
			
			@Override
			public void run() {
				m_optask=null;
				doNextOption();
			}
		};
		TimerTaskMng.TIMER.schedule(m_optask, op_timeout*1000);*/
	}
	private synchronized void cancelOpTimeTask()
	{
		if(m_optask!=null)
		{
			m_optask.cancel();
			m_optask=null;
		}
	}
	private void huangZhuang()
	{
		m_gameState=Define.GAME_STATE_ROUND_OVER;
		m_roundInfo.curaction=Action.ACT_NONE;
		countScore(new int[m_chairCount], new int[m_chairCount], 0, (byte)-1,false);
		
	}
	private void dispatchCard(byte chairid,short card)
	{
		dispatchCard(chairid,card,false);
	}
	private void dispatchCard(byte chairid,short card,boolean starthaidilao)
	{
		sendTailChairid=-1;
		m_progress++;
		changeCurrentPlayer(chairid);
		m_roundInfo.curoutcard=-1;
		if(m_cardDatas[m_roundInfo.curchairid].incard!=null)
			m_cardDatas[m_roundInfo.curchairid].norcard.add(m_cardDatas[m_roundInfo.curchairid].incard);
		m_cardDatas[m_roundInfo.curchairid].incard=card;
		m_roundInfo.curaction=Action.ACT_WAIT_OUT_CARD;//m_haidilao?Action.ACT_NONE:
		m_roundInfo.lefttime=timeout;//进牌了，操作锁定解除
//		if(m_kouting[chairid]==1)
//		{
//			if((m_jiaohu[chairid]&1)==1)
//				m_jiaohu[chairid]^=1;
//		}
//		else
//		{
			m_jiaohu[chairid]=0;
//		}
		//m_op_lock[chairid]=false;
//		if(m_cur_gangchairid==chairid)//&&m_cur_gangtype!=WeaveCard.TYPE_ANGANG)
//		{
//			m_notincludes[chairid].add(card);
//		}
		boolean flag=false;
		for(int i=0;i<m_players.length;i++)
		{
			if(i!=m_roundInfo.curchairid)
				m_cardDatas[i].incard=null;
			Map<String, Object>data=new HashMap<>();
			data.put("progress", m_progress);
			data.put("op", Define.OP_IN_CARD);
			data.put("chairid", m_roundInfo.curchairid);
			data.put("cardid", i==m_roundInfo.curchairid?card:-1);
			data.put("frontremove", m_roundInfo.frontremove);
			data.put("tailremove", m_roundInfo.tailremove);
			data.put("leftcount",  m_roundInfo.leftcount);
			data.put("lefttime", m_roundInfo.lefttime);
			//data.put("haidilao", m_haidilao);//海底捞不能出
			if(starthaidilao)
				data.put("starthaidilao", true);
			if(i==m_roundInfo.curchairid)
			{
				//判断有没有胡，或者杠，听
				flag=updateCurrentPlayerCardOption(i,card,true);
				recordAction(data,true,false);
				if(flag){
					data.put("ops", m_cardDatas[i].ops);
				}
			}
			sendPlayMessage(i,data);
		}
//		if(m_haidilao)
//		{
//			if(!flag)
//				dipatchNext();
//			else 
//				m_roundInfo.curaction=Action.ACT_WAIT_OP;
//		}
//		else
			startTimeCount();
	}
	private short getLeftCardCount(){
		short size=(short)m_cardlist.size();
		if(m_roundInfo.tailremove>0)
		{
			int rmcount=(m_roundInfo.tailremove+6);
			
			return (short)(size-rmcount*2-m_roundInfo.tailremove%2);
		}
		return (short)(size-14);
	}
	/*
	 * 判断是否能杠
	 */
	/*
	private boolean isCanGang()
	{
		short size=(short)m_cardlist.size();
		int rm=m_roundInfo.tailremove+1;
		int rmcount=(rm+6);
		return (size-rmcount*2-rm%2-1)>=4;
	}
	*/
	private synchronized void dispatchCard()
	{
		byte chairid=m_roundInfo.curchairid<0?m_roundInfo.zhuangjiachairid:((byte)((m_roundInfo.curchairid+1)%m_chairCount));
		dispatchFrontCard(chairid);
	}
	private synchronized void dispatchFrontCard(byte chairid)
	{
		cancelTimeCount();
		short count=getLeftCardCount();
		if(count==0)
		{
			huangZhuang();
			return;
		}
		boolean flag=false;
//		if(count<=4)
//		{
//			if(!m_haidilao)
//			{
//				m_haidilao=true;
//				flag=true;
//				sendStartHaiDiLao();
//			}
//		}
		short card=m_cardlist.remove(0);
		m_roundInfo.frontremove++;
		m_roundInfo.leftcount=getLeftCardCount();
		dispatchCard(chairid, card,flag);
	}
	private synchronized void dispatchTailCard(byte chairid)
	{
		/*short count=getLeftCardCount();
		if(count<=4)
		{
			dispatchFrontCard(chairid);
			return;
		}*/
		cancelTimeCount();
		m_roundInfo.tailremove++;
		short card=m_cardlist.remove(m_cardlist.size()-1);
		m_roundInfo.leftcount=getLeftCardCount();
		dispatchCard(chairid,card);
	}
	private void sendPlayMessage(int chairid,Object data)
	{
		Message message=new Message();
		message.what="play";
		message.data=data;
		Player player=m_players[chairid];
		m_room.sendMessage(chairid, player, message);
	}
	private void clearOptions()
	{
		for(int i=0;i<m_cardDatas.length;i++)
			m_cardDatas[i].ops.clear();
	}
	private boolean updateCurrentPlayerCardOption(int chairid,short cardid,boolean isin)
	{
		boolean flag=false;
		//boolean iscangang=isCanGang();
		for(int i=0;i<m_cardDatas.length;i++)
		{
			m_cardDatas[i].ops.clear();
			if(chairid==i)
			{
				boolean rs=false;
				if(isin && m_ting[i] !=0)
				{
					//进牌可以胡
					rs=add(m_cardDatas[i].ops, Logic.getHuOption(m_cardDatas[i],-1,m_logic.getPlayType(),m_logic.getBaoPai()));
					if(rs)
						m_jiaohu[i]|=1;
				}
//				if(!m_haidilao)//海底捞,只能自摸
//				{
				if(m_ting[i]!=1)//扣听只能自摸和调牌
				{
//						if(iscangang)
//							rs=add(m_cardDatas[i].ops, Logic.getGangOption(m_cardDatas[i], (byte)i, cardid, true))||rs;
					//if(m_niu[chairid])//可以扭
						//rs=add(m_cardDatas[i].ops, Logic.getNiuOption(m_cardDatas[i],iscangang,m_notincludes[i]))||rs;
					if(m_isKaimen[i]!=0)//没有扣听，可以检测是否可以扣听,必须要吃或碰一组牌
						rs=add(m_cardDatas[i].ops, Logic.getTing(m_cardDatas,(byte)i))||rs;
//					if (m_logic.getPlayType().get("zhidui") ){//&& m_isKaimen[i]!= 0) {
//						rs=add(m_cardDatas[i].ops,  m_logic.getZhiDuiOption(m_cardDatas[i]))||rs;
//					}
					
				}
					//if(iscangang)//有充足的牌可以调牌
						//rs=add(m_cardDatas[i].ops, Logic.getTiaoOption(m_cardDatas[i],m_kouting[i]==1))||rs;
//				}
				flag=flag||rs;
			}
		}
		return flag;
	}
	private Map<Integer, Object>getAllPlayerOption()
	{
		Map<Integer,Object>rs=new HashMap<>();
		for(int i=0;i<m_chairCount;i++)
		{
			rs.put(i, m_cardDatas[i].ops);
		}
		return rs;
	}
	private Map<Byte, Object>getAllPlayerSelect()
	{
		Map<Byte,Object>rs=new HashMap<>();
		for(UserOption option:m_Options)
		{
			rs.put(option.chairid, option.op);
		}
		return rs;
	}
	private boolean updateOnOutCardOption(int chairid,short cardid,boolean tiao)
	{
		boolean flag=false;
		//boolean iscangang=isCanGang();
		//short cardindex=(short)(cardid/4);
		for(int i=0;i<m_cardDatas.length;i++)
		{
			m_cardDatas[i].ops.clear();
			m_cardDatas[i].lmtid=null;
			if(chairid==i)
			{
				m_user_response[i]=true; 
				m_cardDatas[i].lastout=cardid;
				continue;
			}
			boolean rs=false;
			System.out.println("xxxxx进入出牌操作1");
			if(m_ting[i]!=1)
			{
				System.out.println("xxxxx进入出牌操作2");
				//boolean pengable=!m_userPengCards[i].contains(cardindex);
				//if(!m_op_lock[i])
//				{
				if(m_isKaimen[i]!=0)//没有扣听，可以检测是否可以扣听,必须要吃或碰一组牌
					rs=add(m_cardDatas[i].ops, Logic.getTing(m_cardDatas,(byte)i))||rs;
				
					if(m_cardDatas[i].norcard.size()>=7){
						if((chairid+1)%m_chairCount==i)
						rs=add(m_cardDatas[i].ops, Logic.getChiOption(m_cardDatas[i],cardid))||rs;
						
						boolean p=add(m_cardDatas[i].ops, Logic.getPengOption(m_cardDatas[i], cardid));
						rs=p||rs;
						if(p){
							System.out.println("XXXXXXXXXX检测碰牌成功::"+m_players[chairid].nickname);
							m_userPengCards[i].add((short)(cardid/4));
						}
					}
//					
//					if(iscangang)
//						rs=add(m_cardDatas[i].ops, Logic.getGangOption(m_cardDatas[i], (byte)i, cardid, false))||rs;
//					if((m_jiaohu[i]==1&&m_cardDatas[i].norcard.size()>=4))//if(m_jiaohu[i]==0||(m_jiaohu[i]==1&&m_cardDatas[i].norcard.size()==1))
//					{
//						boolean hu=add(m_cardDatas[i].ops, Logic.getHuOption(m_cardDatas[i],cardid));
//						rs=hu||rs;
//						if(hu)
//							m_jiaohu[i]|=2;
//					}
//				}
			}
			else 
			{
//				if((m_cardDatas[i].norcard.size()>=4))//if(m_jiaohu[i]==0||(m_jiaohu[i]==1&&m_cardDatas[i].norcard.size()==1))
//				{
					boolean hu=add(m_cardDatas[i].ops, Logic.getHuOption(m_cardDatas[i],cardid,m_logic.getPlayType(),m_logic.getBaoPai()));
					rs=hu||rs;
//				}
//				if(m_jiaohu[i]==0 &&)
//				{
//					boolean hu=add(m_cardDatas[i].ops, Logic.getHuOption(m_cardDatas[i],cardid));
//					rs=hu||rs;
//					if(hu)
//						m_jiaohu[i]=2;
//				}
			}
			m_user_response[i]=!rs;
			flag=flag||rs;
		}
		return flag;
	}
	/**
	 * 更新杠后操作
	 * @param chairid
	 * @param cardid
	 * @return
	 */
	/*
	private boolean updateGangOption(int chairid,short cardid)
	{
		boolean flag=false;
		for(int i=0;i<m_cardDatas.length;i++)
		{
			m_cardDatas[i].ops.clear();
			if(chairid==i)
			{
				m_user_response[i]=true;
				continue;
			}
			if(m_jiaohu[i]==0||(m_jiaohu[i]==1&&m_cardDatas[i].norcard.size()==1))//
			{
				
				boolean rs=add(m_cardDatas[i].ops, Logic.getHuOption(m_cardDatas[i],cardid));
				m_user_response[i]=!rs;
				flag=flag||rs;
				if(rs)
					m_jiaohu[i]|=2;
			}
			else 
				m_user_response[i]=true;
		}
		return flag;
	}
	*/
	private boolean add(List list,Object obj)
	{
		if(obj!=null)
		{
			list.add(obj);
			return true;
		}
		return false;
	}
	private synchronized void onUserOutCard(int chairid, IUser user, JsonObject data)
	{
		System.out.println("XXXXXXXXX出牌11111");
		if(chairid==m_roundInfo.curchairid&&m_roundInfo.curaction==Action.ACT_WAIT_OUT_CARD)
		{
			System.out.println("XXXXXXXXX出牌2222");
			cancelTimeCount();
			//resetGangInfo();//出牌就不会有抢杠的可能，不算杠上炮
			short id=data.get("card").getAsShort();
			boolean ting=false;
			boolean zhidui = false;
			if(data.has("ting"))
				ting=data.get("ting").getAsBoolean();
			if(data.has("zhidui"))
				zhidui = data.get("zhidui").getAsBoolean();
			//zhidui = true;
			sendTailChairid=-1;
			if(ting)
			{
				if(!kouting((byte)chairid, id))
				{
					sendGameScene(m_progress, chairid, user);
					return;
				}
				return;
			}
			if (zhidui) {
				if (!zhidui((byte)chairid, id)) {
					sendGameScene(m_progress, chairid, user);
					return;
				}
				return;
			}
			System.out.println("XXXXXXXXX出牌33");
			//boolean tiao=false;
//			if(data.has("tiao"))
//				tiao=data.get("tiao").getAsBoolean();
//			boolean tiaocard=false;
//			if(tiao)
//				tiaocard=isTiaoCard(m_cardDatas[chairid], id, (byte)chairid);
			if(outCard(chairid,m_cardDatas[chairid], id))
			{
				System.out.println("XXXXXXXXX出牌4444");
				if(m_cardDatas[chairid].ops.size()>0)
				{
					System.out.println("XXXXXXXXX出牌555");
					//记录选择
					Map<String, Object>seldata=new HashMap<>();
					seldata.put("progress", m_progress);
					seldata.put("op", Define.OP_SELECT);
					Map<Byte,Object>sel=new HashMap<>();
					sel.put((byte)chairid,-1);
					seldata.put("select", sel);
					recordAction(seldata, false, false);
					//**********
				}
				//m_hasoutcard[chairid]=true;
				m_roundInfo.curaction=Action.ACT_NONE;
				m_roundInfo.curoutcard=id;
				boolean hasop=updateOnOutCardOption(chairid,id,false);
				sendOutCard(chairid, id,ting);
				System.out.println("XXXXXXXXX出牌666");
				if(hasop)
				{
					
					m_roundInfo.curaction=Action.ACT_WAIT_OP;
					
					startOpTimeTask();
				}
				else
				{
					
					dipatchNext();
				}
				
			}
			else 
				sendGameScene(m_progress, chairid, user);
			
		}
	}
	/**
	 * 用户做出选择
	 * @param chairid
	 * @param user
	 * @param data
	 */
	private synchronized void onUserSelect(int chairid, IUser user, JsonObject data)
	{
		synchronized (m_cardDatas[chairid].ops) {
			if(m_cardDatas[chairid].ops.isEmpty())
			{
				LOG.info("["+m_room.getRoomid()+"]"+((Player)user).nickname+" 当前无操作");
				return;
			}
			byte op=data.get("op").getAsByte();
			if(m_roundInfo.curaction==Action.ACT_WAIT_OUT_CARD)
			{
				if(chairid!=m_roundInfo.curchairid||op!=Option.OP_HU)//(op!=Option.OP_GANG&&op!=Option.OP_HU&&op!=Option.OP_NIU))
				{
					LOG.info("["+m_room.getRoomid()+"]"+((Player)user).nickname+" 不能选择操作:"+op);
					return;
				}
			}
			else
				if(m_roundInfo.curaction!=Action.ACT_WAIT_OP)
				{
					LOG.info("["+m_room.getRoomid()+"]"+((Player)user).nickname+" 不能选择操作:"+op);
					return;
				}
			
			m_user_response[chairid]=true;
			LOG.info("["+m_room.getRoomid()+"]"+((Player)user).nickname+" 选择操作:"+op);
			
			if(op!=Option.OP_NONE)
			{
				byte id=data.get("id").getAsByte();
				for(Option opt:m_cardDatas[chairid].ops)
				{
					if(opt.op==op)
					{
						for(OpItem oit:opt.item)         
						{
							if(oit.id==id)
							{
								setOption((byte)chairid, op,oit);
								break;
							}
						}
						m_cardDatas[chairid].ops.remove(opt);
						break;
					}
				}
			}
			else 
			{
				setOption((byte)chairid, op,null);
			}
			m_cardDatas[chairid].ops.clear();//清除用户操作
			if(isAllResponse())
			{
				cancelOpTimeTask();
				
				doNextOption();
					
			}
		}
		
	}
	private boolean isAllResponse()
	{
		for(int i=0;i<m_user_response.length;i++)
		{
			if(!m_user_response[i]&&m_cardDatas[i].ops.size()>0)
				return false;
		}
		return true;
	}
	/**
	 * 设置当前操作
	 */
	private void setOption(byte chairid,byte op,OpItem item)
	{
		m_Options.add(new UserOption(chairid, op,item));
	}
	private synchronized void doNextOption()
	{
		m_roundInfo.curaction=Action.ACT_NONE;
		byte chairid=sendTailChairid;
		sendTailChairid=-1;
		for(int i=0;i<m_cardDatas.length;i++)
		{
			m_cardDatas[i].ops.clear();
		}
		if(!doOption())
		{
			if(chairid>-1)
				dispatchTailCard(chairid);
			else
				dipatchNext();
		}
		
	}
	/*
	private void resetGangInfo()
	{
		m_cur_gangcard=-1;
		m_cur_gangchairid=-1;
		m_cur_gangtype=-1;
	}
	*/
	/*
	private void setGangInfo(byte chairid,short cardid,byte type)
	{
		m_cur_gangcard=cardid;
		m_cur_gangchairid=chairid;
		m_cur_gangtype=type;
	}
	*/
	private synchronized boolean doOption()
	{
		if(m_Options.isEmpty())
		{
			return false;
		}
		boolean rs=false;
		Logic.sortOption(m_Options);
		List<UserOption>huoptions=new ArrayList<>();
		UserOption selectoption=null;
		List<UserOption>otheroptions=new ArrayList<>();
		Map<String, Object>data=new HashMap<>();
		data.put("progress", m_progress);
		data.put("op", Define.OP_SELECT);
		data.put("select", getAllPlayerSelect());
		recordAction(data, false, false);
		for(UserOption userOption:m_Options)
		{
			if(userOption.op==Option.OP_HU)
				huoptions.add(userOption);
			else 
				otheroptions.add(userOption);
		}
		if(huoptions.size()>0)
		{
			//胡了
			LOG.debug("胡了"+huoptions.size());
			return hupai(huoptions,false);
		}
		else if(otheroptions.size()>0)
		{
			selectoption=otheroptions.get(0);
			byte chairid=selectoption.chairid;
			LOG.debug("当前选择操作"+selectoption.op+",charid:"+chairid+","+m_players[chairid].nickname);
			switch(selectoption.op)
			{
				case Option.OP_CHI:
					rs=chi(chairid,selectoption);
					break;
//				case Option.OP_GANG:
//					rs=gang(chairid,selectoption);
//					break;
				case Option.OP_PENG:
					rs=peng(chairid,selectoption);
				break;
//				case Option.OP_ZHIDUI:
//					rs=zhidui(chairid, selectoption);
//					break;
				default:
					rs=false;
					break;
			}
			System.out.println("XXXXXXX操作999999");
			LOG.debug("当前选择操作"+selectoption.op+",charid:"+chairid+","+m_players[chairid].nickname+",成功："+rs);
		}
		m_Options.clear();
		System.out.println("XXXXXXX操作1000000");
		return rs;
		
	}
	private boolean outCard(int chairid,CardData cardData,int card)
	{
//		if(m_haidilao)//海底捞不能出牌
//			return false;
		if(m_ting[chairid]==1)
		{
			//扣听只能出进牌
			if(card==cardData.incard)
			{

				cardData.incard=null;
				cardData.out.add((short)card);
				return true;
			}
			return false;
		}
		if(cardData.lmtid!=null&&cardData.lmtid.length>0)
		{
			boolean flag=false;
			int v=card/4;
			for(short id:cardData.lmtid)
			{
				if(v==id/4)//吃福打福
				{
					flag=true;
					break;
				}
			}
			if(!flag)
				return false;
		}
		if(cardData.incard!=null&&cardData.incard==card)
		{
			cardData.incard=null;
			cardData.out.add((short)card);
			return true;
		}
		for(int i=0;i<cardData.norcard.size();i++)
		{
			if(cardData.norcard.get(i)==card)
			{
				cardData.norcard.remove(i);
				cardData.out.add((short)card);
				if(cardData.incard!=null)
				{
					cardData.norcard.add(cardData.incard);
					cardData.incard=null;
				}
				return true;
			}
		}
		return false;
	}
	private void changeCurrentPlayer(byte chairid)
	{
		if(chairid!=m_roundInfo.curchairid)
		{
			m_userPengCards[chairid].clear();
			if(m_roundInfo.curchairid!=-1)
			{
				//m_niu[m_roundInfo.curchairid]=false;//过手不能再扭
				m_notincludes[m_roundInfo.curchairid].clear();
				//m_tiandihu[m_roundInfo.curchairid]=false;//过手了就不是天地胡
				//updateState(m_kouting, m_roundInfo.curchairid, (byte)-1);//过不能再扣听
			}
		}
		m_roundInfo.curchairid=chairid;
	}
	private void sendOutCard(int chairid,int card,boolean kouting)
	{
		m_progress++;
		for(int i=0;i<m_players.length;i++)
		{
			Map<String, Object>data=new HashMap<>();
			data.put("progress", m_progress);
			data.put("op", Define.OP_OUT_CARD);
			data.put("chairid", chairid);
			data.put("cardid", card);
			if(kouting)
				data.put("ting", true);
			if(i==chairid)
				recordAction(data,true,false);
			data.put("ops", m_cardDatas[i].ops);
			data.put("lefttime", m_cardDatas[i].ops.size()>0?op_timeout:null);
			sendPlayMessage(i, data);
		}
	}
	
	private synchronized void countZiMoScore(byte chairid,int winflag,int wintype,boolean vote)
	{
		int winflags[]=new int[m_chairCount];
		winflags[chairid]=winflag;
		//门清
		for(int j = 0;j<m_chairCount;j++){
			if (m_isKaimen[j] == 0) {
				winflags[j] |= Define.HP_MEN_QING;
			}
		}
		int wintypes[]=new int[m_chairCount];
		wintypes[chairid]=wintype;
		m_players[chairid].playstate|=Define.PLAY_STATE_ZIMO;
		m_logic.setBaoPai(random.nextInt(28));
		RoundResult results[]=Logic.countResult(m_PlayerResults,m_roundInfo.difen, m_cardDatas, winflags, wintypes, -1, 1,m_roundInfo.zhuangjiachairid,m_isKaimen,m_chairCount);
		for(int i=0;i<m_chairCount;i++)
		{
			m_players[i].score+=results[i].score;
		}
		Map<String, Object>wininfo=new HashMap<>();
		wininfo.put("type", 1);
		wininfo.put("chairid", chairid);
		m_winner=chairid;
		sendRoundResult(results,wininfo,false,vote);
	}
	private void sendRoundResult(RoundResult[] results,Map<String, Object>wininfo,boolean huangzhuang,boolean vote)
	{
		cancelOpTimeTask();
		cancelTimeCount();
		//cancelVoteTask();
		if(m_gameState!=Define.GAME_STATE_OVER)
			m_gameState=Define.GAME_STATE_ROUND_OVER;
		m_roundInfo.curaction=Action.ACT_NONE;
		m_progress++;
		Map<String, Object>data=new HashMap<>();
		data.put("progress", m_progress);
		data.put("op", Define.OP_ROUND_OVER);
		Map<String, Object>items[]=new HashMap[m_chairCount];
		int[] winstate=new int[m_chairCount];
		for(int i=0;i<results.length;i++)
		{
			m_PlayerResults[i].add(results[i]);
			m_PlayerResults[i].uid=m_players[i].uid;
			results[i].uid=m_players[i].uid;
			if(results[i].maxloseid>-1)
				results[i].maxloseuid=m_players[results[i].maxloseid].uid;
			if(results[i].maxwinid>-1)
				results[i].maxwinuid=m_players[results[i].maxwinid].uid;
			Map<String, Object>item=new HashMap<>();
			m_cardDatas[i].ops.clear();
			item.put("chairid", i);
			item.put("score", results[i].score);
			item.put("totalscore", m_players[i].score);
			item.put("des", results[i].des);
			results[i].card=Logic.toResultCardid(m_cardDatas[i]);
			item.put("card", results[i].card);
			items[i]=item;
			winstate[i]=huangzhuang?2:(results[i].score>0?1:0);
		}
		Map<String, Object>result=new HashMap<>();
		result.put("detail", items);
		result.put("winstate", winstate);
		data.put("result", result);
		data.put("wininfo", wininfo);
		recordAction(data,false,true);
		if(m_gameState==Define.GAME_STATE_OVER||m_round>config.maxround||(m_round==config.maxround&&isXiaZhuang()))
		{
			m_gameState=Define.GAME_STATE_OVER;
			isover=true;
			Map<String, Object>resultdata=new HashMap<>();
			resultdata.put("progress", m_progress);
			resultdata.put("op", Define.OP_GAME_OVER);
			Map<String, Object>reulutitems[]=new HashMap[m_chairCount];
			for(int i=0;i<m_chairCount;i++)
			{
				Map<String, Object>item=new HashMap<>();
				item.put("detail", m_PlayerResults[i].toItems());
				item.put("chairid", i);
				item.put("score", m_players[i].score);
				item.put("master", m_players[i].getId().equals(m_room.getMasterId()));
				reulutitems[i]=item;
			}
			resultdata.put("result", reulutitems);
			data.put("gameresult", resultdata);
			m_result=resultdata;
			recordAction(resultdata,false,false);
		}
		else
			m_result=data;
		Arrays.fill(m_user_response, false);
		String url=finishRrecord();
		for(int i=0;i<m_players.length;i++)
		{
			sendPlayMessage(i, data);
		}
		m_room.onRoundOver(m_roundInfo.curcircle,m_roundInfo.curround, results, url,!vote||!huangzhuang);
		if(isover)
		{
			m_room.onGameOver(m_PlayerResults,vote?2:1,m_votecharidid);
		}
	}
	private synchronized void countScore(int winflags[],int wintypes[],int wincount,byte fangpaochairid,boolean vote)
	{
		RoundResult results[];
		List<Byte>winner=new ArrayList<>();
		byte winid=-1;
		//门清
		for(int j = 0;j<m_chairCount;j++){
			if (m_isKaimen[j] == 0) {
				winflags[j] |= Define.HP_MEN_QING;
			}
		}
		
		if(wincount>0)
		{
			
			m_logic.setBaoPai(random.nextInt(28));
			results=Logic.countResult(m_PlayerResults,m_roundInfo.difen, m_cardDatas, winflags, wintypes, fangpaochairid, wincount,m_roundInfo.zhuangjiachairid,m_isKaimen,m_chairCount);
			for(int i=0;i<m_chairCount;i++)
			{
				m_players[i].score+=results[i].score;
				if(wintypes[i]!=0)
				{
					winner.add((byte)i);
					if(i==m_roundInfo.zhuangjiachairid)
						winid=(byte)i;
					else if(winid==-1)
						winid=(byte)i;
				}
				LOG.debug(results[i].des);
			}
		}
		else 
		{
			results=new RoundResult[m_chairCount];
			for(int i=0;i<results.length;i++)
				results[i]=new RoundResult();
		}
		m_winner=winid;
		Map<String, Object>wininfo=new HashMap<>();
		wininfo.put("type", wincount>0?2:0);
		wininfo.put("winner", winner.toArray());
		
		wininfo.put("lose", fangpaochairid);
		sendRoundResult(results,wininfo,wincount==0,vote);
	}
	private synchronized boolean hupai(List<UserOption> options,boolean vote)
	{
		if(m_roundInfo.curchairid==-1)
		{
			LOG.error(m_room.getRoomid()+":胡牌了，但是当前出牌玩家不存在");
			return false;
		}
		int wincount=0;
		int winflags[]=new int[m_chairCount];
		int wintypes[]=new int[m_chairCount];
		byte fangpaoid = -1;
		for(UserOption op:options)
		{
			CardData cardData=m_cardDatas[op.chairid];
			if(m_roundInfo.curchairid==op.chairid)//当前玩家
			{
				byte[] counts=new byte[28];
				if(cardData.incard!=null)
					counts[cardData.incard/4]+=1;
				for(short st:cardData.norcard)
				{
					int v=st/4;
					counts[v]++;
				}
				
				int wintype=Logic.getWinType(cardData,-1,m_logic.getPlayType(),m_logic.getBaoPai());
				if (wintype == 0) {
					wintype = m_logic.getHongZhongHu(counts, -1);
				}
				int winflag=0;
				if(wintype==0)
				{
					LOG.error(op.chairid+"自摸判定出错");
					continue;
				}
				winflag|=Define.HP_ZIMO;
				winflag |=wintype; //Logic.getHuWinType();
				winflag|=m_logic.getHongZhongHu(counts, -1);
//				if(m_tiandihu[op.chairid])//可天地胡
//				{
//					if(op.chairid==m_roundInfo.zhuangjiachairid)//是庄家，天胡
//						winflag|=Define.HP_TIAN_HE;
//					else 
//						winflag|=Define.HP_DI_HE;//非庄家，地胡
//				}
//				else
//				{
//					if(cardData.norcard.size()==1)//手把一
//						winflag|=Define.HP_SHOUBA_YI;
//					if(m_haidilao)//海底捞
//						winflag|=Define.HP_HAI_DI_LAO;
//					if(m_ting[op.chairid]==1)//扣听
//						winflag|=Define.HP_KOUTING;
//					if(m_cur_gangchairid==op.chairid)//杠开
//						winflag|=Define.HP_GANG_KAI;
//				}
				m_players[op.chairid].playstate|=Define.PLAY_STATE_ZIMO;
				cardData.winCard=cardData.incard;
				
				//门清
//				for(){
//					
//				}
				
				countZiMoScore(op.chairid,winflag,wintype,vote);
				return true;
			}
			else 
			{
				byte[] counts=new byte[28];
					counts[m_roundInfo.curoutcard/4]+=1;
				for(short st:cardData.norcard)
				{
					int v=st/4;
					counts[v]++;
				}
				
				int wintype=0;
				wintype=Logic.getWinType(cardData,m_roundInfo.curoutcard,m_logic.getPlayType(),m_logic.getBaoPai());
				if (wintype == 0) {
					wintype = m_logic.getHongZhongHu(counts, m_roundInfo.curoutcard);
				}
				if(wintype!=0)
					winflags[op.chairid]|=Define.HP_CHIHU;//捉炮和	
				wintypes[op.chairid]=wintype;
				if(wintype==0)
					continue;
//				if(m_tiandihu[op.chairid]&&op.chairid!=m_roundInfo.zhuangjiachairid)//非庄家未出牌
//					winflags[op.chairid]|=Define.HP_DI_HE;//非庄家，地胡
//				else
//				{
////					if(cardData.norcard.size()==1)//手把一
////						winflags[op.chairid]|=Define.HP_SHOUBA_YI;
////					if(m_kouting[op.chairid]==1)//扣听
////						winflags[op.chairid]|=Define.HP_KOUTING;
//				}
				winflags[op.chairid] |= wintype;//Logic.getHuWinType();
				winflags[op.chairid] |= m_logic.getHongZhongHu(counts, m_roundInfo.curoutcard);
				wincount++;
				cardData.incard=m_roundInfo.curoutcard;
				if (m_ting[m_roundInfo.curchairid] != 1) {//如果没听才为点炮
					winflags[m_roundInfo.curchairid]|=Define.HP_DIANPAO;//当前玩家点炮
					m_players[m_roundInfo.curchairid].playstate|=Define.HP_DIANPAO;
					fangpaoid = m_roundInfo.curchairid;
				}
				m_players[op.chairid].playstate|=Define.HP_CHIHU;
				
			}
		}
		if(wincount>0)
		{
			
			countScore(winflags, wintypes,wincount,fangpaoid,vote);
			return true;
		}
		return false;
		
	}
	private synchronized boolean zhidui(byte chairid,short cardid)
	{
		if (m_isKaimen[chairid] == 0) {
			return false;
		}
		if(chairid!=m_roundInfo.curchairid||m_ting[chairid]!=0)
		{
			return false;
		}
		WeaveCard weaveCard=m_logic.zhidui(m_cardDatas[chairid],m_roundInfo.curchairid,m_roundInfo.curoutcard,cardid);
		if(weaveCard==null)
		{
			return false;
		}
		//m_cardDatas[chairid].out.remove((Object)m_roundInfo.curoutcard);
		CardData cardData=m_cardDatas[chairid];
		if(!outCard(chairid, cardData, cardid))
		{
			LOG.error("支对失败，出牌失败");
			return false;
		}
		//m_roundInfo.curaction=Action.ACT_WAIT_OUT_CARD;
		//changeCurrentPlayer(chairid);
		updateState(m_ting, chairid, (byte)1);
		//m_niu[chairid]=false;
		//m_tiandihu[chairid]=false;
		m_players[chairid].playstate|=Define.PLAY_STATE_ZHIDUI;
		m_roundInfo.curaction=Action.ACT_NONE;
		//m_roundInfo.curoutcard=cardid;
		
		//记录选择
		Map<String, Object>data=new HashMap<>();
		data.put("progress", m_progress);
		data.put("op", Define.OP_SELECT);
		Map<Byte,Object>sel=new HashMap<>();
		sel.put(chairid, Option.OP_ZHIDUI);
		data.put("select", sel);
		recordAction(data, false, false);
		//**********
		
		boolean hasop=updateOnOutCardOption(chairid,cardid,false);
		sendOutCard(chairid, cardid,true);
		if(hasop)
		{
			LOG.debug("等待玩家操作");
			m_roundInfo.curaction=Action.ACT_WAIT_OP;
			startOpTimeTask();
		}
		else
		{
			dipatchNext();
		}
		
		return true;
	}
	
	private synchronized boolean chi(byte chairid,UserOption option)
	{
		if(chairid==m_roundInfo.curchairid)
		{
			return false;
		}
		int wintype=Logic.getWinType(m_cardDatas[chairid], m_roundInfo.curoutcard,m_logic.getPlayType(),m_logic.getBaoPai());
		WeaveCard weaveCard=Logic.chi(m_cardDatas[chairid],m_roundInfo.curchairid,m_roundInfo.curoutcard,option);
		if(weaveCard==null)
		{
			return false;
		}
		m_isKaimen[chairid]++;
		//m_chicount[chairid]++;//吃牌数量累计
		//m_tiandihu[chairid]=false;//吃不能天地胡
		//updateState(m_kouting, chairid, (byte)-1);//吃不能扣听
		//从出牌的玩家手中移除碰的牌
		m_cardDatas[m_roundInfo.curchairid].out.remove((Object)m_roundInfo.curoutcard);
		m_roundInfo.curaction=Action.ACT_WAIT_OUT_CARD;
		updateJiaoHu(chairid,wintype);
		changeCurrentPlayer(chairid);
		//m_cardDatas[chairid].lmtid=option.item.lmtid;
		m_roundInfo.curoutcard=-1;
		m_roundInfo.lefttime=timeout;
		m_progress++;
		/*if(m_cardDatas[chairid].lmtid==null||m_cardDatas[chairid].lmtid.length==0)//吃福打福
			updateCurrentPlayerCardOption(chairid, (short)-1,false);
		else 
			clearOptions();*/
		updateCurrentPlayerCardOption(chairid, (short)-1,false);
		for(int i=0;i<m_players.length;i++)
		{
			Map<String, Object>data=new HashMap<>();
			data.put("progress", m_progress);
			data.put("op", Define.OP_WEAVECARD);
			data.put("chairid", chairid);
			data.put("card", weaveCard);
			data.put("lefttime", timeout);
			data.put("rm", option.item.rm);
			if(i==chairid)
			{
				recordAction(data,true,true);
				//data.put("lmtid", m_cardDatas[i].lmtid);
			}
			data.put("ops",m_cardDatas[i].ops);
			sendPlayMessage(i, data);
		}
		startTimeCount();
		return true;
	}
	private synchronized boolean peng(byte chairid,UserOption option)
	{
		int wintype=Logic.getWinType(m_cardDatas[chairid], m_roundInfo.curoutcard,m_logic.getPlayType(),m_logic.getBaoPai());
		WeaveCard weaveCard=m_logic.peng(m_cardDatas[chairid],m_roundInfo.curchairid,m_roundInfo.curoutcard,option);
		if(weaveCard==null)
		{
			return false;
		}
		m_isKaimen[chairid]++;
		updateJiaoHu(chairid,wintype);
		m_cardDatas[chairid].lmtid=null;
		//从出牌的玩家手中移除碰的牌
		m_cardDatas[m_roundInfo.curchairid].out.remove((Object)m_roundInfo.curoutcard);
		changeCurrentPlayer(chairid);
		//m_tiandihu[chairid]=false;//碰不能天地胡
		//updateState(m_kouting, chairid, (byte)-1);//碰不能扣听
		m_roundInfo.curoutcard=-1;
		m_roundInfo.curaction=Action.ACT_WAIT_OUT_CARD;
		m_progress++;
		updateCurrentPlayerCardOption(chairid, (short)-1,false);
		for(int i=0;i<m_players.length;i++)
		{
			Map<String, Object>data=new HashMap<>();
			data.put("progress", m_progress);
			data.put("op", Define.OP_WEAVECARD);
			data.put("chairid", chairid);
			data.put("card", weaveCard);
			data.put("lefttime", timeout);
			data.put("rm", option.item.rm);
			if(i==chairid)
				recordAction(data,true,true);
			data.put("ops",m_cardDatas[i].ops);
			sendPlayMessage(i, data);
		}
		startTimeCount();
		return true;
	}
	/*
	private synchronized boolean gang(byte chairid,UserOption option)
	{
		Short gangcard=option.item.gang;
		if(chairid!=m_roundInfo.curchairid&&gangcard!=m_roundInfo.curoutcard)
			return false;
		if(gangcard==null)
			gangcard=-1;
		WeaveCard weaveCard=Logic.gang(m_cardDatas[chairid],chairid,m_roundInfo.curchairid,gangcard,option);
		if(weaveCard==null)
		{
			return false;
		}
		boolean flag=false;
		if(chairid!=m_roundInfo.curchairid)//杠别人的牌
		{
			//从出牌的玩家手中移除碰的牌
			m_cardDatas[m_roundInfo.curchairid].out.remove((Object)m_roundInfo.curoutcard);
		}
		else if(weaveCard.type==WeaveCard.TYPE_JIAGANG)//加杠，判断是否有抢杠
		{
			//自己摸牌杠，判断是否抢杠
			 flag=updateGangOption(m_roundInfo.curchairid,gangcard);
		}
		if(weaveCard.type!=WeaveCard.TYPE_ANGANG)
		{
			//m_niu[chairid]=false;//明杠不能再扭牌
			//m_tiandihu[chairid]=false;
			updateState(m_kouting, chairid, (byte)-1);//暗杠不能扣听
		}
		setGangInfo(chairid,gangcard,weaveCard.type);//设置当前杠的信息
		if(flag)
		{
			LOG.debug("发生抢杠");
			m_roundInfo.curaction=Action.ACT_WAIT_OP;
		}
		else 
			m_roundInfo.curaction=Action.ACT_NONE;
		m_cardDatas[chairid].lmtid=null;
		changeCurrentPlayer(chairid);
		//m_tiandihu[chairid]=false;
		m_roundInfo.curoutcard=-1;
		m_progress++;
		for(int i=0;i<m_players.length;i++)
		{
			Map<String, Object>data=new HashMap<>();
			data.put("progress", m_progress);
			data.put("op", Define.OP_WEAVECARD);
			data.put("chairid", chairid);
			data.put("card", weaveCard);
			data.put("lefttime", timeout);
			data.put("rm", option.item.rm);
			if(i==chairid)
				recordAction(data,true,true);
			if(flag)
			{
				data.put("ops", m_cardDatas[i].ops);
				data.put("lefttime", m_cardDatas[i].ops.size()>0?op_timeout:null);
			}
			sendPlayMessage(i, data);
		}
		if(!flag)//没有发生抢杠
			dispatchTailCard(chairid);
		else 
		{
			sendTailChairid=chairid;
			startOpTimeTask();//开启操作倒计时
		}
		return true;
	}
	*/
	/*
	private synchronized boolean niupai(byte chairid,UserOption option)
	{
		if(chairid!=m_roundInfo.curchairid)
		{
			return false;
		}
		WeaveCard weaveCard=Logic.niupai(m_cardDatas[chairid],chairid,option);
		if(weaveCard==null)
		{
			return false;
		}
		m_cardDatas[chairid].lmtid=null;
		changeCurrentPlayer(chairid);
		m_roundInfo.curoutcard=-1;
		m_roundInfo.lefttime=timeout;
		m_progress++;
//		if(option.item.type!=WeaveCard.TYPE_DONG_NAN_XI_BEI)
//			updateCurrentPlayerCardOption(chairid, (short)-1,true);
		for(int i=0;i<m_players.length;i++)
		{
			Map<String, Object>data=new HashMap<>();
			data.put("progress", m_progress);
			data.put("op", Define.OP_WEAVECARD);
			data.put("chairid", chairid);
			data.put("card", weaveCard);
			data.put("lefttime", timeout);
			data.put("rm", option.item.info);
			if(i==chairid)
				recordAction(data,true,false);
//			if(option.item.type!=WeaveCard.TYPE_DONG_NAN_XI_BEI)
//				data.put("ops",m_cardDatas[i].ops);
			sendPlayMessage(i, data);
		}
//		if(option.item.type==WeaveCard.TYPE_DONG_NAN_XI_BEI)
//		{
//			//东南西北需要再补一张牌
//			LOG.debug("东南西北需要再补一张牌");
//			m_roundInfo.curaction=Action.ACT_NONE;
//			dipatchTailDelay(chairid);
//		}
//		else
//		{
			m_roundInfo.curaction=Action.ACT_WAIT_OUT_CARD;
			startTimeCount();
//		}
		return true;
	}
	*/
	private synchronized boolean kouting(byte chairid,short cardid)
	{
		//还需检测是否有1或9牌或者红中
		if (m_isKaimen[chairid] == 0) {
			return false;
		}
		if(chairid!=m_roundInfo.curchairid||m_ting[chairid]!=0)
		{
			return false;
		}
		CardData cardData=m_cardDatas[chairid];
		if(!outCard(chairid, cardData, cardid))
		{
			LOG.error("扣听失败，出牌失败");
			return false;
		}
		updateState(m_ting, chairid, (byte)1);
		//m_niu[chairid]=false;
		//m_tiandihu[chairid]=false;
		m_players[chairid].playstate|=Define.PLAY_STATE_TING;
		m_roundInfo.curaction=Action.ACT_NONE;
		m_roundInfo.curoutcard=cardid;
		
		//记录选择
		Map<String, Object>data=new HashMap<>();
		data.put("progress", m_progress);
		data.put("op", Define.OP_SELECT);
		Map<Byte,Object>sel=new HashMap<>();
		sel.put(chairid, Option.OP_TING);
		data.put("select", sel);
		recordAction(data, false, false);
		//**********
		
		boolean hasop=updateOnOutCardOption(chairid,cardid,false);
		sendOutCard(chairid, cardid,true);
		if(hasop)
		{
			LOG.debug("等待玩家操作");
			m_roundInfo.curaction=Action.ACT_WAIT_OP;
			//if(niucard)
			//	sendTailChairid=chairid;
			startOpTimeTask();
		}
		else
		{
			dipatchNext();
			/*if(!niucard)
				dipatchNext();
			else 
				dispatchTailCard(chairid);//扭牌发尾牌
*/			
		}
		return true;
	}
	
	private void updateJiaoHu(int chairid,int wintype)
	{
		if(wintype>0)
			m_jiaohu[chairid]=1;
		else 
			m_jiaohu[chairid]=0;
	}
	
	/*
	private boolean isTiaoCard(CardData cardData,short cardid,byte chairid)
	{
		if(isCanGang()&&Logic.isNiuCard(cardData, cardid))
		{
			LOG.debug("有扭牌,可发尾牌");
			return true;
		}
		return false;
	}
	*/
	/**
	 * 发送开始海底捞消息
	 */
	private void sendStartHaiDiLao()
	{
		LOG.debug("开始海底捞了");
	}
	public int getGameSate() {
		return m_gameState;
	};
	private void append(StringBuffer buffer,String key,Object vl)
	{
		buffer.append("\""+key+"\":"+GSON.toJson(vl)+",\n");
	}
	private void append(StringBuffer buffer,String key,String vl)
	{
		buffer.append("\""+key+"\":"+vl+",\n");
	}
	private void append(StringBuffer buffer,String vl)
	{
		buffer.append(vl);
	}
	private void recordAction(Object data,boolean appendoption,boolean oppendselect)
	{
		if(recordbuffer!=null)
		{
			if(opsize>0)
				recordbuffer.append(",\n");
			JsonObject object=GSON.toJsonTree(data).getAsJsonObject();
			if(appendoption)
				object.add("opts", GSON.toJsonTree(getAllPlayerOption()));
			recordbuffer.append(object.toString());
			opsize++;
		}
	}
	private String  finishRrecord()
	{
		if(recordbuffer!=null)
		{
			opsize=0;
			recordbuffer.append("]}\n");
			recorder.append(recordfile,recordbuffer.toString());
			recordbuffer=null;
			return recordfile;
		}
		return null;
	}
	private void startRecord()
	{
		opsize=0;
		recordfile=savepath+"/r"+m_roundInfo.curround+".json";
		StringBuffer buffer=new StringBuffer();
		append(buffer, "{\n");
		append(buffer, "date","\""+format3.format(new Date())+"\"");
		append(buffer, "roominfo", m_room.getRoomInfo());
		append(buffer, "player",m_players);
		append(buffer,"roundinfo",m_roundInfo);
		append(buffer,"carddata",m_cardDatas);
		append(buffer,"\"actions\":[\n");
		recordbuffer=buffer;
	}
	private void updateState(byte[] states,int index,byte state)
	{
		if(states[index]==0)
			states[index]=state;
	}
	@Override
	public void destroy() {
		LOG.info("["+m_room.getRoomid()+"]table 已销毁");
	}
	@Override
	public void clock(long time) {
		syntime=syntime+time;
		if(syntime>=10000)//10s一次同步
		{
			syntime=0;
			sendClock();
		}
	}
	private void sendClock()
	{
		LOG.debug("["+m_room.getRoomid()+"]同步时钟");
		Message message=new Message();
		message.what="clock";
		message.data=m_progress;
		m_room.sendAll(message);
	}
}
/**
 * 原来已经叫牌，吃碰杠打出都不能胡，必须过手
 */
