package com.lebin.game.qdmj;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lebin.game.module.IAccount;
import com.lebin.game.module.IRecorder;
import com.lebin.game.module.IRoom;
import com.lebin.game.module.IRoomManager;
import com.lebin.game.module.ISession;
import com.lebin.game.module.ISessionListener;
import com.lebin.game.module.IUser;
import com.lebin.game.module.Table;
import com.lebin.game.module.data.Message;
import com.lebin.game.module.data.Player;
import com.lebin.game.qdmj.define.Define;
import com.lebin.game.qdmj.define.PlayerItem;
import com.lebin.game.qdmj.define.RoundResult;
import com.lebin.server.netty.serviceimpl.RobotMessageSender;
import com.lebin.server.util.StringUtil;

import atg.taglib.json.util.JSONArray;
import atg.taglib.json.util.JSONObject;

public class Room implements IRoom,ISessionListener{
	static Logger LOG=LoggerFactory.getLogger(Room.class);
	static SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static SimpleDateFormat format2=new SimpleDateFormat("HH:mm");
	Table table;
	BitSet m_seatState;
	int m_roomid;
	long masterid;//房主id
	int m_playnum;
	String m_playTypeNum;
	int gameid;
	String serviceid;
	boolean pay=false;
	Date starttime;
	boolean gameover=false;
	IRoomManager manager;
	public  long expirytime;
	private long lastactivetime;
	Map<Long, PlayerItem>playerItems=new ConcurrentHashMap<>();
	Map<String, Object>roomInfo=new HashMap<>();
	IRecorder recorder;
	boolean start=false;
	int ruleid=-1;
	Map<String, Object>userData=new HashMap<>();
	@Override
	public void init(String config) {
		table=new MaJiangTable();
		try {
			LOG.debug(config);
			lastactivetime=System.currentTimeMillis();
			JSONObject json=new JSONObject(config);
			System.out.println("xxxxxx房间数据：："+config);
			m_playnum = new JSONObject(json.getString("extra")).getInt("playerNum");//几人房
			m_playTypeNum = new JSONObject(json.getString("extra")).getString("playtp");
			ruleid=json.getInt("ruleid");
			m_roomid=json.getInt("roomid");
			int circle = json.getInt("circle");
			gameid=json.getInt("gameid");
			masterid=json.getLong("master");
			roomInfo.put("roomid", m_roomid);
			roomInfo.put("playernum", m_playnum);
			roomInfo.put("playTypeNum", m_playTypeNum);
			roomInfo.put("master", masterid);
			roomInfo.put("roomcircle", circle);
			roomInfo.put("rulename", json.getString("rulename"));
			roomInfo.put("ruleid", json.getInt("ruleid"));
			serviceid=json.getString("serviceid");
			roomInfo.put("serviceid", serviceid);
			roomInfo.put("mastername", json.getString("mastername"));
			expirytime=json.getInt("freeroomtime")*60000;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		//System.out.println("zzzzzzzz::"+config);
		table.onInit(this,config);
		m_seatState=new BitSet(table.getChairCount());
	}
	@Override
	public void onCreate(IRoomManager manager,IRecorder recorder) {
		
		this.manager=manager;
		this.recorder=recorder;
		((MaJiangTable)table).setRecorder(recorder);
	}
	@Override
	public void setRoomid(int id) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Object getMasterId() {
		// TODO Auto-generated method stub
		return masterid;
	}
	@Override
	public void setMasterId(Object id) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public int getRoomid() {
		return m_roomid;
	}
	@Override
	public boolean send(IUser user, Object data) {
		if(user==null)
			return false;
		Player player=(Player)user;
		PlayerItem item=playerItems.get(player.uid);
		if(item==null||player.isOffline())
			return false;
		try {
			item.session.send(data);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	@Override
	public void sendAll(Object data) {
		playerItems.forEach((uid, playerItem) -> { 
			try {
				if(!playerItem.player.isOffline())
					playerItem.session.send(data);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
	@Override
	public boolean onMessage(Object data,IUser user, ISession session) {
		lastactivetime=System.currentTimeMillis();
		JsonObject json=(JsonObject)data;
		LOG.debug("rcv "+data);
		int msgtype=json.get("type").getAsInt();
		Player player=(Player)user;
		switch(msgtype)
		{
			case Define.MSG_SYN_SCENE:
				sendScene(json.getAsJsonObject("data").get("progress").getAsLong(),player, session);;
				break;
			case Define.MSG_READY:
				onUserReady(json.getAsJsonObject("data").get("progress").getAsLong(),user, session);;
				break;
			case Define.MSG_RESPONSE:
				onUserOption(json.get("data"), user, session);
				break;
			case Define.MSG_CHAT:
				onChat(json.getAsJsonObject("data"),user);
				break;
			case Define.MSG_FREE_ROOM:
				onFreeRoom(user);
				break;
			default:
				break;
		}
		return false;
	}
	@Override
	public void destroy() {
		table.destroy();
	}
	@Override
	public int joinRoom(int chairId, IUser user, ISession session) {
		lastactivetime=System.currentTimeMillis();
		int rs=-1;
		Player player=null;
		try {
			player=doJoinRoom(chairId, (Player)user, session);
			if(player!=null)
				rs=player.chairid;
			return rs;
		} finally {
			if(player!=null)
			{
				//player.chairid=rs;
				session.setAttribute("uid", user.getId());
				session.addListener(this);
				notifyJoinRoom(player);
			}
		}
	}
	private Player doJoinRoom(int chairId, Player joinPlayer, ISession session)
	{
		joinPlayer.ipold=joinPlayer.ip;
		synchronized (playerItems) {
			if(playerItems.containsKey(joinPlayer.uid))
			{
				PlayerItem item=playerItems.get(joinPlayer.uid);
				if(item==null)
					playerItems.remove(joinPlayer.uid);
				else 
				{
					ISession old=item.session;
					item.player.lat=joinPlayer.lat;
					item.player.lon=joinPlayer.lon;
					item.player.ip=joinPlayer.ip;
					if(old!=session)
					{
						//替换旧session
						if(old!=null)
							old.close();
						item.session=session;
					}
					if(table.onUserConnect(item.player.chairid, joinPlayer))
						return item.player;
					return null;
				}
			}
		}
		synchronized (m_seatState) {
			if(chairId<0)
			{
				for(int i=0;i<table.getChairCount();i++)
				{
					if(!m_seatState.get(i))
					{
						if(table.onUserSitDown(i, joinPlayer))
						{
							m_seatState.set(i);
							joinPlayer.chairid=i;
							PlayerItem item=new PlayerItem();
							item.player=joinPlayer;
							item.chairid=(byte)i;
							item.session=session;
							playerItems.put(joinPlayer.uid, item);
							return joinPlayer;
						}
					}
				}
			}
			else
			{
				if(table.onUserSitDown(chairId, joinPlayer))
				{
					m_seatState.set(chairId);
					joinPlayer.chairid=chairId;
					PlayerItem item=new PlayerItem();
					item.player=joinPlayer;
					item.session=session;
					item.chairid=(byte)chairId;
					playerItems.put(joinPlayer.uid, item);
					return joinPlayer;
				}
			}
		}
		return null;
	}
	@Override
	public boolean exitRoom(IUser user, ISession session) {
		Player player=(Player)user;
		if(table.onUserExit(player.chairid, user))
		{
			PlayerItem item=playerItems.remove(player.uid);
			try {
				m_seatState.set(player.chairid,false);
				if(item!=null)
					item.session.removeListener(this);
				return true;
			} finally {
				notifyExitRoom(player);
			}
			
		}
		return false;
	}
	private void notifyJoinRoom(final Player player)
	{
		final Message msg=new Message();
		msg.from="@"+m_roomid;
		msg.what="joinroom";
		msg.data=player;
		sendToOther(msg,player.uid);
	}
	private void sendToOther(Object msg,long expuid)
	{
		playerItems.forEach((uid, playerItem) -> { // 通知有人掉线
			if(expuid!=uid&&!playerItem.player.isOffline())
			{
				try {
					playerItem.session.send(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	private void notifyOffLineRoom(final Player player)
	{
		final Message msg=new Message();
		msg.from="@"+m_roomid;
		msg.what="offline";
		msg.data=player.chairid;
		sendToOther(msg,player.uid);
	}
	private void notifyExitRoom(final Player player)
	{
		final Message msg=new Message();
		msg.from="@"+m_roomid;
		msg.what="exitroom";
		msg.data=player.chairid;
		sendToOther(msg,player.uid);
	}
	@Override
	public int getChairId(IUser user) {
		return ((Player)user).chairid;
	}
	@Override
	public boolean onUserOffLine(IUser user, ISession session) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean onUserConnect(IUser user, ISession session) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public IUser getUser(int chairId) {
		return table==null?null:table.getUser(chairId);
	}
	@Override
	public void onClosed(ISession session) {
		
		Object uid=session.getAttribute("uid");
		LOG.debug(uid+" closed");
		if(uid!=null)
		{
			PlayerItem item=playerItems.get(uid);
			if(item!=null)
			{
				table.onUserOffLine(item.player.chairid, item.player);
				notifyOffLineRoom(item.player);//通知其他人有人掉线
			}
		}
	}
	@Override
	public List<IUser> getPlayers() {
		List<IUser>users=new ArrayList<>();
		playerItems.forEach((uid, playerItem) -> { // 通知有人掉线
			users.add(playerItem.player);
		});
		return users;
	}
	private void sendScene(long progress,Player user,ISession session)
	{
		table.sendGameScene(progress,user.chairid,user);
	}
	/**
	 * 用户已就绪
	 * @param user
	 * @param session
	 */
	private void onUserReady(long progress,IUser user,ISession session)
	{
		final Message msg=new Message();
		msg.from="@"+m_roomid;
		msg.what="ready";
		try {
			session.send(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		table.onReady(progress,((Player)user).chairid, user);//就绪
	}
	/**
	 * 用户操作
	 * @param user
	 * @param session
	 */
	private void onUserOption(Object data,IUser user,ISession session)
	{
		try {
			Player player=(Player)user;
			// LOG.info("["+m_roomid+"]"+player.uid+"-"+player.nickname+" req:"+data);
			table.onMessage(player.chairid, user, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	private void onChat(JsonObject obj,IUser user)
	{
		Message msg=new Message();
		msg.from="@"+m_roomid;
		msg.what="chat";
		msg.data=obj;
		sendToOther(msg, (Long)user.getId());
	};
	private void onFreeRoom(IUser user)
	{
		if(user.getId().equals(masterid))
		{
			if(table.onUserExit(((Player)user).chairid, user))
			{
				if(manager.free(this, 6))
				{
					Message msg=new Message();
					msg.from="@"+m_roomid;
					msg.what="freeroom";
					sendAll(msg);
				}
			}
		}
	}
	@Override
	public boolean sendMessage(int chairId, IUser user, Message msg) {
		msg.from="@"+m_roomid;
		return send(user, msg);
	}
	@Override
	public boolean sendGameData(int chairId, IUser user, Object data) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean sendGameStart(int chairId, IUser user, Object data) {
		Message msg=new Message();
		msg.from="@"+m_roomid;
		msg.what="start";
		msg.data=data;
		return send(user, msg);
	}
	@Override
	public boolean sendGameScene(int chaireId, IUser user, Object data) {
		Message msg=new Message();
		msg.from="@"+m_roomid;
		msg.what="synscene";
		msg.data=data;
		return send(user, msg);
	}
	@Override
	public Object getRoomInfo() {
		roomInfo.put("gamestate", table.getGameSate());
		return roomInfo;
	}
	@Override
	public String getServiceId() {
		// TODO Auto-generated method stub
		return serviceid;
	}
	@Override
	public void onGameOver(Object result,int type,int votechairid) {
		if(gameover)
			return;
		gameover=true;
		manager.free(this, type);
		Date now=new Date();
		long tc=(now.getTime()-starttime.getTime())/1000;
		String starttimestr= format.format(starttime);
		RoundResult[] result2=(RoundResult[])result;
		JSONArray array=new JSONArray();
		String msg="【游戏结束】"+format2.format(starttime)+"-"+format2.format(now)+"\n房号："+getRoomid()+" "+roomInfo.get("rulename");
		msg+="\n房主："+this.roomInfo.get("mastername")+"\n";
		long voteuid=-1;
		if(votechairid!=-1)
		{
			Player player=playerItems.get(result2[votechairid].uid).player;
			if(player!=null)
			{
				msg+=player.nickname+"("+player.uid+")发起投票\n";
				voteuid=player.uid;
			}
		}
		int maxscore=0;
		List<String>maxscoreuser=new ArrayList<>();
		try {
			if(result2!=null)
			{
				for(RoundResult r:result2)
				{
					JSONObject obj=new JSONObject();
					Player player=playerItems.get(r.uid).player;
					obj.put("nickname", player.nickname);
					obj.put("uid", player.uid);
					obj.put("score", r.score);
					if(voteuid==player.uid)
						obj.put("vote", true);
					array.add(obj);
					msg+="\n"+player.nickname+"："+r.score;
					if(r.score>maxscore)
					{
						maxscore=r.score;
						maxscoreuser.clear();
						maxscoreuser.add(player.nickname);
					}
					else if(r.score==maxscore&&r.score>0)
					{
						maxscoreuser.add(player.nickname);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		LOG.info("游戏结束 "+masterid+" "+msg);
		String sql="update db_gamedata.t_gameinfo set starttime=?,endtime=?,timecount=?,resultdata=? where serviceid=?";
		manager.getDaoService().excuteUpdate(sql, starttimestr,format.format(now),tc,array.toString(),serviceid);
		Map<String, Object> data=new HashMap<>();
		data.put("type", 2);
		data.put("result", msg);
		data.put("ruleid", ruleid);
		data.put("roomid", m_roomid);
		data.put("mastername",roomInfo.get("mastername") );
		data.put("master", masterid);
		data.put("payuser", maxscoreuser);
		data.put("serviceid", getServiceId());
		Gson gson=new Gson();
		sendRmiMsg(gson.toJson(data));
		
	}
	private void saveRoundResult(int circle,int round,RoundResult[] results,String url)
	{
		String sql="insert into db_gamedata.t_gameresult(createdate,"
		+"createtime,gameid,uid,serviceid,circle,round,score,"
		+"wintype,heju,win,lose,chairid,carddata,maxlosetouid,maxlosescore,maxwinfromuid,maxwinscore)"
		+ "values(CURDATE(),NOW(),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		JSONArray array=new JSONArray();
		List<Object[]>args=new ArrayList<>();
		for(int i=0;i<results.length;i++)
		{
			RoundResult rs=results[i];
			int heju=0,win=0,lose=0;
			if(rs.score==0)
				heju=1;
			else if(rs.score>0)
				win=1;
			else 
				lose=1;
			String str=null;//("{\"ct\":"+Arrays.toString(rs.count)+",\"cd:\""+Arrays.toString(rs.card)+"}").replaceAll("\\s", "");
			Object[] arg=new Object[]{gameid,rs.uid,serviceid,circle,round,rs.score,rs.result,heju,win,lose,i,str,rs.maxloseuid,rs.maxlosescore,rs.maxwinuid,rs.maxwinscore};
			args.add(arg);
			JSONObject obj=new JSONObject();
			try {
				Player player=playerItems.get(rs.uid).player;
				obj.put("nickname", player.nickname);
				obj.put("uid", player.uid);
				obj.put("score", rs.score);
				array.add(obj);
			} catch (Exception e) {
				// TODO: handle exception
			}
		
		}
		manager.getDaoService().excuteBatch(sql,args);
		sql="insert into db_gamedata.t_record(serviceid,roomid,"
				+"round,circle,url,createdate,time,data)"
				+ "values(?,?,?,?,?,CURDATE(),NOW(),?)";
		manager.getDaoService().excuteUpdate(sql, getServiceId(),m_roomid,round,circle,url,array.toString());
	}
	@Override
	public void onGameStart() {
		start=true;
		if(starttime==null)
		{
			starttime=new Date();
			String sql="update db_room.t_room set state=2 where id=?";
			manager.getDaoService().excuteUpdate(sql,serviceid);
			sql="update db_gamedata.t_gameinfo set starttime=? where serviceid=?";
			manager.getDaoService().excuteUpdate(sql, format.format(starttime),serviceid);
		}
	}
	@Override
	public void onRoundOver(int circle,int round, Object result, Object record,boolean needpay) {
		if(!pay)
		{
			if(!needpay)
				LOG.info(m_roomid+"投票解散，不需要扣房卡");
			else
				pay=manager.payRoom(this);
		/*pay=account!=null;
			if(pay)
				sendAccountInfo(account);*/
		}
		saveRoundResult(circle,round,(RoundResult[])result,(String)record);
	}
	private void sendAccountInfo(IAccount account)
	{
		Message msg=new Message();
		msg.what="synaccount";
		Map<String, Object>data=new HashMap<>();
		msg.data=data;
		data.put("account", account.account);
		sendMessage(account.uid,msg);
	}
	private void sendMessage(long uid,Object data)
	{
		PlayerItem playerItem=playerItems.get(uid);
		if(playerItem!=null)
			try {
				LOG.debug("send "+data.toString());
				playerItem.session.send(data);
			} catch (Exception e) {
			}
	}
	@Override
	public long getExpiryTime() {
		// TODO Auto-generated method stub
		return expirytime;
	}
	@Override
	public boolean isActive() {
		return System.currentTimeMillis()-lastactivetime<expirytime;
	}
	@Override
	public int getNeedPlayerCount() {
		return table.getChairCount();
	}
	@Override
	public int getCurPlayerCount() {
		return playerItems.size();
	}
	@Override
	public void setUserData(String key, Object value) {
		// TODO Auto-generated method stub
		userData.put(key, value);
	}
	@Override
	public Object getUserData(String key) {
		// TODO Auto-generated method stub
		return userData.get(key);
	}
	public void sendRmiMsg(String msg)
	{
		String robotid=(String)getUserData("robotid");
		if(robotid==null)
			return;
		String gid=(String)getUserData("gid");
		String rmi=(String)getUserData("rmi");
		if(StringUtil.isNullOrEmpty(gid,rmi))
			return;
		RobotMessageSender.getIntance().sendMessage(rmi, robotid, gid, msg);
	}
	@Override
	public void notifyBack(List<IUser> users) {
		if(users==null||users.isEmpty())
			return;
		String robotid=(String)getUserData("robotid");
		if(robotid==null)
			return;
		String gid=(String)getUserData("gid");
		String rmi=(String)getUserData("rmi");
		if(StringUtil.isNullOrEmpty(gid,rmi))
			return;
		Map<String, Object> data=new HashMap<>();
		data.put("type", 3);
		data.put("roomid", getRoomid());
		data.put("serviceid", getServiceId());
		String msg="";
		for(IUser user:users)
		{
			msg+="@"+((Player)user).nickname+"\u2005";
		}
		msg+="游戏已经开始了，快回来！！";
		data.put("msg", msg);
		Gson gson=new Gson();
		sendRmiMsg(gson.toJson(data));
		
	}
	@Override
	public void clock(long time) {
		table.clock(time);
	}
}
