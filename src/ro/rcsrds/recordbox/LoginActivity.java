package ro.rcsrds.recordbox;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class LoginActivity extends Activity {

	private EditText etEmail;
	private EditText etPassword;
	private Button btnLogin;
	private Authentication auth;
	private ProgressDialog dlgProgress;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_login);
		
		etEmail = (EditText) findViewById(R.id.et_email);
		etPassword = (EditText) findViewById(R.id.et_password);
		etPassword.setOnEditorActionListener(new EditorActionListener());
		btnLogin = (Button) findViewById(R.id.btn_login);
		btnLogin.setOnClickListener(new ButtonOnClickListener());	
		
		auth = new Authentication(this);
		
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
			if(isNetworkConnected()) {
				new LoginTask().execute();
			} else {
				Toast.makeText(getApplicationContext(), R.string.message_no_internet, Toast.LENGTH_SHORT).show();
			}			
		}
		
	}
	
	private class EditorActionListener implements OnEditorActionListener {

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			boolean handled = false;
			if(isNetworkConnected()) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {	
		        	new LoginTask().execute();
		            handled = true;
		        }
			} else {
				Toast.makeText(getApplicationContext(), R.string.message_no_internet, Toast.LENGTH_SHORT).show();
			}	        
	        return handled;
		}
		
	}
	
	private class LoginTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			dlgProgress = new ProgressDialog(LoginActivity.this,ProgressDialog.STYLE_SPINNER);
			dlgProgress.setTitle(getResources().getString(R.string.title_logging_in)); 
			dlgProgress.setMessage(getResources().getString(R.string.message_logging_in));
			dlgProgress.show();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			String username = etEmail.getText().toString();
			String password = etPassword.getText().toString();
			return auth.logIn(username, password);
		}
		
		@Override
		protected void onPostExecute(Boolean hasLoggedIn) {
			dlgProgress.dismiss();
			if(hasLoggedIn) {
				//Start main
				Intent intent = new Intent(LoginActivity.this,MainActivity.class);
				startActivity(intent);	
			} else {
				Toast message = Toast.makeText(getApplicationContext(), R.string.message_authentication_failed, Toast.LENGTH_SHORT);
    			message.show();
			}
		}
		
	}
	
	private boolean isNetworkConnected() {
		  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		  return (cm.getActiveNetworkInfo() != null);
	 }

}
