package ro.rcsrds.recordbox;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class RecordingListActivity extends Activity {	
	
	private List<Recording> recordingList;
	private ListView list;
	private RecordingListAdapter adapter;
	private FileManager fm;
	private DatabaseHelper db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recordinglist);
		
		
		fm = new FileManager(RecordingListActivity.this);
		new Thread(new Runnable() {
		    public void run() {		    	
		    	if(isNetworkConnected()) {
		    		fm.connectToCloud();
		    	} else {
		    		//TODO toast
		    		Log.d("Connection","Not network connected");
		    		runOnUiThread(new Runnable() {
			            public void run() {
			            	Toast.makeText(getApplicationContext(), R.string.message_no_internet, Toast.LENGTH_SHORT).show();
			            }
			        });
		    	}
		   }
		}).start();
		
		
		loadRecordings();
		
		list = (ListView) findViewById(R.id.list);
	    
		adapter = new RecordingListAdapter(this, recordingList);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new ListOnClickListener());
		registerForContextMenu(list);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.recordinglist, menu);
		return true;
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==R.id.option_menu_recorder) {
			Intent mediaPlayer = new Intent(RecordingListActivity.this,MainActivity.class);
			startActivity(mediaPlayer);
		}
		return super.onOptionsItemSelected(item);
	}
	
	private boolean isNetworkConnected() {
		  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		  return (cm.getActiveNetworkInfo() != null);
	 }
    	   
	
	public void loadRecordings() {
		
		db = new DatabaseHelper(this);
		recordingList = new ArrayList<Recording>();
		recordingList = db.getAllRecordings();
		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId()==R.id.list) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		    menu.setHeaderTitle(recordingList.get(info.position).getName());
		    String[] menuItems = getResources().getStringArray(R.array.context_recordinglist);
		    for (int i = 0; i<menuItems.length; i++) {
		      menu.add(Menu.NONE, i, i, menuItems[i]);
		    }
		  
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		int menuItemIndex = item.getItemId();
		String[] menuItems = getResources().getStringArray(R.array.context_recordinglist);
		//String menuItemName = menuItems[menuItemIndex];
		//String listItemName = recordingList.get(info.position).getName();
	
		switch(menuItemIndex) {
		case 0: // Edit recording
			Log.d("Recordinglist","Edit recording");
			Intent intent = new Intent(RecordingListActivity.this, EditRecordingActivity.class);
			intent.putExtra("id", recordingList.get(info.position).getId());
			intent.putExtra("new",false);
			startActivity(intent);
			break;
			
		case 1: // Upload to cloud
			Log.d("Recordinglist","Upload to cloud");			
			uploadToCloud(info.position);
			restartActivity();
			break;			
		case 2: // Download from cloud
			Log.d("Recordinglist","Download from cloud");
			downloadFromCloud(info.position);
			restartActivity();
			break;
		case 3: // Delete local file
			Log.d("Recordinglist","Delete local file");
			deleteFromLocal(info.position);
			restartActivity();
			break;
		case 4: // Delete cloud file
			Log.d("Recordinglist","Delete cloud file");
			deleteFromCloud(info.position);
			restartActivity();
			break;
		default:
			Log.d("Recordinglist","Nothing");
		}
		
		return super.onContextItemSelected(item);
	}
	
	private class ListOnClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent = new Intent(RecordingListActivity.this,MediaPlayerActivity.class);
			intent.putExtra("id", recordingList.get((int)id).getId());
			startActivity(intent);
		}
		
			
	}
	
	private void restartActivity() {
		finish();		
		Intent intent = new Intent(RecordingListActivity.this,RecordingListActivity.class);
		startActivity(intent);
	}
	
	private void uploadToCloud(final int position) {
		// upload file
		new Thread(new Runnable() {
		    public void run() {
		    	fm.upload(recordingList.get(position).getFilename());
		   }
		}).start();
		
		// update recording database entry on_cloud to True
		updateEntry("cloud", true, position);
		restartActivity();
	}
	
	private void downloadFromCloud(final int position) {
		
		// download file
		new Thread(new Runnable() {
		    public void run() {
		    	fm.download(recordingList.get(position).getFilename());
		    }
		}).start();
		
		// update recording database entry on_local to True
		updateEntry("local",true,position);
		restartActivity();
		
	}
	
	private void deleteFromCloud(final int position) {
		// delete cloud file
		new Thread(new Runnable() {
		    public void run() {
		    	fm.deleteCloud(recordingList.get(position).getFilename());
		    }
		}).start();
		
		// update recording database entry on_cloud to False
		updateEntry("cloud",false,position);
		restartActivity();
	}
	
	private void deleteFromLocal(final int position) {
		// delete local file
		new Thread(new Runnable() {
		    public void run() {
		    	fm.deleteLocal(recordingList.get(position).getFilename());
		    }
		}).start();
		
		// update recording database entry on_local to False
		updateEntry("local",false,position);
		restartActivity();
	}
	
	private void updateEntry(String location, boolean status, int position) {
		
		Recording recording = new Recording();
		recording = recordingList.get(position);
		if(location.equals("cloud")) {
			recording.setOnCloud(status);			
		} else if(location.equals("local")) {
			recording.setOnLocal(status);
		}
		if((recording.isOnCloud()==false)&&(recording.isOnLocal()==false)) {
			db.deleteRecording(recording);
		} else {
			db.updateRecording(recording);
		}		
		recording = null;
		
	}
	

}
