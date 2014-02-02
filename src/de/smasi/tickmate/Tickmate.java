package de.smasi.tickmate;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import de.smasi.tickmate.views.AboutActivity;
import de.smasi.tickmate.views.EditTracksActivity;

public class Tickmate extends Activity {
    static final int DATE_DIALOG_ID = 0;
    TickMatrix matrix;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_tickmate_ticks);
		matrix = (TickMatrix)findViewById(R.id.tickMatrix1);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_edit_tracks:
				this.editTracks(getCurrentFocus());
	            return true;
			case R.id.action_about:
				this.aboutActivity();
	            return true;	            
			case R.id.action_jump_to_date:
				this.jumpToDate();
	            return true;	            
			case R.id.action_jump_to_today:
				this.jumpToToday();
	            return true;	            
	        default:
	            return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.tickmate, menu);
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		matrix.buildView();
	}
	
	public void editTracks(View v) {
		Intent intent = new Intent(this, EditTracksActivity.class);
	    startActivity(intent);
	}
	
	public void aboutActivity() {
		Intent intent = new Intent(this, AboutActivity.class);
	    startActivity(intent);
	}
	
	public void jumpToDate() {
		DialogFragment newFragment = new DatePickerFragment();
	    newFragment.show(getFragmentManager(), "datePicker");
	}	
	
	public void jumpToToday() {
		matrix.unsetDate();
		matrix.buildView();
	}
	
	public void setDate(int year, int month, int day) {
		matrix.setDate(year, month, day);
		matrix.buildView();
	}
	
	public Calendar getDate() {
		return matrix.getDate();
	}
	
	public static class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Calendar c = ((Tickmate)getActivity()).getDate();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			((Tickmate)getActivity()).setDate(year, month, day);
		}
	}
}
