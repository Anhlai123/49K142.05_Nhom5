package com.example.nhom5.Bill;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom5.R;
import com.example.nhom5.databinding.ItemOrderBinding;
import com.example.nhom5.Bill.OrderModel;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(OrderModel order);
    }

    private final List<OrderModel> orderList = new ArrayList<>();
    private final OnOrderClickListener listener;

    public OrderAdapter(OnOrderClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<OrderModel> newData) {
        orderList.clear();
        orderList.addAll(newData);
        notifyDataSetChanged();
    }

    public List<OrderModel> getCurrentData() {
        return new ArrayList<>(orderList);
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orderList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderBinding binding;

        public OrderViewHolder(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(OrderModel order, OnOrderClickListener listener) {
            binding.tvOrderCode.setText(order.getMaDon());
            binding.tvCustomerName.setText(order.getTenKhachHang());
            binding.tvPhone.setText(order.getSoDienThoai());
            binding.tvTimeDate.setText(formatTime(order.getGioBatDau()) + " - " + formatTime(order.getGioKetThuc()) + " • " + order.getNgayDat());
            binding.tvCourtType.setText(order.getLoaiSan());
            binding.tvCourtName.setText(order.getSanApDung());
            binding.tvTotalPrice.setText(formatMoney(order.getTongTien()));
            binding.tvStatus.setText(order.getTrangThaiDon() != null ? order.getTrangThaiDon().toUpperCase() : "");

            applyStatusStyle(order.getTrangThaiDon());

            binding.cardRoot.setOnClickListener(v -> listener.onOrderClick(order));
        }

        private void applyStatusStyle(String status) {
            if (status == null) return;

            String normalized = status.trim().toLowerCase();
            if (normalized.contains("xác nhận")) {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                binding.tvStatus.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.white));
            }
            if (normalized.equals("đã xác nhận")) {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_confirmed);
                binding.tvStatus.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.primary));
            }
            if (normalized.contains("hoàn")) {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_completed);
                binding.tvStatus.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.white));
            }
        }

        private String formatTime(String time) {
            if (time == null) return "";
            return time.length() >= 5 ? time.substring(0, 5) : time;
        }

        private String formatMoney(String money) {
            if (money == null || money.isEmpty()) return "0đ";
            try {
                long value = Long.parseLong(money.replace(",", "").replace(".", ""));
                return String.format("%,dđ", value);
            } catch (Exception e) {
                return money + "đ";
            }
        }
    }
}