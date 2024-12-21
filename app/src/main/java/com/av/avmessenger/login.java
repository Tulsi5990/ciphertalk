package com.av.avmessenger;

// Import necessary libraries and Firebase authentication services
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {
    // Declare UI components
    TextView logsignup; // TextView for the "Sign up" option
    Button button; // Button for login
    EditText email, password; // Input fields for email and password
    FirebaseAuth auth; // Firebase authentication object
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"; // Email validation pattern
    android.app.ProgressDialog progressDialog; // Progress dialog for loading indication

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Set the layout for login activity

        // Initialize the progress dialog and set its properties
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        // Hide the action bar to give a clean UI look
        getSupportActionBar().hide();

        // Initialize Firebase authentication
        auth = FirebaseAuth.getInstance();

        // Link UI components to their respective IDs in the layout file
        button = findViewById(R.id.logbutton);
        email = findViewById(R.id.editTexLogEmail);
        password = findViewById(R.id.editTextLogPassword);
        logsignup = findViewById(R.id.logsignup);

        // Handle sign-up redirection when the user clicks "Sign up"
        logsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(login.this, registration.class); // Redirect to the registration page
                startActivity(intent); // Start registration activity
                finish(); // Close the login activity
            }
        });

        // Handle login button click event
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get entered email and password
                String Email = email.getText().toString();
                String pass = password.getText().toString();

                // Validate email and password inputs
                if ((TextUtils.isEmpty(Email))) {
                    progressDialog.dismiss(); // Dismiss progress dialog
                    Toast.makeText(login.this, "Enter The Email", Toast.LENGTH_SHORT).show(); // Show error message
                } else if (TextUtils.isEmpty(pass)) {
                    progressDialog.dismiss();
                    Toast.makeText(login.this, "Enter The Password", Toast.LENGTH_SHORT).show();
                } else if (!Email.matches(emailPattern)) {
                    progressDialog.dismiss();
                    email.setError("Give Proper Email Address"); // Invalid email format
                } else if (password.length() < 6) {
                    progressDialog.dismiss();
                    password.setError("More Than Six Characters"); // Password length check
                    Toast.makeText(login.this, "Password Needs To Be Longer Than Six Characters", Toast.LENGTH_SHORT).show();
                } else {
                    // If validation passes, proceed with Firebase email/password sign-in
                    auth.signInWithEmailAndPassword(Email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // If sign-in is successful, redirect to MainActivity
                            if (task.isSuccessful()) {
                                progressDialog.show(); // Show loading indicator
                                try {
                                    Intent intent = new Intent(login.this, MainActivity.class); // Redirect to the main activity
                                    startActivity(intent);
                                    finish(); // Close login activity
                                } catch (Exception e) {
                                    Toast.makeText(login.this, e.getMessage(), Toast.LENGTH_SHORT).show(); // Show error message if any
                                }
                            } else {
                                // If sign-in fails, show the error message from Firebase
                                Toast.makeText(login.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
