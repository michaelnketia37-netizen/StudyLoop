# 📚 StudyLoop — Android App

Spaced Repetition · Forgetting Curve · Productivity Suite

---

## 🚀 Quick Start (5 steps to running in Android Studio)

### Step 1 — Open Project
- Open Android Studio → **File → Open**
- Select this `StudyLoop/` folder
- Click **OK** and wait for Gradle sync (~3–5 min first time)

### Step 2 — Firebase Setup (required)
1. Go to [console.firebase.google.com](https://console.firebase.google.com)
2. Create new project → **StudyLoop**
3. Click **Add app** → Android icon
4. Package name: `com.studyloop`
5. Download `google-services.json`
6. **Replace** `app/google-services.json` with the downloaded file

### Step 3 — AdMob Setup (optional, uses test IDs by default)
The app ships with Google's **test Ad Unit IDs** — safe for development.
When ready to publish, replace in:
- `AndroidManifest.xml` → `APPLICATION_ID` meta-data
- `activity_main.xml` → banner `adUnitId`
- `CalcFragment.kt` → `RewardedAd.load(...)` ad unit ID

Real IDs from: [admob.google.com](https://admob.google.com)

### Step 4 — Run
- Select **Pixel 7 API 34** emulator (or plug in real device)
- Press ▶ **Run** (Shift+F10)

### Step 5 — You're live! 🎉

---

## 📂 Project Structure

```
com.studyloop/
├── App.kt                    ← Application class (Hilt + AdMob + Channels)
├── MainActivity.kt           ← Bottom nav host
│
├── core/
│   ├── database/
│   │   ├── AppDatabase.kt    ← Room database (5 tables)
│   │   ├── Daos.kt           ← All DAOs (Reminder/Review/Note/Todo/Alarm)
│   │   └── DatabaseModule.kt ← Hilt injection
│   ├── model/
│   │   └── Entities.kt       ← All Room entities
│   └── notifications/
│       ├── NotificationHelper.kt
│       └── ReviewBroadcastReceiver.kt
│
├── alarm/
│   ├── AlarmFragment.kt      ← Live clock + alarm list
│   ├── AlarmViewModel.kt
│   ├── AlarmRepository.kt
│   └── AlarmReceiver.kt      ← BroadcastReceiver + BootReceiver + scheduling
│
├── reminder/
│   ├── ReminderFragment.kt   ← Cards with mini curves + review rings
│   ├── ReminderDetailFragment.kt ← Full MPAndroidChart forgetting curve
│   ├── ReminderViewModel.kt
│   ├── ReminderRepository.kt ← Saves + schedules 7 review notifications
│   └── SpacedRepetitionEngine.kt ← Ebbinghaus algorithm
│
├── notes/
│   ├── NotesFragment.kt      ← Staggered grid of color-coded notes
│   ├── NotesViewModel.kt
│   └── NotesRepository.kt
│
├── todo/
│   ├── TodoFragment.kt       ← Progress bar + checkbox list
│   ├── TodoViewModel.kt
│   ├── TodoRepository.kt
│   └── widget/
│       ├── TodoWidgetProvider.kt ← Home screen widget
│       └── TodoWidgetService.kt
│
├── calc/
│   ├── CalcFragment.kt       ← Mode selector + standard + scientific
│   ├── ScientificCalcEngine.kt ← Full trig/log/hyp/memory engine
│   └── StandardCalcEngine    ← Basic arithmetic (in CalcFragment)
│
└── sync/
    └── FirebaseSyncManager.kt ← Firestore cloud sync
```

---

## 🎯 Features

| Feature | Status |
|---------|--------|
| ⏰ Alarms with live clock | ✅ Complete |
| 🧠 Spaced repetition reminders | ✅ Complete |
| 📉 Forgetting curve graph (MPAndroidChart) | ✅ Complete |
| 📝 Color-coded staggered notes grid | ✅ Complete |
| ✅ To-Do with progress bar | ✅ Complete |
| 🔢 Standard calculator | ✅ Complete |
| 🔬 Scientific calculator (Casio-style) | ✅ Complete |
| 📱 Home screen To-Do widget | ✅ Complete |
| 🔔 7 spaced review notifications | ✅ Complete |
| 📊 AdMob rewarded ad gate for sci calc | ✅ Complete |
| ☁️ Firebase Firestore sync | ✅ Ready (needs google-services.json) |
| 🔐 Firebase Auth (Google Sign-In) | 🔜 Add in next sprint |

---

## 💰 Monetisation

The app is pre-wired for AdMob:
- **Banner ad** — shown at bottom of every screen
- **Rewarded video** — gates the scientific calculator (unlocks for session)

Replace test IDs with real ones before publishing. Estimated eCPM:
- Banner: $0.50–2.00
- Rewarded: $8–30 (highest CPM format)

---

## 🔑 Key Files to Customise Before Publishing

| File | What to change |
|------|----------------|
| `app/google-services.json` | Replace with real Firebase config |
| `AndroidManifest.xml` | Replace AdMob App ID |
| `activity_main.xml` | Replace banner Ad Unit ID |
| `CalcFragment.kt` | Replace rewarded Ad Unit ID |
| `build.gradle.kts` | Update `applicationId` if needed |
| Legal docs | Update privacy policy URL in Play Store listing |

---

## 📱 Publishing Checklist

- [ ] Replace `google-services.json`
- [ ] Replace test AdMob IDs with real ones
- [ ] Enable Firebase Auth + Firestore rules in Firebase Console
- [ ] Build → **Generate Signed Bundle/APK** → create keystore
- [ ] Upload `.aab` to Google Play Console
- [ ] Add store listing (screenshots, description, privacy policy URL)
- [ ] Submit for review (3–7 days for first submission)

---

*Built with ❤️ using Claude · Anthropic*
