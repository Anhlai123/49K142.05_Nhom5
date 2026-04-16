package com.example.nhom5.main;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.nhom5.R;
import com.example.nhom5.api.ApiClient;
import com.example.nhom5.Bill.OrderAdapter;
import com.example.nhom5.booking.OrderDetailsBottomSheet;
import com.example.nhom5.databinding.FragmentSecondBinding;
import com.example.nhom5.Bill.OrderModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private OrderAdapter adapter;
    private final List<OrderModel> fullOrderList = new ArrayList<>();
    private String currentFilter = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new OrderAdapter(order -> {
            OrderDetailsBottomSheet bottomSheet = new OrderDetailsBottomSheet(order.getMaDon());
            bottomSheet.show(getParentFragmentManager(), "order_details");
        });

        binding.rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvOrders.setAdapter(adapter);

        binding.filterAll.setOnClickListener(v -> {
            currentFilter = "all";
            updateFilterUI(R.id.filterAll);
            applyFilters();
        });

        binding.filterPending.setOnClickListener(v -> {
            currentFilter = "pending";
            updateFilterUI(R.id.filterPending);
            applyFilters();
        });

        binding.filterConfirmed.setOnClickListener(v -> {
            currentFilter = "confirmed";
            updateFilterUI(R.id.filterConfirmed);
            applyFilters();
        });

        binding.filterAll.setBackgroundResource(R.drawable.bg_pill_active);

        if (binding.getRoot().findViewById(R.id.etSearchOrder) != null) {
            android.widget.EditText etSearch = binding.getRoot().findViewById(R.id.etSearchOrder);
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applyFilters();
                }
            });
        }

        loadOrders();
    }

    private void loadOrders() {
        ApiClient.getApiService().getOrderList().enqueue(new Callback<List<OrderModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<OrderModel>> call, @NonNull Response<List<OrderModel>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    fullOrderList.clear();
                    fullOrderList.addAll(response.body());
                    applyFilters();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<OrderModel>> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void applyFilters() {
        String keyword = "";
        View searchView = binding.getRoot().findViewById(R.id.etSearchOrder);
        if (searchView instanceof android.widget.EditText) {
            keyword = ((android.widget.EditText) searchView).getText().toString().trim().toLowerCase(Locale.ROOT);
        }

        List<OrderModel> filtered = new ArrayList<>();
        for (OrderModel order : fullOrderList) {
            boolean matchStatus = matchStatus(order);
            boolean matchKeyword = matchKeyword(order, keyword);

            if (matchStatus && matchKeyword) {
                filtered.add(order);
            }
        }
        adapter.setData(filtered);
    }

    private boolean matchStatus(OrderModel order) {
        String status = order.getTrangThaiDon() == null ? "" : order.getTrangThaiDon().trim().toLowerCase(Locale.ROOT);

        switch (currentFilter) {
            case "pending":
                return status.contains("chờ");
            case "confirmed":
                return status.equals("đã xác nhận");
            default:
                return true;
        }
    }

    private boolean matchKeyword(OrderModel order, String keyword) {
        if (keyword.isEmpty()) return true;

        return safe(order.getMaDon()).contains(keyword)
                || safe(order.getTenKhachHang()).contains(keyword)
                || safe(order.getSoDienThoai()).contains(keyword);
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private void updateFilterUI(int selectedId) {
        resetFilterStyles();

        if (selectedId == R.id.filterAll) {
            setFilterActive(binding.filterAll);
        } else if (selectedId == R.id.filterPending) {
            setFilterActive(binding.filterPending);
        } else if (selectedId == R.id.filterConfirmed) {
            setFilterActive(binding.filterConfirmed);
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

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}