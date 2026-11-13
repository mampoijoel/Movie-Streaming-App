package com.streamdemo.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.io.ByteArrayInputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Plays the UI sound effects.
 *
 * The effects are synthesised in code (short sine-wave blips), so the project
 * needs no audio files. Playback happens on a background thread and silently
 * does nothing on machines without an audio device.
 */
public final class SoundManager {

    public enum Effect { HOVER, CLICK, OPEN, CLOSE }

    private static final float SAMPLE_RATE = 44_100f;
    private static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
    private static final long HOVER_COOLDOWN_NS = 70_000_000L; // avoid machine-gun hover sounds

    private static final Map<Effect, byte[]> SAMPLES = new EnumMap<>(Effect.class);
    private static final ExecutorService PLAYER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "sound-player");
        t.setDaemon(true);
        return t;
    });

    private static volatile boolean muted = false;
    private static long lastHover = 0;

    static {
        // each note = { start frequency Hz, end frequency Hz, duration seconds }
        SAMPLES.put(Effect.HOVER, synth(0.10, new double[]{1500, 1100, 0.035}));
        SAMPLES.put(Effect.CLICK, synth(0.25, new double[]{700, 900, 0.05}, new double[]{1000, 1300, 0.07}));
        SAMPLES.put(Effect.OPEN, synth(0.25,
                new double[]{300, 600, 0.12}, new double[]{600, 900, 0.14}, new double[]{900, 1400, 0.20}));
        SAMPLES.put(Effect.CLOSE, synth(0.22,
                new double[]{1100, 700, 0.10}, new double[]{700, 350, 0.16}));
    }

    private SoundManager() {
    }

    public static void play(Effect effect) {
        if (muted) {
            return;
        }
        if (effect == Effect.HOVER) {
            long now = System.nanoTime();
            if (now - lastHover < HOVER_COOLDOWN_NS) {
                return;
            }
            lastHover = now;
        }
        final byte[] data = SAMPLES.get(effect);
        PLAYER.submit(() -> {
            try {
                AudioInputStream stream = new AudioInputStream(
                        new ByteArrayInputStream(data), FORMAT, data.length / FORMAT.getFrameSize());
                Clip clip = AudioSystem.getClip();
                clip.open(stream);
                clip.addLineListener(e -> {
                    if (e.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
                clip.start();
            } catch (Exception | LinkageError ignored) {
                // no audio device / unsupported line - the app simply stays silent
            }
        });
    }

    public static boolean isMuted() {
        return muted;
    }

    public static void setMuted(boolean value) {
        muted = value;
    }

    /** Builds 16-bit mono PCM for a sequence of frequency-sweeping notes. */
    private static byte[] synth(double volume, double[]... notes) {
        int total = 0;
        for (double[] n : notes) {
            total += (int) (SAMPLE_RATE * n[2]);
        }
        byte[] out = new byte[total * 2];
        int idx = 0;
        double phase = 0;
        for (double[] n : notes) {
            int len = (int) (SAMPLE_RATE * n[2]);
            for (int i = 0; i < len; i++) {
                double t = (double) i / len;
                double freq = n[0] + (n[1] - n[0]) * t;
                phase += 2 * Math.PI * freq / SAMPLE_RATE;
                double attack = Math.min(1.0, i / (SAMPLE_RATE * 0.004));
                double envelope = attack * Math.pow(1 - t, 1.5);
                short s = (short) (Math.sin(phase) * envelope * volume * Short.MAX_VALUE);
                out[idx++] = (byte) (s & 0xFF);
                out[idx++] = (byte) ((s >> 8) & 0xFF);
            }
        }
        return out;
    }
}
