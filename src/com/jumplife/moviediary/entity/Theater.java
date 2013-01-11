package com.jumplife.moviediary.entity;

import java.util.ArrayList;

public class Theater{
	private int id;
	private String name;
	private ArrayList<Movie> movies;
	private String timeTable;
	private String buyLink;
	private String area;
	
	public Theater (){
		this(-1, "", new ArrayList<Movie> (10), "", "", "");
	}
	
	public Theater (int id, String name) {
		this(id, name, new ArrayList<Movie> (10), "", "", "");
	}
	
	public Theater (int id, String name, String timeTable, String buyLink, String area) {
		this(id, name, new ArrayList<Movie> (1), timeTable, buyLink, area);
	}
	
	public Theater (int id, String name, ArrayList<Movie> movies, String timeTable, String buyLink, String area) {
		this.id = id;
		this.name = name;
		this.movies = movies;
		this.timeTable = timeTable;
		this.buyLink = buyLink;
		this.area = area;
	}
	
	public int getId(){
		return id;
	}
	public void setId(int id){
		this.id = id;
	}

	public String getName(){
		return name;
	}
	public void setName(String name){
		this.name = name;
	}
	public ArrayList<Movie> getMovies(){
		return movies;
	}
	public void setMovies(ArrayList<Movie> movies){
		this.movies = movies;
	}
	
	public String getTimeTable(){
		return timeTable;
	}
	public void setTimeTable(String timeTable){
		this.timeTable = timeTable;
	}
	
	public String getBuyLink(){
		return buyLink;
	}
	public void setBuyLink(String buyLink){
		this.buyLink = buyLink;
	}
	
	public String getArea(){
		return area;
	}
	public void setArea(String area){
		this.area = area;
	}
}


