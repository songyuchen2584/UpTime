package com.example.uptime.room.catalogs

import com.example.uptime.R
import com.example.uptime.room.RoomItem
import com.example.uptime.room.RoomItemCategory

object RoomItemCatalog {
    val all: List<RoomItem> = listOf(
        RoomItem(
            id = "poster_uptime",
            name = "UpTime Banner",
            icon = R.drawable.app_poster_24px,
            category = RoomItemCategory.Wall,
            pointCost = 0,
            widthFraction = 0.14f,
            heightFraction = 0.2f
        ),
        RoomItem(
            id = "poster_band",
            name = "Band Poster",
            icon = R.drawable.poster_24px,
            category = RoomItemCategory.Wall,
            pointCost = 50,
            widthFraction = 0.18f,
            heightFraction = 0.19f
        ),
        RoomItem(
            id = "poster_movie",
            name = "Movie Poster",
            icon = R.drawable.poster_24px,
            category = RoomItemCategory.Wall,
            pointCost = 75,
            widthFraction = 0.185f,
            heightFraction = 0.17f
        ),
        RoomItem(
            id = "plant_pot",
            name = "Potted Plant",
            icon = R.drawable.potted_plant_24px,
            category = RoomItemCategory.Floor,
            pointCost = 25,
            widthFraction = 0.12f,
            heightFraction = 0.135f
        ),
        RoomItem(
            id = "lamp",
            name = "Floor Lamp",
            icon = R.drawable.floor_lamp_24px,
            category = RoomItemCategory.Floor,
            pointCost = 75,
            widthFraction = 0.12f,
            heightFraction = 0.275f
        ),
    )
}