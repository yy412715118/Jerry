package com.lebin.game.qdmj.define;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.lebin.server.util.Config;

public class TimerTaskMng {
	private static ScheduledExecutorService timer= Executors.newScheduledThreadPool(Config.getInt("time_thread", 32));
	public static ScheduledFuture<?> schedule(Runnable runnable,long delay){
		return timer.schedule(runnable, delay, TimeUnit.MILLISECONDS);
	}
}
