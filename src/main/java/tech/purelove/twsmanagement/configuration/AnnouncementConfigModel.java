package tech.purelove.twsmanagement.configuration;

public class AnnouncementConfigModel {
    private boolean enabled = true;
    private int intervalSeconds = 1800;
    private Object messages; // "@file:..." OR inline object

    public boolean isEnabled() {
        return enabled;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public Object getMessages() {
        return messages;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setIntervalSeconds(int intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public void setMessages(Object messages) {
        this.messages = messages;
    }

    /** Convenience for runtime */
    public int getIntervalTicks() {
        return intervalSeconds * 20;
    }
}
