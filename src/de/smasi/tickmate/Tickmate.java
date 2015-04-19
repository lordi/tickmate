package de.smasi.tickmate;

import java.io.IOException;
import java.util.Calendar;

import lab.prada.android.ui.infinitescroll.InfiniteScrollAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import de.smasi.tickmate.database.DatabaseOpenHelper;
import de.smasi.tickmate.views.AboutActivity;
import de.smasi.tickmate.views.EditTracksActivity;

public class Tickmate extends ListActivity implements InfiniteScrollAdapter.InfiniteScrollListener {
    static final int DATE_DIALOG_ID = 0;

    private InfiniteScrollAdapter<TickAdapter> mAdapter;
    private Handler mHandler;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.fragment_tickmate_ticks);
		//matrix = (TickMatrix)findViewById(R.id.tickMatrix1);
        setContentView(R.layout.activity_tickmate_list);
        
		Calendar today = Calendar.getInstance();
		
        RelativeLayout progress = new RelativeLayout(this);
        progress.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, 100));
        progress.setGravity(Gravity.CENTER);
        progress.addView(new ProgressBar(this));

        mAdapter = new InfiniteScrollAdapter<TickAdapter>(this,
                new TickAdapter(this, today), progress);
        mAdapter.addListener(this);
        mHandler = new Handler();
        
        LinearLayout header_group = ((LinearLayout) findViewById(R.id.header));
        header_group.addView(mAdapter.getOriginalAdapter().getHeader());
        
        //getListView().addHeaderView(mAdapter.getOriginalAdapter().getHeader());
		getListView().setStackFromBottom(true);

        getListView().setAdapter(mAdapter);
        //((ListView)this.findViewById(R.id.listView)).setAdapter(mAdapter);
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
			case R.id.action_export_db:
				this.exportDB();
	            return true;	            
			case R.id.action_import_db:
				this.importDB();
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
		refresh();
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
	
	public void exportDB() {
		final EditText input = new EditText(this);
		Calendar today = Calendar.getInstance();
		
		int year = today.get(Calendar.YEAR);
		int month = today.get(Calendar.MONTH) + 1;
		int day = today.get(Calendar.DAY_OF_MONTH);
		
		input.setText(String.format("tickmate-backup-%04d%02d%02d.db", year, month, day));
		new AlertDialog.Builder(this)
		    .setTitle(R.string.export_db)
		    .setView(input)
		    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            Editable value = input.getText();
		            String name = value.toString();
	            	try {
						DatabaseOpenHelper.getInstance(Tickmate.this).exportDatabase(name);
						Toast.makeText(Tickmate.this, R.string.export_db_success, Toast.LENGTH_LONG).show();
					} catch (IOException e) {
						Toast.makeText(Tickmate.this, e.toString(), Toast.LENGTH_LONG).show();
					}
		        }
		    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            // Do nothing.
		        }
		    }).show();
	}
	
	public void importDB() {
	    final String[] items = DatabaseOpenHelper.getInstance(this).getExternalDatabaseNames();
	    if (items.length == 0) {
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage(R.string.import_db_none_found)
	    		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) {
			        }
			    })
	    	    .show();
	    }
	    else {
		    AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    builder.setTitle(R.string.import_db);
		    
		    builder.setItems(items, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {
		        	final int that = which;
				    AlertDialog.Builder builder = new AlertDialog.Builder(Tickmate.this);
				    builder.setMessage(R.string.import_db_really)
			    		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int which) {
					    		try {
									DatabaseOpenHelper.getInstance(Tickmate.this).importDatabase(items[that]);
									Toast.makeText(Tickmate.this, R.string.import_db_success, Toast.LENGTH_LONG).show();
									refresh();
								} catch (IOException e) {
									Toast.makeText(Tickmate.this, e.toString(), Toast.LENGTH_LONG).show();
								}            
					        }
					    })
			    		.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int which) {
					        }
					    })
			    	    .show();						
		        }
		    });
		    builder.show();
	    }
	}	
	
	public void jumpToToday() {
		Calendar day = Calendar.getInstance();
		mAdapter.getAdapter().setDate(day);
		refresh();
	}
	
	public void setDate(int year, int month, int day) {
		Calendar thatday = Calendar.getInstance();
		thatday.set(year, month, day);
		mAdapter.getAdapter().setDate(thatday);
		refresh();
	}
	
	public void refresh() {
		getListView().invalidateViews();
	}
	
	public Calendar getDate() {
		return mAdapter.getAdapter().getDate();
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
	
    @Override
    public void onInfiniteScrolled() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter.getAdapter().addCount(5);
                mAdapter.handledRefresh();
                getListView().setSelection(2);
            }
        }, 500);
    }
}
