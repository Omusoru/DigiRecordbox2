package ro.rcsrds.recordbox;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
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
		
		fm = new FileManager(this);
		
		loadRecordings();
		
		list = (ListView) findViewById(R.id.list);
	    
		adapter = new RecordingListAdapter(this, recordingList);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new ListOnClickListener());
		registerForContextMenu(list);
		
    	    
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
		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		int menuItemIndex = item.getItemId();
		String[] menuItems = getResources().getStringArray(R.array.context_recordinglist);
		//String menuItemName = menuItems[menuItemIndex];
		//String listItemName = recordingList.get(info.position).getName();
	
		switch(menuItemIndex) {
		case 0: // Edit recording
			Log.d("Recordinglist","Edit recording");
			break;
			
		case 1: // Upload to cloud
			Log.d("Recordinglist","Upload to cloud");
			
			// upload file
			new Thread(new Runnable() {
			    public void run() {
			    	fm.upload(recordingList.get(info.position).getFilename());
			    }
			  }).start();
			
			// update recording database entry on_cloud to True
			Recording recording = new Recording();
			recording = recordingList.get(info.position);
			recording.setOnCloud(true);
			db.updateRecording(recording);
			recording = null;
			break;
			
		case 2: // Download from cloud
			Log.d("Recordinglist","Download from cloud");
			break;
		case 3: // Delete local file
			Log.d("Recordinglist","Delete local file");
			break;
		case 4: // Delete cloud file
			Log.d("Recordinglist","Delete cloud file");
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
			//Log.d("Recordinglist","s-a apasat ceva");
			Intent intent = new Intent(RecordingListActivity.this,MediaPlayerActivity.class);
			intent.putExtra("id", recordingList.get((int)id).getId());
			startActivity(intent);
		}
		
			
	}
	

}
