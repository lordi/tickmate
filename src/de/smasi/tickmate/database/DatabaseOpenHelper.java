package de.smasi.tickmate.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
	private static DatabaseOpenHelper sharedInstance;

    public static final String TABLE_TRACKS = "tracks";
    public static final String TABLE_TICKS = "ticks";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_ICON = "icon";
    public static final String COLUMN_ENABLED = "enabled";
    public static final String COLUMN_YEAR = "year";
    public static final String COLUMN_MONTH = "month";
    public static final String COLUMN_DAY = "day";
    public static final String COLUMN_HOUR = "hour";
    public static final String COLUMN_MINUTE = "minute";
    public static final String COLUMN_SECOND = "second";
    public static final String COLUMN_MODIFIED = "modified";
    public static final String COLUMN_TRACK_ID = "_track_id";
    public static final String COLUMN_MULTIPLE_ENTRIES_PER_DAY = "multiple_entries_per_day";

    private static final String DATABASE_NAME = "tickmate.db";
    private static final int DATABASE_VERSION = 10;
    
    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public static DatabaseOpenHelper getInstance(Context context) {
    	if (sharedInstance == null) {
			sharedInstance = new DatabaseOpenHelper(context.getApplicationContext());
		}
    	return sharedInstance;
    }
    
    // Database creation sql statement
    private static final String DATABASE_CREATE_TRACKS = 
    	"create table " + TABLE_TRACKS + "(" 
    	+ COLUMN_ID + " integer primary key autoincrement, "
        + COLUMN_NAME + " text not null, "
        + COLUMN_DESCRIPTION + " text not null, "
        + COLUMN_ICON + " text not null, "
        + COLUMN_ENABLED + " integer not null,"
        + COLUMN_MULTIPLE_ENTRIES_PER_DAY + " integer not null"
        + ");";
    private static final String DATABASE_CREATE_TICKS =
        "create table " + TABLE_TICKS + "("
        + COLUMN_ID + " integer primary key autoincrement, "
        + COLUMN_TRACK_ID + " integer,"
        + COLUMN_YEAR + " integer,"
        + COLUMN_MONTH + " integer,"
        + COLUMN_DAY + " integer,"
        + COLUMN_HOUR + " integer,"
        + COLUMN_MINUTE + " integer,"
        + COLUMN_SECOND + " integer"
        + ");";
    
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d("tickmate", "Creating database");
		db.execSQL(DATABASE_CREATE_TRACKS);
		db.execSQL(DATABASE_CREATE_TICKS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		/* TODO: Alter table depending on version number to support older versions */
		Log.d("tickmate", "Upgrading database");
		if (oldVersion == 9 && newVersion == 10) {
			Log.d("tickmate", "Adding columns from version 10");
			db.execSQL("ALTER TABLE " + TABLE_TRACKS + " ADD COLUMN " + COLUMN_MULTIPLE_ENTRIES_PER_DAY + " integer DEFAULT 0;");
			db.execSQL("ALTER TABLE " + TABLE_TICKS + " ADD COLUMN " + COLUMN_HOUR + " integer;");
			db.execSQL("ALTER TABLE " + TABLE_TICKS + " ADD COLUMN " + COLUMN_MINUTE + " integer;");
			db.execSQL("ALTER TABLE " + TABLE_TICKS + " ADD COLUMN " + COLUMN_SECOND + " integer;");
		} else {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACKS);
		    db.execSQL("DROP TABLE IF EXISTS " + TABLE_TICKS);
		    onCreate(db);
		}
	}
}
