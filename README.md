# YouTube X

<img src="https://github.com/technicalnihilist-lgtm/youtube-x/releases/download/v1.7/ic_launcher_512.png" width="110" alt="YouTube X icon">

![YouTube X 1.5 poster](https://github.com/technicalnihilist-lgtm/youtube-x/releases/download/v1.6/poster-1.5.png)

Ek chhota Android app (Kotlin) jo YouTube ka data **NewPipeExtractor v0.26.5** se laata hai —
search, feed aur in-app video playback. Koi login nahi, koi ads nahi.

UI liquid-glass shell hai: frosted glass pills, radial colour tints, 24dp rounded 16:9 cards,
chips row, floating dock (Home / Explore / Subs + search), 4D depth tilt aur bounce/macOS
sheet animations. App khulte hi sirf **“YouTube X”** bounce karta hua dikhta hai, phir feed.

## Download APK

**[YouTubeX-1.8-debug.apk](https://github.com/technicalnihilist-lgtm/youtube-x/releases/latest)** — 12.3 MB

Install: APK phone me copy karo → file manager se kholo → "unknown sources" allow karo → Install.
Package `com.youtubex.app`, version 1.8 (versionCode 9), **minSdk 26** (Android 8.0+).
Icon ya poster update hone par purana app **uninstall karke** fresh install karo.

APK debug-signed hai — apne phone par test karne ke liye theek, Play Store ke liye apna release
keystore chahiye.

## Features

| Screen | Kya hota hai |
| --- | --- |
| Splash | Sirf “YouTube X” text, squash-and-hop bounce, ~0.8s baad fade out |
| Home | Feed (pehla working kiosk, warna search fallback), chips se category filter |
| Explore | Anime / Cartoon / Movies / Music / Gaming / Tech ke horizontal carousels |
| Subs | Subscribe kiye channels ka grid + latest videos (on-device) |
| Search | Full-screen glass search bar + YouTube autocomplete chips |
| Player | Rounded 16:9 ExoPlayer + glass sheet: Like, Save, Channel, Comments, YouTube, Close |
| Sheets | Channel, Comments, Library (history/liked/later/What's new), Settings |
| Settings | Theme, glass transparency, loop, 4D depth, depth intensity, bounce, macOS animations |

Stream chunne ka order: progressive (video+audio) → HLS → DASH. 1.5 poster app ke andar hai
(`drawable-nodpi/poster_1_5.webp`) aur You → “What's new” sheet me dikhta hai.

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
├── MainActivity.kt            splash + chips + feed + explore + subs + dock + search + sheets
├── PlayerActivity.kt          ExoPlayer playback screen (glass sheet + actions)
├── YtApp.kt                   theme (AppCompatDelegate) boot
├── data/DownloaderImpl.kt     NewPipeExtractor ka network layer (HttpURLConnection)
├── data/YtClient.kt           wrapper: search(), trending(), playable(), channel(), comments()
├── data/Prefs.kt              history / liked / later / subs / settings (SharedPreferences)
├── model/VideoItem.kt         VideoItem / Playable / SourceKind
├── ui/VideoAdapter.kt         card adapter + 4D depth tilt
├── ui/Ui.kt                   glass sheet show/hide (bounce & macOS), rows, toast
└── util/Formats.kt            duration / views formatting

app/src/main/res/
├── anim/splash_bounce.xml     splash ka squash-and-hop bounce
├── mipmap-*dpi/               icon (ic_launcher, ic_launcher_round, ic_launcher_foreground)
├── mipmap-anydpi-v26/         adaptive icon (art 58% safe zone me)
└── drawable-nodpi/            poster_1_5.webp
```

## Changelog

**v1.8** — naya splash: sirf “YouTube X”, bounce karta hua; logo mark, poster aur loader hata diye

**v1.7** — icon size fix (art launcher ke safe zone me)

**v1.6** — naya icon (glossy black/red) + 1.5 poster APK ke andar

**v1.5** — icon (YouTube screen + play mark + “YouTube X”), tilt/scale leak fix

**v1.4** — 4D depth effect + depth slider + bounce/macOS sheet animations

**v1.3** — poora shell: splash, Explore carousels, Subs grid, channel/comments/library sheets,
settings, player actions, search suggestions, toasts

**v1.2** — liquid-glass UI (cards, chips, dock, glass sheets)

**v1.1** — feed fix (default YouTube kiosk `live` NPE)

**v1.0** — pehla build

## Notes

- APK debug-signed hai; real device screenshots repo releases me hain.
- Glass effect native drawables se bana hai (real backdrop blur / SVG refraction nahi).
- YouTube kabhi kabhi stream URL par 403 deta hai (throttling); tab app error dikhata hai aur
  "YouTube par kholo" button kaam aata hai.
- Age-restricted / members-only / login maangne wale videos nahi chalenge.
- NewPipeExtractor **GPL-3.0** hai, isliye ye project bhi GPL-3.0 hai. YouTube ke Terms of Service ka
  khayal rakho — personal use theek hai, Play Store publish risky hai.
