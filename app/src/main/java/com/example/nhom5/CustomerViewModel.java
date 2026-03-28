package com.example.nhom5;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class CustomerViewModel extends ViewModel {
    private final MutableLiveData<List<CustomerFragment.Customer>> customers = new MutableLiveData<>();

    public CustomerViewModel() {
        List<CustomerFragment.Customer> initialList = new ArrayList<>();
        initialList.add(new CustomerFragment.Customer("Nguyễn Văn A", "KH00001", "0901 234 567"));
        initialList.add(new CustomerFragment.Customer("Trần Thị B", "KH00002", "0987 654 321"));
        initialList.add(new CustomerFragment.Customer("Lê Hoàng C", "KH00003", "0912 345 678"));
        customers.setValue(initialList);
    }

    public LiveData<List<CustomerFragment.Customer>> getCustomers() {
        return customers;
    }

    public void addCustomer(CustomerFragment.Customer customer) {
        List<CustomerFragment.Customer> currentList = customers.getValue();
        if (currentList != null) {
            List<CustomerFragment.Customer> newList = new ArrayList<>(currentList);
            newList.add(customer);
            customers.setValue(newList);
        }
    }
}
