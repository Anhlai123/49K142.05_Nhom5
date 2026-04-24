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
import com.example.nhom5.auth.model.UserDto;
import com.example.nhom5.court.Court;
import com.example.nhom5.models.PriceTableCourtModel;
import com.example.nhom5.models.PriceTableModel;
import com.example.nhom5.models.PriceTableTimeSlotModel;
import com.google.gson.JsonElement;
import java.util.List;
import com.example.nhom5.Bill.OrderModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("api/login/")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/register/")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @GET("api/profile/")
    Call<UserDto> getProfile();

    @PATCH("api/profile/")
    Call<UserDto> updateProfile(@Body UserDto user);

    // Lấy danh sách loại sân
    @GET("api/court-types/")
    Call<List<CourtTypeModel>> getCourtTypes();

    // Thêm loại sân mới
    @POST("api/court-types/")
    Call<CourtTypeModel> createCourtType(@Body CourtTypeModel courtType);

    // Cập nhật loại sân
    @PATCH("api/court-types/{id}/")
    Call<CourtTypeModel> updateCourtType(@Path("id") int id, @Body CourtTypeModel courtType);

    // Xóa loại sân
    @DELETE("api/court-types/{id}/")
    Call<Void> deleteCourtType(@Path("id") int id);

    // Lấy danh sách sân
    @GET("api/courts/")
    Call<List<Court>> getCourts();

    // Thêm sân mới
    @POST("api/courts/")
    Call<Court> createCourt(@Body Court court);

    // Cập nhật sân
    @PATCH("api/courts/{id}/")
    Call<Court> updateCourt(@Path("id") int id, @Body Court court);

    // Xóa sân
    @DELETE("api/courts/{id}/")
    Call<Void> deleteCourt(@Path("id") int id);

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

    @PATCH("api/customers/{id}/")
    Call<CustomerApiModel> updateCustomer(@Path("id") int id, @Body CreateCustomerRequest request);

    @GET("api/QL_DonDat/")
    Call<List<OrderModel>> getOrderList();

    // Price Table APIs
    @GET("api/price-tables/")
    Call<List<PriceTableModel>> getPriceTables();

    @POST("api/price-tables/")
    Call<PriceTableModel> createPriceTable(@Body PriceTableModel priceTable);

    @PATCH("api/price-tables/{id}/")
    Call<PriceTableModel> updatePriceTable(@Path("id") int id, @Body PriceTableModel priceTable);

    @DELETE("api/price-tables/{id}/")
    Call<Void> deletePriceTable(@Path("id") int id);

    // Price Table Time Slot APIs
    @GET("api/price-table-time-slots/")
    Call<List<PriceTableTimeSlotModel>> getPriceTableTimeSlots();

    @POST("api/price-table-time-slots/")
    Call<PriceTableTimeSlotModel> createPriceTableTimeSlot(@Body PriceTableTimeSlotModel timeSlot);

    // Price Table Court APIs
    @GET("api/price-table-courts/")
    Call<List<PriceTableCourtModel>> getPriceTableCourts();

    @POST("api/price-table-courts/")
    Call<PriceTableCourtModel> createPriceTableCourt(@Body PriceTableCourtModel priceTableCourt);
}
