package com.example.esp32home

data class Feed(
    val id: Int,
    val name: String,
    val key: String,
    val description: String?
)

data class FeedData(
    val value: String,
    val created_at: String?
)

