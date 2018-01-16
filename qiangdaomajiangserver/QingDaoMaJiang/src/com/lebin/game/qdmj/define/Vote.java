package com.lebin.game.qdmj.define;

import java.util.Arrays;

public class Vote extends SerializaableObject{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public byte[] vote;
	public byte[] result;
	public byte state;
	public long lefttime;
	public Vote(int size)
	{
		this.vote=new byte[size];
		this.result=new byte[size];
		this.state=0;
	}
	public void vote(int chairid,boolean agree)
	{
		if(state!=0||this.vote[chairid]!=0)
			return;
		this.vote[chairid]=(byte)(agree?1:-1);
		for(int i=0;i<this.result.length;i++)
		{
			if(this.result[i]==0)
			{
				this.result[i]=this.vote[chairid];
				break;
			}
		}
		int agreecount=0;
		int disagreecount=0;
		for(byte v:vote)
		{
			if(v==1)
				agreecount++;
			else if(v==-1)
				disagreecount++;
		}
		if(agreecount>this.vote.length/2)
			state=1;
		else if(disagreecount>this.vote.length/2)
			state=-1;
		else if(disagreecount+agreecount==this.vote.length)
			state=-1;
		else
			state=0;
	}
	public void start()
	{
		state=0;
		Arrays.fill(vote, (byte)0);
		Arrays.fill(result, (byte)0);
	}
}
