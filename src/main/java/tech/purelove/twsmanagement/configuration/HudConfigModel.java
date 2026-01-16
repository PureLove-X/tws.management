package tech.purelove.twsmanagement.configuration;

public class HudConfigModel {

    private boolean enabled;
    private Integer refreshRateTicks;
    private Boolean autoEnableOnJoin;

    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public void setRefreshRateTicks(Integer refreshRateTicks) { this.refreshRateTicks = refreshRateTicks; }

    public boolean getEnabled() { return this.enabled; }

    public Integer getRefreshRateTicks() { return this.refreshRateTicks; }

    public void setAutoEnableOnJoin(boolean autoEnableOnJoin) {
        this.autoEnableOnJoin = autoEnableOnJoin;
    }
    public Boolean getAutoEnableOnJoin() { return this.autoEnableOnJoin; }
    public boolean hasAutoEnableOnJoin() { return this.autoEnableOnJoin != null; }
}
