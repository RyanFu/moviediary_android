package com.jumplife.moviediary;

import java.io.InputStream;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;
import com.google.analytics.tracking.android.TrackedActivity;
import com.jumplife.imageload.ImageLoader;
import com.jumplife.imageprocess.ImageProcess;
import com.jumplife.jome.entity.Comment;
import com.jumplife.loginactivity.BaseRequestListener;
import com.jumplife.loginactivity.FacebookIO;
import com.jumplife.loginactivity.LoginActivity;
import com.jumplife.loginactivity.SessionEvents;
import com.jumplife.loginactivity.Utility;
import com.jumplife.moviediary.api.MovieAPI;
import com.jumplife.moviediary.entity.Movie;
import com.jumplife.moviediary.entity.Record;
import com.jumplife.sectionlistview.CommentAdapter;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;

public class MovieRecord extends TrackedActivity {

    private TextView          topbar_text;
    private ImageView         poster;
    private TextView          chinese_name;
    private TextView          english_name;
    private ImageView         level;
    private ImageView         user_avatar;
    private TextView          user_name;
    private TextView          likecount;
    private TextView          runningtime;
    private ListView          listviewTips;
    private ImageButton       imagebuttonAdd;
    private ImageView         imageviewLike;
    private View              viewHeader;
    private View              viewFooter;
    private TextView          textviewPublishContent;
    private RelativeLayout    rlLike;
    private RelativeLayout    rlShare;
    private RelativeLayout    relativeMovieInfo;
    private RelativeLayout    relativeCheckInfo;

    private Movie             movie;
    private Record            record;
    private TextView          score;
    private TextView          comment;
    private TextView          time;
    private int               record_id;
    private boolean           owner;
    private Button            edit_btn;
    private String            fb_id;
    private CommentAdapter    commentAdapter;
    private ImageLoader       imageLoader;
    private UpdateTask        updateTask;
    private DeleteTask        deleteTask;
    private AddCommentTask    addCommentTask;
    private DeleteCommentTask deleteCommentTask;
    private LoadDataTask      loadDataTask;
    private Handler 		  mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movierecord);
        
        mHandler = new Handler();
        imageLoader = new ImageLoader(MovieRecord.this);
        Bundle extras = getIntent().getExtras();
        record_id = extras.getInt("record_id");
        owner = extras.getBoolean("Owner");
        if(Utility.IsSessionValid(MovieRecord.this))
        	fb_id = Utility.usrId;

        findViews();
        loadDataTask = new LoadDataTask();
        if(Build.VERSION.SDK_INT < 11)
        	loadDataTask.execute();
        else
        	loadDataTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case LoginActivity.LOGIN_ACTIVITY_REQUEST_CODE:
        	if (Utility.IsSessionValid(MovieRecord.this) && resultCode == LoginActivity.LOGIN_ACTIVITY_RESULT_CODE_SUCCESS) {
        		fb_id = Utility.usrId;
	        }
	        case LoginActivity.LOGIN_ACTIVITY_REQUEST_CODE_LIKE:
	        	if (Utility.IsSessionValid(MovieRecord.this) && resultCode == LoginActivity.LOGIN_ACTIVITY_RESULT_CODE_SUCCESS) {
	        		if (record.getIsLovedByUser()) {
	                    record.setLoveCount(record.getLoveCount() - 1);
	                    likecount.setTextColor(MovieRecord.this.getResources().getColor(R.color.black));
	                    likecount.setText(record.getLoveCount() + "個人推本打卡");
	                    imageviewLike.setImageResource(R.drawable.md_unlike_short);
	                } else {
	                    record.setLoveCount(record.getLoveCount() + 1);
	                    likecount.setTextColor(MovieRecord.this.getResources().getColor(R.color.title_dark));
	                    likecount.setText(record.getLoveCount() + "個人推本打卡");
	                    imageviewLike.setImageResource(R.drawable.md_like_short);
	                }
	                LikeTask likeTask = new LikeTask(record.getIsLovedByUser(), record.getId() + "");
	                likeTask.execute();
	                record.setIsLovedByUser(!record.getIsLovedByUser());
		        }
        }
    }
    
    private void setView() {
        movie = record.getMovie();
        topbar_text.setText("收藏");
        imageLoader.DisplayImage(movie.getPosterUrl(), poster);
        imageLoader.DisplayImage(movie.getLevelUrl(), level);
        imageLoader.DisplayImage(record.getUser().getIconUrl(), user_avatar);
        chinese_name.setText(movie.getChineseName());
        english_name.setText(movie.getEnglishName());
        if (movie.getRunningTime() != -1)
            runningtime.setText("片長 : " + movie.getRunningTime() + "分");
        else
            runningtime.setText("片長 : 未提供");
        score.setText("評價 : " + record.getScoreString() + "");
        comment.setText("評論 : " + record.getComment());
        likecount.setText(record.getLoveCount() + "個人推本打卡");

        DateFormat createFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        time.setText(createFormatter.format(record.getCheckinTime()));

        user_name.setText(record.getUser().getName());
        if (record.getIsLovedByUser()) {
        	likecount.setTextColor(MovieRecord.this.getResources().getColor(R.color.title_dark));
            imageviewLike.setImageResource(R.drawable.md_like_short);
        } else {
        	likecount.setTextColor(MovieRecord.this.getResources().getColor(R.color.black));
            imageviewLike.setImageResource(R.drawable.md_unlike_short);
        }
        
        if (owner)
        	rlShare.setVisibility(View.VISIBLE);
        else
        	rlShare.setVisibility(View.INVISIBLE);
        
        rlShare.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	PostRecordTask postRecordTask = new PostRecordTask();
            	postRecordTask.execute();
            }
        });
        rlLike.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if (Utility.IsSessionValid(MovieRecord.this)) {
            		if (record.getIsLovedByUser()) {
                        record.setLoveCount(record.getLoveCount() - 1);
                        likecount.setTextColor(MovieRecord.this.getResources().getColor(R.color.black));
                        likecount.setText(record.getLoveCount() + "個人推本打卡");
                        imageviewLike.setImageResource(R.drawable.md_unlike_short);
                    } else {
                        record.setLoveCount(record.getLoveCount() + 1);
                        likecount.setTextColor(MovieRecord.this.getResources().getColor(R.color.title_dark));
                        likecount.setText(record.getLoveCount() + "個人推本打卡");
                        imageviewLike.setImageResource(R.drawable.md_like_short);
                    }
                    LikeTask likeTask = new LikeTask(record.getIsLovedByUser(), record.getId() + "");
                    likeTask.execute();
                    record.setIsLovedByUser(!record.getIsLovedByUser());
                } else {
                	Intent newAct = new Intent(); 
                	newAct.setClass( MovieRecord.this, LoginActivity.class );
                	MovieRecord.this.startActivityForResult(newAct, LoginActivity.LOGIN_ACTIVITY_REQUEST_CODE_LIKE);
                }                
            }
        });
        relativeMovieInfo.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent newAct = new Intent();
                newAct.putExtra("movie_id", movie.getId());
                newAct.setClass(MovieRecord.this, MovieInfo.class);
                startActivity(newAct);
            }
        });
        relativeCheckInfo.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if (!Utility.IsSessionValid(MovieRecord.this)) {
                	Intent newAct = new Intent(); 
                	newAct.setClass( MovieRecord.this, LoginActivity.class );
                	MovieRecord.this.startActivityForResult(newAct, LoginActivity.LOGIN_ACTIVITY_REQUEST_CODE);
                } else {
                	Intent newAct = new Intent();
                    newAct.putExtra("fb_id", record.getUser().getAccount());
                    newAct.putExtra("avoid_check_session", true);
                    newAct.setClass(MovieRecord.this, MyMovieRecord.class);
                    startActivity(newAct);
                }
            }
        });
        edit_btn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                showEditDialog(record);
            }

        });
        imagebuttonAdd.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	if (!Utility.IsSessionValid(MovieRecord.this)) {
                	Intent newAct = new Intent(); 
                	newAct.setClass( MovieRecord.this, LoginActivity.class );
                	MovieRecord.this.startActivityForResult(newAct, LoginActivity.LOGIN_ACTIVITY_REQUEST_CODE);
                } else {
                	addCommentTask = new AddCommentTask(textviewPublishContent.getText().toString());
                    addCommentTask.execute();
                }
            }
        });

        if (owner)
            edit_btn.setVisibility(View.VISIBLE);
        else
            edit_btn.setVisibility(View.GONE);

        commentAdapter = new CommentAdapter(MovieRecord.this, record.getCommentList(), fb_id);
        listviewTips.removeHeaderView(viewHeader);
        listviewTips.removeFooterView(viewFooter);
        listviewTips.addHeaderView(viewHeader);
        listviewTips.addFooterView(viewFooter);
        listviewTips.setOnItemClickListener(clickDeleteTipsItem);
        listviewTips.setAdapter(commentAdapter);
    }

    private void showEditDialog(final Record record) {
        LayoutInflater inflater = LayoutInflater.from(MovieRecord.this);
        View view = inflater.inflate(R.layout.dialog_edit_record, null);
        ImageButton deleteRecord = (ImageButton) view.findViewById(R.id.delet_record);
        Button OK = (Button) view.findViewById(R.id.Confirm);
        ImageView poster = (ImageView) view.findViewById(R.id.imageview_movie_poster);
        final RadioGroup radio_group = (RadioGroup) view.findViewById(R.id.myRadioGroup);
        ImageView user_img = (ImageView) view.findViewById(R.id.imageview_userimg);
        final EditText comment = (EditText) view.findViewById(R.id.textview_descripe);

        ImageLoader imageLoader = new ImageLoader(MovieRecord.this);
        imageLoader.DisplayImage(record.getMovie().getPosterUrl(), poster);

        final int score = record.getScore();
        int score_id = R.id.myRadioButton1;
        switch (score) {
        case MovieAPI.SCORE_GOOD:
            score_id = R.id.myRadioButton1;
            break;
        case MovieAPI.SCORE_NORMAL:
            score_id = R.id.myRadioButton2;
            break;
        case MovieAPI.SCORE_BAD:
            score_id = R.id.myRadioButton3;
            break;
        }
        radio_group.check(score_id);

        Bitmap uerImg = Utility.usrImg;
        if (uerImg != null)
            user_img.setImageBitmap(uerImg);

        comment.setText(record.getComment());

        Builder builder = new AlertDialog.Builder(MovieRecord.this);
        builder.setView(view);

        final AlertDialog alertDialog = builder.create();

        OK.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                alertDialog.dismiss();
                int score = 0;
                switch (radio_group.getCheckedRadioButtonId()) {
                case R.id.myRadioButton1:
                    score = MovieAPI.SCORE_GOOD;
                    break;
                case R.id.myRadioButton2:
                    score = MovieAPI.SCORE_NORMAL;
                    break;
                case R.id.myRadioButton3:
                    score = MovieAPI.SCORE_BAD;
                    break;
                }
                record.setScore(score);
                record.setComment(comment.getText().toString());
                updateTask = new UpdateTask(record);
                updateTask.execute();
            }

        });

        deleteRecord.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                alertDialog.dismiss();
                new AlertDialog.Builder(MovieRecord.this).setTitle("刪除").setMessage("確定刪除這筆記錄嗎？")
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // delete record here
                                deleteTask = new DeleteTask(record);
                                deleteTask.execute();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        }).create().show();
            }

        });

        alertDialog.show();
    }

    private void fetchData() {
        MovieAPI movieAPI = new MovieAPI();
        record = movieAPI.getRecord(fb_id, record_id);
    }

    private void findViews() {
        listviewTips = (ListView) findViewById(R.id.listview_tips);
        topbar_text = (TextView) findViewById(R.id.topbar_text);

        viewHeader = LayoutInflater.from(MovieRecord.this).inflate(R.layout.listview_movierecord_header, null);
        viewFooter = LayoutInflater.from(MovieRecord.this).inflate(R.layout.listview_movierecord_footer, null);
        poster = (ImageView) viewHeader.findViewById(R.id.imageview_movie_poster);
        chinese_name = (TextView) viewHeader.findViewById(R.id.textView_chinese_name);
        english_name = (TextView) viewHeader.findViewById(R.id.textView_english_name);
        likecount = (TextView) viewHeader.findViewById(R.id.textView_like);
        user_avatar = (ImageView) viewHeader.findViewById(R.id.user_avatar);
        user_name = (TextView) viewHeader.findViewById(R.id.user_name);
        level = (ImageView) viewHeader.findViewById(R.id.imageView_level);
        runningtime = (TextView) viewHeader.findViewById(R.id.textView_runningtime);
        score = (TextView) viewHeader.findViewById(R.id.textView_score);
        comment = (TextView) viewHeader.findViewById(R.id.textView_comment);
        time = (TextView) viewHeader.findViewById(R.id.textView_time);
        relativeMovieInfo = (RelativeLayout) viewHeader.findViewById(R.id.relativelayout_movie_info);
        relativeCheckInfo = (RelativeLayout) viewHeader.findViewById(R.id.relativelayout_check_info);
        rlLike = (RelativeLayout) viewHeader.findViewById(R.id.rl_like);
        rlShare = (RelativeLayout) viewHeader.findViewById(R.id.rl_share);
        imageviewLike = (ImageView) viewHeader.findViewById(R.id.imageview_like);
        imagebuttonAdd = (ImageButton) viewFooter.findViewById(R.id.add_comment_btn);
        textviewPublishContent = (TextView) viewFooter.findViewById(R.id.add_comment_text);

        edit_btn = (Button) findViewById(R.id.edit);
    }

    private final OnItemClickListener clickDeleteTipsItem = new OnItemClickListener() {
	  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	      // TODO Auto-generated method stub
	      final int itemPosition = position - 1;
	      if (itemPosition > -1) {
	          final Comment comment = record.getCommentList().get(itemPosition);
	          AlertDialog.Builder delAlertDialog = new AlertDialog.Builder(MovieRecord.this);
	          delAlertDialog.setTitle("- 刪除留言 -");
	          delAlertDialog.setPositiveButton("刪除", new DialogInterface.OnClickListener() {
	
	              public void onClick(DialogInterface arg0, int arg1) {
	                  Log.d("ClickDeleteTipsItem", "item_position:" + itemPosition);
	                  deleteCommentTask = new DeleteCommentTask(comment);
	                  deleteCommentTask.execute();
	              }
	          });
	
	          delAlertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
	              public void onClick(DialogInterface arg0, int arg1) {
	                  // ...
	                  }
	              });
	
	              if (comment.getAccount().equals(fb_id))
	                  delAlertDialog.show();
	          }
	      }
	  };

    class LikeTask extends AsyncTask<Integer, Integer, String> {

        private final boolean          isLove;
        private final String           recordId;
        private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
                                                          public void onCancel(DialogInterface arg0) {
                                                              Log.d("", "loadDataTask.getStatus() != AsyncTask.Status.FINISHED");
                                                              LikeTask.this.cancel(true);
                                                          }
                                                      };

        private LikeTask(Boolean isLove, String recordId) {
            this.isLove = isLove;
            this.recordId = recordId;
        }

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(MovieRecord.this);
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
            if (isLove)
                movieAPI.unlikeRecords(fb_id, recordId);
            else
                movieAPI.likeRecords(fb_id, recordId);

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

    class UpdateTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final Record           record;
        private final OnCancelListener cancelListener = new OnCancelListener() {
                                                          public void onCancel(DialogInterface arg0) {
                                                              Log.d("", "loadDataTask.getStatus() != AsyncTask.Status.FINISHED");
                                                              UpdateTask.this.cancel(true);
                                                          }
                                                      };

        public UpdateTask(Record record) {
            this.record = record;
        }

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(MovieRecord.this);
            progressdialogInit.setTitle("修改");
            progressdialogInit.setMessage("上傳資料中…");
            progressdialogInit.setOnCancelListener(cancelListener);
            progressdialogInit.setCanceledOnTouchOutside(false);
            progressdialogInit.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
            MovieAPI movieAPI = new MovieAPI();
            movieAPI.updateRecord(record);
            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            progressdialogInit.dismiss();
            loadDataTask = new LoadDataTask();
            if(Build.VERSION.SDK_INT < 11)
            	loadDataTask.execute();
            else
            	loadDataTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
            super.onPostExecute(result);
        }

    }

    class DeleteTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final Record           record;
        private final OnCancelListener cancelListener = new OnCancelListener() {
                                                          public void onCancel(DialogInterface arg0) {
                                                              Log.d("", "loadDataTask.getStatus() != AsyncTask.Status.FINISHED");
                                                              DeleteTask.this.cancel(true);
                                                          }
                                                      };

        public DeleteTask(Record record) {
            this.record = record;
        }

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(MovieRecord.this);
            progressdialogInit.setTitle("刪除");
            progressdialogInit.setMessage("上傳資料中…");
            progressdialogInit.setOnCancelListener(cancelListener);
            progressdialogInit.setCanceledOnTouchOutside(false);
            progressdialogInit.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
            MovieAPI movieAPI = new MovieAPI();
            movieAPI.deleteRecord(record);
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
            MovieRecord.this.finish();
        }
    }

    class AddCommentTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final String           mComment;
        private final OnCancelListener cancelListener = new OnCancelListener() {
                                                          public void onCancel(DialogInterface arg0) {
                                                              Log.d("", "loadDataTask.getStatus() != AsyncTask.Status.FINISHED");
                                                              AddCommentTask.this.cancel(true);
                                                          }
                                                      };

        public AddCommentTask(String comment_text) {
            this.mComment = comment_text;
        }

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(MovieRecord.this);
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
            int commentId = movieAPI.createComment(record, fb_id, mComment);
            if (commentId != 0) {
                Comment comment = new Comment(commentId, fb_id, new Date(), mComment, record.getId());
                record.getCommentList().add(comment);
                return "progress end";
            } else
                return "progress failed";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            commentAdapter.notifyDataSetChanged();
            progressdialogInit.dismiss();

            if (result.equals("progress end")) {
                textviewPublishContent.setText(null);
                textviewPublishContent.setHint("新增留言");
                listviewTips.setSelection(listviewTips.getCount());
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                Toast.makeText(MovieRecord.this, "增加留言成功", Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(MovieRecord.this, "[網路異常] 無法增加留言", Toast.LENGTH_LONG).show();

            super.onPostExecute(result);
        }

    }

    class DeleteCommentTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final Comment          mComment;
        private final OnCancelListener cancelListener = new OnCancelListener() {
                                                          public void onCancel(DialogInterface arg0) {
                                                              Log.d("", "loadDataTask.getStatus() != AsyncTask.Status.FINISHED");
                                                              DeleteCommentTask.this.cancel(true);
                                                          }
                                                      };

        public DeleteCommentTask(Comment comment_text) {
            this.mComment = comment_text;
        }

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(MovieRecord.this);
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
            if (movieAPI.deleteComment(mComment)) {
                record.getCommentList().remove(mComment);
                return "progress end";
            } else
                return "progress failed";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            commentAdapter.notifyDataSetChanged();
            progressdialogInit.dismiss();
            if (result.equals("progress end"))
                Toast.makeText(MovieRecord.this, "刪除留言成功", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(MovieRecord.this, "[網路異常] 無法刪除留言", Toast.LENGTH_LONG).show();

            super.onPostExecute(result);
        }

    }

    class LoadDataTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
                                                          public void onCancel(DialogInterface arg0) {
                                                              Log.d("", "loadDataTask.getStatus() != AsyncTask.Status.FINISHED");
                                                              LoadDataTask.this.cancel(true);
                                                              finish();
                                                          }
                                                      };

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(MovieRecord.this);
            progressdialogInit.setTitle("Load");
            progressdialogInit.setMessage("Loading…");
            progressdialogInit.setOnCancelListener(cancelListener);
            progressdialogInit.setCanceledOnTouchOutside(false);
            progressdialogInit.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            fetchData();
            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            progressdialogInit.dismiss();
            if (record == null) {
                showReloadDialog(MovieRecord.this);
            } else {
                setView();
            }
            super.onPostExecute(result);
        }

    }

    class PostRecordTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog progressdialogInit;

        @Override
        protected void onPreExecute() {
            progressdialogInit = ProgressDialog.show(MovieRecord.this, "上傳", "上傳中…");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
        	 if (Utility.IsSessionValid(MovieRecord.this)
             		&& Utility.currentPermissions.containsKey("publish_actions") 
                     && Utility.currentPermissions.get("publish_actions").equals("1")) {
	        	int drawableId = R.drawable.fbgood;
	        	int score = record.getScore();
	            if (score == MovieAPI.SCORE_GOOD) {
	                drawableId = R.drawable.fbgood;
	            } else if (score == MovieAPI.SCORE_NORMAL) {
	                drawableId = R.drawable.fbsoso;
	            } else if (score == MovieAPI.SCORE_BAD) {
	                drawableId = R.drawable.fbbad;
	            }
	
	            String posterUrl = movie.getPosterUrl();
	            posterUrl = posterUrl.replaceFirst("mpost2", "mpost");
	            
	            InputStream is = MovieRecord.this.getResources().openRawResource(drawableId);
	            Bitmap sec = BitmapFactory.decodeStream(is);
	            sec = Bitmap.createScaledBitmap(sec, sec.getWidth(), sec.getHeight(), true);
	            Bitmap fst = imageLoader.getBitmapFromURL(posterUrl);
	            fst = Bitmap.createScaledBitmap(fst, 291, 418, true);
	
	            FacebookIO fbIO = new FacebookIO(MovieRecord.this);                
	            fbIO.photo(ImageProcess.mergeBitmap(fst, sec, 103, 84), record.getComment());                   
                return "progress end";
        	 } else {
             	Bundle bundle = new Bundle();
             	bundle.putString("access_token", Utility.mFacebook.getAccessToken());
                Utility.mAsyncRunner.request("me/permissions", bundle,
                         new permissionsRequestListener());
             	return "facebook error";
             }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
        	if (result.equals("progress end")) {
                Toast toast = Toast.makeText(MovieRecord.this, "FB分享成功", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            } else if(result.equals("facebook error")){
                Toast toast = Toast.makeText(MovieRecord.this, "FB分享 需要Facebook張貼權限", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        	
            progressdialogInit.dismiss();
            	
            super.onPostExecute(result);
        }

    }
    
    /*
     * Callback for the permission OAuth Dialog
     */
    public class permissionsRequestListener extends BaseRequestListener {

        public void onComplete(final String response, final Object state) {
        	/*
             * Clear the current permission list and repopulate with new
             * permissions. This is used to mark assigned permission green and
             * unclickable.
             */
            Utility.currentPermissions.clear();
            
            try {
                JSONObject jsonObject = new JSONObject(response).getJSONArray("data")
                        .getJSONObject(0);
                Iterator<?> iterator = jsonObject.keys();
                
                int permissionInt;
                String permissionStr;
                String permissionBool = null;
                String permissionName = null;
                
                while (iterator.hasNext()) {
                	permissionStr = (String) iterator.next();
                	permissionInt = jsonObject.getInt(permissionStr);
                	permissionName = permissionStr + ",";
                	permissionBool = permissionInt + ",";
                	Utility.currentPermissions.put(permissionStr, String.valueOf(permissionInt));
                }
            	permissionName = permissionName.substring(0, permissionName.length()-1);
                permissionBool = permissionBool.substring(0, permissionBool.length()-1);
                SharePreferenceIO sharepre= new SharePreferenceIO(MovieRecord.this);
                sharepre.SharePreferenceI("fbPERMISSIONNAME", permissionName);
                sharepre.SharePreferenceI("fbPERMISSIONBOOL", permissionBool);
            } catch (JSONException e) {
            }
            mHandler.post(new Runnable() {
                public void run() {
                    Utility.mFacebook.authorize(MovieRecord.this, LoginActivity.permission_publish, new LoginDialogListener());
                }
            });
        }

        public void onFacebookError(FacebookError error) {
        }

    }
    
    /*
     * Callback when user has authorized the app with the new permissions
     */
    private final class LoginDialogListener implements DialogListener {
        public void onComplete(Bundle values) {
            // Inform the parent loginlistener so it can update the user's
            // profile pic and name on the home screen.
            SessionEvents.onLoginSuccess();
        }

        public void onFacebookError(FacebookError error) {
        }

        public void onError(DialogError error) {
        }

        public void onCancel() {
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (loadDataTask != null && loadDataTask.getStatus() != AsyncTask.Status.FINISHED)
            loadDataTask.cancel(true);
        if (updateTask != null && updateTask.getStatus() != AsyncTask.Status.FINISHED)
            updateTask.cancel(true);
        if (deleteTask != null && deleteTask.getStatus() != AsyncTask.Status.FINISHED)
            deleteTask.cancel(true);
        if (addCommentTask != null && addCommentTask.getStatus() != AsyncTask.Status.FINISHED)
            addCommentTask.cancel(true);
        if (deleteCommentTask != null && deleteCommentTask.getStatus() != AsyncTask.Status.FINISHED)
            deleteCommentTask.cancel(true);

        super.onDestroy();
    }

    public void showReloadDialog(final Context context) {
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(context);

        alt_bld.setMessage("是否重新載入資料？").setCancelable(true).setPositiveButton("確定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                LoadDataTask tast = new LoadDataTask();
                if(Build.VERSION.SDK_INT < 11)
                	tast.execute();
                else
                	tast.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
                dialog.dismiss();
            }
        });

        AlertDialog alert = alt_bld.create();
        // Title for AlertDialog
        alert.setTitle("讀取錯誤");
        // Icon for AlertDialog
        // alert.setIcon(R.drawable.gnome_logout);
        alert.show();

    }
}
