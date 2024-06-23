package com.example.map_gis

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionService {
    @GET("/maps/api/directions/json")
    fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String
    ): Call<DirectionResponse>
}

