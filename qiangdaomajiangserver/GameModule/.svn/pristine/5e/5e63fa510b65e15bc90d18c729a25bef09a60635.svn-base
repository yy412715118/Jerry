package com.lebin.game.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.lebin.game.module.IRecorder;

public class RemoteRecorder implements IRecorder{
	private String url;
	private BlockingQueue<RemoteRecord>recordlist=new LinkedBlockingQueue();
	public RemoteRecorder(String url) {
		this.url=url;
	}
	Runnable runnable=new Runnable() {
		
		@Override
		public void run() {
			while(true)
			{
				try {
					RemoteRecord record=recordlist.take();
					if(record!=null)
						handler(record);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	Thread thread;
	public synchronized void start()
	{
		if(thread!=null)
			return;
		thread=new Thread(runnable);
		thread.start();
	}
	private void handler(RemoteRecord record)
	{
		try {
			Map<String, String>head=new HashMap<>();
			head.put("filename", record.filename);
			String rsp=HttpRequest.post(url,head,record.data);
			if(rsp==null||!rsp.trim().equals("1"))
				recordlist.put(record);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public synchronized void append(String file, byte[] data, boolean close) {
		
		RemoteRecord record=new RemoteRecord();
		record.filename=file;
		record.data=data;
		recordlist.add(record);
	}

	@Override
	public synchronized void append(String file, byte[] data) {
		
		append(file, data, true);
	}

	@Override
	public synchronized void append(String file, String data) {
		try {
			append(file, data.getBytes("utf-8"), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public synchronized void append(String file, String data, boolean close) {
		try {
			append(file, data.getBytes("utf-8"), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void close(String file) {
		
		
	}
	static class RemoteRecord
	{
		String filename;
		byte[] data;
	}
}
