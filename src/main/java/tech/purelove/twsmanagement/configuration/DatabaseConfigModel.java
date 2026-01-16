package tech.purelove.twsmanagement.configuration;

public class DatabaseConfigModel {

	private String name;
	private int maxConcurrentConnections;
	
	public void setName(String name) {
		this.name = name;
	}

	public void setMaxConcurrentConnections(int maxConcurrentConnections) {
		this.maxConcurrentConnections = maxConcurrentConnections;
	}

	public String getName() {
		return this.name;
	}

	public int getMaxConcurrentConnections() {
		return this.maxConcurrentConnections;
	}
}
