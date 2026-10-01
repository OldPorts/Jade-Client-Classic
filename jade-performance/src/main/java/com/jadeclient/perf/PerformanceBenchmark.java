package com.jadeclient.perf;

import com.jadeclient.core.perf.FpsTracker;

/**
 * Live A/B benchmark: samples "boost off", toggles Master FPS Boost on,
 * samples again, and exposes the delta for the HUD overlay.
 */
public final class PerformanceBenchmark {

	private static final long SAMPLE_MS = 5000;

	private float beforeAvg;
	private float beforeLow;
	private float afterAvg;
	private float afterLow;
	private long sampleStart = -1;
	private boolean samplingBefore = true;

	public void start() {
		PerformanceModule perf = PerformanceAccess.perf();
		if (perf != null) {
			perf.setEnabled(false); // phase 1 = boost off
		}
		beforeAvg = FpsTracker.get().avgFps();
		beforeLow = FpsTracker.get().low1Pct();
		samplingBefore = true;
		sampleStart = System.currentTimeMillis();
	}

	/** Call once per frame; returns true while the benchmark is running. */
	public boolean tick() {
		if (sampleStart < 0) {
			return false;
		}
		if (System.currentTimeMillis() - sampleStart >= SAMPLE_MS) {
			if (samplingBefore) {
				beforeAvg = FpsTracker.get().avgFps();
				beforeLow = FpsTracker.get().low1Pct();
				samplingBefore = false;
				sampleStart = System.currentTimeMillis();
				PerformanceModule perf = PerformanceAccess.perf();
				if (perf != null) {
					perf.setEnabled(true); // phase 2 = boost on
				}
			} else {
				afterAvg = FpsTracker.get().avgFps();
				afterLow = FpsTracker.get().low1Pct();
				sampleStart = -1;
				return false;
			}
		}
		return true;
	}

	public boolean running() {
		return sampleStart >= 0;
	}

	public float beforeAvg() {
		return beforeAvg;
	}

	public float beforeLow() {
		return beforeLow;
	}

	public float afterAvg() {
		return afterAvg;
	}

	public float afterLow() {
		return afterLow;
	}

	public float avgGain() {
		return beforeAvg > 0 ? (afterAvg - beforeAvg) / beforeAvg * 100f : 0f;
	}

	public float lowGain() {
		return beforeLow > 0 ? (afterLow - beforeLow) / beforeLow * 100f : 0f;
	}
}
