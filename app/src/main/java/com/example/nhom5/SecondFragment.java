package com.example.nhom5;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.nhom5.databinding.FragmentSecondBinding;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Filter click listeners
        binding.filterAll.setOnClickListener(v -> updateFilterUI(v.getId()));
        binding.filterPending.setOnClickListener(v -> updateFilterUI(v.getId()));
        binding.filterConfirmed.setOnClickListener(v -> updateFilterUI(v.getId()));

        // Order card click listeners
        binding.card1.setOnClickListener(v -> showOrderDetails("DDS001"));
        binding.card2.setOnClickListener(v -> showOrderDetails("DDS002"));
        binding.card3.setOnClickListener(v -> showOrderDetails("DDS003"));
        
        // Default state: Show "All" filter
        updateFilterUI(R.id.filterAll);
    }

    private void updateFilterUI(int selectedId) {
        // Reset all buttons to inactive state
        resetFilterStyles();

        // Set the clicked button to active
        if (selectedId == R.id.filterAll) {
            setFilterActive(binding.filterAll);
            showAllOrders();
        } else if (selectedId == R.id.filterPending) {
            setFilterActive(binding.filterPending);
            showPendingOrdersOnly();
        } else if (selectedId == R.id.filterConfirmed) {
            setFilterActive(binding.filterConfirmed);
            showConfirmedOrdersOnly();
        }
    }

    private void resetFilterStyles() {
        binding.filterAll.setBackgroundResource(R.drawable.bg_pill_inactive);
        binding.filterAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_grey));
        
        binding.filterPending.setBackgroundResource(R.drawable.bg_pill_inactive);
        binding.filterPending.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_grey));
        
        binding.filterConfirmed.setBackgroundResource(R.drawable.bg_pill_inactive);
        binding.filterConfirmed.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_grey));
    }

    private void setFilterActive(android.widget.TextView textView) {
        textView.setBackgroundResource(R.drawable.bg_pill_active);
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
    }

    private void showAllOrders() {
        binding.card1.setVisibility(View.VISIBLE);
        binding.card2.setVisibility(View.VISIBLE);
        binding.card3.setVisibility(View.VISIBLE);
    }

    private void showPendingOrdersOnly() {
        binding.card1.setVisibility(View.GONE); // DDS001 is Confirmed in XML
        binding.card2.setVisibility(View.VISIBLE); // DDS002 is Pending in XML
        binding.card3.setVisibility(View.GONE); // DDS003 is Completed in XML
    }
    
    private void showConfirmedOrdersOnly() {
        binding.card1.setVisibility(View.VISIBLE);
        binding.card2.setVisibility(View.GONE);
        binding.card3.setVisibility(View.GONE);
    }

    private void showOrderDetails(String orderId) {
        OrderDetailsBottomSheet bottomSheet = new OrderDetailsBottomSheet(orderId);
        bottomSheet.show(getParentFragmentManager(), "order_details");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
