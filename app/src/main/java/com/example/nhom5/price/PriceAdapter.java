package com.example.nhom5.price;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nhom5.R;
import com.example.nhom5.databinding.PriceMgmtItemBinding;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PriceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<PriceRecord> priceList;
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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PriceMgmtItemBinding binding = PriceMgmtItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PriceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PriceViewHolder vh = (PriceViewHolder) holder;
        PriceRecord price = priceList.get(position);
        vh.binding.tvPriceTitle.setText(price.getTitle());
        vh.binding.tvPriceId.setText(price.getId());
        vh.binding.tvPriceValue.setText(price.getPriceRange());
        vh.binding.tvCourtType.setText(price.getCourtType());
        vh.binding.tvDateRange.setText(price.getDateRange());
        vh.binding.tvTimeFrames.setText(price.getTimeFrameCount());

        Set<String> activeDays = new HashSet<>(price.getActiveDays());
        bindDayPill(vh.binding.dayT2, activeDays.contains("T2"));
        bindDayPill(vh.binding.dayT3, activeDays.contains("T3"));
        bindDayPill(vh.binding.dayT4, activeDays.contains("T4"));
        bindDayPill(vh.binding.dayT5, activeDays.contains("T5"));
        bindDayPill(vh.binding.dayT6, activeDays.contains("T6"));
        bindDayPill(vh.binding.dayT7, activeDays.contains("T7"));
        bindDayPill(vh.binding.dayCn, activeDays.contains("CN"));

        vh.binding.btnView.setOnClickListener(v -> listener.onView(price));
        vh.binding.btnEdit.setOnClickListener(v -> listener.onEdit(price));
        vh.binding.btnDelete.setOnClickListener(v -> listener.onDelete(price));
    }

    private void bindDayPill(android.widget.TextView view, boolean active) {
        view.setBackgroundResource(active ? R.drawable.bg_pill_active : R.drawable.bg_day_pill_inactive);
        view.setTextColor(ContextCompat.getColor(view.getContext(), active ? R.color.white : R.color.inactive));
    }

    @Override
    public int getItemCount() {
        return priceList.size();
    }

    public void updateData(List<PriceRecord> newList) {
        this.priceList = newList;
        notifyDataSetChanged();
    }

    private static class PriceViewHolder extends RecyclerView.ViewHolder {
        final PriceMgmtItemBinding binding;
        public PriceViewHolder(PriceMgmtItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}