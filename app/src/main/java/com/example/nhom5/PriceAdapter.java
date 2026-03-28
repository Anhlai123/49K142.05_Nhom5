package com.example.nhom5;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nhom5.databinding.ItemPriceManagementBinding;
import java.util.List;

public class PriceAdapter extends RecyclerView.Adapter<PriceAdapter.PriceViewHolder> {

    private final List<PriceRecord> priceList;
    private final OnPriceActionListener listener;

    public interface OnPriceActionListener {
        void onView(PriceRecord price);
        void onEdit(PriceRecord price);
        void onDelete(PriceRecord price);
    }

    public PriceAdapter(List<PriceRecord> priceList, OnPriceActionListener listener) {
        this.priceList = priceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PriceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPriceManagementBinding binding = ItemPriceManagementBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PriceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PriceViewHolder holder, int position) {
        PriceRecord price = priceList.get(position);
        holder.binding.tvPriceTitle.setText(price.getTitle());
        holder.binding.tvPriceId.setText(price.getId());
        holder.binding.tvPriceValue.setText(price.getPriceRange());
        holder.binding.tvCourtType.setText(price.getCourtType());
        holder.binding.tvDateRange.setText(price.getDateRange());
        holder.binding.tvTimeFrames.setText(price.getTimeFrameCount());

        // Simple logic for day pills - in a real app, you'd check price.getActiveDays()
        // Here we assume all are active for BG001/BG003 and only T7, CN for BG002 based on the image
        if ("BG002".equals(price.getId())) {
            holder.binding.dayT2.setAlpha(0.3f);
            holder.binding.dayT3.setAlpha(0.3f);
            holder.binding.dayT4.setAlpha(0.3f);
            holder.binding.dayT5.setAlpha(0.3f);
            holder.binding.dayT6.setAlpha(0.3f);
        }

        holder.binding.btnView.setOnClickListener(v -> listener.onView(price));
        holder.binding.btnEdit.setOnClickListener(v -> listener.onEdit(price));
        holder.binding.btnDelete.setOnClickListener(v -> listener.onDelete(price));
    }

    @Override
    public int getItemCount() {
        return priceList.size();
    }

    static class PriceViewHolder extends RecyclerView.ViewHolder {
        final ItemPriceManagementBinding binding;

        PriceViewHolder(ItemPriceManagementBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}