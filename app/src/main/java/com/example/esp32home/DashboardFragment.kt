package com.example.esp32home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat

class DashboardFragment : Fragment() {

    private lateinit var username: String
    private lateinit var apiKey: String
    private lateinit var adafruitApi: AdafruitApi
    private lateinit var temperatureValueTextView: TextView
    private lateinit var humidityValueTextView: TextView
    private lateinit var lightValueTextView: TextView
    private lateinit var statusTextView: TextView
    private lateinit var statusContainer: View
    private lateinit var ledToggleButton: Button


    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 15000L

    private var isLedOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            username = it.getString("username") ?: ""
            apiKey = it.getString("apiKey") ?: ""
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://io.adafruit.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        adafruitApi = retrofit.create(AdafruitApi::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        ledToggleButton = view.findViewById(R.id.ledToggleButton)
        temperatureValueTextView = view.findViewById(R.id.temperatureValueTextView)
        humidityValueTextView = view.findViewById(R.id.humidityValueTextView)
        lightValueTextView = view.findViewById(R.id.lightValueTextView)
        statusTextView = view.findViewById(R.id.statusTextView)
        statusContainer = view.findViewById(R.id.statusContainer)

        checkLedStatus()

        ledToggleButton.setOnClickListener {
            val newValue = if (isLedOn) "0" else "1"
            sendLedValue(newValue)
            isLedOn = !isLedOn
            updateLedButtonAppearance(ledToggleButton)
        }

        startSensorUpdates()
        return view
    }
    private fun fetchLight() {
        val feedKey = "light"
        adafruitApi.getLastFeedValue(username, feedKey, apiKey)
            .enqueue(object : Callback<FeedData> {
                override fun onResponse(call: Call<FeedData>, response: Response<FeedData>) {
                    if (response.isSuccessful) {
                        val light = response.body()?.value ?: "--"
                        lightValueTextView.text = "$light lux"
                    } else {
                        lightValueTextView.text = "Error: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<FeedData>, t: Throwable) {
                    lightValueTextView.text = "Помилка: ${t.message}"
                    Log.e("DashboardFragment", "Помилка зчитування освітленості", t)
                }
            })
    }
    private fun checkLedStatus() {
        val feedKey = "led"
        adafruitApi.getLastFeedValue(username, feedKey, apiKey)
            .enqueue(object : Callback<FeedData> {
                override fun onResponse(call: Call<FeedData>, response: Response<FeedData>) {
                    if (!isAdded) return

                    if (response.isSuccessful) {
                        val feedData = response.body()
                        val value = feedData?.value

                        if (value != null) {
                            isLedOn = value == "1"

                            updateLedButtonAppearance(ledToggleButton)
                        }
                    }
                }

                override fun onFailure(call: Call<FeedData>, t: Throwable) {
                    if (!isAdded) return
                    Toast.makeText(requireContext(), "Не вдалося перевірити стан LED", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun checkEsp32Status() {
        val feedKey = "status"
        adafruitApi.getLastFeedValue(username, feedKey, apiKey)
            .enqueue(object : Callback<FeedData> {
                override fun onResponse(call: Call<FeedData>, response: Response<FeedData>) {
                    if (!isAdded) return

                    if (response.isSuccessful) {
                        val feedData = response.body()
                        val lastUpdated = feedData?.created_at

                        if (lastUpdated != null) {
                            val format = java.text.SimpleDateFormat(
                                "yyyy-MM-dd'T'HH:mm:ssXXX",
                                java.util.Locale.getDefault()
                            )
                            val lastDate = format.parse(lastUpdated)
                            val now = java.util.Date()
                            val diffMillis = now.time - lastDate.time

                            if (diffMillis < 25_000) {
                                statusTextView.text = "Online"
                                statusContainer.setBackgroundColor(
                                    ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
                                )
                            } else {
                                statusTextView.text = "Offline"
                                statusContainer.setBackgroundColor(
                                    ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
                                )
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<FeedData>, t: Throwable) {
                    if (!isAdded) return
                    Toast.makeText(requireContext(), "Помилка з'єднання", Toast.LENGTH_SHORT).show()
                }
            })
    }



    private fun updateLedButtonAppearance(button: Button) {
        if (isLedOn) {
            button.text = "LED ON"
            button.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
            )
        } else {
            button.text = "LED OFF"
            button.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
            )
        }
    }

    private fun sendLedValue(value: String) {
        if (!::username.isInitialized || username.isEmpty() || !::apiKey.isInitialized || apiKey.isEmpty()) {
            Toast.makeText(requireContext(), "User data не ініціалізовані", Toast.LENGTH_SHORT).show()
            return
        }
        val feedKey = "led"

        adafruitApi.sendFeedData(username, feedKey, apiKey, FeedValue(value))
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "LED = $value надіслано", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Помилка надсилання: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(requireContext(), "Помилка з'єднання: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("DashboardFragment", "Помилка з'єднання", t)
                }
            })
    }

    private fun startSensorUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                fetchTemperature()
                fetchHumidity()
                fetchLight()
                checkEsp32Status()
                handler.postDelayed(this, updateInterval)
            }
        })
    }

    private fun fetchTemperature() {
        val feedKey = "temperature"
        adafruitApi.getLastFeedValue(username, feedKey, apiKey)
            .enqueue(object : Callback<FeedData> {
                override fun onResponse(call: Call<FeedData>, response: Response<FeedData>) {
                    if (response.isSuccessful) {
                        val temperature = response.body()?.value ?: "--"
                        temperatureValueTextView.text = "$temperature °C"
                    } else {
                        temperatureValueTextView.text = "Помилка: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<FeedData>, t: Throwable) {
                    temperatureValueTextView.text = "Помилка: ${t.message}"
                    Log.e("DashboardFragment", "Помилка зчитування температури", t)
                }
            })
    }

    private fun fetchHumidity() {
        val feedKey = "humidity"
        adafruitApi.getLastFeedValue(username, feedKey, apiKey)
            .enqueue(object : Callback<FeedData> {
                override fun onResponse(call: Call<FeedData>, response: Response<FeedData>) {
                    if (response.isSuccessful) {
                        val humidity = response.body()?.value ?: "--"
                        humidityValueTextView.text = "$humidity %"
                    } else {
                        humidityValueTextView.text = "Помилка: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<FeedData>, t: Throwable) {
                    humidityValueTextView.text = "Помилка: ${t.message}"
                    Log.e("DashboardFragment", "Помилка зчитування вологості", t)
                }
            })
    }

    companion object {
        fun newInstance(username: String, apiKey: String) =
            DashboardFragment().apply {
                arguments = Bundle().apply {
                    putString("username", username)
                    putString("apiKey", apiKey)
                }
            }
    }
}
