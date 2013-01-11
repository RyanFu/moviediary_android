package com.jumplife.imageprocess;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class ImageProcess {
	public static Bitmap mergeBitmap(Bitmap fst, Bitmap sec, float xoffset, float yoffset) {
		Bitmap merge = null;
		int width, height = 0; 

	    width = sec.getWidth();
	    height = sec.getHeight();
	    
	    merge = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	    
	    Canvas comboImage = new Canvas(merge); 

	    comboImage.drawBitmap(sec, 0.0f, 0.0f, null); 
	    comboImage.drawBitmap(fst, xoffset, yoffset, null); 
	    
	    // this is an extra bit I added, just incase you want to save the new image somewhere and then return the location 
	    /*String tmpImg = String.valueOf(System.currentTimeMillis()) + ".png"; 

	    OutputStream os = null; 
	    try { 
	      os = new FileOutputStream(loc + tmpImg); 
	      cs.compress(CompressFormat.PNG, 100, os); 
	    } catch(IOException e) { 
	      Log.e("combineImages", "problem combining images", e); 
	    }*/ 
		return merge;
	}
}
