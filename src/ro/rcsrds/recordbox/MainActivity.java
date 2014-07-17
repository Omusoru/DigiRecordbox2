package ro.rcsrds.recordbox;

import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private Button btnRecord;
	private Button btnStop;
	private Button btnCancel;
	private TextView tvRecorderTime;
	private AudioRecorder recorder;
	private Authentication auth;
	private String filename;
	private Dialog dlgSaving;
	private boolean buttonRecording;
	//private Handler mHandler = new Handler();
	private boolean needsCancel;
	
	//Timer
	private long startTime = 0L;
	private Handler myHandler = new Handler();
	long timeInMillies = 0L;
	long timeSwap = 0L;
	long finalTime = 0L;
	private boolean paused = true;
	private String previousTime;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		auth = new Authentication(this);
		
		//Check if user is logged in
		if(!auth.isLoggedIn()) {
			//Close main activity so user can't bypass login screen
			finish();
			//Start login activity
			Intent login = new Intent(MainActivity.this,LoginActivity.class);
			startActivity(login);			
		}		
		
		btnRecord = (Button) findViewById(R.id.btn_recorder_start);
		btnRecord.setOnClickListener(new ButtonClickListener());
		btnStop = (Button) findViewById(R.id.btn_recorder_stop);
		btnStop.setOnClickListener(new ButtonClickListener());
		btnStop.setVisibility(View.INVISIBLE);
		btnCancel = (Button) findViewById(R.id.btn_recorder_cancel);
		btnCancel.setOnClickListener(new ButtonClickListener());
		btnCancel.setVisibility(View.INVISIBLE);
		tvRecorderTime = (TextView) findViewById(R.id.tv_recorder_time);
		
		needsCancel = false;
		buttonRecording = true; 
		recorder = new AudioRecorder(auth.getUsername());
		dlgSaving = new Dialog(this);
	}

	private class StopRecordingTask extends AsyncTask<Object, Object, Object> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dlgSaving.setContentView(R.layout.dialog_main);
			dlgSaving.setTitle(getResources().getString(R.string.message_saving)); 
			dlgSaving.show();
		}

		@Override
		protected Object doInBackground(Object... params) {
			recorder.stopRecording();
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			dlgSaving.dismiss();
			startIntent(filename);			
			super.onPostExecute(result);
		}
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(needsCancel) {
			recorder.cancelRecording();
		    btnStop.setVisibility(View.INVISIBLE);
	        btnCancel.setVisibility(View.INVISIBLE);
	        resetButton();
	        stopTimer();
	        Toast.makeText(this, R.string.message_recording_canceled, Toast.LENGTH_SHORT).show();
	        needsCancel = false;
		}
	    
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==R.id.option_menu_logout) {
			auth.logOut();	
		    //Close main activity so user can't bypass login screen
			finish();
			//Start login activity
			Intent login = new Intent(MainActivity.this,LoginActivity.class);
			startActivity(login);	
		} else if(item.getItemId()==R.id.option_menu_list) {
			Intent mediaPlayer = new Intent(MainActivity.this,RecordingListActivity.class);
			startActivity(mediaPlayer);
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class ButtonClickListener implements OnClickListener {

		public void onClick(View v) {
			
			if (v.getId()==R.id.btn_recorder_stop) {			
				previousTime = tvRecorderTime.getText().toString();
				btnStop.setVisibility(View.INVISIBLE);
	            btnCancel.setVisibility(View.INVISIBLE);
				new StopRecordingTask().execute(true);
	            resetButton();	
	            stopTimer();
	            needsCancel = false;
			} else if (v.getId()==R.id.btn_recorder_cancel) {
				recorder.cancelRecording();
				btnStop.setVisibility(View.INVISIBLE);
	            btnCancel.setVisibility(View.INVISIBLE);
	            resetButton();
	            stopTimer();
	            needsCancel = false;
			} else if (v.getId()==R.id.btn_recorder_start) {	
				recorder.startRecording();
				filename = recorder.getLastFilename();
				btnStop.setVisibility(View.VISIBLE);
	            btnCancel.setVisibility(View.VISIBLE);	  
	            disableButton();
	            needsCancel = true;
	            //switchButtons();
	            startTimer();  
			}
			
		}		
		
	}
	
	private void startTimer() {
		if(paused) {
        	startTime = SystemClock.uptimeMillis();
			myHandler.postDelayed(updateTimerMethod, 0);
			paused = false;
        } else {
        	timeSwap += timeInMillies;
			myHandler.removeCallbacks(updateTimerMethod);
			paused = true;
        }
	}
	
	private void stopTimer() {
		myHandler.removeCallbacks(updateTimerMethod);
		paused = true;
		tvRecorderTime.setText("00:00");
		startTime = 0L;
		timeInMillies = 0L;
		timeSwap = 0L;
		finalTime = 0L;
	}
	
	private Runnable updateTimerMethod = new Runnable() {

		public void run() {
			timeInMillies = SystemClock.uptimeMillis()-startTime;
			finalTime = timeSwap + timeInMillies;			
			tvRecorderTime.setText(getTimeFormat(finalTime));
			myHandler.postDelayed(this, 0);
		}
	
	};
	
	public String getTimeFormat(long timeinms){
		String totext=null;
		if((timeinms/1000)/60<10)
			totext="0"+Long.toString((timeinms/1000)/60);
		else totext=Long.toString((timeinms/1000)/60);
		if((timeinms/1000)%60<10)
			totext+=":0"+Long.toString((timeinms/1000)%60);
		else totext+=":"+Long.toString((timeinms/1000)%60);
		
		return totext;
	}
	
	private void disableButton() {
		
        btnRecord.setEnabled(false);
		setEnabledButtons(false);		

		Timer buttonTimer = new Timer();
		buttonTimer.schedule(new TimerTask() {

		    @Override
		    public void run() {
		        runOnUiThread(new Runnable() {
		            public void run() {			            	
		            	btnRecord.setEnabled(true);
		            	setEnabledButtons(true);
		            	switchButtons();		            	
		            }
		        });
		    }
		}, 500);
	}
	
	private void setEnabledButtons(boolean enable) {
		if(enable) {
			if(buttonRecording) {
				btnRecord.setBackgroundResource(R.drawable.button_record_big);
			} else if(!buttonRecording) {
				btnRecord.setBackgroundResource(R.drawable.button_pause_big);				
			}
		} else {
			if(buttonRecording) {
				btnRecord.setBackgroundResource(R.drawable.button_record_big_disabled);				
			} else if(!buttonRecording) {
				btnRecord.setBackgroundResource(R.drawable.button_pause_big_disabled);
			}
		}
		
	}
	
	
	private void switchButtons() {
		if(buttonRecording) {
			btnRecord.setBackgroundResource(R.drawable.button_pause_big);
			buttonRecording = false;
		} else if(!buttonRecording) {
			btnRecord.setBackgroundResource(R.drawable.button_record_big);
			buttonRecording = true;
		}
	}
	
	private void resetButton() {
		btnRecord.setBackgroundResource(R.drawable.button_record_big);
	}
	
	private void startIntent(String filename){
		Log.d("Mediaplyer","filename after startRecording(): "+filename);
        Intent intent = new Intent(MainActivity.this,EditRecordingActivity.class);
		intent.putExtra("filename", filename);
		intent.putExtra("new",true);
		intent.putExtra("duration", previousTime);
		startActivity(intent);
	}

}
