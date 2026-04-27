package com.example.nhom5.court;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom5.R;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class CourtAdapter extends RecyclerView.Adapter<CourtAdapter.CourtViewHolder> {

    private List<Court> courtList;
    private OnCourtActionListener listener;

    public interface OnCourtActionListener {
        void onEdit(Court court);
        void onDelete(Court court);
    }

    public CourtAdapter(List<Court> courtList, OnCourtActionListener listener) {
        this.courtList = courtList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.court_mgmt_item, parent, false);
        return new CourtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourtViewHolder holder, int position) {
        Court court = courtList.get(position);
        holder.tvName.setText(court.getName());
        
        // Hiển thị nút sửa xóa trong trang quản lý sân
        holder.layoutActions.setVisibility(View.VISIBLE);
        
        // Ưu tiên hiển thị mã code (SAN001) thay vì ID số (1)
        String displayCode = court.getCode();
        if (displayCode == null || displayCode.trim().isEmpty()) {
            displayCode = "ID: " + court.getId();
        }
        holder.tvId.setText(displayCode);
        
        holder.tvType.setText(court.getType());
        
        String status = court.getStatus();
        
        if (court.isBusy()) {
            holder.tvStatus.setText("Đang bận");
            holder.tvStatus.setBackgroundResource(R.drawable.status_bg_busy);
            holder.tvStatus.setTextColor(Color.parseColor("#E11D48"));
        } else if ("READY".equalsIgnoreCase(status) || "Sẵn sàng".equalsIgnoreCase(status)) {
            holder.tvStatus.setText("Sẵn sàng");
            holder.tvStatus.setBackgroundResource(R.drawable.status_bg_available);
            holder.tvStatus.setTextColor(Color.parseColor("#00A63E"));
        } else if ("MAINTENANCE".equalsIgnoreCase(status) || "Đang bảo trì".equalsIgnoreCase(status)) {
            holder.tvStatus.setText("Đang bảo trì");
            holder.tvStatus.setBackgroundResource(R.drawable.status_bg_cleaning);
            holder.tvStatus.setTextColor(Color.parseColor("#FFA000"));
        } else {
            holder.tvStatus.setText("Ngừng sử dụng");
            holder.tvStatus.setBackgroundResource(R.drawable.status_bg_busy);
            holder.tvStatus.setTextColor(Color.parseColor("#D32F2F"));
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(court);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(court);
        });
    }

    @Override
    public int getItemCount() {
        return courtList != null ? courtList.size() : 0;
    }

    public void updateData(List<Court> newList) {
        this.courtList = newList;
        notifyDataSetChanged();
    }

    static class CourtViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvId, tvType, tvStatus;
        MaterialButton btnEdit, btnDelete;
        LinearLayout layoutActions;

        public CourtViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_court_name);
            tvId = itemView.findViewById(R.id.tv_court_id);
            tvType = itemView.findViewById(R.id.tv_court_type);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            layoutActions = itemView.findViewById(R.id.layout_actions);
        }
    }
}
