package com.jumplife.LineEditText;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.EditText;

public class LineEditTextComment extends EditText {
	private Paint mPaint;  
    public LineEditTextComment(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        // TODO Auto-generated constructor stub  
        mPaint = new Paint();  
          
        mPaint.setStyle(Paint.Style.STROKE);  
        mPaint.setColor(Color.LTGRAY);
        mPaint.setStrokeWidth(2);
    }  
      
    @Override  
    public void onDraw(Canvas canvas) {  
        super.onDraw(canvas);  
        canvas.drawLine(0, this.getHeight()-15,  this.getWidth()-1, this.getHeight()-15, mPaint);
        canvas.drawLine(0, this.getHeight()-15,  0, this.getHeight()-24, mPaint);
        canvas.drawLine(this.getWidth()-1, this.getHeight()-15,  this.getWidth()-1, this.getHeight()-24, mPaint);
    }
}
