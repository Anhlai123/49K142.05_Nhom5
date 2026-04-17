package com.example.nhom5.court;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_court, parent, false);
        return new CourtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourtViewHolder holder, int position) {
        Court court = courtList.get(position);
        holder.tvName.setText(court.getName());
        holder.tvId.setText("ID: " + court.getId());
        holder.tvType.setText(court.getType());
        
        String status = court.getStatus();
        
        // Chuyển đổi trạng thái từ Django sang tiếng Việt và đổi màu
        if ("READY".equalsIgnoreCase(status)) {
            holder.tvStatus.setText("Sân trống");
            holder.tvStatus.setBackgroundResource(R.drawable.status_bg_available);
            holder.tvStatus.setTextColor(Color.parseColor("#00A63E"));
        } else if ("MAINTENANCE".equalsIgnoreCase(status)) {
            holder.tvStatus.setText("Bảo trì");
            holder.tvStatus.setBackgroundResource(R.drawable.status_bg_cleaning);
            holder.tvStatus.setTextColor(Color.parseColor("#FFA000"));
        } else {
            holder.tvStatus.setText("Ngừng dùng");
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

        public CourtViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_court_name);
            tvId = itemView.findViewById(R.id.tv_court_id);
            tvType = itemView.findViewById(R.id.tv_court_type);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}