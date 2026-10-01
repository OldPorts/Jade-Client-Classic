package com.jadeclient.core.perf;

/**
 * Rolling FPS/frametime statistics. Updated by the perf mixin once per
 * frame; read by the HUD and the benchmark overlay.
 */
public final class FpsTracker {

	private static final FpsTracker INSTANCE = new FpsTracker();

	public static FpsTracker get() {
		return INSTANCE;
	}

	private FpsTracker() {
	}

	private static final int WINDOW = 240; // ~4 seconds at 60fps

	private final float[] frameTimes = new float[WINDOW];
	private int index;
	private int filled;

	private float lastFps;
	private float lastFrameMs;
	private float low1Pct;
	private float avgFps;
	private long frameCount;

	public void onFrame(float frameTimeMs) {
		frameTimes[index] = frameTimeMs;
		index = (index + 1) % WINDOW;
		filled = Math.min(filled + 1, WINDOW);
		frameCount++;

		lastFrameMs = frameTimeMs;
		lastFps = frameTimeMs > 0 ? 1000f / frameTimeMs : 0;

		// Recompute stats every 30 frames; cheap and plenty responsive.
		if (frameCount % 30 == 0) {
			recompute();
			com.jadeclient.core.event.EventBridge.postFrame(avgFps, lastFrameMs);
		}
	}

	private void recompute() {
		int n = filled;
		if (n == 0) {
			return;
		}
		float[] sorted = new float[n];
		float sum = 0;
		for (int i = 0; i < n; i++) {
			sorted[i] = frameTimes[i];
			sum += frameTimes[i];
		}
		java.util.Arrays.sort(sorted);

		avgFps = sum / n > 0 ? 1000f * n / sum : 0;

		// 1% low: average of the slowest 1% of frames (with a floor of 1 frame).
		int worst = Math.max(1, n / 100);
		float worstSum = 0;
		for (int i = 0; i < worst; i++) {
			worstSum += sorted[i]; // sorted ascending = slowest first
		}
		low1Pct = worstSum / worst > 0 ? 1000f * worst / worstSum : 0;
	}

	public float fps() {
		return lastFps;
	}

	public float avgFps() {
		return avgFps;
	}

	public float low1Pct() {
		return low1Pct;
	}

	public float frameTimeMs() {
		return lastFrameMs;
	}

	/** Snapshot of the frametime ring buffer, oldest first, for the HUD graph. */
	public float[] snapshot() {
		float[] out = new float[filled];
		for (int i = 0; i < filled; i++) {
			out[i] = frameTimes[(index + i) % WINDOW];
		}
		return out;
	}

	public void reset() {
		index = 0;
		filled = 0;
		frameCount = 0;
	}
}
