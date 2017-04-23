package de.smasi.tickmate.views;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import de.smasi.tickmate.R;

public class IconListAdapter extends BaseAdapter {
    private Context context;
    private List<String> icon_names;

    public IconListAdapter(Context c) {
        context = c;
        icon_names = new LinkedList<String>();
        
        Field[] drawables = R.drawable.class.getFields();
        
        for (Field f : drawables) {
        	String name = f.getName();
        	if ((name.startsWith("glyphicons") || name.startsWith("myicons")) && name.endsWith("white")) {
        		icon_names.add(f.getName());
        	}                
        }
    }

    public int getCount() {
        return icon_names.size();
    }

    public Object getItem(int position) {
        return icon_names.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
		Resources r = context.getResources();
		int icon_id = r.getIdentifier((String)getItem(position), "drawable", context.getPackageName());

        imageView.setImageResource(icon_id);
        imageView.setContentDescription(icon_names.get(position));
        return imageView;
    }

}