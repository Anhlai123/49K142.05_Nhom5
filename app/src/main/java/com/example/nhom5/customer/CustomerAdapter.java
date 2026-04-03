package com.example.nhom5.customer;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom5.R;
import com.example.nhom5.databinding.ItemCustomerBinding;
import java.util.ArrayList;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {

    private List<CustomerFragment.Customer> customers;

    public CustomerAdapter(List<CustomerFragment.Customer> customers) {
        this.customers = new ArrayList<>(customers);
    }

    public void updateData(List<CustomerFragment.Customer> newCustomers) {
        this.customers = new ArrayList<>(newCustomers);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCustomerBinding binding = ItemCustomerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CustomerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        CustomerFragment.Customer customer = customers.get(position);
        holder.binding.tvCustomerName.setText(customer.name);
        holder.binding.tvCustomerId.setText(customer.id);
        holder.binding.tvCustomerPhone.setText(customer.phone);

        holder.itemView.setOnClickListener(v -> {
            // For now, we only have details for "Nguyễn Văn A"
            if (customer.name.equals("Nguyễn Văn A")) {
                Navigation.findNavController(v).navigate(R.id.action_navigation_customers_to_customerDetailFragment);
            }
        });

        holder.binding.btnEditCustomer.setOnClickListener(v -> {
            UpdateCustomerBottomSheet bottomSheet = new UpdateCustomerBottomSheet(customer);
            bottomSheet.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), bottomSheet.getTag());
        });
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    static class CustomerViewHolder extends RecyclerView.ViewHolder {
        final ItemCustomerBinding binding;

        public CustomerViewHolder(ItemCustomerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
