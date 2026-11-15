# InstaMusic Android

Android native wrapper app for [https://instamusic.lovable.app](https://instamusic.lovable.app), built with AndroidX Media3 (`MediaSessionService` & `MediaSession`), persistent background playback, lock-screen controls, Bluetooth AVRCP, Android Auto car app integration, and offline library support.

## Key Features
- **AndroidX Media3 Interactive Notification**: Persistent media card in System Quick Settings with previous, play/pause, next, and seek/scrubber controls.
- **Lock Screen & Bluetooth**: Full remote playback synchronization with lock screen widgets, Bluetooth wireless earbuds, car stereos, and steering wheel buttons.
- **Android Auto**: Official automotive media app declaration (`automotive_app_desc.xml`) and queue integration.
- **Background Continuity**: Partial WakeLock keeps audio playback and queue progression smooth while the phone is locked or other apps are running.
- **Audio Focus & Becoming Noisy**: Automatically pauses playback when headphones/Bluetooth disconnect or incoming phone calls arrive.
- **In-App Updates**: Automatic version check and seamless in-app APK updater.
- **Android Test Plan**: Complete test suite and ADB hardware simulation commands in [`TEST_PLAN.md`](./TEST_PLAN.md).

## Download & Releases
Latest APK release: [https://github.com/rapar5201-oss/InstaMusic-Android/releases/latest](https://github.com/rapar5201-oss/InstaMusic-Android/releases/latest)
