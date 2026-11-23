# InstaMusic Android Test Plan: Media3 Background Playback & Transport Controls

## 1. Executive Summary & Objective

This test plan defines the complete verification strategy for InstaMusic's **AndroidX Media3 MediaSession** architecture, ensuring flawless background playback and remote transport control across:
- **Android Media Notifications** (System Quick Settings Media Player on Android 11–14+)
- **Lock Screen Media Controls** (AOD and active lock screen)
- **Bluetooth Devices & Car Stereos** (AVRCP key events, steering wheel controls, headset buttons)
- **Android Auto** (Desktop Head Unit / vehicle infotainment displays via `automotive_app_desc.xml`)
- **Background Execution & Battery Optimization** (Partial WakeLock, Audio Focus, Becoming Noisy handling)

---

## 2. Architecture & Control Flow

```
+-----------------------------------------------------------------------------------+
|                            EXTERNAL HARDWARE & CONTROLLERS                         |
|  [ Notification ]   [ Lock Screen ]   [ Bluetooth Headset / Car ]   [ Android Auto ]|
+-----------------------------------------------------------------------------------+
                                         |
                                         v (Media3 Protocol / AVRCP / KeyEvents)
+-----------------------------------------------------------------------------------+
|                        MusicPlaybackService (MediaSessionService)                  |
|  - MediaSession + MediaSession.Callback (Grants all player/session commands)     |
|  - InstaMusicPlayer (SimpleBasePlayer)                                             |
|      * handleSetPlayWhenReady() -> Dispatches ACTION_PLAY / ACTION_PAUSE          |
|      * handleSeek() -> Dispatches ACTION_NEXT / ACTION_PREV / ACTION_SEEK         |
|      * getState() -> STATE_READY when track is loaded (preserves paused state)    |
|  - PowerManager.WakeLock (PARTIAL_WAKE_LOCK ensures CPU stays active)             |
|  - AudioManager Audio Focus & ACTION_AUDIO_BECOMING_NOISY Listener                |
+-----------------------------------------------------------------------------------+
                                         |
                                         v (Internal Broadcast Intents)
+-----------------------------------------------------------------------------------+
|                                MainActivity                                        |
|  - BroadcastReceiver (listens for PLAY, PAUSE, NEXT, PREV, SEEK)                  |
|  - Evaluates JavaScript bridges: __triggerMediaAction(), __triggerMediaSeek()     |
+-----------------------------------------------------------------------------------+
                                         |
                                         v (DOM / MediaSession Bridge)
+-----------------------------------------------------------------------------------+
|                               media_bridge.js                                     |
|  1. Dispatches to navigator.mediaSession action handlers (play, pause, skip, seek)|
|  2. Fallback to DOM button clicks (aria-label="Next", aria-label="Previous")      |
|  3. Direct HTML5 <audio> playback and scrubber input range synchronization        |
|  4. Live metadata reporting back to MusicPlaybackService                          |
+-----------------------------------------------------------------------------------+
```

---

## 3. Test Prerequisites & Equipment

1. **Test Devices**:
   - Primary: Android 13 or 14+ device (SystemUI media player with progress bar scrubbing).
   - Secondary: Android 11 or 12 device (legacy NotificationCompat / standard MediaStyle).
2. **Bluetooth Equipment**:
   - Bluetooth headphones with single/double/triple tap gestures or dedicated buttons.
   - Car audio system or Bluetooth speaker with AVRCP playback controls.
3. **Android Auto Environment**:
   - Google Desktop Head Unit (DHU) via Android Studio SDK, or a real Android Auto-compatible head unit.
4. **Tools**:
   - Android SDK platform-tools (`adb`).
   - USB cable with developer options & USB debugging enabled.

---

## 4. Test Suite 1: Media Notification & System Quick Settings Controls

| Test ID | Title | Description | Expected Result | Pass/Fail |
| :--- | :--- | :--- | :--- | :--- |
| **TC-NOTIF-01** | Persistent Notification Display | Start playback in InstaMusic and swipe down notification shade. | Persistent media notification appears with current track title, artist name, album, and artwork. | |
| **TC-NOTIF-02** | Play / Pause Toggle | Tap Play/Pause button in notification card. | Audio immediately pauses/resumes. The button icon flips instantaneously without lag. | |
| **TC-NOTIF-03** | Next Track Skip | Tap Next button in notification card. | The app advances to the next track in queue. Title, artist, duration, and album artwork refresh. | |
| **TC-NOTIF-04** | Previous Track Skip | Tap Previous button in notification card. | The app skips to previous track in queue (or restarts current song). Metadata updates accordingly. | |
| **TC-NOTIF-05** | Progress Bar Seeking | Drag the scrubber slider on the Android 13/14 system media card. | Audio smoothly seeks to the selected position. Playback continues uninterrupted from that point. | |
| **TC-NOTIF-06** | Upcoming Track Indication | Check system media player details while playing from a queue. | Upcoming track title and artist are correctly reported in Media3 timeline. | |
| **TC-NOTIF-07** | Notification Dismissal on Stop | Stop playback and close the player. | Foreground service stops cleanly, and notification can be swiped away. | |

---

## 5. Test Suite 2: Lock Screen Media Controls

| Test ID | Title | Description | Expected Result | Pass/Fail |
| :--- | :--- | :--- | :--- | :--- |
| **TC-LOCK-01** | Lock Screen Media Widget | Start playback and lock the phone screen. Wake screen. | Lock screen displays InstaMusic media controls with artwork, track title, artist, and elapsed time. | |
| **TC-LOCK-02** | Screen-Off Continuity | Play music, lock the phone, and wait 10 minutes. | Playback continues without stuttering or stopping (verified by Partial WakeLock). | |
| **TC-LOCK-03** | Lock Screen Play/Pause | Press Play/Pause button on lock screen while phone is locked. | Playback pauses/resumes instantly without requiring fingerprint or PIN unlock. | |
| **TC-LOCK-04** | Lock Screen Next / Previous | Press Next or Previous buttons on lock screen. | Next/previous track plays smoothly while phone remains locked. Metadata updates on screen. | |
| **TC-LOCK-05** | Lock Screen Scrubber Scrubbing | Scrub progress slider on lock screen (Android 13+). | Track seeks to exact scrubbed timestamp without unlocking device. | |
| **TC-LOCK-06** | Tap Widget to Open App | Tap on the lock screen media card body. | Unlocks device and brings `MainActivity` to foreground with Now Playing view. | |

---

## 6. Test Suite 3: Bluetooth & Wireless Headset / Car Controls

| Test ID | Title | Description | Expected Result | Pass/Fail |
| :--- | :--- | :--- | :--- | :--- |
| **TC-BT-01** | AVRCP Metadata Display | Connect device to Bluetooth car stereo or smartwatch. | Car dashboard / watch screen displays song title, artist, and playback status. | |
| **TC-BT-02** | Single-Tap Play/Pause | Press single-button / multi-function button on Bluetooth headset. | Triggers `KEYCODE_MEDIA_PLAY_PAUSE`; audio toggles between playing and paused. | |
| **TC-BT-03** | Double-Tap Next Track | Double-click headset button or press dedicated Next button on car steering wheel. | Triggers `KEYCODE_MEDIA_NEXT`; skips to next track immediately. | |
| **TC-BT-04** | Triple-Tap Previous Track | Triple-click headset button or press dedicated Prev button on car steering wheel. | Triggers `KEYCODE_MEDIA_PREVIOUS`; returns to previous track. | |
| **TC-BT-05** | Fast-Forward / Rewind (10s) | Long-press Next/Prev on Bluetooth device or car dial. | Triggers `COMMAND_SEEK_FORWARD` / `COMMAND_SEEK_BACK`; seeks +/- 10 seconds. | |
| **TC-BT-06** | Audio Becoming Noisy | Disconnect Bluetooth or unplug wired earphones while music is playing. | Triggers `ACTION_AUDIO_BECOMING_NOISY`; audio pauses immediately without blasting speaker. | |

---

## 7. Test Suite 4: Android Auto Integration & MediaLibraryService

### Sideloaded APK Setup for Android Auto (Required for Direct APK Installs)
Because this APK is distributed directly via GitHub Releases and in-app updates rather than Google Play Store, Android Auto requires enabling Unknown Sources in Android Auto Developer Settings on the phone:
1. Open Android phone **Settings** -> Search for **Android Auto**.
2. Scroll to the very bottom and tap **Version and permission info** 10 times consecutively to enable **Developer settings**.
3. Tap the 3-dots menu in the top right -> Select **Developer settings**.
4. Check **Unknown sources** ("Allow apps from unknown sources to be shown in Android Auto").
5. Under **Customize launcher**, ensure **InstaMusic** is checked.
6. Connect to car / head unit or Desktop Head Unit (DHU) via USB or Wireless. InstaMusic will now appear directly on your car screen!

| Test ID | Title | Description | Expected Result | Pass/Fail |
| :--- | :--- | :--- | :--- | :--- |
| **TC-AUTO-01** | App Discovery in Android Auto | Connect phone to Android Auto (DHU or in-vehicle display). | InstaMusic icon appears in the car media launcher via `MediaLibraryService` and `automotive_app_desc.xml`. | |
| **TC-AUTO-02** | Media Library Browse Tree | Tap InstaMusic on the car screen to browse categories. | Root tabs appear: "Recently Played", "Top Hits & Trending", "AI Mood Mixes", and "Favorites". | |
| **TC-AUTO-03** | Song Selection from Car Screen | Tap any track from Recently Played or Trending playlists. | Track loads into playback engine, notifies WebView, and begins playing immediately. | |
| **TC-AUTO-04** | Now Playing Screen Display | View Now Playing screen while playback is active. | Car screen renders track title, artist name, high-resolution album artwork, and progress bar. | |
| **TC-AUTO-05** | Car Transport Controls | Tap Next, Previous, and Play/Pause on car touchscreen or steering wheel. | Commands execute instantly without delay or audio dropout. | |
| **TC-AUTO-06** | Queue / Next Track Item Selection | Select the upcoming track from the car display queue. | Advances smoothly to next track with full metadata and artwork. | |
| **TC-AUTO-07** | Seamless Phone/Car Handover | Disconnect car cable / wireless connection while playing. | Audio pauses gracefully; reconnecting restores session cleanly. | |

## 8. Test Suite 5: Background Continuity & Power Management

| Test ID | Title | Description | Expected Result | Pass/Fail |
| :--- | :--- | :--- | :--- | :--- |
| **TC-BG-01** | Multitasking App Switching | Start playback, switch to Google Maps or Chrome, and browse heavy sites. | Music continues playing in the background without stuttering or memory kills. | |
| **TC-BG-02** | Deep Doze Mode Resilience | Leave phone locked and unplugged on a flat surface for 30 minutes. | `PARTIAL_WAKE_LOCK` prevents CPU sleep; playback and playlist progression continue uninterrupted. | |
| **TC-BG-03** | In-App Update Prompt Non-Interference | While playing music, trigger an update check or download. | Media playback continues smoothly without audio glitching during background download. | |

---

## 9. Test Suite 6: Audio Focus & Interruption Handling

| Test ID | Title | Description | Expected Result | Pass/Fail |
| :--- | :--- | :--- | :--- | :--- |
| **TC-FOCUS-01**| Incoming Phone Call | Call test phone while InstaMusic is playing. | Audio focus lost; playback pauses automatically. Notification updates to paused state. | |
| **TC-FOCUS-02**| Call Completion | Hang up incoming call. | System audio focus is restored; user can resume playback from notification or headset. | |
| **TC-FOCUS-03**| Navigation Voice Prompt | Play music while Google Maps or Waze gives a turn-by-turn spoken direction. | Audio pauses or ducks appropriately, resuming clear playback afterward. | |
| **TC-FOCUS-04**| Another Music App Starts | Start playing a YouTube video or Spotify track. | InstaMusic cleanly yields audio focus and pauses its player session. | |

---

## 10. Diagnostic Commands & Verification Tools

### Real-Time Logcat Monitoring
Run the following command while executing test cases to observe MediaSession and playback lifecycle:

```bash
adb logcat -v time -s InstaMusic MediaSession SimpleBasePlayer DefaultMediaNotificationProvider AudioFlinger:E
```

### Simulated Hardware Media Key Events via ADB
Verify headset / car steering wheel buttons directly from your workstation:

```bash
# Play / Pause Toggle
adb shell input keyevent KEYCODE_MEDIA_PLAY_PAUSE

# Skip to Next Track
adb shell input keyevent KEYCODE_MEDIA_NEXT

# Skip to Previous Track
adb shell input keyevent KEYCODE_MEDIA_PREVIOUS

# Fast-Forward (10s)
adb shell input keyevent KEYCODE_MEDIA_FAST_FORWARD

# Rewind (10s)
adb shell input keyevent KEYCODE_MEDIA_REWIND

# Headset Hook (Single tap)
adb shell input keyevent KEYCODE_HEADSETHOOK
```

### WakeLock Inspection
Verify that InstaMusic is holding a valid partial wakelock during background playback:

```bash
adb shell dumpsys power | grep -i "InstaMusic"
```

### Audio Focus Verification
Inspect active audio focus owner:

```bash
adb shell dumpsys audio | grep -A 8 "Audio Focus"
```


---

## 8. Old Hits, Golden Era Classics & Deep Search Verification

| ID | Test Scenario | Steps | Expected Result | Pass/Fail |
|---|---|---|---|---|
| **OH-01** | **Old Hits 📻 Tab on Home** | Tap the "Old Hits 📻" tab in the top navigation bar of the Home screen. | Dedicated Golden Era station appears with retro banner, 6 carousels (Golden Oldies, Maestro Ilaiyaraaja, SPB Classics, 90s Bollywood, Kishore & Lata, All-Time Legends), and Top 30 Evergreen chart. | [ ] |
| **OH-02** | **"Play All Golden Hits" One-Tap** | Tap "Play All Golden Hits" button in the Retro Radio Station banner. | Audio playback begins immediately with the first retro track and enqueues all classics in queue. | [ ] |
| **OH-03** | **Home "For You" Retro Shelves** | Scroll down the "For You" tab on Home. | "📻 Golden Oldies & Evergreen Hits", "🎼 90s & 2000s Nostalgia Rewind", "👑 Maestro Ilaiyaraaja & SPB Classics", and "🌟 Kishore Kumar & Lata Golden Era" appear with high-res artwork. | [ ] |
| **OH-04** | **Deep Multi-Page Search** | Search for "old hit songs", "SPB hits", "Ilaiyaraaja 80s", or "Kishore Kumar". | Search queries 3 deep pages plus complementary hit queries; returns 80–120+ unique songs without duplicates. | [ ] |
| **OH-05** | **Retro Quick Filters in Search** | Open Search screen before typing. | Quick filter pills for "Old Hit Songs", "80s Ilaiyaraaja Golden Hits", "SPB Evergreen Classics", "Kishore Kumar Hits", "90s Bollywood", etc. are displayed and clickable. | [ ] |
| **OH-06** | **Old Hits & Retro Genre Hub** | Tap "Old Hits & Retro" in Browse by Genre on Search screen. | Opens `/genre/oldhits` displaying dozens of classic tracks with 1-tap playback and queueing. | [ ] |
| **OH-07** | **Android Auto Old Hits Category** | In car display or DHU, browse the media root menu. | "Old Hits & Evergreen Classics" category is listed; tapping opens Kishore Kumar, Lata, SPB, Ilaiyaraaja, AR Rahman, Queen, and MJ classics. | [ ] |
