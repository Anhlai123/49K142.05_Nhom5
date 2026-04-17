package com.example.nhom5.api;

import com.example.nhom5.models.BookingRequest;
import com.example.nhom5.models.BookingResponse;
import com.example.nhom5.models.CourtScheduleResponse;
import com.example.nhom5.models.CourtTypeModel;
import com.example.nhom5.models.CreateCustomerRequest;
import com.example.nhom5.models.CustomerApiModel;
import com.example.nhom5.auth.model.LoginRequest;
import com.example.nhom5.auth.model.LoginResponse;
import com.example.nhom5.auth.model.RegisterRequest;
import com.example.nhom5.auth.model.RegisterResponse;
import com.example.nhom5.court.Court;
import com.google.gson.JsonElement;
import java.util.List;
import com.example.nhom5.Bill.OrderModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @POST("api/login/")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/register/")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    // Lấy danh sách loại sân
    @GET("api/court-types/")
    Call<List<CourtTypeModel>> getCourtTypes();

    // Thêm loại sân mới
    @POST("api/court-types/")
    Call<CourtTypeModel> createCourtType(@Body CourtTypeModel courtType);

    // Lấy danh sách sân
    @GET("api/courts/")
    Call<List<Court>> getCourts();

    // Thêm sân mới
    @POST("api/courts/")
    Call<Court> createCourt(@Body Court court);

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

    @GET("api/QL_DonDat/")
    Call<List<OrderModel>> getOrderList();
}
