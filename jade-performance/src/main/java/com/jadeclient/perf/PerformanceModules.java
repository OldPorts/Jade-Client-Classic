package com.jadeclient.perf;

import com.jadeclient.ModuleRegistrar.ModuleProvider;
import com.jadeclient.core.ModuleManager;

/** Registers all jade-performance modules. */
public final class PerformanceModules implements ModuleProvider {

	@Override
	public void register(ModuleManager manager) {
		manager.register(new PerformanceModule());
	}
}
