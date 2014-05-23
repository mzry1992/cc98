package android.application.cc98.network;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.application.cc98.GetWebPageInterface;
import android.application.cc98.SinglePostActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

public class DownloadImageTask extends AsyncTask<ImageView, Void, Bitmap> {

	private SinglePostActivity activity = null;
    ImageView imageView = null;

    public DownloadImageTask(SinglePostActivity activity) {
		if (null == activity)
			return;
		this.activity = activity;
	}
    
    @Override
    protected Bitmap doInBackground(ImageView... imageViews) {
        this.imageView = imageViews[0];
        return download_Image((String)imageView.getTag());
    }

    @Override
    protected void onPostExecute(Bitmap result) {
    	super.onPreExecute();
        imageView.setImageBitmap(result);
        this.activity.postLoadImage();
    }

    private Bitmap download_Image(String url) {

        Bitmap bmp =null;
        try{
            URL ulrn = new URL(url);
            HttpURLConnection con = (HttpURLConnection)ulrn.openConnection();
            InputStream is = con.getInputStream();
            bmp = BitmapFactory.decodeStream(is);
            if (null != bmp)
                return bmp;

            }catch(Exception e){}
        return bmp;
    }
}