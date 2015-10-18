package de.smasi.tickmate;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;

import de.smasi.tickmate.database.DatabaseOpenHelper;
import de.smasi.tickmate.models.Group;
import de.smasi.tickmate.views.AboutActivity;
import de.smasi.tickmate.views.EditGroupsActivity;
import de.smasi.tickmate.views.EditTracksActivity;
import de.smasi.tickmate.views.GroupPreferenceActivity;
import de.smasi.tickmate.views.SettingsActivity;
import lab.prada.android.ui.infinitescroll.InfiniteScrollAdapter;

public class Tickmate extends ListActivity implements InfiniteScrollAdapter.InfiniteScrollListener, View.OnClickListener {
    static final int DATE_DIALOG_ID = 0;
    private static final String TAG = "Tickmate";

    private InfiniteScrollAdapter<TickAdapter> mAdapter;
    private Handler mHandler;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(" + savedInstanceState + ")");
        //setContentView(R.layout.fragment_tickmate_ticks);
		//matrix = (TickMatrix)findViewById(R.id.tickMatrix1);
        setContentView(R.layout.activity_tickmate_list);
        
		Calendar today = Calendar.getInstance();
		
        RelativeLayout progress = new RelativeLayout(this);
        progress.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, 100));
        progress.setGravity(Gravity.CENTER);
        progress.addView(new ProgressBar(this));

        mAdapter = new InfiniteScrollAdapter<TickAdapter>(this,
                new TickAdapter(this, today, savedInstanceState), progress);
        mAdapter.addListener(this);
        mHandler = new Handler();

        updateHeader();

		TextView emptyView = (TextView)findViewById(android.R.id.empty);

		emptyView.setOnClickListener(this);

	   	getListView().setStackFromBottom(true);
        getListView().setAdapter(mAdapter);
        getListView().setOnTouchListener(mAdapter.getAdapter());
        getListView().getEmptyView().setOnTouchListener(mAdapter.getAdapter());
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState(" + outState + ")");
        mAdapter.getAdapter().saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        Log.d(TAG, "onRestoreInstanceState(" + state + ")");
        mAdapter.getAdapter().restoreState(state);
        super.onRestoreInstanceState(state);
    }

    private void updateHeader() {
        LinearLayout header_group = ((LinearLayout) findViewById(R.id.header));
//		if (mAdapter.getOriginalAdapter().getCount() > 0) { // With the group spinner,
// this getCount() no longer tells us whether these views need to be redrawn.
// TODO Consider re-inserting a conditional here to determine whether these views need to be redrawn,
//    and which might compare values based on the spinner position (or...?)
        header_group.removeAllViews();
        header_group.addView(mAdapter.getOriginalAdapter().getHeader());
//		}
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
            case R.id.action_settings:
                this.settingsActivity();
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
            // "Edit Groups" being placed in this menu because we don't yet have a better place.
            case R.id.action_edit_groups:
                this.editGroups();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void editGroups() {
        Intent intent = new Intent(this, EditGroupsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tickmate, menu);
        return true;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume()");

        refresh();

        // Next stanza introduced to address issue #42
        boolean isTodayAtTop = PreferenceManager.getDefaultSharedPreferences(this).
                getBoolean("reverse-date-order-key", false);
        int scrollposition = (isTodayAtTop) ? 0 : mAdapter.getAdapter().getCount() - 1;
        getListView().smoothScrollToPosition(scrollposition);

        super.onResume();
    }

    public void editTracks(View v) {
        Intent intent = new Intent(this, EditTracksActivity.class);
        startActivity(intent);
    }

    public void aboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    public void settingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
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
        } else {
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
        ((TickAdapter) getListAdapter()).scrollToLatest();  // TODO js Confirm if this is the best place in the code to force a scroll to latest.
        // TODO avp and hg, does this look okay to you?  Can I leave the above line where it is and delete the above comment?
        mAdapter.getAdapter().unsetActiveDay();
        refresh();
    }

    public void setDate(int year, int month, int day) {
        Calendar thatday = Calendar.getInstance();
        thatday.set(year, month, day);
        mAdapter.getAdapter().setActiveDay(thatday);
        refresh();
    }

    public void refresh() {
        mAdapter.getAdapter().notifyDataSetChanged();
        getListView().invalidateViews();
        updateHeader();
    }

    public Calendar getDate() {
        return mAdapter.getAdapter().getActiveDay();
    }

    @Override
    public void onClick(View v) {
        Group displayedGroup = mAdapter.getAdapter().getGroupCurrentlyDisplayed();

        if ( displayedGroup == Group.ALL_GROUP ) {
            this.editTracks(getCurrentFocus());
        } else {
            Intent intent = new Intent(this, GroupPreferenceActivity.class);
            intent.putExtra("group_id", displayedGroup.getId());
            intent.putExtra("openTrackList", true);
            startActivity(intent);
        }
    }

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar c = ((Tickmate) getActivity()).getDate();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            ((Tickmate) getActivity()).setDate(year, month, day);
            ((TickAdapter) ((Tickmate) getActivity()).getListAdapter()).scrollToLatest();
        }
	}


    @Override
    public ListAdapter getListAdapter() {
        return mAdapter.getAdapter();
    }

    @Override
    public void onInfiniteScrolled() {
        final int CHUNK_SIZE = 20; // Large chunk sizes (more than items on the screen) prevent problems when switching date order direction
        // Matching the addCount() amount to the setSelection() amount keeps the screen in the right place when infiniteScrolling upwards

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter.getAdapter().addCount(CHUNK_SIZE);  // was: mAdapter.getAdapter().addCount(5);
                mAdapter.handledRefresh();

                Boolean reverseDateOrdering = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).
                        getBoolean("reverse-date-order-key", false);
                if (!reverseDateOrdering) {
                    getListView().setSelection(CHUNK_SIZE); // When infiniteScrolling upwards,
                    // prevents the infinite loop bug and keeps display from jumping
                }
            }
        }, 500);
    }


}
