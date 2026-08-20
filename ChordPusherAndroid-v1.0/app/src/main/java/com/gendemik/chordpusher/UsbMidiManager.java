package com.gendemik.chordpusher;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * UsbMidiManager - handles USB MIDI host mode communication with Ableton Live 10
 * via the ChordPusher Remote Script.
 *
 * Protocol: Standard USB MIDI 1.0 (USB Audio Class, subclass MIDI Streaming)
 * Packets: 4-byte USB MIDI Event Packets (CIN + status + data1 + data2)
 */
public class UsbMidiManager {

    private static final String TAG = "UsbMidiManager";
    private static final String ACTION_USB_PERMISSION = "com.gendemik.chordpusher.USB_PERMISSION";

    // USB MIDI Audio class / MIDI streaming subclass
    private static final int USB_CLASS_AUDIO = 1;
    private static final int USB_SUBCLASS_MIDISTREAMING = 3;

    private final Context context;
    private final UsbManager usbManager;
    private final MidiEventListener listener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private UsbDevice connectedDevice;
    private UsbDeviceConnection usbConnection;
    private UsbEndpoint endpointOut;
    private UsbEndpoint endpointIn;

    private ExecutorService readExecutor;
    private volatile boolean running = false;

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            String action = intent.getAction();
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (ACTION_USB_PERMISSION.equals(action)) {
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    openDevice(device);
                } else {
                    Log.w(TAG, "USB permission denied for device: " + device);
                    mainHandler.post(() -> listener.onDisconnected());
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                if (device != null && device.equals(connectedDevice)) {
                    closeDevice();
                    mainHandler.post(() -> listener.onDisconnected());
                }
            }
        }
    };

    public interface MidiEventListener {
        void onConnected(String deviceName);
        void onDisconnected();
        /** Called on main thread with raw MIDI bytes from Ableton */
        void onMidiReceived(byte[] data, int length);
    }

    public UsbMidiManager(Context context, MidiEventListener listener) {
        this.context = context.getApplicationContext();
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        this.listener = listener;
    }

    /** Register receivers and scan for already-attached MIDI devices */
    public void start() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        context.registerReceiver(usbReceiver, filter);
        scanForMidiDevices();
    }

    public void stop() {
        try {
            context.unregisterReceiver(usbReceiver);
        } catch (IllegalArgumentException ignored) {}
        closeDevice();
    }

    private void scanForMidiDevices() {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        for (UsbDevice device : deviceList.values()) {
            if (isMidiDevice(device)) {
                requestPermissionAndOpen(device);
                return;
            }
        }
    }

    private boolean isMidiDevice(UsbDevice device) {
        for (int i = 0; i < device.getInterfaceCount(); i++) {
            UsbInterface iface = device.getInterface(i);
            if (iface.getInterfaceClass() == USB_CLASS_AUDIO
                    && iface.getInterfaceSubclass() == USB_SUBCLASS_MIDISTREAMING) {
                return true;
            }
        }
        return false;
    }

    private void requestPermissionAndOpen(UsbDevice device) {
        if (usbManager.hasPermission(device)) {
            openDevice(device);
        } else {
            PendingIntent pi = PendingIntent.getBroadcast(
                    context, 0,
                    new Intent(ACTION_USB_PERMISSION),
                    PendingIntent.FLAG_IMMUTABLE);
            usbManager.requestPermission(device, pi);
        }
    }

    private void openDevice(UsbDevice device) {
        if (device == null) return;

        UsbInterface midiInterface = findMidiInterface(device);
        if (midiInterface == null) {
            Log.e(TAG, "No MIDI streaming interface found on device");
            return;
        }

        UsbDeviceConnection conn = usbManager.openDevice(device);
        if (conn == null) {
            Log.e(TAG, "Could not open USB device");
            return;
        }

        if (!conn.claimInterface(midiInterface, true)) {
            Log.e(TAG, "Could not claim MIDI interface");
            conn.close();
            return;
        }

        // Find bulk IN and OUT endpoints
        UsbEndpoint epOut = null;
        UsbEndpoint epIn = null;
        for (int i = 0; i < midiInterface.getEndpointCount(); i++) {
            UsbEndpoint ep = midiInterface.getEndpoint(i);
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                    epOut = ep;
                } else {
                    epIn = ep;
                }
            }
        }

        if (epOut == null) {
            Log.e(TAG, "No bulk OUT endpoint found");
            conn.close();
            return;
        }

        connectedDevice = device;
        usbConnection = conn;
        endpointOut = epOut;
        endpointIn = epIn;

        startReading();

        String name = device.getProductName() != null
                ? device.getProductName()
                : "USB MIDI Device";
        Log.i(TAG, "Connected to: " + name);
        mainHandler.post(() -> listener.onConnected(name));
    }

    private UsbInterface findMidiInterface(UsbDevice device) {
        for (int i = 0; i < device.getInterfaceCount(); i++) {
            UsbInterface iface = device.getInterface(i);
            if (iface.getInterfaceClass() == USB_CLASS_AUDIO
                    && iface.getInterfaceSubclass() == USB_SUBCLASS_MIDISTREAMING) {
                return iface;
            }
        }
        return null;
    }

    private void startReading() {
        if (endpointIn == null) return;
        running = true;
        readExecutor = Executors.newSingleThreadExecutor();
        readExecutor.submit(() -> {
            byte[] buffer = new byte[64];
            while (running && usbConnection != null) {
                int transferred = usbConnection.bulkTransfer(endpointIn, buffer, buffer.length, 100);
                if (transferred > 0) {
                    final byte[] data = new byte[transferred];
                    System.arraycopy(buffer, 0, data, 0, transferred);
                    mainHandler.post(() -> listener.onMidiReceived(data, data.length));
                }
            }
        });
    }

    private void closeDevice() {
        running = false;
        if (readExecutor != null) {
            readExecutor.shutdownNow();
            readExecutor = null;
        }
        if (usbConnection != null) {
            usbConnection.close();
            usbConnection = null;
        }
        connectedDevice = null;
        endpointOut = null;
        endpointIn = null;
    }

    /**
     * Send a MIDI Note On message.
     * @param channel 0-15
     * @param note    0-127
     * @param velocity 0-127 (0 = note off)
     */
    public void sendNoteOn(int channel, int note, int velocity) {
        byte status = (byte) (0x90 | (channel & 0x0F));
        // USB MIDI Event Packet: CIN=0x09 (note on), status, note, velocity
        byte cin = (velocity > 0) ? (byte) 0x09 : (byte) 0x08;
        if (velocity == 0) status = (byte) (0x80 | (channel & 0x0F));
        sendUsbMidiPacket(cin, status, (byte) (note & 0x7F), (byte) (velocity & 0x7F));
    }

    /**
     * Send a MIDI Note Off message.
     */
    public void sendNoteOff(int channel, int note) {
        byte status = (byte) (0x80 | (channel & 0x0F));
        sendUsbMidiPacket((byte) 0x08, status, (byte) (note & 0x7F), (byte) 0x00);
    }

    /**
     * Send a MIDI Control Change message.
     */
    public void sendControlChange(int channel, int cc, int value) {
        byte status = (byte) (0xB0 | (channel & 0x0F));
        sendUsbMidiPacket((byte) 0x0B, status, (byte) (cc & 0x7F), (byte) (value & 0x7F));
    }

    private void sendUsbMidiPacket(byte cin, byte status, byte data1, byte data2) {
        if (usbConnection == null || endpointOut == null) return;
        byte[] packet = {cin, status, data1, data2};
        int sent = usbConnection.bulkTransfer(endpointOut, packet, packet.length, 50);
        if (sent < 0) {
            Log.w(TAG, "bulkTransfer failed: " + sent);
        }
    }

    public boolean isConnected() {
        return usbConnection != null;
    }
}
