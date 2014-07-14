package ro.rcsrds.recordbox;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class RecordingListActivity extends Activity {	
	
//	ArrayList<String> listItems=new ArrayList<String>();
//	ArrayAdapter<String> adapter;
	List<Recording> recordingList;
	private ListView list;
	private RecordingListAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recordinglist);
		
		loadRecordings();
		
		list = (ListView) findViewById(R.id.list);
	    
		adapter = new RecordingListAdapter(this, recordingList);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new ListOnClickListener());
		
    	    
}
	
	public void loadRecordings() {
		
		DatabaseHelper db = new DatabaseHelper(this);
		recordingList = new ArrayList<Recording>();
		recordingList = db.getAllRecordings();
		
	}
	
	private class ListOnClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			//Log.d("Recordinglist","s-a apasat ceva");
			Intent intent = new Intent(RecordingListActivity.this,MediaPlayerActivity.class);
			intent.putExtra("filename", recordingList.get((int)id).getFilename());
			startActivity(intent);
		}
		
		
		
	}
	
	

}
