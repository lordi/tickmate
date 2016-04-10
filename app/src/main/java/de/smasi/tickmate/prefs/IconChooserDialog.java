package de.smasi.tickmate.prefs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import de.smasi.tickmate.R;
import de.smasi.tickmate.views.IconListAdapter;

public class IconChooserDialog extends Dialog {
	
	String selected_icon = null;
	public IconChooserDialog(Context context) {
		super(context);
		selected_icon = null;
	}

	public String getSelectedIcon() {
		return selected_icon;
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      final Dialog dialog = this;
      setTitle(R.string.title_icon_chooser);
      setCancelable(true);
      setContentView(R.layout.dialog_icon_chooser);
      GridView gridview = (GridView)findViewById(R.id.icon_chooser_gridview);

      gridview.setAdapter(new IconListAdapter(getContext()));

      gridview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int id, long arg3) {
				selected_icon = (String)arg0.getItemAtPosition(id);
				dialog.dismiss();
			}
      });     
    }

}
