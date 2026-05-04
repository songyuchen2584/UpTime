package com.example.uptime.room.catalogs

import com.example.uptime.room.RoomItemAnchor
import com.example.uptime.room.RoomItemCategory

object RoomAnchorCatalog {
    val wallAnchors = listOf(
        RoomItemAnchor("wall_left",   RoomItemCategory.Wall, 0.15f, 0.11f),
        RoomItemAnchor("wall_left_center", RoomItemCategory.Wall, 0.4f, 0.11f),
        RoomItemAnchor("wall_right_center", RoomItemCategory.Wall, 0.625f, 0.415f),
        RoomItemAnchor("wall_right",  RoomItemCategory.Wall, 0.875f, 0.415f)
    )
    val floorAnchors = listOf<RoomItemAnchor>(
    )
    val all = wallAnchors + floorAnchors
}