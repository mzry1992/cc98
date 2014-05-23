package android.application.cc98;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

class ExpressionAdapter extends BaseAdapter {
    private Context mContext;

    public ExpressionAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return expressions.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setBackgroundResource(expressions[position]);
		AnimationDrawable frameAnimation = (AnimationDrawable) imageView.getBackground();
		frameAnimation.start();
		
        return imageView;
    }

	// references to our images
    private Integer[] expressions = {
            R.drawable.em01, R.drawable.em02,
            R.drawable.em03, R.drawable.em04,
            R.drawable.em05, R.drawable.em06,
            R.drawable.em07, R.drawable.em08,
            R.drawable.em09, R.drawable.em10,
            R.drawable.em11, R.drawable.em12,
    };
}
