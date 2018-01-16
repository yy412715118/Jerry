package com.lebin.game.utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.lebin.game.module.IRecorder;

public class LocalRecorder implements IRecorder{
	private BlockingQueue<Record>recordlist=new LinkedBlockingQueue();
	private Map<String, RandomAccessFile> map;
	private String savepath;
	public LocalRecorder(final int maxfile,String savepath)
	{
		savepath=savepath.replaceAll("\\\\", "/");
		if(!savepath.endsWith("/"))
			savepath+="/";
		this.savepath=savepath;
		map = new LinkedHashMap<String, RandomAccessFile>((int) Math.ceil(maxfile / 0.75f) + 1, 0.75f, true) {
		    @Override
		    protected boolean removeEldestEntry(Map.Entry<String, RandomAccessFile> eldest) {
		    	boolean rs= size() > maxfile;
		    	try {
		    		if(rs)
			    		eldest.getValue().close();
				} catch (Exception e) {
				}
		    	return rs;
		    }
		};
		
	}
	Runnable runnable=new Runnable() {
		
		@Override
		public void run() {
			while(true)
			{
				try {
					Record record=recordlist.take();
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
	public synchronized void append(String file,byte[] data,boolean close)
	{
		Record record=new Record();
		record.data=data;
		record.file=savepath+file;
		record.close=close;
		System.out.println(record.file);
		try {
			recordlist.put(record);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public synchronized void append(String file,byte[] data)
	{
		append(file, data, false);
	}
	public synchronized void append(String file,String data)
	{
		try {
			append(file, data.getBytes("utf-8"), false);
		} catch (Exception e) {
		}
	}
	public synchronized void append(String file,String data,boolean close)
	{
		try {
			append(file, data.getBytes("utf-8"), close);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	private void handler(Record record)
	{
		RandomAccessFile randomFile=null;
		if(record.close)
			randomFile=map.remove(record.file);
		else 
			randomFile =map.get(record.file);
		if(record.data!=null)
		{
			try {
				if(randomFile==null)
				{
					File file=new File(record.file);
					if(!file.getParentFile().exists())
						file.getParentFile().mkdirs();
					randomFile= new RandomAccessFile(record.file, "rw");
					if(!record.close)
						map.put(record.file, randomFile);
				}
				long fileLength = randomFile.length();
	            randomFile.seek(fileLength);
	            randomFile.write(record.data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(record.close&&randomFile!=null)
		{
			try {
				randomFile.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
			
		}
		
	}
	public synchronized void close(String file)
	{
		Record record=new Record();
		record.close=true;
		record.file=file;
		try {
			recordlist.put(record);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	class Record
	{
		String file;
		byte[] data;
		boolean close;
	}
}
