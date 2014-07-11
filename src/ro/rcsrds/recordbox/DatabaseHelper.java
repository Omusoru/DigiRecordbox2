package ro.rcsrds.recordbox;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	// Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "DigiRecordbox";
    // Recordings Table name
    private static final String TABLE_RECORDINGS = "recordings";
    // Recordings Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_DATE = "date";
    private static final String KEY_OWNER = "owner";
    private static final String KEY_FILENAME = "filename";
    private static final String KEY_DURATION = "duration";
    

	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_RECORDINGS_TABLE = "CREATE TABLE " + TABLE_RECORDINGS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_DESCRIPTION + " TEXT," + KEY_DATE + " TEXT,"
                + KEY_OWNER + " TEXT," + KEY_FILENAME + " TEXT,"
                + KEY_DURATION + " INTEGER" + ")";
        db.execSQL(CREATE_RECORDINGS_TABLE);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop old table
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORDINGS + ";");
		// and create a new one
        onCreate(db);
		
	}
	


}
