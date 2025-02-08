package com.example.eventtrackapp.controller;

import android.content.Context;
import com.example.eventtrackapp.DatabaseHelper;

import java.util.HashMap;
import java.util.List;

public class EventController {
    private DatabaseHelper dbHelper;

    public EventController(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public HashMap<Integer, String> getEventCache() {
        return dbHelper.getEventCache();
    }

    public boolean addEvent(String eventName, String eventDate) {
        return dbHelper.addEvent(eventName, eventDate);
    }

    public boolean updateEvent(int eventId, String eventName, String eventDate) {
        return dbHelper.updateEvent(eventId, eventName, eventDate);
    }

    public boolean deleteEvent(int eventId) {
        return dbHelper.deleteEvent(eventId);
    }

    public List<String> filterEventsByDateRange(String startDate, String endDate) {
        return dbHelper.getEventsByDateRange(startDate, endDate);
    }

    public int getEventIdByPosition(int position) {
        return dbHelper.getEventIdByPosition(position);
    }
}
