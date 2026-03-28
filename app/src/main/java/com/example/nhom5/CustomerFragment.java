package com.example.nhom5;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.nhom5.databinding.FragmentCustomersBinding;
import java.util.ArrayList;
import java.util.List;

public class CustomerFragment extends Fragment {

    private FragmentCustomersBinding binding;
    private CustomerViewModel viewModel;
    private CustomerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCustomersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(CustomerViewModel.class);

        binding.rvCustomers.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new CustomerAdapter(new ArrayList<>());
        binding.rvCustomers.setAdapter(adapter);

        viewModel.getCustomers().observe(getViewLifecycleOwner(), customers -> {
            adapter.updateData(customers);
        });

        // Set up the Add button (+)
        binding.btnAddCustomer.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_navigation_customers_to_addCustomerFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public static class Customer {
        String name;
        String id;
        String phone;

        public Customer(String name, String id, String phone) {
            this.name = name;
            this.id = id;
            this.phone = phone;
        }
    }
}
