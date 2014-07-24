package ro.rcsrds.recordbox;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
public class EditRecordingActivity extends ActionBarActivity {
	
	private EditText etName;
	private EditText etDescription;
	private Button btnSave;
	private Button btnSavePlay;
	private String filename;
	private String owner;
	private String duration;
	private int lastRecordingId;
	private boolean newRecording;
	public static final String PREFS_NAME = "Authentication";
	private Recording recording;
	private FileManager fm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editrecording);
		
		etName = (EditText) findViewById(R.id.et_name);
		etDescription = (EditText) findViewById(R.id.et_description);
		btnSave = (Button) findViewById(R.id.btn_save);
		btnSave.setOnClickListener(new ButtonOnClickListener());
		btnSavePlay = (Button) findViewById(R.id.btn_save_play);
		btnSavePlay.setOnClickListener(new ButtonOnClickListener());
		
		fm = new FileManager(this);
		new Thread(new Runnable() {
			 public void run() {		    	
			    	if(isNetworkConnected()) {
			    		fm.connectToCloud();
			    	}
		   }
		}).start();
		
		
		
		if(getIntent().getExtras().getBoolean("new")) {
			newRecording = true;
			// get filename parameter passed from main activity
			filename = getIntent().getExtras().getString("filename");
			duration = getIntent().getExtras().getString("duration");
		} else {			
			newRecording = false;
			// get recording id passed from recording list
			lastRecordingId = getIntent().getExtras().getInt("id");	
			getRecordingInfo();
		}
		
		// get logged in username
		SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
		owner = preferences.getString("username", "no owner");
	}
	
	@Override
	public void onBackPressed() {
		if(newRecording) {
			Toast.makeText(getApplicationContext(), R.string.message_audio_not_inserted, Toast.LENGTH_LONG).show();
			super.onBackPressed();
		} else {
			super.onBackPressed();
		}
		
	}
	
	private class ButtonOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if(v.getId()==R.id.btn_save) {
				if(validateFields()) {
					if(newRecording) {
						saveRecording();
					} else {
						editRecording();
						Intent intent = new Intent(EditRecordingActivity.this,RecordingListActivity.class);
						startActivity(intent);
					}
					finish();
				} else {
					Toast.makeText(getApplicationContext(), R.string.message_name_validation, Toast.LENGTH_LONG).show();
				}
				
				
				
			} else if(v.getId()==R.id.btn_save_play) {
				if(validateFields()) {
					if(newRecording) {
						saveRecording();
					} else {
						editRecording();
					}
					finish();
					// Launch media player with filename parameter
					Intent intent = new Intent(EditRecordingActivity.this,MediaPlayerActivity.class);
					intent.putExtra("id", lastRecordingId);
					startActivity(intent);
				} else {
					Toast.makeText(getApplicationContext(), R.string.message_name_validation, Toast.LENGTH_LONG).show();
				}
				
			}
			
		}
		
	}
	
	private boolean isNetworkConnected() {
		  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		  return (cm.getActiveNetworkInfo() != null);
	 }
	
	private boolean validateFields() {
		
		String content = etName.getText().toString();
		String regex = "[a-zA-Z0-9 _-]*";
		if(content.matches(regex)) return true;
		else return false;
		
	}
	
	private void saveRecording() {
		
		//rename file		
		final String currentDate = getCurrentFormatedDate();
		final String name = etName.getText().toString();
		final String oldFilename = filename;
		final String newFilename = name+" "+currentDate+".mp4";		

		fm.rename(oldFilename,newFilename);		
		
		// create recording object
		Recording newRecording = new Recording();
		newRecording.setName(name);
		newRecording.setDescription(etDescription.getText().toString());
		newRecording.setDate(currentDate);
		newRecording.setOwner(owner);
		newRecording.setLocalFilename(newFilename);
		newRecording.setCloudFilename(newFilename);
		newRecording.setDuration(duration);
		newRecording.setOnLocal(true);
		newRecording.setOnCloud(false);
		
		
		
		if(isNetworkConnected()) {
			uploadToCloud(newFilename);
			newRecording.setOnCloud(true);
		} else {
			Toast.makeText(getApplicationContext(), R.string.message_not_uploaded, Toast.LENGTH_SHORT).show();
		}
		
		// insert recording into database
		DatabaseHelper db = new DatabaseHelper(this);
		lastRecordingId = db.insertRecording(newRecording);
		newRecording = null;
		
	}
	
	private void editRecording() {
		
		recording.setName(etName.getText().toString());
		recording.setDescription(etDescription.getText().toString());
		
		final String currentDate = getCurrentFormatedDate();
		final String name = etName.getText().toString();
		final String oldLocalFilename = recording.getLocalFilename();
		final String oldCloudFilename = recording.getCloudFilename();
		final String newFilename = name+" "+currentDate+".mp4";
		
		//rename local file
		if(recording.isOnLocal()) {
			fm.rename(oldLocalFilename,newFilename);	
			recording.setLocalFilename(newFilename);
		}
		//rename cloud file
		if(recording.isOnCloud()) {
			if(isNetworkConnected()) {
				new Thread(new Runnable() {
				    public void run() {
				    	fm.renameCloud(oldCloudFilename, newFilename);
				   }
				}).start();				
				recording.setCloudFilename(newFilename);
			} else {
				Toast.makeText(getApplicationContext(), R.string.message_cloud_not_renamed, Toast.LENGTH_SHORT).show();
			}
		}
		
		// update recording
		DatabaseHelper db = new DatabaseHelper(this);
		db.updateRecording(recording);
		recording = null;
	}
	
	private void uploadToCloud(final String filename) {
		new Thread(new Runnable() {
		    public void run() {
		    	fm.upload(filename);
		   }
		}).start();
	}
	
	private void getRecordingInfo() {
		DatabaseHelper db = new DatabaseHelper(this);
		recording = db.getRecording(lastRecordingId);
		// populate fields
		etName.setText(recording.getName());
		etDescription.setText(recording.getDescription());
	}
	
	private String getCurrentFormatedDate() {
		
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		return df.format(c.getTime());
		
	}
	

}
