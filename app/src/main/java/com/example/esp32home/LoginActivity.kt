package com.example.esp32home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameInput = findViewById<EditText>(R.id.usernameInput)
        val apiKeyInput = findViewById<EditText>(R.id.apiKeyInput)
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val apiKey = apiKeyInput.text.toString().trim()

            if (username.isEmpty() || apiKey.isEmpty()) {
                Toast.makeText(this, "Введіть нікнейм та ключ", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("username", username)
                intent.putExtra("apiKey", apiKey)
                startActivity(intent)
                finish()
            }
        }
    }
}