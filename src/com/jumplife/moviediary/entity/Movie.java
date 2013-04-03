package com.jumplife.moviediary.entity;

import java.util.ArrayList;
import java.util.Date;

public class Movie {
	private int id;
	private String chineseName;
	private String englishName;
	private String inttroduction;
	private Date releaseDate;
	private String posterUrl;
	private int runningTime;
	private String levelUrl;
	private ArrayList<String> actors;
	private ArrayList<String> directors;
	
	private ArrayList<Record> recordList;
	private String youtubeId;
	private int recordsCount;
	private int goodCount;
	private boolean isEzding = false;
	
	public Movie() {
		this(-1, "", "", "", new Date(), "", -1, "", new ArrayList<String>(10), new ArrayList<String>(10), new ArrayList<Record>(10), "", 0, 0, false);
	}
	
	public Movie (int id, String chineseName, String englishName, String introduction, Date releaseDate, String posterUrl, 
				  int runningTime, String levelUrl, ArrayList<String> actors, ArrayList<String> directors, 
				  ArrayList<Record> recordList, String youtubeId, int recordsCount, int goodCount, boolean isEzding) {
		this.id = id;
		this.chineseName = chineseName;
		this.englishName = englishName;
		this.inttroduction = introduction;
		this.releaseDate = releaseDate;
		this.posterUrl = posterUrl;
		this.runningTime = runningTime;
		this.levelUrl = levelUrl;
		this.actors = actors;
		this.directors = directors;
		this.recordList = recordList;
		this.youtubeId = youtubeId;
		this.recordsCount = recordsCount;
		this.goodCount = goodCount;
		this.isEzding = isEzding;
	}
	
	
	public int getId(){
		return id;
	}
	public void setId(int id){
		this.id = id;
	}
	public String getChineseName(){
		return chineseName;
	}
	public void setChineseName(String chineseName){
		this.chineseName = chineseName;
	}
	public String getEnglishName(){
		return englishName;
	}
	public void setEnglishName(String englishName){
		this.englishName = englishName;
	}
	public String getInttroduction(){
		return inttroduction;
	}
	public void setInttroduction(String inttroduction){
		this.inttroduction = inttroduction;
	}
	public Date getReleaseDate(){
		return releaseDate;
	}
	public void setReleaseDate(Date releaseDate){
		this.releaseDate = releaseDate;
	}
	public String getPosterUrl(){
		return posterUrl;
	}
	public void setPosterUrl(String posterUrl){
		this.posterUrl = posterUrl;
	}
	public int getRunningTime(){
		return runningTime;
	}
	public void setRunningTime(int runningTime){
		this.runningTime = runningTime;
	}
	public String getLevelUrl(){
		return levelUrl;
	}
	public void setLevelUrl(String levelUrl){
		this.levelUrl = levelUrl;
	}
	public ArrayList<String> getActors(){
		return actors;
	}
	public void setActors(ArrayList<String> actors){
		this.actors = actors;
	}
	public ArrayList<String> getDirectors(){
		return directors;
	}
	public void setDirectors(ArrayList<String> directors){
		this.directors = directors;
	}
	public ArrayList<Record> getRecordList(){
		return recordList;
	}
	public void setRecordList(ArrayList<Record> recordList){
		this.recordList = recordList;
	}
	public void setYoutubeId(String youtube){
		this.youtubeId = youtube;
	}
	public String getYoutubeId(){
		return youtubeId;
	}
	public String getYoutubeVideoUrl(){
		return "http://www.youtube.com/watch?v="+youtubeId;
	}
	public String getYoutubeVideoImage(){
		return "http://img.youtube.com/vi/" + youtubeId +"/0.jpg";
	}

	public int getRecordsCount(){
		return recordsCount;
	}
	public void setRecordsCount(int recordsCount){
		this.recordsCount = recordsCount;
	}
	public int getGoodCount(){
		return goodCount;
	}
	public void setGoodCount(int goodCount){
		this.goodCount = goodCount;
	}
	public void setIsEzding(boolean isEzding){
		this.isEzding = isEzding;
	}
	public boolean getIsEzding(){
		return this.isEzding;
	}
}
