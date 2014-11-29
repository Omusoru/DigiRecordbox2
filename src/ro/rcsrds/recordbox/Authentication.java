package ro.rcsrds.recordbox;

import net.koofr.api.v2.DefaultClientFactory;
import net.koofr.api.v2.StorageApiException;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Authentication {

	private String host = "storage.rcs-rds.ro";
	private String username = "";
	private String password = "";
	//private String authToken;
	private boolean loggedIn;
	private static final String TAG = "Authentication";
	public static final String PREFS_NAME = "Authentication";
	private SharedPreferences preferences;
	
	public Authentication(Context context) {
		this.preferences = context.getSharedPreferences(PREFS_NAME, 0);
		this.loggedIn = preferences.getBoolean("loggedIn", false);
		this.username = preferences.getString("username", "");
		this.password = preferences.getString("password", "");
		//this.authToken = preferences.getString("authToken", "");
	}
	
	public boolean isLoggedIn() {
		return this.loggedIn;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public boolean logIn(String username, String password) {
		
		this.loggedIn = true;
		try {
			DefaultClientFactory.create(this.host,username, password);
		} catch (StorageApiException sae) {
			Log.e(Authentication.TAG,sae.getMessage());
			this.loggedIn = false;
		}	
		
		
		if(this.loggedIn) {
			//this.authToken = api.getAuthToken();
			//Save login info to SharedPreferences	
		    SharedPreferences.Editor editor = this.preferences.edit();
		    editor.putBoolean("loggedIn", true);
		    editor.putString("username",username);
		    editor.putString("password",password);
		    //String authToken = api.getAuthToken();
		    //editor.putString("authToken", authToken);
		    editor.commit();
		    return true;
		} else {
			return false;			
		}
		
	}
	
	public void logOut() {
		
		this.loggedIn = false;
		this.username = "";
		this.password = "";
		//this.authToken = "";
		
		// Clear login info
	    SharedPreferences.Editor editor = preferences.edit();
	    editor.putBoolean("loggedIn", false);
	    editor.putString("username","");
	    editor.putString("password","");
	    //editor.putString("authToken", "");
	    editor.commit();
	    
	}
	
}
