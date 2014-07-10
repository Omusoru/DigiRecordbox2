package ro.rcsrds.recordbox;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MediaPlayerActivity extends Activity {
	
	Button btnPlay;
	Button btnStop;
	AudioRecorder recorder;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_mediaplayer);
		
		btnPlay = (Button) findViewById(R.id.btn_player_play);
		btnPlay.setOnClickListener(new ButtonOnClickListener());
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
	
	private class ButtonOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if(v.getId()==R.id.btn_player_play) {
				recorder.startPlaying();
				if(btnPlay.getText().toString().equalsIgnoreCase("Play")) {
					btnPlay.setText("Pause");
				} else if(btnPlay.getText().toString().equalsIgnoreCase("Pause")) {
					btnPlay.setText("Play");
				}
			} else if(v.getId()==R.id.btn_player_stop) {
				recorder.stopPlaying();
				finish();	
			}
					
		}
		
	}

}
