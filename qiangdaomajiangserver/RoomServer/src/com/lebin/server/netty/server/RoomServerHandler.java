package com.lebin.server.netty.server;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hogense.server.define.Response;
import com.hogense.server.define.Status;
import com.lebin.game.module.IRoom;
import com.lebin.game.module.data.Player;
import com.lebin.game.module.data.Request;
import com.lebin.server.annotation.MessageHandler;
import com.lebin.server.annotation.Service;
import com.lebin.server.net.util.RequestParser;
import com.lebin.server.netty.Session;
import com.lebin.server.netty.server.services.UserService;
import com.lebin.server.netty.serviceimpl.RoomManager;
import com.lebin.server.util.StringUtil;

import atg.taglib.json.util.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;


/**
 * WebSocket服务端Handler
 *
 */
@Service
public class RoomServerHandler extends SimpleChannelInboundHandler<Object> {
	private static final Logger LOG = LoggerFactory.getLogger(RoomServerHandler.class.getName());
	private WebSocketServerHandshaker handshaker;
	private Session session;
	private Player user;
	private IRoom curRoom;
	public static AtomicInteger usercount=new AtomicInteger(0);
	private final Object lock=new Object();
	private int UNCONNECT_NUM_S = 0;  
	private String ip;
	private EventExecutor executor;
    public RoomServerHandler(EventExecutorGroup executorGroup) {
    	executor=executorGroup.next();
	}
	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		if (msg instanceof FullHttpRequest) { // 传统的HTTP接入
			handleHttpRequest(ctx, (FullHttpRequest) msg);
		} else if (msg instanceof WebSocketFrame) { // WebSocket接入
			handleWebSocketFrame(ctx, (WebSocketFrame) msg); 
					}
	}
	/**
	 * Http返回
	 * @param ctx
	 * @param request
	 * @param response
	 */
	private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
		// 返回应答给客户端
		if (response.getStatus().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(response.getStatus().toString(), CharsetUtil.UTF_8);
			response.content().writeBytes(buf);
			buf.release();
			HttpHeaders.setContentLength(response, response.content().readableBytes());
		}
		// 如果是非Keep-Alive，关闭连接
		ChannelFuture f = ctx.channel().writeAndFlush(response);
		if (!HttpHeaders.isKeepAlive(request) || response.getStatus().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}
	/**
	 * Http返回
	 * @param ctx
	 * @param request
	 * @param response
	 */
	private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponseStatus status,String data) {
		// 返回应答给客户端
		DefaultFullHttpResponse response=new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
		if (response.getStatus().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(response.getStatus().toString(), CharsetUtil.UTF_8);
			response.content().writeBytes(buf);
			buf.release();
			HttpHeaders.setContentLength(response, response.content().readableBytes());
		}
		else if(data!=null&&!data.isEmpty())
		{
			ByteBuf buf = Unpooled.copiedBuffer(data, CharsetUtil.UTF_8);
			response.content().writeBytes(buf);
			buf.release();
			HttpHeaders.setContentLength(response, response.content().readableBytes());
		}
		// 如果是非Keep-Alive，关闭连接
		ChannelFuture f = ctx.channel().writeAndFlush(response);
		if (!HttpHeaders.isKeepAlive(request) || response.getStatus().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	/**
	 * 处理Http请求，完成WebSocket握手<br/>
	 * 注意：WebSocket连接第一次请求使用的是Http
	 * @param ctx
	 * @param request
	 * @throws Exception
	 */
	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		
		if (!request.getDecoderResult().isSuccess() || (!"websocket".equals(request.headers().get("Upgrade")))) {
			doHttpRequest(ctx,request);
			return;
		}
		// 正常WebSocket的Http连接请求，构造握手响应返回
		List<Entry<String, String>>entries=request.headers().entries();
		request.headers().remove("Sec-WebSocket-Protocol");
	//	for(Entry<String, String>en:entries)
		//	System.out.println(en.getKey()+" "+en.getValue());
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://" + request.headers().get(HttpHeaders.Names.HOST), null, false);
		handshaker = wsFactory.newHandshaker(request);
		if (handshaker == null) { // 无法处理的websocket版本
			WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
		} else { // 向客户端发送websocket握手,完成握手
			handshaker.handshake(ctx.channel(), request);
			// 记录管道处理上下文，便于服务器推送数据到客户端
			
		
		}
	}
	private void doHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request)
	{
		String uri=request.getUri();
		int index=uri.indexOf("?");
		if(index!=-1)
			uri=uri.substring(0, index);
		if(uri.equals("/robot"))
		{
			InetSocketAddress insocket = (InetSocketAddress)ctx.channel().remoteAddress();
			String ip = insocket.getAddress().getHostAddress();
			LOG.debug("rb"+ip);
			if(RoomServer.configInfo.robotserviceips.contains(ip))
			{
				handleRobotRequest(ctx,request);
				return;
			}
		}
		sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
	}
	
	/**
	 * 处理Socket请求
	 * @param ctx
	 * @param frame
	 * @throws Exception 
	 */
	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
		// 判断是否是关闭链路的指令
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
			return;
		}
		// 判断是否是Ping消息
		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			return;
		}
		// 当前只支持文本消息，不支持二进制消息
		if (!(frame instanceof TextWebSocketFrame)) {
			throw new UnsupportedOperationException("当前只支持文本消息，不支持二进制消息");
		}
		String text=((TextWebSocketFrame)frame).text();
		if(session!=null)
		{
			if("b".equals(text))
			{
			//	LOG.debug("b");
				session.send("a");
				UNCONNECT_NUM_S = 0;
				return;
			}
			else if("a".equals(text))
			{
				UNCONNECT_NUM_S = 0;
				return;
			}
		}
		executor.execute(new Runnable() {
			
			@Override
			public void run() {
				try
				{
					RoomServerHandler.this.handleMessage(ctx, text);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}
	protected void handleMessage(ChannelHandlerContext ctx,String text)
	{
		// 处理来自客户端的WebSocket请求
				Response response = new Response();
				Request request;
				try {
					request = Request.create(text, Request.class);
					response.setWhat(request.what);
				} catch (Exception e) {
					ctx.channel().writeAndFlush(new TextWebSocketFrame(response.setWhat("-1").setStatus(Status.ARG_ERR).setData(new JsonObject()).toString()));
					return;
				}
				try {
					if(!MessageHandlerFactory.invokeIfExists(this, request.what, ctx,request,response))
					{
						ctx.channel().writeAndFlush(new TextWebSocketFrame(response.setStatus(Status.ACCESS_DENY).setData(new JsonObject()).toString()));
					}
				} catch (JsonSyntaxException e1) {
					LOG.warn("Json解析异常", e1);
					ctx.channel().writeAndFlush(new TextWebSocketFrame(response.setStatus(Status.ARG_ERR).setData(new JsonObject()).toString()));
				} catch (Exception e2) {
					LOG.error("处理Socket请求异常", e2);
					ctx.channel().writeAndFlush(new TextWebSocketFrame(response.setStatus(Status.ERROR).setData(new JsonObject()).toString()));
				}
	}
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if(IdleStateEvent.class.isAssignableFrom(evt.getClass()))
		{
			if(UNCONNECT_NUM_S >= 4) {  
				 LOG.info("client connect status is disconnect."); 
               if(session==null)
               	ctx.close();  
               else 
               	session.close();
               return;  
           }  
			IdleStateEvent v=(IdleStateEvent)evt;
			switch (v.state()) {
			case READER_IDLE:
				UNCONNECT_NUM_S++;  
				//LOG.info("reader_idle over.");  
				break;
			case WRITER_IDLE:
				//UNCONNECT_NUM_S++;
				if(session!=null)
				{
					session.send("b");
				//	LOG.info("send ping to client");  
				}
				break;
			case ALL_IDLE:
				// LOG.info("all_idle over.");  
                 //UNCONNECT_NUM_S++;  
				break;
			default:
				break;
			}
			return;
		}
		super.userEventTriggered(ctx, evt);
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOG.error("WebSocket异常", cause);
		if(session!=null)
		{
			session.close();
		}
		else 
			ctx.close();
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.handlerRemoved(ctx);
		int num=usercount.decrementAndGet();//人数减
		if(session!=null)
		{
			session.close();
			LOG.debug("session关闭 "+session.isClosed());
			session=null;
		}
		else 
			ctx.close();
		LOG.info("remove "+ip+" "+num);
	}
	private int trytime=0;
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.handlerAdded(ctx);
		int num=usercount.incrementAndGet();//人数加
		InetSocketAddress insocket = (InetSocketAddress) ctx.channel()
                .remoteAddress();
		ip = insocket.getAddress().getHostAddress();
		LOG.info("add "+ip+" "+num);
	}
	@MessageHandler("enter")// 进入房间
	public void enterroom(ChannelHandlerContext ctx,Request request,Response response) throws Exception
	{
		long uid=request.data.get("uid").getAsLong();
		int gameid=request.data.get("gameid").getAsInt();
		String accesstoken=request.data.get("token").getAsString();
		long reqtime=Long.parseLong(request.data.get("reqtime").getAsString());
		long d=System.currentTimeMillis()-reqtime;
		if(Math.abs(d)>40000)
		{
			Map<String, Object>data=new HashMap<>();
			data.put("time", System.currentTimeMillis());
			response.data=data;
			response.setStatus(Status.SIGN_TIMEOUT);
			if(trytime<3)
			{
				trytime++;
				response.what="reenter";
			}
			ctx.channel().writeAndFlush(new TextWebSocketFrame( response.toString()));
			return;
		}
		trytime=0;
		user=UserService.login(uid, accesstoken, gameid,reqtime);
		if(user==null)
		{
			response.setStatus(Status.TOKEN_TIMEOUT);
			ctx.channel().writeAndFlush(new TextWebSocketFrame( response.toString()));
			return;
		}
		if(session==null)
			session=new Session(ctx);
		if(request.data.has("lat"))
			user.lat=request.data.get("lat").getAsDouble();
		if(request.data.has("lon"))
			user.lon=request.data.get("lon").getAsDouble();
		user.ip = ip;
		String serviceid=null;
		if(request.data.has("serviceid"))
		{
			JsonElement element=request.data.get("serviceid");
			if(!element.isJsonNull())
				serviceid=element.getAsString();
		}
		int op=request.data.get("op").getAsInt();
		synchronized (lock) {
			if(op==1)
			{
				JsonObject extra=request.data.has("extra")?request.data.getAsJsonObject("extra"):null;
				LOG.info(extra+"------");
				createRoom(request.data.get("type").getAsInt(), response,accesstoken,reqtime,extra);
			}
			else 
				joinroom(serviceid,session, request.data.get("roomid").getAsInt(), -1, response,accesstoken,reqtime);
		}
		session.send(response);
	}
	private boolean createRoom(int type,Response response,String token,long reqtime,JsonObject extra)
	{
		synchronized (lock) {
			IRoom room=RoomManager.createRoom(session,user,token,type,response,reqtime,extra);
			if(room!=null)
			{
				curRoom=room;
				return true;
			}
			return false;
		}
	}
	public boolean joinroom(String serviceid,Session session,int roomid,int chairid,Response response,String token,long reqtime) throws Exception
	{
		synchronized(lock)
		{
			IRoom room=RoomManager.joinRoom(serviceid,user,token, session,roomid,chairid,response,reqtime);
			if(room!=null)
			{
				curRoom=room;
				return true;
			}
		}
		return false;
	}
	@MessageHandler("1002")// 客户端发送消息到聊天群
	public void sendMsg(Session session,Request request,Response response) throws Exception
	{
		
	}
	@MessageHandler("exitroom")// 退出房间
	public void exitroom(ChannelHandlerContext ctx,Request request,Response response) throws Exception
	{
		if(session==null)
			return;
		synchronized (lock) {
			boolean rs=RoomManager.exitRoom(user,curRoom, session, response);
			if(rs)
				curRoom=null;
		}
		session.send(response);
	}
	@MessageHandler("gamemsg")// 游戏信息
	public void gamemsg(ChannelHandlerContext ctx ,Request request,Response response) throws Exception
	{
		if(session==null)
			return;
		if(curRoom==null)
		{
			session.send(response.setStatus(Status.FAIL.code).setMsg("你当前不在房间中"));
			return;
		}
		curRoom.onMessage(request.data, user, session);
	   
	}
	private void handleRobotRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
		
		try {
			Map<String, String>args=RequestParser.parse(request);
			String op=args.get("op");
			if("createroom".equals(op))
				robotCreateRoom(args, ctx, request);
			else if("roomstate".equals(op))
				sendRoomState(args, ctx, request);
			else 
				throw new Exception("not found");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sendHttpResponse(ctx, request, HttpResponseStatus.OK,"{\"status\":-1}");
		}
	}
	private void robotCreateRoom(Map<String, String>args,ChannelHandlerContext ctx, FullHttpRequest request)throws Exception
	{
		String robotid=args.get("robotid");
		int type=Integer.parseInt(args.get("type"));
		String gid=args.get("gid");
		long uid=Long.parseLong(args.get("uid"));
		String rmiurl=args.get("rmi");
		if(StringUtil.isNullOrEmpty(robotid,gid))
			throw new Exception("缺少参数");
		Response rsp=new Response();
		String extra=args.get("extra");
		RoomManager.autoCreateRoom(session, uid, type, gid, robotid,rmiurl,rsp,extra);
		sendHttpResponse(ctx, request, HttpResponseStatus.OK, rsp.toString());
	}
	private void sendRoomState(Map<String, String>args,ChannelHandlerContext ctx, FullHttpRequest request)throws Exception
	{
		String serviceid=args.get("serviceid");
		int count=RoomManager.getRoomPlayerCount(serviceid);
		JSONObject js=new JSONObject();
		js.put("status", 200);
		js.put("curplayercount", count);
		sendHttpResponse(ctx, request, HttpResponseStatus.OK, js.toString());
	}
}
