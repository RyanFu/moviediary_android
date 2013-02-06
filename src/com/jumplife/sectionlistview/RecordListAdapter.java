package com.jumplife.sectionlistview;

import java.util.ArrayList;

import com.facebook.widget.ProfilePictureView;
import com.jumplife.imageload.ImageLoader;
/*import com.jumplife.loginactivity.LoginActivity;
import com.jumplife.loginactivity.Utility;*/
import com.jumplife.loginactivity.LoginActivity;
import com.jumplife.loginactivity.Utility;
import com.jumplife.moviediary.R;
import com.jumplife.moviediary.api.MovieAPI;
import com.jumplife.moviediary.entity.Record;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RecordListAdapter extends BaseAdapter{
	
    private Activity mActivity;
    private String fbId;
	private ArrayList<Record> records;
	private TextView textviewLike;
	private ImageView imageviewLike;
	//private LinearLayout linearLike;
	
	public RecordListAdapter(Activity mActivity, ArrayList<Record> recordList, String fbId){
		this.records = recordList;
		this.mActivity = mActivity;
		this.fbId = fbId;
		new ImageLoader(mActivity);
	}

	public int getCount() {
		
		return records.size();
	}

	public Object getItem(int position) {

		return records.get(position);
	}

	public long getItemId(int position) {
	
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		
		LayoutInflater myInflater = LayoutInflater.from(mActivity);
		View converView = myInflater.inflate(R.layout.listview_movieshow, null);
		ProfilePictureView user_avatar = (ProfilePictureView)converView.findViewById(R.id.user_avatar);
		TextView name = (TextView)converView.findViewById(R.id.user_name);
		TextView score = (TextView)converView.findViewById(R.id.user_score);
		TextView user_comment = (TextView)converView.findViewById(R.id.user_comment);
		textviewLike = (TextView)converView.findViewById(R.id.textview_like);
		imageviewLike = (ImageView)converView.findViewById(R.id.imageview_like);
		//linearLike = (LinearLayout)converView.findViewById(R.id.linearlayout_like);
		
		//imageLoader.DisplayImage(records.get(position).getUser().getIconUrl(), user_avatar);
		if (records.get(position).getUser().getAccount() != null)
			user_avatar.setProfileId(records.get(position).getUser().getAccount());
		else 
			user_avatar.setProfileId(null);
		
		name.setText(records.get(position).getUser().getName());
		score.setText("評價 :" + records.get(position).getScoreString());
		user_comment.setText(records.get(position).getComment());
		if(records.get(position).getComment().length() > 50) {
			user_comment.setText(Html.fromHtml(records.get(position).getComment().subSequence(0, 40) + "<font color=\"#818181\">"
					+ "   ...更多" + "</font>"));
		}
		textviewLike.setText(records.get(position).getLoveCount()+"");
		if(records.get(position).getIsLovedByUser()) { 
			//imageviewLike.setImageResource(R.drawable.facebook_like);
			//linearLike.setBackgroundResource(R.drawable.button_like_press);
			//textviewLike.setTextColor(Color.DKGRAY);
			imageviewLike.setImageResource(R.drawable.md_like);
			textviewLike.setTextColor(mActivity.getResources().getColor(R.color.white1));
		} else {
			//imageviewLike.setImageResource(R.drawable.facebook_like_grey);
			//linearLike.setBackgroundResource(R.drawable.button_like);
			//textviewLike.setTextColor(Color.GRAY);
			imageviewLike.setImageResource(R.drawable.md_unlike);
			textviewLike.setTextColor(mActivity.getResources().getColor(R.color.like));
		}
		
		//linearLike.setOnClickListener(new ItemButton_Click(position));
		imageviewLike.setOnClickListener(new ItemButton_Click(position));
		
		return converView;

	}

	class ItemButton_Click implements OnClickListener {
		private int position;

		ItemButton_Click(int pos) {
			position = pos;
		}

		public void onClick(View v) {
			if (Utility.IsSessionValid(mActivity)) {
				if(records.get(position).getIsLovedByUser()) {
		    		records.get(position).setLoveCount( records.get(position).getLoveCount() - 1 );
		    		textviewLike.setText(records.get(position).getLoveCount()+"");
		    		//textviewLike.setTextColor(Color.GRAY);
		    		//imageviewLike.setImageResource(R.drawable.facebook_like_grey);
		    		//linearLike.setBackgroundResource(R.drawable.button_like);
		    		imageviewLike.setImageResource(R.drawable.md_unlike);
					textviewLike.setTextColor(mActivity.getResources().getColor(R.color.like));
		    		Log.d("", "IsLovedByUser");
		    	} else {
		    		records.get(position).setLoveCount( records.get(position).getLoveCount() + 1 );
		    		textviewLike.setText(records.get(position).getLoveCount()+"");
		    		//textviewLike.setTextColor(Color.DKGRAY);
		    		//imageviewLike.setImageResource(R.drawable.facebook_like);
		    		//linearLike.setBackgroundResource(R.drawable.button_like_press);
		    		imageviewLike.setImageResource(R.drawable.md_like);
					textviewLike.setTextColor(mActivity.getResources().getColor(R.color.white1));
		    		Log.d("", "IsNotLovedByUser");
		    	}
				LikeTask likeTask = new LikeTask(records.get(position).getIsLovedByUser(), records.get(position).getId()+"");
				likeTask.execute();
				records.get(position).setIsLovedByUser(!records.get(position).getIsLovedByUser());
		    	notifyDataSetChanged();
            } else {
            	Intent newAct = new Intent(); 
            	newAct.setClass( mActivity, LoginActivity.class );
            	mActivity.startActivityForResult(newAct, LoginActivity.LOGIN_ACTIVITY_REQUEST_CODE_LIKE);
            }
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
        	progressdialogInit= ProgressDialog.show(mActivity, "Load", "Loading…");
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
