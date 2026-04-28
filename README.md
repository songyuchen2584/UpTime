# UpTime

A streak-based Android app that links screen time reduction to physical activity. Users maintain daily streaks by staying under their screen time limit and completing their walking goal.

Built with Kotlin, Jetpack Compose, Room, Firebase, and Retrofit.

## Features

- **Dashboard** — progress rings for screen time and walking, streak card, daily goals, motivational quote via Retrofit API
- **Screen Time Tracking** — tracks selected app usage via UsageStatsManager with app picker
- **Walking Tracking** — step counter + Health Connect with background foreground service
- **Streak System** — daily streak with monthly calendar view and weekly summary
- **Room Customization** — isometric room with themes, trophy cases, and achievements
- **Auth** — Firebase anonymous guest mode, email/password sign-up with guest-to-account linking
- **Profile & Friends** — add/remove friends by email, view friends' streaks and trophies via Firestore
- **Settings** — goal configuration, account management

<table>
  <tr>
    <td><img width="301" height="601" alt="Dashboard" src="https://github.com/user-attachments/assets/59d72fa4-9c7c-4b03-ab8a-e880fa155be5" /></td>
    <td><img width="300" alt="Room" src="https://github.com/user-attachments/assets/e6090d1e-eb06-481d-95ac-c6c494dd40e2" /></td>
    <td><img width="300" alt="Friend's List" src="https://github.com/user-attachments/assets/d830117d-bacc-4fab-822a-7e7b1e97a3ca" /></td>
  </tr>
</table>



## Architecture

MVVM pattern with reactive data flow:

```
Sensors / APIs → ViewModel → Room DB → Flow → collectAsState() → Compose UI
```

### Project Structure

```
com.example.uptime/
├── api/            # Retrofit service, Firestore sync
├── auth/           # Firebase AuthViewModel
├── dashboard/      # Dashboard screen + ViewModel
├── data/           # Room entities, DAOs, database, stats repository
├── profile/        # Profile screen, friends repository
├── room/           # Room customization, catalogs, Firebase sync
├── screentime/     # UsageStats tracking, app picker
├── streak/         # Streak report with calendar
├── walking/        # Step counter, Health Connect, foreground service
├── ui/theme/       # Material 3 theming
├── MainActivity.kt
├── NavDestination.kt
└── SettingsScreen.kt
```

### Database

**Room** — `daily_logs` (date, screenTimeMinutes, walkingMinutes, goals, streakMaintained), `room_settings` (theme, layout, placed items), `user_inventory` (points, unlocked achievements, purchased items)

**Firestore** — `users/{userId}` with email, name, streak, trophies, friends list, and room sync subcollection

### Tech Stack

Kotlin, Jetpack Compose, Room, DataStore, Retrofit + Gson, Firebase Auth, Cloud Firestore, Health Connect, UsageStatsManager, Foreground Service, WorkManager, Material 3

## Testing

- **Manual**: streak logic across multiple days, auth flow (anonymous → sign up → log out → log in), friend add/remove, walking/screen time in foreground and background
- **Database**: App Inspection to verify Room tables, Firestore Console for cloud data
- **Edge cases**: offline (API skips gracefully, Room persists), permission denied (shows rationale), day boundary (WorkManager handles midnight rollover)

## Debugging

- Room 2.6.1 incompatible with Kotlin 2.0.21 — KSP annotation processor crashed with `unexpected jvm signature V`, resolved by upgrading to Room 2.7.1
- AGP 9.0.1 + KSP source set conflict — resolved with `android.disallowKotlinSourceSets=false` in gradle.properties
- `java.time.LocalDate` requires API 26+ — bumped minSdk from 24 to 26
- Firestore user lookup failing — `saveUserProfile` wasn't called on login, only on sign-up. Added profile sync to login flow
- Compose delegate errors after file refactor — missing `getValue`/`setValue` imports dropped during Refactor → Move

## Team Workflow

Each team member worked on feature branches, merging into `dev` for integration testing before pushing to `main`. Branches included `feature/room-viewmodel`, `feature/streak-screen`, `feature/api-quote`, `NavSetup`, `healthconnect-demo`, and sensor/screen time branches. Code was reviewed by checking for conflicts before each merge, and features were built to avoid touching other members' files when possible.

## Team

| Member | Work                                                                                                                                                       |
| ------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Justin | Dashboard UI, Room DB + ViewModel, streak calendar, Retrofit API, Firebase Auth, Firestore friends, profile screen                                         |
| Cody   | Navigation (bottom bar + rail), Room customization (isometric canvas, themes, achievements), Room ViewModel + inventory, Firebase room sync, accessibility |
| Jevon  | Walking sensor (step counter + Health Connect), foreground service, screen time tracker (UsageStats + app picker), walking ViewModel, permissions          |

## Setup

1. Clone the repo
2. Place `google-services.json` in `app/` (obtain from team or Firebase Console)
3. Open in Android Studio, sync Gradle, run on device (API 26+)

## AI Usage

Used Claude for Room/ViewModel scaffolding (Entity, DAO, Database, ViewModel following class lecture patterns), Retrofit setup (QuoteService interface + Gson for ZenQuotes API), Firebase Auth and Firestore integration (anonymous auth, email/password sign-up, friends list repository), debugging, and organizing some parts of README.md.

**Rejected**: Hilt dependency injection (unfamiliar library, not necessary for our use case), DataStore for user preferences (stored goals directly in Room DailyLog entity instead — simpler, one source of truth).


## Stretch Goals

| Goal                          | Notes                                                                  |
| ----------------------------- | ---------------------------------------------------------------------- |
| View friends' rooms           | Firestore room sync implemented, rendering friend's room state is next |
| Room screenshot for profile   | Capture Canvas as bitmap, upload to Firebase Storage                   |
| Weekly point rewards (50 pts) | Complete                                                               |
| Notification system           | Streak reminders and screen time warnings in progress                  |
| Cross-device data sync        | Firestore infrastructure in place, need to sync daily logs             |
| Smartwatch companion          | Descoped — multiplied project scope                                    |
