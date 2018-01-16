package com.hogense.game.server.listener;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;

public class LifeCycleLisener implements LifeCycle.Listener{
	private Server server;
	public LifeCycleLisener(Server server) {
		this.server=server;
	}
	@Override
	public void lifeCycleFailure(LifeCycle arg0, Throwable arg1) {
		System.out.println("lifeCycleFailure");
		
	}

	@Override
	public void lifeCycleStarted(LifeCycle arg0) {
		// TODO Auto-generated method stub
		System.out.println("lifeCycleStarted");
	}

	@Override
	public void lifeCycleStarting(LifeCycle arg0) {
		System.out.println("lifeCycleStarting");
	}

	@Override
	public void lifeCycleStopped(LifeCycle arg0) {
		System.out.println("lifeCycleStopped");
	}

	@Override
	public void lifeCycleStopping(LifeCycle arg0) {
		// TODO Auto-generated method stub
		System.out.println("lifeCycleStopping");
	}

}
