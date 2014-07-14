package ro.rcsrds.recordbox;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

	private EditText etEmail;
	private EditText etPassword;
	private Button btnLogin;
	public static final String PREFS_NAME = "Authentication";
	private Authentication auth;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_login);
		
		etEmail = (EditText) findViewById(R.id.et_email);
		etPassword = (EditText) findViewById(R.id.et_password);
		btnLogin = (Button) findViewById(R.id.btn_login);
		btnLogin.setOnClickListener(new ButtonOnClickListener());	
		
		SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
		auth = new Authentication(preferences);
		
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		//Toast message = Toast.makeText(Login.this, R.string.message_authentification_failed, Toast.LENGTH_SHORT);
		//message.show();		
		finish();
	}
	
	private class ButtonOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {			
			new Thread(new Runnable() {
			    public void run() {
			    	String username = etEmail.getText().toString();
					String password = etPassword.getText().toString();
			    	login(username,password);	
			    }
			  }).start();

					
		}
		
	}
	
	private void login(String username, String password) {
		
		if(auth.logIn(username, password)) {			
			//Start main
			Intent intent = new Intent(LoginActivity.this,MainActivity.class);
			startActivity(intent);	
		} else {
			runOnUiThread(new Runnable() {
	            public void run() {
	            	Toast message = Toast.makeText(getApplicationContext(), R.string.message_authentication_failed, Toast.LENGTH_SHORT);
	    			message.show();
	            }
	        });
	    }
		
	}
}
