package de.smasi.tickmate.views;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import de.smasi.tickmate.R;
import de.smasi.tickmate.models.Track;

public class TrackListAdapter extends ArrayAdapter<Track> {
	private final Context context;
	private final Track[] values;
	
	public TrackListAdapter(Context context, Track[] values) {
	  super(context, R.layout.rowlayout, values);
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
			textView.setText(values[position].getName().substring(4).toUpperCase(Locale.getDefault()));			
		}
		else {
			rowView = inflater.inflate(R.layout.rowlayout, parent, false);
			Track t = values[position];
			TextView textView = (TextView) rowView.findViewById(R.id.label);
			TextView textViewS = (TextView) rowView.findViewById(R.id.sublabel);
			ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
			imageView.setImageResource(t.getIconId(context));
			if (t.isEnabled()) {
				textView.setText(t.getName());
				textView.setTypeface(null, Typeface.NORMAL);
				textViewS.setTypeface(null, Typeface.NORMAL);
				imageView.setAlpha(1.0f);
			}
			else {
				textView.setText(t.getName() + " (" + context.getString(R.string.inactive) + ")");
				int gray = getContext().getResources().getColor(android.R.color.darker_gray);
				textView.setTypeface(null, Typeface.ITALIC);
				textView.setTextColor(gray);
				textViewS.setTypeface(null, Typeface.ITALIC);
				textViewS.setTextColor(gray);
				imageView.setAlpha(0.5f);
			}
			textViewS.setText(t.getDescription());
			
		}

		return rowView;
	  }
}
