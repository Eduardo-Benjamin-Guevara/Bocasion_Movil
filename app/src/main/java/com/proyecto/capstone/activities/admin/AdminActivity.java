package com.proyecto.capstone.activities.admin;

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
import com.proyecto.capstone.activities.admin.fragments.HomeAdminFragment;
import com.proyecto.capstone.activities.admin.fragments.ManageCategoriesFragment;
import com.proyecto.capstone.activities.admin.fragments.ManageItemsFragment;
import com.proyecto.capstone.activities.admin.fragments.ManageUsersFragment;
import com.proyecto.capstone.activities.admin.fragments.SalesFragment;
import com.proyecto.capstone.activities.general.ProfileFragment;
import com.proyecto.capstone.utils.NotificationHelper;

public class AdminActivity extends BaseActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navViewDrawer;
    private ImageButton menuButton;
    private FirebaseAuth mAuth;

    private final Fragment homeAdminFragment = new HomeAdminFragment();
    private final Fragment manageItemsFragment = new ManageItemsFragment();
    private final Fragment manageUsersFragment = new ManageUsersFragment();
    private final Fragment salesFragment = new SalesFragment();
    private final Fragment manageCategoriesFragment = new ManageCategoriesFragment();
    private final Fragment profileFragment = new ProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

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
                selectedFragment = homeAdminFragment;
            } else if (itemId == R.id.nav_manage_items) {
                selectedFragment = manageItemsFragment;
            } else if (itemId == R.id.nav_manage_users) {
                selectedFragment = manageUsersFragment;
            } else if (itemId == R.id.nav_sales) {
                selectedFragment = salesFragment;
            } else if (itemId == R.id.nav_manage_categories) {
                selectedFragment = manageCategoriesFragment;
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
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeAdminFragment).commit();
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