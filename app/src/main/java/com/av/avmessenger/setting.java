package com.av.avmessenger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class setting extends AppCompatActivity {
    // Declare UI components and variables
    ImageView setprofile; // ImageView to display profile picture
    EditText setname, setstatus; // EditText for user name and status
    Button donebut; // Button to save changes
    FirebaseAuth auth; // Firebase Authentication instance
    FirebaseDatabase database; // Firebase Database instance
    FirebaseStorage storage; // Firebase Storage instance
    Uri setImageUri; // URI to hold selected image
    String email, password; // Variables to hold user email and password
    ProgressDialog progressDialog; // ProgressDialog to show loading state

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getSupportActionBar().hide(); // Hide action bar for cleaner UI

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        // Bind UI components to their respective views
        setprofile = findViewById(R.id.settingprofile);
        setname = findViewById(R.id.settingname);
        setstatus = findViewById(R.id.settingstatus);
        donebut = findViewById(R.id.donebutt);

        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving...");
        progressDialog.setCancelable(false);

        // Get user data from Firebase Database
        DatabaseReference reference = database.getReference().child("user").child(auth.getUid());
        StorageReference storageReference = storage.getReference().child("upload").child(auth.getUid());

        // Listen for changes in user data
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve user data from Firebase
                email = snapshot.child("mail").getValue().toString();
                password = snapshot.child("password").getValue().toString();
                String name = snapshot.child("userName").getValue().toString();
                String profile = snapshot.child("profilepic").getValue().toString();
                String status = snapshot.child("status").getValue().toString();

                // Populate UI with the retrieved user data
                setname.setText(name);
                setstatus.setText(status);
                Picasso.get().load(profile).into(setprofile); // Use Picasso to load profile image
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error if needed
            }
        });

        // Set up the profile image click listener
        setprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open image picker to select an image
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
            }
        });

        // Set up the done button click listener to save user data
        donebut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show(); // Show loading dialog while saving data

                String name = setname.getText().toString(); // Get user name from EditText
                String status = setstatus.getText().toString(); // Get user status from EditText

                // Check if a new profile image was selected
                if (setImageUri != null) {
                    // Upload selected image to Firebase Storage
                    storageReference.putFile(setImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            // On upload success, retrieve the image URL
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String finalImageUri = uri.toString(); // Get the final image URI

                                    // Create a Users object with updated data
                                    Users users = new Users(auth.getUid(), name, email, password, finalImageUri, status, "", "");
                                    // Save the data to Firebase Database
                                    reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            // Handle success or failure
                                            progressDialog.dismiss(); // Hide progress dialog
                                            if (task.isSuccessful()) {
                                                Toast.makeText(setting.this, "Data Saved", Toast.LENGTH_SHORT).show();
                                                // Redirect to MainActivity
                                                Intent intent = new Intent(setting.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(setting.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });
                } else {
                    // If no new image, just use the existing one
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String finalImageUri = uri.toString(); // Get the existing image URI
                            // Create a Users object with updated data
                            Users users = new Users(auth.getUid(), name, email, password, finalImageUri, status, "", "");
                            // Save the data to Firebase Database
                            reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // Handle success or failure
                                    progressDialog.dismiss(); // Hide progress dialog
                                    if (task.isSuccessful()) {
                                        Toast.makeText(setting.this, "Data Saved", Toast.LENGTH_SHORT).show();
                                        // Redirect to MainActivity
                                        Intent intent = new Intent(setting.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(setting.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    // Handle image selection result from the gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            if (data != null) {
                setImageUri = data.getData(); // Get the selected image URI
                setprofile.setImageURI(setImageUri); // Set the selected image to the ImageView
            }
        }
    }
}
