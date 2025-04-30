package com.example.schat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    User user;
    FirebaseAuth mAuth;
    FirebaseDatabase fDB;
    DatabaseReference dbRef;
    private boolean isChatActive = false;

    ArrayList<Message> messages;
    ChatAdapter chatAdapter;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        // Инициализация Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("images");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.arrow_back));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView attachBtn = findViewById(R.id.attachBtn);
        attachBtn.setOnClickListener(v -> openGallery());

        ImageView imageView = findViewById(R.id.userImg);
        TextView userName = findViewById(R.id.userName);
        TextView onlineStatus = findViewById(R.id.onlineStatus);
        TextInputEditText text = findViewById(R.id.msgInput);
        ImageView sendBtn = findViewById(R.id.sendBtn);
        RecyclerView recyclerView = findViewById(R.id.recycler);

        mAuth = FirebaseAuth.getInstance();
        fDB = FirebaseDatabase.getInstance();
        dbRef = fDB.getReference().child("chats");
        messages = new ArrayList<>();

        chatAdapter = new ChatAdapter(this, messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        user = (User) getIntent().getSerializableExtra("user");
        userName.setText(user.getUserName());

        // Загрузка изображения профиля пользователя
        if (!user.getProfilePic().isEmpty()) {
            Picasso.get().load(user.getProfilePic()).into(imageView);
        }

        // Слушаем изменения статуса пользователя
        FirebaseDatabase.getInstance().getReference("users")
                .child(user.getUserId())
                .child("status")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String status = snapshot.getValue(String.class);
                            if ("online".equals(status)) {
                                onlineStatus.setText("online");
                                onlineStatus.setTextColor(ContextCompat.getColor(ChatActivity.this, R.color.bg_white));
                            } else {
                                onlineStatus.setText("offline");
                                onlineStatus.setTextColor(ContextCompat.getColor(ChatActivity.this, R.color.bg));
                            }
                        } else {
                            onlineStatus.setText("offline");
                            onlineStatus.setTextColor(ContextCompat.getColor(ChatActivity.this, R.color.bg));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("UsersAdapter", "Ошибка при получении статуса: " + error.getMessage());
                    }
                });

        String senderId = mAuth.getUid();
        String receiverId = user.getUserId();
        final String senderRoom = senderId + receiverId;
        final String receiverRoom = receiverId + senderId;

        // Подключение к Firebase и отслеживание сообщений
        dbRef.child(senderRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    messages.add(message);

                    if (!message.isRead() && isChatActive) {
                        dataSnapshot.getRef().child("read").setValue(true);
                    }


                }
                chatAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messages.size() - 1); // Прокрутка вниз

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error reading messages: " + error.getMessage());
            }
        });

        // Обработка нажатия на кнопку отправки сообщения
        sendBtn.setOnClickListener(view -> {
            String msg = text.getText().toString();
            long time = new Date().getTime(); // Получаем текущее время

            // Отправка текстового сообщения
            if (!msg.isEmpty()) {
                Message message = new Message(senderId, msg, time);
                text.setText(""); // Очистка поля ввода

                // Отправка сообщения в оба чата
                dbRef.child(senderRoom)
                        .push()
                        .setValue(message)
                        .addOnSuccessListener(unused -> {
                            recyclerView.scrollToPosition(messages.size() - 1); // Прокрутка вниз
                            dbRef.child(receiverRoom)
                                    .push()
                                    .setValue(message);
                        });
            }
            // Отправка изображения
            else if (imageUri != null) {
                uploadImageToFirebase(imageUri);
            } else {
                Toast.makeText(ChatActivity.this, "Введите сообщение или выберите изображение", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Открытие галереи для выбора изображения
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();  // Сохраняем Uri выбранного изображения

            // Показываем изображение в ImageView до отправки
            ImageView previewImage = findViewById(R.id.previewImage);
            previewImage.setVisibility(View.VISIBLE);  // Показываем ImageView
            Picasso.get().load(imageUri).into(previewImage);  // Загружаем изображение в ImageView
        }
    }



    // Открытие галереи
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onResume() {
        super.onResume();
        isChatActive = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isChatActive = false;
    }


    // Загрузка изображения в Firebase Storage
    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("chat_images");
        StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        sendMessage(imageUrl);  // Отправка изображения
                    }).addOnFailureListener(e -> {
                        Log.e("UploadError", "Failed to get download URL: " + e.getMessage());
                        Toast.makeText(ChatActivity.this, "Не удалось получить URL изображения.", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("UploadError", "Failed to upload image: " + e.getMessage());
                    Toast.makeText(ChatActivity.this, "Не удалось загрузить изображение.", Toast.LENGTH_SHORT).show();
                });
    }

    // Отправка сообщения с изображением
    private void sendMessage(String imageUrl) {
        long time = new Date().getTime(); // Текущее время
        String senderId = mAuth.getUid();
        String receiverId = user.getUserId();
        String senderRoom = senderId + receiverId;
        String receiverRoom = receiverId + senderId;

        Message message = new Message(senderId, imageUrl, time, true); // Сообщение с изображением
        dbRef.child(senderRoom)
                .push()
                .setValue(message)
                .addOnSuccessListener(unused -> {
                    dbRef.child(receiverRoom)
                            .push()
                            .setValue(message)
                            .addOnSuccessListener(unused1 -> {
                                // После отправки изображения скрываем предварительный просмотр
                                ImageView previewImage = findViewById(R.id.previewImage);
                                previewImage.setVisibility(View.GONE);
                            });
                });
        imageUri = null;  // Очистка Uri
    }

    // Получение расширения файла
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}
