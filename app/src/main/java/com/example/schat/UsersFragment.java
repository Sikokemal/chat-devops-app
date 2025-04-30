package com.example.schat;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class UsersFragment extends Fragment {

    RecyclerView recyclerView;
    FirebaseAuth mAuth;
    FirebaseDatabase fDb;
    FirebaseUser user;
    DatabaseReference dbRef;
    List<User> userList;
    UsersAdapter usersAdapter;
    ToolDotProgress progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        // Найти Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

        // Найти кнопку для добавления друга
        ImageButton btnAddFriend = view.findViewById(R.id.btnAddFriend);

        // Инициализация RecyclerView и индикатора загрузки
        recyclerView = view.findViewById(R.id.recycler);
        progress = view.findViewById(R.id.dots_progress);

        // Показать индикатор загрузки
        progress.setVisibility(View.VISIBLE);

        // Инициализация Firebase
        mAuth = FirebaseAuth.getInstance();
        fDb = FirebaseDatabase.getInstance();
        dbRef = fDb.getReference().child("users");
        user = mAuth.getCurrentUser();
        userList = new ArrayList<>();

        // Получаем пользователей из Firebase
        getUsers();

        // Инициализация адаптера для RecyclerView
        usersAdapter = new UsersAdapter(userList, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(usersAdapter);

        btnAddFriend.setOnClickListener(v -> {
            Log.d("UsersFragment", "Кнопка нажата, пытаемся открыть AddFriendFragment");

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE); // Очистка стека

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, new AddFriendFragment(), "ADD_FRIEND_FRAGMENT");
            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();

            Log.d("UsersFragment", "Фрагмент должен был замениться");
        });




        return view;
    }

    public void getUsers() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Получаем список друзей текущего пользователя
                DatabaseReference friendsRef = fDb.getReference().child("friends").child(user.getUid());
                friendsRef.addValueEventListener(new ValueEventListener() { // <-- Заменил здесь
                    @Override
                    public void onDataChange(@NonNull DataSnapshot friendsSnapshot) {
                        Set<String> friendIds = new HashSet<>();
                        for (DataSnapshot friendSnapshot : friendsSnapshot.getChildren()) {
                            friendIds.add(friendSnapshot.getKey());
                        }

                        userList.clear(); // Очищаем список перед обновлением

                        // Заполняем список друзей
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user1 = dataSnapshot.getValue(User.class);
                            if (user1 != null && user != null) {
                                user1.setUserId(dataSnapshot.getKey());

                                if (!user.getEmail().equalsIgnoreCase(user1.getEmail()) && friendIds.contains(user1.getUserId())) {
                                    user1.setStatus("Friend");
                                    userList.add(user1);
                                }
                            }
                        }

                        usersAdapter.notifyDataSetChanged(); // Обновление адаптера
                        progress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("UsersFragment", "Ошибка при получении данных друзей: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UsersFragment", "Ошибка при получении данных: " + error.getMessage());
            }
        });
    }
}
