package com.jumplife.jome.entity;

import java.util.Date;

public class Comment
{
	private int id;
	private String account;
	private Date time;
	private String context;
	private int record_id;
	private String userName;
	private String movieName;
	
	public Comment()
	{
		this(-1, "", new Date(), "",-1);
	}
	
	public Comment(int id, String account, Date time, String context)
	{
		this.id = id;
		this.account = account;
		this.time = time;
		this.context = context;
		this.record_id = -1;
		this.userName = "";
		this.movieName = "";
	}
	
	public Comment(int id, String account, Date time, String context, int record_id)
	{
		this.id = id;
		this.account = account;
		this.time = time;
		this.context = context;
		this.record_id = record_id;
		this.userName = "";
		this.movieName = "";
	}
	
	public Comment(int id, String account, Date time, String context, int record_id, String userName, String movieName)
	{
		this.id = id;
		this.account = account;
		this.time = time;
		this.context = context;
		this.record_id = record_id;
		this.userName = userName;
		this.movieName = movieName;
	}
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public String getAccount()
	{
		return account;
	}
	public void setAccount(String account)
	{
		this.account = account;
	}
	public Date getTime()
	{
		return time;
	}
	public void setTime(Date time)
	{
		this.time = time;
	}
	public String getContext()
	{
		return context;
	}
	public void setContext(String context)
	{
		this.context = context;
	}
	
	public int getRecordId()
	{
		return record_id;
	}
	public void setSetReordId(int id)
	{
		this.record_id = id;
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public String getMovieName()
	{
		return movieName;
	}

	public void setMovieName(String movieName)
	{
		this.movieName = movieName;
	}
	
	
	
}
