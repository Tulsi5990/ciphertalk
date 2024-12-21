package com.av.avmessenger;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdpter extends RecyclerView.Adapter<UserAdpter.viewholder> {
    // Context for the activity and the list of users to display
    Context mainActivity;
    ArrayList<Users> usersArrayList;

    // Constructor to initialize the adapter with the activity and the list of users
    public UserAdpter(MainActivity mainActivity, ArrayList<Users> usersArrayList) {
        this.mainActivity = mainActivity;  // Setting the main activity context
        this.usersArrayList = usersArrayList; // Setting the users list
    }

    // This method is called when a new viewholder is created
    @NonNull
    @Override
    public UserAdpter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each individual item in the RecyclerView
        View view = LayoutInflater.from(mainActivity).inflate(R.layout.user_item, parent, false);
        return new viewholder(view);  // Return a new viewholder with the inflated view
    }

    // This method is called to bind data to the viewholder
    @Override
    public void onBindViewHolder(@NonNull UserAdpter.viewholder holder, int position) {
        // Get the current user from the list based on the position
        Users users = usersArrayList.get(position);

        // Set the user details to the corresponding views in the viewholder
        holder.username.setText(users.userName); // Set username in TextView
        holder.userstatus.setText(users.status); // Set user status in TextView

        // Use Picasso library to load the user's profile image into the CircleImageView
        Picasso.get().load(users.profilepic).into(holder.userimg);

        // Set an onClickListener on the itemView (entire row) for each user
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an Intent to open the chat window activity
                Intent intent = new Intent(mainActivity, chatwindo.class);
                intent.putExtra("nameeee", users.getUserName()); // Pass the user's name
                intent.putExtra("reciverImg", users.getProfilepic()); // Pass the user's profile image
                intent.putExtra("uid", users.getUserId()); // Pass the user's unique ID
                mainActivity.startActivity(intent); // Start the chat activity
            }
        });
    }

    // This method returns the total number of items in the RecyclerView
    @Override
    public int getItemCount() {
        return usersArrayList.size(); // Return the size of the users list
    }

    // Inner class that defines the viewholder for each item in the RecyclerView
    public class viewholder extends RecyclerView.ViewHolder {
        CircleImageView userimg; // CircleImageView to display the user's profile image
        TextView username; // TextView to display the user's name
        TextView userstatus; // TextView to display the user's status

        // Constructor to initialize the views
        public viewholder(@NonNull View itemView) {
            super(itemView);
            // Bind the views from the item layout to the corresponding variables
            userimg = itemView.findViewById(R.id.userimg);
            username = itemView.findViewById(R.id.username);
            userstatus = itemView.findViewById(R.id.userstatus);
        }
    }
}
