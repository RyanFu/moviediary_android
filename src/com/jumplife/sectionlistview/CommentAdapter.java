package com.jumplife.sectionlistview;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.FacebookError;
import com.facebook.android.Util;
//import com.jumplife.facebook.FacebookIO;
import com.jumplife.imageload.ImageLoader;
import com.jumplife.jome.entity.Comment;
import com.jumplife.loginactivity.FacebookIO;
import com.jumplife.loginactivity.Utility;
import com.jumplife.moviediary.R;
import android.app.Activity;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CommentAdapter extends BaseAdapter{
	
	Activity mActivity;
	private ArrayList<Comment> comments;
	private ImageLoader imageLoader;
	private String mUsrId;
	private Map<String, String> mapName;
	
	public CommentAdapter(Activity mActivity,  ArrayList<Comment> commentList, String usrId){
		this.comments = commentList;
		this.mActivity = mActivity;
		this.mUsrId = usrId;
		imageLoader=new ImageLoader(mActivity);
		
		FacebookIO fbIO = new FacebookIO(mActivity);
		String[] nameArray = new String[commentList.size()]; 
		Comment comment;
		for(int i=0; i<commentList.size(); i++){			
	 		comment = commentList.get(i);
	 		nameArray[i] = comment.getAccount();
		}
		this.mapName = fbIO.getUserName(nameArray);
		if (mapName == null)
			mapName = new HashMap<String, String>();
	}

	public int getCount() {
		
		return comments.size();
	}

	public Object getItem(int position) {

		return comments.get(position);
	}

	public long getItemId(int position) {
	
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		
		LayoutInflater myInflater = LayoutInflater.from(mActivity);
		View converView = myInflater.inflate(R.layout.listview_comments, null);
		ImageView user_avatar = (ImageView)converView.findViewById(R.id.user_img);
		TextView user_name = (TextView)converView.findViewById(R.id.post_name);
		TextView record_date = (TextView)converView.findViewById(R.id.post_time);
		TextView user_comment = (TextView)converView.findViewById(R.id.content);
		ImageView delete_comment = (ImageView)converView.findViewById(R.id.delete_img);
		imageLoader.DisplayImage("http://graph.facebook.com/" + comments.get(position).getAccount() + 
				"/picture?type=square", user_avatar);
		
		String usrName = mapName.get(comments.get(position).getAccount());
		if (usrName != null)
			user_name.setText(usrName);
		else {
			LoadUserNameTask task = new LoadUserNameTask(user_name, comments.get(position).getAccount());
			task.execute();
		}
			
		user_comment.setText(comments.get(position).getContext());
		DateFormat createFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		record_date.setText(createFormatter.format(comments.get(position).getTime()));
		
		if(comments.get(position).getAccount().equals(mUsrId))
			delete_comment.setVisibility(View.VISIBLE);
    	else 
    		delete_comment.setVisibility(View.INVISIBLE);
    	
		return converView;

	}

	class LoadUserNameTask extends AsyncTask<Integer, Integer, String>{  
        
		private String name;
		private String fb_id;
		private TextView user_name;
		
        public LoadUserNameTask(TextView userName, String fbid) {
        	this.fb_id = fbid;
        	this.user_name = userName;
        }
        
		@Override  
        protected void onPreExecute() {
        	super.onPreExecute();  
        }  
          
        @Override  
        protected String doInBackground(Integer... params) {
			String response = null;
			try {
				response = Utility.mFacebook.request(fb_id);
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			JSONObject json = null;
			try {
				json = Util.parseJson(response);
				name = json.getString("name");
			} catch (FacebookError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return "progress end";  
        }  
  
        @Override  
        protected void onProgressUpdate(Integer... progress) {    
            super.onProgressUpdate(progress);  
        }  
  
        @Override  
        protected void onPostExecute(String result) {
        	if(name != null) {
        		CommentAdapter.this.mapName.put(this.fb_id, this.name);
        		user_name.setText(name);
        	}
        	super.onPostExecute(result);  
        }  
          
    }
}
