package com.example.eventtrackapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventtrackapp.controller.EventController;

import java.util.ArrayList;
import java.util.List;

public class EventDisplayActivity extends AppCompatActivity {

    private GridView gridView;
    private Button addEventButton, searchButton, filterButton, logoutButton;
    private EditText searchEventInput, startDateInput, endDateInput;
    private EventController eventController;
    private ArrayList<String> eventList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_display);

        // Initialize UI components
        gridView = findViewById(R.id.event_grid);
        addEventButton = findViewById(R.id.add_event_button);
        searchEventInput = findViewById(R.id.search_event_input);
        startDateInput = findViewById(R.id.start_date_input);
        endDateInput = findViewById(R.id.end_date_input);
        searchButton = findViewById(R.id.search_button);
        filterButton = findViewById(R.id.filter_button);

        eventController = new EventController(this);
        eventList = new ArrayList<>();

        // Initialize adapter before calling loadEventData
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventList);
        gridView.setAdapter(adapter);

        loadEventData();  // Ensure adapter is set before updating data

        addEventButton.setOnClickListener(v -> showEventDialog(null, -1));
        searchButton.setOnClickListener(v -> searchEvent());
        filterButton.setOnClickListener(v -> filterEvents());

        gridView.setOnItemClickListener((parent, view, position, id) -> showEventDialog(eventList.get(position), position));
    }

    private void loadEventData() {
        eventList.clear();
        eventList.addAll(eventController.getAllEvents());

        if (eventList.isEmpty()) {
            Log.d("EVENTS", "⚠️ No events found, list is empty.");
        } else {
            Log.d("EVENTS", "✅ Events loaded: " + eventList.size());
        }

        // Ensure adapter is not null before updating UI
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            Log.e("EventDisplayActivity", "⚠️ Adapter is null when trying to update UI!");
        }
    }

    private void searchEvent() {
        String query = searchEventInput.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Enter an event name to search", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> results = eventController.searchEventByName(query);
        if (results.isEmpty()) {
            Toast.makeText(this, "No events found", Toast.LENGTH_SHORT).show();
        } else {
            eventList.clear();
            eventList.addAll(results);
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void filterEvents() {
        String startDate = startDateInput.getText().toString().trim();
        String endDate = endDateInput.getText().toString().trim();

        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Enter both start and end dates", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> filteredEvents = eventController.filterEventsByDateRange(startDate, endDate);
        if (filteredEvents.isEmpty()) {
            Toast.makeText(this, "No events found in this date range", Toast.LENGTH_SHORT).show();
        } else {
            eventList.clear();
            eventList.addAll(filteredEvents);
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void logout() {
        getSharedPreferences("UserSession", MODE_PRIVATE)
                .edit()
                .putBoolean("isLoggedIn", false)
                .apply();

        Intent intent = new Intent(EventDisplayActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showEventDialog(String eventData, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(eventData == null ? "Add Event" : "Edit Event");

        View viewInflated = getLayoutInflater().inflate(R.layout.dialog_add_event, null);
        EditText inputName = viewInflated.findViewById(R.id.input_event_name);
        EditText inputDate = viewInflated.findViewById(R.id.input_event_date);

        int eventId = -1;
        if (eventData != null) {
            String[] parts = eventData.split(" - ");
            inputName.setText(parts[0]);
            inputDate.setText(parts[1]);
            eventId = eventController.getEventIdByPosition(position);
        }

        builder.setView(viewInflated);
        final int finalEventId = eventId;

        builder.setPositiveButton(eventData == null ? "Add" : "Update", (dialog, which) -> {
            String eventName = inputName.getText().toString();
            String eventDate = inputDate.getText().toString();

            if (eventData == null) {
                if (eventController.addEvent(eventName, eventDate)) {
                    Toast.makeText(EventDisplayActivity.this, "Event added", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EventDisplayActivity.this, SMSNotificationsActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(EventDisplayActivity.this, "Failed to add event", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (eventController.updateEvent(finalEventId, eventName, eventDate)) {
                    Toast.makeText(EventDisplayActivity.this, "Event updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EventDisplayActivity.this, "Failed to update event", Toast.LENGTH_SHORT).show();
                }
            }
            loadEventData();
        });

        builder.setNegativeButton(eventData == null ? "Cancel" : "Delete", (dialog, which) -> {
            if (eventData != null) {
                if (eventController.deleteEvent(finalEventId)) {
                    Toast.makeText(EventDisplayActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
                    loadEventData();
                } else {
                    Toast.makeText(EventDisplayActivity.this, "Failed to delete event", Toast.LENGTH_SHORT).show();
                }
            }
            dialog.dismiss();
        });

        builder.show();
    }
}
