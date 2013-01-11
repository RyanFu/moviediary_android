package com.jumplife.moviediary.entity;

import java.util.ArrayList;
import java.util.Date;

import com.jumplife.jome.entity.Comment;
import com.jumplife.moviediary.api.MovieAPI;

public class Record {
	private int id;
	private Date checkinTime;
	private int score;
	private String comment;
	private User user;
	private Movie movie;
	private int loveCount;
	private boolean isLovedByUser;
	private ArrayList<Comment> commentList;
	
	public Record() {
		this (-1, new Date(), -1, "", new User(), new Movie(), null, 0, false);
	}
	
	public Record(int id, Date checkinTime, int score, String comment, User user, Movie movie, int loveCount, boolean isLovedByUser) {
		this.id = id;
		this.checkinTime = checkinTime;
		this.score = score;
		this.comment = comment;
		this.user = user;
		this.movie = movie;
		this.loveCount = loveCount;
		this.isLovedByUser = isLovedByUser;
	}
	
	public Record(int id, Date checkinTime, int score, String comment, User user, Movie movie, ArrayList<Comment> comments, int loveCount, boolean isLovedByUser) {
		this.id = id;
		this.checkinTime = checkinTime;
		this.score = score;
		this.comment = comment;
		this.user = user;
		this.movie = movie;
		this.commentList = comments;
		this.loveCount = loveCount;
		this.isLovedByUser = isLovedByUser;
	}
	
	public int getId(){
		return id;
	}
	public void setId(int id){
		this.id = id;
	}
	public Date getCheckinTime(){
		return checkinTime;
	}
	public void setCheckinTime(Date checkinTime){
		this.checkinTime = checkinTime;
	}
	public int getScore(){
		return score;
	}
	
	public String getScoreString(){
		String returnStr = "";
		switch(score){
		case MovieAPI.SCORE_GOOD:
			returnStr = "好看";
			break;
		case MovieAPI.SCORE_NORMAL:
			returnStr = "普通";
			break;
		case MovieAPI.SCORE_BAD:
			returnStr = "難看";
			break;
			
		}
		return returnStr;
	}
	
	public void setScore(int score){
		this.score = score;
	}
	public String getComment(){
		return comment;
	}
	public void setComment(String comment){
		this.comment = comment;
	}
	public User getUser(){
		return user;
	}
	public void setUser(User user){
		this.user = user;
	}
	public Movie getMovie(){
		return movie;
	}
	public void setMovie(Movie movie){
		this.movie = movie;
	}
	
	public ArrayList<Comment> getCommentList(){
		return commentList;
	}
	public void setCommentList(ArrayList<Comment> commentList){
		this.commentList = commentList;
	}
	
	public int getLoveCount(){
		return loveCount;
	}
	public void setLoveCount(int loveCount){
		this.loveCount = loveCount;
	}
	
	public boolean getIsLovedByUser(){
		return isLovedByUser;
	}
	public void setIsLovedByUser(boolean isLovedByUser){
		this.isLovedByUser = isLovedByUser;
	}
}
