package com.example.schat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    Button register;
    TextInputEditText userName, userPassword, cUserPassword;
    TextView btnLogin;

    FirebaseAuth mAuth;
    FirebaseDatabase fDb;
    DatabaseReference dbRef;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        FirebaseAuth.getInstance().setLanguageCode("en");

        register = findViewById(R.id.register);
        userName = findViewById(R.id.userName);
        userPassword = findViewById(R.id.userPassword);
        cUserPassword = findViewById(R.id.userCPassword);
        btnLogin = findViewById(R.id.btnLogin);

        mAuth = FirebaseAuth.getInstance();
        fDb = FirebaseDatabase.getInstance();
        dbRef = fDb.getReference();

        btnLogin.setOnClickListener(view -> startActivity(new Intent(this, LoginActivity.class)));

        register.setOnClickListener(view -> {
            String email = userName.getText().toString();
            String password = userPassword.getText().toString();
            String cPassword = cUserPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty() || cPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(cPassword)) {
                Toast.makeText(this, "Passwords don't match!", Toast.LENGTH_SHORT).show();
            } else {
                register.setEnabled(false);

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            register.setEnabled(true);

                            if (task.isSuccessful()) {
                                user = mAuth.getCurrentUser();
                                String userId = user.getUid();

                                // Создайте объект User с email и статусом
                                User user1 = new User(email, email, "", "online");
                                user1.setUserId(userId);  // Устанавливаем userId

                                // Сохраняем пользователя в Firebase
                                dbRef.child("users").child(userId).setValue(user1)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                // Перенаправление на экран входа
                                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                                Toast.makeText(RegisterActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(RegisterActivity.this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            } else {
                                Toast.makeText(RegisterActivity.this, "Registration failed! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }
}
