package com.example.eventtrackapp.controller;

import android.content.Context;
import com.example.eventtrackapp.DatabaseHelper;

import java.util.HashMap;
import java.util.List;

public class EventController {

    // Database helper instance for interacting with the database
    private DatabaseHelper dbHelper;

    public EventController(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // ✅ Fetch cached list of events from database & sync with Firestore
    public HashMap<Integer, String> getEventCache() {
        dbHelper.syncWithFirebase();  // Ensure Firestore syncs data
        return dbHelper.getEventCache();
    }

    // ✅ Add a new event (Local + Firestore)
    public boolean addEvent(String eventName, String eventDate) {
        return dbHelper.addEvent(eventName, eventDate);
    }

    // ✅ Update an existing event (Local + Firestore)
    public boolean updateEvent(int eventId, String eventName, String eventDate) {
        return dbHelper.updateEvent(eventId, eventName, eventDate);
    }

    // ✅ Delete an event from SQLite & Firestore
    public boolean deleteEvent(int eventId) {
        return dbHelper.deleteEvent(eventId);
    }

    // ✅ Filter events by date range
    public List<String> filterEventsByDateRange(String startDate, String endDate) {
        return dbHelper.getEventsByDateRange(startDate, endDate);
    }

    // ✅ Retrieve an event ID based on its position in the list
    public int getEventIdByPosition(int position) {
        return dbHelper.getEventIdByPosition(position);
    }
}
