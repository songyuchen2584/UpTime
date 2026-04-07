package com.example.uptime

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class Quote(
    @SerializedName("q") val text: String,
    @SerializedName("a") val author: String
)

interface QuoteService {
    @GET("api/random")
    suspend fun getRandomQuote(): List<Quote>
}

object QuoteApi {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://zenquotes.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: QuoteService = retrofit.create(QuoteService::class.java)
}
