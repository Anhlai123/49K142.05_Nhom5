package com.example.nhom5.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nhom5.R
import com.example.nhom5.api.ApiClient
import com.example.nhom5.auth.model.RegisterRequest
import com.example.nhom5.auth.model.RegisterResponse
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etFullName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etAddress: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvBackToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        bindViews()
        setupActions()
    }

    private fun bindViews() {
        etUsername = findViewById(R.id.et_username)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        etFullName = findViewById(R.id.et_full_name)
        etPhone = findViewById(R.id.et_phone)
        etAddress = findViewById(R.id.et_address)
        btnRegister = findViewById(R.id.btn_register)
        tvBackToLogin = findViewById(R.id.tv_back_to_login)
    }

    private fun setupActions() {
        tvBackToLogin.setOnClickListener { finish() }

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val fullName = etFullName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val address = etAddress.text.toString().trim()

            if (!validateInput(username, email, password, fullName, phone, address)) {
                return@setOnClickListener
            }

            callRegisterApi(
                RegisterRequest(
                    username = username,
                    email = email,
                    password = password,
                    fullName = fullName,
                    phone = phone,
                    address = address
                )
            )
        }
    }

    private fun validateInput(
        username: String,
        email: String,
        password: String,
        fullName: String,
        phone: String,
        address: String
    ): Boolean {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() ||
            fullName.isEmpty() || phone.isEmpty() || address.isEmpty()
        ) {
            Toast.makeText(this, "Vui long nhap day du thong tin", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email khong hop le", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(this, "Mat khau toi thieu 6 ky tu", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun callRegisterApi(request: RegisterRequest) {
        setLoading(true)

        ApiClient.getApiService().register(request).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                setLoading(false)

                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "Dang ky thanh cong"
                    Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_LONG).show()
                    finish()
                    return
                }

                val errorMessage = parseErrorMessage(response)
                Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                setLoading(false)
                Toast.makeText(
                    this@RegisterActivity,
                    "Khong the ket noi server: ${t.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun parseErrorMessage(response: Response<RegisterResponse>): String {
        return try {
            val raw = response.errorBody()?.string().orEmpty()
            if (raw.isEmpty()) return "Dang ky that bai. Vui long thu lai"

            val json = JSONObject(raw)
            when {
                json.has("username") -> json.getJSONArray("username").optString(0)
                json.has("email") -> json.getJSONArray("email").optString(0)
                json.has("phone") -> json.getJSONArray("phone").optString(0)
                json.has("password") -> json.getJSONArray("password").optString(0)
                json.has("detail") -> json.optString("detail")
                else -> "Dang ky that bai. Vui long kiem tra thong tin"
            }
        } catch (_: Exception) {
            "Dang ky that bai. Vui long thu lai"
        }
    }

    private fun setLoading(isLoading: Boolean) {
        btnRegister.isEnabled = !isLoading
        btnRegister.text = if (isLoading) "Dang xu ly..." else "Dang ky"
        val visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        tvBackToLogin.visibility = visibility
    }
}

