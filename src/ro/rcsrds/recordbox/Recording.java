package ro.rcsrds.recordbox;

public class Recording {

	private int id;
	private String name;
	private String description;
	private String date;
	private String owner;
	private String filename;
	private int duration;
	
	public Recording() {}
	
	public Recording(String name, String description, String date,
			String owner, String filename, int duration) {
		this.id = 0;
		this.name = name;
		this.description = description;
		this.date = date;
		this.owner = owner;
		this.filename = filename;
		this.duration = duration;
	}
	
	
	public int getId() { return this.id; }
	public String getName() {return this.name;}
	public String getDescription() {return this.description;}
	public String getDate() { return this.date;}
	public String getOwner() { return this.owner; }
	public String getFilename() { return this.filename; }
	public int getDuration() { return this.duration; }
	
	public void setId(int id) { this.id = id; }
	public void setName(String name) { this.name = name; }
	public void setDescription(String description) { this.description = description; }
	public void setDate(String date) { this.date = date; }
	public void setOwner(String owner) { this.owner = owner; }
	public void setFilename(String filename) { this.filename = filename; }
	public void setDuration(int duration) { this.duration = duration; }
	
	
	
	
	
}
