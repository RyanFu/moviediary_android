package com.jumplife.sectionlistview;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jumplife.imageload.AsyncImageLoader;
import com.jumplife.imageload.AsyncImageLoader.ImageCallBack;
import com.jumplife.moviediary.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class AsyncAdapter extends SimpleAdapter {
	
	private AsyncImageLoader imageLoader = new AsyncImageLoader();  
    private Map<Integer, View> mapView = new HashMap<Integer, View>();  
    private ViewBinder viewbinder;  
    private List<? extends Map<String, ?>> mData;   //List列表存放的数据  
    private int mResource;                          //绑定的页面 ,例如：R.layout.search_item,   
    private LayoutInflater mInflater;  
    private String[] mFrom;                         //绑定控件对应的数组里面的值名称  
    private int[] mTo;                              //绑定控件的ID
    private String mUsrId;
          
    /*
     * LayoutInflater根据XML布局文件来绘制View
     * 这个类无法直接创建实例，要通过context对象的getLayoutInflater()
     * 或getSystemService(String)方法来获得实例
     * 这样获得的布局泵实例符合设备的环境配置。        
     */
    public AsyncAdapter(Context context, List<? extends Map<String, ?>> data,
			int resource, String[] from, int[] to, String usrId) {
		super(context, data, resource, from, to);
		mData = data;  
        mResource = resource;  
        mFrom = from;  
        mTo = to;  
        mUsrId = usrId;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}  
        
	/*
	 * SimpleAdapter基类显示每个Item都是通过这个方法生成的
	 * 在getView(int position, View convertView, ViewGroup parent)中又调用了
     * SimpleAdapter的私有方法createViewFromResource
	 * 来组装View，在createViewFromResource中对SimpleAdapter的参数String[] from 和int[] to进行了组装    
	 */  
    public View getView(int position, View convertView, ViewGroup parent) {  
        return createViewFromResource(position, convertView, parent, mResource);    //调用下面方法  
    }  
  
   
    //在createViewFromResource方法中又有一个bindView(position, v)方法对item中的各个View进行了组装，bindView(position, v)  
    private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {  
       	View rowView = this.mapView.get(position);  
       			  
        if(rowView == null) {
        	rowView = mInflater.inflate(resource, null);
        	bindView(position, rowView);    //调用下面方法对Item中的  
		    mapView.put(position, rowView);  
        }
        //Delete要重新配置mapView;
        return rowView;  
    }  
      
    /*
     * 对ViewImage进行组装的代码了
     * else if (v instanceof ImageView)”  
     */
    private void bindView(int position, View view) {  
        //final Map<String, ?> dataSet = mData.get(position);  
    	final Map<String, ?> dataSet = mData.get(position);
    	if (dataSet == null) {  
            return;  
        }  
  
        final ViewBinder binder = viewbinder;  
        //final View[] holder = (View[]) view.getTag();  
        final String[] from = mFrom;  
        final int[] to = mTo;  
        final int count = to.length;  
  
        for (int i = 0; i < count; i++) {  
            if(to[i] == R.id.delete_img) {
        		final Object data = dataSet.get(from[i]);  
            	String id = data == null ? "" : data.toString();
            	final View v = view.findViewById(to[i]);
            	if(id.equals(mUsrId)) {
            		v.setVisibility(View.VISIBLE);
            	} else {
            		v.setVisibility(View.INVISIBLE);
            	}
        	} else {
	            final View v = view.findViewById(to[i]);
	            if (v != null) {  
	                final Object data = dataSet.get(from[i]);  
	                String urlText = data == null ? "" : data.toString();
	                if (urlText == null) {
	                	urlText = "";
	                }  
	                
	                boolean bound = false;  
	                if (binder != null) {  
	                    bound = binder.setViewValue(v, data, urlText);  
	                }  
	  
	                if (!bound) {  
	                    if (v instanceof Checkable) {  
	                        if (data instanceof Boolean) {  
	                            ((Checkable) v).setChecked((Boolean) data);  
	                        } 
	                        else {  
	                            throw new IllegalStateException(v.getClass()  
	                                    .getName()  
	                                    + " should be bound to a Boolean, not a "  
	                                    + data.getClass());  
	                        }  
	                    } 
	                    else if (v instanceof TextView) {  
	                        setViewText((TextView) v, urlText);  
	                    } 
	                    else if (v instanceof ImageView) {  
	                        if (data instanceof Integer) {  
	                            setViewImage((ImageView) v, (Integer) data);  
	                        } 
	                        else if (data instanceof Bitmap) {
	                        	setViewImage((ImageView) v, (Bitmap) data);
	                        } 
	                        else {  
	                            setViewImage((ImageView) v, urlText);  
	                        }  
	                    } 
	                    else {  
	                        throw new IllegalStateException(  
	                                v.getClass().getName()  
	                                        + " is not a "  
	                                        + " view that can be bounds by this SimpleAdapter");  
	                    }  
	                }  
	            } 
        	}
        }  
    }  
    
    public void remove(int item_position) {   
    	for(int position = 0; position < mapView.size()-1; position++) {
    		if(position >= item_position) {
    			mapView.remove(position);
		    	View rowView = mapView.get(position+1);  
		    	mapView.put(position, rowView);
    		}
    	}
    	mapView.remove(mapView.size()-1);
    }
  
    public void setViewImage(ImageView v, int value) {  
        v.setImageResource(value);  
    }
     
    public void setViewImage(ImageView v, Bitmap bitimg) {  
    	v.setImageBitmap(bitimg);
    }  
  
	public void setViewImage(final ImageView v, String url) {  
		imageLoader.loadBitmap(v, url, new ImageCallBack() {  
			public void imageLoad(ImageView imageView, Bitmap bitmap) {
				// TODO Auto-generated method stub
				if(bitmap == null) {  
		            imageView.setImageResource(R.drawable.person_color);  
		        }  
		        else {
		        	Bitmap usrImage = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()*2, bitmap.getHeight()*2, false);
		            imageView.setImageBitmap(usrImage);  
		        }
			 }  
    	 });  
     }
}
