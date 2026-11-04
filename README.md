# YouTube X

<img src="https://github.com/technicalnihilist-lgtm/youtube-x/releases/download/v1.5/ic_launcher_512.png" width="96" alt="YouTube X icon">

![YouTube X — real device screenshots](https://github.com/technicalnihilist-lgtm/youtube-x/releases/download/v1.5/preview.png)

> Upar wali image app ke **asli device screenshots** hain — Home feed (All / Movies / Gaming) aur Search.

Ek chhota Android app (Kotlin) jo YouTube ka data **NewPipeExtractor v0.26.5** se laata hai —
search, feed aur in-app video playback. Koi login nahi, koi ads nahi.

UI liquid-glass shell hai: frosted glass pills, radial colour tints, 24dp rounded 16:9 cards,
chips row, floating dock (Home / Explore / Subs + search), 4D depth tilt aur bounce/macOS
sheet animations.

## Download APK

**[YouTubeX-1.5-debug.apk](https://github.com/technicalnihilist-lgtm/youtube-x/releases/latest)** — 12.0 MB

Install: APK phone me copy karo → file manager se kholo → "unknown sources" allow karo → Install.
Package `com.youtubex.app`, version 1.5 (versionCode 6), **minSdk 26** (Android 8.0+).
Naya icon dikhane ke liye purana app uninstall karke fresh install karna best hai.

APK debug-signed hai — apne phone par test karne ke liye theek, Play Store ke liye apna release
keystore chahiye.

## Features

| Screen | Kya hota hai |
| --- | --- |
| Home | Feed (pehla working kiosk, warna search fallback), chips se category filter |
| Explore | Anime / Cartoon / Movies / Music / Gaming / Tech ke horizontal carousels |
| Subs | Subscribe kiye channels ka grid + latest videos (on-device) |
| Search | Full-screen glass search bar + YouTube autocomplete chips |
| Player | Rounded 16:9 ExoPlayer + glass sheet: Like, Save, Channel, Comments, YouTube, Close |
| Sheets | Channel (subscribe + videos), Comments, Library (history/liked/later), Settings |
| Settings | Theme, glass transparency, loop, 4D depth, depth intensity, bounce, macOS animations |

Stream chunne ka order: progressive (video+audio) → HLS → DASH.

## Build

1. Android Studio (Ladybug ya naya) → **Open** → ye folder. JDK 17 + Android SDK 34 chahiye.
2. Gradle sync hone do, phir Run.

Do choti baatein:

- `gradle/wrapper/gradle-wrapper.jar` aur `gradlew` scripts repo me commit nahi hain (binary).
  Release me **YouTubeX-android-project.zip** attached hai jisme poora project (wrapper + prebuilt
  jars) hai — wahi use karna sabse aasan hai. Ya phir `gradle wrapper --gradle-version 8.9` chala lo.
- `app/build.gradle.kts` me NewPipeExtractor JitPack se aata hai
  (`implementation("com.github.teamnewpipe:NewPipeExtractor:v0.26.5")`). Offline build ke liye
  release zip me `app/libs/` ke prebuilt jars hain.

## Structure

```
app/src/main/java/com/youtubex/app/
├── MainActivity.kt            chips + feed + explore + subs + dock + search + sheets
├── PlayerActivity.kt          ExoPlayer playback screen (glass sheet + actions)
├── YtApp.kt                   theme (AppCompatDelegate) boot
├── data/DownloaderImpl.kt     NewPipeExtractor ka network layer (HttpURLConnection)
├── data/YtClient.kt           wrapper: search(), trending(), playable(), channel(), comments()
├── data/Prefs.kt              history / liked / later / subs / settings (SharedPreferences)
├── model/VideoItem.kt         VideoItem / Playable / SourceKind
├── ui/VideoAdapter.kt         card adapter + 4D depth tilt
├── ui/Ui.kt                   glass sheet show/hide (bounce & macOS), rows, toast
└── util/Formats.kt            duration / views formatting
```

## Changelog

**v1.5** — naya app icon
- Launcher icon: YouTube jaisa white screen + red play mark, neeche **"YouTube X"** — adaptive icon
  (`mipmap-anydpi-v26`) plus mdpi…xxxhdpi PNGs
- Recycler view fix: card ka tilt/scale recycled views par carry nahi hota
- Screenshots updated (5 real device shots, search included)

**v1.4** — 4D depth effect + depth slider + bounce/macOS sheet animations

**v1.3** — poora shell: splash, Explore carousels, Subs grid, channel/comments/library sheets,
settings, player actions, search suggestions, toasts

**v1.2** — liquid-glass UI (cards, chips, dock, glass sheets)

**v1.1** — feed fix (default YouTube kiosk `live` NPE)

**v1.0** — pehla build

## Notes

- APK debug-signed hai; screenshots real device se hain, par har phone par behaviour thoda alag ho
  sakta hai.
- Glass effect native drawables se bana hai (real backdrop blur / SVG refraction nahi).
- YouTube kabhi kabhi stream URL par 403 deta hai (throttling); tab app error dikhata hai aur
  "YouTube par kholo" button kaam aata hai.
- Age-restricted / members-only / login maangne wale videos nahi chalenge.
- NewPipeExtractor **GPL-3.0** hai, isliye ye project bhi GPL-3.0 hai. YouTube ke Terms of Service ka
  khayal rakho — personal use theek hai, Play Store publish risky hai.
