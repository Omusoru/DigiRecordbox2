package ro.rcsrds.recordbox;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;

@SuppressLint("SimpleDateFormat")
public class EditRecordingActivity extends ActionBarActivity {
	
	private EditText etName;
	private EditText etDescription;
	private Button btnSave;
	private Button btnSavePlay;
	private String filename;
	private String owner;
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
				//TODO save and launch media player
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
		
		// insert recording into database
		DatabaseHelper db = new DatabaseHelper(this);
		db.insertRecording(newRecording);
		
	}
	
	private String getCurrentFormatedDate() {
		
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(c.getTime());
		
	}
	
	private int getDuration() {
		MediaPlayer player = new MediaPlayer();	
		Log.d("Database",""+player.getDurationInSeconds(filename));
		return player.getDurationInSeconds(filename);
	}

}
