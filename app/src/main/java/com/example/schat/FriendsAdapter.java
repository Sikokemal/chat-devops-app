package com.example.schat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {
    private Context context;
    private List<User> userList;
    private FirebaseUser currentUser;

    public FriendsAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userName.setText(user.getUserName());

        if (user.getProfilePic() != null && !user.getProfilePic().isEmpty()) {
            Glide.with(context).load(user.getProfilePic()).into(holder.userImg);
        } else {
            holder.userImg.setImageResource(R.drawable.person);
        }

        holder.btnAddFriend.setOnClickListener(view -> {
            DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference().child("friends");

            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String friendUserId = user.getUserId();

            if (!currentUserId.equals(friendUserId)) {
                friendsRef.child(currentUserId).child(friendUserId).setValue(true)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Добавлено в друзья", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });


    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userImg;
        TextView userName;
        Button btnAddFriend;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userImg = itemView.findViewById(R.id.userImg);
            userName = itemView.findViewById(R.id.userName);
            btnAddFriend = itemView.findViewById(R.id.btnAddFriend);
        }
    }
}
