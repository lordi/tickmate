package de.smasi.tickmate.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
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
    public static final String COLUMN_MODIFIED = "modified";
    public static final String COLUMN_TRACK_ID = "_track_id";

    private static final String DATABASE_NAME = "tickmate.db";
    private static final int DATABASE_VERSION = 9;
    
    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
    
    // Database creation sql statement
    private static final String DATABASE_CREATE_TRACKS = 
    	"create table " + TABLE_TRACKS + "(" 
    	+ COLUMN_ID + " integer primary key autoincrement, "
        + COLUMN_NAME + " text not null, "
        + COLUMN_DESCRIPTION + " text not null, "
        + COLUMN_ICON + " text not null, "
        + COLUMN_ENABLED + " integer not null "
        + ");";
    private static final String DATABASE_CREATE_TICKS =
        "create table " + TABLE_TICKS + "("
        + COLUMN_ID + " integer primary key autoincrement, "
        + COLUMN_TRACK_ID + " integer,"
        + COLUMN_YEAR + " integer,"
        + COLUMN_MONTH + " integer,"
        + COLUMN_DAY + " integer"
        + ");";
    
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE_TRACKS);
		db.execSQL(DATABASE_CREATE_TICKS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACKS);
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_TICKS);
	    onCreate(db);
	}
}
