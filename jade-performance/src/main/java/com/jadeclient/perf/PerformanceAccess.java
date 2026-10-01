package com.jadeclient.perf;

import org.lwjgl.opengl.Display;

/**
 * Static bridge between Forge event hooks and the perf settings. Avoids
 * repeated lookups inside hot paths.
 */
public final class PerformanceAccess {

	private PerformanceAccess() {
	}

	/** Nanosecond timestamp of the previous frame start. */
	private static volatile long lastFrameStart = System.nanoTime();
	private static volatile float lastDeltaMs;

	public static long lastFrameStart() {
		return lastFrameStart;
	}

	public static float lastFrameDeltaMs() {
		long now = System.nanoTime();
		lastDeltaMs = (now - lastFrameStart) / 1000000f;
		lastFrameStart = now;
		return lastDeltaMs;
	}

	/** Master FPS Boost on? Fast bail-out used by every perf hook. */
	public static boolean perfEnabled() {
		return JadePerfBridge.fpsBoostEnabled();
	}

	public static PerformanceModule perf() {
		return JadePerfBridge.module();
	}

	public static boolean windowFocused() {
		try {
			return Display.isActive();
		} catch (Exception e) {
			return true;
		}
	}

	/** True when the unfocused cap should throttle rendering. */
	public static boolean shouldThrottleUnfocused() {
		PerformanceModule m = perf();
		return m != null && m.isEnabled() && !windowFocused();
	}
}
