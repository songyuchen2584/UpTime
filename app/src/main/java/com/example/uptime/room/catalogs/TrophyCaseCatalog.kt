package com.example.uptime.room.catalogs

import com.example.uptime.room.AchievementSize
import kotlin.collections.List

object TrophyCaseCatalog {
    data class TrophyCaseOption(
        val id: String,
        val name : String,
        val shelfSlots: List<ShelfSlot> = listOf()
    )

    enum class ShelfSection { TopRow, MidRow1, MidRow2, BottomRow }

    data class ShelfSlot(
        val id: String,
        val section: ShelfSection,
        val acceptedSizes: List<AchievementSize>
    )

    val allTrophyCases: List<TrophyCaseOption> = listOf(
        TrophyCaseOption(
            id = "default",
            name = "Simple Shelf",
            shelfSlots = listOf(
                // Top area: 2 medium
                ShelfSlot("top_large", ShelfSection.TopRow, listOf(AchievementSize.Large)),
                ShelfSlot("top_med_1", ShelfSection.TopRow, listOf(AchievementSize.Medium)),
                ShelfSlot("top_med_2", ShelfSection.TopRow, listOf(AchievementSize.Medium)),
                // First row: 3 small
                ShelfSlot("mid1_1", ShelfSection.MidRow1, listOf(AchievementSize.Small)),
                ShelfSlot("mid1_2", ShelfSection.MidRow1, listOf(AchievementSize.Small)),
                ShelfSlot("mid1_3", ShelfSection.MidRow1, listOf(AchievementSize.Small)),
                // Second row: 3 small
                ShelfSlot("mid2_1", ShelfSection.MidRow2, listOf(AchievementSize.Small)),
                ShelfSlot("mid2_2", ShelfSection.MidRow2, listOf(AchievementSize.Small)),
                ShelfSlot("mid2_3", ShelfSection.MidRow2, listOf(AchievementSize.Small)),
                // Bottom area: 2 medium
                ShelfSlot("bot_large", ShelfSection.BottomRow, listOf(AchievementSize.Large)),
                ShelfSlot("bot_med_1", ShelfSection.BottomRow, listOf(AchievementSize.Medium)),
                ShelfSlot("bot_med_2", ShelfSection.BottomRow, listOf(AchievementSize.Medium)),
            )
        )
    )
}