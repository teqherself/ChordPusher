package com.gendemik.chordpusher;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gendemik.chordpusher.databinding.ActivityMainBinding;

/**
 * MainActivity - ChordPusher Android v1.0
 *
 * Main controller UI for the ChordPusher Ableton Live 10 Remote Script.
 * Designed for Lenovo Tab M10 3rd Gen in landscape mode.
 *
 * Panels:
 *   CHORDS - 8x4 chord pad grid with octave and velocity controls
 *   DRUMS  - 4x4 drum rack pad grid matching Ableton Drum Rack layout
 *   SETTINGS - MIDI channel configuration and device info
 *
 * MIDI:
 *   Sends Note On/Off over USB MIDI (host mode)
 *   Receives Note On feedback from Ableton for LED simulation
 */
public class MainActivity extends AppCompatActivity
        implements UsbMidiManager.MidiEventListener {

    private ActivityMainBinding binding;
    private UsbMidiManager midiManager;

    // Controller state
    private int chordOctave = 4;          // MIDI octave for chord root (C4 = 60)
    private int chordVelocity = 100;
    private int drumVelocity = 100;
    private int chordMidiChannel = 0;     // 0-based (channel 1)
    private int drumMidiChannel = 9;      // 0-based (channel 10, GM drums)

    // Active notes tracking to send note-off on release
    private final int[][] activeChordNotes = new int[32][];
    private final boolean[] activeDrumPad = new boolean[16];

    // Pad button arrays
    private final Button[] chordPads = new Button[32];
    private final Button[] drumPads = new Button[16];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen on during performance
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Fullscreen
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupTabs();
        setupChordsPanel();
        setupDrumsPanel();
        setupSettingsPanel();

        midiManager = new UsbMidiManager(this, this);
        midiManager.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (midiManager != null) midiManager.stop();
    }

    // ── Tab navigation ────────────────────────────────────────────────────────

    private void setupTabs() {
        binding.btnTabChords.setOnClickListener(v -> showPanel(0));
        binding.btnTabDrums.setOnClickListener(v -> showPanel(1));
        binding.btnTabSettings.setOnClickListener(v -> showPanel(2));
    }

    private void showPanel(int index) {
        binding.chordsPanel.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        binding.drumsPanel.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        binding.settingsPanel.setVisibility(index == 2 ? View.VISIBLE : View.GONE);

        int activeColor = ContextCompat.getColor(this, R.color.accent_red);
        int inactiveColor = ContextCompat.getColor(this, R.color.text_secondary);
        binding.btnTabChords.setTextColor(index == 0 ? activeColor : inactiveColor);
        binding.btnTabDrums.setTextColor(index == 1 ? activeColor : inactiveColor);
        binding.btnTabSettings.setTextColor(index == 2 ? activeColor : inactiveColor);
    }

    // ── Chord pads setup ──────────────────────────────────────────────────────

    private void setupChordsPanel() {
        GridLayout grid = binding.chordsPanel.findViewById(R.id.chordGrid);

        // Build 32 pads: 4 rows x 8 cols, displayed top-to-bottom = row3..row0
        for (int row = 3; row >= 0; row--) {
            for (int col = 0; col < 8; col++) {
                final int padIndex = row * 8 + col;
                Button pad = new Button(this);
                pad.setText(ChordDefinitions.CHORD_NAMES[padIndex]);
                pad.setBackground(ContextCompat.getDrawable(this, R.drawable.pad_chord));
                pad.setTextColor(Color.WHITE);
                pad.setTextSize(10f);
                pad.setAllCaps(false);

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                        GridLayout.spec(GridLayout.UNDEFINED, 1f),
                        GridLayout.spec(GridLayout.UNDEFINED, 1f));
                lp.width = 0;
                lp.height = 0;
                lp.setMargins(4, 4, 4, 4);
                pad.setLayoutParams(lp);

                final int idx = padIndex;
                pad.setOnTouchListener((v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        onChordPadDown(idx, pad);
                    } else if (event.getAction() == MotionEvent.ACTION_UP
                            || event.getAction() == MotionEvent.ACTION_CANCEL) {
                        onChordPadUp(idx, pad);
                    }
                    return true;
                });

                grid.addView(pad);
                chordPads[padIndex] = pad;
            }
        }

        // Octave controls
        TextView tvOctave = binding.chordsPanel.findViewById(R.id.tvOctave);
        Button btnOctaveUp = binding.chordsPanel.findViewById(R.id.btnOctaveUp);
        Button btnOctaveDown = binding.chordsPanel.findViewById(R.id.btnOctaveDown);

        btnOctaveUp.setOnClickListener(v -> {
            if (chordOctave < 8) {
                chordOctave++;
                tvOctave.setText(String.valueOf(chordOctave));
            }
        });
        btnOctaveDown.setOnClickListener(v -> {
            if (chordOctave > 0) {
                chordOctave--;
                tvOctave.setText(String.valueOf(chordOctave));
            }
        });

        // Velocity seek bar
        SeekBar seekVel = binding.chordsPanel.findViewById(R.id.seekVelocity);
        TextView tvVel = binding.chordsPanel.findViewById(R.id.tvVelocity);
        seekVel.setProgress(chordVelocity);
        seekVel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean u) {
                chordVelocity = p;
                tvVel.setText(String.valueOf(p));
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
    }

    private void onChordPadDown(int padIndex, Button pad) {
        int octaveBase = (chordOctave + 1) * 12; // C4=60 when octave=4: (4+1)*12=60
        int[] notes = ChordDefinitions.getChordNotes(padIndex, octaveBase);
        activeChordNotes[padIndex] = notes;
        for (int note : notes) {
            midiManager.sendNoteOn(chordMidiChannel, note, chordVelocity);
        }
        pad.setBackgroundColor(ContextCompat.getColor(this, R.color.pad_active));
    }

    private void onChordPadUp(int padIndex, Button pad) {
        if (activeChordNotes[padIndex] != null) {
            for (int note : activeChordNotes[padIndex]) {
                midiManager.sendNoteOff(chordMidiChannel, note);
            }
            activeChordNotes[padIndex] = null;
        }
        pad.setBackground(ContextCompat.getDrawable(this, R.drawable.pad_chord));
    }

    // ── Drum pads setup ───────────────────────────────────────────────────────

    private void setupDrumsPanel() {
        GridLayout grid = binding.drumsPanel.findViewById(R.id.drumGrid);

        // 4x4 grid: displayed top row = high notes, bottom row = low notes
        for (int row = 3; row >= 0; row--) {
            for (int col = 0; col < 4; col++) {
                final int padIndex = row * 4 + col;
                Button pad = new Button(this);
                pad.setText(ChordDefinitions.DRUM_PAD_NAMES[padIndex]);
                pad.setBackground(ContextCompat.getDrawable(this, R.drawable.pad_drum));
                pad.setTextColor(Color.WHITE);
                pad.setTextSize(9f);
                pad.setAllCaps(false);

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                        GridLayout.spec(GridLayout.UNDEFINED, 1f),
                        GridLayout.spec(GridLayout.UNDEFINED, 1f));
                lp.width = 0;
                lp.height = 0;
                lp.setMargins(4, 4, 4, 4);
                pad.setLayoutParams(lp);

                final int idx = padIndex;
                pad.setOnTouchListener((v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        onDrumPadDown(idx, pad);
                    } else if (event.getAction() == MotionEvent.ACTION_UP
                            || event.getAction() == MotionEvent.ACTION_CANCEL) {
                        onDrumPadUp(idx, pad);
                    }
                    return true;
                });

                grid.addView(pad);
                drumPads[padIndex] = pad;
            }
        }

        // Drum velocity seek bar
        SeekBar seekVel = binding.drumsPanel.findViewById(R.id.seekDrumVelocity);
        TextView tvVel = binding.drumsPanel.findViewById(R.id.tvDrumVelocity);
        seekVel.setProgress(drumVelocity);
        seekVel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean u) {
                drumVelocity = p;
                tvVel.setText(String.valueOf(p));
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
    }

    private void onDrumPadDown(int padIndex, Button pad) {
        int note = ChordDefinitions.DRUM_NOTES[padIndex];
        activeDrumPad[padIndex] = true;
        midiManager.sendNoteOn(drumMidiChannel, note, drumVelocity);
        pad.setBackgroundColor(ContextCompat.getColor(this, R.color.pad_drum_active));
    }

    private void onDrumPadUp(int padIndex, Button pad) {
        if (activeDrumPad[padIndex]) {
            int note = ChordDefinitions.DRUM_NOTES[padIndex];
            midiManager.sendNoteOff(drumMidiChannel, note);
            activeDrumPad[padIndex] = false;
        }
        pad.setBackground(ContextCompat.getDrawable(this, R.drawable.pad_drum));
    }

    // ── Settings panel setup ──────────────────────────────────────────────────

    private void setupSettingsPanel() {
        TextView tvChordCh = binding.settingsPanel.findViewById(R.id.tvChordChannel);
        Button btnChordPlus = binding.settingsPanel.findViewById(R.id.btnChordChannelPlus);
        Button btnChordMinus = binding.settingsPanel.findViewById(R.id.btnChordChannelMinus);

        btnChordPlus.setOnClickListener(v -> {
            if (chordMidiChannel < 15) {
                chordMidiChannel++;
                tvChordCh.setText(String.valueOf(chordMidiChannel + 1));
            }
        });
        btnChordMinus.setOnClickListener(v -> {
            if (chordMidiChannel > 0) {
                chordMidiChannel--;
                tvChordCh.setText(String.valueOf(chordMidiChannel + 1));
            }
        });

        TextView tvDrumCh = binding.settingsPanel.findViewById(R.id.tvDrumChannelSettings);
        Button btnDrumPlus = binding.settingsPanel.findViewById(R.id.btnDrumChannelPlus);
        Button btnDrumMinus = binding.settingsPanel.findViewById(R.id.btnDrumChannelMinus);

        btnDrumPlus.setOnClickListener(v -> {
            if (drumMidiChannel < 15) {
                drumMidiChannel++;
                tvDrumCh.setText(String.valueOf(drumMidiChannel + 1));
                // Update drum channel display in drums panel
                TextView tvDrum = binding.drumsPanel.findViewById(R.id.tvDrumChannel);
                tvDrum.setText(String.valueOf(drumMidiChannel + 1));
            }
        });
        btnDrumMinus.setOnClickListener(v -> {
            if (drumMidiChannel > 0) {
                drumMidiChannel--;
                tvDrumCh.setText(String.valueOf(drumMidiChannel + 1));
                TextView tvDrum = binding.drumsPanel.findViewById(R.id.tvDrumChannel);
                tvDrum.setText(String.valueOf(drumMidiChannel + 1));
            }
        });
    }

    // ── MIDI event listener callbacks ─────────────────────────────────────────

    @Override
    public void onConnected(String deviceName) {
        binding.tvMidiStatus.setText(R.string.status_connected);
        binding.tvMidiStatus.setTextColor(
                ContextCompat.getColor(this, R.color.status_connected));
        binding.tvDeviceName.setText(deviceName);

        TextView tvDeviceInfo = binding.settingsPanel.findViewById(R.id.tvDeviceInfo);
        tvDeviceInfo.setText(deviceName);
    }

    @Override
    public void onDisconnected() {
        binding.tvMidiStatus.setText(R.string.status_disconnected);
        binding.tvMidiStatus.setTextColor(
                ContextCompat.getColor(this, R.color.status_disconnected));
        binding.tvDeviceName.setText("");

        TextView tvDeviceInfo = binding.settingsPanel.findViewById(R.id.tvDeviceInfo);
        tvDeviceInfo.setText("No device connected");
    }

    /**
     * Handle incoming MIDI from Ableton Live (feedback / LED updates).
     * USB MIDI packets are 4 bytes: [CIN, status, data1, data2]
     */
    @Override
    public void onMidiReceived(byte[] data, int length) {
        // Process 4-byte USB MIDI packets
        for (int i = 0; i + 3 < length; i += 4) {
            int status = data[i + 1] & 0xFF;
            int msgType = status & 0xF0;
            int channel = status & 0x0F;
            int note = data[i + 2] & 0x7F;
            int velocity = data[i + 3] & 0x7F;

            if (msgType == 0x90 && velocity > 0) {
                // Note On: Ableton is signaling a pad LED "on"
                highlightPadForNote(channel, note, true);
            } else if (msgType == 0x80 || (msgType == 0x90 && velocity == 0)) {
                // Note Off: Ableton LED "off"
                highlightPadForNote(channel, note, false);
            }
        }
    }

    /** Flash a chord or drum pad background when Ableton sends feedback */
    private void highlightPadForNote(int channel, int note, boolean active) {
        // Check drum pads
        if (channel == drumMidiChannel) {
            for (int i = 0; i < ChordDefinitions.DRUM_NOTES.length; i++) {
                if (ChordDefinitions.DRUM_NOTES[i] == note && drumPads[i] != null) {
                    final int idx = i;
                    final boolean on = active;
                    drumPads[i].post(() -> {
                        if (on) {
                            drumPads[idx].setBackgroundColor(
                                    ContextCompat.getColor(this, R.color.pad_drum_active));
                        } else {
                            drumPads[idx].setBackground(
                                    ContextCompat.getDrawable(this, R.drawable.pad_drum));
                        }
                    });
                    return;
                }
            }
        }

        // Check chord pads
        if (channel == chordMidiChannel) {
            int octaveBase = (chordOctave + 1) * 12;
            for (int i = 0; i < 32; i++) {
                int[] notes = ChordDefinitions.getChordNotes(i, octaveBase);
                for (int n : notes) {
                    if (n == note && chordPads[i] != null) {
                        final int idx = i;
                        final boolean on = active;
                        chordPads[i].post(() -> {
                            if (on) {
                                chordPads[idx].setBackgroundColor(
                                        ContextCompat.getColor(this, R.color.pad_active));
                            } else {
                                chordPads[idx].setBackground(
                                        ContextCompat.getDrawable(this, R.drawable.pad_chord));
                            }
                        });
                        return;
                    }
                }
            }
        }
    }
}
