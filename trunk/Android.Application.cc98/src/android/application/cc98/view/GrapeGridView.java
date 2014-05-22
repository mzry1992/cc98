package android.application.cc98.view;

import android.application.cc98.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;

public class GrapeGridView extends GridView {

	public GrapeGridView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		}
		public GrapeGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		}
		
		public GrapeGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
     
    //通过重新dispatchTouchEvent方法来禁止滑动
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if(ev.getAction() == MotionEvent.ACTION_MOVE){
			return true;//禁止Gridview进行滑动
		}
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
    protected void dispatchDraw(Canvas canvas){
        super.dispatchDraw(canvas);
        View localView1 = getChildAt(0);
        int column = getWidth() / localView1.getWidth();
        int childCount = getChildCount();
        Paint localPaint;
        localPaint = new Paint();
        localPaint.setStyle(Paint.Style.STROKE);
        localPaint.setColor(getContext().getResources().getColor(R.color.grid_line));
        for(int i = 0;i < childCount;i++){
            View cellView = getChildAt(i);
            if((i + 1) % column == 0){
                canvas.drawLine(cellView.getLeft(), cellView.getBottom(), cellView.getRight(), cellView.getBottom(), localPaint);
            }else if((i + 1) > (childCount - (childCount % column))){
                canvas.drawLine(cellView.getRight(), cellView.getTop(), cellView.getRight(), cellView.getBottom(), localPaint);
            }else{
                canvas.drawLine(cellView.getRight(), cellView.getTop(), cellView.getRight(), cellView.getBottom(), localPaint);
                canvas.drawLine(cellView.getLeft(), cellView.getBottom(), cellView.getRight(), cellView.getBottom(), localPaint);
            }
        }
        if(childCount % column != 0){
            for(int j = 0 ;j < (column-childCount % column) ; j++){
                View lastView = getChildAt(childCount - 1);
                canvas.drawLine(lastView.getRight() + lastView.getWidth() * j, lastView.getTop(), lastView.getRight() + lastView.getWidth()* j, lastView.getBottom(), localPaint);
            }
        }
    }
}