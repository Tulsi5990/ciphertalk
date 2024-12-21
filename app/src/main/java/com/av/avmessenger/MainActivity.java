package com.av.avmessenger;

// Import required libraries and packages

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth; // Firebase Authentication instance for user login/logout
    RecyclerView mainUserRecyclerView; // RecyclerView to display the list of users
    UserAdpter adapter; // Adapter to bind user data to RecyclerView
    FirebaseDatabase database; // Firebase Database instance for accessing the database
    ArrayList<Users> usersArrayList; // List to store user objects retrieved from the database
    ImageView imglogout; // ImageView for the logout button
    ImageView cumbut, setbut; // ImageView for camera and settings buttons

    @SuppressLint("MissingInflatedId") // Suppress lint warnings about missing ID for this layout
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the activity's layout
        getSupportActionBar().hide(); // Hide the default action bar for a cleaner UI

        // Initialize Firebase instances
        database = FirebaseDatabase.getInstance(); // Get an instance of the Firebase Database
        auth = FirebaseAuth.getInstance(); // Get an instance of Firebase Authentication

        // Find UI components by their IDs
        cumbut = findViewById(R.id.camBut); // Camera button
        setbut = findViewById(R.id.settingBut); // Settings button

        // Reference the "user" node in Firebase Realtime Database
        DatabaseReference reference = database.getReference().child("user");

        // Initialize the ArrayList to hold user data
        usersArrayList = new ArrayList<>();

        // Set up the RecyclerView to display users
        mainUserRecyclerView = findViewById(R.id.mainUserRecyclerView); // Bind RecyclerView by ID
        mainUserRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // Set vertical layout for RecyclerView
        adapter = new UserAdpter(MainActivity.this, usersArrayList); // Initialize adapter with the user list
        mainUserRecyclerView.setAdapter(adapter); // Attach the adapter to the RecyclerView

        // Retrieve user data from Firebase and update the RecyclerView
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) { // Loop through each child in the "user" node
                    Users users = dataSnapshot.getValue(Users.class); // Convert snapshot data into a Users object
                    if (users != null) { // Check if the user data is valid
                        usersArrayList.add(users); // Add the user to the ArrayList
                    }
                }
                adapter.notifyDataSetChanged(); // Notify adapter of data changes to refresh the UI
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database read errors (currently left empty)
            }
        });

        // Logout button functionality
        imglogout = findViewById(R.id.logoutimg); // Bind the logout button by ID
        imglogout.setOnClickListener(new View.OnClickListener() { // Set a click listener for the logout button
            @Override
            public void onClick(View v) {
                // Create a dialog to confirm logout
                Dialog dialog = new Dialog(MainActivity.this, R.style.dialoge); // Use custom dialog style
                dialog.setContentView(R.layout.dialog_layout); // Set the layout for the dialog
                Button no, yes; // Declare buttons for "Yes" and "No"
                yes = dialog.findViewById(R.id.yesbnt); // Bind "Yes" button by ID
                no = dialog.findViewById(R.id.nobnt); // Bind "No" button by ID

                // If "Yes" is clicked, log the user out
                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseAuth.getInstance().signOut(); // Sign out the current user
                        Intent intent = new Intent(MainActivity.this, login.class); // Navigate to the login activity
                        startActivity(intent); // Start the login activity
                        finish(); // Close the current activity
                    }
                });

                // If "No" is clicked, dismiss the dialog
                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss(); // Close the dialog
                    }
                });
                dialog.show(); // Display the dialog
            }
        });

        // Settings button functionality
        setbut.setOnClickListener(new View.OnClickListener() { // Set a click listener for the settings button
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, setting.class); // Navigate to the settings activity
                startActivity(intent); // Start the settings activity
            }
        });

        // Camera button functionality
        cumbut.setOnClickListener(new View.OnClickListener() { // Set a click listener for the camera button
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // Launch the camera for capturing images
                startActivityForResult(intent, 10); // Start the camera activity with request code 10
            }
        });

        // Check if the user is not logged in
        if (auth.getCurrentUser() == null) { // If there's no logged-in user
            Intent intent = new Intent(MainActivity.this, login.class); // Navigate to the login activity
            startActivity(intent); // Start the login activity
            finish(); // Close the current activity
        }
    }
}
