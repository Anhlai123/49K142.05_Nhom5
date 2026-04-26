package com.example.nhom5.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.nhom5.R;
import com.example.nhom5.api.ApiClient;
import com.example.nhom5.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApiClient.init(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupNavigation();
        setupDrawer();
    }

    private void setupDrawer() {
        SharedPreferences pref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String username = pref.getString("username", "N/A");
        String phone = pref.getString("phone", "");
        String role = pref.getString("role", "customer");

        binding.navViewRight.getHeaderView(0); // If using header
        
        // Populate Drawer Info
        binding.drawerProfileLayout.tvDrawerUsername.setText(username);
        
        // Show phone only for customers or if phone exists
        if (!phone.isEmpty()) {
            binding.drawerProfileLayout.layoutPhone.setVisibility(View.VISIBLE);
            binding.drawerProfileLayout.tvDrawerPhone.setText(phone);
        } else {
            binding.drawerProfileLayout.layoutPhone.setVisibility(View.GONE);
        }

        binding.drawerProfileLayout.btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.apply();
            
            binding.drawerLayout.closeDrawer(GravityCompat.END);
            
            // Navigate to login
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.loginFragment);
        });
    }

    public void openProfileDrawer() {
        if (binding.drawerLayout != null) {
            setupDrawer(); // Refresh data
            binding.drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    private void setupNavigation() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

        // Reset tab backstack when re-clicking the current tab
        binding.bottomNavigation.setOnItemReselectedListener(item -> {
            navController.popBackStack(item.getItemId(), false);
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (isFinishing()) return;
            
            applyRolePermissions();

            int id = destination.getId();
            if (id == R.id.loginFragment || id == R.id.customerDetailFragment || id == R.id.addCustomerFragment 
                || id == R.id.addPriceFragment || id == R.id.updatePriceFragment
                || id == R.id.bookingConfirmationFragment) {
                binding.bottomNavigation.setVisibility(View.GONE);
            } else {
                binding.bottomNavigation.setVisibility(View.VISIBLE);
            }
        });
    }

    private void applyRolePermissions() {
        SharedPreferences pref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String role = pref.getString("role", "customer"); 
        String username = pref.getString("username", "");
        
        Log.d(TAG, "Checking Permissions - User: [" + username + "] Role: [" + role + "]");

        Menu menu = binding.bottomNavigation.getMenu();
        
        // CƠ CHẾ QUYẾT ĐỊNH: Nếu username là admin thì MẶC ĐỊNH là Admin
        boolean isManager = "admin".equalsIgnoreCase(username.trim()) 
                         || "admin".equalsIgnoreCase(role.trim()) 
                         || "staff".equalsIgnoreCase(role.trim());

        if (isManager) {
            // Hiển thị các mục quản lý
            MenuItem customerItem = menu.findItem(R.id.navigation_customers);
            if (customerItem != null) customerItem.setVisible(true);
            
            MenuItem orderItem = menu.findItem(R.id.navigation_orders);
            if (orderItem != null) orderItem.setTitle("Quản lý đơn");
        } else {
            // Ẩn các mục quản lý đối với khách hàng
            MenuItem customerItem = menu.findItem(R.id.navigation_customers);
            if (customerItem != null) customerItem.setVisible(false);
            
            MenuItem orderItem = menu.findItem(R.id.navigation_orders);
            if (orderItem != null) orderItem.setTitle("Lịch sử đặt");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyRolePermissions();
    }
}
