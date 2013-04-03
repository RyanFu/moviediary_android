package com.jumplife.sectionlistview;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.jumplife.moviediary.EzCheckActivity;
import com.jumplife.moviediary.R;
import com.jumplife.moviediary.entity.Theater;

public class ScheduleAdapter extends BaseAdapter{
	ArrayList<Theater> theaters;
    Context mContext;
	
	public ScheduleAdapter(Context mContext, ArrayList<Theater> theaterList){
		this.theaters = theaterList;
		this.mContext = mContext;
	}

	public int getCount() {
		
		return theaters.size();
	}

	public Object getItem(int position) {

		return theaters.get(position);
	}

	public long getItemId(int position) {
	
		return position;
	}

	@SuppressWarnings("deprecation")
	public View getView(int position, View convertView, ViewGroup parent) {
		
		LayoutInflater myInflater = LayoutInflater.from(mContext);
		View converView = myInflater.inflate(R.layout.listview_schedule, null);
		
		if(theaters.size() != 0) {
			TextView theaterName = (TextView)converView.findViewById(R.id.textview_theatername);
			Button buttonBooking = (Button)converView.findViewById(R.id.button_booking);
			LinearLayout llSchedule = (LinearLayout)converView.findViewById(R.id.linearlayout_schedule);
			
			final Theater theater = theaters.get(position);
			theaterName.setText(theater.getName());
			
			if(theater.getBuyLink() != null) {
				final String url = theater.getBuyLink();
				buttonBooking.setVisibility(View.VISIBLE);
				buttonBooking.setOnClickListener(new OnClickListener(){
					public void onClick(View arg0) {
						EasyTracker.getTracker().trackEvent("電影訂票", "戲院", theater.getName(), (long)0);
						
						if(theater.getBuyLink().contains("http://www.ezding.com.tw/jumplife")) {
							Intent newAct = new Intent();
			                newAct.setClass(mContext, EzCheckActivity.class);
			                mContext.startActivity(newAct);
						} else {
							Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
							mContext.startActivity(intent);
						}
					}				
				});
			} else
				buttonBooking.setVisibility(View.GONE);
			
			String[] timeTable=theater.getTimeTable().split("\\|") ;
			Log.d(null, "timeTable Size : " + timeTable.length);
			for(int i=0; i<timeTable.length; i+=3){
				TableRow Schedule_row = new TableRow(mContext);
				for(int j=0; j<3; j++){
					int index = i + j;				
					TextView tv = new TextView(mContext);
					if(index < timeTable.length) 
						tv.setText(timeTable[index]);
					
					tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
					tv.setTextColor(mContext.getResources().getColor(R.color.background_deep));
					tv.setGravity(Gravity.CENTER);
					TableRow.LayoutParams Params = new TableRow.LayoutParams
							(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 0.33f);
					Params.setMargins(3, 3, 3, 3);
					tv.setLayoutParams(Params);
					Schedule_row.addView(tv);
				}
				Schedule_row.setLayoutParams(new LayoutParams
						(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				llSchedule.addView(Schedule_row);
			}			
		} else
			converView.setVisibility(View.GONE);


		return converView;
	}	
}
