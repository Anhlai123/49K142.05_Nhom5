package com.example.nhom5.Bill;

import com.google.gson.annotations.SerializedName;

public class OrderModel {

    @SerializedName("id")
    private int id;

    @SerializedName("ma_don")
    private String maDon;

    @SerializedName("ten_khach_hang")
    private String tenKhachHang;

    @SerializedName("so_dien_thoai")
    private String soDienThoai;

    @SerializedName("gio_bat_dau")
    private String gioBatDau;

    @SerializedName("gio_ket_thuc")
    private String gioKetThuc;

    @SerializedName("loai_san")
    private String loaiSan;

    @SerializedName("san_ap_dung")
    private String sanApDung;

    @SerializedName("ngay_dat")
    private String ngayDat;

    @SerializedName("tong_tien")
    private String tongTien;

    @SerializedName("trang_thai_don")
    private String trangThaiDon;

    @SerializedName("thanh_toan")
    private String thanhToan;

    @SerializedName("ghi_chu")
    private String ghiChu;

    @SerializedName("booking")
    private Integer booking;

    public int getId() {
        return id;
    }

    public String getMaDon() {
        return maDon;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public String getGioBatDau() {
        return gioBatDau;
    }

    public String getGioKetThuc() {
        return gioKetThuc;
    }

    public String getLoaiSan() {
        return loaiSan;
    }

    public String getSanApDung() {
        return sanApDung;
    }

    public String getNgayDat() {
        return ngayDat;
    }

    public String getTongTien() {
        return tongTien;
    }

    public String getTrangThaiDon() {
        return trangThaiDon;
    }

    public String getThanhToan() {
        return thanhToan;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public Integer getBooking() {
        return booking;
    }
}