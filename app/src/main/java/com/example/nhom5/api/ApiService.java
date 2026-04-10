package com.example.nhom5.api;

import com.example.nhom5.models.BookingRequest;
import com.example.nhom5.models.BookingResponse;
import com.example.nhom5.models.CourtScheduleResponse;
import com.example.nhom5.models.CreateCustomerRequest;
import com.example.nhom5.models.CustomerApiModel;
import com.google.gson.JsonElement;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // Lấy lịch sân
    @GET("api/courts/schedule/")
    Call<CourtScheduleResponse> getCourtSchedule(
        @Query("date") String date,
        @Query("court_type_id") int courtTypeId
    );

    // Đặt sân
    @POST("api/bookings/create_booking/")
    Call<BookingResponse> createBooking(@Body BookingRequest bookingRequest);

    @GET("api/customers/")
    Call<JsonElement> getCustomers();

    @POST("api/customers/")
    Call<CustomerApiModel> createCustomer(@Body CreateCustomerRequest request);
}
