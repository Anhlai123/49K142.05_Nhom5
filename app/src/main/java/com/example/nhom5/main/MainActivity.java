package com.example.nhom5.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
        // Nạp dữ liệu lần đầu ngay khi app khởi động (nếu đã login)
        updateDrawerUI();
    }

    private void updateDrawerUI() {
        SharedPreferences pref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String username = pref.getString("username", "N/A");
        String fullName = pref.getString("fullName", username);
        String phone = pref.getString("phone", "Chưa cập nhật SĐT");
        String email = pref.getString("email", "admin@system.com");
        String address = pref.getString("address", "Chưa cập nhật địa chỉ");
        String role = pref.getString("role", "customer");

        boolean isManager = "admin".equalsIgnoreCase(username.trim()) 
                         || "admin".equalsIgnoreCase(role.trim()) 
                         || "staff".equalsIgnoreCase(role.trim());

        // Cập nhật thông tin cơ bản
        binding.drawerProfileLayout.tvDrawerUsername.setText(fullName);
        binding.drawerProfileLayout.tvDrawerEmail.setText(email);

        if (isManager) {
            // Chế độ Admin: Căn giữa, hiện Badge, ẩn chi tiết
            binding.drawerProfileLayout.tvRoleBadge.setVisibility(View.VISIBLE);
            binding.drawerProfileLayout.tvRoleBadge.setText(role.toUpperCase());
            binding.drawerProfileLayout.layoutEmailInfo.setVisibility(View.VISIBLE);
            
            binding.drawerProfileLayout.layoutDetailsContainer.setVisibility(View.GONE);
        } else {
            // Chế độ Khách hàng: Hiện chi tiết, ẩn Badge
            binding.drawerProfileLayout.tvRoleBadge.setVisibility(View.GONE);
            binding.drawerProfileLayout.layoutDetailsContainer.setVisibility(View.VISIBLE);
            
            binding.drawerProfileLayout.tvDrawerPhone.setText(phone);
            binding.drawerProfileLayout.tvDrawerAddress.setText(address);
        }

        // Cài đặt Listeners (chỉ cần cài 1 lần hoặc cập nhật lại)
        binding.drawerProfileLayout.btnEditProfile.setOnClickListener(v -> {
            binding.drawerLayout.closeDrawer(GravityCompat.END);
            Navigation.findNavController(this, R.id.nav_host_fragment_content_main).navigate(R.id.profileFragment);
        });

        binding.drawerProfileLayout.btnLogout.setOnClickListener(v -> {
            pref.edit().clear().apply();
            binding.drawerLayout.closeDrawer(GravityCompat.END);
            Navigation.findNavController(this, R.id.nav_host_fragment_content_main).navigate(R.id.loginFragment);
        });
    }

    public void openProfileDrawer() {
        if (binding.drawerLayout != null) {
            updateDrawerUI(); // Cập nhật dữ liệu ngay trước khi mở
            binding.drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    private void setupNavigation() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

        binding.bottomNavigation.setOnItemReselectedListener(item -> {
            navController.popBackStack(item.getItemId(), false);
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (isFinishing()) return;
            applyRolePermissions();

            int id = destination.getId();
            if (id == R.id.loginFragment || id == R.id.forgotPasswordFragment
                || id == R.id.customerDetailFragment || id == R.id.addCustomerFragment 
                || id == R.id.addPriceFragment || id == R.id.updatePriceFragment
                || id == R.id.bookingConfirmationFragment) {
                binding.bottomNavigation.setVisibility(View.GONE);
                if (getSupportActionBar() != null) getSupportActionBar().hide();
            } else {
                binding.bottomNavigation.setVisibility(View.VISIBLE);
                if (getSupportActionBar() != null) getSupportActionBar().show();
            }
        });
    }

    private void applyRolePermissions() {
        SharedPreferences pref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String role = pref.getString("role", "customer"); 
        String username = pref.getString("username", "");
        Menu menu = binding.bottomNavigation.getMenu();
        
        boolean isManager = "admin".equalsIgnoreCase(username.trim()) 
                         || "admin".equalsIgnoreCase(role.trim()) 
                         || "staff".equalsIgnoreCase(role.trim());

        MenuItem customerItem = menu.findItem(R.id.navigation_customers);
        if (customerItem != null) customerItem.setVisible(isManager);
        
        MenuItem orderItem = menu.findItem(R.id.navigation_orders);
        if (orderItem != null) orderItem.setTitle(isManager ? "Quản lý đơn" : "Lịch sử đặt");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDrawerUI(); // Cập nhật lại mỗi khi quay lại màn hình chính
        applyRolePermissions();
    }
}
