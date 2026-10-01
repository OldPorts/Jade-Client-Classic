package com.jadeclient.perf;

import com.jadeclient.ModuleRegistrar;
import com.jadeclient.core.Module;
import com.jadeclient.core.ModuleManager;

/**
 * jade-performance bootstrap: wires the perf bridge to the module manager
 * once modules are registered. On Forge the actual hooks live in
 * {@code com.jadeclient.forge.JadeForgeEvents}; this only supplies settings.
 */
public final class PerformanceInit {

	private PerformanceInit() {
	}

	public static void install(final ModuleManager manager) {
		// If JadeClient.boot already ran this executes immediately, otherwise
		// it queues until fireAfterModules. Either way the bridge is wired
		// exactly once modules exist.
		ModuleRegistrar.runAfterModules(new java.util.function.Consumer<ModuleManager>() {
			@Override
			public void accept(ModuleManager ignored) {
				JadePerfBridge.install(
					new java.util.function.Supplier<Boolean>() {
						@Override
						public Boolean get() {
							Module m = manager.byId("fps_boost");
							return m instanceof PerformanceModule && m.isEnabled();
						}
					},
					new java.util.function.Supplier<PerformanceModule>() {
						@Override
						public PerformanceModule get() {
							Module m = manager.byId("fps_boost");
							return m instanceof PerformanceModule ? (PerformanceModule) m : null;
						}
					});
			}
		});
	}
}
