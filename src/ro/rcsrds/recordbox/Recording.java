package ro.rcsrds.recordbox;

public class Recording {

	private int id;
	private String name;
	private String description;
	private String date;
	private String owner;
	private String filename;
	private String duration;
	private boolean onLocal;
	private boolean onCloud;
	
	public Recording() {}
	
	public Recording(String name, String description, String date,
			String owner, String filename, String duration, boolean onLocal, boolean onCloud) {
		this.id = 0;
		this.name = name;
		this.description = description;
		this.date = date;
		this.owner = owner;
		this.filename = filename;
		this.duration = duration;
		this.onLocal = onLocal;
		this.onCloud = onCloud;
	}
	
	
	public int getId() { return this.id; }
	public String getName() {return this.name;}
	public String getDescription() {return this.description;}
	public String getDate() { return this.date;}
	public String getOwner() { return this.owner; }
	public String getFilename() { return this.filename; }
	public String getDuration() { return this.duration; }
	public boolean isOnLocal() { return this.onLocal; }
	public boolean isOnCloud() { return this.onCloud; }
	
	public void setId(int id) { this.id = id; }
	public void setName(String name) { this.name = name; }
	public void setDescription(String description) { this.description = description; }
	public void setDate(String date) { this.date = date; }
	public void setOwner(String owner) { this.owner = owner; }
	public void setFilename(String filename) { this.filename = filename; }
	public void setDuration(String duration) { this.duration = duration; }
	public void setOnLocal (boolean onLocal) { this.onLocal = onLocal; }
	public void setOnCloud (boolean onCloud) { this.onCloud = onCloud; }
	
	
	
	
	
}
