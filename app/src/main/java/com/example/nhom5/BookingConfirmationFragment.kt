package com.example.nhom5

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import java.text.DecimalFormat

class BookingConfirmationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 3. Auto-Fill Logic: Retrieve arguments
        val courtName = arguments?.getString("courtName") ?: "Sân Cầu Lông 2"
        val startTime = arguments?.getString("startTime") ?: "07:00"
        val date = arguments?.getString("date") ?: "13/01/2026"
        val pricePerHour = arguments?.getInt("pricePerHour")?.toDouble() ?: 60000.0

        // Calculate End Time (Assume 1 hour duration for simplicity, or pass as arg)
        val startHour = startTime.split(":")[0].toInt()
        val endTime = "${String.format("%02d", startHour + 1)}:00"
        val totalHours = 1.0 // This could be calculated if multiple slots were passed
        val totalPrice = totalHours * pricePerHour

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    BookingConfirmationScreen(
                        courtName = courtName,
                        timeSlot = "$startTime - $endTime",
                        date = date,
                        totalHours = "${totalHours.toInt()}h00",
                        totalPrice = totalPrice,
                        onBack = { Navigation.findNavController(this).navigateUp() },
                        onConfirm = { name, phone ->
                            Toast.makeText(requireContext(), "Đặt sân thành công cho $name", Toast.LENGTH_SHORT).show()
                            Navigation.findNavController(this).navigate(R.id.navigation_home)
                        }
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BookingConfirmationScreen(
        courtName: String,
        timeSlot: String,
        date: String,
        totalHours: String,
        totalPrice: Double,
        onBack: () -> Unit,
        onConfirm: (String, String) -> Unit
    ) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        
        val priceFormatter = DecimalFormat("#,###")
        val formattedPrice = "${priceFormatter.format(totalPrice)} đ"

        val primaryGreen = Color(0xFF00A63E)
        val backgroundColor = Color(0xFFF8F9FA)
        val greyText = Color(0xFF888888)
        val fieldBg = Color(0xFFF8F9FA)
        val fieldBorder = Color(0xFFF1F3F4)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            // Header
            CenterAlignedTopAppBar(
                title = { Text("Đặt sân", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = primaryGreen)
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .weight(1f)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Card 1: Court Info
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(courtName, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text("60.000/h", color = primaryGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("Loại: Cầu lông", color = greyText, fontSize = 14.sp)
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 1.dp, color = Color(0xFFEEEEEE))
                        
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                IconInfoItem("📐", "13.4m x 6.1m", Modifier.weight(1f))
                                IconInfoItem("🌲", "Sân gỗ chuẩn", Modifier.weight(1f))
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                IconInfoItem("❄️", "Có điều hòa", Modifier.weight(1f))
                                IconInfoItem("💡", "Đèn LED", Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card 2: Auto-filled Booking Info
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Thông tin lịch đặt mới", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Ngày đặt: $date", color = greyText, fontSize = 14.sp)

                        Spacer(modifier = Modifier.height(20.dp))

                        // Sân & Thời gian (ReadOnly)
                        Text("Sân & Thời gian", fontWeight = FontWeight.Bold, color = greyText, fontSize = 14.sp)
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            ReadOnlyField(courtName, Modifier.weight(1f), fieldBg, fieldBorder)
                            Spacer(modifier = Modifier.width(12.dp))
                            ReadOnlyField(timeSlot, Modifier.weight(1f), fieldBg, fieldBorder)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Tổng giờ & Tổng tiền (Auto-calculated)
                        Text("Tổng giờ & Tổng tiền", fontWeight = FontWeight.Bold, color = greyText, fontSize = 14.sp)
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            ReadOnlyField(totalHours, Modifier.weight(1f), fieldBg, fieldBorder)
                            Spacer(modifier = Modifier.width(12.dp))
                            ReadOnlyField(formattedPrice, Modifier.weight(1f), fieldBg, fieldBorder, textColor = Color.Red)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // User Input Integration
                        Text("Thông tin khách hàng", fontWeight = FontWeight.Bold, color = greyText, fontSize = 14.sp)
                        
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("Họ và tên", color = Color.LightGray) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryGreen,
                                unfocusedBorderColor = fieldBorder,
                                unfocusedContainerColor = fieldBg,
                                focusedContainerColor = fieldBg
                            )
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { if (it.all { char -> char.isDigit() }) phone = it },
                            placeholder = { Text("Số điện thoại", color = Color.LightGray) },
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryGreen,
                                unfocusedBorderColor = fieldBorder,
                                unfocusedContainerColor = fieldBg,
                                focusedContainerColor = fieldBg
                            )
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), 
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = onBack,
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F3F4))
                            ) {
                                Text("HỦY BỎ", color = Color(0xFF6C757D), fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onConfirm(name, phone) },
                                enabled = name.isNotBlank() && phone.isNotBlank(),
                                modifier = Modifier.weight(1.5f).height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                            ) {
                                Text("ĐẶT NGAY", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    @Composable
    fun ReadOnlyField(value: String, modifier: Modifier = Modifier, backgroundColor: Color, borderColor: Color, textColor: Color = Color.Black) {
        Box(
            modifier = modifier
                .height(56.dp)
                .background(backgroundColor, RoundedCornerShape(12.dp))
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(text = value, color = textColor, fontSize = 15.sp)
        }
    }

    @Composable
    fun IconInfoItem(emoji: String, text: String, modifier: Modifier = Modifier) {
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, color = Color(0xFF666666), fontSize = 14.sp)
        }
    }
}
