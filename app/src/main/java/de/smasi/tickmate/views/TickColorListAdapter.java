package de.smasi.tickmate.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

import de.smasi.tickmate.TickColor;

public class TickColorListAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mTickColorNames;

    public TickColorListAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return TickColor.getNumberOfColors();
    }

    public Object getItem(int position) {
        return mTickColorNames.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        imageView = new ImageView(mContext);

        imageView.setLayoutParams(new GridView.LayoutParams(240, 120));
        imageView.setScaleType(ImageView.ScaleType.CENTER);

        imageView.setPadding(8, 8, 8, 8);
        imageView.setImageDrawable(TickColor.getPreferenceDrawable(mContext, position));  // TODO JS for now, using the unchecked image.  replace with appropriate colored image
        return imageView;
    }
}