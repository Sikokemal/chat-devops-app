package com.example.schat;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;

public class ChatAdapter extends RecyclerView.Adapter {

    private static final int SENDER_VIEW_TYPE = 1;
    private static final int RECEIVER_VIEW_TYPE = 2;

    private Context context;
    private ArrayList<Message> messages;

    public ChatAdapter(Context context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_sender, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_receiver, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        String niceDateStr = (String) DateUtils.getRelativeTimeSpanString(
                message.getCreatedAt(), Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS);
        if (holder instanceof SenderViewHolder) {
            SenderViewHolder senderHolder = (SenderViewHolder) holder;

            // Проверка на null и пустоту для URL изображения
            String imageUrl = message.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                senderHolder.senderMsg.setVisibility(View.GONE);
                senderHolder.senderImage.setVisibility(View.VISIBLE);
                Glide.with(context).load(imageUrl).into(senderHolder.senderImage);
            } else {
                senderHolder.senderMsg.setVisibility(View.VISIBLE);
                senderHolder.senderImage.setVisibility(View.GONE);

                // Проверка на null и пустоту для текста сообщения
                String text = message.getText();
                if (text != null && !text.isEmpty()) {
                    senderHolder.senderMsg.setText(text);
                } else {
                    senderHolder.senderMsg.setText(""); // Можно установить пустую строку, если текст null или пустой
                }
            }

            senderHolder.timeSend.setText(niceDateStr);
        } else {
            ReceiverViewHolder receiverHolder = (ReceiverViewHolder) holder;

            // Проверка на null и пустоту для URL изображения
            String imageUrl = message.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                receiverHolder.receiverMsg.setVisibility(View.GONE);
                receiverHolder.receiverImage.setVisibility(View.VISIBLE);
                Glide.with(context).load(imageUrl).into(receiverHolder.receiverImage);
            } else {
                receiverHolder.receiverMsg.setVisibility(View.VISIBLE);
                receiverHolder.receiverImage.setVisibility(View.GONE);

                // Проверка на null и пустоту для текста сообщения
                String text = message.getText();
                if (text != null && !text.isEmpty()) {
                    receiverHolder.receiverMsg.setText(text);
                } else {
                    receiverHolder.receiverMsg.setText(""); // Можно установить пустую строку, если текст null или пустой
                }
            }

            receiverHolder.timeRe.setText(niceDateStr);
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getuId().equals(FirebaseAuth.getInstance().getUid()) ? SENDER_VIEW_TYPE : RECEIVER_VIEW_TYPE;
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView senderMsg, timeSend;
        ImageView senderImage;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg = itemView.findViewById(R.id.msgSend);
            timeSend = itemView.findViewById(R.id.timSend);
            senderImage = itemView.findViewById(R.id.imageSend);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView receiverMsg, timeRe;
        ImageView receiverImage;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverMsg = itemView.findViewById(R.id.msgReceived);
            timeRe = itemView.findViewById(R.id.timReceived);
            receiverImage = itemView.findViewById(R.id.imageSend);
        }
    }
}
