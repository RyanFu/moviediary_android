package com.jumplife.moviediary.entity;

import java.util.Date;

import android.graphics.Bitmap;

public class User {	
	private String account;
	private String name;
	//use User.SEX_MALE or User.SEX_FEMALE 
	private String sex;
	private Date birthday;
	private Bitmap icon;
	private int recordCount;
	private int friendCount;

	public static String SEX_MALE = "MALE";
	public static String SEX_FEMAILE = "FEMALE";

	public User() {
		this("", "", "", new Date());
	}
	
	public User(String account) {
		this(account, "", "", new Date());
	}
	
	public User(String account, String name, String sex, Date birthday) {
		this.account = account;
		this.name = name;
		this.sex = sex;
		this.birthday = birthday;
		this.recordCount = -1;
		this.friendCount = -1;
	}
	
	public User(String account, String name, String sex, Date birthday, int recordCount, int friendCount) {
		this.account = account;
		this.name = name;
		this.sex = sex;
		this.birthday = birthday;
		this.recordCount = recordCount;
		this.friendCount = friendCount;
	}
	
	public String getAccount() {
		return account;
	}
	
	public void setAccount(String account) {
		this.account = account;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getSex() {
		return sex;
	}
	
	public void setSex(String sex) {
		this.sex = sex;
	}
	
	public Date getBirthday() {
		return birthday;
	}
	
	public void setBirthday(Date date) {
		this.birthday = date;
	}

	public Bitmap getIcon(){
		return icon;
	}

	public void setIcon(Bitmap icon){
		this.icon = icon;
	}
	
	public String getIconUrl(){
		return "http://graph.facebook.com/" + this.account + "/picture?type=square";
	}

	public int getRecordCount(){
		return recordCount;
	}

	public void setRecordCount(int recordCounnt){
		this.recordCount = recordCounnt;
	}

	public int getFriendCount()
	{
		return friendCount;
	}

	public void setFriendCount(int friendCount)
	{
		this.friendCount = friendCount;
	}
	
}
