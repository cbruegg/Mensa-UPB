# AGENTS.md

This repository is an Android app. The default expectation after making a code change is to verify it on a physical device over `adb`, not just by reading code.

## Scope

These instructions apply to the whole repository.

## Change Flow

1. Make the code change.
2. Build a debug APK:
   `./gradlew :app:assembleDebug`
3. Confirm a device is connected:
   `adb devices -l`
4. Install the updated APK:
   `adb install -r app/build/outputs/apk/debug/app-debug.apk`
5. If state may affect the result, clear app data:
   `adb shell pm clear com.cbruegg.mensaupb`
6. Launch the app:
   `adb shell am start -n com.cbruegg.mensaupb/.main.MainActivity`
7. Reproduce the changed behavior on-device.
8. Verify with `adb` evidence when useful:
   `adb exec-out screencap -p > /tmp/mensa-check.png`
   `adb exec-out uiautomator dump /dev/tty`
   `adb logcat -d -v brief`

## Verification Standard

- Prefer verifying on the USB-connected Android device.
- If the app has a direct intent path to the changed screen, use it instead of manual tapping.
- For UI changes, capture either a screenshot, a UI hierarchy dump, or both.
- For data-loading changes, inspect `logcat` for fetch, cache, and error output.
- If device verification is blocked, say exactly what blocked it.
- Prefer testing with Mensa Academica

## Project Notes

- The app build requires `MENSA_UPB_API_KEY` in the environment.
- Use debug builds unless the task explicitly requires release behavior.
- Avoid resetting unrelated local changes in the worktree.
