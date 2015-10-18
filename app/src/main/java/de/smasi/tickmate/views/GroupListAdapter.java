package de.smasi.tickmate.views;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Locale;

import de.smasi.tickmate.R;
import de.smasi.tickmate.models.Group;

public class GroupListAdapter extends ArrayAdapter<Group> {
    private final Context context;
    private final Group[] values;

    public GroupListAdapter(Context context, Group[] values) {
        super(context, R.layout.group_row, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (this.values[position].isSectionHeader()) {
            rowView = inflater.inflate(R.layout.rowlayout_header, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.section_header);
            textView.setText(values[position].getName().toUpperCase(Locale.getDefault()));
        } else {
            rowView = inflater.inflate(R.layout.group_row, parent, false);
            Group g = values[position];
            TextView textView = (TextView) rowView.findViewById(R.id.label);
            TextView textViewS = (TextView) rowView.findViewById(R.id.sublabel);
//            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
//			imageView.setImageResource(g.getIconId(context));
            textView.setText(g.getName());
            textView.setTypeface(null, Typeface.NORMAL);
            textViewS.setTypeface(null, Typeface.NORMAL);



//            imageView.setAlpha(1.0f);

            textViewS.setText(g.getDescription());
        }

        return rowView;
    }
}

