package com.example.nhom5.main;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.nhom5.R;
import com.example.nhom5.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                navController.popBackStack(R.id.navigation_home, false);
                navController.navigate(R.id.navigation_home);
            } else if (itemId == R.id.navigation_orders) {
                navController.popBackStack(R.id.navigation_orders, false);
                navController.navigate(R.id.navigation_orders);
            } else if (itemId == R.id.navigation_schedule) {
                navController.popBackStack(R.id.navigation_schedule, false);
                navController.navigate(R.id.navigation_schedule);
            } else if (itemId == R.id.navigation_customers) {
                navController.popBackStack(R.id.navigation_customers, false);
                navController.navigate(R.id.navigation_customers);
            } else if (itemId == R.id.navigation_more) {
                navController.popBackStack(R.id.navigation_more, false);
                navController.navigate(R.id.navigation_more);
            }
            return true;
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            if (id == R.id.loginFragment || id == R.id.customerDetailFragment || id == R.id.addCustomerFragment) {
                binding.bottomNavigation.setVisibility(View.GONE);
            } else {
                binding.bottomNavigation.setVisibility(View.VISIBLE);
            }
        });
    }
}
