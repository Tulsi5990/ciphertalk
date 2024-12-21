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
    String reciverimg, reciverUid, reciverName, SenderUID;
    CircleImageView profile;
    TextView reciverNName;
    FirebaseDatabase database;
    FirebaseAuth firebaseAuth;
    public static String senderImg;
    public static String reciverIImg;
    CardView sendbtn;
    EditText textmsg;
    String senderRoom, reciverRoom;
    RecyclerView messageAdpter;
    ArrayList<msgModelclass> messagesArrayList;
    messagesAdpter mmessagesAdpter;
    private CRYSTALS_KYBER kyber;

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
        setContentView(R.layout.activity_chatwindo);
        getSupportActionBar().hide();
        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        kyber = new CRYSTALS_KYBER(256, 3, 3329);

        reciverName = getIntent().getStringExtra("nameeee");
        reciverimg = getIntent().getStringExtra("reciverImg");
        reciverUid = getIntent().getStringExtra("uid");

        messagesArrayList = new ArrayList<>();

        sendbtn = findViewById(R.id.sendbtnn);
        textmsg = findViewById(R.id.textmsg);
        reciverNName = findViewById(R.id.recivername);
        profile = findViewById(R.id.profileimgg);
        messageAdpter = findViewById(R.id.msgadpter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageAdpter.setLayoutManager(linearLayoutManager);
        mmessagesAdpter = new messagesAdpter(chatwindo.this, messagesArrayList);
        messageAdpter.setAdapter(mmessagesAdpter);

        Picasso.get().load(reciverimg).into(profile);
        reciverNName.setText(reciverName);

        SenderUID = firebaseAuth.getUid();
        senderRoom = SenderUID + reciverUid;
        reciverRoom = reciverUid + SenderUID;

        DatabaseReference reference = database.getReference().child("user").child(firebaseAuth.getUid());
        DatabaseReference chatreference = database.getReference().child("chats").child(senderRoom).child("messages");

        chatreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    msgModelclass messages = dataSnapshot.getValue(msgModelclass.class);

                    // Skip decryption if the message was sent by the current user
                    if (messages.getSenderid().equals(SenderUID)) {
                        Log.d("ChatWindow", "Message sent by current user. Skipping decryption.");
                        messagesArrayList.add(messages);
                        continue;
                    }

                    try {
                        byte[] privateKey = retrievePrivateKey();
                        Rq[] privateKeyRqArray=kyber.convertBytesToRqArray(privateKey);
                        byte[] uBytesArray = listToByteArray(messages.getU());
                        byte[] vBytesArray = listToByteArray(messages.getV());



                        Rq[] uRqArray = kyber.convertBytesToRqArray(uBytesArray);
                        Rq vRq = kyber.convertBytesToRq(vBytesArray);


                        byte[] combinedKeyAndIV = kyber.decapsulate(privateKeyRqArray, uRqArray, vRq);
                        Log.d("Decryption", "keyAndIV (Base64): " + Base64.encodeToString(combinedKeyAndIV, Base64.DEFAULT));
                        Log.d("Decryption", "keyAndIV  len(Base64): " + combinedKeyAndIV.length);


                        byte[] decryptedSessionKeyBytes = Arrays.copyOfRange(combinedKeyAndIV, 0, 32);
                        byte[] decryptedSessionIV = Arrays.copyOfRange(combinedKeyAndIV, 32, 48);
                        Log.d("Decryption", "Session IV (Base64): " + Base64.encodeToString(decryptedSessionIV, Base64.DEFAULT));
                        Log.d("Decryption", "Session IV len (Base64): " + decryptedSessionIV.length);

                        Log.d("Decryption", "Session Key (Base64): " + Base64.encodeToString(decryptedSessionKeyBytes, Base64.DEFAULT));
                        Log.d("Decryption", "Session Key len (Base64): " + decryptedSessionKeyBytes.length);
                        SecretKey sessionKey = new SecretKeySpec(decryptedSessionKeyBytes, "AES");

                        String decryptedMessage = AESHelper.decrypt(messages.getMessage(), sessionKey, decryptedSessionIV);
                        messages.setMessage(decryptedMessage);

                    } catch (Exception e) {
                        Log.e("ChatWindow", "Decryption failed: " + e.getMessage());
                    }

                    messagesArrayList.add(messages);
                }
                mmessagesAdpter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChatWindow", "Database error: " + error.getMessage());
            }
        });

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

        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = textmsg.getText().toString();
                if (message.isEmpty()) {
                    Toast.makeText(chatwindo.this, "Enter message first", Toast.LENGTH_SHORT).show();
                    return;
                }
                textmsg.setText("");

                SecretKey sessionKey;
                byte[] sessionKeyBytes;
                byte[] sessionIV;

                try {
                    sessionKey = AESHelper.generateKey();
                    sessionKeyBytes = sessionKey.getEncoded();
                    sessionIV = AESHelper.generateIV();
                    Log.d("Encryption", "Session IV (Base64): " + Base64.encodeToString(sessionIV, Base64.DEFAULT));
                    Log.d("Encryption", "Session Key (Base64): " + Base64.encodeToString(sessionKey.getEncoded(), Base64.DEFAULT));
                    Log.d("Encryption", "Session IV len (Base64): " + sessionIV.length);
                    Log.d("Encryption", "Session Key len (Base64): " + sessionKeyBytes.length);

                } catch (Exception e) {
                    Log.e("ChatWindow", "Key generation failed: " + e.getMessage());
                    Toast.makeText(chatwindo.this, "Failed to generate keys.", Toast.LENGTH_SHORT).show();
                    return;
                }

                database.getReference().child("user").child(reciverUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String publicKeyJson = snapshot.child("publicKeyJson").getValue(String.class);
                        String matrixAJson = snapshot.child("matrixAJson").getValue(String.class);
                        Gson gson = new Gson();
                        List<Rq> publicKeyList = gson.fromJson(publicKeyJson, new com.google.gson.reflect.TypeToken<List<Rq>>(){}.getType());
                        List<List<Rq>> matrixAList = gson.fromJson(matrixAJson, new com.google.gson.reflect.TypeToken<List<List<Rq>>>(){}.getType());
                        //Rq[] publicKeyArray = gson.fromJson(publicKeyJson, Rq[].class); // Deserialize publicKeyJson to an array of Rq
                        //Rq[][] matrixAArray = gson.fromJson(matrixAJson, Rq[][].class); // Deserialize matrixAJson to a 2D array of Rq
                        Rq[] publicKeyArray = publicKeyList.toArray(new Rq[0]);
                        Rq[][] matrixAArray = new Rq[matrixAList.size()][];
                        for (int i = 0; i < matrixAList.size(); i++) {
                            matrixAArray[i] = matrixAList.get(i).toArray(new Rq[0]);
                        }
                        //byte[] publicKey = new Gson().fromJson(publicKeyJson, byte[].class);
                        //byte[][] matrixA = new Gson().fromJson(matrixAJson, byte[][].class);

                        byte[] keyAndIV = new byte[sessionKeyBytes.length + sessionIV.length];
//                        System.arraycopy(sessionKey.getEncoded(), 0, keyAndIV, 0, sessionKey.getEncoded().length);
//                        System.arraycopy(sessionIV, 0, keyAndIV, sessionKey.getEncoded().length, sessionIV.length);
                        System.arraycopy(sessionKeyBytes, 0, keyAndIV, 0, 32);  // Copy session key into first 32 bytes of keyAndIV
                        System.arraycopy(sessionIV, 0, keyAndIV, 32, 16);               // Copy IV into the next 16 bytes of keyAndIV
                        Log.d("Encryption", "keyAndIV (Base64): " + Base64.encodeToString(keyAndIV, Base64.DEFAULT));
                        Log.d("Encryption", "keyAndIV len(Base64): " + keyAndIV.length);

// Convert byte[] publicKey and matrixA to Rq[] or Rq[][] as needed
                        //Rq[] publicKeyRqArray = kyber.convertBytesToRqArray(publicKey);
                        //Rq[][] matrixARqArray = new Rq[matrixA.length][];
//                        for (int i = 0; i < matrixA.length; i++) {
//                            matrixARqArray[i] = kyber.convertBytesToRqArray(matrixA[i]);
//                        }

// Prepare encapsulation
                            EncapsulationResult encapsulationResult = kyber.encapsulate(publicKeyArray, matrixAArray, keyAndIV);

// Get byte[] from encapsulation results for storage/transmission
                        byte[] uBytes = encapsulationResult.getU(); // getU() will convert Rq[] to byte[]
                        byte[] vBytes = encapsulationResult.getV(); // getV() will convert Rq to byte[]
                        List<Integer> uList = new ArrayList<>();
                        for (byte b : uBytes) {
                            uList.add((int) b);
                        }

                        List<Integer> vList = new ArrayList<>();
                        for (byte b : vBytes) {
                            vList.add((int) b);
                        }

                        try {
                            String encryptedMessage = AESHelper.encrypt(message, sessionKey, sessionIV);
                            Date date = new Date();
                            msgModelclass messages = new msgModelclass(encryptedMessage, SenderUID, date.getTime(), uList, vList,message);
                            //messages.setCipherText(encryptedMessage);
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
