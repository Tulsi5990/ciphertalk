package com.av.avmessenger;

// Static imports for accessing sender and receiver image URIs from chatwindo class
import static com.av.avmessenger.chatwindo.reciverIImg;
import static com.av.avmessenger.chatwindo.senderImg;

import android.app.AlertDialog; // For creating alert dialogs
import android.content.Context; // Provides access to application-specific resources
import android.content.DialogInterface; // For handling dialog button clicks
import android.view.LayoutInflater; // For inflating layout XML files
import android.view.View; // Represents UI components
import android.view.ViewGroup; // Used for organizing UI elements in RecyclerView
import android.widget.TextView; // UI element to display text

import androidx.annotation.NonNull; // Ensures that null values are not passed
import androidx.recyclerview.widget.RecyclerView; // RecyclerView for displaying messages

import com.google.firebase.auth.FirebaseAuth; // Firebase authentication for identifying current user
import com.squareup.picasso.Picasso; // Library for loading images from URLs

import java.util.ArrayList; // ArrayList to manage a dynamic list of messages

import de.hdodenhof.circleimageview.CircleImageView; // Custom library for circular image views

public class messagesAdpter extends RecyclerView.Adapter {

    // Variables for context and message list
    Context context; // Application context
    ArrayList<msgModelclass> messagesAdpterArrayList; // List of message objects

    // Constants to differentiate between sender and receiver message types
    int ITEM_SEND = 1; // Message sent by the current user
    int ITEM_RECIVE = 2; // Message received from another user

    // Constructor to initialize the adapter with context and message list
    public messagesAdpter(Context context, ArrayList<msgModelclass> messagesAdpterArrayList) {
        this.context = context; // Store the context
        this.messagesAdpterArrayList = messagesAdpterArrayList; // Store the message list
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the appropriate layout based on the message type
        if (viewType == ITEM_SEND) { // If the message is sent by the current user
            View view = LayoutInflater.from(context).inflate(R.layout.sender_layout, parent, false);
            return new senderVierwHolder(view); // Return a sender view holder
        } else { // If the message is received from another user
            View view = LayoutInflater.from(context).inflate(R.layout.reciver_layout, parent, false);
            return new reciverViewHolder(view); // Return a receiver view holder
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Get the current message object
        msgModelclass messages = messagesAdpterArrayList.get(position);

        // Set up a long-click listener to display a delete confirmation dialog
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // Create and show an AlertDialog to confirm message deletion
                new AlertDialog.Builder(context).setTitle("Delete")
                        .setMessage("Are you sure you want to delete this message?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Action for deleting the message can be added here
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss(); // Dismiss the dialog if "No" is selected
                            }
                        }).show();

                return false; // Return false to allow further processing of the long-click event
            }
        });

        // Bind the message data to the appropriate view holder
        if (holder.getClass() == senderVierwHolder.class) {
            // If the message is sent by the current user
            senderVierwHolder viewHolder = (senderVierwHolder) holder;
            viewHolder.msgtxt.setText(messages.getMessage()); // Set the message text in the sender's view
            if (senderImg != null && !senderImg.isEmpty()) {
                // Load the sender's image using Picasso if the URL is valid
                Picasso.get().load(senderImg).into(viewHolder.circleImageView);
            } else {
                // Load a placeholder image if senderImg is null or empty
                Picasso.get().load(R.drawable.bg).into(viewHolder.circleImageView); // Replace with your placeholder image resource
            }

        } else {
            // If the message is received from another user
            reciverViewHolder viewHolder = (reciverViewHolder) holder;
            viewHolder.msgtxt.setText(messages.getMessage()); // Set the message text in the receiver's view
            Picasso.get().load(reciverIImg).into(viewHolder.circleImageView); // Load the receiver's image using Picasso
        }
    }

    @Override
    public int getItemCount() {
        // Return the total number of messages in the list
        return messagesAdpterArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        // Determine the type of message (sent or received) based on the sender's ID
        msgModelclass messages = messagesAdpterArrayList.get(position);
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messages.getSenderid())) {
            return ITEM_SEND; // Return ITEM_SEND if the current user is the sender
        } else {
            return ITEM_RECIVE; // Return ITEM_RECIVE if the current user is the receiver
        }
    }

    // ViewHolder for sent messages
    class senderVierwHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView; // Circular image view for sender's profile picture
        TextView msgtxt; // TextView for displaying the message text

        public senderVierwHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.profilerggg); // Bind the sender's profile image view
            msgtxt = itemView.findViewById(R.id.msgsendertyp); // Bind the sender's message text view
        }
    }

    // ViewHolder for received messages
    class reciverViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView; // Circular image view for receiver's profile picture
        TextView msgtxt; // TextView for displaying the message text

        public reciverViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.pro); // Bind the receiver's profile image view
            msgtxt = itemView.findViewById(R.id.recivertextset); // Bind the receiver's message text view
        }
    }
}
