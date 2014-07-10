package ro.rcsrds.recordbox;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

public class MediaPlayerActivity extends Activity {
	
	ToggleButton tglPlay;
	Button btnStop;
	AudioRecorder recorder;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_mediaplayer);
		
		tglPlay = (ToggleButton) findViewById(R.id.tgl_player_play);
		tglPlay.setOnCheckedChangeListener(new ButtonToggleListener());
		btnStop = (Button) findViewById(R.id.btn_player_stop);
		btnStop.setOnClickListener(new ButtonOnClickListener());
		
		recorder = new AudioRecorder();
		
		//TODO start playing this file:
		//String filename = getIntent().getExtras().getString("fileName");
		//Log.d("Mediaplayer",filename);		
		
		//Play incepe automat
		//recorder.startPlaying();
		//tglPlay.setChecked(true);
		
	}
	
	private class ButtonToggleListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			recorder.startPlaying();
		}
		
	}
	
	private class ButtonOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			recorder.stopPlaying();
			finish();			
		}
		
	}

}
