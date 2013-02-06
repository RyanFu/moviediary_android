package com.jumplife.moviediary.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.jumplife.jome.entity.Comment;
import com.jumplife.moviediary.entity.Area;
import com.jumplife.moviediary.entity.Movie;
import com.jumplife.moviediary.entity.News;
import com.jumplife.moviediary.entity.Record;
import com.jumplife.moviediary.entity.Spread;
import com.jumplife.moviediary.entity.Theater;
import com.jumplife.moviediary.entity.User;

public class MovieAPI {

	private String urlAddress;
	private HttpURLConnection connection;
	private String requestedMethod;
	private int connectionTimeout;
	private int readTimeout;
	private boolean usercaches;
	private boolean doInput;
	private boolean doOutput;
	
	public static final String TAG = "MOVIE_API";
	public static final boolean DEBUG = true;
	
	public static final String FILTER_RECENT = "FILTER_RECENT";
	public static final String FILTER_FIRST_ROUND = "FILTER_FIRST_ROUND";
	public static final String FILTER_SECOND_ROUND = "FILTER_SECOND_ROUND";
	public static final String FILTER_TOP_10 = "FILTER_TOP_10";
	public static final String FILTER_THIS_WEEK = "FILTER_THIS_WEEK";
	public static final String FILTER_ALL = "FILTER_ALL";
	
	public static final int SCORE_GOOD = 0;
	public static final int SCORE_NORMAL = 1;
	public static final int SCORE_BAD = 2;
	
	
	public MovieAPI(String urlAddress, int connectionTimeout, int readTimeout) {
		this.urlAddress = new String(urlAddress + "/");
		this.connectionTimeout = connectionTimeout;
		this.readTimeout = readTimeout;
		this.usercaches = false;
		this.doInput = true;
		this.doOutput = true;
	}
	public MovieAPI(String urlAddress) {
		this(new String(urlAddress), 5000, 5000);
	}
	
	public MovieAPI() {
		this(new String("http://106.187.101.252"));
	}
	
	public int connect(String requestedMethod, String apiPath) {
		int status = -1;
		try {
			URL url = new URL(urlAddress + apiPath);
			
			if(DEBUG)
				Log.d(TAG, "URL: " + url.toString());
			connection = (HttpURLConnection) url.openConnection();
					
			connection.setRequestMethod(requestedMethod);
			connection.setReadTimeout(this.readTimeout);
			connection.setConnectTimeout(this.connectionTimeout);
			connection.setUseCaches(this.usercaches);
			connection.setDoInput(this.doInput);
			connection.setDoOutput(this.doOutput);
			connection.setRequestProperty("Content-Type",  "application/json;charset=utf-8");
			
			connection.connect();

		} 
		catch (MalformedURLException e1) {
			e1.printStackTrace();
			return status;
		}
		catch (IOException e) {
			e.printStackTrace();
			return status;
		}
		
		return status;
	}
	
	public void disconnect()
	{
		connection.disconnect();
	}
	//取得縣市列表
	public ArrayList<Area> getAreaList() {
		ArrayList<Area> areaList = new ArrayList<Area> (20);
		
		String message = getMessageFromServer("GET", "/api/v1/areas.json", null);
		
		if(message == null) {
			return null;
		}
		else {
			JSONArray areaArray;
			try {
				areaArray = new JSONArray(message.toString());
				for(int i = 0; i < areaArray.length() ; i++) {
					JSONObject areaJson = areaArray.getJSONObject(i).getJSONObject("area");
					Area area = new Area(areaJson.getInt("id"), areaJson.getString("name"));
					areaList.add(area);
				}
				return areaList;
			} 
			catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}	
	}
	//取得戲院列表
	public ArrayList<Theater> getThreaterList(Area area) {
		
		ArrayList<Theater> threaterList = new ArrayList<Theater>(10);
		
		String message = getMessageFromServer("GET", "/api/v1/theaters.json?area_id=" + area.getId(), null);
		
		if(message == null) {
			return null;
		}
		else {
			JSONArray theaterArray;
			try {
				theaterArray = new JSONArray(message.toString());
				for (int i = 0; i < theaterArray.length() ; i++) {
					JSONObject theaterJson = theaterArray.getJSONObject(i).getJSONObject("theater");
					Theater theater = new Theater(theaterJson.getInt("id"), theaterJson.getString("name"));
					threaterList.add(theater);
				}
			} 
			catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		return threaterList;
	}
	//取得戲院電影時刻表
	public ArrayList<Theater> getThreaterTimeTableList(int movieId) {
		
		ArrayList<Theater> threaterList = new ArrayList<Theater>(10);
		
		String message = getMessageFromServer("GET", "/api/v1/movies/" + movieId + "/timetable.json", null);
		
		if(message == null) {
			return null;
		}
		else {
			JSONArray theaterArray;
			String buyLink = null;
			try {
				theaterArray = new JSONArray(message.toString());
				for (int i = 0; i < theaterArray.length() ; i++) {
					JSONObject theaterJson = theaterArray.getJSONObject(i).getJSONObject("movie_theater_ship");
					if(theaterJson.isNull("buy_link"))
						buyLink = null;
					else
						buyLink = theaterJson.getString("buy_link");
					
					//
					Theater theater = new Theater(theaterJson.getInt("id"), 
						theaterJson.getString("theater"), 
						theaterJson.getString("timetable"),
						buyLink,
						theaterJson.getString("area"));
					
					threaterList.add(theater);
				}
			} 
			catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		return threaterList;
	}
	//取得某一間戲院的電影列表
	public Theater getMovieList(Theater theater, String fbId) {
		ArrayList<Movie> movieList = new ArrayList<Movie>(10);
		String message = getMessageFromServer("GET", "/api/v1/movies.json?theater_id=" + theater.getId() + "&fb_id=" + fbId, null);
		
		if(message == null) {
			return null;
		}
		else {
			JSONArray movieArray;

			try
			{
				movieArray = new JSONArray(message.toString());
				
				for (int i = 0; i < movieArray.length() ; i++) {
					JSONObject movieJson = movieArray.getJSONObject(i).getJSONObject("movie");
					Movie movie = movieJsonToClass(movieJson);
					movieList.add(movie);
				}
			} 
			catch (JSONException e){
				e.printStackTrace();
				return null;
			}
			theater.setMovies(movieList);
		}
		return theater;
	}
	
	//取得新聞列表
	public ArrayList<News> getNewsList() {
		ArrayList<News> newsList = new ArrayList<News>(10);
		
		String message = getMessageFromServer("GET", "/api/v1/news.json", null);
		
		if(message == null) {
			return null;
		}
		else {
			JSONArray newsArray;
			
			try {
				newsArray = new JSONArray(message.toString());
				for(int i = 0; i < newsArray.length(); i++) {
					JSONObject newsJson = newsArray.getJSONObject(i).getJSONObject("news");
					String title = newsJson.getString("title");
					String thumbnailUrl = newsJson.getString("thumbnail_url");
					
					News news = new News();
				
					if(!(newsJson.isNull("created_at"))){
						DateFormat releaseFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
						Date date = releaseFormatter.parse(newsJson.getString("created_at"));
						news.setReleaseDate(date);
					}
					
					news.setTitle(title);
					news.setThumbnailUrl(thumbnailUrl);
					int type = newsJson.getInt("news_type");
					news.setType(type);
					
					if(type == News.TYPE_LINK){
						if(!(newsJson.isNull("link"))){
							String link = newsJson.getString("link");
							news.setLink(link);
						}
					}
					else if (type == News.TYPE_PIC){
						if(!(newsJson.isNull("picture_url"))){
							String pictureUrl = newsJson.getString("picture_url");
							news.setPictureUrl(pictureUrl);
						}
						if(!(newsJson.isNull("content"))){
							String content = newsJson.getString("content");
							news.setContent(content);
							//news.setTitle(content);
						}
					}
					
					newsList.add(news);
					
					int a = 0;
					a = a + 1;
				}
			} 
			catch (JSONException e) {
				e.printStackTrace();
				return null;
			} catch (ParseException e){
				e.printStackTrace();
				return null;
			}
			
		}
		
		return newsList;
	}
	
	//取得新聞列表
	public ArrayList<News> getNewsList(int page) {
		ArrayList<News> newsList = new ArrayList<News>(10);
		
		String message = getMessageFromServer("GET", "/api/v1/news.json?page=" + page, null);
		
		if(message == null) {
			return null;
		}
		else {
			JSONArray newsArray;
			
			try {
				newsArray = new JSONArray(message.toString());
				for(int i = 0; i < newsArray.length(); i++) {
					JSONObject newsJson = newsArray.getJSONObject(i).getJSONObject("news");
					String title = newsJson.getString("title");
					String thumbnailUrl = newsJson.getString("thumbnail_url");
					
					News news = new News();
				
					if(!(newsJson.isNull("created_at"))){
						DateFormat releaseFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
						Date date = releaseFormatter.parse(newsJson.getString("created_at"));
						news.setReleaseDate(date);
					}
					
					if(!(newsJson.isNull("source")))
						news.setSource(newsJson.getString("source"));
					
					news.setTitle(title);
					news.setThumbnailUrl(thumbnailUrl);
					int type = newsJson.getInt("news_type");
					news.setType(type);
					
					if(type == News.TYPE_LINK){
						if(!(newsJson.isNull("link"))){
							String link = newsJson.getString("link");
							news.setLink(link);
						}
					}
					else if (type == News.TYPE_PIC){
						if(!(newsJson.isNull("picture_url"))){
							String pictureUrl = newsJson.getString("picture_url");
							news.setPictureUrl(pictureUrl);
						}
						if(!(newsJson.isNull("content"))){
							String content = newsJson.getString("content");
							news.setContent(content);
						}
					}
					
					newsList.add(news);
					
					int a = 0;
					a = a + 1;
				}
			} 
			catch (JSONException e) {
				e.printStackTrace();
				return null;
			} catch (ParseException e){
				e.printStackTrace();
				return null;
			}
			
		}
		
		return newsList;
	}
		
	//取得首輪, 二輪, Top10, 所有的電影列表
	public ArrayList<Movie> getMovieList (String filter) {
		ArrayList<Movie> movieList = new ArrayList<Movie>(10);
		String message = "";
		
		if(filter.equalsIgnoreCase(MovieAPI.FILTER_RECENT)) {
			message = getMessageFromServer("GET", "/api/v1/movies/comming.json", null);
		}
		else if(filter.equalsIgnoreCase(MovieAPI.FILTER_FIRST_ROUND)) {
			message = getMessageFromServer("GET", "/api/v1/movies/first_round.json", null);
		}
		else if (filter.equalsIgnoreCase(MovieAPI.FILTER_SECOND_ROUND)) {
			message = getMessageFromServer("GET", "/api/v1/movies/second_round.json", null);
		}
		else if (filter.equalsIgnoreCase(MovieAPI.FILTER_TOP_10)) {
			message = getMessageFromServer("GET", "api/v1/movies/hot.json", null);
		}
		else if (filter.equalsIgnoreCase(MovieAPI.FILTER_THIS_WEEK)) {
			message = getMessageFromServer("GET", "api/v1/movies/this_week.json", null);
		}
		else if (filter.equalsIgnoreCase(MovieAPI.FILTER_ALL)) {
			message = getMessageFromServer("GET", "api/v1/movies/all.json", null);
		}
		
		if(message == null) {
			return null;
		}
		else {
			JSONArray movieArray;
			try {
				movieArray = new JSONArray(message.toString());
				for (int i = 0; i < movieArray.length() ; i++) {
					JSONObject movieJson = movieArray.getJSONObject(i).getJSONObject("movie");
					Movie movie = new Movie();
					movie.setId(movieJson.getInt("id"));
					movie.setChineseName(movieJson.getString("name"));
					movie.setEnglishName(movieJson.getString("name_en"));
					movie.setPosterUrl(movieJson.getString("poster_url"));
					movie.setGoodCount(movieJson.getInt("good_count"));
					
					if(!movieJson.isNull("records_count")) {
						movie.setRecordsCount(movieJson.getInt("records_count"));
					}
					
					if (!filter.equalsIgnoreCase(MovieAPI.FILTER_ALL))
						movie.setYoutubeId(movieJson.getString("youtube_video_id"));
					movieList.add(movie);
				}
				
			} 
			catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		
		return movieList;
	}
	
	//取得詳細電影資訊
	public Movie getMovieMoreInfo (int movieId, String fbId) {
		//http://106.187.101.252/api/v1/movies/1.json?fb_id=3346731036355
		String message = getMessageFromServer("GET", "/api/v1/movies/" + movieId + ".json?fb_id=" + fbId, null);
		//DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		
		Movie movie = null;
		
		if(message == null) {
			return null;
		}
		else {
			JSONObject movieJson;
			try {
				movieJson = new JSONObject(message).getJSONObject("movie");
				movie = movieJsonToClass(movieJson);
				
			} 
			catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		return movie;
	}
	
	/*
	 * {"record" => {"comment" => "my comment", "score" => "my score", "movie_id" => 1 },
"fb_id" => "3346731036355"
}
	 */
	public int recordMovie(Record record) {
		int result = -1;
		
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost("http://106.187.101.252/api/v1/records.json");
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		    nameValuePairs.add(new BasicNameValuePair("comment", record.getComment()));
		    nameValuePairs.add(new BasicNameValuePair("score", String.valueOf(record.getScore())));
		    nameValuePairs.add(new BasicNameValuePair("movie_id", String.valueOf(record.getMovie().getId())));
		    nameValuePairs.add(new BasicNameValuePair("fb_id", record.getUser().getAccount()));
		    
		    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
			
			//httpPost.setEntity(new ByteArrayEntity(requestJson.toString().getBytes("UTF8")));
			HttpResponse response = httpClient.execute(httpPost);			
			StatusLine statusLine =  response.getStatusLine();
			if (statusLine.getStatusCode() == 200){
				result = 1;
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			String json = reader.readLine();
			JSONObject recordJson = new JSONObject(json);
			if(recordJson.has("message")) {
				String alreadyExist = recordJson.getString("message");
				if(alreadyExist.equals("already_exist")) {
					result = 2;
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return result;
		} catch (ClientProtocolException e)
		{
			e.printStackTrace();
			return result;
		} catch (IOException e)
		{
			e.printStackTrace();
			return result;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return result;
	}
		
	public ArrayList<Record> getMovieFriendRecordList (String fbId, String movieId) {
		ArrayList<Record> recordList = new ArrayList<Record> (10);
		
		String message = getMessageFromServer("GET", "/api/v1/records.json?fb_id=" + fbId + "&movie_id=" + movieId, null);
		
		if(message == null) {
			return null;
		}
		try {
			JSONArray recordArray = new JSONArray(message);
			for(int i = 0; i < recordArray.length(); i++) {
				JSONObject recordJson  = recordArray.getJSONObject(i).getJSONObject("record");
				
				JSONObject movieJson = recordJson.getJSONObject("movie");
				
				Movie movie = new Movie();
				movie.setChineseName(movieJson.getString("name"));
				movie.setPosterUrl(movieJson.getString("poster_url"));
				
				User user = new User();
				user.setName(recordJson.getString("user_name"));
				user.setAccount(recordJson.getString("user_fb_id"));
				
				int loveCount = 0;
				if(!(recordJson.isNull("love_count")))
					loveCount = recordJson.getInt("love_count");
				
				boolean isLovedByUser = false;
				if(!(recordJson.isNull("is_loved_by_user")))
					isLovedByUser = recordJson.getBoolean("is_loved_by_user");
				
				Record record = new Record();
				record.setId(recordJson.getInt("id"));
				record.setMovie(movie);
				record.setComment(recordJson.getString("comment"));
			    record.setScore(recordJson.getInt("score"));
				record.setUser(user);
				record.setLoveCount(loveCount);
				record.setIsLovedByUser(isLovedByUser);
				
				if(!(recordJson.isNull("created_at"))){
					DateFormat releaseFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
					Date date = releaseFormatter.parse(recordJson.getString("created_at"));
					record.setCheckinTime(date);
				}
				
				recordList.add(record);
				
			}
		} 
		catch (JSONException e){
			e.printStackTrace();
			return null;
		}
		catch (ParseException e){
			e.printStackTrace();
			return null;
		}
		
		return recordList;
	}
	
	public ArrayList<Record> getMovieRecordList (String fbId, String movieId) {
		ArrayList<Record> recordList = new ArrayList<Record> (10);
		
		String message = getMessageFromServer("GET", "/api/v1/records/get_movie_records.json?fb_id=" + fbId + "&movie_id=" + movieId, null);
		
		if(message == null) {
			return null;
		}
		try {
			JSONArray recordArray = new JSONArray(message);
			for(int i = 0; i < recordArray.length(); i++) {
				JSONObject recordJson  = recordArray.getJSONObject(i).getJSONObject("record");
				
				JSONObject movieJson = recordJson.getJSONObject("movie");
				
				Movie movie = new Movie();
				movie.setChineseName(movieJson.getString("name"));
				movie.setPosterUrl(movieJson.getString("poster_url"));
				
				User user = new User();
				user.setName(recordJson.getString("user_name"));
				user.setAccount(recordJson.getString("user_fb_id"));
				
				int loveCount = 0;
				if(!(recordJson.isNull("love_count")))
					loveCount = recordJson.getInt("love_count");
				
				boolean isLovedByUser = false;
				if(!(recordJson.isNull("is_loved_by_user")))
					isLovedByUser = recordJson.getBoolean("is_loved_by_user");
				
				Record record = new Record();
				record.setId(recordJson.getInt("id"));
				record.setMovie(movie);
				record.setComment(recordJson.getString("comment"));
			    record.setScore(recordJson.getInt("score"));
				record.setUser(user);
				record.setLoveCount(loveCount);
				record.setIsLovedByUser(isLovedByUser);
				
				if(!(recordJson.isNull("created_at"))){
					DateFormat releaseFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
					Date date = releaseFormatter.parse(recordJson.getString("created_at"));
					record.setCheckinTime(date);
				}
				
				recordList.add(record);
				
			}
		} 
		catch (JSONException e){
			e.printStackTrace();
			return null;
		}
		catch (ParseException e){
			e.printStackTrace();
			return null;
		}
		
		return recordList;
	}
	
	//用fb id 取得comment 和 record 交錯的list
	public ArrayList<Object> getFriendStatusList (String fbId) {
		ArrayList<Object> statusList = new ArrayList<Object> (50);
		
		//http://106.187.101.252/api/v1/streams.json?fb_id=1511171039
		String message = getMessageFromServer("GET", "/api/v1/streams.json?fb_id=" + fbId, null);
		//String message = getMessageFromServer("GET", "/api/v1/records/friend_stream.json?fb_id=" + fbId, null);
		
		if(message == null) {
			return null;
		}
		try {
			JSONArray statusArray = new JSONArray(message);
			for(int i = 0; i < statusArray.length(); i++) {
				JSONObject streamJson  = statusArray.getJSONObject(i).getJSONObject("stream");
				
				//先取得type
				int type = streamJson.getInt("stream_type");
				//如果type 是  comment
				if(type == 2) {
					JSONObject commentJson = streamJson.getJSONObject("comment");
					//json: text, user_fb_id, created_at
					
					DateFormat releaseFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
					String context = commentJson.getString("text");
					String account = commentJson.getString("user_fb_id");
					String userName = commentJson.getString("user_name");
					int recordId = commentJson.getInt("record_id");
					Date date = releaseFormatter.parse(commentJson.getString("created_at"));
					/*
					Log.d(TAG, "context: " + context);
					Log.d(TAG, "account: " + account);
					Log.d(TAG, "userName: " + userName);
					Log.d(TAG, "record id: " + recordId);
					Log.d(TAG, "created_at: " + releaseFormatter.format(date));
					*/
					//get the movie name
					String movieName = commentJson.getString("movie_name");
	
					Comment comment = new Comment(-1, account, date, context, recordId, userName, movieName);
					
					statusList.add(comment);
				}
				//如果type 是  record
				else if(type == 1) {
					JSONObject recordJson  = streamJson.getJSONObject("record");
					
					JSONObject movieJson = recordJson.getJSONObject("movie");
					
					Movie movie = new Movie();
					movie.setChineseName(movieJson.getString("name"));
					movie.setPosterUrl(movieJson.getString("poster_url"));
					movie.setId(recordJson.getInt("movie_id"));
					
					User user = new User();
					user.setName(recordJson.getString("user_name"));
					user.setAccount(recordJson.getString("user_fb_id"));
					
					int loveCount = 0;
					if(!(recordJson.isNull("love_count")))
						loveCount = recordJson.getInt("love_count");
					
					boolean isLovedByUser = false;
					if(!(recordJson.isNull("is_loved_by_user")))
						isLovedByUser = recordJson.getBoolean("is_loved_by_user");
					
					Record record = new Record();
					record.setId(recordJson.getInt("id"));
					record.setComment(recordJson.getString("comment"));
				    record.setScore(recordJson.getInt("score"));
					record.setMovie(movie);
					record.setUser(user);
					record.setLoveCount(loveCount);
					record.setIsLovedByUser(isLovedByUser);
					
					if(!(recordJson.isNull("created_at"))){
						DateFormat releaseFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
						Date date = releaseFormatter.parse(recordJson.getString("created_at"));
						record.setCheckinTime(date);
					}
					
					statusList.add(record);
				}
			}
		}
		catch (JSONException e){
			e.printStackTrace();
			return null;
		}
		catch (ParseException e){
			e.printStackTrace();
			return null;
		}
		
		return statusList;
	}
	
	public ArrayList<Object> getFriendStatusListWithPage (String fbId, int page) {
		ArrayList<Object> statusList = new ArrayList<Object> (50);
		
		String message = getMessageFromServer("GET", "api/v1/streams/streams_with_page.json?fb_id=" + fbId + "&page=" + page, null);
		
		if(message == null) {
			return null;
		}
		try {
			JSONArray statusArray = new JSONArray(message);
			for(int i = 0; i < statusArray.length(); i++) {
				JSONObject streamJson  = statusArray.getJSONObject(i).getJSONObject("stream");
				
				//先取得type
				int type = streamJson.getInt("stream_type");
				//如果type 是  comment
				if(type == 2) {
					JSONObject commentJson = streamJson.getJSONObject("comment");
					//json: text, user_fb_id, created_at
					
					DateFormat releaseFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
					String context = commentJson.getString("text");
					String account = commentJson.getString("user_fb_id");
					String userName = commentJson.getString("user_name");
					int recordId = commentJson.getInt("record_id");
					Date date = releaseFormatter.parse(commentJson.getString("created_at"));
					/*
					Log.d(TAG, "context: " + context);
					Log.d(TAG, "account: " + account);
					Log.d(TAG, "userName: " + userName);
					Log.d(TAG, "record id: " + recordId);
					Log.d(TAG, "created_at: " + releaseFormatter.format(date));
					*/
					//get the movie name
					String movieName = commentJson.getString("movie_name");
	
					Comment comment = new Comment(-1, account, date, context, recordId, userName, movieName);
					
					statusList.add(comment);
				}
				//如果type 是  record
				else if(type == 1) {
					JSONObject recordJson  = streamJson.getJSONObject("record");
					
					JSONObject movieJson = recordJson.getJSONObject("movie");
					
					Movie movie = new Movie();
					movie.setChineseName(movieJson.getString("name"));
					movie.setPosterUrl(movieJson.getString("poster_url"));
					movie.setId(recordJson.getInt("movie_id"));
					
					User user = new User();
					user.setName(recordJson.getString("user_name"));
					user.setAccount(recordJson.getString("user_fb_id"));
					
					int loveCount = 0;
					if(!(recordJson.isNull("love_count")))
						loveCount = recordJson.getInt("love_count");
					
					boolean isLovedByUser = false;
					if(!(recordJson.isNull("is_loved_by_user")))
						isLovedByUser = recordJson.getBoolean("is_loved_by_user");
					
					Record record = new Record();
					record.setId(recordJson.getInt("id"));
					record.setComment(recordJson.getString("comment"));
				    record.setScore(recordJson.getInt("score"));
					record.setMovie(movie);
					record.setUser(user);
					record.setLoveCount(loveCount);
					record.setIsLovedByUser(isLovedByUser);
					
					if(!(recordJson.isNull("created_at"))){
						DateFormat releaseFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
						Date date = releaseFormatter.parse(recordJson.getString("created_at"));
						record.setCheckinTime(date);
					}
					
					statusList.add(record);
				}
			}
		}
		catch (JSONException e){
			e.printStackTrace();
			return null;
		}
		catch (ParseException e){
			e.printStackTrace();
			return null;
		}
		
		return statusList;
	}
		
	public ArrayList<Record> getFriendRecordList (String fbId) {
		ArrayList<Record> recordList = new ArrayList<Record> (10);
		
		String message = getMessageFromServer("GET", "/api/v1/records/friend_stream.json?fb_id=" + fbId, null);
		
		if(message == null) {
			return null;
		}
		try {
			JSONArray recordArray = new JSONArray(message);
			for(int i = 0; i < recordArray.length(); i++) {
				JSONObject recordJson  = recordArray.getJSONObject(i).getJSONObject("record");
				
				JSONObject movieJson = recordJson.getJSONObject("movie");
				
				Movie movie = new Movie();
				movie.setChineseName(movieJson.getString("name"));
				movie.setPosterUrl(movieJson.getString("poster_url"));
				movie.setId(recordJson.getInt("movie_id"));
				
				User user = new User();
				user.setName(recordJson.getString("user_name"));
				user.setAccount(recordJson.getString("user_fb_id"));
				
				Record record = new Record();
				record.setId(recordJson.getInt("id"));
				record.setMovie(movie);
				record.setComment(recordJson.getString("comment"));
			    record.setScore(recordJson.getInt("score"));
				record.setUser(user);
				
				if(!(recordJson.isNull("created_at"))){
					DateFormat releaseFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
					Date date = releaseFormatter.parse(recordJson.getString("created_at"));
					record.setCheckinTime(date);
				}
				
				recordList.add(record);
				
			}
		} 
		catch (JSONException e){
			e.printStackTrace();
			return null;
		}
		catch (ParseException e){
			e.printStackTrace();
			return null;
		}
		
		return recordList;
	}
	
	//取得朋友列表
	public ArrayList<User> getFriendList (String fbId) {
		
		ArrayList<User> friendList = new ArrayList<User> (10);
		
		String message = getMessageFromServer("GET", "/api/v1/users/friends_list.json?fb_id=" + fbId, null);
		
		if(message == null) {
			return null;
		}
		
		try{
			JSONArray userArray = new JSONArray(message);
			for (int i = 0; i < userArray.length(); i++) {
				JSONObject userJson = userArray.getJSONObject(i).getJSONObject("user");
				
				String account = "";
				String name = "";
				int recordCount = 0;
				
				account = userJson.getString("fb_id");
				name = userJson.getString("name");
				recordCount = userJson.getInt("records_count");
				
				User user = new User();
				
				user.setAccount(account);
				user.setName(name);
				user.setRecordCount(recordCount);
				
				friendList.add(user);
			}
		} 
		catch (JSONException e){
			e.printStackTrace();
			return null;
		}
		
		
		return friendList;
	}
	
	//取得使用者個人資料
	public User getUserData(String fbId) {
		User user = new User();
		
		String message = getMessageFromServer("GET", "/api/v1/users/user_info.json?fb_id=" + fbId, null);
		
		if(message == null) {
			return null;
		}
		
		try{
			JSONObject responseJson = new JSONObject(message);
			JSONObject userJson = responseJson.getJSONObject("user");
			
			String account = userJson.getString("fb_id");
			String name = userJson.getString("name");
			int recordCount = userJson.getInt("records_count");
			int friendCount = userJson.getInt("friend_count");
			
			user.setAccount(account);
			user.setName(name);
			user.setRecordCount(recordCount);
			user.setFriendCount(friendCount);
			
		} 
		catch (JSONException e){
			e.printStackTrace();
		}
		
		
		return user;
		
	}
	
	public ArrayList<Record> getRecordList (String fbId) {
		ArrayList<Record> recordList = new ArrayList<Record> (10);
		
		String message = getMessageFromServer("GET", "/api/v1/records.json?fb_id=" + fbId, null);
		
		if(message == null) {
			return null;
		}
		try {
			JSONArray recordArray = new JSONArray(message);
			for(int i = 0; i < recordArray.length(); i++) {
				JSONObject recordJson  = recordArray.getJSONObject(i).getJSONObject("record");
				
				JSONObject movieJson = recordJson.getJSONObject("movie");
				
				Movie movie = new Movie();
				movie.setChineseName(movieJson.getString("name"));
				movie.setPosterUrl(movieJson.getString("poster_url"));
				
				int loveCount = 0;
				if(!(recordJson.isNull("love_count")))
					loveCount = recordJson.getInt("love_count");
				
				boolean isLovedByUser = false;
				if(!(recordJson.isNull("is_loved_by_user")))
					isLovedByUser = recordJson.getBoolean("is_loved_by_user");
				
				Record record = new Record();
				record.setId(recordJson.getInt("id"));
				record.setComment(recordJson.getString("comment"));
			    record.setScore(recordJson.getInt("score"));
				record.setMovie(movie);
				record.setLoveCount(loveCount);
				record.setIsLovedByUser(isLovedByUser);
				
				recordList.add(record);
				
			}
		} 
		catch (JSONException e){
			e.printStackTrace();
			return null;
		} 
		
		return recordList;
	}
	
	public ArrayList<Record> getRecordListWithPage (String fbId, int page) {
		ArrayList<Record> recordList = new ArrayList<Record> (10);
		
		String message = getMessageFromServer("GET", "/api/v1/records/records_with_page.json?fb_id=" + fbId +
				"&page=" + page, null);
		
		if(message == null) {
			return null;
		}
		try {
			JSONArray recordArray = new JSONArray(message);
			for(int i = 0; i < recordArray.length(); i++) {
				JSONObject recordJson  = recordArray.getJSONObject(i).getJSONObject("record");
				
				JSONObject movieJson = recordJson.getJSONObject("movie");
				
				Movie movie = new Movie();
				movie.setChineseName(movieJson.getString("name"));
				movie.setPosterUrl(movieJson.getString("poster_url"));
				
				int loveCount = 0;
				if(!(recordJson.isNull("love_count")))
					loveCount = recordJson.getInt("love_count");
				
				boolean isLovedByUser = false;
				if(!(recordJson.isNull("is_loved_by_user")))
					isLovedByUser = recordJson.getBoolean("is_loved_by_user");
				
				Record record = new Record();
				record.setId(recordJson.getInt("id"));
				record.setComment(recordJson.getString("comment"));
			    record.setScore(recordJson.getInt("score"));
				record.setMovie(movie);
				record.setLoveCount(loveCount);
				record.setIsLovedByUser(isLovedByUser);
				
				recordList.add(record);
				
			}
		} 
		catch (JSONException e){
			e.printStackTrace();
			return null;
		} 
		
		return recordList;
	}
	
	public Record getRecord(String fbId, int recordId) {
		Record record = null;
		
		String message = getMessageFromServer("GET", "/api/v1/records/" + recordId + ".json?fb_id="+fbId, null);
		
		if(message == null) {
			return null;
		}
		DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		DateFormat releaseFormatter = new SimpleDateFormat("yyyy/MM/dd");
		
		try {
			JSONObject recordJson = new JSONObject(message).getJSONObject("record");
			
			JSONObject movieJson = recordJson.getJSONObject("movie");
			
			//Movie movie = movieJsonToClass(movieJson);
			Movie movie = new Movie();
			movie.setId(recordJson.getInt("movie_id"));
			movie.setChineseName(movieJson.getString("name"));
			movie.setEnglishName(movieJson.getString("name_en"));
			if(!movieJson.isNull("release"))
				movie.setReleaseDate(releaseFormatter.parse(movieJson.getString("release")));
			movie.setPosterUrl(movieJson.getString("poster_url"));
			movie.setLevelUrl(movieJson.getString("level_url"));
			if(!movieJson.isNull("running_time"))
				movie.setRunningTime(movieJson.getInt("running_time"));
			else
				movie.setRunningTime(-1);
			/*
			 * movie = new Movie(movieJson.getInt("id"), movieJson.getString("name"), movieJson.getString("name_en"), movieJson.getString("intro"), 
						 formatter.parse(movieJson.getString("release")), movieJson.getString("poster_url"), movieJson.getInt("running_time"),
						 movieJson.getString("level_url"), actors, directors, recordList);
			 */
			
			
			User user = new User();
			
			if(!(recordJson.isNull("user_name")))
				user.setName(recordJson.getString("user_name"));
			
			if(!(recordJson.isNull("user_fb_id")))
				user.setAccount(recordJson.getString("user_fb_id"));
			
			int score = -1;
			if(!(recordJson.isNull("score")))
				score = recordJson.getInt("score");
			
			String comment = "";
			if(!(recordJson.isNull("comment")))
				comment = recordJson.getString("comment");
			
			ArrayList<Comment> commentList = new ArrayList<Comment> ();
			
			if(!(recordJson.isNull("comments"))){
				JSONArray commentArray = recordJson.getJSONArray("comments");
				for(int i = 0; i < commentArray.length() ; i++) {
					JSONObject commentJson = commentArray.getJSONObject(i).getJSONObject("comment");
					Comment record_comment = new Comment(commentJson.getInt("id"), commentJson.getString("user_fb_id"),formatter.parse(commentJson.getString("created_at")),commentJson.getString("text"),recordJson.getInt("id"));
					commentList.add(record_comment);
				}	
			}
			
			int loveCount = 0;
			if(!(recordJson.isNull("love_count")))
				loveCount = recordJson.getInt("love_count");
			
			boolean isLovedByUser = false;
			if(!(recordJson.isNull("is_loved_by_user")))
				isLovedByUser = recordJson.getBoolean("is_loved_by_user");
			
			//Record(int id, Date checkinTime, int score, String comment, User user, Movie movie)
			record = new Record(recordJson.getInt("id"), formatter.parse(recordJson.getString("created_at")), score, comment, user, movie, commentList, loveCount, isLovedByUser);
		} 
		catch (JSONException e){
			e.printStackTrace();
			return null;
		} 
		catch (ParseException e){
			e.printStackTrace();
			return null;
		}	
		return record;
	}
	
	public boolean updateRecord(Record record) {
		boolean result = false;

		try{
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPut httpPut = new HttpPut("http://106.187.101.252/api/v1/records/"+record.getId()+".json");
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		    nameValuePairs.add(new BasicNameValuePair("score", record.getScore()+"" ));
		    nameValuePairs.add(new BasicNameValuePair("comment", record.getComment()));
		    
		    httpPut.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
			
			//httpPost.setEntity(new ByteArrayEntity(requestJson.toString().getBytes("UTF8")));
			HttpResponse response = httpClient.execute(httpPut);
			
			StatusLine statusLine =  response.getStatusLine();
			if (statusLine.getStatusCode() == 200){
				result = true;
			}
		} 
	    catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return result;
		} 
		catch (ClientProtocolException e) {
			e.printStackTrace();
			return result;
		} 
		catch (IOException e){
			e.printStackTrace();
			return result;
		}	
		return result;
	}
	
	public boolean deleteRecord(Record record) {
		boolean result = false;

		try{
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpDelete httpDelete = new HttpDelete("http://106.187.101.252/api/v1/records/"+record.getId()+".json");		
			HttpResponse response = httpClient.execute(httpDelete);
			
			StatusLine statusLine =  response.getStatusLine();
			if (statusLine.getStatusCode() == 200){
				result = true;
			}
		} 
	    catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return result;
		} 
		catch (ClientProtocolException e) {
			e.printStackTrace();
			return result;
		} 
		catch (IOException e){
			e.printStackTrace();
			return result;
		}	
		return result;
	}
	
	public int createComment(Record record, String fb_id, String comment_text) {
		int result = 0;
		
		try{
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost("http://106.187.101.252/api/v1/comments.json");
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		    nameValuePairs.add(new BasicNameValuePair("fb_id", fb_id ));
		    nameValuePairs.add(new BasicNameValuePair("record_id", record.getId()+""));
		    nameValuePairs.add(new BasicNameValuePair("text", comment_text));
	    
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
			
			//httpPost.setEntity(new ByteArrayEntity(requestJson.toString().getBytes("UTF8")));
			HttpResponse response = httpClient.execute(httpPost);
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			String json = reader.readLine();
			JSONObject recordJson = new JSONObject(json);
			result = recordJson.getInt("comment_id");


		} 
	    catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return result;
		} 
		catch (ClientProtocolException e) {
			e.printStackTrace();
			return result;
		} 
		catch (IOException e){
			e.printStackTrace();
			return result;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return result;
	}
	
	public boolean deleteComment(Comment comment) {
		boolean result = false;

		try{
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpDelete httpDelete = new HttpDelete("http://106.187.101.252/api/v1/comments/"+comment.getId()+".json");		
			HttpResponse response = httpClient.execute(httpDelete);
			
			StatusLine statusLine =  response.getStatusLine();
			if (statusLine.getStatusCode() == 200){
				result = true;
			}
		} 
	    catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return result;
		} 
		catch (ClientProtocolException e) {
			e.printStackTrace();
			return result;
		} 
		catch (IOException e){
			e.printStackTrace();
			return result;
		}	
		return result;
	}

	public boolean createUser(User user, String fbToken, String regId) {
		boolean result = false;
		
		try{
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost("http://106.187.101.252/api/v1/users.json");
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		    nameValuePairs.add(new BasicNameValuePair("fb_token", fbToken));
		    nameValuePairs.add(new BasicNameValuePair("name", user.getName()));
		    nameValuePairs.add(new BasicNameValuePair("sex", user.getSex()));
		    nameValuePairs.add(new BasicNameValuePair("birthday", user.getBirthday()));
		    
		    nameValuePairs.add(new BasicNameValuePair("fb_id", user.getAccount()));
		    nameValuePairs.add(new BasicNameValuePair("registration_id", regId));
	    
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
			
			//httpPost.setEntity(new ByteArrayEntity(requestJson.toString().getBytes("UTF8")));
			HttpResponse response = httpClient.execute(httpPost);
			
			StatusLine statusLine =  response.getStatusLine();
			if (statusLine.getStatusCode() == 200){
				result = true;
			}
		} 
	    catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return result;
		} 
		catch (ClientProtocolException e) {
			e.printStackTrace();
			return result;
		} 
		catch (IOException e){
			e.printStackTrace();
			return result;
		}	
		return result;
	}
	
	public boolean logoutUser(String userId) {
		boolean result = false;
		
		try{
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpDelete httpDelete = new HttpDelete("http://106.187.101.252/api/v1/users/" + userId + ".json");
			
			/*List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		    nameValuePairs.add(new BasicNameValuePair("score", record.getScore()+"" ));
		    nameValuePairs.add(new BasicNameValuePair("comment", record.getComment()));
		    
		    httpPut.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));*/
			HttpResponse response = httpClient.execute(httpDelete);
			
			StatusLine statusLine =  response.getStatusLine();
			if (statusLine.getStatusCode() == 200){
				result = true;
			}
		} 
	    catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return result;
		} 
		catch (ClientProtocolException e) {
			e.printStackTrace();
			return result;
		} 
		catch (IOException e){
			e.printStackTrace();
			return result;
		}
		
		return result;
	}
	
	// Spread
	public ArrayList<Spread> getCurrentSpreadList() {
		ArrayList<Spread> spreadList = new ArrayList<Spread>(10);
		String message = getMessageFromServer("GET", "/api/v1/campaigns.json", null);
		
		if(message == null) {
			return null;
		}
		else {
			JSONArray spreadArray;

			try
			{
				spreadArray = new JSONArray(message.toString());				
				for (int i = 0; i < spreadArray.length() ; i++) {
					Spread spread = new Spread();
					spread.setId(spreadArray.getJSONObject(i).getInt("id"));
					spread.setImageUrl(spreadArray.getJSONObject(i).getString("picture_out"));
					spreadList.add(spread);
				}
			} 
			catch (JSONException e){
				e.printStackTrace();
				return null;
			}
		}
		return spreadList;
	}
	
	public ArrayList<Spread> getResultSpreadList() {
		ArrayList<Spread> spreadList = new ArrayList<Spread>(10);
		String message = getMessageFromServer("GET", "/api/v1/campaigns/announce_list.json", null);
		
		if(message == null) {
			return null;
		}
		else {
			JSONArray spreadArray;

			try
			{
				spreadArray = new JSONArray(message.toString());				
				for (int i = 0; i < spreadArray.length() ; i++) {
					Spread spread = new Spread();
					spread.setId(spreadArray.getJSONObject(i).getInt("id"));
					spread.setImageUrl(spreadArray.getJSONObject(i).getString("picture_out"));
					spreadList.add(spread);
				}
			} 
			catch (JSONException e){
				e.printStackTrace();
				return null;
			}
		}
		return spreadList;
	}
	
	public Spread getCurrentSpread(int spreadId) {
		Spread spread = null;
		
		String message = getMessageFromServer("GET", "/api/v1/campaigns/" + spreadId + ".json", null);
		
		if(message == null) {
			return null;
		}
		
		try {
			spread = new Spread();
			
			JSONObject spreadJson = new JSONObject(message);
			
			if(spreadJson.has("picture_out"))
				spread.setImageUrl(spreadJson.getString("picture_out"));
			
			if(spreadJson.has("picture_in"))
				spread.setSpreadPosterUrl(spreadJson.getString("picture_in"));
				
			if(spreadJson.has("title"))
				spread.setSpreadTitle(spreadJson.getString("title"));
			
			if(spreadJson.has("description"))
				spread.setSpreadTitleContent(spreadJson.getString("description"));
			
			if(spreadJson.has("time_active"))
				spread.setSpreadTimeContent(spreadJson.getString("time_active"));
			
			if(spreadJson.has("measure"))
				spread.setSpreadMethodStep(spreadJson.getString("measure"));
			
			if(spreadJson.has("teach_pic"))
				spread.setSpreadMethodStepUrl(spreadJson.getString("teach_pic"));
			
			if(spreadJson.has("award_condition"))
				spread.setSpreadMethodResult(spreadJson.getString("award_condition"));
			
			if(spreadJson.has("award"))
				spread.setSpreadGiftContent(spreadJson.getString("award"));
			
			if(spreadJson.has("award_pic"))
				spread.setSpreadGiftUrl(spreadJson.getString("award_pic"));
			
			if(spreadJson.has("precaution"))
				spread.setSpreadNotifyContent(spreadJson.getString("precaution"));
			
			if(spreadJson.has("movie_id"))
				spread.setMovieId(spreadJson.getInt("movie_id"));						
		} 
		catch (JSONException e){
			e.printStackTrace();
			return null;
		}	
		return spread;
	}
	
	public Spread getResultSpread(int spreadId) {
		Spread spread = null;
		
		String message = getMessageFromServer("GET", "/api/v1/campaigns/" + spreadId + "/announce.json", null);
		
		if(message == null) {
			return null;
		}
		
		try {
			spread = new Spread();
			
			JSONObject spreadJson = new JSONObject(message);
			
			if(spreadJson.has("picture_in"))
				spread.setSpreadPosterUrl(spreadJson.getString("picture_in"));
				
			if(spreadJson.has("title"))
				spread.setSpreadTitle(spreadJson.getString("title"));
			
			if(spreadJson.has("award_list"))
				spread.setSpreadTitleContent(spreadJson.getString("award_list"));
		} 
		catch (JSONException e){
			e.printStackTrace();
			return null;
		}	
		return spread;
	}
	
	public Movie movieJsonToClass (JSONObject movieJson) {
		Movie movie = null;
		DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		
		if(movieJson == null) {
			return null;
		}
		else {
			try {
				//actor list
				ArrayList<String> actors = new ArrayList<String>(5);
				JSONArray actorsArray = movieJson.getJSONArray("actors");
				for (int j = 0; j < actorsArray.length(); j++) {
					actors.add(actorsArray.getString(j));
				}
				
				//director list
				ArrayList<String> directors = new ArrayList<String>(3);
				JSONArray directorsArray = movieJson.getJSONArray("directors");
				for (int k = 0; k < directorsArray.length(); k++) {
					directors.add(directorsArray.getString(k));
				}
				
				ArrayList<Record> recordList = new ArrayList<Record> (10);
				//確認是否有record
				if(!(movieJson.isNull("record"))) {
					JSONArray recordArray = movieJson.getJSONArray("record");
					for (int l = 0; l < recordArray.length(); l++) {
						Record record = new Record();
						JSONObject recordJson = recordArray.getJSONObject(l);
						record.setId(recordJson.getInt("id"));
						
						int score = -1;
						if(!(recordJson.isNull("score"))) {
							score = recordJson.getInt("score");
							record.setScore(score);
						}
						
						String comment = "";
						if(!(recordJson.isNull("comment"))) {
							comment = recordJson.getString("comment");
							record.setComment(comment);
						}
						
						if(!(recordJson.isNull("created_at"))){
							DateFormat releaseFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
							Date date = releaseFormatter.parse(recordJson.getString("created_at"));
							record.setCheckinTime(date);
						}
							
						
						User user = new User();
						user.setAccount(recordArray.getJSONObject(l).getString("user_fb_id"));
						record.setUser(user);
						recordList.add(record);
					}
				}
				Date releaseDate = null;
				int runningTime = -1;
				if(!movieJson.isNull("release"))
					releaseDate = formatter.parse(movieJson.getString("release"));
				if(!movieJson.isNull("running_time"))
					runningTime = movieJson.getInt("running_time");
				
				 movie = new Movie(movieJson.getInt("id"), movieJson.getString("name"), movieJson.getString("name_en"), movieJson.getString("intro"), 
						 releaseDate, movieJson.getString("poster_url"), runningTime, movieJson.getString("level_url"), actors, directors, recordList, 
						 movieJson.getString("youtube_video_id"), 0,0);
				 
			} 
			catch (JSONException e) {
				e.printStackTrace();
				return null;
			} 
			catch (ParseException e) {
				e.printStackTrace();
				return null;
			}	
		}
		return movie;
	}
	
	public String getMessageFromServer(String requestMethod, String apiPath, JSONObject json) {
		URL url;
		try {
			url = new URL(this.urlAddress +  apiPath);
			if(DEBUG)
				Log.d(TAG, "URL: " + url);
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(requestMethod);
			
			connection.setRequestProperty("Content-Type",  "application/json;charset=utf-8");
			if(requestMethod.equalsIgnoreCase("POST"))
				connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.connect();
			
			
			if(requestMethod.equalsIgnoreCase("POST")) {
				OutputStream outputStream;
				
				outputStream = connection.getOutputStream();
				if(DEBUG)
					Log.d("post message", json.toString());
				
				outputStream.write(json.toString().getBytes());
				outputStream.flush();
				outputStream.close();
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder lines = new StringBuilder();;
			String tempStr;
			
			while ((tempStr = reader.readLine()) != null) {
	            lines = lines.append(tempStr);
	        }
			if(DEBUG)
				Log.d("MOVIE_API", lines.toString());
			
			reader.close();
			connection.disconnect();
			
			return lines.toString();
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} 
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean likeRecords(String fbId, String recordId) {
		boolean result = false;
		
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost;
			String url = "http://106.187.101.252/api/v1/records/"
					+ recordId + "/love.json?fb_id=" + fbId;
						
			if(DEBUG)
				Log.d(TAG, "URL : " + url);
			httpPost = new HttpPost(url);
			HttpResponse response = httpClient.execute(httpPost);
			
			StatusLine statusLine =  response.getStatusLine();
			if (statusLine.getStatusCode() == 200){
				result = true;
			}
			
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return result;
		} catch (ClientProtocolException e)
		{
			e.printStackTrace();
			return result;
		} catch (IOException e)
		{
			e.printStackTrace();
			return result;
		}
		
		
		return result;
	}
	
	public boolean unlikeRecords(String fbId, String recordId) {
		boolean result = false;
		
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpDelete httpDelete;
			String url = "http://106.187.101.252/api/v1/records/"
						+ recordId + "/unlove.json?fb_id=" + fbId;
			
			if(DEBUG)
				Log.d(TAG, "URL : " + url);
			httpDelete = new HttpDelete(url);
			HttpResponse response = httpClient.execute(httpDelete);
			
			StatusLine statusLine =  response.getStatusLine();
			if (statusLine.getStatusCode() == 200){
				result = true;
			}
			
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return result;
		} catch (ClientProtocolException e)
		{
			e.printStackTrace();
			return result;
		} catch (IOException e)
		{
			e.printStackTrace();
			return result;
		}
		
		
		return result;
	}
	
	public void getPromotion(String picUrl, String link, String title, String description) {
				
		String message = getMessageFromServer("GET", "api/promotion.json", null);
		
		try{
			JSONObject responseJson = new JSONObject(message);
			
			picUrl = responseJson.getString("picture_link");
			link = responseJson.getString("link");
			title = responseJson.getString("tilte");
			description = responseJson.getString("description");
		} 
		catch (JSONException e){
			e.printStackTrace();
		}
	}
	
	//取得使用者個人資料
	public String[] getPromotion() {
				
		String message = getMessageFromServer("GET", "api/promotion.json", null);
		String[] tmp = new String[5];
		
		if(message == null) {
			return null;
		}
		
		try{
			JSONObject responseJson = new JSONObject(message);
			
			tmp[0] = (responseJson.getString("picture_link"));
			tmp[1] = (responseJson.getString("link"));
			tmp[2] = (responseJson.getString("tilte"));
			tmp[3] = (responseJson.getString("description"));
			tmp[4] = (responseJson.getString("version"));
		} 
		catch (JSONException e){
			e.printStackTrace();
		}
		
		return tmp;
	}
		
	public String getUrlAddress() {
		return urlAddress;
	}
	public void setUrlAddress(String urlAddress) {
		this.urlAddress = urlAddress;
	}
	public HttpURLConnection getConnection() {
		return connection;
	}
	public void setConnection(HttpURLConnection connection) {
		this.connection = connection;
	}
	public String getRequestedMethod() {
		return requestedMethod;
	}
	public void setRequestedMethod(String requestedMethod) {
		this.requestedMethod = requestedMethod;
	}
	public int getConnectionTimeout() {
		return connectionTimeout;
	}
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}
	public int getReadTimeout() {
		return readTimeout;
	}
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}
	public boolean isUsercaches() {
		return usercaches;
	}
	public void setUsercaches(boolean usercaches) {
		this.usercaches = usercaches;
	}
	public boolean isDoInput() {
		return doInput;
	}
	public void setDoInput(boolean doInput) {
		this.doInput = doInput;
	}
	public boolean isDoOutput() {
		return doOutput;
	}
	public void setDoOutput(boolean doOutput) {
		this.doOutput = doOutput;
	}
}
