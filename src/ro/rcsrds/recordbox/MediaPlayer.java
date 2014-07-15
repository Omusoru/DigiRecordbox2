package ro.rcsrds.recordbox;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

public class MediaPlayer {
	private static final String LOG_TAG="Testing MediaPlayer";
	private static final String PREFS_NAME="Authentication";
	
	private android.media.MediaPlayer Player = null;	
	
	private String localFilePath;
	
	private int playingPausedAt;
	private String username;
	
	private SharedPreferences preferences;
	
	private boolean isPlaying = false;
	private boolean canPlay = true;
	
	public MediaPlayer(Context context) {
		this.preferences = context.getSharedPreferences(PREFS_NAME, 0);
		username = preferences.getString("username", "").toLowerCase();
		username = username.replace(".", "DOT");
		username = username.replace("@", "AT");
		localFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DigiRecordbox/"+username+"/";
		
	}
	
	public void startPlaying(String filename) {
		localFilePath = localFilePath + filename;
		if(canPlay){
	        Player = new android.media.MediaPlayer();
	        
	        try {
	            Player.setDataSource(localFilePath);
	            Player.prepare();
	            Player.start();
	        } catch (IOException e) {
	            Log.e(LOG_TAG, "prepare() failed");
	        }        
	        isPlaying=true;
	        canPlay=false;
		}    	
    	else if(isPlaying){
    		Player.pause();
    		playingPausedAt = Player.getCurrentPosition();
    		isPlaying=false;
    	}
    	else if(!isPlaying){
    		Player.seekTo(playingPausedAt);
    		Player.start();    		
    		isPlaying=true;    	   
    	}
		
		/*while(Player.getDuration()-Player.getCurrentPosition()>0){
			while(Player.getDuration()-Player.getCurrentPosition()<=0)
			{
				stopPlaying();
			}
		}*/
	}
    
    public void stopPlaying() {
    	if(isPlaying) {
    		Player.release();
            Player = null;
            canPlay=true;
            isPlaying=false;
    	}
        
    }
    
    public android.media.MediaPlayer getPlayerStatus()
    {
    	return Player;
    }
    
    public void setCurentPosition(int timeorposition){
    	playingPausedAt=timeorposition;
    }
    
    // !!!!!!! reinstantiaza Player. NU apela odata cu startRecording(); !!!!!!!!!!
    public int getDuration (String filename) {
    	android.media.MediaPlayer Player2 = new android.media.MediaPlayer();
    	String filePath= localFilePath + filename;
    	try {
    		Player2.setDataSource(filePath);
    		Player2.prepare();
    	} catch (IOException ioe) {
    		Log.d(LOG_TAG,"IOException: "+ioe.getMessage());
    	}
    	return Player2.getDuration();    	
    }
    
}
