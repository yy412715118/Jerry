package com.lebin.game.module;

public interface IRecorder {
	public  void append(String file,byte[] data,boolean close);
	public  void append(String file,byte[] data);
	public  void append(String file,String data);
	public  void append(String file,String data,boolean close);
	public void close(String file);
	public void start();
}
