package de.smasi.tickmate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.smasi.tickmate.Globals;
import de.smasi.tickmate.TickColor;
import de.smasi.tickmate.models.Group;
import de.smasi.tickmate.models.Tick;
import de.smasi.tickmate.models.Track;

public class DataSource {
    private static final String TAG = "DataSource";
    private static DataSource mInstance;

	public static final int DIRECTION_UP = -1;
	public static final int DIRECTION_DOWN = 1;

	private SQLiteDatabase database;
    private DatabaseOpenHelper dbHelper = DatabaseOpenHelper.getInstance(Globals.getInstance());

    private static final String[] allColumnsTracks = {
			DatabaseOpenHelper.COLUMN_ID,
			DatabaseOpenHelper.COLUMN_NAME,
			DatabaseOpenHelper.COLUMN_ENABLED,
			DatabaseOpenHelper.COLUMN_DESCRIPTION,
			DatabaseOpenHelper.COLUMN_ICON,
			DatabaseOpenHelper.COLUMN_MULTIPLE_ENTRIES_PER_DAY,
            DatabaseOpenHelper.COLUMN_COLOR,
			"\"" + DatabaseOpenHelper.COLUMN_ORDER + "\""
	};

    private static final String[] allColumnsTicks = {
			DatabaseOpenHelper.COLUMN_ID,
			DatabaseOpenHelper.COLUMN_TRACK_ID,
			DatabaseOpenHelper.COLUMN_YEAR,
			DatabaseOpenHelper.COLUMN_MONTH,
			DatabaseOpenHelper.COLUMN_DAY,
			DatabaseOpenHelper.COLUMN_HOUR,
			DatabaseOpenHelper.COLUMN_MINUTE,
			DatabaseOpenHelper.COLUMN_SECOND
	};

    private static final String[] allColumnsGroups = {
            DatabaseOpenHelper.COLUMN_ID,
            DatabaseOpenHelper.COLUMN_NAME,
            DatabaseOpenHelper.COLUMN_DESCRIPTION,
            "\"" + DatabaseOpenHelper.COLUMN_ORDER + "\""
    };
    // Column indices in the db, for allColumnsGroups.
    private static final int GROUP_ID_COLUMN = 0;
    private static final int GROUP_NAME_COLUMN = 1;
    private static final int GROUP_DESCRIPTION_COLUMN = 2;
    private static final int GROUP_ORDER_COLUMN = 3;

    private final static String[] allColumnsTracks2Groups = {
            DatabaseOpenHelper.COLUMN_ID,
            DatabaseOpenHelper.COLUMN_TRACK_ID,
            DatabaseOpenHelper.COLUMN_GROUP_ID
    };
    // Column indices in the db, for allColumnsTracks2Groups.
    private final static int T2G_TRACK_ID_COLUMN = 1;
    private final static int T2G_GROUP_ID_COLUMN = 2;

    private List<Tick> ticks;


    private DataSource(Context context) {
        dbHelper = DatabaseOpenHelper.getInstance(context.getApplicationContext());
    }

    public static DataSource getInstance() {
        if (mInstance == null) {
            mInstance = new DataSource(Globals.getInstance());
        }
        return mInstance;
    }

	private void open() throws SQLException {
		if (database == null || !database.isOpen()) {
  			Log.d("tickmate", "Opening database");
			database = dbHelper.getWritableDatabase();
		}
	}

	private void close() {
		Log.d("tickmate", "Closing database");
		dbHelper.close();
	}

	public void deleteTrack(Track track) {
		long id = track.getId();

		open();

		try {
			int rows = database.delete(DatabaseOpenHelper.TABLE_TRACKS,
				DatabaseOpenHelper.COLUMN_ID + " = " + id, null);
			if (rows > 0)
				System.out.println("Track deleted with id: " + id);
            rows = database.delete(DatabaseOpenHelper.TABLE_TRACK2GROUPS,
                    DatabaseOpenHelper.COLUMN_TRACK_ID + " = " + id, null);
            if (rows > 0)
                Log.d(TAG, String.format("number of track2group relations deleted: %d", rows));
        } finally {
            if (database != null) {
                close();
			}
		}
	}

	public Track getTrack(int id) {
        open();
		Cursor cursor = database.query(DatabaseOpenHelper.TABLE_TRACKS,
                allColumnsTracks, DatabaseOpenHelper.COLUMN_ID + " = " + id, null,
				null, null, null, null);
		cursor.moveToFirst();
//        Log.d(TAG, "getTrack called with id = " + id);
        Track newTrack = cursorToTrack(cursor);
		cursor.close();
		return newTrack;
	}

	/**
	 * Find and return a {@link Group}
	 * from the {@link DatabaseOpenHelper#TABLE_GROUPS} database table.
	 *
	 * @param id group id
	 * @return group
	 */
    public Group getGroup(int id) {
        open();
        Cursor cursor = database.query(DatabaseOpenHelper.TABLE_GROUPS,
                allColumnsGroups, DatabaseOpenHelper.COLUMN_ID + " = " + id, null,
                null, null, null, null);
        cursor.moveToFirst();

        Group newGroup = cursorToGroup(cursor);
        cursor.close();
        return newGroup;
    }

    public List<Track> getTracks() {
		List<Track> tracks = new ArrayList<>();

		open();

		Cursor cursor = database.query(DatabaseOpenHelper.TABLE_TRACKS,
                allColumnsTracks, null, null, null, null,
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

	/**
	 * Delete a given {@link Group}
     * from the {@link DatabaseOpenHelper#TABLE_GROUPS} database table.
	 *
	 * @param group group to delete
	 */
    public void deleteGroup(Group group) {
        open();

        long id = group.getId();
        try {
            int rows = database.delete(DatabaseOpenHelper.TABLE_GROUPS,
                    DatabaseOpenHelper.COLUMN_ID + " = " + id, null);
            if (rows > 0)
                Log.d(TAG, "deleteGroup deleted: " + id);
            rows = database.delete(DatabaseOpenHelper.TABLE_TRACK2GROUPS,
                    DatabaseOpenHelper.COLUMN_GROUP_ID + " = " + id, null);
            if (rows > 0)
                Log.d(TAG, String.format("number of track2group relations deleted: %d", rows));
        } finally {
            if (database != null) {
                close();
            }
        }
    }

	/**
	 * Retrieve all {@link Group}
     * from the {@link DatabaseOpenHelper#TABLE_GROUPS} database table.
     *
	 * @return a list of all Groups for this user
	 */
    public List<Group> getGroups() {
        List<Group> groups = new ArrayList<>();
        open();

        Cursor cursor = database.query(DatabaseOpenHelper.TABLE_GROUPS,
                allColumnsGroups, null, null, null, null,
                "\"" + DatabaseOpenHelper.COLUMN_ORDER + "\" ASC", null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Group g = cursorToGroup(cursor);
            groups.add(g);
            cursor.moveToNext();
//            Log.d(TAG, "in getGroups, adding group: " + g);
        }
        cursor.close(); // Make sure to close the cursor
        return groups;
    }

    /**
     * Retrieve all {@link Group} associated with track ID
     * from the {@link DatabaseOpenHelper#TABLE_TRACK2GROUPS} database table.
     *
     * @param id track id
     * @return a list of all Groups for this track id
     */
    public List<Group> getGroupsForTrack(int id) {
        // Get the groups linked to a particular track ID
        List<Group> groups = new ArrayList<>();
        open();

        List<String> columns = new LinkedList<>();
        for (String col : allColumnsGroups) {
            columns.add(String.format("%s.%s AS %s", DatabaseOpenHelper.TABLE_GROUPS, col, col));
        }

        StringBuilder query = new StringBuilder();
        query.append(String.format("SELECT %s FROM %s AS t2g ",
                TextUtils.join(", ", columns), DatabaseOpenHelper.TABLE_TRACK2GROUPS));
        query.append(String.format("JOIN %s ON %s.%s = t2g.%s ",
                DatabaseOpenHelper.TABLE_GROUPS, DatabaseOpenHelper.TABLE_GROUPS,
                DatabaseOpenHelper.COLUMN_ID, DatabaseOpenHelper.COLUMN_GROUP_ID));
        query.append(String.format("AND %s.%s IS NOT NULL AND t2g.%s = ? ",
                DatabaseOpenHelper.TABLE_GROUPS, DatabaseOpenHelper.COLUMN_ID,
                DatabaseOpenHelper.COLUMN_TRACK_ID));

        //Log.d("tickmate", query.toString());
        Cursor cursor = database.rawQuery(query.toString(), new String[]{String.valueOf(id)});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Group g = cursorToGroup(cursor);
            groups.add(g);
            cursor.moveToNext();
        }

        // Make sure to close the cursor
        cursor.close();

        return groups;
    }

	/**
	 * Retrieve all {@link Group} ids associated with track id
	 *
	 * @param id Track id
	 * @return a list of Group ids
	 */
    private List<Integer> getGroupIdsForTrack(long id) {
        // get groupIds for track
        List<Integer> groupIds = new ArrayList<>();
        open();

        Cursor cursor = database.query(DatabaseOpenHelper.TABLE_TRACK2GROUPS,
                allColumnsTracks2Groups, DatabaseOpenHelper.COLUMN_TRACK_ID + " = " + id, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int groupId = cursor.getInt(T2G_GROUP_ID_COLUMN);
            cursor.moveToNext();
            groupIds.add(groupId);
        }

        // Make sure to close the cursor
        cursor.close();
        return groupIds;
    }

	/**
	 * Retrieve all {@link Track} ids associated with Group id
	 *
	 * @param groupId Group id
	 * @return a list of Track ids
	 */
    private List<Integer> getTrackIdsForGroup(int groupId) {
        open();
        Cursor cursor = database.rawQuery(
            "SELECT " + DatabaseOpenHelper.TABLE_TRACKS + "." + DatabaseOpenHelper.COLUMN_ID +
                " FROM " + DatabaseOpenHelper.TABLE_TRACK2GROUPS +
                " JOIN " + DatabaseOpenHelper.TABLE_TRACKS +
                " ON " + DatabaseOpenHelper.TABLE_TRACK2GROUPS + "." + DatabaseOpenHelper.COLUMN_TRACK_ID +
                    " = " + DatabaseOpenHelper.TABLE_TRACKS + "." + DatabaseOpenHelper.COLUMN_ID +
                " WHERE " + DatabaseOpenHelper.TABLE_TRACK2GROUPS + "." + DatabaseOpenHelper.COLUMN_GROUP_ID +
                    " = ?" +
             " ORDER BY " + DatabaseOpenHelper.TABLE_TRACKS + ".\"" + DatabaseOpenHelper.COLUMN_ORDER + "\"",
                new String[] { String.format("%d", groupId) }
        );

        cursor.moveToFirst();
        List<Integer> ids = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            int trackId = cursor.getInt(0);
            //Log.d(TAG, "Getting track #" + trackId);
            ids.add(Integer.valueOf(trackId));
            cursor.moveToNext();
        }

        // Make sure to close the cursor
        cursor.close();

        return ids;
    }

	/**
	 * Retrieve all {@link Track} objects associated with group id
	 *
	 * @param groupId group id
	 * @return a list of Track objects
	 */
    public List<Track> getTracksForGroup(int groupId) {
        // get tracks for group
//        Log.d(TAG, "getTracksForGroup(" + groupId+")");
        List<Integer> ids = getTrackIdsForGroup(groupId);
//        Log.d(TAG, "   Found tracks IDs: " + ids);


        List<Track> tracks = new ArrayList<>();
        for (Integer trackId: ids) {
            tracks.add(getTrack(trackId));
        }
//        Log.d(TAG, "   Found tracks: " + tracks);
        return tracks;
    }

	/**
	 * Retrieve all {@link Group} objects associated with track id
	 *
	 * @param track Track object
	 * @return a list of Group objects
	 */
    public List<Group> getGroupsForTrack(Track track) {
        return getGroupsForTrack(track.getId());
    }

	/**
	 * Store this {@link Group} object
	 * to the {@link DatabaseOpenHelper#TABLE_GROUPS} database table.
	 *
	 * @param group Group object
	 */
    public void storeGroup(Group group) {
        open();

        ContentValues values = new ContentValues();

        values.put(DatabaseOpenHelper.COLUMN_NAME, group.getName());
        values.put(DatabaseOpenHelper.COLUMN_DESCRIPTION, group.getDescription());
        values.put("\"" + DatabaseOpenHelper.COLUMN_ORDER +"\"", group.getOrder());

        if (group.getId() > 0) {
            database.update(DatabaseOpenHelper.TABLE_GROUPS, values,
                    DatabaseOpenHelper.COLUMN_ID + "=?",
                    new String[]{Integer.toString(group.getId())});
//            Log.d(TAG, "saving group id=" + group.getId());
        } else {
            long t_id = database.insert(DatabaseOpenHelper.TABLE_GROUPS, null, values);
            group.setId((int) t_id);
//            Log.d("Tickmate", "inserted group id=" + group.getId());
        }

        close();
    }


    public List<Track> getActiveTracks() {
        List<Track> tracks = new ArrayList<>();
        open();

		Cursor cursor = database.query(DatabaseOpenHelper.TABLE_TRACKS,
                allColumnsTracks, DatabaseOpenHelper.COLUMN_ENABLED + " = 1", null, null, null,
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

        open();
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

		//Log.d("Tickmate", "loaded: track_id=" + cursor.getColorValue(0) + " @ " + cursor.getString(1) + " = " + cursor.getColorValue(2));
		cursor.close();

		return ret;
	}

	public List<Tick> getTicks(int track_id) {
		List<Tick> ticks = new ArrayList<Tick>();

        open();

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

        open();

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

	/**
	 * Resolve the Cursor into a {@link Track} object.
	 *
	 * @param cursor Track cursor
	 * @return Track
	 */
	private Track cursorToTrack(Cursor cursor) {
		Track track = new Track(cursor.getString(1), cursor.getString(3));
		track.setId(cursor.getInt(0));
		track.setEnabled(cursor.getInt(2) >= 1);
		track.setMultipleEntriesEnabled(cursor.getInt(5) >= 1);
		track.setIcon(cursor.getString(4));
        track.setTickColor(new TickColor(cursor.getInt(6)));
        track.setOrder(cursor.getInt(7));
        return track;
	}

	/**
	 * Resolve the Cursor into a {@link Group} object.
	 *
	 * @param cursor Group cursor
	 * @return Group
	 */
    private Group cursorToGroup(Cursor cursor) {
        Group group = new Group(cursor.getString(GROUP_NAME_COLUMN),
                cursor.getString(GROUP_DESCRIPTION_COLUMN));
        group.setId(cursor.getInt(GROUP_ID_COLUMN));
        group.setOrder(cursor.getInt(GROUP_ORDER_COLUMN));
        return group;
    }

	private Tick cursorToTick(Cursor cursor) {
		Calendar c = Calendar.getInstance();
		c.set(cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), 0, 0, 0 );
		c.set(Calendar.MILLISECOND, 0);
		Tick tick = new Tick(cursor.getInt(1), c);	// implicitly sets tick.hasTimeInfo to false
		tick.tick_id = cursor.getInt(0);
		return tick;
	}

	public void storeTrack(Track t) {
        open();

		ContentValues values = new ContentValues();

		values.put(DatabaseOpenHelper.COLUMN_NAME, t.getName());
		values.put(DatabaseOpenHelper.COLUMN_ENABLED, t.isEnabled() ? 1 : 0);
		values.put(DatabaseOpenHelper.COLUMN_MULTIPLE_ENTRIES_PER_DAY, t.multipleEntriesEnabled() ? 1 : 0);
		values.put(DatabaseOpenHelper.COLUMN_DESCRIPTION, t.getDescription());
		values.put(DatabaseOpenHelper.COLUMN_ICON, t.getIcon());
        Log.e(TAG, "storing color: " + Integer.toHexString(t.getTickColor().getColorValue()));
                values.put(DatabaseOpenHelper.COLUMN_COLOR, t.getTickColor().getColorValue());
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

        close();
	}

	public void setTick(Track track, Calendar date, boolean hasTimeInfo) {
		open();

		ContentValues values = new ContentValues();
		values.put(DatabaseOpenHelper.COLUMN_TRACK_ID, track.getId());
		values.put(DatabaseOpenHelper.COLUMN_YEAR,date.get(Calendar.YEAR));
		values.put(DatabaseOpenHelper.COLUMN_MONTH,date.get(Calendar.MONTH));
		values.put(DatabaseOpenHelper.COLUMN_DAY, date.get(Calendar.DAY_OF_MONTH));
		values.put(DatabaseOpenHelper.COLUMN_HOUR, date.get(Calendar.HOUR_OF_DAY));
		values.put(DatabaseOpenHelper.COLUMN_MINUTE, date.get(Calendar.MINUTE));
		values.put(DatabaseOpenHelper.COLUMN_SECOND, date.get(Calendar.SECOND));
		values.put(DatabaseOpenHelper.COLUMN_HAS_TIME_INFO, hasTimeInfo ? 1 : 0);
		Log.d("Tickmate", "Inserting tick at " + date.get(Calendar.YEAR) + " " + date.get(Calendar.MONTH) + " " + date.get(Calendar.DAY_OF_MONTH)
                + " - " + date.get(Calendar.HOUR_OF_DAY) + ":" + date.get(Calendar.MINUTE) + ":" + date.get(Calendar.SECOND));
		database.insert(DatabaseOpenHelper.TABLE_TICKS, null, values);

        close();
	}

	public void removeTick(Track track, Calendar date) {
        open();

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

        close();
	}

	public boolean removeLastTickOfDay(Track track, Calendar date) {
        open();
        List<Tick> ticks = getTicksForDay(track, date);

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

        close();

		if (affectedRows > 0)
			return true;

		return false;
	}

	public int getTickCount(int track_id) {
        open();

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


    public void orderGroups() {
        open();

        Cursor cursor = database.query(DatabaseOpenHelper.TABLE_GROUPS,
                allColumnsGroups, null, null, null, null,
                "\"" + DatabaseOpenHelper.COLUMN_ORDER + "\" ASC", null);

        cursor.moveToFirst();
        for (int groupOrder = 0; !cursor.isAfterLast(); groupOrder += 10) {
            Group group = cursorToGroup(cursor);
            //Log.d("Tickmate", group.getName() + " is " + group.getOrder() + ", gets " + trackOrder);
            group.setOrder(groupOrder);
            storeGroup(group);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
    }

	public void orderTracks() {
		open();

		Cursor cursor = database.query(DatabaseOpenHelper.TABLE_TRACKS,
                allColumnsTracks, null, null, null, null,
                "\"" + DatabaseOpenHelper.COLUMN_ORDER + "\" ASC", null);

		cursor.moveToFirst();
		for (int trackOrder = 0; !cursor.isAfterLast(); trackOrder += 10) {
			Track track = cursorToTrack(cursor);
			//Log.d("Tickmate", track.getName() + " is " + track.getOrder() + ", gets " + trackOrder);
			track.setOrder(trackOrder);
			storeTrack(track);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
	}

	public void moveTrack(Track t, int dir) {
		open();
		orderTracks();

		Track t_updated = getTrack(t.getId());
		t_updated.setOrder(t_updated.getOrder() + dir * 15);
		//Log.d("Tickmate", t_updated.getName() + " got " + t_updated.getOrder());

		storeTrack(t_updated);

		orderTracks();
	}

	/**
	 * Store the fact that this one Track and set of Groups are associated with each other
     *   Assumes that all groups IDs that are *not* included in newGroupIds should be unlinked
     *   from the track trackId
	 *
	 * @param trackId a complete list of the track IDs to link to the given group ID
	 * @param newGroupIds group id
	 */
    public void linkOneTrackManyGroups(int trackId, List<Integer> newGroupIds) {
        List<Integer> currentGroupIds = getGroupIdsForTrack(trackId);
                Log.d(TAG, "Updating track (" + trackId + ") with new group IDS(" + newGroupIds + "), previously were: " + printGroupIdsForTrack(trackId));

        // If the ID is currently in the table, but not in newGroupIds, then delete it
        for (Integer gId : currentGroupIds) {
            if (!newGroupIds.contains(gId)) {
                unlinkOneTrackOneGroup(trackId, gId);
            }
        }

        // If the ID is in newGroupIds, but not currently in the table, then add it
        for (Integer gId : newGroupIds) {
            if (!currentGroupIds.contains(gId)) {
                linkOneTrackOneGroup(trackId, gId);
            }
        }
        Log.d(TAG, "Check that new group ids were set correctly for (" + trackId + "), using (" + newGroupIds + ").  After update, they are: " + printGroupIdsForTrack(trackId));
    }


	/**
	 * Remove the fact that this Track and Group were associated with each other
	 *
	 * @param trackId track ids
	 * @param groupId group id
	 */
    private void unlinkOneTrackOneGroup(int trackId, Integer groupId) {
        open();
        database.delete(DatabaseOpenHelper.TABLE_TRACK2GROUPS,
                DatabaseOpenHelper.COLUMN_TRACK_ID + " = " + trackId
                        + " AND " + DatabaseOpenHelper.COLUMN_GROUP_ID + " = " + groupId, null);
        close();
        //        Log.d(TAG, "called unlinkOneTrackOneGroup(" + trackId + ", " + groupId + "). ");
    }

    /**
     * Store the fact that this one Group and set of Tracks are associated with each other
     *   Assumes that all track IDs that are *not* included in newTrackIds should be unlinked
     *   from the group groupId
     *
     * @param groupId a complete list of the track IDs to link to the given group ID
     * @param newTrackIds a complete list of the
     */
        public void linkManyTracksOneGroup(List<Integer> newTrackIds, int groupId) {
//        Log.d(TAG, "Updating group (" + groupId + ") with new track IDS(" + newTrackIds + "), previously were: " + printTrackIdsForGroup(groupId));
            List<Integer> currentTrackIds = getTrackIdsForGroup(groupId);

        // If the ID is currently in the table, but not in newTrackIds, then delete it
        for (Integer tId : currentTrackIds) {
            if (!newTrackIds.contains(tId)) {
                unlinkOneTrackOneGroup(tId, groupId);
            }
        }

        // If the ID is in newTrackIds, but not currently in the table, then add it
        for (Integer tId : newTrackIds) {
            if (!currentTrackIds.contains(tId)) {
                linkOneTrackOneGroup(tId, groupId);
            }
        }
//        Log.d(TAG, "Check that new track ids were set correctly for (" + groupId + "), using (" + newTrackIds + ").  After update, they are: " + printTrackIdsForGroup(groupId));
    }




    /**
	 * Store the fact that this Track and Group are associated with each other
	 *
	 * @param trackId track id
	 * @param groupId group id
	 */
    public void linkOneTrackOneGroup(long trackId, long groupId) {
        open();

        // Check whether the linkage already exists.  Query the database for it:
        Cursor cursor = database.query(DatabaseOpenHelper.TABLE_TRACK2GROUPS, allColumnsTracks2Groups,
                DatabaseOpenHelper.COLUMN_TRACK_ID + " = " + trackId + " AND "
                        + DatabaseOpenHelper.COLUMN_GROUP_ID + " = " + groupId,
                null, null, null, null, null);

        // If moveToFirst returns true, then the link already exists and there is nothing to do.
        if (cursor.moveToFirst()) {
           return;  // Link already exists, so exit this method
        }

        // The link doesn't already exist, so create it:
        ContentValues values = new ContentValues();
        values.put(DatabaseOpenHelper.COLUMN_TRACK_ID, trackId);
        values.put(DatabaseOpenHelper.COLUMN_GROUP_ID, groupId);
        long t2g_id = database.insert(DatabaseOpenHelper.TABLE_TRACK2GROUPS, null, values);

        cursor.close();
        close();

//        Log.d("Tickmate", "inserted t2g id=" + t2g_id + ", to associate track (" + trackId + ") and group (" + groupId + ")");
    }

    // Temporary method for debug only
    private String printGroupIdsForTrack(long trackId) {
        List<Integer> groupIds = getGroupIdsForTrack(trackId);
//        Log.d(TAG, "in printGroupIdsForTrack - groupIds = " + groupIds + ", " + TextUtils.join("\n", groupIds));
        return TextUtils.join(",", groupIds);
    }

    public void moveGroup(Group g, int direction) {
        open();
        orderGroups();

        Group updatedGroup = getGroup(g.getId());
        updatedGroup.setOrder(updatedGroup.getOrder() + direction * 15); // Mimicking moveTrack,
        // Though I don't know why we do: * 15
        storeGroup(updatedGroup);
        orderGroups();
        //Log.d("Tickmate", t_updated.getName() + " got " + t_updated.getOrder());

    }
}
