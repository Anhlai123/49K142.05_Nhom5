package com.example.nhom5.main;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom5.R;
import com.example.nhom5.court.Court;

import java.util.List;

public class CourtStatusAdapter extends RecyclerView.Adapter<CourtStatusAdapter.ViewHolder> {

    private List<Court> courtList;

    public CourtStatusAdapter(List<Court> courtList) {
        this.courtList = courtList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_court, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Court court = courtList.get(position);
        holder.tvCourtName.setText(court.getName());
        
        // Ưu tiên hiển thị mã code (SAN001) thay vì ID số (1)
        String displayCode = court.getCode();
        if (displayCode == null || displayCode.trim().isEmpty()) {
            displayCode = "ID: " + court.getId();
        }
        holder.tvCourtId.setText(displayCode);
        
        holder.tvCourtType.setText(court.getType());
        
        String status = court.getStatus();
        if ("READY".equalsIgnoreCase(status) || "Sẵn sàng".equalsIgnoreCase(status)) {
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
    }

    @Override
    public int getItemCount() {
        return courtList != null ? courtList.size() : 0;
    }

    public void updateData(List<Court> newCourtList) {
        this.courtList = newCourtList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourtName, tvCourtId, tvCourtType, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourtName = itemView.findViewById(R.id.tv_court_name);
            tvCourtId = itemView.findViewById(R.id.tv_court_id);
            tvCourtType = itemView.findViewById(R.id.tv_court_type);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }
    }
}
