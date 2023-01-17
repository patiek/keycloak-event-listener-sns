package fr.redfroggy.keycloak;

import org.keycloak.events.Event;

public class SnsEvent {
   
    private final Event event;

    public SnsEvent(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

}
