package com.example.nhom5.courtype;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nhom5.databinding.ItemCourtTypeBinding;
import java.util.List;

public class CourtTypeAdapter extends RecyclerView.Adapter<CourtTypeAdapter.CourtTypeViewHolder> {

    private List<CourtType> courtTypeList;
    private final OnCourtTypeActionListener listener;

    public interface OnCourtTypeActionListener {
        void onEdit(CourtType courtType);
        void onDelete(CourtType courtType);
    }

    public CourtTypeAdapter(List<CourtType> courtTypeList, OnCourtTypeActionListener listener) {
        this.courtTypeList = courtTypeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourtTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCourtTypeBinding binding = ItemCourtTypeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CourtTypeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CourtTypeViewHolder holder, int position) {
        CourtType courtType = courtTypeList.get(position);
        holder.binding.tvTypeName.setText(courtType.getName());
        holder.binding.tvTypeId.setText(courtType.getId());
        holder.binding.tvStatus.setText(courtType.getStatus());

        holder.binding.btnEdit.setOnClickListener(v -> listener.onEdit(courtType));
        holder.binding.btnDelete.setOnClickListener(v -> listener.onDelete(courtType));
    }

    @Override
    public int getItemCount() {
        return courtTypeList.size();
    }

    public void updateData(List<CourtType> newList) {
        this.courtTypeList = newList;
        notifyDataSetChanged();
    }

    static class CourtTypeViewHolder extends RecyclerView.ViewHolder {
        final ItemCourtTypeBinding binding;

        CourtTypeViewHolder(ItemCourtTypeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}