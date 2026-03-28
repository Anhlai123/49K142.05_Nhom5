package com.example.nhom5

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookingSelection(
    val courtName: String,
    val startTime: String,
    val endTime: String,
    val date: String,
    val pricePerHour: Double
) : Parcelable
