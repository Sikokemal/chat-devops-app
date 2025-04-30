package com.example.schat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyViewHolder> {

    List<User> userList;
    Context context;

    public UsersAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userName.setText(user.getUserName());

        if (user.getProfilePic() != null && !user.getProfilePic().isEmpty()) {
            Uri uri = Uri.parse(user.getProfilePic());
            Picasso.get().load(uri).error(R.drawable.person).into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.person);
        }

        // Проверка статуса пользователя и обновление UI
        if ("online".equals(user.getStatus())) {
            holder.onlineStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_green));
        } else {
            holder.onlineStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_white));
        }

        FirebaseDatabase.getInstance().getReference("users")
                .child(user.getUserId())
                .child("status")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String status = snapshot.getValue(String.class);
                        if ("online".equals(status)) {
                            holder.onlineStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_green));
                        } else {
                            holder.onlineStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_white));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("UsersAdapter", "Ошибка при получении статуса: " + error.getMessage());
                    }
                });

        // Отображение количества непрочитанных сообщений, но без их автоматического обновления
        FirebaseDatabase.getInstance().getReference("chats")
                .child(FirebaseAuth.getInstance().getUid() + user.getUserId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int unreadCount = 0;
                        String lastMessageText = "";
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            if (message != null) {
                                lastMessageText = message.getText();  // Последнее сообщение
                                if (!message.isRead() && !message.getuId().equals(FirebaseAuth.getInstance().getUid())) {
                                    unreadCount++;
                                }
                            }
                        }

                        // Обновление счетчика непрочитанных сообщений
                        if (unreadCount > 0) {
                            holder.msgCount.setVisibility(View.VISIBLE);
                            holder.msgCount.setText(String.valueOf(unreadCount));
                        } else {
                            holder.msgCount.setVisibility(View.GONE);
                        }

                        // Обновление текста последнего сообщения
                        holder.lastMsg.setText(lastMessageText);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("UsersAdapter", "Ошибка при получении данных чатов: " + error.getMessage());
                    }
                });

        // Переход в чат при клике
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("user", user);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView userName, lastMsg, msgCount, time, onlineStatus;
        ImageView imageView, lastMsgIcon;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            lastMsg = itemView.findViewById(R.id.lastMassage);
            msgCount = itemView.findViewById(R.id.msgCount1);
            time = itemView.findViewById(R.id.time);
            onlineStatus = itemView.findViewById(R.id.onlineStatus2);
            imageView = itemView.findViewById(R.id.userImg);
            lastMsgIcon = itemView.findViewById(R.id.lastMassageIcon);
        }
    }
}
