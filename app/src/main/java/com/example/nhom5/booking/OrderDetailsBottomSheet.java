package com.example.nhom5.booking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.nhom5.R;
import com.example.nhom5.api.ApiClient;
import com.example.nhom5.Bill.OrderModel;
import com.example.nhom5.databinding.BottomSheetConfirmDeleteCourtBinding;
import com.example.nhom5.databinding.LayoutOrderDetailsBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailsBottomSheet extends BottomSheetDialogFragment {

    private LayoutOrderDetailsBinding binding;

    // orderId: numeric database ID (dùng cho API calls, VD: "5")
    private final String orderId;
    // orderCode: mã đơn hiển thị (VD: "DD00001")
    private final String orderCode;

    private OnOrderUpdatedListener listener;

    public interface OnOrderUpdatedListener {
        void onOrderUpdated();
    }

    public void setOnOrderUpdatedListener(OnOrderUpdatedListener listener) {
        this.listener = listener;
    }

    /**
     * Constructor nhận numeric ID cho API và mã đơn để hiển thị.
     * @param orderId   Numeric database ID (String.valueOf(order.getId()))
     * @param orderCode Mã đơn hiển thị (order.getMaDon())
     */
    public OrderDetailsBottomSheet(String orderId, String orderCode) {
        this.orderId = orderId != null ? orderId : "";
        this.orderCode = (orderCode != null && !orderCode.isEmpty()) ? orderCode : orderId;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutOrderDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hiển thị mã đơn (orderCode) thay vì numeric ID
        binding.tvTitle.setText("Chi tiết đơn đặt: " + orderCode);

        // Load order details from API dùng numeric orderId
        loadOrderDetails();

        // Close buttons
        binding.btnCloseHeader.setOnClickListener(v -> dismiss());
        binding.btnCloseBottom.setOnClickListener(v -> dismiss());

        // Nút xác nhận đơn - gửi đúng giá trị backend mong đợi
        binding.btnConfirm.setOnClickListener(v -> updateOrderStatus("Đã xác nhận"));

        // Nút cập nhật trạng thái
        binding.btnUpdateStatus.setOnClickListener(v -> {
            UpdateStatusBottomSheet updateStatusBottomSheet = new UpdateStatusBottomSheet();
            updateStatusBottomSheet.setOnStatusSelectedListener(this::updateOrderStatus);
            updateStatusBottomSheet.show(getParentFragmentManager(), "update_status");
        });

        // Nút xóa đơn
        binding.btnDelete.setOnClickListener(v -> showDeleteConfirmBottomSheet());
    }

    private void loadOrderDetails() {
        if (orderId.isEmpty()) {
            // Không có ID hợp lệ, hiển thị trạng thái mặc định
            updateStatusUI("Chờ xác nhận");
            return;
        }

        ApiClient.getApiService().getOrderDetail(orderId).enqueue(new Callback<OrderModel>() {
            @Override
            public void onResponse(@NonNull Call<OrderModel> call, @NonNull Response<OrderModel> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    OrderModel order = response.body();
                    populateOrderDetails(order);
                } else {
                    // Lỗi API - hiển thị trạng thái mặc định, KHÔNG crash
                    updateStatusUI("Chờ xác nhận");
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderModel> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                t.printStackTrace();
                // Lỗi mạng - hiển thị trạng thái mặc định, KHÔNG crash
                updateStatusUI("Chờ xác nhận");
            }
        });
    }

    private void populateOrderDetails(OrderModel order) {
        // Hiển thị mã đơn từ order (ma_don), nếu null thì dùng orderCode
        String displayCode = order.getMaDon() != null ? order.getMaDon() : orderCode;
        binding.tvTitle.setText("Chi tiết đơn đặt: " + displayCode);
        binding.tvCustomerName.setText(safe(order.getTenKhachHang()));
        binding.tvPhoneNumber.setText(safe(order.getSoDienThoai()));
        binding.tvStartTime.setText(formatTime(order.getGioBatDau()));
        binding.tvEndTime.setText(formatTime(order.getGioKetThuc()));
        binding.tvCourtType.setText(safe(order.getLoaiSan()));
        binding.tvCourtName.setText(safe(order.getSanApDung()));
        binding.tvDate.setText(safe(order.getNgayDat()));
        binding.tvPayment.setText(safe(order.getThanhToan()));
        binding.tvNotes.setText(safe(order.getGhiChu()));
        binding.tvTotalPrice.setText(formatMoney(order.getTongTien()) + "đ");

        updateStatusUI(order.getTrangThaiDon());
    }

    private void showDeleteConfirmBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        BottomSheetConfirmDeleteCourtBinding sheetBinding = BottomSheetConfirmDeleteCourtBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        sheetBinding.tvTitle.setText("Xác nhận xóa đơn");
        sheetBinding.tvMessage.setText("Bạn có muốn xóa đơn đặt này?");

        sheetBinding.btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnConfirm.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            deleteOrder();
        });

        bottomSheetDialog.show();
    }

    private void updateStatusUI(String status) {
        if (!isAdded() || binding == null) return;

        String displayStatus = status != null ? status : "Chờ xác nhận";
        binding.tvStatus.setText(displayStatus.toUpperCase());

        String normalized = displayStatus.trim().toLowerCase();

        if (normalized.equals("chờ xác nhận")) {
            // 🟠 Cam - Chờ xác nhận
            binding.layoutStatusBanner.setBackgroundResource(R.drawable.bg_banner_pending);
            binding.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
            binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#F97316"));

        } else if (normalized.equals("đã xác nhận")) {
            // 🟢 Xanh lá - Đã xác nhận
            binding.layoutStatusBanner.setBackgroundResource(R.drawable.bg_banner_confirmed);
            binding.tvStatus.setBackgroundResource(R.drawable.bg_status_confirmed);
            binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#16A34A"));

        } else if (normalized.contains("hoàn thành")) {
            // 🔵 Xanh dương - Hoàn thành
            binding.layoutStatusBanner.setBackgroundResource(R.drawable.bg_banner_completed);
            binding.tvStatus.setBackgroundResource(R.drawable.bg_status_completed);
            binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#3B82F6"));

        } else if (normalized.equals("hủy") || normalized.contains("đã hủy")) {
            // 🔴 Đỏ - Hủy
            binding.layoutStatusBanner.setBackgroundResource(R.drawable.bg_banner_cancelled);
            binding.tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
            binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#EF4444"));

        } else {
            // Mặc định - cam
            binding.layoutStatusBanner.setBackgroundResource(R.drawable.bg_banner_pending);
            binding.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
            binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#F97316"));
        }

        // Ẩn nút "Xác nhận" nếu đơn đã được xử lý
        boolean isDone = status != null && (
                status.equalsIgnoreCase("Đã xác nhận") ||
                status.equalsIgnoreCase("Hoàn thành") ||
                status.equalsIgnoreCase("Hủy") ||
                status.equalsIgnoreCase("ĐÃ XÁC NHẬN") ||
                status.equalsIgnoreCase("HOÀN TẤT") ||
                status.equalsIgnoreCase("ĐÃ HỦY")
        );
        binding.btnConfirm.setVisibility(isDone ? View.GONE : View.VISIBLE);
    }

    /**
     * Gửi PATCH lên backend để cập nhật trạng thái đơn.
     * Dùng orderId (numeric) làm path param.
     * newStatus phải khớp với choices backend: "Chờ xác nhận", "Đã xác nhận", "Hoàn thành", "Hủy"
     */
    private void updateOrderStatus(String newStatus) {
        if (orderId.isEmpty()) return;

        OrderModel updateOrder = new OrderModel();
        updateOrder.setTrangThaiDon(newStatus);

        ApiClient.getApiService().updateOrderStatus(orderId, updateOrder).enqueue(new Callback<OrderModel>() {
            @Override
            public void onResponse(@NonNull Call<OrderModel> call, @NonNull Response<OrderModel> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    updateStatusUI(newStatus);
                    if (listener != null) listener.onOrderUpdated();
                } else {
                    // Nếu thất bại vẫn cập nhật UI tạm thời
                    updateStatusUI(newStatus);
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderModel> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                t.printStackTrace();
                updateStatusUI(newStatus);
            }
        });
    }

    private void deleteOrder() {
        if (orderId.isEmpty()) {
            dismiss();
            return;
        }

        ApiClient.getApiService().deleteOrder(orderId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) return;
                dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                t.printStackTrace();
                dismiss();
            }
        });
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String safe(String value) {
        return value != null ? value : "";
    }

    private String formatTime(String time) {
        if (time == null || time.isEmpty()) return "";
        return time.length() >= 5 ? time.substring(0, 5) : time;
    }

    private String formatMoney(String money) {
        if (money == null || money.isEmpty()) return "0";
        try {
            long value = Long.parseLong(money.replace(",", "").replace(".", ""));
            return String.format("%,d", value);
        } catch (Exception e) {
            return money;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
