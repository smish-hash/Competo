package com.StartupBBSR.competo.Models;

import java.util.List;

public class EventModel {
    private String eventPoster;
    private String eventTitle, eventDescription, eventVenue;
    private String eventDate, eventTime;
    private List<String> eventTags;

    private String eventOrganizerID;

    public EventModel() {
//        Firebase needs empty constructor
    }

    public EventModel(String eventPoster, String eventTitle, String eventDescription, String eventVenue, String eventDate, String eventTime, List<String> eventTags, String eventOrganizerID) {
        this.eventPoster = eventPoster;
        this.eventTitle = eventTitle;
        this.eventDescription = eventDescription;
        this.eventVenue = eventVenue;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.eventTags = eventTags;
        this.eventOrganizerID = eventOrganizerID;
    }

    public String getEventPoster() {
        return eventPoster;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public String getEventVenue() {
        return eventVenue;
    }

    public String getEventDate() {
        return eventDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public List<String> getEventTags() {
        return eventTags;
    }

    public String getEventOrganizerID() {
        return eventOrganizerID;
    }
}