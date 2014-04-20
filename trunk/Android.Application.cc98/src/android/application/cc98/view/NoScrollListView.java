package android.application.cc98.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.ListView;

public class NoScrollListView extends ListView {
    
    public NoScrollListView(Context context, AttributeSet attrs) {
            super(context, attrs);
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int mExpandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                            MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, mExpandSpec);
    }

}