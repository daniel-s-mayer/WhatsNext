package com.mayerstudios.whatsnext.ui.dashboard;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.mayerstudios.whatsnext.R;

/**
 * ImageAdapter class to display the proper image in the subscriptions list.
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;

    // Superclass overrides.
    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    /**
     * Create a new ImageView for the image being displayed.
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

    // All of the possible icons.
    public Integer[] mThumbIds = {R.drawable.question, R.drawable.tv,
            R.drawable.phone, R.drawable.smartphone, R.drawable.game,
            R.drawable.ic_gplay, R.drawable.ic_google, R.drawable.ic_office,
            R.drawable.ic_spotify, R.drawable.ic_netflix, R.drawable.ic_cloud,
            R.drawable.ic_news, R.drawable.wifi, R.drawable.ic_cc,
            R.drawable.pandora, R.drawable.amazon, R.drawable.vpn, R.drawable.math,
            R.drawable.amusic, R.drawable.video, R.drawable.box, R.drawable.hbo,
            R.drawable.autodesk};
}
