package de.smasi.tickmate.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
    private static DatabaseOpenHelper sharedInstance;
	private Context context;
    private static final String TAG = "DatabaseOpenHelper";

    /* Table names */
    public static final String TABLE_TRACKS = "tracks";
    public static final String TABLE_TICKS = "ticks";
    public static final String TABLE_GROUPS = "groups";
    public static final String TABLE_TRACK2GROUPS = "track2groups";

    /* Field names */
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_ICON = "icon";
    public static final String COLUMN_ENABLED = "enabled";
    public static final String COLUMN_ORDER = "order";
    public static final String COLUMN_YEAR = "year";
    public static final String COLUMN_MONTH = "month";
    public static final String COLUMN_DAY = "day";
    public static final String COLUMN_HOUR = "hour";
    public static final String COLUMN_MINUTE = "minute";
    public static final String COLUMN_SECOND = "second";
    public static final String COLUMN_TRACK_ID = "_track_id";
    public static final String COLUMN_GROUP_ID = "_group_id";
    public static final String COLUMN_MULTIPLE_ENTRIES_PER_DAY = "multiple_entries_per_day";
    public static final String COLUMN_HAS_TIME_INFO = "has_time_info";
    public static final String COLUMN_COLOR = "color";

    private static final String DATABASE_NAME = "tickmate.db";
    private static final int DATABASE_VERSION = 14;
    
    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
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
        + COLUMN_MULTIPLE_ENTRIES_PER_DAY + " integer DEFAULT 0,"
        + COLUMN_COLOR + " integer DEFAULT 0,"
        + "\"" + COLUMN_ORDER + "\" integer DEFAULT -1"
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
        + COLUMN_SECOND + " integer,"
        + COLUMN_HAS_TIME_INFO + " integer DEFAULT 0"
        + ");";
    private static final String DATABASE_CREATE_GROUPS =
            "create table " + TABLE_GROUPS + "("
                    + COLUMN_ID + " integer primary key autoincrement, "
                    + COLUMN_NAME + " text not null, "
                    + COLUMN_DESCRIPTION + " text not null, "
                    + "\"" + COLUMN_ORDER + "\" integer DEFAULT -1"
                    + ");";
    private static final String DATABASE_CREATE_TRACK2GROUPS =
			"create table " + TABLE_TRACK2GROUPS + "("
					+ COLUMN_ID + " integer primary key autoincrement, "
					+ COLUMN_TRACK_ID + " integer not null, "
                    + COLUMN_GROUP_ID + " integer not null "
                    + ");";

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d("tickmate", "Creating database");
		db.execSQL(DATABASE_CREATE_TRACKS);
		db.execSQL(DATABASE_CREATE_TICKS);
        db.execSQL(DATABASE_CREATE_GROUPS);
        db.execSQL(DATABASE_CREATE_TRACK2GROUPS);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d("tickmate", "Upgrading database");
        if (oldVersion >= 9 && newVersion <= DATABASE_VERSION) {
			if (oldVersion <= 9) {
				Log.d("tickmate", "Migrating database to version 10");
				db.execSQL("ALTER TABLE " + TABLE_TRACKS + " ADD COLUMN \"" + COLUMN_MULTIPLE_ENTRIES_PER_DAY + "\" integer DEFAULT 0;");
				
				db.execSQL("ALTER TABLE " + TABLE_TICKS + " ADD COLUMN \"" + COLUMN_HOUR + "\" integer;");
				db.execSQL("ALTER TABLE " + TABLE_TICKS + " ADD COLUMN \"" + COLUMN_MINUTE + "\" integer;");
				db.execSQL("ALTER TABLE " + TABLE_TICKS + " ADD COLUMN \"" + COLUMN_SECOND + "\" integer;");
				db.execSQL("ALTER TABLE " + TABLE_TICKS + " ADD COLUMN \"" + COLUMN_HAS_TIME_INFO + "\" integer DEFAULT 0;");
			}
			if (oldVersion <= 10) {
				Log.d("tickmate", "Migrating database to version 11");
				db.execSQL("ALTER TABLE " + TABLE_TRACKS + " ADD COLUMN \"" + COLUMN_ORDER + "\" integer DEFAULT -1;");
			}
			if (oldVersion == 11) {
				try {
					db.execSQL("ALTER TABLE " + TABLE_TRACKS + " ADD COLUMN \"" + COLUMN_ORDER + "\" integer DEFAULT -1;");
				} catch (SQLException e) {
					Log.d("tickmate", "Ignoring SQL error: " + e.toString());
				}
			}
			if (oldVersion <= 12) {
				db.execSQL(DATABASE_CREATE_GROUPS);
				db.execSQL(DATABASE_CREATE_TRACK2GROUPS);
			}
			if (oldVersion <= 13) {
				Log.d("tickmate", "Migrating database to version 14");
				db.execSQL("ALTER TABLE " + TABLE_TRACKS + " ADD COLUMN \"" + COLUMN_COLOR + "\" integer DEFAULT " + Integer.toString(0x4ea6e0));
			}
		} else {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACKS);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_TICKS);
			onCreate(db);
		}
	}

	public String getDatabasePath() {
		String db_path = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
		Log.v("tickmate", "internal database path: " + db_path);
		return db_path;
	}

	public String getExternalDatabasePath(String name) throws IOException {
		String db_path = new File(getExternalDatabaseFolder(), name).getAbsolutePath();
		Log.v("tickmate", "external database path: " + db_path);
		return db_path;
	}

	/**
	 * Copies internal database to outputStream.
	 */
	public boolean exportDatabase(OutputStream outputStream) throws IOException {
		// Close the SQLiteOpenHelper so it will commit the created empty
		// database to internal storage.
		close();

		File myDb = new File(getDatabasePath());
		FileInputStream inFile = new FileInputStream(myDb);
		FileUtils.copyFile(inFile, outputStream);

		return true;
	}

	/**
	 * Copies the database file at the specified location over the current
	 * internal application database.
	 */
	public boolean importDatabase(InputStream inputStream) throws IOException {
		// Close the SQLiteOpenHelper so it will commit the created empty
		// database to internal storage.
		close();

		File myDb = new File(getDatabasePath());
		FileUtils.copyFile(inputStream, new FileOutputStream(myDb));
		getWritableDatabase().close();
		return true;
	}

	public String[] getExternalDatabaseNames() {
		
		 File ext_dir;
		try {
			ext_dir = getExternalDatabaseFolder();
		} catch (IOException e) {
			return new String[0];
		}
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                File sel = new File(dir, filename);
                return filename.endsWith(".db") && !sel.isDirectory();
            }
        };
        return ext_dir.list(filter);

	}

	public File getExternalDatabaseFolder() throws IOException {
    	
		// On Android 4.4, it is not possible to write on the external SD card
		// So fall back to getExternalFilesDir
		// We choose the first writable folder from the following list:
		File[] ext_dirs = {
            new File(FileUtils.getRemovableStorageDirectory(), "Tickmate"),
            new File(FileUtils.getRemovableStorageDirectory(), "Android/data/" + context.getPackageName()),
            this.context.getExternalFilesDir("backup"),
            this.context.getExternalFilesDir(null),
            this.context.getFilesDir()
		};
		
		boolean valid = false;
		File ext_dir = null;
		for (int i = 0; !valid && i < ext_dirs.length; i++) {
			ext_dir = ext_dirs[i];
			if (ext_dir == null) {
				Log.v(TAG, "path is null");
				continue;
			}
			valid = true;
			if (!ext_dir.exists()) {
				if (ext_dir.mkdirs() == false) {
					valid = false;
					Log.v(TAG, ext_dir.getAbsolutePath() + ": no mkdirs");
					continue;
				}
			}		
			if (valid && !ext_dir.canWrite()) { // check writing permissions
				valid = false;
				Log.v(TAG, ext_dir.getAbsolutePath() + ": no write");
				continue;
			}	
		}
		if (!valid) {
			throw new IOException("Could not find external storage directory.");
		}
		return ext_dir;
	}
}
