package com.example.esp32home
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

data class FeedValue(val value: String)

interface AdafruitApi {
    @GET("api/v2/{username}/feeds")
    fun getFeeds(
        @Path("username") username: String,
        @Header("X-AIO-Key") apiKey: String
    ): Call<List<Feed>>

    @POST("api/v2/{username}/feeds/{feed_key}/data")
    fun sendFeedData(
        @Path("username") username: String,
        @Path("feed_key") feedKey: String,
        @Header("X-AIO-Key") apiKey: String,
        @Body value: FeedValue
    ): Call<Void>

    @GET("api/v2/{username}/feeds/{feed_key}/data/last")
    fun getLastFeedValue(
        @Path("username") username: String,
        @Path("feed_key") feedKey: String,
        @Header("X-AIO-Key") apiKey: String
    ): Call<FeedData>


    @GET("api/v2/{username}/feeds/{feed_key}/data?limit=2800")
    fun getFeedData(
        @Path("username") username: String,
        @Path("feed_key") feedKey: String,
        @Query("X-AIO-Key") apiKey: String
    ): Call<List<FeedData>>

}