package com.jadeclient.perf.culling;

import com.jadeclient.perf.PerformanceAccess;
import com.jadeclient.perf.PerformanceModule;

/**
 * Per-frame culling scratch space, populated by the level renderer mixin
 * and consulted by HUD/benchmark reporting.
 */
public final class CullingState {

	private CullingState() {
	}

	private static volatile boolean frameActive;
	private static volatile boolean occlusionEnabled;
	private static volatile int sectionsCulled;

	public static void beginFrame() {
		frameActive = true;
		sectionsCulled = 0;
		PerformanceModule perf = PerformanceAccess.perf();
		occlusionEnabled = perf != null && perf.occlusionCulling.get();
	}

	public static void endFrame() {
		frameActive = false;
	}

	public static boolean active() {
		return frameActive;
	}

	public static boolean occlusionEnabled() {
		return occlusionEnabled;
	}

	public static void recordCulled() {
		sectionsCulled++;
	}

	public static int sectionsCulledThisFrame() {
		return sectionsCulled;
	}
}
