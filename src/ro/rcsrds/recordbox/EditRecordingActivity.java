package ro.rcsrds.recordbox;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

@SuppressLint("SimpleDateFormat")
public class EditRecordingActivity extends ActionBarActivity {
	
	private EditText etName;
	private EditText etDescription;
	private Button btnSave;
	private Button btnSavePlay;
	private String filename;
	private String owner;
	private int lastRecordingId;
	public static final String PREFS_NAME = "Authentication";

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
		
		// get filename parameter passed from mainactivity
		filename = getIntent().getExtras().getString("filename");
		
		// get logged in username
		SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
		owner = preferences.getString("username", "no owner");
	}
	
	private class ButtonOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if(v.getId()==R.id.btn_save) {
				saveRecording();
				finish();
			} else if(v.getId()==R.id.btn_save_play) {
				saveRecording();
				finish();
				// Launch media player with filename parameter
				Intent intent = new Intent(EditRecordingActivity.this,MediaPlayerActivity.class);
				intent.putExtra("id", lastRecordingId);
				startActivity(intent);
			}
			
		}
		
	}
	
	private void saveRecording() {
		
		// create recording object
		Recording newRecording = new Recording();
		newRecording.setName(etName.getText().toString());
		newRecording.setDescription(etDescription.getText().toString());
		newRecording.setDate(getCurrentFormatedDate());
		newRecording.setOwner(owner);
		newRecording.setFilename(filename);
		newRecording.setDuration(getDuration());
		newRecording.setOnLocal(true);
		newRecording.setOnCloud(false);
		
		// insert recording into database
		DatabaseHelper db = new DatabaseHelper(this);
		lastRecordingId = db.insertRecording(newRecording);
		
	}
	
	private String getCurrentFormatedDate() {
		
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(c.getTime());
		
	}
	
	private String getDuration() {
		MediaPlayer player = new MediaPlayer();	
		return getTimeFormat(player.getDuration(filename));
	}
	
	public String getTimeFormat(int timeinms){
		String totext=null;
		if((timeinms/1000)/60<10)
			totext="0"+Integer.toString((timeinms/1000)/60);
		else totext=Integer.toString((timeinms/1000)/60);
		if((timeinms/1000)%60<10)
			totext+=":0"+Integer.toString((timeinms/1000)%60);
		else totext+=":"+Integer.toString((timeinms/1000)%60);
		
		return totext;
	}

}
