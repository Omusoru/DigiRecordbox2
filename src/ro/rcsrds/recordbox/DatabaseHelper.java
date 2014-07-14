package ro.rcsrds.recordbox;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	// Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "DigiRecordbox";
    // Recordings Table name
    private static final String TABLE_RECORDINGS = "recordings";
    // Recordings Table Column names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_DATE = "date";
    private static final String KEY_OWNER = "owner";
    private static final String KEY_FILENAME = "filename";
    private static final String KEY_DURATION = "duration";
    private static final String KEY_ON_LOCAL = "on_local";
    private static final String KEY_ON_CLOUD = "on_cloud";
    // Recordings Table Columns list
    private static final String[] COLUMNS_RECORDINGS = {
    	KEY_ID,KEY_NAME,KEY_DESCRIPTION,KEY_DATE,
    	KEY_OWNER,KEY_FILENAME,KEY_DURATION,KEY_ON_LOCAL,KEY_ON_CLOUD
    };
    

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_RECORDINGS_TABLE = "CREATE TABLE " + TABLE_RECORDINGS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_DESCRIPTION + " TEXT," + KEY_DATE + " TEXT,"
                + KEY_OWNER + " TEXT," + KEY_FILENAME + " TEXT,"
                + KEY_DURATION + " TEXT," + KEY_ON_LOCAL + " BOOLEAN,"
                + KEY_ON_CLOUD + " BOOLEAN" + ")";
        db.execSQL(CREATE_RECORDINGS_TABLE);        
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop old table
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORDINGS + ";");
		// and create a new one
        onCreate(db);
		
	}
	
	public int insertRecording(Recording recording) {
		Log.d("Database", "Insert: "+recording.getName());
		
		// get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();
		
		// create ContentValues to add key "column"/value
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, recording.getName());
		values.put(KEY_DESCRIPTION, recording.getDescription());
		values.put(KEY_DATE, recording.getDate());
		values.put(KEY_OWNER, recording.getOwner());		
		values.put(KEY_FILENAME, recording.getFilename());
		values.put(KEY_DURATION, recording.getDuration());
		values.put(KEY_ON_LOCAL, recording.isOnLocal() ? 1 : 0);
		values.put(KEY_ON_CLOUD, recording.isOnCloud() ? 1 : 0);
		
		// insert
		int lastRow = (int)db.insert(TABLE_RECORDINGS, null, values);
		
		// close
		db.close();		
		
		return lastRow;
		
	}
	
	public Recording getRecording(int id) {
		
		// get reference to readable DB
	    SQLiteDatabase db = this.getReadableDatabase();
	    
	    // build query
	    Cursor cursor = db.query(TABLE_RECORDINGS, // a. table
		            COLUMNS_RECORDINGS, // b. column names
		            KEY_ID + "=?", // c. selections
		            new String[] { String.valueOf(id) }, // d. selections args
		            null, // e. group by
		            null, // f. having
		            null, // g. order by
		            null); // h. limit
		
	    // if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();
        
        // build recording object
        Recording recording = new Recording();
        recording.setId(Integer.parseInt(cursor.getString(0)));
        recording.setName(cursor.getString(1));
        recording.setDescription(cursor.getString(2));
        recording.setDate(cursor.getString(3));
        recording.setOwner(cursor.getString(4));
        recording.setFilename(cursor.getString(5));
        recording.setDuration(cursor.getString(6));
        recording.setOnLocal(Integer.parseInt(cursor.getString(7)) == 1 ? true : false);
        recording.setOnCloud(Integer.parseInt(cursor.getString(8)) == 1 ? true : false);
        
        Log.d("Database","getRecording("+id+"): "+recording.getName());
	   
		return recording;
	}
	
	public List<Recording> getAllRecordings() {
		
		List<Recording> recordings = new ArrayList<Recording>();
		
		// build the query
        String query = "SELECT  * FROM " + TABLE_RECORDINGS;
        
        // get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        
        // go over each row, build recording and add it to the list
        Recording recording = null;
        if(cursor.moveToFirst()) {
        	do {
        		recording = new Recording();
        		recording.setId(Integer.parseInt(cursor.getString(0)));
                recording.setName(cursor.getString(1));
                recording.setDescription(cursor.getString(2));
                recording.setDate(cursor.getString(3));
                recording.setOwner(cursor.getString(4));
                recording.setFilename(cursor.getString(5));
                recording.setDuration(cursor.getString(6));
                recording.setOnLocal(Integer.parseInt(cursor.getString(7)) == 1 ? true : false);
                recording.setOnCloud(Integer.parseInt(cursor.getString(8)) == 1 ? true : false);
                
                recordings.add(recording);
        	} while(cursor.moveToNext());
        }
		
        // log
        for (Recording recordingx : recordings) {
        	Log.d("Database","getAllRecordings: "+recordingx.getName());
        }
		
		
		return recordings;
		
	}
	
	public int updateRecording(Recording recording) {
		
		// get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        
        // create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, recording.getName());
		values.put(KEY_DESCRIPTION, recording.getDescription());
		values.put(KEY_DATE, recording.getDate());
		values.put(KEY_OWNER, recording.getOwner());		
		values.put(KEY_FILENAME, recording.getFilename());
		values.put(KEY_DURATION, recording.getDuration());
		values.put(KEY_ON_LOCAL, recording.isOnLocal() ? 1 : 0);
		values.put(KEY_ON_CLOUD, recording.isOnCloud() ? 1 : 0);
		
		// updating row
        int i = db.update(TABLE_RECORDINGS, //table
                values, // column/value
                KEY_ID+" = ?", // selections
                new String[] { String.valueOf(recording.getId()) }); //selection args
		
        // close 
        db.close();
        
        Log.d("Database","updateDatabase: "+recording.getName());
        
        return i;
        
	}
	
	public void deleteRecording(Recording recording) {
		
		// get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        
        // delete
        db.delete(TABLE_RECORDINGS,
        		KEY_ID+" =?",
        		new String[] { String.valueOf(recording.getId()) });
        
        // close
        db.close();
        
        Log.d("Database","deleteRecording: "+recording.getName());
		
	}
	
	


}
