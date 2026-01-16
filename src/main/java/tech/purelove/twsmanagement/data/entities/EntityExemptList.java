package tech.purelove.twsmanagement.data.entities;

import java.util.ArrayList;
import java.util.UUID;

public class EntityExemptList {

    private ArrayList<UUID> playerIds;

    private int maxPageCount;

    /**
     * Get Player Ids retrieved from the Database
     * @return ArrayList of Player UUIDs
     */
    public ArrayList<UUID> getPlayerIds() {
        return this.playerIds;
    }

    /**
     * Get the maximum number of pages
     * @return int max number of possible pages (with specified page index and page size)
     */
    public int getMaxPageCount() {
        return this.maxPageCount;
    }

    /**
     * Set the Player Ids
     * @param playerIds Player Ids to set
     */
    public void setPlayerIds(ArrayList<UUID> playerIds) {
        this.playerIds = playerIds;
    }

    /**
     * Set the maximum number of pages
     * @param maxPageCount maximum number of pages
     */
    public void setMaxPageCount(int maxPageCount) {
        this.maxPageCount = maxPageCount;
    }
}
