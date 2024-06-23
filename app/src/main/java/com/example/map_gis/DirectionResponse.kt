package com.example.map_gis

import com.google.gson.annotations.SerializedName

data class DirectionResponse(
    @SerializedName("routes") val routes: List<Route>
)

data class Route(
    @SerializedName("overview_polyline") val overviewPolyline: Polyline
)

data class Polyline(
    @SerializedName("points") val points: String
)


