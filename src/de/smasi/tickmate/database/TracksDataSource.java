package de.smasi.tickmate.database;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import de.smasi.tickmate.models.Tick;
import de.smasi.tickmate.models.Track;

public class TracksDataSource {

	// Database fields
	private SQLiteDatabase database;
	private DatabaseOpenHelper dbHelper;
	
	private String[] allColumns = {
			DatabaseOpenHelper.COLUMN_ID,
			DatabaseOpenHelper.COLUMN_NAME,
			DatabaseOpenHelper.COLUMN_ENABLED,
			DatabaseOpenHelper.COLUMN_DESCRIPTION,
			DatabaseOpenHelper.COLUMN_ICON,
			DatabaseOpenHelper.COLUMN_MULTIPLE_ENTRIES_PER_DAY,
			"\"" + DatabaseOpenHelper.COLUMN_ORDER + "\""
	};
	private String[] allColumnsTicks = {
			DatabaseOpenHelper.COLUMN_ID,
			DatabaseOpenHelper.COLUMN_TRACK_ID,
			DatabaseOpenHelper.COLUMN_YEAR,
			DatabaseOpenHelper.COLUMN_MONTH,
			DatabaseOpenHelper.COLUMN_DAY,
			DatabaseOpenHelper.COLUMN_HOUR,
			DatabaseOpenHelper.COLUMN_MINUTE,
			DatabaseOpenHelper.COLUMN_SECOND
	};
	
	List<Tick> ticks;

	public TracksDataSource(Context context) {
		dbHelper = DatabaseOpenHelper.getInstance(context.getApplicationContext());
	}

	public void open() throws SQLException {
		if (database == null || !database.isOpen()) {
  			Log.d("tickmate", "Opening database");
			database = dbHelper.getWritableDatabase();
		}
	}

	public void close() {
		Log.d("tickmate", "Closing database");
		dbHelper.close();
	}

	public void deleteTrack(Track track) {
		long id = track.getId();
		
		this.open();
		
		try {
			int rows = database.delete(DatabaseOpenHelper.TABLE_TRACKS,
				DatabaseOpenHelper.COLUMN_ID + " = " + id, null);
			if (rows > 0)
				System.out.println("Track deleted with id: " + id);
		} finally {
			if (this.database != null) {
				this.close();
			}
		}
	}

	public Track getTrack(int id) {		
		this.open();
		Cursor cursor = database.query(DatabaseOpenHelper.TABLE_TRACKS,
				allColumns, DatabaseOpenHelper.COLUMN_ID + " = " + id, null,
				null, null, null, null);
		cursor.moveToFirst();
		Track newTrack = cursorToTrack(cursor);
		cursor.close();
		return newTrack;
	}

	public List<Track> getTracks() {
		List<Track> tracks = new ArrayList<Track>();
		/*
		int trackOrder = 0;
		Log.v("XXX", "O" + track.getOrder() + " : " + trackOrder);
		if (track.getOrder() < trackOrder) {
			track.setOrder(trackOrder);
		}
		trackOrder = track.getOrder() + 1;
		*/
		this.open();

		Cursor cursor = database.query(DatabaseOpenHelper.TABLE_TRACKS,
				allColumns, null, null, null, null,
				"\"" + DatabaseOpenHelper.COLUMN_ORDER + "\" ASC", null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Track track = cursorToTrack(cursor);
			tracks.add(track);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		
		return tracks;
	}
	
	public List<Track> getActiveTracks() {
		List<Track> tracks = new ArrayList<Track>();
		this.open();

		Cursor cursor = database.query(DatabaseOpenHelper.TABLE_TRACKS,
				allColumns, DatabaseOpenHelper.COLUMN_ENABLED + " = 1", null, null, null,
				"\"" + DatabaseOpenHelper.COLUMN_ORDER + "\" ASC", null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Track track = cursorToTrack(cursor);
			tracks.add(track);
			cursor.moveToNext();
		}
		
		// Make sure to close the cursor
		cursor.close();
		
		return tracks;
	}

	public void retrieveTicks(Calendar startday, Calendar endday) {
		ticks = new ArrayList<Tick>();

		this.open();
		String[] args = { 
				Integer.toString(startday.get(Calendar.YEAR)),
				Integer.toString(startday.get(Calendar.YEAR)),
				Integer.toString(startday.get(Calendar.MONTH)),
				Integer.toString(endday.get(Calendar.YEAR)),
				Integer.toString(endday.get(Calendar.YEAR)),
				Integer.toString(endday.get(Calendar.MONTH))
		};
		Cursor cursor = database.query(DatabaseOpenHelper.TABLE_TICKS,
				allColumnsTicks, 
				"(" + DatabaseOpenHelper.COLUMN_YEAR + " > ? or (" + DatabaseOpenHelper.COLUMN_YEAR + " = ? and " + DatabaseOpenHelper.COLUMN_MONTH + " >= ?)) and " +
				"(" + DatabaseOpenHelper.COLUMN_YEAR + " < ? or (" + DatabaseOpenHelper.COLUMN_YEAR + " = ? and " + DatabaseOpenHelper.COLUMN_MONTH + " <= ?))",
				args, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Tick tick = cursorToTick(cursor);
			ticks.add(tick);
			cursor.moveToNext();
		}

		// Make sure to close the cursor
		cursor.close();
	}
	
	public Map<Integer, Map<Long, Integer> > retrieveTicksByMonths() {
		ticks = new ArrayList<Tick>();
		Map<Integer, Map<Long, Integer> > ret = new HashMap<Integer, Map<Long, Integer> >();

		Cursor cursor = database.query(DatabaseOpenHelper.TABLE_TICKS,
				new String[] {
    				"_track_id",
					"date(strftime('%y-%m-01', datetime(date, 'unixepoch'))) as month",
					"count(date) as count"
				}, null, null, 
				"_track_id, month", null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Integer track_id = cursor.getInt(0);
			Map<Long, Integer> map;
			if (!ret.containsKey(track_id)) {
				ret.put(track_id, new HashMap<Long, Integer>());
			}
			map = ret.get(track_id);
			map.put(cursor.getLong(1), cursor.getInt(2));
			cursor.moveToNext();
		}

		//Log.d("Tickmate", "loaded: track_id=" + cursor.getInt(0) + " @ " + cursor.getString(1) + " = " + cursor.getInt(2));
		cursor.close();
		
		return ret;
	}

	public List<Tick> getTicks(int track_id) {
		List<Tick> ticks = new ArrayList<Tick>();
		
		this.open();
		
		Cursor cursor = database.query(DatabaseOpenHelper.TABLE_TICKS,
				allColumnsTicks, DatabaseOpenHelper.COLUMN_TRACK_ID + " = " + Integer.toString(track_id),
				null, null, null, "year, month, day");

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Tick tick = cursorToTick(cursor);
			ticks.add(tick);
			cursor.moveToNext();
		}

		// Make sure to close the cursor
		cursor.close();
		
		return ticks;
	}
	
	public int getTickCountForDay(Track track, Calendar date) {
		
		int tickCount = 0;
		for (int i = 0; i < ticks.size(); i++) {
			Calendar d = ticks.get(i).date;
			if (ticks.get(i).track_id == track.getId() &&
				d.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
				d.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
				d.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)) {
				tickCount++;
			}
		}
	
		return tickCount;
	}
	
	public List<Tick> getTicksForDay(Track track, Calendar date) {
		List<Tick> ticks = new ArrayList<Tick>();
		
		this.open();
		
		String[] args = { Integer.toString(track.getId()),
				Integer.toString(date.get(Calendar.YEAR)),
				Integer.toString(date.get(Calendar.MONTH)),
				Integer.toString(date.get(Calendar.DAY_OF_MONTH)) };
		Cursor cursor = database.query(DatabaseOpenHelper.TABLE_TICKS,
				allColumnsTicks,
				DatabaseOpenHelper.COLUMN_TRACK_ID +"=? AND " +
				DatabaseOpenHelper.COLUMN_YEAR +"=? AND " + 
				DatabaseOpenHelper.COLUMN_MONTH +"=? AND " +
				DatabaseOpenHelper.COLUMN_DAY +"=?",
				args, null, null,
				DatabaseOpenHelper.COLUMN_HOUR + ", " +
				DatabaseOpenHelper.COLUMN_MINUTE + ", " +
				DatabaseOpenHelper.COLUMN_SECOND + " ASC");
		
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Tick tick = cursorToTick(cursor);
			ticks.add(tick);
			cursor.moveToNext();
		}

//		Log.d("Tickmate", ticks.size() + "ticks for day " + date.get(Calendar.YEAR) + " " + date.get(Calendar.MONTH) + " " + date.get(Calendar.DAY_OF_MONTH));
		// Make sure to close the cursor
		cursor.close();
		
		return ticks;
	}

	public boolean isTicked(Track t, Calendar date, boolean hasTimeInfo) {
		date.clear(Calendar.MILLISECOND);
		//Log.v("Tickmate", "checking for " + t.getId() + " and " + date.toString());
		return ticks.contains(new Tick(t.getId(), date));
	}
	
	private Track cursorToTrack(Cursor cursor) {
		Track track = new Track(cursor.getString(1), cursor.getString(3));
		track.setId(cursor.getInt(0));
		track.setEnabled(cursor.getInt(2) >= 1);
		track.setMultipleEntriesEnabled(cursor.getInt(5) >= 1);
		track.setIcon(cursor.getString(4));
		track.setOrder(cursor.getInt(6));
		return track;
	}
	
	private Tick cursorToTick(Cursor cursor) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, cursor.getInt(2));
		c.set(Calendar.MONTH, cursor.getInt(3));
		c.set(Calendar.DAY_OF_MONTH, cursor.getInt(4));
		c.set(Calendar.HOUR_OF_DAY, cursor.getInt(5));
		c.set(Calendar.MINUTE, cursor.getInt(6));
		c.set(Calendar.SECOND, cursor.getInt(7));
		Tick tick = new Tick(cursor.getInt(1), c);
		tick.tick_id = cursor.getInt(0);
		return tick;
	}

	public void storeTrack(Track t) {
		this.open();
		
		ContentValues values = new ContentValues();

		values.put(DatabaseOpenHelper.COLUMN_NAME, t.getName());
		values.put(DatabaseOpenHelper.COLUMN_ENABLED, t.isEnabled() ? 1 : 0);
		values.put(DatabaseOpenHelper.COLUMN_MULTIPLE_ENTRIES_PER_DAY, t.multipleEntriesEnabled() ? 1 : 0);
		values.put(DatabaseOpenHelper.COLUMN_DESCRIPTION, t.getDescription());
		values.put(DatabaseOpenHelper.COLUMN_ICON, t.getIcon());
		values.put("\"" + DatabaseOpenHelper.COLUMN_ORDER + "\"", t.getOrder());

		if (t.getId() > 0) {
			Log.d("Tickmate", "saving track id=" + t.getId());
			database.update(DatabaseOpenHelper.TABLE_TRACKS, values,
					DatabaseOpenHelper.COLUMN_ID + "=?",
					new String[] { Integer.toString(t.getId()) });
		} else {
			long t_id = database.insert(DatabaseOpenHelper.TABLE_TRACKS, null, values);
			t.setId((int)t_id);
			Log.d("Tickmate", "inserted track id=" + t.getId());
		}
		
		this.close();
	}

	public void setTick(Track track, Calendar date, boolean hasTimeInfo) {
		this.open();
		
		ContentValues values = new ContentValues();
		values.put(DatabaseOpenHelper.COLUMN_TRACK_ID, track.getId());
		values.put(DatabaseOpenHelper.COLUMN_YEAR,date.get(Calendar.YEAR));
		values.put(DatabaseOpenHelper.COLUMN_MONTH,date.get(Calendar.MONTH));
		values.put(DatabaseOpenHelper.COLUMN_DAY,date.get(Calendar.DAY_OF_MONTH));
		values.put(DatabaseOpenHelper.COLUMN_HOUR, date.get(Calendar.HOUR_OF_DAY));
		values.put(DatabaseOpenHelper.COLUMN_MINUTE, date.get(Calendar.MINUTE));
		values.put(DatabaseOpenHelper.COLUMN_SECOND, date.get(Calendar.SECOND));
		values.put(DatabaseOpenHelper.COLUMN_HAS_TIME_INFO, hasTimeInfo ? 1 : 0);
		Log.d("Tickmate", "insert at " + date.get(Calendar.YEAR) + " " + date.get(Calendar.MONTH) + " " + date.get(Calendar.DAY_OF_MONTH)
				+ " - " + date.get(Calendar.HOUR_OF_DAY) + ":" + date.get(Calendar.MINUTE) + ":" + date.get(Calendar.SECOND));
		database.insert(DatabaseOpenHelper.TABLE_TICKS, null, values);

		this.close();
	}

	public void removeTick(Track track, Calendar date) {
		this.open();
		
		String[] args = { Integer.toString(track.getId()),
				Integer.toString(date.get(Calendar.YEAR)),
				Integer.toString(date.get(Calendar.MONTH)),
				Integer.toString(date.get(Calendar.DAY_OF_MONTH)) };
		
		int affectedRows = database.delete(DatabaseOpenHelper.TABLE_TICKS,
				DatabaseOpenHelper.COLUMN_TRACK_ID +"=? AND " +
				DatabaseOpenHelper.COLUMN_YEAR+"=? AND " + 
				DatabaseOpenHelper.COLUMN_MONTH+"=? AND " + 
				DatabaseOpenHelper.COLUMN_DAY+"=?", args);
		Log.d("Tickmate", "delete " + affectedRows + "rows at " + date.get(Calendar.YEAR) + " " + date.get(Calendar.MONTH) + " " + date.get(Calendar.DAY_OF_MONTH));

		this.close();
	}
	
	public boolean removeLastTickOfDay(Track track, Calendar date) {
		this.open();
		List<Tick> ticks = this.getTicksForDay(track, date);
		
		if (ticks.size() == 0)
			return false;
		
		Tick tick = ticks.get(ticks.size()-1);
				
		String[] args = { Integer.toString(track.getId()),
				Integer.toString(tick.tick_id) };
		int affectedRows = database.delete(DatabaseOpenHelper.TABLE_TICKS,
				DatabaseOpenHelper.COLUMN_TRACK_ID +"=? AND " +
				DatabaseOpenHelper.COLUMN_ID +"=?", args);
		Log.d("Tickmate", "delete " + affectedRows + "rows at " +
				tick.date.get(Calendar.YEAR) + " " +
				tick.date.get(Calendar.MONTH) + " " +
				tick.date.get(Calendar.DAY_OF_MONTH) + " - " +
				tick.date.get(Calendar.HOUR_OF_DAY) + ":" +
				tick.date.get(Calendar.MINUTE) + ":" +
				tick.date.get(Calendar.SECOND));
		
		this.close();
		
		if (affectedRows > 0)
			return true;
		
		return false;
	}

	public int getTickCount(int track_id) {
		this.open();
		
		Cursor cursor = database.query(DatabaseOpenHelper.TABLE_TICKS,
				new String[] {
					"count("+DatabaseOpenHelper.COLUMN_TRACK_ID+") as count"
				}, DatabaseOpenHelper.COLUMN_TRACK_ID + " = " + Integer.toString(track_id), null, 
				DatabaseOpenHelper.COLUMN_TRACK_ID , null, null);
		cursor.moveToFirst();
		int c = 0;
		if (cursor.getCount() > 0) {
			c = cursor.getInt(0);
		}
		cursor.close();		
		return c;
	}
	
	public void resortTracks() {
		this.open();

		Cursor cursor = database.query(DatabaseOpenHelper.TABLE_TRACKS,
				allColumns, null, null, null, null,
				"\"" + DatabaseOpenHelper.COLUMN_ORDER + "\" ASC", null);
		
		int trackOrder = 0;
		cursor.moveToFirst();
		for (trackOrder = 0; !cursor.isAfterLast(); trackOrder += 10) {
			Track track = cursorToTrack(cursor);
			//Log.d("Tickmate", track.getName() + " is " + track.getOrder() + ", gets " + trackOrder);
			track.setOrder(trackOrder);
			storeTrack(track);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
	}

	public void moveTrack(Track t, int i) {
		this.open();
		resortTracks();
		
		Track t_updated = getTrack(t.getId());
		t_updated.setOrder(t_updated.getOrder() + i * 15);
		//Log.d("Tickmate", t_updated.getName() + " got " + t_updated.getOrder());

		storeTrack(t_updated);

		resortTracks();
	}
}