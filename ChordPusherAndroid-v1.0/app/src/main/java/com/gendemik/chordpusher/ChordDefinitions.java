package com.gendemik.chordpusher;

/**
 * ChordDefinitions - defines the 32 chord pad mappings for the ChordPusher grid.
 *
 * Grid layout (8 columns x 4 rows, bottom-left origin):
 *   Row 3 (top):    Maj7, min7, dom7, dim7, aug, sus2, sus4, add9   (extensions)
 *   Row 2:          Fmaj, Gmin, Amaj, Bmin, Cmaj, Dmin, Emaj, Fmin  (diatonic 2)
 *   Row 1:          Cmaj, Dmin, Emin, Fmaj, Gmaj, Amin, Bdim, Cmaj  (C major scale)
 *   Row 0 (bottom): C,   D,    E,    F,    G,    A,    B,    C      (single notes)
 *
 * Each chord is represented as an array of semitone intervals from root.
 * The root note is calculated from base note (C4=60) + octave offset.
 */
public class ChordDefinitions {

    public static final String[] CHORD_NAMES = {
        // Row 0 — single notes (chromatic)
        "C", "C#", "D", "D#", "E", "F", "F#", "G",
        // Row 1 — C major diatonic triads (Cmaj, Dmin, Emin, Fmaj, Gmaj, Amin, Bdim, Cmaj)
        "Cmaj", "Dmin", "Emin", "Fmaj", "Gmaj", "Amin", "Bdim", "Cmaj",
        // Row 2 — 7th chords in C major
        "Cmaj7", "Dm7", "Em7", "Fmaj7", "G7", "Am7", "Bm7b5", "Cmaj7",
        // Row 3 — chord types on C
        "Cmaj", "Cmin", "Cdim", "Caug", "Csus2", "Csus4", "C7", "Cm7"
    };

    /** Semitone intervals above root for each chord type */
    public static final int[][] CHORD_INTERVALS = {
        // Row 0 — single notes
        {0}, {1}, {2}, {3}, {4}, {5}, {6}, {7},
        // Row 1 — C major diatonic triads: Cmaj, Dmin, Emin, Fmaj, Gmaj, Amin, Bdim, Cmaj
        {0, 4, 7}, {0, 3, 7}, {0, 3, 7}, {0, 4, 7},
        {0, 4, 7}, {0, 3, 7}, {0, 3, 6}, {0, 4, 7},
        // Row 2 — 7ths
        {0, 4, 7, 11}, {0, 3, 7, 10}, {0, 3, 7, 10}, {0, 4, 7, 11},
        {0, 4, 7, 10}, {0, 3, 7, 10}, {0, 3, 6, 10}, {0, 4, 7, 11},
        // Row 3 — chord types
        {0, 4, 7},    // major
        {0, 3, 7},    // minor
        {0, 3, 6},    // diminished
        {0, 4, 8},    // augmented
        {0, 2, 7},    // sus2
        {0, 5, 7},    // sus4
        {0, 4, 7, 10},// dom7
        {0, 3, 7, 10} // min7
    };

    /**
     * Root note (MIDI) for each pad, relative to octave base.
     * Row 0: chromatic from C
     * Row 1: C major diatonic roots
     * Row 2: C major diatonic roots (for 7ths)
     * Row 3: all C (chord type showcase)
     */
    public static final int[] ROOT_SEMITONES = {
        // Row 0
        0, 1, 2, 3, 4, 5, 6, 7,
        // Row 1 — C D E F G A B C
        0, 2, 4, 5, 7, 9, 11, 12,
        // Row 2
        0, 2, 4, 5, 7, 9, 11, 12,
        // Row 3
        0, 0, 0, 0, 0, 0, 0, 0
    };

    /**
     * Returns the MIDI note numbers for a chord pad.
     * @param padIndex  0-31 (col + row*8, bottom-left origin)
     * @param octaveBase MIDI note for C in the current octave (e.g. 48 for C3, 60 for C4)
     */
    public static int[] getChordNotes(int padIndex, int octaveBase) {
        int[] intervals = CHORD_INTERVALS[padIndex];
        int root = octaveBase + ROOT_SEMITONES[padIndex];
        int[] notes = new int[intervals.length];
        for (int i = 0; i < intervals.length; i++) {
            notes[i] = Math.min(127, Math.max(0, root + intervals[i]));
        }
        return notes;
    }

    /** Drum Rack pad names (standard GM mapping starting at note 36) */
    public static final String[] DRUM_PAD_NAMES = {
        // Row 3 (top)
        "HH Open", "Crash", "Ride", "Cowbell",
        // Row 2
        "HH Closed", "Snare 2", "Tom Hi", "Tom Mid",
        // Row 1
        "Clap", "Snare", "Tom Lo", "Kick 2",
        // Row 0 (bottom)
        "Kick", "Rim", "Hi Bongo", "Lo Bongo"
    };

    /** MIDI note for each drum pad (Ableton Drum Rack default layout) */
    public static final int[] DRUM_NOTES = {
        // Row 3 (top) — displayed top to bottom, but MIDI bottom-up
        46, 49, 51, 56,  // HH Open, Crash, Ride, Cowbell
        42, 40, 48, 47,  // HH Closed, Snare 2, Tom Hi, Tom Mid
        39, 38, 45, 35,  // Clap, Snare, Tom Lo, Kick 2
        36, 37, 60, 61   // Kick, Rim, Hi Bongo, Lo Bongo
    };
}
