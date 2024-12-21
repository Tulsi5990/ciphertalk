package com.av.avmessenger;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;
import kyber.CRYSTALS_KYBER;
import kyber.CRYSTALS_KYBER.EncapsulationResult;
import kyber.Rq;

public class chatwindo extends AppCompatActivity {
    // Declaring variables for various UI components and logic
    String reciverimg, reciverUid, reciverName, SenderUID;
    CircleImageView profile; // UI component to display the profile image
    TextView reciverNName;   // UI component to display receiver's name
    FirebaseDatabase database; // Firebase database instance
    FirebaseAuth firebaseAuth; // Firebase authentication instance
    public static String senderImg; // Sender's image URL
    public static String reciverIImg; // Receiver's image URL
    CardView sendbtn; // Send button in the chat
    EditText textmsg; // Input field for typing messages
    String senderRoom, reciverRoom; // Unique chat room identifiers for sender and receiver
    RecyclerView messageAdpter; // RecyclerView for displaying chat messages
    ArrayList<msgModelclass> messagesArrayList; // List of messages
    messagesAdpter mmessagesAdpter; // Adapter for RecyclerView
    private CRYSTALS_KYBER kyber; // Instance of the CRYSTALS-KYBER encryption algorithm

    // Utility method to convert a List of Integers to a byte array
    public static byte[] listToByteArray(List<Integer> list) {
        byte[] byteArray = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            byteArray[i] = list.get(i).byteValue();
        }
        return byteArray;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatwindo); // Set the layout for the activity
        getSupportActionBar().hide(); // Hide the action bar
        database = FirebaseDatabase.getInstance(); // Initialize Firebase database instance
        firebaseAuth = FirebaseAuth.getInstance(); // Initialize Firebase authentication instance
        kyber = new CRYSTALS_KYBER(256, 3, 3329); // Initialize CRYSTALS-KYBER with specific parameters

        // Retrieve data from the Intent
        reciverName = getIntent().getStringExtra("nameeee");
        reciverimg = getIntent().getStringExtra("reciverImg");
        reciverUid = getIntent().getStringExtra("uid");

        messagesArrayList = new ArrayList<>(); // Initialize the messages list

        // Initialize UI components
        sendbtn = findViewById(R.id.sendbtnn);
        textmsg = findViewById(R.id.textmsg);
        reciverNName = findViewById(R.id.recivername);
        profile = findViewById(R.id.profileimgg);
        messageAdpter = findViewById(R.id.msgadpter);

        // Set up the RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true); // Stack messages from the end
        messageAdpter.setLayoutManager(linearLayoutManager);
        mmessagesAdpter = new messagesAdpter(chatwindo.this, messagesArrayList);
        messageAdpter.setAdapter(mmessagesAdpter);

        // Load receiver's profile image using Picasso library
        Picasso.get().load(reciverimg).into(profile);
        reciverNName.setText(reciverName); // Set receiver's name in the TextView

        SenderUID = firebaseAuth.getUid(); // Get the current user's UID
        senderRoom = SenderUID + reciverUid; // Create a unique room ID for sender
        reciverRoom = reciverUid + SenderUID; // Create a unique room ID for receiver

        // Database references for user and chat messages
        DatabaseReference reference = database.getReference().child("user").child(firebaseAuth.getUid());
        DatabaseReference chatreference = database.getReference().child("chats").child(senderRoom).child("messages");

        // Listen for changes in the chat messages
        chatreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear(); // Clear the messages list
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    msgModelclass messages = dataSnapshot.getValue(msgModelclass.class); // Deserialize message

                    // Skip decryption if the message was sent by the current user
                    if (messages.getSenderid().equals(SenderUID)) {
                        Log.d("ChatWindow", "Message sent by current user. Skipping decryption.");
                        messagesArrayList.add(messages);
                        continue;
                    }

                    try {
                        // Retrieve private key from local storage
                        byte[] privateKey = retrievePrivateKey();
                        Rq[] privateKeyRqArray = kyber.convertBytesToRqArray(privateKey);

                        // Convert message components back to Rq format
                        byte[] uBytesArray = listToByteArray(messages.getU());
                        byte[] vBytesArray = listToByteArray(messages.getV());

                        Rq[] uRqArray = kyber.convertBytesToRqArray(uBytesArray);
                        Rq vRq = kyber.convertBytesToRq(vBytesArray);

                        // Perform decapsulation to retrieve session key and IV
                        byte[] combinedKeyAndIV = kyber.decapsulate(privateKeyRqArray, uRqArray, vRq);
                        Log.d("Decryption", "keyAndIV (Base64): " + Base64.encodeToString(combinedKeyAndIV, Base64.DEFAULT));

                        // Split combined key and IV into separate arrays
                        byte[] decryptedSessionKeyBytes = Arrays.copyOfRange(combinedKeyAndIV, 0, 32);
                        byte[] decryptedSessionIV = Arrays.copyOfRange(combinedKeyAndIV, 32, 48);

                        // Log decrypted session key and IV
                        Log.d("Decryption", "Session Key (Base64): " + Base64.encodeToString(decryptedSessionKeyBytes, Base64.DEFAULT));
                        SecretKey sessionKey = new SecretKeySpec(decryptedSessionKeyBytes, "AES");

                        // Decrypt the message
                        String decryptedMessage = AESHelper.decrypt(messages.getMessage(), sessionKey, decryptedSessionIV);
                        messages.setMessage(decryptedMessage); // Update message content

                    } catch (Exception e) {
                        Log.e("ChatWindow", "Decryption failed: " + e.getMessage());
                    }

                    messagesArrayList.add(messages); // Add message to the list
                }
                mmessagesAdpter.notifyDataSetChanged(); // Notify adapter to refresh RecyclerView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChatWindow", "Database error: " + error.getMessage());
            }
        });

        // Listen for changes in user profile
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("imageUri").exists() && snapshot.child("imageUri").getValue() != null) {
                    senderImg = snapshot.child("imageUri").getValue().toString();
                } else {
                    senderImg = "";
                    Log.e("ChatWindow", "'imageUri' is null or does not exist");
                }
                reciverIImg = reciverimg;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChatWindow", "Database error: " + error.getMessage());
            }
        });

        // Handle send button click
        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = textmsg.getText().toString(); // Get message text
                if (message.isEmpty()) {
                    Toast.makeText(chatwindo.this, "Enter message first", Toast.LENGTH_SHORT).show();
                    return;
                }
                textmsg.setText(""); // Clear input field

                SecretKey sessionKey;
                byte[] sessionKeyBytes;
                byte[] sessionIV;

                try {
                    // Generate session key and IV for AES encryption
                    sessionKey = AESHelper.generateKey();
                    sessionKeyBytes = sessionKey.getEncoded();
                    sessionIV = AESHelper.generateIV();
                } catch (Exception e) {
                    Log.e("ChatWindow", "Key generation failed: " + e.getMessage());
                    Toast.makeText(chatwindo.this, "Failed to generate keys.", Toast.LENGTH_SHORT).show();
                    return;
                }

                database.getReference().child("user").child(reciverUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Retrieve receiver's public key and matrixA from database
                        String publicKeyJson = snapshot.child("publicKeyJson").getValue(String.class);
                        String matrixAJson = snapshot.child("matrixAJson").getValue(String.class);
                        Gson gson = new Gson();
                        List<Rq> publicKeyList = gson.fromJson(publicKeyJson, new com.google.gson.reflect.TypeToken<List<Rq>>(){}.getType());
                        List<List<Rq>> matrixAList = gson.fromJson(matrixAJson, new com.google.gson.reflect.TypeToken<List<List<Rq>>>(){}.getType());
                        Rq[] publicKeyArray = publicKeyList.toArray(new Rq[0]);
                        Rq[][] matrixAArray = new Rq[matrixAList.size()][];
                        for (int i = 0; i < matrixAList.size(); i++) {
                            matrixAArray[i] = matrixAList.get(i).toArray(new Rq[0]);
                        }

                        // Prepare key and IV for encapsulation
                        byte[] keyAndIV = new byte[sessionKeyBytes.length + sessionIV.length];
                        System.arraycopy(sessionKeyBytes, 0, keyAndIV, 0, 32); // Copy session key
                        System.arraycopy(sessionIV, 0, keyAndIV, 32, 16); // Copy IV

                        EncapsulationResult encapsulationResult = kyber.encapsulate(publicKeyArray, matrixAArray, keyAndIV);

                        // Extract encapsulated components
                        byte[] uBytes = encapsulationResult.getU();
                        byte[] vBytes = encapsulationResult.getV();
                        List<Integer> uList = new ArrayList<>();
                        for (byte b : uBytes) {
                            uList.add((int) b);
                        }

                        List<Integer> vList = new ArrayList<>();
                        for (byte b : vBytes) {
                            vList.add((int) b);
                        }

                        try {
                            // Encrypt the message using AES
                            String encryptedMessage = AESHelper.encrypt(message, sessionKey, sessionIV);
                            Date date = new Date();
                            msgModelclass messages = new msgModelclass(encryptedMessage, SenderUID, date.getTime(), uList, vList, message);

                            // Push message to Firebase database
                            database.getReference().child("chats").child(senderRoom).child("messages").push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    database.getReference().child("chats").child(reciverRoom).child("messages").push().setValue(messages);
                                }
                            });
                        } catch (Exception e) {
                            Log.e("ChatWindow", "Encryption failed: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ChatWindow", "Database error: " + error.getMessage());
                    }
                });
            }
        });
    }

    // Retrieve private key from local storage
    private byte[] retrievePrivateKey() {
        SharedPreferences sharedPreferences = getSharedPreferences("KyberKeys", MODE_PRIVATE);
        String privateKeyBase64 = sharedPreferences.getString("KyberPrivateKey", null);
        Log.d("Chatwindow", "Stored private key: " + privateKeyBase64);
        if (privateKeyBase64 != null) {
            return Base64.decode(privateKeyBase64, Base64.DEFAULT);
        } else {
            Log.e("ChatWindow", "Private key not found in local storage");
            return null;
        }
    }
}