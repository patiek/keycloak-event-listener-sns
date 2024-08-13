package fr.redfroggy.keycloak;

import org.keycloak.events.Event;

class SnsEvent {
   
    private final Event event;
    private final String username;

    public SnsEvent(Event event, String username) {
        this.event = event;
        this.username = username;
    }

    public Event getEvent() {
        return event;
    }

    public String getEventId() {
        if (event == null) {
            return null;
        } else {
            return event.getId();
        }
    }

    public String getUsername(){
        return username;
    }

}
