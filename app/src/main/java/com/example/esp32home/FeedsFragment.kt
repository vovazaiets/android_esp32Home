package com.example.esp32home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FeedsFragment : Fragment() {

    private lateinit var username: String
    private lateinit var apiKey: String
    private lateinit var adafruitApi: AdafruitApi
    private lateinit var feedsRecycler: RecyclerView

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
        val view = inflater.inflate(R.layout.fragment_feeds, container, false)

        val loadFeedsButton = view.findViewById<Button>(R.id.loadFeedsButton)
        feedsRecycler = view.findViewById(R.id.feedsRecycler)
        feedsRecycler.layoutManager = LinearLayoutManager(requireContext())

        loadFeedsButton.setOnClickListener {
            loadFeeds()
        }

        loadFeeds()

        return view
    }

    private fun loadFeeds() {
        adafruitApi.getFeeds(username, apiKey).enqueue(object : Callback<List<Feed>> {
            override fun onResponse(call: Call<List<Feed>>, response: Response<List<Feed>>) {
                if (!isAdded) return

                if (response.isSuccessful) {
                    val feeds = response.body() ?: emptyList()
                    feedsRecycler.adapter = FeedAdapter(feeds)
                    Toast.makeText(requireContext(), "Знайдено: ${feeds.size} фідів", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Помилка: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Feed>>, t: Throwable) {
                if (!isAdded) return
                Toast.makeText(requireContext(), "Мережна помилка: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("AdafruitError", "Помилка з'єднання", t)
            }
        })
    }

    companion object {
        fun newInstance(username: String, apiKey: String) =
            FeedsFragment().apply {
                arguments = Bundle().apply {
                    putString("username", username)
                    putString("apiKey", apiKey)
                }
            }
    }
}
