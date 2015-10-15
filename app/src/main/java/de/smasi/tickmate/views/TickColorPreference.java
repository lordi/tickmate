package de.smasi.tickmate.views;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import de.smasi.tickmate.R;
import de.smasi.tickmate.TickColor;

// Todo, low  priority:  Find a less fragile way to transmit the hex code from preference to track
// Note that setText is used to store the 6 character hex code for the color value
//   (ie 8a8aff), which is used to pass the select color value to track and to the
//   database.  If the content of this text field is changed, this will break.

public class TickColorPreference extends EditTextPreference {

    private static final String TAG = "TickColorPreference";

    public void setColor(TickColor tickColor) {
        super.setText(tickColor.getName());
        setIcon(tickColor.getTickedButtonDrawable(getContext()));
        setText(tickColor.getName());
    }


    @Override

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        GridView gridView = (GridView) view.findViewById(R.id.tick_color_chooser_gridview);

        gridView.setAdapter(new TickColorListAdapter(getContext()));

        gridView.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int id, long arg3) {
//                setText((String) arg0.getItemAtPosition(id));
//                getDialog().dismiss();
//            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setText(TickColor.getColor(position).getName());
                setColor(TickColor.getColor(position));
                getDialog().dismiss();
            }
        });
    }

    public TickColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setLayoutResource(R.layout.tick_color_preference);
        setDialogLayoutResource(R.layout.dialog_tick_color_chooser);
        setDialogTitle("Choose a tick color TODO ");
    }

    // Returns a sting hex code ready for 'parseInt(x,16)'
    public String getHexString() {
        // TODO confirm which modifications to getText() are actually needed to make this work
        return getText().trim().toUpperCase();
    }
}
