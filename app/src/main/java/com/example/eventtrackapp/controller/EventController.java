package com.example.eventtrackapp.controller;

import android.content.Context;
import com.example.eventtrackapp.DatabaseHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventController {
    private DatabaseHelper databaseHelper;
    private HashMap<Integer, String> eventCache;

    public EventController(Context context) {
        this.databaseHelper = new DatabaseHelper(context);
        this.eventCache = new HashMap<>();
        loadEvents();
    }

    private void loadEvents() {
        eventCache.clear();
        List<String> events = databaseHelper.getAllEvents();
        for (int i = 0; i < events.size(); i++) {
            eventCache.put(i, events.get(i));
        }
    }

    public boolean checkUser(String username, String password) {
        return databaseHelper.checkUser(username, password);
    }

    public List<String> getAllEvents() {
        return new ArrayList<>(eventCache.values());
    }

    public boolean addEvent(String eventName, String eventDate) {
        boolean result = databaseHelper.addEvent(eventName, eventDate);
        if (result) {
            loadEvents(); // Refresh cache
        }
        return result;
    }

    public boolean updateEvent(int eventId, String eventName, String eventDate) {
        boolean result = databaseHelper.updateEvent(eventId, eventName, eventDate);
        if (result) {
            loadEvents(); // Refresh cache
        }
        return result;
    }

    public boolean deleteEvent(int eventId) {
        boolean result = databaseHelper.deleteEvent(eventId);
        if (result) {
            loadEvents(); // Refresh cache
        }
        return result;
    }

    public int getEventIdByPosition(int position) {
        return databaseHelper.getEventIdByPosition(position);
    }

    public List<String> searchEventByName(String eventName) {
        List<String> matchingEvents = new ArrayList<>();
        for (String event : eventCache.values()) {
            if (event.toLowerCase().contains(eventName.toLowerCase())) {
                matchingEvents.add(event);
            }
        }
        return matchingEvents;
    }

    public List<String> filterEventsByDateRange(String startDate, String endDate) {
        return databaseHelper.getEventsByDateRange(startDate, endDate);
    }
}
