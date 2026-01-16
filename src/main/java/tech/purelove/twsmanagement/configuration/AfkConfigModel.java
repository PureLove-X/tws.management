package tech.purelove.twsmanagement.configuration;

public class AfkConfigModel {

	private int timeMinutes;
	private int kickTimeMinutes;
	private String kickMessage;
	private int playerCountNeededForKick;
	private String[] events;
	private boolean sendPlayerAfkMessage;
	private boolean afkKick;
	
	public int getTimeMinutes() {
		return this.timeMinutes;
	}

	public int getKickTimeMinutes() {
		return this.kickTimeMinutes;
	}
	
	public String getKickMessage() {
		return this.kickMessage;
	}
	
	public int getPlayerCountNeededForKick() {
		return this.playerCountNeededForKick;
	}
	
	public String[] getEvents() {
		return this.events;
	}

	public boolean getSendPlayerAfkMessage() {
		return this.sendPlayerAfkMessage;
	}

	public boolean getAfkKick() {
		return this.afkKick;
	}
	
	public void setTimeMinutes(int timeMinutes) {
		this.timeMinutes = timeMinutes;
	}
	
	public void setKickTimeMinutes(int kickTimeMinutes) {
		this.kickTimeMinutes = kickTimeMinutes;
	}
	
	public void setKickMessage(String kickMessage) {
		this.kickMessage = kickMessage;
	}
	
	public void setPlayerCountNeededForKick(int playerCountNeededForKick) {
		this.playerCountNeededForKick = playerCountNeededForKick;
	}
	
	public void setEvents(String[] events) {
		this.events = events;
	}

	public void setSendPlayerAfkMessage(boolean sendPlayerAfkMessage) {
		this.sendPlayerAfkMessage = sendPlayerAfkMessage;
	}

	public void setAfkKick(boolean afkKick) {
		this.afkKick = afkKick;
	}
}
