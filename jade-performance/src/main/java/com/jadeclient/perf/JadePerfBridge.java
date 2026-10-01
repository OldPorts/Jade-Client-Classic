package com.jadeclient.perf;

import java.util.function.Supplier;

/**
 * Decouples jade-performance from the root entrypoint. jade-ui (or the
 * root) installs an implementation at startup; until then perf features
 * stay dormant, which also makes this module safely testable in isolation.
 */
public final class JadePerfBridge {

	private JadePerfBridge() {
	}

	private static Supplier<Boolean> enabledLookup = () -> false;
	private static Supplier<PerformanceModule> moduleLookup = () -> null;

	public static void install(Supplier<Boolean> enabled, Supplier<PerformanceModule> module) {
		enabledLookup = enabled;
		moduleLookup = module;
	}

	public static boolean fpsBoostEnabled() {
		try {
			return enabledLookup.get();
		} catch (Exception e) {
			return false;
		}
	}

	public static PerformanceModule module() {
		try {
			return moduleLookup.get();
		} catch (Exception e) {
			return null;
		}
	}
}
