package com.jadeclient;

import com.jadeclient.core.ModuleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Consumer;

/**
 * Discovers modules from every Jade gradle module via Java ServiceLoader.
 * Each library module ships a META-INF/services entry, so adding a new
 * feature module never requires touching this file.
 */
public final class ModuleRegistrar {

	private ModuleRegistrar() {
	}

	public static void registerAll(ModuleManager manager) {
		ServiceLoader<ModuleProvider> loader = ServiceLoader.load(ModuleProvider.class);
		for (ModuleProvider provider : loader) {
			try {
				provider.register(manager);
			} catch (Exception e) {
				JadeClient.LOGGER.error("Module provider {} failed to register", provider.getClass().getName(), e);
			}
		}
	}

	/** Implemented once per gradle module. */
	public interface ModuleProvider {
		void register(ModuleManager manager);
	}

	private static final List<Consumer<ModuleManager>> afterModulesHooks = new ArrayList<>();
	private static ModuleManager registeredManager;

	/**
	 * Runs the hook once modules are registered (immediately if they already
	 * are). Lets library modules wire bridges without depending on the root
	 * entrypoint.
	 */
	public static void runAfterModules(Consumer<ModuleManager> hook) {
		if (registeredManager != null) {
			hook.accept(registeredManager);
		} else {
			afterModulesHooks.add(hook);
		}
	}

	/** Called by the root entrypoint after registerAll completes. */
	public static void fireAfterModules(ModuleManager manager) {
		registeredManager = manager;
		for (Consumer<ModuleManager> hook : afterModulesHooks) {
			hook.accept(manager);
		}
		afterModulesHooks.clear();
	}
}
