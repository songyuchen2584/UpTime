package com.example.uptime.room

import android.R.attr.category
import android.app.Application
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Transaction
import com.example.uptime.profile.FriendsRepository
import com.example.uptime.room.catalogs.AchievementCatalog
import com.example.uptime.room.catalogs.TrophyCaseCatalog
import com.example.uptime.data.UpTimeDatabase
import com.example.uptime.data.UserStatsRepository
import com.example.uptime.profile.FriendProfile
import com.example.uptime.room.catalogs.RoomAnchorCatalog
import com.example.uptime.room.catalogs.RoomItemCatalog
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.text.contains

class RoomViewModel(application: Application, val userId: String) : AndroidViewModel(application) {
    private val db = UpTimeDatabase.Companion.getDatabase(application)
    private val rsDao = db.roomSettingsDao()
    private val invDao = db.userInventoryDao()
    private val statsRepository = UserStatsRepository(db.dailyLogDao())
    private val roomRepository = FirebaseRoomRepository()
    val friendsRepository = FriendsRepository()
    private val auth = FirebaseAuth.getInstance()
    private val _currentUserId = MutableStateFlow<String?>(auth.currentUser?.uid)
    val currentUserId: StateFlow<String?> = _currentUserId
    val isOwner = userId == "me"
    private val LOCAL_ID = "me"

    val currentSettings: StateFlow<RoomSettings?> = if (isOwner) {
        rsDao.observeRoomSettings(LOCAL_ID)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    } else {
        roomRepository.observeRoomSettings(userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    private val _newlyUnlocked = MutableSharedFlow<List<Achievement>>()
    val newlyUnlocked = _newlyUnlocked.asSharedFlow()

    val currentInventory: StateFlow<UserInventory?> = if (isOwner) {
        invDao.observeUserInventory(LOCAL_ID)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    } else {
        roomRepository.observeUserInventory(userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    val totalUnlockedAchievements: StateFlow<Int> = currentInventory
        .map { it?.unlockedAchievementIds?.size ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _roomState = MutableStateFlow<RoomState?>(null)
    val roomState: StateFlow<RoomState?> = _roomState

    private var restoreCompleted = false

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val newUid = firebaseAuth.currentUser?.uid
            if (_currentUserId.value != newUid) {
                _currentUserId.value = newUid
                restoreCompleted = false
                Log.d("RoomVM", "Auth identity shifted to: $newUid")
            }
        }

        viewModelScope.launch {
            _currentUserId.collect { uid ->
                if (isOwner && uid != null) {
                    restoreCompleted = false
                    var localSettings = rsDao.getSettings("me")
                    var localInv = invDao.getInventory("me")

                    val remoteSettings = roomRepository.getRoomSettings(uid)
                    val remoteInv = roomRepository.getUserInventory(uid)

                    when {
                        // Prefer real remote data
                        remoteSettings != null -> {
                            Log.d("RoomVM", "Restoring settings from remote for $uid")
                            rsDao.upsertRoomSettings(remoteSettings.copy(userId = "me"))
                        }
                        // No remote data = new user, keep or create local defaults
                        localSettings == null -> {
                            Log.d("RoomVM", "Creating default settings for new user")
                            rsDao.upsertRoomSettings(RoomSettings(userId = "me"))
                        }
                        // Local exists, no remote = first time syncing, don't overwrite
                        else -> {
                            Log.d("RoomVM", "No remote settings found, keeping local for $uid")
                        }
                    }

                    when {
                        remoteInv != null -> {
                            Log.d("RoomVM", "Restoring inventory from remote for $uid")
                            invDao.upsertInventory(remoteInv.copy(userId = "me"))
                        }
                        localInv == null -> {
                            Log.d("RoomVM", "Creating default inventory for new user")
                            invDao.upsertInventory(UserInventory(userId = "me"))
                        }
                        else -> {
                            Log.d("RoomVM", "No remote inventory found, keeping local for $uid")
                        }
                    }

                    restoreCompleted = true  // safe to sync
                    Log.d("RoomVM", "Restore complete for $uid")
                }
            }
        }

        // Automatically update when repository changes
        viewModelScope.launch {
            statsRepository.userStats.collect { stats ->
                checkAndUnlockAchievements(stats)
            }
        }

        viewModelScope.launch {
            combine(
                totalUnlockedAchievements,
                statsRepository.userStats
            ) {
                trophies, stats ->
                trophies to stats.currentStreak
            }
                .distinctUntilChanged()
                .collect { (trophies, streak) ->
                friendsRepository.syncStats(
                    streak = streak,
                    trophies = trophies
                )
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

        viewModelScope.launch {
            combine(currentSettings, currentInventory, currentUserId) { settings, inventory, uid ->
                Triple(settings, inventory, uid)
            }.collect { (settings, inventory, uid) ->
                if (isOwner && settings != null && inventory != null && uid != null
                    && restoreCompleted  // gate on this
                ) {
                    val settingsToSync  = settings.copy(userId = uid)
                    val inventoryToSync = inventory.copy(userId = uid)

                    roomRepository.syncRoomSettings(uid, settingsToSync)
                    roomRepository.syncInventory(uid, inventoryToSync)
                }
            }
        }
    }

    companion object {
        const val DAILY_COMPLETION_POINTS = 50
    }

    fun addFriendById(userId: String){
        viewModelScope.launch {
            friendsRepository.addFriendById(userId)
        }
    }

    fun removeFriendById(userId: String){
        viewModelScope.launch {
            friendsRepository.removeFriend(userId)
        }
    }

    @Composable
    fun getFriendProfileById(userId: String): FriendProfile? {
        return friendsRepository.observeFriendProfile(userId).collectAsState(null).value
    }

    fun getRandomUserRoom(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val randomUserId = roomRepository.getRandomUserId(currentUserId)
            onResult(randomUserId)
            Log.d("RandomRoom", "CurrentUserId: $currentUserId")
            Log.d("RandomRoom", "RandomUserId: $randomUserId")
        }
    }

    fun placeRoomItem(itemId: String, anchorId: String) {
        viewModelScope.launch {
            val settings = rsDao.getSettings(userId) ?: RoomSettings(userId)
            val anchor = RoomAnchorCatalog.all.find { it.id == anchorId }?: return@launch
            val item = RoomItemCatalog.all.find { it.id == itemId }?: return@launch
            if (item.category != anchor.category) return@launch

            val updated = settings.placedRoomItems
                .filterValues { it != itemId }
                .plus(anchorId to itemId)

            rsDao.upsertRoomSettings(settings.copy(placedRoomItems = updated))
        }
    }

    fun placeRoomItem(itemId: String, category: RoomItemCategory) {
        viewModelScope.launch {
            val settings = rsDao.getSettings(userId) ?: RoomSettings(userId)
            val usedAnchors = settings.placedRoomItems.keys
            val anchor =
                RoomAnchorCatalog.all.firstOrNull { it.category == category && it.id !in usedAnchors }
                    ?: return@launch
            val item = RoomItemCatalog.all.find { it.id == itemId }?: return@launch
            if (item.category != anchor.category) return@launch


            val updated = settings.placedRoomItems
                .filterValues { it != itemId }
                .plus(anchor.id to itemId)

            rsDao.upsertRoomSettings(settings.copy(placedRoomItems = updated))
        }
    }

    fun removeRoomItem(itemId: String) {
        viewModelScope.launch {
            val settings = rsDao.getSettings(userId)?: return@launch
            rsDao.upsertRoomSettings(
                settings.copy(
                    placedRoomItems = settings.placedRoomItems.filterValues { it != itemId }
                )
            )
        }
    }

    private suspend fun checkAndUnlockAchievements(
        stats: UserStatsRepository.UserStats,
        singlePurchaseAmount: Int = 0,
        isEndOfDay: Boolean = false
    ) {
        if (!isOwner) return
        val inventory = invDao.getInventory(userId) ?: UserInventory(userId)
        val alreadyUnlocked = inventory.unlockedAchievementIds

        val statsWithPurchase = stats.copy(largestSinglePurchase = singlePurchaseAmount)

        val newlyUnlocked = AchievementCatalog.all
            .filter { it.id !in alreadyUnlocked }
            .filter { meetsCondition(it, statsWithPurchase, isEndOfDay) }
            .map { it.id }

        if (newlyUnlocked.isEmpty()) return

        invDao.upsertInventory(
            inventory.copy(unlockedAchievementIds = alreadyUnlocked + newlyUnlocked)
        )
        _newlyUnlocked.emit(
            newlyUnlocked.mapNotNull { id -> AchievementCatalog.all.find { it.id == id } }
        )
    }

    private fun meetsCondition(achievement: Achievement, stats: UserStatsRepository.UserStats, isEndOfDay: Boolean): Boolean {
        return when (achievement.id) {
            // Walking
            "walk_60" -> stats.totalWalkingMins >= 60
            "walk_120" -> stats.totalWalkingMins >= 120
            "walk_360" -> stats.totalWalkingMins >= 360
            "walk_600" -> stats.totalWalkingMins >= 600
            "walk_1000" -> stats.totalWalkingMins >= 1000
            "walk_2000" -> stats.totalWalkingMins >= 2000
            "walk_5000" -> stats.totalWalkingMins >= 5000
            "walk_10000" -> stats.totalWalkingMins >= 10000
            "walk_20000" -> stats.totalWalkingMins >= 20000
            "walk_50000" -> stats.totalWalkingMins >= 50000

            // Total spending
            "spend_50" -> stats.totalPointsSpent >= 50
            "spend_250" -> stats.totalPointsSpent >= 250
            "spend_500" -> stats.totalPointsSpent >= 500
            "spend_1000" -> stats.totalPointsSpent >= 1000
            "spend_2000" -> stats.totalPointsSpent >= 2000
            "spend_5000" -> stats.totalPointsSpent >= 5000
            "spend_10000" -> stats.totalPointsSpent >= 10000
            "spend_20000" -> stats.totalPointsSpent >= 20000
            "spend_50000" -> stats.totalPointsSpent >= 50000
            "spend_100000" -> stats.totalPointsSpent >= 100000

            // Single purchase
            "save_200" -> stats.largestSinglePurchase >= 200
            "save_500" -> stats.largestSinglePurchase >= 500
            "save_1000" -> stats.largestSinglePurchase >= 1000
            "save_5000" -> stats.largestSinglePurchase >= 5000

            // Special
            "start" -> true

            else -> false
        }
    }

    fun purchaseRoomTheme(themeId: String, cost: Int) {
        if (!isOwner) return
        viewModelScope.launch {
            processPurchase(cost) { it.copy(unlockedRoomThemeIds = it.unlockedRoomThemeIds + themeId) }
        }
    }

    fun purchaseWoodTheme(themeId: String, cost: Int) {
        if (!isOwner) return
        viewModelScope.launch {
            processPurchase(cost) { it.copy(unlockedWoodThemeIds = it.unlockedWoodThemeIds + themeId) }
        }
    }

    fun purchaseItem(itemId: String, cost: Int) {
        if (!isOwner) return
        viewModelScope.launch {
            processPurchase(cost) { it.copy(unlockedRoomItemIds = it.unlockedRoomItemIds + itemId) }
        }
    }

    // May be used later for collecting items from other means
    fun unlockRoomItem(roomItemId: String) {
        if (!isOwner) return
        viewModelScope.launch {
            val inventory = invDao.getInventory(userId) ?: UserInventory(userId)
            val alreadyUnlocked = inventory.unlockedRoomItemIds

            val newlyUnlocked = RoomItemCatalog.all
                .filter { it.id !in alreadyUnlocked && it.id == roomItemId }
                .map { it.id }

            invDao.upsertInventory(
                inventory.copy(unlockedRoomItemIds = alreadyUnlocked + newlyUnlocked)
            )
        }
    }

    @Transaction
    private suspend fun processPurchase(
        cost: Int,
        transform: (UserInventory) -> UserInventory
    ): Boolean {
        if (!isOwner) return false
        val inventory = invDao.getInventory(userId) ?: UserInventory(userId)
        if (inventory.currentPoints < cost) return false

        val updated = transform(
            inventory.copy(
                currentPoints = inventory.currentPoints - cost,
                totalPointsSpent = inventory.totalPointsSpent + cost
            )
        )
        invDao.upsertInventory(updated)
        checkAndUnlockAchievements(
            stats = buildUserStats(updated),
            singlePurchaseAmount = cost
        )
        return true
    }

    private suspend fun buildUserStats(
        inventory: UserInventory? = null,
        singlePurchaseAmount: Int = 0,
        screenTimeOverBy: Int = 0,
        walkingUnderBy: Int = 0
    ): UserStatsRepository.UserStats {
        val inv = inventory ?: invDao.getInventory(userId) ?: UserInventory(userId)
        val statsFromLogs = statsRepository.userStats.first()

        return statsFromLogs.copy(
            totalPointsSpent = inv.totalPointsSpent,
            largestSinglePurchase = singlePurchaseAmount,
            screenTimeFailCount = inv.screenTimeFailCount,
            consecutiveScreenTimeSuccess = inv.consecutiveScreenTimeSuccess,
            screenTimeOverBy = screenTimeOverBy,
            walkingUnderBy = walkingUnderBy
        )
    }

    fun updatePoints(points: Int) {
        if (!isOwner) return
        // Assumes currentPoints + points is > 0
        viewModelScope.launch {
            val inventory = invDao.getInventory(userId) ?: UserInventory(userId)
            val currentPoints = inventory.currentPoints

            invDao.upsertInventory(
                inventory.copy(currentPoints = currentPoints + points)
            )
        }
    }

    fun updateDisplayName(newName: String) {
        if (!isOwner) return
        viewModelScope.launch {
            val current = rsDao.getSettings(userId) ?: RoomSettings(userId)

            val updated = current.copy(
                displayName = newName
            )

            rsDao.upsertRoomSettings(updated)
        }
    }

    fun selectRoomTheme(themeId: String) {
        if (!isOwner) return
        viewModelScope.launch {
            val currentSettings = rsDao.getSettings(userId) ?: RoomSettings(userId)

            val updated = currentSettings.copy(
                selectedRoomThemeId = themeId
            )

            rsDao.upsertRoomSettings(updated)
        }
    }

    fun selectWoodTheme(themeId: String) {
        if (!isOwner) return
        viewModelScope.launch {
            val current = rsDao.getSettings(userId) ?: RoomSettings(userId)

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
        if (!isOwner) return
        viewModelScope.launch {
            val currentSettings = rsDao.getSettings(userId) ?: RoomSettings(userId)
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
        if (!isOwner) return
        viewModelScope.launch {
            val settings = rsDao.getSettings(userId) ?: return@launch
            val updatedMap = settings.placedAchievements
                .filterValues { it != achievementId }

            rsDao.upsertRoomSettings(settings.copy(placedAchievements = updatedMap))
        }
    }

    fun removePlacedAchievements() {
        if (!isOwner) return
        viewModelScope.launch {
            val settings = rsDao.getSettings(userId) ?: return@launch

            rsDao.upsertRoomSettings(settings.copy(placedAchievements = emptyMap()))
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