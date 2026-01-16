package tech.purelove.twsmanagement.afk.events;

import java.util.UUID;

/**
 * 
 * Event to Fire when Players go AFK
 *
 */
public class AfkCancelledEvent extends AfkEventBase {
	
	public AfkCancelledEvent(UUID playerId) {
		super(playerId);
	}
}
