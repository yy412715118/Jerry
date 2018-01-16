package com.lebin.server.netty.server;

import java.lang.management.ManagementFactory;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hogense.roomgame.databaseclient.ApplicationContext;
import com.lebin.game.module.IRoom;
import com.lebin.game.utils.HttpRequest;
import com.lebin.server.netty.serviceimpl.RobotMessageSender;
import com.lebin.server.netty.serviceimpl.RoomManager;
import com.lebin.server.util.Config;
import com.lebin.server.util.ConfigInfo;
import com.lebin.server.util.ImpClassMapping;

import atg.taglib.json.util.JSONObject;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * WebSocket服务
 *
 */
public class RoomServer {
	public static ConfigInfo configInfo;
	private static final Logger LOG = LoggerFactory.getLogger(RoomServer.class);
	public void run(ConfigInfo configInfo) throws Exception {
		RoomServer.configInfo=configInfo;
		if(!reg(configInfo))
			return;
		ApplicationContext.load("config/dubbo.xml");
		final EventExecutorGroup executorGroup = new DefaultEventExecutorGroup(Config.getInt("execute_thread", 16));
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		MessageHandlerFactory.load();
		try {
			ServerBootstrap b = new ServerBootstrap();
		
			b.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.TCP_NODELAY, true)
			.option(ChannelOption.SO_BACKLOG, 1024)
			.childHandler(new ChannelInitializer<Channel>() {

				@Override
				protected void initChannel(Channel channel) throws Exception {
					ChannelPipeline pipeline = channel.pipeline();
					pipeline.addLast(new IdleStateHandler(10, 10, 0));
					pipeline.addLast("http-codec", new HttpServerCodec()); // Http消息编码解码
					pipeline.addLast("aggregator", new HttpObjectAggregator(65536)); // Http消息组装
					pipeline.addLast("http-chunked", new ChunkedWriteHandler()); // WebSocket通信支持
					//pipeline.addLast("handler", new  WebSocketServerProtocolHandler("room/","",true)); // WebSocket服务端Handler
					//pipeline.addLast(executorGroup,"handler",new  RoomServerHandler()); // WebSocket服务端Handler
					pipeline.addLast("handler",new  RoomServerHandler(executorGroup)); // WebSocket服务端Handler
				}
				
			});
			
			Channel channel = b.bind(configInfo.port).sync().channel();
			RoomManager.init(configInfo,ImpClassMapping.get(IRoom.class));
			startTimer();
			RobotMessageSender.getIntance().start();
			System.out.println(ManagementFactory.getRuntimeMXBean().getName());
			LOG.info("RoomServer 已经启动，serverid："+configInfo.serverid+"，host："+configInfo.host+"，端口：" + configInfo.port + ".");
			channel.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	private void startTimer()
	{
		Timer timer = new Timer();
	    timer.scheduleAtFixedRate(new TimerTask() {
	      public void run() {
	    	  try {
	    		  sendStatus();
			} catch (Exception e) {
				LOG.error(e.getMessage(),e);
			}
	    	 
	      }
	    }, 5000, 30000);
	}
	private boolean reg(ConfigInfo configInfo)
	{
		try {
			LOG.info("----注册服务"+configInfo);
			String rsp=HttpRequest.get(configInfo.centerhost+"/api/server/reg?host="+configInfo.host
					+"&port="+configInfo.port+"&gameid="+configInfo.gameid+"&id="
					+configInfo.serverid+"&maxroom="+configInfo.maxroomcount);
			if(rsp==null)
			{
				LOG.error("无法注册服务");
				return false;
			}
			JSONObject js=new JSONObject(rsp);
			if(js.getInt("status")==200)
			{
				LOG.info("注册服务成功");
				return true;
			}
			else
			{
				LOG.error("无法注册服务,错误："+js.getInt("status")+","+js.getString("msg"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	private void sendStatus() throws Exception
	{
		String rsp=HttpRequest.get(configInfo.centerhost+"/api/server/syn?host="+configInfo.host
				+"&port="+configInfo.port+"&gameid="+configInfo.gameid+"&id="+configInfo.serverid
				+"&maxroom="+configInfo.maxroomcount+"&roomcount="+RoomManager.getRoomCount()
				+"&usercount="+RoomServerHandler.usercount.get());
		if(rsp==null)
		{
			LOG.warn("无法访问中心服务器");
			return;
		}
		JSONObject js=new JSONObject(rsp);
		if(js.getInt("status")!=200)
			LOG.warn("状态同步失败"+js.getInt("status")+","+js.getString("msg"));
			
	}
	
}
