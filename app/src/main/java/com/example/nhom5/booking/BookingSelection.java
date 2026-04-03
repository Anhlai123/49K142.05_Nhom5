package com.example.nhom5.booking;

import android.os.Parcel;
import android.os.Parcelable;

public class BookingSelection implements Parcelable {
    private String courtName;
    private String startTime;
    private String endTime;
    private String date;
    private double pricePerHour;

    public BookingSelection(String courtName, String startTime, String endTime, String date, double pricePerHour) {
        this.courtName = courtName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.pricePerHour = pricePerHour;
    }

    protected BookingSelection(Parcel in) {
        courtName = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        date = in.readString();
        pricePerHour = in.readDouble();
    }

    public static final Creator<BookingSelection> CREATOR = new Creator<BookingSelection>() {
        @Override
        public BookingSelection createFromParcel(Parcel in) {
            return new BookingSelection(in);
        }

        @Override
        public BookingSelection[] newArray(int size) {
            return new BookingSelection[size];
        }
    };

    public String getCourtName() { return courtName; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getDate() { return date; }
    public double getPricePerHour() { return pricePerHour; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(courtName);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeString(date);
        dest.writeDouble(pricePerHour);
    }
}
