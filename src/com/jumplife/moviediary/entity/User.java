package com.jumplife.moviediary.entity;

import android.graphics.Bitmap;

public class User {	
	private String account;
	private String name;
	//use User.SEX_MALE or User.SEX_FEMALE 
	private String sex;
	private String birthday;
	private Bitmap icon;
	private int recordCount;
	private int friendCount;

	public static String SEX_MALE = "MALE";
	public static String SEX_FEMAILE = "FEMALE";

	public User() {
		this("", "", "", "");
	}
	
	public User(String account) {
		this(account, "", "", "");
	}
	
	public User(String account, String name, String sex, String string) {
		this.account = account;
		this.name = name;
		this.sex = sex;
		this.birthday = string;
		this.recordCount = -1;
		this.friendCount = -1;
	}
	
	public User(String account, String name, String sex, String birthday, int recordCount, int friendCount) {
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
	
	public String getBirthday() {
		return birthday;
	}
	
	public void setBirthday(String date) {
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
