package com.example.eventtrackapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "eventTrackApp.db";
    private static final int DATABASE_VERSION = 3;  // Updated version

    private static final String TABLE_EVENTS = "events";
    private static final String COLUMN_EVENT_ID = "event_id";
    private static final String COLUMN_EVENT_NAME = "event_name";
    private static final String COLUMN_EVENT_DATE = "event_date";

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    private HashMap<Integer, String> eventCache;  // HashMap for fast lookup
    private FirebaseFirestore firestore;  // Firebase Firestore instance

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        firestore = FirebaseFirestore.getInstance();  // Initialize Firestore
        eventCache = new HashMap<>();
        loadEventCache();
        syncWithFirebase();  // Sync Firestore data when the app starts
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_PASSWORD + " TEXT)";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + " (" +
                COLUMN_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_EVENT_NAME + " TEXT, " +
                COLUMN_EVENT_DATE + " TEXT)";
        db.execSQL(CREATE_EVENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }

    // ✅ Add event to SQLite and Firestore
    public boolean addEvent(String eventName, String eventDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_NAME, eventName);
        values.put(COLUMN_EVENT_DATE, eventDate);

        long result = db.insert(TABLE_EVENTS, null, values);
        db.close();

        if (result != -1) {
            loadEventCache();
            saveEventToFirestore((int) result, eventName, eventDate);
            return true;
        }
        return false;
    }

    // ✅ Save event to Firestore
    private void saveEventToFirestore(int eventId, String eventName, String eventDate) {
        Map<String, Object> event = new HashMap<>();
        event.put("event_id", eventId);
        event.put("event_name", eventName);
        event.put("event_date", eventDate);

        firestore.collection("events").document(String.valueOf(eventId))
                .set(event)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "✅ Event added to Firestore: " + eventName))
                .addOnFailureListener(e -> Log.e("Firestore", "❌ Failed to add event to Firestore", e));
    }

    // ✅ Update event in SQLite and Firestore
    public boolean updateEvent(int eventId, String eventName, String eventDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_NAME, eventName);
        values.put(COLUMN_EVENT_DATE, eventDate);

        int result = db.update(TABLE_EVENTS, values, COLUMN_EVENT_ID + "=?", new String[]{String.valueOf(eventId)});
        db.close();

        if (result > 0) {
            loadEventCache();
            updateEventInFirestore(eventId, eventName, eventDate);
        }

        return result > 0;
    }

    // ✅ Update Firestore when an event is modified
    private void updateEventInFirestore(int eventId, String eventName, String eventDate) {
        Map<String, Object> updatedEvent = new HashMap<>();
        updatedEvent.put("event_name", eventName);
        updatedEvent.put("event_date", eventDate);

        firestore.collection("events").document(String.valueOf(eventId))
                .update(updatedEvent)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "✅ Event updated in Firestore: " + eventName))
                .addOnFailureListener(e -> Log.e("Firestore", "❌ Failed to update event in Firestore", e));
    }

    // ✅ Delete an event from SQLite and Firestore
    public boolean deleteEvent(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_EVENTS, COLUMN_EVENT_ID + "=?", new String[]{String.valueOf(eventId)});
        db.close();

        if (result > 0) {
            eventCache.remove(eventId);  // Remove from cache
            deleteEventFromFirestore(eventId);
            return true;
        }
        return false;
    }

    // ✅ Delete event from Firestore
    private void deleteEventFromFirestore(int eventId) {
        firestore.collection("events").document(String.valueOf(eventId))
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "✅ Event deleted from Firestore: " + eventId))
                .addOnFailureListener(e -> Log.e("Firestore", "❌ Failed to delete event from Firestore", e));
    }

    // ✅ Fetch events within a date range
    public List<String> getEventsByDateRange(String startDate, String endDate) {
        List<String> filteredEvents = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + COLUMN_EVENT_NAME + ", " + COLUMN_EVENT_DATE +
                " FROM " + TABLE_EVENTS +
                " WHERE " + COLUMN_EVENT_DATE + " BETWEEN ? AND ?";

        Cursor cursor = db.rawQuery(query, new String[]{startDate, endDate});

        if (cursor.moveToFirst()) {
            do {
                String eventName = cursor.getString(0);
                String eventDate = cursor.getString(1);
                filteredEvents.add(eventName + " - " + eventDate);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return filteredEvents;
    }

    // ✅ Get event ID by its position in the list
    public int getEventIdByPosition(int position) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_EVENT_ID + " FROM " + TABLE_EVENTS + " LIMIT 1 OFFSET " + position;
        Cursor cursor = db.rawQuery(query, null);

        int eventId = -1;
        if (cursor.moveToFirst()) {
            eventId = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return eventId;
    }

    // ✅ Add a new user to the database
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();

        return result != -1;  // Returns true if insert was successful
    }

    // ✅ Sync Firestore events into SQLite
    public void syncWithFirebase() {
        firestore.collection("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                SQLiteDatabase db = this.getWritableDatabase();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    int eventId = document.getLong("event_id").intValue();
                    String eventName = document.getString("event_name");
                    String eventDate = document.getString("event_date");

                    ContentValues values = new ContentValues();
                    values.put(COLUMN_EVENT_ID, eventId);
                    values.put(COLUMN_EVENT_NAME, eventName);
                    values.put(COLUMN_EVENT_DATE, eventDate);

                    db.insertWithOnConflict(TABLE_EVENTS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                }
                db.close();
                loadEventCache();
                Log.d("Firestore", "✅ Firestore events synced into SQLite");
            } else {
                Log.e("Firestore", "❌ Failed to sync Firestore data", task.getException());
            }
        });
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public HashMap<Integer, String> getEventCache() {
        return eventCache;
    }

    private void loadEventCache() {
        eventCache.clear();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_EVENT_ID + ", " + COLUMN_EVENT_NAME + ", " + COLUMN_EVENT_DATE + " FROM " + TABLE_EVENTS;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                int eventId = cursor.getInt(0);
                String eventName = cursor.getString(1);
                String eventDate = cursor.getString(2);
                eventCache.put(eventId, eventName + " - " + eventDate);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }
}
