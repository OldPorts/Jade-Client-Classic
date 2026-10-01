package com.jadeclient.ui;

import com.jadeclient.core.Module;
import com.jadeclient.perf.PerformanceModule;

/**
 * jade-ui client helpers. Tick/key/render wiring lives in
 * {@link JadeForgeEvents}; this class keeps UI-side helpers such as the
 * onboarding preset shortcut.
 */
public final class JadeClientClient {

	private JadeClientClient() {
	}

	/** Preset helper used by the onboarding flow. */
	public static void applyPreset(String preset) {
		Module raw = com.jadeclient.JadeClient.byId("fps_boost");
		if (raw instanceof PerformanceModule) {
			PerformanceModule perf = (PerformanceModule) raw;
			perf.applyPreset(preset);
			perf.setEnabled(true);
		}
	}
}
