package ro.rcsrds.recordbox;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
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
	
	public boolean isPlaying = false;
	private boolean canPlay = true;
	
	public MediaPlayer(Context context) {
		this.preferences = context.getSharedPreferences(PREFS_NAME, 0);
		username = preferences.getString("username", "").toLowerCase();
		username = username.replace(".", "DOT");
		username = username.replace("@", "AT");
		localFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DigiRecordbox/"+username+"/";
		
	}
	
	public void startPlaying(String filename,Boolean online) {
		if(online){			
			localFilePath=filename;
		}
		else localFilePath = localFilePath + filename;
		if(canPlay){
			if(Player==null){
			    Player = new android.media.MediaPlayer();			    
			    if(online) Player.setAudioStreamType(AudioManager.STREAM_MUSIC);	    
			   
			}
			else
			{
				Player.release();
				Player=null;
				Player = new android.media.MediaPlayer();
				if(online) Player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			}
	        
	        try {	        	
	            Player.setDataSource(localFilePath);
	            //Log.d("dltest","AJUNGE AICI");
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
    	Player.seekTo(timeorposition);
    	playingPausedAt=timeorposition;
    }
    
    public void pausePlaying(){
    	if(isPlaying==true){
    	playingPausedAt = Player.getCurrentPosition();
    	Player.pause();		
		isPlaying=false;
    	}
    }
    
    public void resumePlayingAt(int time){
    	Player.seekTo(time);
		Player.start();    		
		isPlaying=true;
    }
    
    public void resumePlaying(){
    	if(isPlaying==false){
    		Player.seekTo(playingPausedAt);
    		Player.start();    		
    		isPlaying=true;
    	}
    }    
    
}
