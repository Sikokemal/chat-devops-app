package com.example.schat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements HomeFragment.HomeFrag{
    BottomNavigationView bottomNavigationView;
    ViewPager viewPager;
    FirebaseDatabase fDb;
    DatabaseReference dbRef;
    MenuItem prevMenuItem;

    public static boolean isFirstLaunch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isFirstLaunch=true;
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Ошибка получения токена", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    Log.d("FCM", "FCM токен: " + token);
                });

        bottomNavigationView=findViewById(R.id.btmNavigation);
        viewPager=findViewById(R.id.pager);

        fDb=FirebaseDatabase.getInstance();
        dbRef=fDb.getReference("users").child(FirebaseAuth.getInstance().getUid());



        BottomPagerAdapter adapter=new BottomPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

          viewPagerChangeLister();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        viewPager.setCurrentItem(0);
//                        getSupportFragmentManager().beginTransaction().replace(R.id.container,homeFragment).commit();
                        return true;
                    case R.id.people:
                        viewPager.setCurrentItem(1);
//                        getSupportFragmentManager().beginTransaction().replace(R.id.container,usersFragment).commit();
                        return true;
                    case R.id.call:
                        viewPager.setCurrentItem(2);
//                        getSupportFragmentManager().beginTransaction().replace(R.id.container,AddFriendFragment).commit();
                        return true;
                    case R.id.settings:
                        viewPager.setCurrentItem(3);
//                        getSupportFragmentManager().beginTransaction().replace(R.id.container,settingsFragment).commit();
                        return true;
                }
                return false;
            }
        });
        online();
//        checkNetwork();

    }

    @Override
    protected void onStart() {
        super.onStart();
        online();
        CheckNet.checkNetwork(this);
    }

    public void online(){
        HashMap<String,Object> map=new HashMap<>();
        map.put("status","online");
        dbRef.updateChildren(map);
    }
    public void offline(){
        HashMap<String,Object> map=new HashMap<>();
        map.put("status","offline");
        dbRef.updateChildren(map);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        offline();
    }

    @Override
    public void onClick() {
//        UsersFragment usersFragment=new UsersFragment();
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.container,usersFragment).commit();
        viewPager.setCurrentItem(1);
        bottomNavigationView.setSelectedItemId(R.id.people);
    }

    void viewPagerChangeLister(){
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                }
                else
                {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNavigationView.getMenu().getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }
    public class BottomPagerAdapter  extends FragmentPagerAdapter {

        public BottomPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0: return new HomeFragment();
                case 1:return new UsersFragment();
                case 2:return new AddFriendFragment();
                case 3:return new SettingsFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }
    }



}