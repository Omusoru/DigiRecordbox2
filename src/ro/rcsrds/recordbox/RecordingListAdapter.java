package ro.rcsrds.recordbox;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RecordingListAdapter extends BaseAdapter {

	private Activity activity;
	private List<Recording> recordingList;
	private static LayoutInflater inflater=null;
	
	
	public RecordingListAdapter(Activity activity, List<Recording> recordingList ) {
		this.activity = activity;
		this.recordingList = recordingList;
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return recordingList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
		// return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
		// return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		if(convertView==null) {
			vi = inflater.inflate(R.layout.rowlayout_recordinglist, null);
		}
		
		TextView tvName = (TextView) vi.findViewById(R.id.tv_name);
		TextView tvDescription = (TextView) vi.findViewById(R.id.tv_description);
		
		Recording recording = new Recording();
		recording = recordingList.get(position);
		
		tvName.setText(recording.getName());
		tvDescription.setText(recording.getDescription());
		
		return vi;
	}
	
	

	
	
}
