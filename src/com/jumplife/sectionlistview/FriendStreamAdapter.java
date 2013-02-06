package com.jumplife.sectionlistview;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.facebook.widget.ProfilePictureView;
import com.jumplife.imageload.ImageLoader;
import com.jumplife.jome.entity.Comment;
import com.jumplife.moviediary.R;
import com.jumplife.moviediary.api.MovieAPI;
import com.jumplife.moviediary.entity.Record;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendStreamAdapter extends BaseAdapter{
	
    Context mContext;
    private String fbId;
	private ArrayList<Object> objects;
	private TextView textviewLike;
	private ImageView imageviewLike;
	//private LinearLayout linearLike;
	
	public FriendStreamAdapter(Context mContext, ArrayList<Object> objectList, String fbId){
		this.objects = objectList;
		this.mContext = mContext;
		this.fbId = fbId;
		new ImageLoader(mContext);
	}

	public int getCount() {
		
		return objects.size();
	}

	public Object getItem(int position) {

		return objects.get(position);
	}

	public long getItemId(int position) {
	
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater myInflater = LayoutInflater.from(mContext);
		View converView;
		if(objects.get(position) instanceof Record) {
			converView = myInflater.inflate(R.layout.listview_records, null);
		} else {
			converView = myInflater.inflate(R.layout.listview_friend_stream_comment, null);
		}
		
		
		if(objects.get(position) instanceof Record) {
			Record record = (Record) objects.get(position);
			ProfilePictureView user_avatar = (ProfilePictureView)converView.findViewById(R.id.user_avatar);
			TextView name = (TextView)converView.findViewById(R.id.user_name);
			TextView score = (TextView)converView.findViewById(R.id.user_score);
			TextView user_comment = (TextView)converView.findViewById(R.id.user_comment);
			TextView record_date = (TextView)converView.findViewById(R.id.record_date);
			textviewLike = (TextView)converView.findViewById(R.id.textview_like);
			imageviewLike = (ImageView)converView.findViewById(R.id.imageview_like);
			//linearLike = (LinearLayout)converView.findViewById(R.id.linearlayout_like);
			
			//imageLoader.DisplayImage(record.getUser().getIconUrl(), user_avatar);
			if (record.getUser().getAccount() != null)
				user_avatar.setProfileId(record.getUser().getAccount());
			else 
				user_avatar.setProfileId(null);
			
			String text = record.getUser().getName() + " 覺得<b>《<font color='#3366FF'>" + record.getMovie().getChineseName()
					+ "</font>》</b>" + record.getScoreString();
			name.setText(Html.fromHtml(text));
			score.setText("評價 : " + record.getScoreString());
			user_comment.setText(record.getComment());
			textviewLike.setText(record.getLoveCount()+"");
			if(record.getIsLovedByUser()) {
				//imageviewLike.setImageResource(R.drawable.facebook_like);
				//linearLike.setBackgroundResource(R.drawable.button_like_press);
				imageviewLike.setImageResource(R.drawable.md_like);
				textviewLike.setTextColor(mContext.getResources().getColor(R.color.white1));
			} else {
				//imageviewLike.setImageResource(R.drawable.facebook_like_grey);
				//linearLike.setBackgroundResource(R.drawable.button_like);
				imageviewLike.setImageResource(R.drawable.md_unlike);
				textviewLike.setTextColor(mContext.getResources().getColor(R.color.like));
			}
			
			//linearLike.setOnClickListener(new ItemButton_Click(position));
			imageviewLike.setOnClickListener(new ItemButton_Click(position));
			DateFormat createFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			record_date.setText(createFormatter.format(record.getCheckinTime()));
			/*Record record = (Record) objects.get(position);
			TextView user_score = (TextView)converView.findViewById(R.id.user_score);
			user_score.setVisibility(View.GONE);
			String color;
			if(record.getScoreString().equals("好看"))
				color = "'#c9aa0c'";
			else if(record.getScoreString().equals("普通"))
				color = "'#7fa4ce'";
			else
				color = "'#70970f'";
			String text = record.getUser().getName() + " 覺得<b>《" + record.getMovie().getChineseName() + "》</b>"
					+ /*"<font color=" + color'#3366FF' + ">" +*//* record.getScoreString() +"</font>";
			imageLoader.DisplayImage(record.getUser().getIconUrl(), user_avatar);
			user_name.setText(Html.fromHtml(text));
			//user_score.setText("評價：" + record.getScoreString());
			user_comment.setText(record.getComment());
			DateFormat createFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			record_date.setText(createFormatter.format(record.getCheckinTime()));*/
		} else {
			ProfilePictureView user_avatar = (ProfilePictureView)converView.findViewById(R.id.user_avatar);
			TextView user_name = (TextView)converView.findViewById(R.id.user_name);
			TextView user_comment = (TextView)converView.findViewById(R.id.user_comment);
			TextView record_date = (TextView)converView.findViewById(R.id.record_date);
			
			Comment comment = (Comment) objects.get(position);
			//String text = comment.getUserName() + " 回應了 \"" + "<font color='#3366FF'>" + comment.getMovieName() + "</font>\"";
			String text = comment.getUserName() + " 回應了<b>《<font color='#3366FF'>" + comment.getMovieName() + "</font>》</b>";
			//imageLoader.DisplayImage("http://graph.facebook.com/" + comment.getAccount() + "/picture?type=square", user_avatar);
			if (comment.getAccount() != null)
				user_avatar.setProfileId(comment.getAccount());
			else 
				user_avatar.setProfileId(null);
			user_name.setText(Html.fromHtml(text));
			user_comment.setText(comment.getContext());
			DateFormat createFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			record_date.setText(createFormatter.format(comment.getTime()));
		}
		
		return converView;

	}

	class ItemButton_Click implements OnClickListener {
		private int position;

		ItemButton_Click(int pos) {
			position = pos;
		}

		public void onClick(View v) {
			Record record = (Record) objects.get(position);
			if(record.getIsLovedByUser()) {
				record.setLoveCount( record.getLoveCount() - 1 );
	    		textviewLike.setText(record.getLoveCount()+"");
	    		//textviewLike.setTextColor(Color.GRAY);
	    		//imageviewLike.setImageResource(R.drawable.facebook_like_grey);
	    		//linearLike.setBackgroundResource(R.drawable.button_like);
	    		imageviewLike.setImageResource(R.drawable.md_unlike);
				textviewLike.setTextColor(mContext.getResources().getColor(R.color.like));
	    		Log.d("", "IsLovedByUser");
	    	} else {
	    		record.setLoveCount( record.getLoveCount() + 1 );
	    		textviewLike.setText(record.getLoveCount()+"");
	    		//textviewLike.setTextColor(Color.DKGRAY);
	    		//imageviewLike.setImageResource(R.drawable.facebook_like);
	    		//linearLike.setBackgroundResource(R.drawable.button_like_press);
	    		imageviewLike.setImageResource(R.drawable.md_like);
				textviewLike.setTextColor(mContext.getResources().getColor(R.color.white1));
	    		Log.d("", "IsNotLovedByUser");
	    	}
			LikeTask likeTask = new LikeTask(record.getIsLovedByUser(), record.getId()+"");
			likeTask.execute();
			record.setIsLovedByUser(!record.getIsLovedByUser());
	    	notifyDataSetChanged();
		}
	}
	
	class LikeTask extends AsyncTask<Integer, Integer, String>{  
        
		private boolean isLove;
		private String recordId;
		private ProgressDialog progressdialogInit;

		private LikeTask(Boolean isLove, String recordId) {
			this.isLove = isLove;
			this.recordId = recordId;
		}
		
        @Override  
        protected void onPreExecute() {
        	progressdialogInit= ProgressDialog.show(mContext, "Load", "Loading…");
            super.onPreExecute();  
        }  
          
        @Override  
        protected String doInBackground(Integer... params) {
        	MovieAPI movieAPI = new MovieAPI();
        	if(isLove)
        		movieAPI.unlikeRecords(fbId, recordId);
        	else
        		movieAPI.likeRecords(fbId, recordId);
        		
            return "progress end";  
        }  
  
        @Override  
        protected void onProgressUpdate(Integer... progress) {    
            super.onProgressUpdate(progress);  
        }  
  
        @Override  
        protected void onPostExecute(String result) {
        	progressdialogInit.dismiss();
        	super.onPostExecute(result);  
        }  
          
    }
}
