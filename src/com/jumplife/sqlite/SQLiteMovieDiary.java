package com.jumplife.sqlite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.jumplife.moviediary.api.MovieAPI;
import com.jumplife.moviediary.entity.Movie;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

public class SQLiteMovieDiary extends SQLiteOpenHelper {
	private static final String MovieTable = "movie";
	private static final String DB_PATH = "/data/data/com.jumplife.moviediary/databases/";
	private static final String DB_NAME = "searchmovie.db";	//資料庫名稱
	private static final int DATABASE_VERSION = 1;	//資料庫版本
	private static SQLiteDatabase db;
	
	public SQLiteMovieDiary(Activity mActivity) {
		super(mActivity, DB_NAME, null, DATABASE_VERSION);
		checkFileSystem(mActivity);
		
		if(!checkDataBase()) {
            db = this.getWritableDatabase();
            closeDB();
        }
		
		//test
		//LoadDataTask tast = new LoadDataTask();
        //tast.execute();
	}
 
	@Override
	public void onCreate(SQLiteDatabase database) {
		// TODO Auto-generated method stub
		createMovie();
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + MovieTable);
		Log.d("SQLiteMovieDiary", "Delete old Database");
		onCreate(db);
	}
	
	public SQLiteDatabase GetDB() {
		openDataBase();
		return db;
	}
	
	private static void openDataBase() {
		db = SQLiteDatabase.openOrCreateDatabase(DB_PATH + DB_NAME, null);
	}
	
	public static void closeDB() { 
		if(null != db)
			db.close();  
    }
	
	private boolean checkDataBase(){
		File dbtest = new File(DB_PATH + DB_NAME);
		if(dbtest.exists())
			return true;
		else 
			return false;
    }

	private void checkFileSystem(Activity mActivity){
		//if((new File(DB_PATH + DB_NAME)).exists() == false) {
			// 如 SQLite 数据库文件不存在，再检查一下 database 目录是否存在
			File f = new File(DB_PATH);
			// 如 database 目录不存在，新建该目录
			if (!f.exists()) {
				f.mkdir();
			}

			try {
				// 得到 assets 目录下我们实现准备好的 SQLite 数据库作为输入流
				InputStream is = mActivity.getBaseContext().getAssets().open(DB_NAME);
				// 输出流
				OutputStream os = new FileOutputStream(DB_PATH + DB_NAME);

				// 文件写入
				byte[] buffer = new byte[1024];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}

				// 关闭文件流
				os.flush();
				os.close();
				is.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		//}
	}
	
	
	public static void createMovie() {
		openDataBase();
		String DATABASE_CREATE_TABLE =
				"CREATE TABLE IF NOT EXISTS " + MovieTable + " (" +
				" ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE," +
				" CHNAME VARCHAR," +
				" ENNAME VARCHAR," +
				" POSTERURL VARCHAR" +
				" );";
		
		db.execSQL(DATABASE_CREATE_TABLE);
		closeDB();
	}
	
	public long addMovie(Movie movie) {
		openDataBase();
		long rowId = insertMovie(movie);
		closeDB();
		
		return rowId;
	}	
	private static long insertMovie(Movie movie) {
		ContentValues args = new ContentValues();
		args.put("ID", movie.getId());
		args.put("CHNAME", movie.getChineseName());
		args.put("ENNAME", movie.getEnglishName());
		args.put("POSTERURL", movie.getPosterUrl());
		return db.insert(MovieTable, null, args);
	}	
	public int deleteMovie(long rowId) {
		return db.delete(MovieTable,
		"MAIN_ID = " + rowId,
		null
		);
	}
	public static void addMovieList(ArrayList<Movie> moviet_lst) {
		openDataBase();
		Log.d("SQLiteMovieDiary", "Movie List Size : " + moviet_lst.size());
		for(int i=0; i<moviet_lst.size(); i++) {
			Movie movie = moviet_lst.get(i);
			insertMovie(movie);
		}
		closeDB();
	}
	public static void deleteMovie_lst() {
		openDataBase();
		db.execSQL("DROP TABLE IF EXISTS " + MovieTable);
		closeDB();
	}
	
	public ArrayList<Movie> getMovieList() throws SQLException {
		openDataBase();
		ArrayList<Movie> movie_lst = new ArrayList<Movie>();  
        Cursor cursor = db.rawQuery("SELECT * FROM " + MovieTable, null);
		while (cursor.moveToNext()){  
            Movie movie = new Movie();  
            movie.setId(cursor.getInt(0));
            movie.setChineseName(cursor.getString(1));
            movie.setEnglishName(cursor.getString(2));
            movie.setPosterUrl(cursor.getString(3));
            movie_lst.add(movie);  
        }

        cursor.close();
		closeDB();
		return movie_lst;
	}
	
	class LoadDataTask extends AsyncTask<Integer, Integer, String>{  
        
		ArrayList<Movie> movieList = new ArrayList<Movie>();
		
		@Override  
        protected void onPreExecute() {
        	super.onPreExecute();  
        }  
          
        @Override  
        protected String doInBackground(Integer... params) {
        	MovieAPI movieAPI = new MovieAPI();
    		movieList = movieAPI.getMovieList(MovieAPI.FILTER_ALL);
    		
            return "progress end";  
        }  
  
        @Override  
        protected void onProgressUpdate(Integer... progress) {    
            super.onProgressUpdate(progress);  
        }  
  
        @Override  
        protected void onPostExecute(String result) {
        	
        	if(result.equals("progress end")) {
        		SQLiteMovieDiary.deleteMovie_lst();
        		SQLiteMovieDiary.createMovie();
        		SQLiteMovieDiary.addMovieList(movieList);
        	}
        	
        	super.onPostExecute(result);
        }  
          
    }
}