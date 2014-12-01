package ro.rcsrds.recordbox;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditRecordingActivity extends Activity {
	
	private EditText etName;
	private EditText etDescription;
	private Button btnSave;
	private Button btnSavePlay;
	private String filename;
	private String owner;
	private String duration;
	private int lastRecordingId;
	private boolean isNewRecording;
	private boolean willBePlayed;
	private boolean allowAutoUpload;
	private boolean useDeviceName;
	public static final String PREFS_NAME = "Authentication";
	private Recording recording;
	private FileManager fm;
	private ProgressDialog dlgProgress;

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
			isNewRecording = true;
			// get filename parameter passed from main activity
			filename = getIntent().getExtras().getString("filename");
			duration = getIntent().getExtras().getString("duration");
		} else {			
			isNewRecording = false;
			// get recording id passed from recording list
			lastRecordingId = getIntent().getExtras().getInt("id");	
			getRecordingInfo();
		}
		
		// get logged in username
		SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
		owner = preferences.getString("username", "no owner");
		
		// get application settings
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		allowAutoUpload = settings.getBoolean("pref_key_auto_upload", true) ? true : false; 
		useDeviceName = settings.getBoolean("pref_key_device_name", true) ? true : false; 
	}
	
	@Override
	public void onBackPressed() {
		if(isNewRecording) {
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
				if(validateFields()==0) {
					btnSave.setEnabled(false);
					btnSavePlay.setEnabled(false);
					if(isNewRecording) {
						saveRecording();
						if(allowAutoUpload) {
							if(isNetworkConnected()) {
								willBePlayed = false;
								uploadToCloud(); // finish() is called after upload is done
							} else {
								Toast.makeText(getApplicationContext(), R.string.message_not_uploaded, Toast.LENGTH_SHORT).show();
								finish();
							}
						} else {
							finish();
						}
					} else {
						editRecording();
						//Intent intent = new Intent(EditRecordingActivity.this,RecordingListActivity.class);
						//startActivity(intent);
						//RecordingListActivity.getAdapter().notifyDataSetChanged();
						finish();
					}
					
				} else if(validateFields()==1) {
					Toast.makeText(getApplicationContext(), R.string.message_name_validation1, Toast.LENGTH_LONG).show();
				} else if(validateFields()==2) {
					Toast.makeText(getApplicationContext(), R.string.message_name_validation2, Toast.LENGTH_LONG).show();
				}
			} else if(v.getId()==R.id.btn_save_play) {
				if(validateFields()==0) {
					btnSave.setEnabled(false);
					btnSavePlay.setEnabled(false);
					if(isNewRecording) {
						saveRecording();
						if(allowAutoUpload) {
							if(isNetworkConnected()) {
								willBePlayed = true;
								uploadToCloud(); // finish() and launchMediaPlayer() is called after upload is done
							} else {
								Toast.makeText(getApplicationContext(), R.string.message_not_uploaded, Toast.LENGTH_SHORT).show();
								finish();
								launchMediaPlayer();
							}
						} else {
							finish();
							launchMediaPlayer();
						}
						
					} else {
						editRecording();
						finish();
						launchMediaPlayer();
					}
				} else if(validateFields()==1) {
					Toast.makeText(getApplicationContext(), R.string.message_name_validation1, Toast.LENGTH_LONG).show();
				} else if(validateFields()==2) {
					Toast.makeText(getApplicationContext(), R.string.message_name_validation2, Toast.LENGTH_LONG).show();
				}
				
			} else if (v.getId()==R.id.btn_more) {
				openOptionsMenu();
			}
			
		}
		
	}
	
	private boolean isNetworkConnected() {
		  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		  return (cm.getActiveNetworkInfo() != null);
	}
	
	private int validateFields() {
		
		String content = etName.getText().toString();
		if(content.equals("")) return 2;
		String regex = "[a-zA-Z0-9 _-]*";
		if(content.matches(regex)) return 0;
		else return 1;
		
	}
	
	private void saveRecording() {
		
		String currentDate = getCurrentFormatedDate();
		String name = etName.getText().toString();		
		String oldFilename = filename;
		String newFilename;
		
		if(useDeviceName) {
			BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
			String deviceName = sanitizeDeviceName(myDevice.getName());
			newFilename = name+" "+currentDate+" "+deviceName+".mp4";	
		} else {
			newFilename = name+" "+currentDate+".mp4";
		}

		fm.rename(oldFilename,newFilename);
		filename = newFilename;
		
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
		
		// insert recording into database
		DatabaseHelper db = new DatabaseHelper(this);
		lastRecordingId = db.insertRecording(newRecording);
		newRecording = null;
		
	}
	
	private void editRecording() {
		
		recording.setName(etName.getText().toString());
		recording.setDescription(etDescription.getText().toString());
		
		String currentDate = getCurrentFormatedDate();
		String name = etName.getText().toString();
		String oldLocalFilename = recording.getLocalFilename();
		final String oldCloudFilename = recording.getCloudFilename();
		String newFilename;
		
		if(useDeviceName) {
			BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
			String deviceName = sanitizeDeviceName(myDevice.getName());
			newFilename = name+" "+currentDate+" "+deviceName+".mp4";	
		} else {
			newFilename = name+" "+currentDate+".mp4";
		}
		
		final String newFilenameForCloud = newFilename;
		
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
				    	fm.renameCloud(oldCloudFilename, newFilenameForCloud);
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
	
	private void uploadToCloud() {
		
		DatabaseHelper db = new DatabaseHelper(this);
		Recording recording = db.getRecording(lastRecordingId);
		
		new UploadToCloudTask().execute(filename);
		recording.setOnCloud(true);
		db.updateRecording(recording);
		recording = null;
		
	}
	
	private class UploadToCloudTask extends AsyncTask<String, Void, Void> {
		
		@Override
		protected void onPreExecute() {
			dlgProgress = new ProgressDialog(EditRecordingActivity.this,ProgressDialog.STYLE_SPINNER);
			dlgProgress.setTitle(getResources().getString(R.string.title_uploading));
			dlgProgress.setMessage(getResources().getString(R.string.message_uploading));
			dlgProgress.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			fm.upload(params[0]);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			dlgProgress.dismiss();
			finish();
			if(willBePlayed) {
				launchMediaPlayer();
			}
		}
		
	}
	
	private void launchMediaPlayer() {
		// Launch media player with id parameter
		Intent intent = new Intent(EditRecordingActivity.this,MediaPlayerActivity.class);
		intent.putExtra("id", lastRecordingId);
		startActivity(intent);
	}
	
	private void getRecordingInfo() {
		DatabaseHelper db = new DatabaseHelper(this);
		recording = db.getRecording(lastRecordingId);
		// populate fields
		etName.setText(recording.getName());
		etDescription.setText(recording.getDescription());
	}
	
	@SuppressLint("SimpleDateFormat")
	private static String getCurrentFormatedDate() {
		
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		return df.format(c.getTime());
		
	}
	
	private static String sanitizeDeviceName(String deviceName) {
		
		if(deviceName == null) {
			return new String("");
		}
		
		char cleanName[] = deviceName.toCharArray();
		
		for(int i=0;i<deviceName.length();i++) {
			if(!Character.isLetter(cleanName[i]) && !Character.isDigit(cleanName[i])) {
				cleanName[i] = '_';
			}
		}
		
		return new String(cleanName);
		
	}
	

}
