package com.jadeclient;

import com.jadeclient.config.ConfigSpi;
import com.jadeclient.config.JadeConfig;
import com.jadeclient.config.JadeConfigImpl;
import com.jadeclient.core.Module;
import com.jadeclient.core.ModuleManager;
import com.jadeclient.core.event.EventBridge;
import com.jadeclient.core.event.EventBus;
import com.jadeclient.core.spi.ConfigSpiImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * Jade Client bootstrap (platform-neutral part). The Forge entrypoint
 * {@code com.jadeclient.forge.JadeForgeMod} calls {@link #boot(File)} during
 * {@code FMLInitializationEvent}; this class owns the module registry,
 * the event bridge and the config store.
 *
 * Boot order:
 * 1. register all modules (via ServiceLoader, so features live in their own
 *    source trees)
 * 2. register the event bridge
 * 3. load config (applies persisted settings + enabled states)
 */
public final class JadeClient {

	public static final String MOD_ID = "jadeclient";
	public static final String VERSION = "1.8.9-release0.1";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	private static ModuleManager moduleManager;
	private static boolean booted;

	private JadeClient() {
	}

	public static synchronized void boot(File gameDir) {
		if (booted) {
			return;
		}
		booted = true;

		moduleManager = new ModuleManager();
		ModuleRegistrar.registerAll(moduleManager);
		ModuleRegistrar.fireAfterModules(moduleManager);

		EventBridge.register();
		moduleManager.init();

		// Re-install the config store with the real SPI, then load state.
		ConfigSpi spi = new ConfigSpiImpl(moduleManager);
		new JadeConfigImpl(configRoot(gameDir), spi).load();

		int count = moduleManager.modules().size();
		LOGGER.info("Jade Client {} ready - {} modules registered", VERSION, count);
	}

	private static java.nio.file.Path configRoot(File gameDir) {
		return gameDir.toPath().resolve("jade");
	}

	public static ModuleManager modules() {
		return moduleManager;
	}

	public static Module byId(String id) {
		return moduleManager == null ? null : moduleManager.byId(id);
	}

	/** Resource path helper (1.8.9 uses plain domain:path strings). */
	public static String id(String path) {
		return MOD_ID + ":" + path;
	}
}
