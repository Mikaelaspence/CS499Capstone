package com.example.eventtrackapp.controller;

import android.content.Context;
import com.example.eventtrackapp.DatabaseHelper;

import java.util.HashMap;
import java.util.List;

public class EventController {

    //Database helper instance for interacting with the database
    private DatabaseHelper dbHelper;

    public EventController(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    //Retrieves cached list of events from database
    public HashMap<Integer, String> getEventCache() {
        return dbHelper.getEventCache();
    }

    //Add event
    public boolean addEvent(String eventName, String eventDate) {
        return dbHelper.addEvent(eventName, eventDate);
    }


    //Update event
    public boolean updateEvent(int eventId, String eventName, String eventDate) {
        return dbHelper.updateEvent(eventId, eventName, eventDate);
    }


    //Delete event
    public boolean deleteEvent(int eventId) {
        return dbHelper.deleteEvent(eventId);
    }

    //Filtering by date
    public List<String> filterEventsByDateRange(String startDate, String endDate) {
        return dbHelper.getEventsByDateRange(startDate, endDate);
    }

    //Event retrieved by position
    public int getEventIdByPosition(int position) {
        return dbHelper.getEventIdByPosition(position);
    }
}
