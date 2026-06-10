# Scroll Tax - Implementation Guide

## Quick Start

### 1. Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or higher
- Android SDK API 34
- Gradle 8.4

### 2. Open Project
1. Extract the ScrollTax.zip file
2. Open Android Studio
3. Select "Open an existing project"
4. Navigate to the ScrollTax folder
5. Wait for Gradle sync to complete

### 3. Build APK
```bash
# Using command line
./gradlew assembleDebug

# Or use the build script
./build_apk.sh
```

### 4. Install on Device
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Architecture Overview

### Module Structure
```
ScrollTax/
├── app/                    # Main application module
│   ├── data/              # Data layer (models, database, repositories)
│   ├── di/                # Dependency injection (Hilt modules)
│   ├── ui/                # Compose UI screens
│   │   ├── dashboard/     # Dashboard screen
│   │   ├── onboarding/    # 6-step onboarding flow
│   │   ├── settings/      # Settings screen
│   │   └── theme/         # Material Design 3 theme
│   └── services/          # Tracking & overlay services
├── tracking/              # Usage stats tracking service
├── intervention/          # Overlay intervention system
├── analytics/             # Analytics & reporting
├── onboarding/            # Onboarding flow module
├── settings/              # Settings management
└── billing/               # Premium billing (future)
```

## Key Features Implemented

### 1. Onboarding Flow (6 Screens)
1. **Welcome Screen** - App introduction and value proposition
2. **Permission Screen** - Usage access and overlay permission education
3. **App Selection** - Choose apps to track from installed apps
4. **Sensitivity** - Low/Medium/High sensitivity selection
5. **Monkey Setup** - Choose tone (Funny, Balanced, Savage)
6. **Success** - Completion and first-day expectations

### 2. Tracking Engine
- Foreground service with partial wake lock
- Polling-based usage stats detection (1-second intervals)
- Session state machine (Idle → Opened → Warning → Ignored → Monkey → Exit/Complete)
- Reopen detection within 15/5 minutes
- Night usage detection (configurable bedtime hours)

### 3. Scoring Model
| Event | Score |
|-------|-------|
| Open event | +1 |
| Reopen within 15 min | +2 |
| Reopen within 5 min | +3 |
| Night usage | +2 |
| Session over 60 sec | +2 |
| Session over 180 sec | +4 |
| Prior ignores | +2 each |

### 4. Intervention System
| Score | Intervention |
|-------|------------|
| 1-2 | Small chip (top) |
| 3-4 | Strong chip (top, more prominent) |
| 5-6 | Intent card (center modal) |
| 7+ | Monkey overlay (bottom with animation) |

### 5. Dashboard Widgets
- Today's impulse opens
- Trap apps (most opened tracked apps)
- Monkey interventions count
- Saved minutes estimate
- Night scroll report
- 7-day trend chart
- Quick pause toggle (1 hour)

### 6. Settings
- Track apps (add/remove)
- Excluded apps (banking, emergency)
- Bedtime hours (start/end time)
- Monkey on/off toggle
- Motion intensity (reduced motion)
- Strictness level (1-5 slider)
- Silent mode
- Weekly report toggle
- Data export/delete

## Privacy & Trust
- All data stored locally on device
- No cloud sync in MVP
- No message content reading
- No screen recordings
- No data selling
- Clear permission explanations
- One-tap data deletion

## Database Schema (Room)

### Tables
1. **tracked_apps** - Apps selected for tracking
2. **app_open_events** - Each app open/close event
3. **session_tax_results** - Session outcome records
4. **user_settings** - User preferences
5. **daily_summaries** - Aggregated daily stats
6. **intervention_events** - Each intervention shown/dismissed
7. **session_states** - Active session tracking

## Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### QA Checklist
- [ ] Interventions appear only on selected apps
- [ ] Banking/payment apps excluded by default
- [ ] Overlay never blocks critical actions
- [ ] Monkey animation respects reduced-motion
- [ ] Usage stats accurate across app switches
- [ ] Night tax starts/ends correctly
- [ ] Session labels stored correctly
- [ ] Battery impact acceptable
- [ ] No ANRs, crashes, or stuck overlays
- [ ] All interventions dismiss cleanly

## Known Limitations (MVP)
- Single monkey character (3 emotional states only)
- No multi-character marketplace
- No social leaderboards
- No voice packs
- No cloud sync
- No AI coaching
- No group challenges
- Local storage only

## Phase 2 Roadmap
- More monkey expressions and skins
- Personalized interventions by app/time
- Weekly narrative reports
- Friend accountability mode
- Family mode
- Smart bedtime automation
- Streak recovery mechanics
- Premium mascot packs

## Troubleshooting

### Build Errors
1. **Gradle sync fails**: Check JDK version (must be 17+)
2. **Hilt errors**: Ensure KSP plugin is applied
3. **Compose errors**: Check Kotlin version compatibility

### Runtime Issues
1. **No tracking**: Check Usage Access permission in Settings
2. **No overlays**: Check "Display over other apps" permission
3. **Battery optimization**: Add app to battery whitelist

## Support
For issues or questions, refer to the project documentation or contact the development team.

---
**Scroll Tax v1.0.0** - Break the scroll loop!
