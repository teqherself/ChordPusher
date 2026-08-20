# ChordPusher

Android controller application for ChordPusher by Gendemik Digital.

---

## Overview

ChordPusher Android v1.0 is a performance controller app that communicates bidirectionally
with the **ChordPusher Ableton Live 10 Remote Script** over Android USB MIDI (host mode).

Designed and optimised for the **Lenovo Tab M10 3rd Gen** in landscape orientation.

---

## Android App — ChordPusherAndroid-v1.0

### Opening in Android Studio

1. Open Android Studio (Electric Eel or later recommended).
2. Choose **File → Open** and navigate to the `ChordPusherAndroid-v1.0` folder in this
   repository.  Open **that exact folder** — do not open a parent or child directory.
3. Allow Gradle to sync.

### Building & Installing

```bash
cd ChordPusherAndroid-v1.0
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or use the **Run** button in Android Studio with a connected Lenovo Tab M10 3rd Gen.

### Requirements

| Item | Detail |
|------|--------|
| Android target device | Lenovo Tab M10 3rd Gen (TB328FU) |
| Android API | Min 26 (Android 8.0), Target 34 (Android 14) |
| Connectivity | USB-A to USB-B cable (OTG host mode) |
| Ableton Live | Version 10 with ChordPusher Remote Script installed |

---

## Hardware Setup

1. Connect the Lenovo Tab M10 3rd Gen to your audio interface / USB MIDI device
   (or directly to the computer running Ableton Live 10) using a **USB OTG cable**.
2. The app auto-detects USB MIDI devices on startup and when a device is plugged in.
3. Grant USB permission when prompted on first connect.

---

## Ableton Live 10 Setup

1. Copy the `ChordPusherRemoteScript` folder into your Ableton Live MIDI Remote Scripts
   directory:
   - **macOS**: `~/Music/Ableton/User Library/Remote Scripts/`
   - **Windows**: `%USERPROFILE%\Documents\Ableton\User Library\Remote Scripts\`
2. In Ableton Live 10: **Preferences → MIDI** → set Control Surface to **ChordPusher**,
   and select the USB MIDI device for both Input and Output.
3. In your Live Set, create a **Drum Rack** on MIDI channel 10 for drum pads, and
   instrument/MIDI tracks on MIDI channel 1 for chord pads.

---

## App UI

### CHORDS tab

- **8 × 4 pad grid** — 32 chord pads organised as:
  - Row 0 (bottom): chromatic single notes C–G
  - Row 1: C-major diatonic triads
  - Row 2: C-major diatonic 7th chords
  - Row 3 (top): common chord types (maj, min, dim, aug, sus2, sus4, 7, m7)
- **OCT** (left sidebar): raises/lowers the chord root octave (0–8)
- **VEL** (left sidebar): velocity slider 0–127

### DRUMS tab

- **4 × 4 drum pad grid** — 16 pads mapped to Ableton Drum Rack default layout:
  - Bottom-left = Kick (MIDI note 36), ascending right then up
  - Includes Kick, Snare, Hi-Hat (open/closed), Clap, Toms, Crash, Ride, Cowbell, Bongos
- **CH**: shows the active drum MIDI channel (default 10)
- **VEL**: drum velocity slider

### SETTINGS tab

- Adjust **Chord MIDI Channel** (1–16)
- Adjust **Drum MIDI Channel** (1–16)
- View connected **device name**

---

## Bidirectional MIDI

The app both **sends** and **receives** MIDI:

| Direction | Description |
|-----------|-------------|
| App → Ableton | Note On/Off for chord and drum pads |
| Ableton → App | Note On/Off feedback lights up the corresponding pad |

The Ableton Remote Script can send Note On messages back to the app to indicate
clip/track state — these are reflected as highlighted pad colours in real time.

---

## Drum Rack Workflow

The default drum pad layout matches Ableton's Push 2 Drum Rack grid:

```
[HH Open] [Crash   ] [Ride    ] [Cowbell ]   ← Row 3 (top)
[HH Clos ] [Snare 2] [Tom Hi  ] [Tom Mid ]   ← Row 2
[Clap    ] [Snare   ] [Tom Lo  ] [Kick 2  ]   ← Row 1
[Kick    ] [Rim     ] [Hi Bongo] [Lo Bongo]   ← Row 0 (bottom)
```

MIDI notes follow the GM drum map starting at note 36 (Kick = C1).

---

## Performance Design Notes

- The app keeps the screen on during use (`FLAG_KEEP_SCREEN_ON`).
- The UI runs in fullscreen immersive mode for maximum pad real estate.
- Touch events use `ACTION_DOWN`/`ACTION_UP` for tight Note On/Off timing.
- Multiple chord notes are sent atomically in a loop for low-latency polyphonic output.

---

## Project Structure

```
ChordPusherAndroid-v1.0/
├── app/
│   ├── src/main/
│   │   ├── java/com/gendemik/chordpusher/
│   │   │   ├── MainActivity.java        # Main UI + MIDI event handling
│   │   │   ├── UsbMidiManager.java      # USB MIDI host driver
│   │   │   └── ChordDefinitions.java    # Chord/drum pad note mappings
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml    # Root layout (tabs + status bar)
│   │   │   │   ├── panel_chords.xml     # Chord pad grid panel
│   │   │   │   ├── panel_drums.xml      # Drum pad grid panel
│   │   │   │   └── panel_settings.xml  # Settings panel
│   │   │   ├── values/                  # Colors, strings, styles
│   │   │   ├── drawable/                # Pad button backgrounds
│   │   │   └── xml/usb_device_filter.xml
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
└── settings.gradle
```

---

*ChordPusher v1.0 — Gendemik Digital*
