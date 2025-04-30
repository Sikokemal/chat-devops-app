package com.example.schat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddFriendFragment extends Fragment {

    private EditText searchUser;
    private RecyclerView recyclerView;
    private FriendsAdapter friendsAdapter;
    private List<User> userList;
    private FirebaseDatabase fDb;
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public AddFriendFragment() {}

    public static AddFriendFragment newInstance() {
        return new AddFriendFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_friend, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        searchUser = view.findViewById(R.id.searchUser);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        friendsAdapter = new FriendsAdapter(getContext(), userList);  // Инициализация адаптера
        recyclerView.setAdapter(friendsAdapter);  // Теперь можно установить адаптер

        fDb = FirebaseDatabase.getInstance();
        dbRef = fDb.getReference().child("users");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        searchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchForUser(s.toString());
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void searchForUser(String name) {
        if (name.isEmpty()) {
            userList.clear();
            friendsAdapter.notifyDataSetChanged();
            return;
        }

        dbRef.orderByChild("userName").startAt(name).endAt(name + "\uf8ff")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            User foundUser = data.getValue(User.class);
                            if (foundUser != null && !foundUser.getUserId().equals(currentUser.getUid())) {
                                userList.add(foundUser);
                            }
                        }
                        friendsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
