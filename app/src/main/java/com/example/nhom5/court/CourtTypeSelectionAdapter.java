package com.example.nhom5.court;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nhom5.databinding.CourttypeSelectItemBinding;
import com.example.nhom5.models.CourtTypeModel;
import java.util.List;

public class CourtTypeSelectionAdapter extends RecyclerView.Adapter<CourtTypeSelectionAdapter.ViewHolder> {

    private final List<CourtTypeModel> list;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CourtTypeModel item);
    }

    public CourtTypeSelectionAdapter(List<CourtTypeModel> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CourttypeSelectItemBinding binding = CourttypeSelectItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourtTypeModel item = list.get(position);
        holder.binding.tvTypeName.setText(item.getName());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final CourttypeSelectItemBinding binding;

        public ViewHolder(CourttypeSelectItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
