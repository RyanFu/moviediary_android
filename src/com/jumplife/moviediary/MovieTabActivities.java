package com.jumplife.moviediary;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.google.analytics.tracking.android.TrackedTabActivity;
import com.jumplife.imageload.ImageLoader;
import com.jumplife.loginactivity.LoginActivity;
import com.jumplife.loginactivity.Utility;
import com.jumplife.moviediary.api.MovieAPI;
import com.jumplife.setting.Setting;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;

@SuppressWarnings("deprecation")
public class MovieTabActivities extends TrackedTabActivity implements OnTabChangeListener {

    public final static int   SETTING     = 1;
    public final static int   FBLOGOUT    = 10;

    private static int        tabIdNumber = 0;
    private TabHost           tabHost;
    private TabHost.TabSpec   spec;                              // Resusable TabSpec for each tab
    private TextView          topbar_text;
    private ImageButton       search;
    private ImageButton       setting;
    private View              line;
    private SharePreferenceIO sharepre;
    private LinearLayout      topbarLayout;
    private int               openCount;
    private int               version;

    public static String      TAG         = "MovieTabActivities";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        topbarLayout = (LinearLayout) findViewById(R.id.topbar);

        topbar_text = (TextView) findViewById(R.id.topbar_text);
        topbar_text.setText("活動快報");

        Gcm gcm = new Gcm(this);
        
        tabHost = getTabHost(); // The activity TabHost
        tabHost.setup();

        tabSpread();
        tabActivityList();
        tabCreate();
        tabMyList();
        tabNews();

        setting = (ImageButton) findViewById(R.id.imagebutton_setting);
        setting.setOnClickListener(clickSetting);
        search = (ImageButton) findViewById(R.id.imagebutton_search);
        search.setOnClickListener(clickSearch);

        line = findViewById(R.id.view_line1);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle == null)
            tabHost.setCurrentTab(0);

        line.setVisibility(View.GONE);
        search.setVisibility(View.GONE);
        topbarLayout.setVisibility(View.VISIBLE);
        setting.setVisibility(View.VISIBLE);

        tabHost.setOnTabChangedListener(this);
        sharepre = new SharePreferenceIO(MovieTabActivities.this);
        openCount = sharepre.SharePreferenceO("opencount", 0);
        version = sharepre.SharePreferenceO("version", 0);
        if (openCount > 5) {
            LoadPromoteTask loadPromoteTask = new LoadPromoteTask();
            loadPromoteTask.execute();
            openCount = 0;
        }
        openCount += 1;
        sharepre.SharePreferenceI("opencount", openCount);
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null)
            tabHost.setCurrentTab(bundle.getInt("tabNo"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case SETTING:
            if (resultCode == FBLOGOUT) {
                tabHost.setCurrentTab(0);
            }

        case LoginActivity.LOGIN_ACTIVITY_REQUEST_CODE:
        	if (Utility.IsSessionValid(MovieTabActivities.this) && resultCode == LoginActivity.LOGIN_ACTIVITY_RESULT_CODE_SUCCESS) {
        		SharePreferenceIO sharepre = new SharePreferenceIO(MovieTabActivities.this);
                sharepre.SharePreferenceI("notification_key", true);
                tabHost.setCurrentTab(tabIdNumber);
                Log.d(TAG, "Success tab number : " + tabIdNumber);
	        }  else if(resultCode == LoginActivity.LOGIN_ACTIVITY_RESULT_CODE_FAIL)
	        	tabHost.setCurrentTab(0);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }

    private void tabSpread() { 	
        View ActivitysTab = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        ImageView image = (ImageView) ActivitysTab.findViewById(R.id.imageview_tabicon);
        image.setImageResource(R.drawable.activitybar);
        TextView ActivitysTabLabel = (TextView) ActivitysTab.findViewById(R.id.textview_tabicon);
        ActivitysTabLabel.setText("活動快報");

        Intent intentSpread = new Intent().setClass(this, SpreadActivity.class);
        spec = tabHost.newTabSpec("tab4").setIndicator(ActivitysTab).setContent(intentSpread);
        tabHost.addTab(spec);
    }

    private void tabActivityList() {
        View ActivitysTab = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        ImageView image = (ImageView) ActivitysTab.findViewById(R.id.imageview_tabicon);
        image.setImageResource(R.drawable.calendar_blank);
        TextView ActivitysTabLabel = (TextView) ActivitysTab.findViewById(R.id.textview_tabicon);
        ActivitysTabLabel.setText("電影打卡");

        // Create an Intent to launch an Activity for the tab (to be reused)
        // Initialize a TabSpec for each tab and add it to the TabHost
        Intent intentList = new Intent().setClass(this, MovieWaterfall.class);
        spec = tabHost.newTabSpec("tab1").setIndicator(ActivitysTab).setContent(intentList);
        tabHost.addTab(spec);
    }

    private void tabMyList() {
        View MyListTab = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        ImageView image = (ImageView) MyListTab.findViewById(R.id.imageview_tabicon);
        image.setImageResource(R.drawable.notebook);
        TextView MyListTabLabel = (TextView) MyListTab.findViewById(R.id.textview_tabicon);
        MyListTabLabel.setText("電影櫃");

        Intent intentMyList = new Intent().setClass(this, MyMovieRecord.class);
        spec = tabHost.newTabSpec("tab2").setIndicator(MyListTab).setContent(intentMyList);
        tabHost.addTab(spec);
    }

    private void tabCreate() {
        View CreateTab = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        ImageView image = (ImageView) CreateTab.findViewById(R.id.imageview_tabicon);
        image.setImageResource(R.drawable.users);
        TextView CreateTabLabel = (TextView) CreateTab.findViewById(R.id.textview_tabicon);
        CreateTabLabel.setText("朋友動態");

        Intent intentCreate = new Intent().setClass(this, FriendStream.class);
        spec = tabHost.newTabSpec("tab3").setIndicator(CreateTab).setContent(intentCreate);
        tabHost.addTab(spec);
    }

    private void tabNews() {
        View ActivitysTab = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        ImageView image = (ImageView) ActivitysTab.findViewById(R.id.imageview_tabicon);
        image.setImageResource(R.drawable.about);
        TextView ActivitysTabLabel = (TextView) ActivitysTab.findViewById(R.id.textview_tabicon);
        ActivitysTabLabel.setText("關於我們");

        Intent intentList = new Intent().setClass(this, AboutUsActivity.class);
        spec = tabHost.newTabSpec("tab5").setIndicator(ActivitysTab).setContent(intentList);
        tabHost.addTab(spec);
    }

    private final OnClickListener clickSearch  = new OnClickListener() {
	   public void onClick(View v) {
	       Intent intentCreate = new Intent().setClass(MovieTabActivities.this, SearchMovieActivity.class);
	       if (tabHost.getCurrentTab() == 0)
	           intentCreate.putExtra("check", false);
	       else
	    	   intentCreate.putExtra("check", true);
	           startActivity(intentCreate);
	       }
	   };

    private final OnClickListener clickSetting = new OnClickListener() {
       public void onClick(View v) {
           Intent intentCreate = new Intent().setClass(MovieTabActivities.this, Setting.class);
           startActivityForResult(intentCreate, SETTING);
       }
	};

    public void onTabChanged(String tabId) {
        if (tabId == "tab1") {
            tabIdNumber = 0;
            line.setVisibility(View.GONE);
            search.setVisibility(View.GONE);
            topbarLayout.setVisibility(View.GONE);
            topbar_text.setText("電影打卡");
        } else if (tabId == "tab2") {
            tabIdNumber = 1;
            line.setVisibility(View.GONE);
            search.setVisibility(View.GONE);
            topbar_text.setText("電影櫃");
            setting.setVisibility(View.VISIBLE);
            topbarLayout.setVisibility(View.VISIBLE);
            if (!Utility.IsSessionValid(MovieTabActivities.this)) {
            	Intent newAct = new Intent(); 
            	newAct.setClass( MovieTabActivities.this, LoginActivity.class );
            	MovieTabActivities.this.startActivityForResult(newAct, LoginActivity.LOGIN_ACTIVITY_REQUEST_CODE);
            }
        } else if (tabId == "tab3") {
            tabIdNumber = 2;
            line.setVisibility(View.GONE);
            search.setVisibility(View.GONE);
            topbar_text.setText("朋友動態");
            setting.setVisibility(View.VISIBLE);
            topbarLayout.setVisibility(View.VISIBLE);
            if (!Utility.IsSessionValid(MovieTabActivities.this)) {
            	Intent newAct = new Intent(); 
            	newAct.setClass( MovieTabActivities.this, LoginActivity.class );
            	MovieTabActivities.this.startActivityForResult(newAct, LoginActivity.LOGIN_ACTIVITY_REQUEST_CODE);
            }
        } else if (tabId == "tab4") {
            tabIdNumber = 3;
            line.setVisibility(View.GONE);
            search.setVisibility(View.GONE);
            setting.setVisibility(View.VISIBLE);
            topbarLayout.setVisibility(View.VISIBLE);
            // topbarLayout.setVisibility(View.GONE);
            topbar_text.setText("活動快報");
        } else if (tabId == "tab5") {
            tabIdNumber = 4;
            line.setVisibility(View.GONE);
            search.setVisibility(View.GONE);
            setting.setVisibility(View.VISIBLE);
            topbarLayout.setVisibility(View.VISIBLE);
            topbar_text.setText("關於我");
        }

    }
    
    class LoadPromoteTask extends AsyncTask<Integer, Integer, String> {

        private String[]               promotion      = new String[5];
        private ProgressDialog         progressdialogInit;
        private AlertDialog            dialogPromotion;

        private final OnCancelListener cancelListener = new OnCancelListener() {
                                                          public void onCancel(DialogInterface arg0) {
                                                              LoadPromoteTask.this.cancel(true);
                                                          }
                                                      };

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(MovieTabActivities.this);
            progressdialogInit.setTitle("Load");
            progressdialogInit.setMessage("Loading…");
            progressdialogInit.setOnCancelListener(cancelListener);
            progressdialogInit.setCanceledOnTouchOutside(false);
            progressdialogInit.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
            MovieAPI movieAPI = new MovieAPI();
            promotion = movieAPI.getPromotion();
            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            progressdialogInit.dismiss();
            if (promotion != null && !promotion[1].equals("null") && Integer.valueOf(promotion[4]) > version) {
                View viewPromotion;
                LayoutInflater factory = LayoutInflater.from(MovieTabActivities.this);
                viewPromotion = factory.inflate(R.layout.dialog_promotion, null);
                dialogPromotion = new AlertDialog.Builder(MovieTabActivities.this).create();
                dialogPromotion.setView(viewPromotion);
                ImageView imageView = (ImageView) viewPromotion.findViewById(R.id.imageView1);
                TextView textviewTitle = (TextView) viewPromotion.findViewById(R.id.textView1);
                TextView textviewDescription = (TextView) viewPromotion.findViewById(R.id.textView2);
                ImageLoader imageLoader = new ImageLoader(MovieTabActivities.this);
                if (!promotion[0].equals("null"))
                    imageLoader.DisplayImage(promotion[0], imageView);
                else
                    imageView.setVisibility(View.GONE);
                if (!promotion[2].equals("null"))
                    textviewTitle.setText(promotion[2]);
                else
                    textviewTitle.setVisibility(View.GONE);
                if (!promotion[3].equals("null"))
                    textviewDescription.setText(promotion[3]);
                else
                    textviewDescription.setVisibility(View.GONE);
                dialogPromotion.setOnKeyListener(new OnKeyListener() {
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    	sharepre.SharePreferenceI("version", Integer.valueOf(promotion[4]));
                        if (KeyEvent.KEYCODE_BACK == keyCode)
                            if (dialogPromotion != null && dialogPromotion.isShowing())
                                dialogPromotion.cancel();
                        return false;
                    }
                });
                ((Button) viewPromotion.findViewById(R.id.button2)).setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        // 取得文字方塊中的關鍵字字串
                        sharepre.SharePreferenceI("version", Integer.valueOf(promotion[4]));
                        if (dialogPromotion != null && dialogPromotion.isShowing())
                            dialogPromotion.cancel();

                        Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(promotion[1]));
                        MovieTabActivities.this.startActivity(intent);
                    }
                });
                dialogPromotion.setCanceledOnTouchOutside(false);
                dialogPromotion.show();
            }
            super.onPostExecute(result);
        }

    }
}