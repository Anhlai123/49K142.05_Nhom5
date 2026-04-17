package com.example.nhom5.main;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.nhom5.R;
import com.example.nhom5.api.ApiClient;
import com.example.nhom5.api.ApiService;
import com.example.nhom5.databinding.ActivityMainBinding;
import com.example.nhom5.models.CourtScheduleResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getApiService();
        
        setupNavigation();
    }

    private void setupNavigation() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        
        // Use the standard way to link BottomNavigationView with NavController
        // This handles backstack, singleTop, and state saving correctly.
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

        // Reset tab backstack when re-clicking the current tab
        binding.bottomNavigation.setOnItemReselectedListener(item -> {
            navController.popBackStack(item.getItemId(), false);
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (isFinishing()) return;
            int id = destination.getId();
            if (id == R.id.loginFragment || id == R.id.customerDetailFragment || id == R.id.addCustomerFragment) {
                binding.bottomNavigation.setVisibility(View.GONE);
            } else {
                binding.bottomNavigation.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadCourtSchedule(String date, int courtTypeId) {
        Call<CourtScheduleResponse> call = apiService.getCourtSchedule(date, courtTypeId);

        call.enqueue(new Callback<CourtScheduleResponse>() {
            @Override
            public void onResponse(Call<CourtScheduleResponse> call, Response<CourtScheduleResponse> response) {
                if (isFinishing()) return;
                if (response.isSuccessful() && response.body() != null) {
                    // Handle success
                } else {
                    Toast.makeText(MainActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CourtScheduleResponse> call, Throwable t) {
                if (isFinishing()) return;
                Toast.makeText(MainActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
