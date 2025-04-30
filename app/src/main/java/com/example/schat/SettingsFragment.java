package com.example.schat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingsFragment extends Fragment {

    FirebaseAuth mAuth;
    FirebaseDatabase fDb;
    FirebaseStorage storage;
    ImageView userImg;
    TextView userName, userAbout;
    Button update;
    ToolDotProgress progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        userImg = view.findViewById(R.id.userImg);
        userName = view.findViewById(R.id.userName);
        userAbout = view.findViewById(R.id.userAbout);
        update = view.findViewById(R.id.updateProf);
        progress = view.findViewById(R.id.dots_progress);

        progress.setVisibility(View.VISIBLE);

        mAuth = FirebaseAuth.getInstance();
        fDb = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        // Загрузка данных о пользователе
        fDb.getReference().child("users").child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progress.setVisibility(View.GONE);
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            userName.setText(user.getUserName());
                            userAbout.setText(user.getUserAbout());
                            // Проверка, если изображение профиля пустое или отсутствует
                            if (user.getProfilePic() != null && !user.getProfilePic().isEmpty()) {
                                Picasso.get().load(user.getProfilePic()).into(userImg);
                            } else {
                                // Если изображения нет, можно установить изображение по умолчанию
                                userImg.setImageResource(R.drawable.done_all);  // Используйте свой ресурс изображения по умолчанию
                            }
                        } else {
                            Toast.makeText(getContext(), "User data not found.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error loading user data.", Toast.LENGTH_SHORT).show();
                    }
                });

        // Обработчик клика по изображению профиля
        userImg.setOnClickListener(view1 -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 25);
        });

        // Обработчик обновления профиля
        update.setOnClickListener(view1 -> {
            String name = userName.getText().toString();
            String about = userAbout.getText().toString();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "UserName is required!", Toast.LENGTH_SHORT).show();
            } else {
                HashMap<String, Object> map = new HashMap<>();
                map.put("userName", name);
                map.put("userAbout", about);
                fDb.getReference().child("users").child(FirebaseAuth.getInstance().getUid()).updateChildren(map)
                        .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show());
            }
        });

        return view;
    }

    // Обработка результата выбора изображения
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && data.getData() != null) {
            Uri uri = data.getData();
            userImg.setImageURI(uri);

            final StorageReference reference = storage.getReference()
                    .child("profile_pic")
                    .child(FirebaseAuth.getInstance().getUid());

            // Загрузка изображения в Firebase Storage
            reference.putFile(uri).addOnSuccessListener(taskSnapshot -> reference.getDownloadUrl().addOnSuccessListener(uri1 -> {
                        fDb.getReference().child("users")
                                .child(FirebaseAuth.getInstance().getUid()).child("profilePic")
                                .setValue(uri1.toString())
                                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile picture updated successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update profile picture", Toast.LENGTH_SHORT).show());
                    }))
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show());
        }
    }
}
