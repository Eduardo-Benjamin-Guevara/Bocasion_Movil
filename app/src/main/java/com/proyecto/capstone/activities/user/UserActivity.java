package com.proyecto.capstone.activities.user;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyecto.capstone.R;
import com.proyecto.capstone.activities.BaseActivity;
import com.proyecto.capstone.activities.general.ProfileFragment;
import com.proyecto.capstone.activities.user.fragments.CartFragment;
import com.proyecto.capstone.activities.user.fragments.HistoryFragment;
import com.proyecto.capstone.activities.user.fragments.HomeUserFragment;
import com.proyecto.capstone.activities.user.fragments.MenuFragment;
import com.proyecto.capstone.activities.user.fragments.OrderStatusFragment;
import com.proyecto.capstone.utils.NotificationHelper;

public class UserActivity extends BaseActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navViewDrawer;
    private ImageButton menuButton;
    private FirebaseAuth mAuth;

    private final Fragment homeUserFragment = new HomeUserFragment();
    private final Fragment menuFragment = new MenuFragment();
    private final Fragment cartFragment = new CartFragment();
    private final Fragment orderStatusFragment = new OrderStatusFragment();
    private final Fragment historyFragment = new HistoryFragment();
    private final Fragment profileFragment = new ProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        NotificationHelper.createNotificationChannel(this);

        mAuth = FirebaseAuth.getInstance();

        drawerLayout = findViewById(R.id.drawer_layout);
        navViewDrawer = findViewById(R.id.nav_view_drawer);
        menuButton = findViewById(R.id.menu_button);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        menuButton.setOnClickListener(v -> {
            drawerLayout.openDrawer(navViewDrawer);
        });

        updateNavHeader();

        navViewDrawer.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = homeUserFragment;
            } else if (itemId == R.id.nav_menu) {
                selectedFragment = menuFragment;
            } else if (itemId == R.id.nav_cart) {
                selectedFragment = cartFragment;
            } else if (itemId == R.id.nav_order_status) {
                selectedFragment = orderStatusFragment;
            } else if (itemId == R.id.nav_history) {
                selectedFragment = historyFragment;
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = profileFragment;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                navViewDrawer.setCheckedItem(itemId);
            }
            drawerLayout.closeDrawer(navViewDrawer);
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeUserFragment).commit();
            navViewDrawer.setCheckedItem(R.id.nav_home);
        }
    }

    private void updateNavHeader() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            View headerView = navViewDrawer.getHeaderView(0);
            TextView emailTextView = headerView.findViewById(R.id.nav_header_email);
            TextView usernameTextView = headerView.findViewById(R.id.nav_header_username);

            if (emailTextView != null) {
                emailTextView.setText(currentUser.getEmail());
            }

            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String name = dataSnapshot.child("name").getValue(String.class);
                        if (usernameTextView != null && name != null) {
                            usernameTextView.setText(name);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

}