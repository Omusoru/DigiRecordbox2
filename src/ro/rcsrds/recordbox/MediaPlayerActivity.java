package ro.rcsrds.recordbox;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MediaPlayerActivity extends Activity {
	
	Button btnPlay;
	Button btnStop;
	MediaPlayer player;
	String filename;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_mediaplayer);
		
		btnPlay = (Button) findViewById(R.id.btn_player_play);
		btnPlay.setOnClickListener(new ButtonOnClickListener());
		btnStop = (Button) findViewById(R.id.btn_player_stop);
		btnStop.setOnClickListener(new ButtonOnClickListener());
		
		player = new MediaPlayer();
		
		//TODO start playing this file:
		filename = getIntent().getExtras().getString("fileName");
		//Log.d("Mediaplayer",filename);		
		
		//Play incepe automat
		player.startPlaying(filename);
		btnPlay.setText("Pause");
		
	}
	
	private class ButtonOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if(v.getId()==R.id.btn_player_play) {
				player.startPlaying(filename);
				if(btnPlay.getText().toString().equalsIgnoreCase("Play")) {
					btnPlay.setText("Pause");
				} else if(btnPlay.getText().toString().equalsIgnoreCase("Pause")) {
					btnPlay.setText("Play");
				}
			} else if(v.getId()==R.id.btn_player_stop) {
				player.stopPlaying();
				finish();	
			}
					
		}
		
	}

}
