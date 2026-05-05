# UpTime

A streak-based Android app that links screen time reduction to physical activity. Users maintain daily streaks by staying under their screen time limit and completing their walking goal.

Built with Kotlin, Jetpack Compose, Room, and Firebase.

## Features

- **Dashboard** — progress rings for screen time and walking, streak card, daily goals
- **Screen Time Tracking** — tracks selected app usage via UsageStatsManager with app picker
- **Walking Tracking** — step counter + Health Connect with background foreground service
- **Streak System** — daily streak with monthly calendar view (tap any day for details), weekly summary
- **Room Customization** — isometric room with themes, wood finishes, trophy cases, achievements, and point-based shop
- **Auth** — Firebase anonymous guest mode, email/password sign-up with guest-to-account linking
- **Profile & Friends** — customizable profile icons, add/remove friends by email, view friends' streaks/trophies, visit friends' rooms
- **Notifications** — streak reminders, screen time warnings, walking goal alerts with customizable settings
- **Onboarding** — dashboard setup checklist guiding users through walking, screen time, notifications, and sign-up configuration
- **Settings** — goal configuration, account management, screen time and walking goal navigation
- **Cloud Sync** — room settings and inventory sync to Firestore, persists across reinstalls for signed-in users

<br>

<table>
  <tr>
    <td><img width="313" height="634" alt="Screenshot 2026-05-05 at 5 17 36 PM" src="https://github.com/user-attachments/assets/ef9880e9-4b2a-4947-9537-363019a7885b" /></td>
    <td><img width="309" height="614" alt="Screenshot 2026-05-05 at 5 17 48 PM" src="https://github.com/user-attachments/assets/c5ba93fe-9cfb-4b27-acfd-5cac05e1fc80" /></td>
    <td><img width="313" height="608" alt="Screenshot 2026-05-05 at 5 17 59 PM" src="https://github.com/user-attachments/assets/83e97f25-9036-4563-bf3a-7fceff31028b" /></td>
  </tr>
</table>
<br>


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

### Tool
Claude (Anthropic)
ChatGPT
<br>

### How AI was used
Used Claude for Room/ViewModel scaffolding (Entity, DAO, Database, ViewModel), Retrofit setup (QuoteService interface + Gson for ZenQuotes API) which we dropped eventually, Firebase Auth and Firestore integration (anonymous auth, email/password sign-up, friends list repository), UI iteration, debugging, and organizing parts of README.md.
<br>

ChatGPT was used to debug Android-specific issues, such as permission handling (e.g., understanding why permission dialogs stop appearing after repeated denials and implementing proper fallback behavior), but all suggestions were tested and adjusted to match the app’s architecture and ensure correct behavior.

All AI-generated code was verified and tested on physical devices. 
<br>

### Where it influenced the project
- **Architecture**: Set up Room for local data and Firestore for cloud/social features.
- **Code**: Generated auth flow and friends system. 
- **UX**: Iterated on dashboard layout, streak calendar, and profile screen.
- **Debugging**: Identified Room/KSP version mismatch, API level fix, Firestore lookup failure on login, and Firestore room sync race condition (anonymous defaults overwriting cloud data on sign-in).

### What AI accelerated
Room database setup, Firebase Auth with anonymous-to-account linking, and Firestore friends system — each implemented in single sessions.
<br>

### Rejected suggestions
**Rejected**: Hilt dependency injection (unfamiliar library, not necessary for our use case), DataStore for user preferences (stored goals directly in Room DailyLog entity instead. It was simpler and had one source of truth).


