# Gendemik Digital — ChordPusher

**Company:** Gendemik Digital  
**Product:** ChordPusher  
**Current build:** v1.0  
**Platform:** Lenovo Tab M10 3rd Gen / Android + Ableton Live 10 Remote Script  
**Purpose:** A performance-ready, touchscreen, Push-style Ableton controller with an additional intelligent chord/progression workflow.

---

## 1. Product vision

ChordPusher is not intended to be a cosmetic clone of Ableton Push. It is a dedicated live-performance controller that reproduces the workflows that matter on Push while taking advantage of a touchscreen and adding a chord/progression system that Push 1 does not provide.

The architecture is deliberately split into two parts:

```text
Lenovo Android tablet
        ↕ USB MIDI
ChordPusher Android app
        ↕ custom MIDI control protocol
ChordPusher Live 10 Remote Script
        ↕ Live Object Model / _Framework
Ableton Live 10
```

Ableton Live is the **source of truth** for track selection, clip state, Drum Rack contents, clip notes and transport state. ChordPusher renders that state on the tablet and sends performance/edit commands back to Live.

The target experience is:

```text
Load Live Set
→ Open ChordPusher
→ USB MIDI connects automatically
→ Ableton handshake succeeds
→ ChordPusher reads the selected track
→ Surface changes automatically for that track/device
→ Pads light from real Live state
→ Perform, sequence and edit without touching the computer
```

---

## 2. Physical / UI specification

### Portrait-first performance layout

ChordPusher is designed primarily for portrait use on the Lenovo Tab M10 3rd Gen.

The **8×8 pad grid remains full-width and fixed at the bottom of the screen**. The pad surface must not move or resize when changing modes; only the meaning, labels and lighting of the pads change.

Above the pads is the command/control deck.

Target layout:

```text
┌────────────────────────────────────┐
│ CHORDPUSHER     LIVE ●     CH 03   │
│ Track: DRUMS    Rack: 909 KIT      │
├────────────────────────────────────┤
│ SESSION │ NOTE │ DRUM │ CHORD      │
│ SEQ     │ MIX  │DEVICE│ MORE       │
├────────────────────────────────────┤
│ TRK− │ TRK+ │ CH− │ CH+ │ OCT−/+  │
├────────────────────────────────────┤
│ PLAY │ STOP │ REC │ UNDO │ PANIC   │
├────────────────────────────────────┤
│ MODE-SPECIFIC COMMAND / INFO AREA  │
├────────────────────────────────────┤
│ ■ ■ ■ ■ ■ ■ ■ ■                  │
│ ■ ■ ■ ■ ■ ■ ■ ■                  │
│ ■ ■ ■ ■ ■ ■ ■ ■                  │
│ ■ ■ ■ ■ ■ ■ ■ ■                  │
│ ■ ■ ■ ■ ■ ■ ■ ■                  │
│ ■ ■ ■ ■ ■ ■ ■ ■                  │
│ ■ ■ ■ ■ ■ ■ ■ ■                  │
│ ■ ■ ■ ■ ■ ■ ■ ■                  │
└────────────────────────────────────┘
```

### Stage requirements

- Portrait orientation locked during normal performance.
- Screen remains awake.
- Full-width 8×8 pads.
- Large, precise command buttons suitable for live use.
- Multitouch pad operation.
- Immediate local visual feedback; Ableton return feedback is layered on top.
- Haptic feedback for pad/command interaction where appropriate.
- Performance Lock to reduce accidental navigation or destructive commands.
- Obvious connection state: `LIVE CONNECTED`, `MIDI ↔ OK`, `SYNC`, BPM.
- PANIC / All Notes Off available at all times.
- Automatic USB MIDI rediscovery/reconnection.
- Connection-loss state must be visually obvious rather than silently failing.

---

## 3. Track and MIDI-channel workflow

A live set is prepared before performance by assigning each Ableton track its preferred ChordPusher role and MIDI channel.

Example:

```text
01  DRUMS       CH01   AUTO / DRUM
02  BASS        CH02   SEQ
03  CHORDS      CH03   CHORD
04  LEAD        CH04   NOTE
05  FX          CH05   NOTE
06  LOOPS       CH06   SESSION
```

### Required behaviour

- `TRK− / TRK+` changes the selected Ableton track without interrupting playback.
- `CH− / CH+` changes/assigns the performance MIDI channel while playing.
- Channels 1–15 are available for musical/performance routing.
- MIDI channel 16 is reserved for the ChordPusher control protocol.
- Track assignments are remembered by the Android app.
- Each track may remember a preferred melodic mode: NOTE, CHORD or SEQ.
- Ableton device detection can override the stored mode when appropriate.

### Automatic mode selection

When the selected track changes, the Remote Script inspects the selected Live track and reports its device type to ChordPusher.

Expected examples:

```text
Selected track contains Drum Rack → DRUM mode
Selected melodic instrument        → NOTE / saved preferred mode
Track configured as CHORD          → CHORD mode
Track configured as SEQ            → SEQ mode
SESSION button                     → SESSION mode regardless of track
```

The user can disable automatic switching and select a mode manually when required.

---

## 4. Pad-light feedback model

Pad lighting is functional information, not decoration.

ChordPusher maintains separate states for:

- Local finger press.
- Latched/held note or chord.
- Ableton-returned MIDI activity.
- Selected Drum Rack pad.
- Loaded/empty Drum Rack pad.
- Existing sequencer step.
- Current sequencer playhead.
- Active Session clip.
- Playing Session clip.
- Recording Session clip.
- Harmonic recommendation strength in CHORD mode.
- Scale/root landmarks in NOTE mode.

Current conceptual colour meanings include:

```text
Gold     Local/current/held performance state
Cyan     MIDI / state returned from Ableton
Green    Strong harmonic next-chord recommendation
Blue     Good harmonic alternative
Purple   More adventurous harmonic option
Dim      Available but inactive
Dark     Empty / unavailable
```

Exact colours may evolve with Gendemik Digital branding, but the semantic distinction must remain clear on stage.

---

## 5. SESSION mode

SESSION is the Push-style clip-launching surface.

### Target behaviour

- 8×8 pads map to 8 tracks × 8 scenes.
- Pad states come from Live rather than being guessed locally.
- Empty clip slot = dark.
- Clip present = lit.
- Playing = active feedback.
- Recording = distinct feedback.
- Tap pad = launch corresponding clip/slot.
- Track/scene bank navigation moves the controlled 8×8 Session rectangle.
- Live Session View highlight follows the ChordPusher window.
- Playback continues while navigating.

---

## 6. NOTE mode

NOTE mode is a Push-style isomorphic melodic keyboard.

### Required behaviour

- One pad = one MIDI note.
- Notes are laid out consistently across the 8×8 surface.
- Root-note landmarks are clearly illuminated.
- Scale notes are differentiated from non-scale positions where applicable.
- Key and scale are selectable.
- `OCT− / OCT+` shifts the playable surface by ±12 semitones.
- Separate grid-position/navigation controls can move the isomorphic layout independently from literal octave changes.
- MIDI channel follows the selected track assignment.
- Ableton-returned note activity can illuminate the corresponding pad.

---

## 7. CHORD mode / intelligent progression map

CHORD mode is a ChordPusher-specific feature and a core product differentiator.

### Chord performance

- One pad can trigger a complete chord voicing.
- Key, scale, octave and voicing family are selectable.
- Voicing families include core triads, 7ths, 9ths, inversions, sus, open/spread and house-oriented voicings.
- HOLD/LATCH allows a chord to remain active without keeping a finger on the pad.

### Dynamic harmonic guidance

After a chord is played, the 8×8 grid recalculates and illuminates possible next harmonic moves.

Example:

```text
Am9
  ↓
Fmaj7 / Dm9 / Cmaj9 / Em7 / E7 ...
```

When another chord is selected, it becomes the new harmonic context and the recommendations update again.

Recommendation tiers:

- Strong/natural next move.
- Good alternative.
- Colour/tension/borrowed move.
- Current/home/return state.

Long-term target: recommendations become **progression-aware**, considering the chords already played rather than only the last chord.

### Progression capture target

ChordPusher should maintain a visible progression strip such as:

```text
Am9 → Fmaj7 → Cmaj9 → G6
```

That progression can then be transferred directly into SEQ mode for editing and playback.

---

## 8. DRUM mode

DRUM mode is intended to reproduce the practical Push 1 Drum Rack workflow while using Live itself as the pattern store.

### Automatic Drum Rack detection

If the selected Ableton track contains a Drum Rack, Live reports it to ChordPusher and DRUM mode becomes active automatically when AUTO TRACK is enabled.

### Drum-pad feedback

For each visible Drum Rack pad Live sends:

- Whether the pad contains a chain/sound.
- The Drum Rack pad name.
- Fallback chain/device name if required.
- Which drum pad is selected.
- Current bank position.

The touchscreen pads therefore show names such as:

```text
KICK 909
SNARE TIGHT
CLOSED HAT
OPEN HAT
CLAP
TOM LOW
```

instead of generic MIDI note numbers.

Expected states:

- Loaded sound = illuminated.
- Empty pad = dark.
- Selected drum = strongly highlighted.
- Sounding drum = temporary activity flash from returned MIDI where available.
- Drum Rack names change immediately when moving banks.

### Drum-bank navigation

- Drum Rack is navigated in 16-pad banks.
- `BANK− / BANK+` moves across the complete Drum Rack.
- The selected drum and rack scroll position are updated in Live.
- ChordPusher receives the new pad names/states after every bank move.

---

## 9. Live-backed drum pattern editing

The Drum sequencer edits the **actual Ableton MIDI clip**. The tablet does not maintain a disconnected copy as the primary source.

### Step editor

- 32 visible steps.
- 1/16-note resolution.
- 32 steps = two bars in 4/4.
- Tap an empty step = create a note in the Live MIDI clip.
- Tap an existing step = remove it.
- Existing Live notes are read back and illuminated.
- Selected drum determines which MIDI note the step editor displays/edits.
- Selected velocity is used when inserting new notes.
- Playhead feedback follows Live clock/transport.

### Clip handling

- Uses the currently relevant/highlighted MIDI clip slot on the selected track.
- If a suitable clip is not present, the implementation may create a useful default-length MIDI clip for step editing.
- Live remains the authoritative pattern store.

---

## 10. Drum bars / pattern-page navigation

Long clips must be editable without repeated blind paging.

For a two-bar page size:

```text
1–2 | 3–4 | 5–6 | 7–8 | 9–10 | 11–12 | 13–14 | 15–16
```

### Required behaviour

- One tap jumps directly to the selected two-bar region.
- `BARS ◀ / BARS ▶` moves to the previous/next two-bar page.
- For clips longer than the visible page strip, the page strip follows the current group.
- Page navigation wraps or clamps sensibly to the active clip loop.
- Switching pages must not stop Live playback.

### Target BAR OVERVIEW mode

A BAR OVERVIEW command should temporarily repurpose the 8×8 surface as a clip-section map so the performer can jump instantly to distant areas of a long pattern.

---

## 11. SEQ mode

SEQ mode is the melodic/chord sequencing workflow.

### Current/target behaviour

- 32-step pattern surface.
- 1/16-note step timing.
- Chords or notes can be assigned to steps.
- Tap to create/remove steps.
- Real-time/tap recording can capture played chords into the current sequencer position.
- Playhead is illuminated while running.
- Ableton MIDI clock can be used as master timing.
- Local clock remains available for standalone testing/fallback.

Target direction: increasingly move sequencer state into the actual Live clip, following the same source-of-truth model as DRUM mode.

---

## 12. Transport and timing

ChordPusher supports/targets:

- PLAY.
- STOP.
- RECORD.
- Ableton MIDI clock reception.
- Start / Continue / Stop realtime messages.
- 24 PPQN MIDI clock interpretation.
- 1/16-note playhead movement every six MIDI clocks.
- Output sync from Live when Live is master.

Future Push-style additions include Fixed Length, Repeat, Accent, Quantize and Undo integration.

---

## 13. MIX / DEVICE / BROWSE target modes

These are part of the complete product specification but are not considered finished in v1.0.

### MIX

Target controls:

- Track selection.
- Volume.
- Pan.
- Sends.
- Mute.
- Solo.
- Arm.
- Track feedback from Live.

### DEVICE

Target controls:

- Selected device name.
- Parameter banks.
- Eight primary parameters per page.
- Page/bank navigation.
- Parameter value feedback from Live.
- Device enable/bypass where practical.

### BROWSE

Target controls:

- Device/preset browsing where the Live 10 API allows a reliable workflow.
- Clear acknowledgement where an operation cannot be reproduced safely through Live 10's Remote Script API.

---

## 14. Performance safety / reliability specification

ChordPusher is intended to be playable in live shows, so reliability requirements are part of the product, not optional polish.

Required/target safeguards:

- Automatic USB MIDI endpoint discovery on app launch.
- Automatic reconnect after cable interruption where Android permits.
- Ableton connection/handshake indicator.
- MIDI IN and MIDI OUT activity indication.
- Immediate local pad response even if Live feedback is delayed.
- All Notes Off / PANIC.
- Stuck-note cleanup when changing track, mode or MIDI channel.
- Screen-awake operation.
- Portrait orientation lock.
- Performance Lock for accidental-touch prevention.
- Destructive operations require deliberate actions.
- Track/mode/channel changes must not stop global Live playback.
- Remote Script exceptions must fail gracefully rather than crashing the entire controller surface.

---

## 15. Current v1.0 implementation status

The following functionality is present in the v1.0 codebase:

- Gendemik Digital / ChordPusher product naming in the visible application and Live Remote Script.
- Android portrait orientation.
- Android keep-screen-on and immersive system-bar handling.
- 8×8 performance surface.
- Bidirectional Android MIDI connection code.
- Automatic MIDI endpoint rediscovery/reconnect loop.
- MIDI channels 1–15 for musical output with channel 16 reserved by the control protocol.
- Per-selected-track MIDI channel memory on Android.
- Per-track NOTE / CHORD / SEQ mode preference memory.
- Track previous/next control through Live Remote Script.
- Selected Live track name returned to the tablet.
- Selected-track primary device/rack name returned to the tablet.
- Automatic Drum Rack discovery through the selected track device chain.
- Drum Rack mode hint returned to Android.
- Drum Rack 16-pad bank navigation.
- Drum Rack pad loaded/empty feedback.
- Drum Rack pad names returned to Android.
- Drum Rack selected-pad state.
- Selection of Drum Rack pad in Live from ChordPusher.
- Drum velocity control message.
- Live-backed drum clip discovery/creation logic.
- Live-backed 32-step drum note reading/editing at 1/16 resolution.
- Two-bar drum page model and page-count feedback.
- SESSION framework component and Session highlight support.
- NOTE, CHORD, DRUM, SESSION and SEQ surface modes in the Android application.
- Chord engine with key/scale/octave/voicing mapping.
- Local hold/latch and panic behaviour.
- MIDI realtime clock/start/continue/stop handling.
- Local sequencer fallback state retained for non-Live use/testing.

Items described elsewhere in this README as **target**, **future**, **long-term**, or **required target behaviour** should not be interpreted as fully completed in v1.0.

---

## 16. Installation — Android

1. Unzip the release package.
2. Open this exact project folder in Android Studio:

   ```text
   ChordPusherAndroid-v1.0
   ```

   Do **not** open the outer `ChordPusher-v1.0` directory as the Android Studio project.

3. Select the Lenovo TB328FU.
4. Run the app from Android Studio.
5. The Android application ID intentionally remains compatible with the earlier development builds so the new build can install over them.
6. For normal Ableton use, set the tablet USB function to **MIDI**.

The app attempts to discover and connect to a suitable Android MIDI endpoint automatically.

---

## 17. Installation — Ableton Live 10 Remote Script

1. Close Ableton Live completely.
2. Remove the old custom `ChordPush` or previous `ChordPusher` Remote Script folder if present.
3. Copy:

   ```text
   Live10_Remote_Script/ChordPusher
   ```

   into:

   ```text
   User Library/Remote Scripts/ChordPusher
   ```

4. Restart Ableton Live 10.
5. Open:

   ```text
   Preferences → Link MIDI
   ```

6. Configure:

   ```text
   Control Surface: ChordPusher
   Input:           MIDI function
   Output:          MIDI function
   ```

7. Enable the required Track/Remote switches for the MIDI-function ports.
8. Enable Sync on Live's MIDI output when ChordPusher should follow Ableton clock.

---

## 18. Preparing a Live performance set

Before a show:

1. Load the Live Set.
2. Start ChordPusher and confirm `LIVE CONNECTED` / MIDI status.
3. Use `TRK− / TRK+` to visit each performance track.
4. Assign the desired channel with `CH− / CH+`.
5. For melodic tracks, choose the preferred NOTE, CHORD or SEQ behaviour.
6. Leave Drum Rack tracks on AUTO so Live can force DRUM mode.
7. Check that Drum Rack names and loaded states appear correctly.
8. Check direct bar-page navigation on the performance clips.
9. Test PANIC / All Notes Off before performance.
10. Enable Performance Lock when the set is ready.

During performance, changing ChordPusher focus must not interrupt the running Live scene or other tracks.

---

## 19. Naming and branding

The company name is **Gendemik Digital**.

The product name is **ChordPusher**.

Preferred product presentation:

```text
ChordPusher
by Gendemik Digital
```

The final launcher icon and in-app brand mark are intended to use the Gendemik Digital **G** mark when the final artwork is supplied.

---

## 20. Design principle

ChordPusher should feel like dedicated performance hardware rather than an Android utility.

The key rule is:

> **The 8×8 pads stay physically predictable; Ableton determines what they mean.**

Session clips, notes, chords, Drum Rack sounds and sequencer steps all occupy the same familiar playing surface, while the command deck and Live feedback explain the current context.

The goal is not to reproduce every physical detail of Push 1. The goal is to reproduce and extend its **performance workflow** in a form optimised for the Gendemik Digital ChordPusher touchscreen controller.
