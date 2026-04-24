package com.example.nhom5.customer;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.nhom5.R;
import com.example.nhom5.databinding.FragmentCustomersBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerFragment extends Fragment {

    private FragmentCustomersBinding binding;
    private CustomerViewModel viewModel;
    private CustomerAdapter adapter;
    private List<Customer> allCustomers = new ArrayList<>();

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
            allCustomers = customers;
            filterCustomers(binding.etSearch.getText().toString());
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        setupSearch();
        viewModel.loadCustomers();

        binding.btnAddCustomer.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_navigation_customers_to_addCustomerFragment);
        });

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCustomers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterCustomers(String query) {
        if (query.isEmpty()) {
            adapter.updateData(allCustomers);
            return;
        }

        String lowerQuery = query.toLowerCase().trim();
        List<Customer> filteredList = new ArrayList<>();
        for (Customer c : allCustomers) {
            if (c.name.toLowerCase().contains(lowerQuery) || c.id.toLowerCase().contains(lowerQuery)) {
                filteredList.add(c);
            }
        }
        adapter.updateData(filteredList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public static class Customer {
        public String name;
        public String id;
        public String phone;
        public String email;
        public String notes;

        public Customer(String name, String id, String phone) {
            this.name = name;
            this.id = id;
            this.phone = phone;
        }

        public Customer(String name, String id, String phone, String email, String notes) {
            this.name = name;
            this.id = id;
            this.phone = phone;
            this.email = email;
            this.notes = notes;
        }
    }
}
