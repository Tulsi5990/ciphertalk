package com.av.avmessenger;

// Importing necessary packages and libraries

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kyber.CRYSTALS_KYBER;
import kyber.Rq;

public class registration extends AppCompatActivity {

    // UI elements for registration form
    TextView loginbut;
    EditText rg_username, rg_email, rg_password, rg_repassword;
    Button rg_signup;
    CircleImageView rg_profileImg;

    // Firebase and Storage components
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;

    // To handle user-selected profile image
    Uri imageURI;
    String imageuri;

    // Progress dialog to show feedback during long-running operations
    ProgressDialog progressDialog;

    // Kyber cryptography library for public/private key generation
    private CRYSTALS_KYBER kyber;

    // SharedPreferences keys for storing private keys
    private static final String PREFS_NAME = "KyberKeys";
    private static final String PRIVATE_KEY_PREF = "KyberPrivateKey";

    // Regex pattern for validating email addresses
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize Kyber cryptography instance with predefined parameters
        kyber = new CRYSTALS_KYBER(256, 3, 3329);

        // Initialize UI components and Firebase instances
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Establishing The Account");
        progressDialog.setCancelable(false);
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        // Bind UI elements to variables
        loginbut = findViewById(R.id.loginbut);
        rg_username = findViewById(R.id.rgusername);
        rg_email = findViewById(R.id.rgemail);
        rg_password = findViewById(R.id.rgpassword);
        rg_repassword = findViewById(R.id.rgrepassword);
        rg_profileImg = findViewById(R.id.profilerg0);
        rg_signup = findViewById(R.id.signupbutton);

        // Handle "Login" button click, redirecting to login screen
        loginbut.setOnClickListener(v -> {
            Intent intent = new Intent(registration.this, login.class);
            startActivity(intent);
            finish();
        });

        // Handle "Sign Up" button click
        rg_signup.setOnClickListener(v -> {
            // Collect form input values
            String name = rg_username.getText().toString();
            String email = rg_email.getText().toString();
            String password = rg_password.getText().toString();
            String confirmPassword = rg_repassword.getText().toString();
            String status = "Hey I'm Using This Application";

            // Generate Kyber key pair (public/private keys and matrix A)
            CRYSTALS_KYBER.KeyPair keyPair = kyber.generateKeyPair();

            // Serialize public key and matrix A to JSON using Gson
            Gson gson = new Gson();
            List<Rq> publicKeyList = Arrays.asList(keyPair.publicKey);
            List<List<Rq>> matrixAList = new ArrayList<>();
            for (Rq[] row : keyPair.A) {
                matrixAList.add(Arrays.asList(row));
            }
            String publicKeyJson = gson.toJson(publicKeyList);
            String matrixAJson = gson.toJson(matrixAList);

            // Serialize and store the private key securely
            byte[] privateKeyBytes = kyber.convertRqArrayToBytes(keyPair.secretKey);
            String privateKeyBase64 = Base64.encodeToString(privateKeyBytes, Base64.DEFAULT);
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putString(PRIVATE_KEY_PREF, privateKeyBase64).apply();
            Log.d("Registration", "Stored private key: " + privateKeyBase64);

            // Validate form inputs
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                progressDialog.dismiss();
                Toast.makeText(registration.this, "Please Enter Valid Information", Toast.LENGTH_SHORT).show();
            } else if (!email.matches(emailPattern)) {
                progressDialog.dismiss();
                rg_email.setError("Type A Valid Email Here");
            } else if (password.length() < 6) {
                progressDialog.dismiss();
                rg_password.setError("Password Must Be 6 Characters Or More");
            } else if (!password.equals(confirmPassword)) {
                progressDialog.dismiss();
                rg_password.setError("The Password Doesn't Match");
            } else {
                // Proceed to create a user with Firebase Authentication
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Fetch user ID from Firebase
                        String id = task.getResult().getUser().getUid();
                        DatabaseReference reference = database.getReference().child("user").child(id);
                        StorageReference storageReference = storage.getReference().child("Upload").child(id);

                        // If a profile image is provided, upload it to Firebase Storage
                        if (imageURI != null) {
                            storageReference.putFile(imageURI).addOnCompleteListener(uploadTask -> {
                                if (uploadTask.isSuccessful()) {
                                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                        imageuri = uri.toString();
                                        Users users = new Users(id, name, email, password, imageuri, status, publicKeyJson, matrixAJson);
                                        reference.setValue(users).addOnCompleteListener(databaseTask -> {
                                            if (databaseTask.isSuccessful()) {
                                                progressDialog.show();
                                                Intent intent = new Intent(registration.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(registration.this, "Error in creating the user", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    });
                                }
                            });
                        } else {
                            // Use default profile image if no image is selected
                            imageuri = "https://firebasestorage.googleapis.com/v0/b/av-messenger-dc8f3.appspot.com/o/man.png?alt=media&token=880f431d-9344-45e7-afe4-c2cafe8a5257";
                            Users users = new Users(id, name, email, password, imageuri, status, publicKeyJson, matrixAJson);
                            reference.setValue(users).addOnCompleteListener(databaseTask -> {
                                if (databaseTask.isSuccessful()) {
                                    progressDialog.show();
                                    Intent intent = new Intent(registration.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(registration.this, "Error in creating the user", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(registration.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Handle profile image selection
        rg_profileImg.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
        });
    }

    // Handle image selection result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && data != null) {
            imageURI = data.getData();
            rg_profileImg.setImageURI(imageURI);
        }
    }
}
