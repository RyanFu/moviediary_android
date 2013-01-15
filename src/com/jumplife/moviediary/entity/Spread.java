package com.jumplife.moviediary.entity;

public class Spread {
	private int id;
	private String imageUrl;

    private String spreadPosterUrl;   
	private String spreadTitle;
    private String spreadTitleContent;
    private String spreadTimeContent;
    private String spreadMethodStep;
    private String spreadMethodStepUrl;
    private String spreadMethodResult;
    private String spreadGiftContent;
    private String spreadGiftUrl;
    private String spreadNotifyContent;
	
	public Spread() {
		this(-1, "", "", "", "", "", "", "", "", "", "", "");
	}
	
	public Spread(int id, String imageUrl, String spreadPosterUrl, String spreadTitle, String spreadTitleContent,
			String spreadTimeContent, String spreadMethodStep, String spreadMethodStepUrl, String spreadMethodResult, 
			String spreadGiftContent, String spreadGiftUrl, String spreadNotifyContent) {
		this.id = id;
		this.imageUrl = imageUrl;
		this.spreadPosterUrl = spreadPosterUrl;   
		this.spreadTitle = spreadTitle;
		this.spreadTitleContent = spreadTitleContent;
		this.spreadTimeContent = spreadTimeContent;
		this.spreadMethodStep = spreadMethodStep;
		this.spreadMethodStepUrl = spreadMethodStepUrl;
		this.spreadMethodResult = spreadMethodResult;
		this.spreadGiftContent = spreadGiftContent;
		this.spreadGiftUrl = spreadGiftUrl;
		this.spreadNotifyContent = spreadNotifyContent;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getSpreadPosterUrl() {
		return spreadPosterUrl;
	}
	public void setSpreadPosterUrl(String spreadPosterUrl) {
		this.spreadPosterUrl = spreadPosterUrl;
	}
	public String getSpreadTitle() {
		return spreadTitle;
	}
	public void setSpreadTitle(String spreadTitle) {
		this.spreadTitle = spreadTitle;
	}
	public String getSpreadTitleContent() {
		return spreadTitleContent;
	}
	public void setSpreadTitleContent(String spreadTitleContent) {
		this.spreadTitleContent = spreadTitleContent;
	}
	public String getSpreadTimeContent() {
		return spreadTimeContent;
	}
	public void setSpreadTimeContent(String spreadTimeContent) {
		this.spreadTimeContent = spreadTimeContent;
	}
	public String getSpreadMethodStep() {
		return spreadMethodStep;
	}
	public void setSpreadMethodStep(String spreadMethodStep) {
		this.spreadMethodStep = spreadMethodStep;
	}
	public String getSpreadMethodStepUrl() {
		return spreadMethodStepUrl;
	}
	public void setSpreadMethodStepUrl(String spreadMethodStepUrl) {
		this.spreadMethodStepUrl = spreadMethodStepUrl;
	}
	public String getSpreadMethodResult() {
		return spreadMethodResult;
	}
	public void setSpreadMethodResult(String spreadMethodResult) {
		this.spreadMethodResult = spreadMethodResult;
	}
	public String getSpreadGiftContent() {
		return spreadGiftContent;
	}
	public void setSpreadGiftContent(String spreadGiftContent) {
		this.spreadGiftContent = spreadGiftContent;
	}
	public String getSpreadGiftUrl() {
		return spreadGiftUrl;
	}
	public void setSpreadGiftUrl(String spreadGiftUrl) {
		this.spreadGiftUrl = spreadGiftUrl;
	}
	public String getSpreadNotifyContent() {
		return spreadNotifyContent;
	}
	public void setSpreadNotifyContent(String spreadNotifyContent) {
		this.spreadNotifyContent = spreadNotifyContent;
	}
}
