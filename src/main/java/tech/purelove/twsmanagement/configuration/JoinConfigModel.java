package tech.purelove.twsmanagement.configuration;

import java.util.List;

public class JoinConfigModel {

    public int delay = 0;

    public OnJoinAnnouncement onJoinMessage;
    public FirstJoinMessage firstJoinMessage;
    public GiveWrittenBooks giveWrittenBooks;
    public Teleport teleport;

    public static class FirstJoinMessage {
        public boolean enabled = true;
        public String message = "&d%player_name has joined for the first time!";
    }

    public static class GiveWrittenBooks {
        public boolean enabled = false;
        public List<String> bookFiles = List.of();
    }

    public static class Teleport {
        public boolean enabled = false;
        public String world = "world";
        public double x = 0;
        public double y = 64;
        public double z = 0;
        public float yaw = 0f;
        public float pitch = 0f;
    }

    public static class OnJoinAnnouncement {
        public boolean enabled = true;
        public boolean showToFirstJoins = false;
        public String message = "@file:localJoin.txt";
    }
}
