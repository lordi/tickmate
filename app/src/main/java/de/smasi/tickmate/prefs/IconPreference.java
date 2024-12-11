package de.smasi.tickmate.prefs;

import android.content.Context;
import android.content.res.Resources;
import android.preference.EditTextPreference;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;

import de.smasi.tickmate.R;
import de.smasi.tickmate.views.IconListAdapter;

public class IconPreference extends EditTextPreference {

    @Override
	public void setText(String text) {
		super.setText(text);
		Context context = getContext();
		Resources r = context.getResources();
		
		setIcon(r.getIdentifier(getText(), "drawable", context.getPackageName()));
	}

    @Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
	      GridView gridview = (GridView) view.findViewById(R.id.icon_chooser_gridview);

	      gridview.setAdapter(new IconListAdapter(getContext()));

	      gridview.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int id, long arg3) {
					setText((String)arg0.getItemAtPosition(id));
					getDialog().dismiss();
				}
	      });
		getEditText().setVisibility(View.GONE);
		getEditText().setInputType(InputType.TYPE_NULL);
		((LinearLayout) view.findViewById(R.id.icon_chooser_layout)).addView(getEditText());
	}

	public IconPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		setLayoutResource(R.layout.icon_preference);
		setDialogLayoutResource(R.layout.dialog_icon_chooser);
		setDialogTitle(R.string.title_icon_chooser);
	}

}
