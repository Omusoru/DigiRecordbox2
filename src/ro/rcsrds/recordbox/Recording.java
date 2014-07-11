package ro.rcsrds.recordbox;

public class Recording {

	private int id;
	private String name;
	private String description;
	private String date;
	private String owner;
	private String filename;
	private int duration;
	
	public Recording(int id, String name, String description, String date,
			String owner, String filename, int duration) {
		this.id = id;
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
	public String getFilenane() { return this.filename; }
	public int getDuration() { return this.duration; }
	
	
	
	
	
	
}
