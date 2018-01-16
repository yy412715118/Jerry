package com.lebin.game.qdmj.define;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

public class FileUtils {
	public static byte[] readyByteArray(String file)
	{
		try {
			FileInputStream fIn=new FileInputStream(file);
			byte[] data=new byte[1024];
			ByteArrayOutputStream bout=new ByteArrayOutputStream();
			int len;
			while((len=fIn.read(data))!=-1)
				bout.write(data,0,len);
			fIn.close();
			bout.close();
			return bout.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static String readString(String file,String charset)
	{
		try {
			byte[] data=readyByteArray(file);
			if(data!=null)
			{
				if(charset==null)
					return new String(data);
				else
					return new String(data, charset);
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
