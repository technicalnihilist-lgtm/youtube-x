# YouTube X

![YouTube X UI preview](https://github.com/technicalnihilist-lgtm/youtube-x/releases/download/v1.1/preview.png)

> Upar wali image **UI preview (mockup)** hai — app ke asli layouts se banaya gaya render,
> kisi phone ka screenshot nahi.

Ek chhota Android app (Kotlin) jo YouTube ka data **NewPipeExtractor v0.26.5** se laata hai —
search, feed aur in-app video playback. Koi login nahi, koi ads nahi.

## Download APK

**[YouTubeX-1.1-debug.apk](https://github.com/technicalnihilist-lgtm/youtube-x/releases/latest)** — 11.8 MB

Install: APK phone me copy karo → file manager se kholo → "unknown sources" allow karo → Install.
Package `com.youtubex.app`, version 1.1 (versionCode 2), **minSdk 26** (Android 8.0+).

APK debug-signed hai — apne phone par test karne ke liye theek, Play Store ke liye apna release
keystore chahiye.

## Features

| Screen | Kya hota hai |
| --- | --- |
| Main screen | App khulte hi feed load hoti hai (pehla working kiosk, warna search fallback) |
| Search | Search bar se YouTube search — title, channel, views, duration, thumbnail |
| Player | Video tap → ExoPlayer (media3), plus "YouTube par kholo" button |

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
├── MainActivity.kt            search + feed screen
├── PlayerActivity.kt          ExoPlayer playback screen
├── data/DownloaderImpl.kt     NewPipeExtractor ka network layer (HttpURLConnection)
├── data/YtClient.kt           wrapper: search(), trending(), playable()
├── model/VideoItem.kt         VideoItem / Playable / SourceKind
├── ui/VideoAdapter.kt         RecyclerView adapter (Coil thumbnails)
└── util/Formats.kt            duration / views formatting
```

## Changelog

**v1.1**
- Fix: app khulte hi feed load nahi hoti thi. NewPipeExtractor ka default YouTube kiosk (`live`)
  aur `Trending` dono NPE dete hain (YouTube ne Trending page hata diya). Ab app
  `trending_music` → `trending_movies_and_shows` → baaki kiosks try karta hai, aur sab fail hone par
  search-based feed dikhata hai.

**v1.0** — pehla build: search, feed, playback.

## Notes

- APK build hua hai (AGP 8.5.2 / Gradle 8.9 / Kotlin 1.9.24) lekin kisi real device par install karke
  test nahi kiya gaya.
- YouTube kabhi kabhi stream URL par 403 deta hai (throttling); tab app error dikhata hai aur
  "YouTube par kholo" button kaam aata hai.
- Age-restricted / members-only / login maangne wale videos nahi chalenge.
- NewPipeExtractor **GPL-3.0** hai, isliye ye project bhi GPL-3.0 hai. YouTube ke Terms of Service ka
  khayal rakho — personal use theek hai, Play Store publish risky hai.
