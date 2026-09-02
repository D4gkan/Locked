# Locked

A personal focus/discipline app for Android. Detects Instagram, TikTok, and
Brave coming to the foreground, interrupts with a slow motivational message
sequence, then requires an uninterrupted 20-second hold + confirmation to
proceed -- once, for that single opening. Also includes an optional
once-a-day morning self-hypnosis session (spoken via TextToSpeech).

## Opening the project

1. Open this folder directly in Android Studio (File > Open). Studio will
   generate the Gradle wrapper jar/scripts automatically on first sync --
   they aren't included in this export since they're binary files. If it
   doesn't offer to, run `gradle wrapper` once from a terminal with Gradle
   installed, or just let Studio's "Sync Project with Gradle Files" handle it.
2. `compileSdk`/`targetSdk` are set to 35 (Android 15) in
   `app/build.gradle.kts`. If your installed SDK has Android 16 (API 36)
   available, bump both to 36 -- nothing else needs to change.
3. Build and install onto your Galaxy S25 Ultra as normal.

## First run / permissions

On first launch you'll see three permission prompts (Accessibility,
"display over other apps", and Notifications) -- Locked can't function
without the first two. Grant them, then you land on the home screen.

## App icon

`icon.png` wasn't attached in this session, so the launcher icon is a
placeholder vector padlock (`app/src/main/res/drawable/ic_launcher_foreground.xml`).
To swap in your real icon:

1. Export it as a set of `mipmap-*/ic_launcher_foreground.png` (or a single
   large PNG and let Android Studio's Image Asset tool
   [right-click `res` > New > Image Asset] generate the adaptive icon set
   for you -- easiest route).
2. Delete the two placeholder vector drawables and let the wizard replace
   `mipmap-anydpi-v26/ic_launcher.xml` / `ic_launcher_round.xml`.

## Morning session music

No audio asset is bundled. Drop a royalty-free ambient loop at
`app/src/main/assets/morning_ambient.mp3` and it's picked up automatically
(see `PUT_MUSIC_HERE.txt` in that folder). Without it, the morning session
still runs fine, just without background music.

## What's intentionally not here yet

- No app-selection UI (packages are hard-coded in `ProtectedApps.kt`, by
  design, per the spec).
- No bypass of any kind on the 20-second hold -- this is deliberate.
- No statistics/streaks/analytics -- deliberate.

## Architecture at a glance

- `service/ProtectionAccessibilityService.kt` -- event-driven foreground-app
  detection (TYPE_WINDOW_STATE_CHANGED), launches the block screen.
- `service/ProtectionForegroundService.kt` -- persistent notification for
  process resilience + `ACTION_USER_PRESENT` listener for the morning
  trigger.
- `unlock/UnlockState.kt` -- the one-time-unlock flag, in memory only.
- `ui/block/` -- the full block-screen flow (messages -> hold -> confirm).
- `ui/morning/` -- the morning self-hypnosis session.
- `data/` -- editable message/script lists + DataStore settings.

## Known limitation

This was generated without access to an Android SDK/emulator, so it hasn't
been compiled in this environment -- review it in Android Studio and let
its inline error checking catch anything before your first build.
