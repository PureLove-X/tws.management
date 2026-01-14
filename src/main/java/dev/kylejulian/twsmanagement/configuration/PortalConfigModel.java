package dev.kylejulian.twsmanagement.configuration;

public class PortalConfigModel {

    private boolean enabled;
    private int netherY_Level;
    private int avoidHubRadius;
    private boolean allowNetherToOverworld;

    private String messageNether;
    private String messageOverworld;

    public boolean getEnabled() {
        return this.enabled;
    }

    public int getNetherY_Level() {
        return this.netherY_Level;
    }

    public int getAvoidHubRadius() {
        return this.avoidHubRadius;
    }

    public boolean getAllowNetherToOverworld() {
        return this.allowNetherToOverworld;
    }

    public String getMessageNether() {
        return this.messageNether;
    }

    public String getMessageOverworld() {
        return this.messageOverworld;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setNetherY_Level(int netherY_Level) {
        this.netherY_Level = netherY_Level;
    }

    public void setAvoidHubRadius(int avoidHubRadius) {
        this.avoidHubRadius = avoidHubRadius;
    }

    public void setAllowNetherToOverworld(boolean allowNetherToOverworld) {
        this.allowNetherToOverworld = allowNetherToOverworld;
    }

    public void setMessageNether(String messageNether) {
        this.messageNether = messageNether;
    }

    public void setMessageOverworld(String messageOverworld) {
        this.messageOverworld = messageOverworld;
    }
}
