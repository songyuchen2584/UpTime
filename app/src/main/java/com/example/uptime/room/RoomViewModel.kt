package com.example.uptime.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.uptime.room.catalogs.AchievementCatalog
import com.example.uptime.room.catalogs.TrophyCaseCatalog
import com.example.uptime.UpTimeDatabase
import com.example.uptime.UserStatsRepository
import com.example.uptime.room.catalogs.RoomItemCatalog
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoomViewModel(application: Application) : AndroidViewModel(application) {
    private val db     = UpTimeDatabase.Companion.getDatabase(application)
    private val rsDao  = db.roomSettingsDao()
    private val invDao = db.userInventoryDao()
    private val statsRepository = UserStatsRepository(db.dailyLogDao())

    private val _newlyUnlocked = MutableSharedFlow<List<Achievement>>()
    val newlyUnlocked = _newlyUnlocked.asSharedFlow()

    val currentSettings: StateFlow<RoomSettings?> = rsDao.observeRoomSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentInventory: StateFlow<UserInventory?> =
        invDao.observeUserInventory()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _roomState = MutableStateFlow<RoomState?>(null)
    val roomState: StateFlow<RoomState?> = _roomState

    init {
        viewModelScope.launch {
            if (rsDao.getSettings() == null) rsDao.upsertRoomSettings(RoomSettings())
            if (invDao.getInventory() == null) invDao.upsertInventory(UserInventory())
        }
        // Automatically update when repository changes
        viewModelScope.launch {
            statsRepository.userStats.collect { stats ->
                checkAndUnlockAchievements(stats)
            }
        }

        // Award points when goals completed
        viewModelScope.launch {
            statsRepository.goalCompletionEvents.collect {
                updatePoints(DAILY_COMPLETION_POINTS)
            }
        }

        viewModelScope.launch {
            combine(currentSettings, currentInventory) { settings, inventory ->
                if (settings == null || inventory == null) return@combine null

                RoomState(
                    selectedRoomLayoutId = settings.selectedRoomLayoutId,
                    selectedRoomThemeId = settings.selectedRoomThemeId,
                    selectedWoodThemeId = settings.selectedWoodThemeId,
                    displayName = settings.displayName,
                    placedRoomItems = settings.placedRoomItems,
                    placedAchievements = settings.placedAchievements,
                    unlockedRoomItemIds = inventory.unlockedRoomItemIds,
                    unlockedRoomThemeIds = inventory.unlockedRoomThemeIds,
                    unlockedAchievementIds = inventory.unlockedAchievementIds,
                    unlockedWoodThemeIds = inventory.unlockedWoodThemeIds,
                    unlockedRoomLayoutIds = inventory.unlockedRoomLayoutIds,
                    currentPoints = inventory.currentPoints
                )
            }.collect { newState ->
                if (newState != null) {
                    _roomState.value = newState
                }
            }
        }
    }

    companion object {
        const val DAILY_COMPLETION_POINTS = 50
    }

    private suspend fun checkAndUnlockAchievements(stats: UserStatsRepository.UserStats) {
        val inventory = invDao.getInventory() ?: UserInventory()
        val alreadyUnlocked = inventory.unlockedAchievementIds

        val newlyUnlocked = AchievementCatalog.all
            .filter { it.id !in alreadyUnlocked }
            .filter { meetsCondition(it, stats) }
            .map { it.id }

        if (newlyUnlocked.isEmpty()) return

        invDao.upsertInventory(
            inventory.copy(unlockedAchievementIds = alreadyUnlocked + newlyUnlocked)
        )
        _newlyUnlocked.emit(
            newlyUnlocked.mapNotNull { id ->
                AchievementCatalog.all.find { it.id == id }
            }
        )
    }

    private fun meetsCondition(achievement: Achievement, stats: UserStatsRepository.UserStats): Boolean {
        return when (achievement.id) {
            "start" -> stats.totalWalkingMins > 0
            "streak_7" -> stats.currentStreak >= 7
            "streak_14" -> stats.currentStreak >= 14
            "streak_21" -> stats.currentStreak >= 21
            "streak_28" -> stats.currentStreak >= 28
            "streak_50" -> stats.currentStreak >= 50
            "streak_100" -> stats.currentStreak >= 100
            "walk_60" -> stats.totalWalkingMins >= 60
            "walk_120" -> stats.totalWalkingMins >= 120
            "walk_360" -> stats.totalWalkingMins >= 360
            "walk_600" -> stats.totalWalkingMins >= 600
            "walk_1000" -> stats.totalWalkingMins >= 1000
            else -> false
        }
    }

    fun purchaseRoomTheme(themeId: String, cost: Int) {
        viewModelScope.launch {
            val inventory = invDao.getInventory() ?: UserInventory()

            if (inventory.currentPoints < cost) return@launch

            val updatedInventory = inventory.copy(
                currentPoints = inventory.currentPoints - cost,
                unlockedRoomThemeIds = inventory.unlockedRoomThemeIds + themeId
            )

            invDao.upsertInventory(updatedInventory)
        }
    }

    fun purchaseWoodTheme(themeId: String, cost: Int) {
        viewModelScope.launch {
            val inventory = invDao.getInventory() ?: UserInventory()

            if (inventory.currentPoints < cost) return@launch

            val updatedInventory = inventory.copy(
                currentPoints = inventory.currentPoints - cost,
                unlockedWoodThemeIds = inventory.unlockedWoodThemeIds + themeId
            )

            invDao.upsertInventory(updatedInventory)
        }
    }

    fun purchaseItem(itemId: String, cost: Int) {
        viewModelScope.launch {
            val inventory = invDao.getInventory() ?: UserInventory()

            if (inventory.currentPoints < cost) return@launch

            val updatedInventory = inventory.copy(
                currentPoints = inventory.currentPoints - cost,
                unlockedWoodThemeIds = inventory.unlockedWoodThemeIds + itemId
            )

            invDao.upsertInventory(updatedInventory)
        }
    }

    // May be used later for collecting items from other means
    fun unlockRoomItem(roomItemId: String) {
        viewModelScope.launch {
            val inventory = invDao.getInventory() ?: UserInventory()
            val alreadyUnlocked = inventory.unlockedRoomItemIds

            val newlyUnlocked = RoomItemCatalog.all
                .filter { it.id !in alreadyUnlocked && it.id == roomItemId }
                .map { it.id }

            invDao.upsertInventory(
                inventory.copy(unlockedRoomItemIds = alreadyUnlocked + newlyUnlocked)
            )
        }
    }

    fun updatePoints(points: Int) {
        // Assumes currentPoints + points is > 0
        viewModelScope.launch {
            val inventory = invDao.getInventory() ?: UserInventory()
            val currentPoints = inventory.currentPoints

            invDao.upsertInventory(
                inventory.copy(currentPoints = currentPoints + points)
            )
        }
    }

    fun updateDisplayName(newName: String) {
        viewModelScope.launch {
            val current = rsDao.getSettings() ?: RoomSettings()

            val updated = current.copy(
                displayName = newName
            )

            rsDao.upsertRoomSettings(updated)
        }
    }

    fun selectRoomTheme(themeId: String) {
        viewModelScope.launch {
            val currentSettings = rsDao.getSettings() ?: RoomSettings()

            val updated = currentSettings.copy(
                selectedRoomThemeId = themeId
            )

            rsDao.upsertRoomSettings(updated)
        }
    }

    fun selectWoodTheme(themeId: String) {
        viewModelScope.launch {
            val current = rsDao.getSettings() ?: RoomSettings()

            val updated = current.copy(
                selectedWoodThemeId = themeId
            )

            rsDao.upsertRoomSettings(updated)
        }
    }

    fun getCurrentLayoutSlots(roomLayoutId: String): List<TrophyCaseCatalog.ShelfSlot> {
        return TrophyCaseCatalog.all
            .find { it.id == roomLayoutId }
            ?.shelfSlots
            ?: emptyList()
    }

    fun getAchievementById(id: String): Achievement? {
        return AchievementCatalog.all.find { it.id == id }
    }

    fun placeAchievement(achievementId: String) {
        viewModelScope.launch {
            val currentSettings = rsDao.getSettings() ?: RoomSettings()
            val slots = getCurrentLayoutSlots(currentSettings.selectedRoomLayoutId)
            val placed = currentSettings.placedAchievements.toMutableMap()
            val achievement = getAchievementById(achievementId) ?: return@launch

            val targetSlot = when (achievement.size) {
                AchievementSize.Large -> slots
                    .filter { AchievementSize.Large in it.acceptedSizes }
                    .firstOrNull { slot ->
                        val sectionSlots = slots.filter { it.section == slot.section }
                        sectionSlots.all { placed[it.id] == null }
                    }
                AchievementSize.Medium -> slots
                    .filter { AchievementSize.Medium in it.acceptedSizes && placed[it.id] == null }
                    .firstOrNull { slot ->
                        val sectionSlots = slots.filter { it.section == slot.section }
                        val hasLarge = sectionSlots.any {
                            AchievementSize.Large in it.acceptedSizes && placed[it.id] != null
                        }
                        val mediumsFilled = sectionSlots.count {
                            AchievementSize.Medium in it.acceptedSizes && placed[it.id] != null
                        }
                        !hasLarge && mediumsFilled < 2
                    }
                AchievementSize.Small -> slots
                    .firstOrNull {
                        AchievementSize.Small in it.acceptedSizes && placed[it.id] == null
                    }
            } ?: return@launch

            // Remove any existing placement of this achievement
            placed.entries.removeAll { it.value == achievementId }
            // Place in new slot
            placed[targetSlot.id] = achievementId

            rsDao.upsertRoomSettings(currentSettings.copy(placedAchievements = placed))
        }
    }

    fun removeAchievement(achievementId: String) {
        viewModelScope.launch {
            val settings = rsDao.getSettings() ?: return@launch
            val updatedMap = settings.placedAchievements
                .filterValues { it != achievementId }

            rsDao.upsertRoomSettings(settings.copy(placedAchievements = updatedMap))
        }
    }

    fun hasShelfSpace(achievement: Achievement): Boolean {
        val settings = currentSettings.value ?: return false
        val slots = getCurrentLayoutSlots(settings.selectedRoomLayoutId)
        val placed = settings.placedAchievements

        return when (achievement.size) {
            AchievementSize.Large -> slots
                .filter { AchievementSize.Large in it.acceptedSizes }
                .any { slot ->
                    val sectionSlots = slots.filter { it.section == slot.section }
                    sectionSlots.all { placed[it.id] == null }
                }
            AchievementSize.Medium -> slots
                .filter { AchievementSize.Medium in it.acceptedSizes && placed[it.id] == null }
                .any { slot ->
                    val sectionSlots = slots.filter { it.section == slot.section }
                    val hasLarge = sectionSlots.any {
                        AchievementSize.Large in it.acceptedSizes && placed[it.id] != null
                    }
                    val mediumsFilled = sectionSlots.count {
                        AchievementSize.Medium in it.acceptedSizes && placed[it.id] != null
                    }
                    !hasLarge && mediumsFilled < 2
                }
            AchievementSize.Small -> slots
                .any {
                    AchievementSize.Small in it.acceptedSizes && placed[it.id] == null
                }
        }
    }
}